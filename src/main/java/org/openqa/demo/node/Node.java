package org.openqa.demo.node;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.demo.nodes.service.BrowserFinderUtils;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.selenium.utils.WebDriverJSONConfigurationUtils;
import org.openqa.selenium.Platform;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

public class Node {

	private static final Logger log = Logger.getLogger(Node.class.getName());
	private BrowserFinderUtils finder = new BrowserFinderUtils();
	private Map<String, String> errorPerBrowser = new HashMap<String, String>();

	private Platform platform = Platform.getCurrent();
	private int port = -1;
	private URL registrationURL;
	private File backup = new File("node.json");
	private List<DesiredCapabilities> capabilities = new ArrayList<DesiredCapabilities>();
	private Map<String, Object> configuration = new HashMap<String, Object>();

	private URL hub;

	public Node() {
		init();
	}

	private void init() {

		try {
			hub = new URL("http://localhost:4444");
		} catch (MalformedURLException e1) {
			// shouldnt happen
			throw new RuntimeException("impossible");
		}

		try {
			capabilities.add(finder.getDefaultIEInstall());
		} catch (Throwable e) {
			errorPerBrowser.put("internet eplorer", e.getMessage());
		}
		try {
			capabilities.add(finder.getDefaultFirefoxInstall());
		} catch (Throwable e) {
			errorPerBrowser.put("firefox", e.getMessage());
		}
		try {
			capabilities.add(finder.getDefaultChromeInstall());
		} catch (Throwable e) {
			errorPerBrowser.put("chrome", e.getMessage());
		}

	}

	public void clearAll() {
		capabilities.clear();
		configuration.clear();
		errorPerBrowser.clear();
		
	}
	public void reset() {
		capabilities.clear();
		configuration.clear();
		errorPerBrowser.clear();
		init();
	}

	public File getBackupFile() {
		return backup;
	}

	public void setBackupFile(File backup) {
		this.backup = backup;
	}

	public void load() throws IOException, JSONException {
		clearAll();
		
		JSONObject object = WebDriverJSONConfigurationUtils.loadJSON(backup.getCanonicalPath());
		JSONArray caps = object.getJSONArray("capabilities");

		for (int i = 0; i < caps.length(); i++) {
			DesiredCapabilities c = new DesiredCapabilities();
			JSONObject cap = caps.getJSONObject(i);
			for (Iterator iterator = cap.keys(); iterator.hasNext();) {
				String key = (String) iterator.next();
				c.setCapability(key, cap.get(key));
			}
			capabilities.add(c);
		}

		JSONObject conf = object.getJSONObject("configuration");
		for (Iterator iterator = conf.keys(); iterator.hasNext();) {
			String key = (String) iterator.next();
			configuration.put(key, conf.get(key));
		}
	}

	public JSONObject getJSON() {
		try {
			JSONObject res = new JSONObject();

			// capabilities
			JSONArray caps = new JSONArray();
			for (DesiredCapabilities cap : capabilities) {
				JSONObject c = new JSONObject();
				for (String key : cap.asMap().keySet()) {
					c.put(key, cap.getCapability(key));
				}
				caps.put(c);
			}

			// configuration
			JSONObject c = new JSONObject();
			for (String key : configuration.keySet()) {
				c.put(key, configuration.get(key));
			}
			c.put("hub", getHubURL());
			
			res.put("capabilities", caps);
			res.put("configuration", c);
			return res;
		} catch (JSONException e) {
			throw new RuntimeException("Bug. " + e.getMessage());
		}
	}

	public void save() throws IOException {
		JSONObject node = getJSON();
		if (backup == null) {
			throw new RuntimeException("Cannot save the config. File not specified.");
		}
		BufferedWriter out = new BufferedWriter(new FileWriter(backup.getAbsolutePath()));
		out.write(node.toString());
		out.close();

	}

	public URL getRegistrationURL() {
		return registrationURL;
	}

	public void setRegistrationURL(URL registrationURL) {
		this.registrationURL = registrationURL;
	}

	public List<DesiredCapabilities> getCapabilities() {
		return capabilities;
	}

	public Map<String, Object> getConfiguration() {
		return configuration;
	}

	public boolean addNewBrowserInstall(DesiredCapabilities cap) {
		// 2 firefox installs are equal if the point to the same exe.
		if ("firefox".equals(cap.getBrowserName())) {
			for (DesiredCapabilities c : getCapabilities()) {
				String path = (String) c.getCapability(FirefoxDriver.BINARY);
				if (path != null && path.equals(cap.getCapability(FirefoxDriver.BINARY))) {
					return false;
				}
			}
			capabilities.add(cap);
			return true;
		} else {
			throw new RuntimeException("NI");
		}
	}

	public Map<String, String> getErrorPerBrowser() {
		return errorPerBrowser;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Platform getPlatform() {
		return platform;
	}

	public void setHubURL(URL hubUrl) {
		this.hub = hubUrl;
	}

	public URL getHubURL() {
		return hub;
	}
}
