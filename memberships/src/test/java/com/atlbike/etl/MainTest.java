package com.atlbike.etl;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

public class MainTest {

	@Test
	public void testExtractFileNameDate() {
		File[] inputFiles = new File[3];
		inputFiles[0] = new File("Individual_members.csv");
		inputFiles[1] = new File("IndividualMembers-20150123.csv");
		// inputFiles[2] = new File("Individual_members_20150123.csv");
		String suffix = Main.extractFileNameDate(inputFiles);
		assertNotNull(suffix);
		System.out.println("Suffix set to " + suffix);
	}

}
