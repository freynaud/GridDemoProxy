package org.openqa.demo.node;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.openqa.selenium.Platform;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

public class BrowserFinderUtils {

	private static final Logger log = Logger.getLogger(BrowserFinderUtils.class.getName());

	/**
	 * get all the info possible about this firefox install : OS, version ( from
	 * application.ini )
	 * 
	 * @param exe
	 *            the firefox executable
	 * @return a DesiredCapability with everything filed in
	 */
	public static DesiredCapabilities discoverFirefoxCapability(File exe) {
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
	public static List<File> guessInstallsInFolder(File folder) {
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
