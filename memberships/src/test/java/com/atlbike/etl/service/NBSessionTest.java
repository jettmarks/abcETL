/**
 * 
 */
package com.atlbike.etl.service;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import com.atlbike.etl.session.NBSession;
import com.atlbike.etl.util.ETLProperties;

/**
 * @author a8l8f
 * 
 */
public class NBSessionTest {

	private String targetURL = "https://atlbike.nationbuilder.com/admin/membership_types/14/download";

	/**
	 * Test method for {@link com.atlbike.etl.session.NBSession#login()}.
	 */
	@Test
	public void testLogin() {

		NBSession nbSession = new NBSession();
		assertNotNull(nbSession);
		ETLProperties etlProps = new ETLProperties();
		etlProps.load();

		nbSession.login(etlProps.getLoginEmail(), etlProps.getLoginPwd());
		File currentDirectory = new File(
				etlProps.getProperty("nb.etl.default.directory"));
		nbSession.save(targetURL, currentDirectory);

	}

}
