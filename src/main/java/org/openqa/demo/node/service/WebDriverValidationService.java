package org.openqa.demo.node.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.demo.node.WebDriverNodeConfigServlet;
import org.openqa.grid.internal.exception.GridException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * run simple test to check the settings.
 * 
 * @author freynaud
 * 
 */
public class WebDriverValidationService {

	/**
	 * run a test to see that everything is properly setup, and get a more
	 * accurate desiredCapabilities
	 * 
	 * @param port
	 * @param cap
	 * @return
	 */
	public DesiredCapabilities validate(int port, DesiredCapabilities cap) {
		URL url = null;
		try {
			url = new URL("http://localhost:" + port + "/wd/hub");
		} catch (MalformedURLException e) {
			new GridException("Cannot create the URL for the node" + e.getMessage());
		}
		RemoteWebDriver driver = null;
		try {
			driver = new RemoteWebDriver(url, cap);
			driver.get("http://localhost:" + port + "/extra/WebDriverNodeConfigServlet");
			String title = driver.getTitle();
			if (!WebDriverNodeConfigServlet.PAGE_TITLE.equals(title)) {
				throw new GridException("Couldn't validate the install for " + cap);
			}
			DesiredCapabilities res = new DesiredCapabilities(driver.getCapabilities().asMap());
			return res;
		} catch (Exception e) {
			throw new GridException("Not working " + e.getMessage());
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}
	}
}
