package com.test.dpg.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EventLogDao {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	private String dropTempTable = "drop table if exists %s";

	/**
	 * Drops the provided table if exists in db
	 * 
	 * @param tableName
	 */
	public void dropTable(String tableName) {
		String query = String.format(dropTempTable, tableName);
		jdbcTemplate.execute(query);
	}
	
	private String insertIntoAlertEvents = "insert into event_details (id, duration, type, host, alert) " + 
			"select start.id, (finish.timestamp - start.timestamp) as duration, start.type, start.host, case when (finish.timestamp - start.timestamp) > 4 then 1 else 0 end as alert from " + 
			"(select * from %s where state = 'STARTED') start " + 
			"join " + 
			"(select * from %s where state = 'FINISHED') finish " + 
			"on start.id = finish.id";

	/**
	 * Inserts all logged events from temp table to EVENT_DETAILS table with alert flag
	 * 
	 * @param tempTable
	 */
	public void insertAlertEvents(String tempTable) {
		jdbcTemplate.update(String.format(insertIntoAlertEvents, tempTable, tempTable));
	}

}
