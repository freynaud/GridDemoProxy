package org.openqa.demo.nodes.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.grid.internal.exception.GridException;

public class HubUtils {

	private URL status;

	private HubUtils() {

	}

	
	public HubUtils(String host, int port) {
		String u = "http://" + host + ":" + port + "/grid/status";
		try {
			status = new URL(u);
		} catch (MalformedURLException e) {
			throw new GridException(u + " isn't a valid url. " + e.getMessage());
		}
	}

	public boolean isHubReachable() {
		try {
			JSONObject o = getProxyDetails(null);
			return o.has("success");
		} catch (Throwable t) {
			return false;
		}
	}

	public JSONObject getProxyDetails(String id) {
		try {
			DefaultHttpClient client = new DefaultHttpClient();

			JSONObject o = new JSONObject();
			o.put("id",id);
			
			BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest("POST", status.toExternalForm());
			r.setEntity(new StringEntity(o.toString()));

			HttpHost host = new HttpHost(status.getHost(), status.getPort());
			HttpResponse response = client.execute(host, r);

			JSONObject res = extractObject(response);
			return res;
		} catch (Throwable e) {
			return null;
		}
	}

	private JSONObject extractObject(HttpResponse resp) throws IOException, JSONException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
		StringBuffer s = new StringBuffer();
		String line;
		while ((line = rd.readLine()) != null) {
			s.append(line);
		}
		rd.close();
		return new JSONObject(s.toString());
	}


	public URL getUrl() {
		return status;
	}
}
