package com.atlbike.etl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ETLPropertiesTest {

	@Test
	public void testStore() {
		ETLProperties etlProps = new ETLProperties();
		assertNotNull(etlProps);
		etlProps.load();
		String memType1 = etlProps.getProperty("nb.etl.membership.types.1");
		assertEquals("Individual", memType1);

		// try {
		// etlProps.store();
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

}
