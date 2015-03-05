package com.atlbike.etl.util;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

public class FileUtilTest {

	@Test
	public void testGetIndexedDateFile() {
		ETLProperties etlProps = ETLProperties.getInstance();
		etlProps.load();
		File directory = new File(
				etlProps.getProperty("nb.etl.default.directory"));
		File indexedFile = FileUtil.getIndexedDateFile(directory, "base.csv");
		assertNotNull(indexedFile);
		System.out.println("New File: " + indexedFile.getAbsolutePath());
	}

}
