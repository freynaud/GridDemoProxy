package org.openqa.demo.node;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Platform;
import org.openqa.selenium.browserlaunchers.locators.Firefox3Locator;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import bsh.Capabilities;

import com.google.common.io.ByteStreams;

public class WebDriverNodeConfigServlet extends HttpServlet {

	private Set<File> checkedFolder = new HashSet<File>();

	private List<DesiredCapabilities> firefoxes = new ArrayList<DesiredCapabilities>();

	private static final Logger log = Logger.getLogger(WebDriverNodeConfigServlet.class.getName());

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			process(request, response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			process(request, response);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected void process(HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(200);

		StringBuilder builder = new StringBuilder();

		if ("POST".equalsIgnoreCase(request.getMethod())) {
			String completion = request.getParameter("completion");
			String submit = request.getParameter("submit");
			String current = request.getParameter("current");

			if (completion != null) {
				String sep = System.getProperty("file.separator");
				String[] pieces = completion.split(sep);
				if (pieces.length == 0) {
					throw new RuntimeException();
				}
				StringBuilder b = new StringBuilder();
				b.append(sep);

				for (int i = 0; i < (pieces.length - 1); i++) {
					b.append(pieces[i]).append(sep);
				}
				File folder = new File(b.toString());
				if (!folder.exists()) {
					throw new RuntimeException();
				}
				final String last = pieces[pieces.length - 1];

				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.startsWith(last);
					}
				};
				String[] children = folder.list(filter);
				if (children.length == 0) {
					throw new RuntimeException();
				}
				if (children.length == 1) {
					builder.append(new File(folder, children[0]).getAbsolutePath());
				} else {
					List<String> names = Arrays.asList(children);
					String common = findCommonStart(names, last);
					builder.append(new File(folder, common).getAbsolutePath());

				}

			} else if (submit != null) {
				int added = processPath(submit);

				// return the new list.
				builder.append("<ul>");
				for (DesiredCapabilities capability : firefoxes) {
					builder.append("<li>");
					builder.append(capability);
					builder.append("</li>");
				}
			} else if (current != null) {
				File f = new File(current);
				if (!f.exists()) {
					JSONObject res = new JSONObject();
					res.put("success", false);
					res.put("info", f + " is not a valid file");
					builder.append(res.toString());
				} else if (!f.isFile()) {
					JSONObject res = new JSONObject();
					res.put("success", false);
					res.put("info", f + " is a folder.You need to specify a file.");
					builder.append(res.toString());
				} else {
					int added = processPath(current);
					if (added == 0) {
						JSONObject res = new JSONObject();
						res.put("success", false);
						res.put("info", f + " isn't a new browser for that node.");
						builder.append(res.toString());
					} else {
						JSONObject res = new JSONObject();
						res.put("success", false);
						res.put("info", "Woot." + added + " new browsers found");
						// return the new list.

						StringBuilder msg = new StringBuilder();
						msg.append("<ul>");
						for (DesiredCapabilities capability : firefoxes) {
							msg.append("<li>");
							msg.append(capability);
							msg.append("</li>");
						}
						res.put("content", msg.toString());
						builder.append(res.toString());
					}
				}
			}
		} else {
			builder.append("<html>");
			builder.append("<head>");

			builder.append("<script src='http://ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.min.js'></script>");
			builder.append("<script src='resources/NodeConfig.js'></script>");
			builder.append("<title>WebDriver node config</title>");

			builder.append("<div id='ffs'>");
			builder.append("<ul>");
			for (DesiredCapabilities capability : firefoxes) {
				builder.append("<li>");
				builder.append(capability);
				builder.append("</li>");
			}
			builder.append("</div>");

			builder.append("<input id='firefox' size='50'>");
			builder.append("<div id='info' >");
			builder.append("</ul>");
			builder.append("</body>");
			builder.append("</html>");
		}

		InputStream in = new ByteArrayInputStream(builder.toString().getBytes("UTF-8"));
		try {
			ByteStreams.copy(in, response.getOutputStream());
		} finally {
			in.close();
			response.getOutputStream().close();
		}
	}

	private int processPath(String path) {
		int origin = firefoxes.size();
		// try to add the new ones
		File more = new File(path);
		String exeName = more.getName();
		File folder = new File(more.getParent());
		File parent = new File(folder.getParent());

		List<File> folders = guessInstallsInFolder(parent);
		for (File f : folders) {
			File exe = new File(f, exeName);
			DesiredCapabilities c = discoverFirefoxCapability(exe);
			if (c != null) {
				addNewFirefoxInstall(c);
			}
		}
		int delta = firefoxes.size() - origin;
		return delta;
	}

	private String findCommonStart(List<String> names, String last) {
		int i = last.length();
		String lastok = last;

		String first = names.get(0);

		while (i < first.length()) {
			String commonPrefix = first.substring(0, i);
			for (String s : names) {
				if (s.length() < i) {
					return lastok;
				}
				String prefix = s.substring(0, i);
				if (!commonPrefix.equals(prefix)) {
					return lastok;
				}
			}
			lastok = commonPrefix;
			i++;

		}
		return lastok;

	}

	private void addNewFirefoxInstall(DesiredCapabilities cap) {
		for (DesiredCapabilities c : firefoxes) {
			if (c.getCapability(FirefoxDriver.BINARY).equals(cap.getCapability(FirefoxDriver.BINARY))) {
				return;
			}
		}
		firefoxes.add(cap);
	}

	/**
	 * get all the info possible about this firefox install : OS, version ( from
	 * application.ini )
	 * 
	 * @param exe
	 *            the firefox executable
	 * @return a DesiredCapability with everything filed in
	 */
	private DesiredCapabilities discoverFirefoxCapability(File exe) {
		DesiredCapabilities ff = DesiredCapabilities.firefox();

		if (!exe.exists()) {
			throw new RuntimeException("Cannot find " + exe);
		}

		ff.setCapability(FirefoxDriver.BINARY, exe.getAbsolutePath());
		ff.setCapability(CapabilityType.PLATFORM, Platform.getCurrent());
		String p = exe.getParent();
		File appIni = new File(p, "application.ini");

		if (!appIni.exists()) {
			log.warning("corrupted install ? cannot find " + appIni.getAbsolutePath());
		}
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(appIni));
			String version = prop.getProperty("Version");
			if (version == null) {
				log.warning("corrupted install ? cannot find Version in " + appIni.getAbsolutePath());
			}
			ff.setVersion(version);
		} catch (Exception e) {
			log.warning("corrupted install ? " + e.getMessage());
		}
		return ff;
	}

	/**
	 * iterate all the folder in the folder passed as a param, and return all
	 * the folders that could be firefox installs.
	 * 
	 * @param folder
	 *            the folder to look into
	 * @return a list of folder that could be firefox install folders.
	 */
	private List<File> guessInstallsInFolder(File folder) {
		List<File> possibleMatch = new ArrayList<File>();

		if (!folder.isDirectory()) {
			return possibleMatch;
		}
		for (File f : folder.listFiles()) {
			if (f.isDirectory() && f.getName().toLowerCase().contains("firefox")) {
				possibleMatch.add(f);
			}
		}
		return possibleMatch;
	}

}
