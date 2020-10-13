package com.test.dpg.service.workers;

import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.test.dpg.service.EventServiceImpl;

public class InsertBulkEventLogWorker implements Callable<Integer> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EventServiceImpl.class);
	
	private JdbcTemplate jdbcTemplate; 
	private String tableName; 
	private List<Object[]> logs;
	
	public InsertBulkEventLogWorker(JdbcTemplate jdbcTemplate, String tableName, List<Object[]> logs) {
		this.jdbcTemplate = jdbcTemplate;
		this.tableName = tableName;
		this.logs = logs;
	}
	
	private String createTempTable = "create table if not exists %s (uuid varchar(255) not null, host varchar(255), id varchar(255), state varchar(255), timestamp bigint not null, type varchar(255), primary key (uuid))";
	
	private String insertIntoTempTable = "insert into %s (host, id, state, timestamp, type, uuid) values (?, ?, ?, ?, ?, ?)";

	@Override
	public Integer call() throws Exception {
		LOGGER.debug("Inserting into temp table " + tableName + " to store all events for processing");
		String query = String.format(createTempTable, tableName);
		LOGGER.trace("Creating temp table if not created already. Query: " + query);
		jdbcTemplate.execute(query);
		int[] batchUpdate = jdbcTemplate.batchUpdate(String.format(insertIntoTempTable, tableName), logs);
		LOGGER.debug("Batch insert into temp table result" + batchUpdate);
		return logs.size();
	}
	
}
