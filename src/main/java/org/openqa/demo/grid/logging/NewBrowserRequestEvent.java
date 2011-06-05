package org.openqa.demo.grid.logging;

import org.openqa.selenium.remote.DesiredCapabilities;

public class NewBrowserRequestEvent extends SeleniumGridEvent {

	public NewBrowserRequestEvent(DesiredCapabilities cap) {
		super(cap);
	}

	public String toString() {
		return "new test " + super.toString();
	}

}
