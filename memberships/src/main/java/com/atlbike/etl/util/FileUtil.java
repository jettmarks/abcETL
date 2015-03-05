package com.atlbike.etl.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {
	/**
	 * Given a directory and a base file name, locates potential clashes of file
	 * name and if clash is found, will generate an indexed file name where the
	 * base has the string "(n)" where 'n' increments each time a clash is
	 * detected.
	 * 
	 * The new file -- whether a clash or not -- is created and returned.
	 * 
	 * @param directory
	 * @param baseName
	 */
	public static File getIndexedDateFile(File directory, String baseName) {
		File indexedFile = new File(directory.getAbsolutePath()
				+ File.separator + baseName);
		int i = 1;
		while (indexedFile.exists()) {
			String base = indexedFile.getName();
			String[] tokens = base.split("\\.(?=[^\\.]+$)");
			int pos;
			if ((pos = tokens[0].indexOf("(")) > 0) {
				tokens[0] = tokens[0].substring(0, pos - 1);
			}
			String path = indexedFile.getParent() + File.separator + tokens[0]
					+ " (" + i + ")." + tokens[1];
			i++;
			indexedFile = new File(path);
		}
		try {
			indexedFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return indexedFile;
	}
}
