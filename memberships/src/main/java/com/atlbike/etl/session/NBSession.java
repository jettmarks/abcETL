package com.atlbike.etl.session;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NBSession {

	private String loginRedirectURL = "https://atlbike.nationbuilder.com/login";
	private String loginPostURL = "https://atlbike.nationbuilder.com/forms/user_sessions";
	private CloseableHttpClient httpClient;
	private boolean useProxyFlag = false;
	private HttpHost proxy;
	private RequestConfig proxyConfig;

	public void login(String userEmail, String password) {
		String authenticityToken = "";

		if (useProxyFlag) {
			proxy = new HttpHost("localhost", 3128);
			proxyConfig = RequestConfig.custom().setProxy(proxy).build();
		}

		BasicCookieStore cookieStore = new BasicCookieStore();
		httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore)
				.setRedirectStrategy(new LaxRedirectStrategy()).build();

		try {
			HttpGet httpGet = new HttpGet(loginRedirectURL);
			if (useProxyFlag) {
				httpGet.setConfig(proxyConfig);
			}
			CloseableHttpResponse response1;
			response1 = httpClient.execute(httpGet);

			try {
				HttpEntity entity = response1.getEntity();
				String content = EntityUtils.toString(entity);
				Document doc = Jsoup.parse(content);
				Elements metaElements = doc.select("META");
				for (Element elem : metaElements) {
					// System.out.println(elem);
					if (elem.hasAttr("name")
							&& "csrf-token".equals(elem.attr("name"))) {
						// System.out.println("Value: " + elem.attr("content"));
						authenticityToken = elem.attr("content");
					}
				}
			} finally {
				response1.close();
			}

			// HttpPost login = RequestBuilder.post()
			RequestBuilder requestBuilder = RequestBuilder.post();
			if (useProxyFlag) {
				requestBuilder = requestBuilder.setConfig(proxyConfig);
			}
			HttpUriRequest login = requestBuilder.setUri(new URI(loginPostURL))
					.addParameter("email_address", "")
					.addParameter("user_session[email]", userEmail)
					.addParameter("user_session[password]", password)
					.addParameter("user_session[remember_me]", "1")
					.addParameter("commit", "Sign in with email")
					.addParameter("authenticity_token", authenticityToken)
					.build();
			CloseableHttpResponse response2 = httpClient.execute(login);
			try {

				HttpEntity loginEntity = response2.getEntity();
				EntityUtils.consume(loginEntity);

				/*
				 * Check cookies List<Cookie> cookies =
				 * context.getCookieStore().getCookies(); if (cookies.isEmpty())
				 * { System.out.println("None"); } else { for (int i = 0; i <
				 * cookies.size(); i++) { System.out.println("- " +
				 * cookies.get(i).toString()); } }
				 */

				/*
				 * HttpPost httpPost = new HttpPost(targetURL); HttpResponse
				 * fileResponse = httpClient.execute(httpPost); HttpEntity
				 * fileEntity = fileResponse.getEntity();
				 * System.out.println(fileResponse.getStatusLine());
				 * saveEntity(fileEntity);
				 */
			} finally {
				response2.close();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// httpClient is left open to permit next set of calls
	}

	private void saveEntity(HttpEntity loginEntity) throws IOException,
			FileNotFoundException {
		InputStream inStream = loginEntity.getContent();
		BufferedInputStream bis = new BufferedInputStream(inStream);
		String path = "localFile.csv";
		BufferedOutputStream bos = new BufferedOutputStream(
				new FileOutputStream(new File(path)));
		int byteCount = 0;
		int inByte;
		while ((inByte = bis.read()) != -1) {
			bos.write(inByte);
			byteCount++;
		}
		bis.close();
		bos.close();
		System.out.println("Byte Count: " + byteCount);
	}

	public void save(String targetURL) {
		HttpGet httpGet = new HttpGet(targetURL);
		if (useProxyFlag) {
			httpGet.setConfig(proxyConfig);
		}
		CloseableHttpResponse response;
		try {
			response = httpClient.execute(httpGet);
			try {
				HttpEntity entity = response.getEntity();
				saveEntity(entity);
			} finally {
				response.close();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
