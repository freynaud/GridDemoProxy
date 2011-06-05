package org.openqa.demo.grid.logging;

import org.openqa.selenium.remote.DesiredCapabilities;

public abstract class SeleniumGridEvent {

	private String browser;
	private String version;
	private String platform;

	@SuppressWarnings("unused")
	private SeleniumGridEvent() {
	};

	public SeleniumGridEvent(DesiredCapabilities cap) {
		this.browser = cap.getBrowserName();
		this.version = cap.getVersion();
		this.platform = cap.getPlatform().toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((browser == null) ? 0 : browser.hashCode());
		result = prime * result + ((platform == null) ? 0 : platform.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SeleniumGridEvent other = (SeleniumGridEvent) obj;
		if (browser == null) {
			if (other.browser != null)
				return false;
		} else if (!browser.equals(other.browser))
			return false;
		if (platform == null) {
			if (other.platform != null)
				return false;
		} else if (!platform.equals(other.platform))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (browser != null) {
			builder.append(browser);
		}
		if (version != null && !"".equalsIgnoreCase(version)) {
			builder.append(" (" + version + ")");
		}
		if (platform != null && !"ANY".equalsIgnoreCase(platform) && !"".equalsIgnoreCase(platform)) {
			builder.append("," + platform);
		}
		return builder.toString();
	}

}
