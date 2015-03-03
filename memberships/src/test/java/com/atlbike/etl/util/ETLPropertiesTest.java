package com.atlbike.etl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class ETLPropertiesTest {

	private String loginEmail;
	private String loginPwd;

	@Test
	public void testLoad() {
		ETLProperties etlProps = new ETLProperties();
		assertNotNull(etlProps);
		etlProps.load();
		String memType1 = etlProps.getProperty("nb.etl.membership.types.1");
		assertEquals("Individual", memType1);

	}

	/**
	 * In addition to exercising the functionality for storing password info,
	 * this allows populating the local file without revealing those credentials
	 * to the internet when this code is put in a public repo.
	 * 
	 */
	@Test
	public void testCheckPassword() {
		ETLProperties etlProps = new ETLProperties();
		assertNotNull(etlProps);
		etlProps.load();

		loginEmail = etlProps.getLoginEmail();
		assertNotNull(loginEmail);
		System.out.println("Email: " + loginEmail);

		byte[] answer = new byte[50];
		if (loginEmail.length() == 0) {
			System.out.print("Enter email address: ");
			try {
				System.in.read(answer);
				loginEmail = new String(answer).trim();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out
					.println("\nYou provided the email address " + loginEmail);
			etlProps.setLoginEmail(loginEmail);
		}

		loginPwd = etlProps.getLoginPwd();
		assertNotNull(loginPwd);
		System.out.println("Pwd: " + loginPwd);

		answer = new byte[50];
		if (loginPwd.length() == 0) {
			System.out.print("Enter password: ");
			try {
				System.in.read(answer);
				loginPwd = new String(answer).trim();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("\nYou provided the password " + loginPwd);
			etlProps.setLoginPwd(loginPwd);
		}

		try {
			etlProps.store();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
