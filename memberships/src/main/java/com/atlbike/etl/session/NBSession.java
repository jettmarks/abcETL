package com.atlbike.etl.session;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class NBSession {

	private String targetURL = "https://atlbike.nationbuilder.com/admin/membership_types/14/download";
	private String loginURL = "https://atlbike.nationbuilder.com/forms/user_sessions";

	public void login() {
		HttpResponse loginResponse = null;

		// httpClient = HttpClientBuilder.create()
		// .setRedirectStrategy(new LaxRedirectStrategy()).build();
		RequestConfig globalConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.BEST_MATCH).build();
		CookieStore cookieStore = new BasicCookieStore();
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);

		CloseableHttpClient httpClient = HttpClients.custom()
				.setRedirectStrategy(new LaxRedirectStrategy())
				.setDefaultRequestConfig(globalConfig)
				.setDefaultCookieStore(cookieStore).build();

		// HttpHost proxy = new HttpHost("localhost", 3128);
		// RequestConfig config =
		// RequestConfig.custom().setProxy(proxy).build();

		// HttpPost httpPost = new HttpPost(targetURL);
		HttpPost httpPost = new HttpPost(loginURL);

		// httpPost.setConfig(config);
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("user_session_email", "email@domain"));
		nvps.add(new BasicNameValuePair("user_session_password", "magicCookie"));

		try {

			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			loginResponse = httpClient.execute(httpPost);
			System.out.println(loginResponse.getStatusLine());
			HttpEntity loginEntity = loginResponse.getEntity();
			EntityUtils.consume(loginEntity);

			/* Check cookies */
			List<Cookie> cookies = context.getCookieStore().getCookies();
			if (cookies.isEmpty()) {
				System.out.println("None");
			} else {
				for (int i = 0; i < cookies.size(); i++) {
					System.out.println("- " + cookies.get(i).toString());
				}
			}

			httpPost = new HttpPost(targetURL);
			HttpResponse fileResponse = httpClient.execute(httpPost);
			HttpEntity fileEntity = fileResponse.getEntity();
			System.out.println(fileResponse.getStatusLine());
			saveEntity(fileEntity);

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

}
