package com.test.dpg.service;

import java.io.FileNotFoundException;
import java.util.List;

import com.test.dpg.models.EventDetails;

public interface EventService {

	/**
	 * Process all events from provided log file and store them in db
	 * 
	 * @param fileName
	 * @return All events after the operation
	 * @throws FileNotFoundException if provided fileName is not valid
	 */
	List<EventDetails> logEventsFromFile(String fileName) throws FileNotFoundException;

	/**
	 * Fetches all logged events from db
	 * @return List of {@link EventDetails}
	 */
	List<EventDetails> getLoggedEvents();

	/**
	 * Deletes all stored events from event_details db table
	 * @return successMessage
	 */
	String deleteLoggedEvents();

}
