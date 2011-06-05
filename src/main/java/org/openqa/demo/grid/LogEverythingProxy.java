package org.openqa.demo.grid;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openqa.demo.grid.logging.NewBrowserRequestEvent;
import org.openqa.demo.grid.logging.SeleniumGridEvent;
import org.openqa.demo.grid.logging.TimeoutEvent;
import org.openqa.demo.grid.logging.UserReport;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.grid.internal.TestSession;
import org.openqa.grid.internal.listeners.CommandListener;
import org.openqa.grid.internal.listeners.TimeoutListener;
import org.openqa.grid.internal.utils.HtmlRenderer;
import org.openqa.grid.selenium.proxy.WebDriverRemoteProxy;
import org.openqa.selenium.remote.DesiredCapabilities;

public class LogEverythingProxy extends WebDriverRemoteProxy implements TimeoutListener {

	Map<String, UserReport> usageByUser = new HashMap<String, UserReport>();

	public LogEverythingProxy(RegistrationRequest request) {
		super(request);
	}

	@Override
	public void beforeSession(TestSession session) {
		super.beforeSession(session);
		String user = (String) session.getRequestedCapabilities().get("user");
		addEvent(user, new NewBrowserRequestEvent(new DesiredCapabilities(session.getRequestedCapabilities())));
	}

	@Override
	public void beforeRelease(TestSession session) {
		super.beforeRelease(session);
		String user = (String) session.getRequestedCapabilities().get("user");
		addEvent(user, new TimeoutEvent(new DesiredCapabilities(session.getRequestedCapabilities())));
	}

	private synchronized void addEvent(String user, SeleniumGridEvent event) {
		if (user == null) {
			user = "anonymous";
		}
		UserReport report = usageByUser.get(user);
		if (report == null) {
			report = new UserReport(user);
			usageByUser.put(user, report);
		}
		report.addEvent(event);

	}

	HtmlRenderer renderer = new LogEverythingProxyRenderer(this);

	@Override
	public HtmlRenderer getHtmlRender() {
		return renderer;
	}

}
