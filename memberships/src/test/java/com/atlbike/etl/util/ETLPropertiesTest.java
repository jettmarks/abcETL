package com.atlbike.etl.util;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class ETLPropertiesTest {

	@Test
	public void testStore() {
		ETLProperties etlProps = new ETLProperties();
		assertNotNull(etlProps);
		etlProps.load();
		try {
			etlProps.store();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
