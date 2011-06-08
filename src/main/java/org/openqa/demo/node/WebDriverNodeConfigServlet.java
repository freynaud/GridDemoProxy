package org.openqa.demo.node;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.demo.nodes.service.BrowserFinderUtils;
import org.openqa.demo.nodes.service.FileSystemAjaxService;
import org.openqa.demo.nodes.service.HubUtils;
import org.openqa.demo.nodes.service.WebDriverValidationService;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.exception.GridException;
import org.openqa.grid.web.utils.BrowserNameUtils;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.io.ByteStreams;

public class WebDriverNodeConfigServlet extends HttpServlet {

	private static final long serialVersionUID = 7490344466454529896L;
	private Node node = new Node();
	private FileSystemAjaxService service = new FileSystemAjaxService();
	private BrowserFinderUtils browserUtils = new BrowserFinderUtils();
	private WebDriverValidationService wdValidator = new WebDriverValidationService();

	public final static String PAGE_TITLE = "WebDriver node config";

	private static final Logger log = Logger.getLogger(WebDriverNodeConfigServlet.class.getName());

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (node.getPort() == -1) {
			int port = request.getServerPort();
			node.setPort(port);
		}
		String page = getPage();
		write(page, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {
			JSONObject ajax = processAjax(request, response);
			write(ajax.toString(), response);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private JSONObject processAjax(HttpServletRequest request, HttpServletResponse response) throws JSONException {

		String status = request.getParameter("status");
		if (status != null) {
			JSONObject o = new JSONObject();
			String url = request.getParameter("url");
			URL hubUrl;
			try {
				hubUrl = new URL(url);
				node.setHubURL(hubUrl);
			} catch (MalformedURLException e) {
				o.put("success", false);
				o.put("hub_satus_icon.src", Icon.ALERT.path());
				o.put("hub_satus_icon.title", url + " is not a valid url");
				o.put("hubInfo", url + " is not a valid url");
				return o;
			}

			HubUtils hubUtils = new HubUtils(node.getHubURL().getHost(), node.getHubURL().getPort());
			boolean ok = hubUtils.isHubReachable();

			o.put("success", ok);
			if (ok) {
				o.put("hub_satus_icon.src", Icon.CLEAN.path());
				o.put("hub_satus_icon.title", "/extra/resources/clean.png");
				o.put("hubInfo", "hub up and waiting for reg request.");
			} else {
				o.put("hub_satus_icon.src", Icon.ALERT.path());
				o.put("hub_satus_icon.title", "Cannot contact " + hubUtils.getUrl());
				o.put("hubInfo", "Cannot contact " + hubUtils.getUrl());
			}

			return o;
		}

		String reset = request.getParameter("reset");
		if (reset != null) {
			node.reset();
			JSONObject o = new JSONObject();
			o.put("success", true);
			o.put("resetFB", "");
			o.put("capabilities", getCapabilitiesDiv());
			o.put("configuration", getConfigurationDiv());
			return o;
		}

		String update = request.getParameter("update");
		if (update != null) {
			for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
				String p = (String) e.nextElement();
				if (p.startsWith("capabilities")) {
					String value = request.getParameter(p);
					String[] pieces = p.split("\\.");
					int capIndex = Integer.parseInt(pieces[1]);
					String capKey = pieces[2];
					String capValue = value;
					node.getCapabilities().get(capIndex).setCapability(capKey, cast(capValue));
				} else if (p.startsWith("configuration")) {
					String value = request.getParameter(p);
					String configKey = value.split("\\.")[1];
					String configValue = value;
					node.getConfiguration().put(configKey, cast(configValue));
				} else {
					System.out.println("? : " + p);
				}

			}

			JSONObject o = new JSONObject();
			o.put("success", true);
			o.put("resetFB", "");
			o.put("capabilities", getCapabilitiesDiv());
			o.put("configuration", getConfigurationDiv());
			return o;
		}

		String typed = request.getParameter("completion");
		if (typed != null) {
			return service.complete(typed);
		}

		String proposedPath = request.getParameter("submit");
		if (proposedPath != null) {
			JSONObject o = seekBrowsers(proposedPath);
			return o;
		}

		String load = request.getParameter("load");
		if (load != null) {
			JSONObject o = new JSONObject();
			try {
				node.load();
				o.put("success", true);
				o.put("loadFB", "Great success!");
				o.put("capabilities", getCapabilitiesDiv());
				o.put("configuration", getConfigurationDiv());

				return o;
			} catch (IOException e) {
				o.put("success", false);
				o.put("loadFB", ":( " + e.getMessage());
				return o;
			}

		}
		String save = request.getParameter("save");
		if (save != null) {
			JSONObject o = new JSONObject();
			try {
				node.save();
				o.put("success", true);
				o.put("saveFB", "Great success! Config saved in " + node.getBackupFile().getAbsolutePath());
				return o;
			} catch (IOException e) {
				o.put("success", false);
				o.put("saveFB", ":( " + e.getMessage());
				return o;
			}

		}

		String current = request.getParameter("current");
		if (current != null) {
			JSONObject o = service.seemsValid(current);
			return o;
		}
		String index = request.getParameter("validate");
		if (index != null) {
			int i = Integer.parseInt(index);
			DesiredCapabilities c = node.getCapabilities().get(i);
			JSONObject o = new JSONObject();
			try {
				c.setCapability("valid", "running");
				DesiredCapabilities realCap = wdValidator.validate(node.getPort(), c);
				o.put("success", true);
				BrowserFinderUtils.updateGuessedCapability(c, realCap);
				o.put("info", "Success !");
				c.setCapability("valid", "true");
			} catch (GridException e) {
				o.put("success", false);
				c.setCapability("valid", "false");
				c.setCapability("error", e.getMessage());
				o.put("info", e.getMessage());

			}
			o.put("capabilities", getCapabilitiesDiv());
			return o;
		}

		return null;
	}

	/**
	 * try to guess the type.If it looks like an int, return an int.
	 * 
	 * @param capValue
	 * @return
	 */
	private Object cast(String capValue) {
		try {
			return Integer.parseInt(capValue);
		} catch (Throwable e) {
			return capValue;
		}
	}

	private void write(String content, HttpServletResponse response) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(200);

		InputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));
		try {
			ByteStreams.copy(in, response.getOutputStream());
		} finally {
			in.close();
			response.getOutputStream().close();
		}
	}

	private String getPage() {
		StringBuilder builder = new StringBuilder();
		builder.append("<html>");

		builder.append("<head>");

		builder.append("<script src='http://ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.min.js'></script>");
		builder.append("<script src='resources/NodeConfig.js'></script>");
		builder.append("<link rel='stylesheet' type='text/css' href='resources/NodeConfig.css' />");
		builder.append("<title>" + PAGE_TITLE + "</title>");
		builder.append("</head>");

		builder.append("<body>");

		builder.append("<div class='error' >");
		for (String browser : node.getErrorPerBrowser().keySet()) {
			builder.append(browser + " : " + node.getErrorPerBrowser().get(browser) + "</br>");
		}

		// Platform
		builder.append("</div>");

		String iconp = "?";
		switch (node.getPlatform()) {
		case LINUX:
			iconp = "tux.png";
			break;
		case MAC:
			iconp = "mac.png";
			break;
		case WINDOWS:

			break;
		default:
			break;
		}
		builder.append("<b>Platform :</b> <img  src='/extra/resources/" + iconp + "' title='" + node.getPlatform() + "'  ></br></br>");

		// hub
		builder.append("<b>Part of the grid : </b>");
		builder.append(" <input id='hub_url' size='40' value='http://localhost:4444' >");
		builder.append("<img ' id='hub_satus_icon' src='" + Icon.NOT_SURE.path() + "' >");

		builder.append("<div id ='hubInfo' ><i>edit the url to point to another hub.</i></div></br>");

		// capabilities
		builder.append(getCapabilitiesDiv());

		builder.append("</div></br>");

		// discover more browsers manually
		builder.append("<b>Add more capabilities :</b></br>");
		builder.append("<input id='browserLocation' size='50' >");
		builder.append("<div id='seekBrowserFB' class='autoHide'></div>");
		builder.append("<div id='completionFB' class='autoHide' ></div></br>");

		// configuration
		builder.append("<b>Configuration:</b></br>");
		builder.append("<div id='configuration'>");
		builder.append(getConfigurationDiv());
		builder.append("</div>");

		// save / load.
		builder.append("<b>Backup:</b></br>");
		builder.append("<div id='backupFile'  >" + node.getBackupFile().getAbsolutePath() + "</div>");
		builder.append("<a id='load' href='#' >load</a>");
		builder.append("<div id='loadFB' class='autoHide' ></div>");
		builder.append("<a id='save' href='#' >save</a>");
		builder.append("<div id='saveFB' class='autoHide' ></div>");

		builder.append("<a id='reset' href='#' >reset</a>");
		builder.append("<div id='resetFB' ></div>");
		builder.append("</body>");
		builder.append("</html>");

		return builder.toString();

	}

	private String getConfigurationDiv() {
		StringBuilder builder = new StringBuilder();
		builder.append("<ul>");
		for (String key : node.getConfiguration().keySet()) {
			builder.append("<li>");
			builder.append("<b>" + key + "</b> : ");
			builder.append(node.getConfiguration().get(key));
			builder.append("</li>");
		}
		builder.append("</ul>");
		return builder.toString();
	}

	private String getCapabilitiesDiv() {
		StringBuilder builder = new StringBuilder();
		builder.append("<div id='capabilities'>");

		builder.append("<b>Discovered capabilities :</b></br>");
		builder.append("<table border='2'>");
		builder.append("<tr>");
		builder.append("<td width='90px'>Status</td>");
		builder.append("<td width='90px'>Browser</td>");
		builder.append("<td width='90px' >Instances</td>");
		builder.append("<td width='100px' >Version</td>");
		builder.append("<td>Binary</td>");
		builder.append("</tr>");
		// builder.append("<ul>");
		int i = 0;
		for (DesiredCapabilities capability : node.getCapabilities()) {

			int index = node.getCapabilities().indexOf(capability);
			// builder.append("<li>");
			builder.append("<tr id='capability_" + index + "'>");

			// status
			String status;
			String iconStatus = Icon.NOT_SURE.path();
			String clazz = "";
			String valid = (String) capability.getCapability("valid");
			if ("running".equals(valid)) {
				iconStatus = Icon.NOT_SURE.path();
				status = "running a test";
			} else if ("true".equals(valid)) {
				iconStatus = Icon.CLEAN.path();
				status = "browser ready";
				clazz = "validate_cap";
			} else if ("false".equals(valid)) {
				iconStatus = Icon.ALERT.path();
				clazz = "validate_cap";
				status = "" + capability.getCapability("error");

			} else {
				iconStatus = Icon.NOT_SURE.path();
				clazz = "validate_cap";
				status = "may be working.";
			}

			builder.append("<td>");
			builder.append("<img index='" + index + "' src='" + iconStatus + "' title='" + status + "' class='" + clazz + "' >");
			builder.append("</td>");

			// browser
			builder.append("<td>");
			String browser = capability.getBrowserName();
			builder.append("<img src='/extra/resources/" + BrowserNameUtils.consoleIconName(capability) + ".png'  title='" + browser + "'>");
			builder.append("</td>");

			// instance
			builder.append("<td>");
			int instances = (Integer)(capability.getCapability(RegistrationRequest.MAX_INSTANCES));
			builder.append("<input  size='2' class='" + RegistrationRequest.MAX_INSTANCES + "' index='" + index + "' value='" + instances + "' />");
			builder.append("</td>");

			// version
			builder.append("<td>");
			builder.append(("".equals(capability.getVersion()) ? "??" : capability.getVersion()));
			builder.append("</td>");

			// binary
			builder.append("<td>");
			if ("firefox".equals(browser)) {
				builder.append(capability.getCapability(FirefoxDriver.BINARY));
			} else if ("chrome".equals(browser)) {
				builder.append(capability.getCapability("chrome.binary"));
			} else if ("opera".equals(browser)) {
				builder.append(capability.getCapability("opera.binary"));
			}
			builder.append("<td>");

			builder.append("</tr>");

			i++;
		}
		builder.append("</table>");
		return builder.toString();
	}

	public JSONObject seekBrowsers(String proposedPath) throws JSONException {
		JSONObject o = new JSONObject();
		o.put("success", true);
		o.put("seekBrowserFB", "");

		File f = new File(proposedPath);
		if (!f.exists()) {
			o.put("success", false);
			o.put("seekBrowserFB", f + " is not a valid file.");
			return o;
		} else if (!f.isFile()) {
			o.put("success", false);
			o.put("seekBrowserFB", f + " is a folder.You need to specify a file.");
			return o;
		} else {
			List<String> addeds = new ArrayList<String>();
			List<DesiredCapabilities> founds = browserUtils.findAllInstallsAround(proposedPath);
			for (DesiredCapabilities c : founds) {
				if (node.addNewBrowserInstall(c)) {
					addeds.add(c.getBrowserName() + " v" + c.getVersion());
				}
			}
			if (addeds.isEmpty()) {
				o.put("success", false);
				o.put("seekBrowserFB", "no new browser install found from " + proposedPath);
				return o;
			} else {

				String c = "Woot." + addeds.size() + " new browsers found</br>";
				for (String s : addeds) {
					c += s + "<br>";
				}
				o.put("seekBrowserFB", c);

				// TODO freynaud remove formatitng from here.
				o.put("capabilities", getCapabilitiesDiv());
				return o;
			}
		}
	}

}

enum Icon {
	LOAD("/extra/resources/loader.gif"),

	ALERT("/extra/resources/alert.png"),

	CLEAN("/extra/resources/clean.png"),

	NOT_SURE("/extra/resources/kblackbox.png"),

	VALIDATED("/extra/resources/cnrgrey.png"),

	VALIDATE("/extra/resources/cnrclient.png"),

	FIREFOX("/extra/resources/firefox.png"),

	CHROME("/extra/resources/chrome.png"),

	AURORA("/extra/resources/aurora.png"),

	MAC("/extra/resources/mac.png"),

	LINUX("/extra/resources/tux.png");

	private final String path;

	Icon(String path) {
		this.path = path;
	}

	public String path() {
		return path;
	}

}
