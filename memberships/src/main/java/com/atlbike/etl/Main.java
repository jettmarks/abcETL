package com.atlbike.etl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrRegEx;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import com.atlbike.etl.domain.Membership;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("usage: Main <inputFileName> (in csv format)");
			System.exit(-1);
		}

		String inputFileName = args[0];
		File inputFileCSV = new File(inputFileName);
		if (inputFileCSV == null || !inputFileCSV.exists()) {
			System.err.println("File not found: " + inputFileCSV);
		}

		List<Membership> memberships;
		memberships = readMemberships(inputFileCSV);

		Workbook workbookTemplate;
		workbookTemplate = getWorkbookTemplate();

		Sheet dataSheet = workbookTemplate.getSheet("importedData");
		int rowIndex = 1;
		String monthFormula = "DATE(YEAR(INDIRECT(\"RC[-3]\",0)),MONTH(INDIRECT(\"RC[-3]\", 0)),1)";

		for (Membership membership : memberships) {
			Integer colIndex = 0;
			Row row = dataSheet.getRow(rowIndex);
			populateCell(row, colIndex++, membership.getMembership_name());
			populateCell(row, colIndex++,
					membership.getNationbuilder_signup_id());
			populateCell(row, colIndex++, membership.getMembership_id());
			populateCell(row, colIndex++, membership.getFirst_name());
			populateCell(row, colIndex++, membership.getLast_name());
			populateCell(row, colIndex++, membership.getEmail());
			populateCell(row, colIndex++, membership.getPhone_number());
			populateCell(row, colIndex++, membership.getMobile_number());
			populateCell(row, colIndex++, membership.getStatus());
			populateCell(row, colIndex++, membership.getExpires_on());
			populateCell(row, colIndex++, membership.getStarted_on());
			populateCell(row, colIndex++, membership.getStatus_reason());
			row.createCell(colIndex);
			row.getCell(colIndex).setCellFormula(monthFormula);
			rowIndex++;
		}

		// Write the modified file back out to a new file
		String destinationFile = "newFile.xlsx";
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(new File(destinationFile));
			workbookTemplate.write(outputStream);
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void populateCell(Row row, Integer index, String value) {
		if (row.getCell(index) == null) {
			row.createCell(index);
		}
		row.getCell(index).setCellValue(value);
	}

	private static void populateCell(Row row, Integer index, Date value) {
		if (row.getCell(index) == null) {
			row.createCell(index);
		}
		if (value != null) {
			row.getCell(index).setCellValue(value);
		}
	}

	/**
	 * 
	 */
	private static Workbook getWorkbookTemplate() {
		Workbook workbookTemplate = null;
		String templateFile = "template.xlsx";
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(new File(templateFile));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			workbookTemplate = new XSSFWorkbook(inputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			inputStream.close();
		} catch (IOException e) {
			// ignore
		}
		return workbookTemplate;
	}

	/**
	 * @param inputFileCSV
	 * @return
	 */
	private static List<Membership> readMemberships(File inputFileCSV) {
		List<Membership> memberships;
		memberships = new ArrayList<Membership>();
		ICsvBeanReader beanReader = null;
		try {
			beanReader = new CsvBeanReader(new FileReader(inputFileCSV),
					CsvPreference.STANDARD_PREFERENCE);

			// the header elements are used to map the values to the bean (names
			// must match)
			final String[] header = beanReader.getHeader(true);
			final CellProcessor[] processors = getProcessors();

			Membership membership;
			while ((membership = beanReader.read(Membership.class, header,
					processors)) != null) {
				memberships.add(membership);
				// System.out.println(String.format(
				// "lineNo=%s, rowNo=%s, customer=%s",
				// beanReader.getLineNumber(), beanReader.getRowNumber(),
				// membership));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (beanReader != null) {
				try {
					beanReader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		System.out.println("Read " + memberships.size() + " records");
		return memberships;
	}

	/**
	 * Matches up with the input side of the CSV.
	 * 
	 * @return the cell processors
	 */
	private static CellProcessor[] getProcessors() {

		final String emailRegex = "[a-z0-9\\._]+@[a-z0-9\\.]+"; // just an
																// example, not
																// very robust!
		StrRegEx.registerMessage(emailRegex, "must be a valid email address");

		final CellProcessor[] processors = new CellProcessor[] { new NotNull(), // membership_name
				null, // nationbuilder_signup_id
				null, // membership_id
				new Optional(), // first_name
				new Optional(), // last_name
				new Optional(), // new StrRegEx(emailRegex), // email
				null, // phone_number
				null, // mobile_number
				new NotNull(), // status
				new Optional(new ParseDate("yyyy-MM-dd")), // expires_on
				new Optional(new ParseDate("yyyy-MM-dd")), // started_on
				null // status reason
		};

		return processors;
	}
}