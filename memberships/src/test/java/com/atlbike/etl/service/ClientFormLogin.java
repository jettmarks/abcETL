package com.atlbike.etl.service;

/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * A example that demonstrates how HttpClient APIs can be used to perform
 * form-based logon.
 */
public class ClientFormLogin {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String authenticityToken = "";
		BasicCookieStore cookieStore = new BasicCookieStore();
		CloseableHttpClient httpclient = HttpClients.custom()
				.setDefaultCookieStore(cookieStore)
				.setRedirectStrategy(new LaxRedirectStrategy()).build();
		try {
			HttpGet httpget = new HttpGet(
					"https://atlbike.nationbuilder.com/login");
			CloseableHttpResponse response1 = httpclient.execute(httpget);
			try {
				HttpEntity entity = response1.getEntity();
				System.out.println("Content Length: "
						+ entity.getContentLength());

				System.out.println("Login form get: "
						+ response1.getStatusLine());
				// EntityUtils.consume(entity);
				String content = EntityUtils.toString(entity);
				Document doc = Jsoup.parse(content);
				Elements metaElements = doc.select("META");
				for (Element elem : metaElements) {
					System.out.println(elem);
					if (elem.hasAttr("name")
							&& "csrf-token".equals(elem.attr("name"))) {
						System.out.println("Value: " + elem.attr("content"));
						authenticityToken = elem.attr("content");
					}
				}

				System.out.println("Initial set of cookies:");
				List<Cookie> cookies = cookieStore.getCookies();
				if (cookies.isEmpty()) {
					System.out.println("None");
				} else {
					for (int i = 0; i < cookies.size(); i++) {
						System.out.println("- " + cookies.get(i).toString());
					}
				}
			} finally {
				response1.close();
			}

			HttpUriRequest login = RequestBuilder
					.post()
					.setUri(new URI(
							"https://atlbike.nationbuilder.com/forms/user_sessions"))
					.addParameter("email_address", "")
					.addParameter("user_session[email]", "email@domain")
					.addParameter("user_session[password]", "magicCookie")
					.addParameter("user_session[remember_me]", "1")
					.addParameter("commit", "Sign in with email")
					.addParameter("authenticity_token", authenticityToken)
					.build();
			CloseableHttpResponse response2 = httpclient.execute(login);
			try {
				HttpEntity entity = response2.getEntity();
				// for (Header h : response2.getAllHeaders()) {
				// System.out.println(h);
				// }
				System.out.println("Content Length: "
						+ entity.getContentLength());

				System.out.println("Login form get: "
						+ response2.getStatusLine());
				EntityUtils.consume(entity);

				System.out.println("Post logon cookies:");
				List<Cookie> cookies = cookieStore.getCookies();
				if (cookies.isEmpty()) {
					System.out.println("None");
				} else {
					for (int i = 0; i < cookies.size(); i++) {
						System.out.println("- " + cookies.get(i).toString());
					}
				}
			} finally {
				response2.close();
			}

			httpget = new HttpGet(
			// HttpUriRequest file = RequestBuilder
			// .post()
			// .setUri(new URI(
					"https://atlbike.nationbuilder.com/admin/membership_types/14/download");
			// .build();
			// CloseableHttpResponse response3 = httpclient.execute(file);
			CloseableHttpResponse response3 = httpclient.execute(httpget);
			try {
				HttpEntity entity = response3.getEntity();
				System.out.println("Content Length: "
						+ entity.getContentLength());

				System.out.println("File Get: " + response3.getStatusLine());
				saveEntity(entity);
				// EntityUtils.consume(entity);

				System.out.println("Post file get cookies:");
				List<Cookie> cookies = cookieStore.getCookies();
				if (cookies.isEmpty()) {
					System.out.println("None");
				} else {
					for (int i = 0; i < cookies.size(); i++) {
						System.out.println("- " + cookies.get(i).toString());
					}
				}
			} finally {
				response3.close();
			}

		} finally {
			httpclient.close();
		}
	}

	private static void saveEntity(HttpEntity loginEntity) throws IOException,
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
