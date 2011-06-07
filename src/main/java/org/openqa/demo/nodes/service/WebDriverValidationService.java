package org.openqa.demo.nodes.service;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.demo.node.WebDriverNodeConfigServlet;
import org.openqa.grid.internal.exception.GridException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Test;

import com.opera.core.systems.OperaDriver;

public class WebDriverValidationService {

	public DesiredCapabilities validate(int port, DesiredCapabilities cap) {
		URL url = null;
		try {
			url = new URL("http://localhost:" + port + "/wd/hub");
		} catch (MalformedURLException e) {
			new GridException("Cannot create the URL for the node" + e.getMessage());
		}
		RemoteWebDriver driver=null;
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
