package org.openqa.demo.node;

import static org.openqa.grid.common.RegistrationRequest.CLEAN_UP_CYCLE;
import static org.openqa.grid.common.RegistrationRequest.MAX_SESSION;
import static org.openqa.grid.common.RegistrationRequest.TIME_OUT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.grid.common.RegistrationRequest;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.net.NetworkUtils;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.google.common.collect.ImmutableBiMap.Builder;

public class Launcher {

	private static final String hub = "localhost:4444";
	private static NetworkUtils networkUtils = new NetworkUtils();

	public static void main(String[] args) throws Exception {
		/*System.out.println("RegisteringLinuxWebDriver");
		Hub.getInstance().start();
		System.out.println("hub started.");

		RemoteControlConfiguration config = new RemoteControlConfiguration();
		config.setPort(4446);
		SeleniumServer node = new SeleniumServer(config);
		node.boot();*/
		//System.out.println("registering from "+args[0]);
		//registerNode(args[0]);
		registerNode(args[0]);
	}

	private static void registerNode(String res) throws IOException, Exception {
		StringBuilder b = new StringBuilder();
		InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(res);
		
		InputStreamReader inputreader = new InputStreamReader(in);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        while (( line = buffreader.readLine()) != null) {
        	b.append(line);
        }
		
		String json = b.toString();
		System.out.println(json);
		JSONObject o = new JSONObject(json);
		JSONArray jcaps   = o.getJSONArray("capabilities");
		JSONObject jconfig = o.getJSONObject("configuration");
		
		
		
		
		BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest("POST", "http://" + hub + "/grid/register/");
		r.setEntity(new StringEntity(json));
		
		
		/*DefaultHttpClient client = new DefaultHttpClient();
		URL hubURL = new URL("http://" + hub);
		HttpHost host = new HttpHost(hubURL.getHost(), hubURL.getPort());
		HttpResponse response = client.execute(host, r);*/

	}

	private static String getNodeHost() {
		return networkUtils.getIp4NonLoopbackAddressOfThisMachine().getHostAddress();
	}
}
