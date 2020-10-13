package com.test.dpg.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.dpg.models.EventDetails;
import com.test.dpg.models.EventLog;
import com.test.dpg.repositories.EventDetailsRepository;
import com.test.dpg.repositories.EventLogDao;
import com.test.dpg.service.workers.InsertBulkEventLogWorker;

@Service
public class EventServiceImpl implements EventService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EventLogDao dao;
	
	@Autowired
	private EventDetailsRepository repository;
	
	@Value("${MAX_THREAD_POOL_SIZE:10}")
	private String MAX_THREAD_POOL_SIZE;

	@Override
	public List<EventDetails> logEventsFromFile(final String fileName) throws FileNotFoundException {
		// All data from logFile will be stored in a temp db table, after the operation, temp table will be dropped
		// This will consume more time for extra db operations, but it will surely handle large files with millions of records
		// This temp table name should be unique to each request, so that we can drop the temp table after the operation
		// To maintain the uniqueness timestamp is appended to the temp table name, considering 2 requests will never come at same instant
		// In practical scenario timestamp should be replaced by requestId/orderId - which is unique to each request
		String tempTable = "event_info_" + System.currentTimeMillis();
		// Using java.util.Scanner to read line-by-line
		// This will consume very less memory compared to loading the whole file content at once
		InputStream inputStream = null;
		Scanner sc = null;
		try {
			inputStream = new FileSystemResource(fileName).getInputStream();
			sc = new Scanner(inputStream, "UTF-8");
			final ObjectMapper mapper = new ObjectMapper();
			final ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(MAX_THREAD_POOL_SIZE));
			final List<Object[]> logs = new ArrayList<>();
			final List<Future<Integer>> futures = new ArrayList<>();
			while (sc.hasNextLine()) {
				final String line = sc.nextLine();
				try {
					final EventLog event = mapper.readValue(line, EventLog.class);
					System.out.println(event);
					logs.add(new Object[] { event.getHost(), event.getId(), event.getState(), event.getTimestamp(),
							event.getType(), event.getUuid() });
					// Doing batch inserts(max 200 rows at a time) for faster execution
					if (logs.size() == 200) {
						// batch insert operations are executed in new Callable thread, so that the main file processing does not wait
						futures.add(executor.submit(new InsertBulkEventLogWorker(jdbcTemplate, tempTable, new ArrayList<>(logs))));
						logs.clear();
					}
				} catch (final JsonProcessingException e) {
					LOGGER.warn("Unable to parse log: " + line, e);
				}
			}
			if (CollectionUtils.isNotEmpty(logs)) {
				futures.add(executor.submit(new InsertBulkEventLogWorker(jdbcTemplate, tempTable, logs)));
			}
			// using callable.get() method to ensure all threads has finished batch inserts
			futures.parallelStream().forEach(t -> {
				try {
					final Integer processed = t.get();
					LOGGER.debug("Inserted " + processed + " rows in " + tempTable + " table.");
				} catch (InterruptedException | ExecutionException e) {
					LOGGER.error("Failed to store some logs in db temp table", t);
				}
			});
			// Calculate alert flag and insert into main table
			dao.insertAlertEvents(tempTable);
		} catch (final IOException e1) {
			throw new FileNotFoundException("File '" + fileName + "' not found!");
		} finally {
			// Drop the temp table when all operations are done
			dao.dropTable(tempTable);
			if (sc != null)
				sc.close();
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (final IOException e) {
				// Suppressing exception
			}
		}
		return getLoggedEvents();
	}

	@Override
	public List<EventDetails> getLoggedEvents() {
		return repository.findAll();
	}

	@Override
	public String deleteLoggedEvents() {
		repository.deleteAll();
		return "Successfully deleted all events";
	}

}
