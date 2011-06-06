package org.openqa.demo.nodes.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.openqa.grid.internal.exception.GridException;
import org.openqa.selenium.Platform;
import org.openqa.selenium.browserlaunchers.locators.BrowserInstallation;
import org.openqa.selenium.browserlaunchers.locators.Firefox3Locator;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.os.CommandLine;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class BrowserFinderUtils {

	private static final Logger log = Logger.getLogger(BrowserFinderUtils.class.getName());

	public DesiredCapabilities getDefaultFirefoxInstall() {
		BrowserInstallation install = new Firefox3Locator().findBrowserLocationOrFail();
		DesiredCapabilities c = discoverFirefoxCapability(new File(install.launcherFilePath()));
		return c;

	}

	public DesiredCapabilities getDefaultChromeInstall() {
		// check the chrome driver is here.
		ChromeDriverService.createDefaultService();
		// check the chrome itself is installed.
		String c = CommandLine.findExecutable("google-chrome");
		if (c == null){
			throw new GridException("google-chrome is not in your path. Is it installed ?");
		}
		Platform p = Platform.getCurrent();
		DesiredCapabilities cap = DesiredCapabilities.chrome();
		cap.setPlatform(Platform.getCurrent());
		return cap;
	}

	public DesiredCapabilities getDefaultIEInstall() {
		if (Platform.getCurrent().is(Platform.WINDOWS)) {
			return null;
		} else {
			throw new GridException("No IE on " + Platform.getCurrent());
		}
	}

	/**
	 * get all the info possible about this firefox install : OS, version ( from
	 * application.ini )
	 * 
	 * @param exe
	 *            the firefox executable
	 * @return a DesiredCapability with everything filed in
	 */
	public DesiredCapabilities discoverFirefoxCapability(File exe) {
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
	public List<File> guessInstallsInFolder(File folder) {
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

	/**
	 * Find all the browsers install in the same folder as the one specified. If
	 * you specify /home/user/firefox2/firefox
	 * 
	 * the following will also be found : /home/user/firefox3/firefox
	 * /home/user/firefox3.5/firefox /home/user/firefox4/firefox
	 * 
	 * @param exe
	 *            the exe of a browser.
	 * @return
	 */
	public List<DesiredCapabilities> findAllInstallsAround(String exe) {
		List<DesiredCapabilities> res = new ArrayList<DesiredCapabilities>();

		// try to add the new ones
		File more = new File(exe);
		String exeName = more.getName();
		File folder = new File(more.getParent());
		File parent = new File(folder.getParent());

		List<File> folders = guessInstallsInFolder(parent);
		for (File f : folders) {
			File otherexe = new File(f, exeName);
			DesiredCapabilities c = discoverFirefoxCapability(otherexe);
			if (c != null) {
				res.add(c);
			}
		}
		return res;
	}

	public static void updateGuessedCapability(DesiredCapabilities c, DesiredCapabilities realCap) {
		if (!c.getBrowserName().equals(realCap)){
			throw new GridException("Error validation the browser. Expected "+c.getBrowserName()+" but got "+realCap.getBrowserName());
		}
		if (c.getVersion()==null){
			c.setVersion(realCap.getVersion());
		}
		
	}

}