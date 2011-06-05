package org.openqa.demo.node;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.collect.ImmutableBiMap.Builder;
import com.google.common.io.ByteStreams;

public class WebDriverNodeConfigServlet extends HttpServlet {

	private Node node = new Node();

	private static final Logger log = Logger.getLogger(WebDriverNodeConfigServlet.class.getName());

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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

		String typed = request.getParameter("completion");
		if (typed != null) {
			return complete(typed);
		}

		String proposedPath = request.getParameter("submit");
		if (proposedPath != null) {
			JSONObject o = seekBrowser(proposedPath);
			return o;
		}

		String current = request.getParameter("current");
		if (current != null) {
			JSONObject o = seemsValid(current);
			return o;
		}

		return null;
	}

	private JSONObject seekBrowser(String proposedPath) throws JSONException {
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
			int added = processPath(proposedPath);
			if (added == 0) {
				o.put("success", false);
				o.put("info", "no new browser install found from " + proposedPath);
				return o;
			} else {
				o.put("info", "Woot." + added + " new browsers found");
				// return the new list.

				StringBuilder msg = new StringBuilder();
				msg.append("<ul>");
				for (DesiredCapabilities capability : node.getCapabilities()) {
					msg.append("<li>");
					msg.append(capability);
					msg.append("</li>");
				}
				// TODO freynaud remove formatitng from here.
				o.put("content", msg.toString());
				return o;
			}
		}
	}

	private JSONObject seemsValid(String path) throws JSONException {
		JSONObject o = new JSONObject();
		o.put("success", false);
		o.put("content", path+" is not a valid browser executable");
		
		File f = new File(path);
		if (f.exists() && f.isFile() ){
			o.put("success", true);
			DesiredCapabilities cap = BrowserFinderUtils.discoverFirefoxCapability(new File(path));
			o.put("content", path+" appear to be a valid firefox "+cap.getVersion()+" install.");
			
		}
		return o;
		
	}

	private JSONObject complete(String typed) throws JSONException {

		JSONObject o = new JSONObject();
		o.put("success", true);
		o.put("info", "");
		o.put("content", typed);

		String sep = System.getProperty("file.separator");
		String[] pieces = typed.split(sep);
		if (pieces.length == 0) {
			o.put("success", false);
			o.put("info", typed + " doesn't look like a valid path.");
			return o;
		}

		StringBuilder b = new StringBuilder();
		b.append(sep);

		for (int i = 0; i < (pieces.length - 1); i++) {
			b.append(pieces[i]).append(sep);
		}

		File folder = new File(b.toString());
		if (!folder.exists()) {
			o.put("success", false);
			o.put("info", folder + " should be a folder. It isn't.");
			return o;
		}
		String lastTmp =pieces[pieces.length - 1];
		File ft = new File(folder,lastTmp);
		if (ft.isDirectory()){
			folder =ft;
			lastTmp ="";
		}
		final String last = lastTmp; 
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (new File(dir,name).isHidden()){
					return false;
				}
				return name.startsWith(last);
			}
		};
		String[] children = folder.list(filter);

		if (children.length == 0) {
			o.put("success", false);
			o.put("info", "nothing in " + folder + " starting with " + last);
			return o;
		}

		StringBuilder builder = new StringBuilder();
		if (children.length == 1) {
			File f = new File(folder, children[0]);
			builder.append(f.getAbsolutePath());
			if (f.isDirectory()) {
				builder.append(sep);
				o.put("isDirectory", true);	
			}
		} else {
			List<String> names = Arrays.asList(children);
			String common = findCommonStart(names, last);
			File f = new File(folder, common);

			builder.append(f.getAbsolutePath());

			if (f.isDirectory()) {
				builder.append(sep);
			}

			StringBuilder t = new StringBuilder();
			t.append("<ul>");
			for (String child : children) {
				t.append("<li>"+child.replaceAll(folder.getAbsolutePath(), "") + "</li>");
			}
			t.append("</ul>");
			o.put("success", false);
			o.put("info", "several choices :" + t.toString());
			o.put("content", typed);
		}
		o.put("content", builder.toString());
		return o;
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
		builder.append("<title>WebDriver node config</title>");
		builder.append("</head>");

		builder.append("<body>");
		builder.append("<div id='ffs'>");
		builder.append("<ul>");
		for (DesiredCapabilities capability : node.getCapabilities()) {
			builder.append("<li>");
			builder.append(capability);
			builder.append("</li>");
		}
		builder.append("</div>");

		builder.append("<input id='firefox' size='50'  >");
		builder.append("<div id='info' >");
		builder.append("</ul>");

		builder.append("</body>");
		builder.append("</html>");

		return builder.toString();

	}

	private int processPath(String path) {
		int origin = node.getCapabilities().size();
		// try to add the new ones
		File more = new File(path);
		String exeName = more.getName();
		File folder = new File(more.getParent());
		File parent = new File(folder.getParent());

		List<File> folders = BrowserFinderUtils.guessInstallsInFolder(parent);
		for (File f : folders) {
			File exe = new File(f, exeName);
			DesiredCapabilities c = BrowserFinderUtils.discoverFirefoxCapability(exe);
			if (c != null) {
				node.addNewBrowserInstall(c);
			}
		}
		int delta = node.getCapabilities().size() - origin;
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

}
