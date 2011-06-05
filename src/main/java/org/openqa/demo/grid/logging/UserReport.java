package org.openqa.demo.grid.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserReport {



	private String user;
	private Map<SeleniumGridEvent, Integer> eventscount = new ConcurrentHashMap<SeleniumGridEvent, Integer>();

	@SuppressWarnings("unused")
	private UserReport() {

	}

	public UserReport(String user) {
		this.user = user;
	}

	public synchronized int addEvent(SeleniumGridEvent event) {
		System.out.println("Adding event "+event);
		Integer count = eventscount.get(event) == null ? 0 : eventscount.get(event);
		count++;
		eventscount.put(event, count);
		return count;

	}
	
	public Map<SeleniumGridEvent, Integer> getEventscount() {
		return eventscount;
	}

	

}
