package org.openqa.demo.grid.logging;

import org.openqa.selenium.remote.DesiredCapabilities;

public class TimeoutEvent extends SeleniumGridEvent {

		
	public TimeoutEvent(DesiredCapabilities cap) {
		super(cap);
	}

	public String toString() {
		return "timeout " + super.toString() ;
	}

}
