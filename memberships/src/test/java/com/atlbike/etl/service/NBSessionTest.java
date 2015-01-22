/**
 * 
 */
package com.atlbike.etl.service;

import static org.junit.Assert.*;

import org.junit.Test;

import com.atlbike.etl.session.NBSession;

/**
 * @author a8l8f
 * 
 */
public class NBSessionTest {

	/**
	 * Test method for {@link com.atlbike.etl.session.NBSession#login()}.
	 */
	@Test
	public void testLogin() {
		NBSession nbSession = new NBSession();
		assertNotNull(nbSession);
		nbSession.login();
	}

}
