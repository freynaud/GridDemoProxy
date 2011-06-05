package org.openqa.demo.grid;

import java.util.Set;

import org.openqa.demo.grid.logging.SeleniumGridEvent;
import org.openqa.demo.grid.logging.UserReport;
import org.openqa.grid.internal.TestSlot;
import org.openqa.grid.internal.utils.HtmlRenderer;
import org.openqa.grid.web.utils.BrowserNameUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

public class LogEverythingProxyRenderer implements HtmlRenderer {

	private LogEverythingProxy proxy;

	@SuppressWarnings("unused")
	private LogEverythingProxyRenderer() {

	}

	public LogEverythingProxyRenderer(LogEverythingProxy proxy) {
		this.proxy = proxy;
	}

	public String renderSummary() {
		StringBuilder builder = new StringBuilder();
		builder.append("<div>");
		builder.append("<b>Proxy : " + proxy.getClass().getName()).append("</b><br/>");

		for (TestSlot slot : proxy.getTestSlots()) {
			DesiredCapabilities c = new DesiredCapabilities(slot.getCapabilities());
			builder.append("<img src='" + BrowserNameUtils.getConsoleIconPath(c.getBrowserName()) + "' title='" + c + "' /> ");
		}

		builder.append("</br>Some log for this node :")
		.append("<ul>");
		for (String user : proxy.usageByUser.keySet()) {
			UserReport report = proxy.usageByUser.get(user);
			builder.append("<li><b>" + user + ":</b></br></li><ul>");

			Set<SeleniumGridEvent> events = report.getEventscount().keySet();
			for (SeleniumGridEvent event : events) {
				builder.append("<li>").append(event).append(":" + report.getEventscount().get(event)).append("</li>");
			}
			builder.append("</ul>");
		}
		builder.append("</ul>");

		builder.append("</div>");
		return builder.toString();

	}

}
