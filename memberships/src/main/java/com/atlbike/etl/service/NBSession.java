package com.atlbike.etl.service;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class NBSession {

	private CloseableHttpClient httpClient = HttpClients.createDefault();
	private String loginURL = "https://atlbike.nationbuilder.com/forms/user_sessions";
	private Closeable loginResponse;

	public void login() {
		HttpHost proxy = new HttpHost("localhost", 3128);
		RequestConfig config = RequestConfig.custom().setProxy(proxy).build();

		HttpPost httpPost = new HttpPost(loginURL);
		// httpPost.setConfig(config);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("user_session_email",
				"jett@atlantabike.org"));
		nvps.add(new BasicNameValuePair("user_session_password", "bikeAngel"));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			CloseableHttpResponse loginResponse = httpClient.execute(httpPost);
			System.out.println(loginResponse.getStatusLine());
			HttpEntity loginEntity = loginResponse.getEntity();
			EntityUtils.consume(loginEntity);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (loginResponse != null) {
					loginResponse.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}

}
