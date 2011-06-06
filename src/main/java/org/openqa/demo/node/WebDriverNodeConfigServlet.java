package org.openqa.demo.node;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.demo.nodes.service.BrowserFinderUtils;
import org.openqa.demo.nodes.service.FileSystemAjaxService;
import org.openqa.demo.nodes.service.WebDriverValidationService;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.exception.GridException;
import org.openqa.grid.internal.listeners.RegistrationListener;
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

		String reset = request.getParameter("reset");
		if (reset != null) {
			node.reset();
			JSONObject o = new JSONObject();
			o.put("success", true);
			o.put("info", "");
			o.put("capabilities", getCapabilitiesDiv());
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
				DesiredCapabilities realCap = wdValidator.validate(node.getPort(), c);
				o.put("success", true);
				BrowserFinderUtils.updateGuessedCapability(c, realCap);
				o.put("info", "Success !");
				c.setCapability("valid", true);
			} catch (GridException e) {
				o.put("success", false);
				c.setCapability("valid", false);
				o.put("info", e.getMessage());
				
			}
			o.put("capabilities", getCapabilitiesDiv());
			return o;
		}

		return null;
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
		builder.append("</div>");

		builder.append(getCapabilitiesDiv());

		builder.append("</div>");

		builder.append("More :</br>");
		builder.append("<input id='browserLocation' size='50' >");
		builder.append("<div id='completionHelp' ></div>");
		builder.append("<div id='info' >provide the path to another browser executable to add its capability to the node.</div>");

		builder.append("<a id='reset' href='#' >reset</a>");
		builder.append("<div id='validationMsg' >validation log</div>");
		builder.append("</body>");
		builder.append("</html>");

		return builder.toString();

	}

	private String getCapabilitiesDiv() {
		StringBuilder builder = new StringBuilder();
		builder.append("<div id='capabilities'>");
		builder.append("Discovered capabilities :");
		builder.append("<ul>");
		int i = 0;
		for (DesiredCapabilities capability : node.getCapabilities()) {
			int index = node.getCapabilities().indexOf(capability);
			builder.append("<li>");
			builder.append("<div id='capability_" + index + "'>");
			// browser
			String browser = capability.getBrowserName();
			builder.append("<img src='/extra/resources/images/" + BrowserNameUtils.consoleIconName(capability) + ".png'  title='" + browser + "'>");
			builder.append("<b> " + browser + "</b>");
			// version
			builder.append(", v:" + ("".equals(capability.getVersion()) ? "??" : capability.getVersion()));
			// binary
			if ("firefox".equals(browser)) {
				builder.append(" , path:" + capability.getCapability(FirefoxDriver.BINARY));
			}

			Object validated = capability.getCapability("valid");

			if (validated == null) {
				builder.append("<a class='validate_cap' index='" + index + "' href='#' >validate</a>");
			} else if ((Boolean) validated) {
				builder.append("<div>checked - passed</div>");
			} else {
				builder.append("<div>checked - failed</div>");
			}

			builder.append("</div>");
			builder.append("</li>");
			i++;
		}
		return builder.toString();
	}

	public JSONObject seekBrowsers(String proposedPath) throws JSONException {
		JSONObject o = new JSONObject();
		o.put("success", true);
		o.put("info", "");

		File f = new File(proposedPath);
		if (!f.exists()) {
			o.put("success", false);
			o.put("info", f + " is not a valid file.");
			return o;
		} else if (!f.isFile()) {
			o.put("success", false);
			o.put("info", f + " is a folder.You need to specify a file.");
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
				o.put("info", "no new browser install found from " + proposedPath);
				return o;
			} else {

				String c = "Woot." + addeds.size() + " new browsers found</br>";
				for (String s : addeds) {
					c += s + "<br>";
				}
				o.put("info", c);

				// TODO freynaud remove formatitng from here.
				o.put("capabilities", getCapabilitiesDiv());
				return o;
			}
		}
	}

}
