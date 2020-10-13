package com.test.dpg.controller;

import java.io.FileNotFoundException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.test.dpg.models.EventDetails;
import com.test.dpg.service.EventService;

@RestController
@RequestMapping(path = "/events")
public class EventController {
	
	@Autowired
	private EventService service;
	
	@GetMapping
	public List<EventDetails> getAllEvents() {
		return service.getLoggedEvents();
	}
	
	@DeleteMapping
	public String deleteAllEvents() {
		return service.deleteLoggedEvents();
	}
	
	@PostMapping(path = "/storeLogFileEvents")
	public List<EventDetails> logEvents(@RequestParam String fileName) throws FileNotFoundException {
		return service.logEventsFromFile(fileName);
	}
	
}
