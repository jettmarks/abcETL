package com.atlbike.etl;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrRegEx;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import com.atlbike.etl.domain.Membership;
import com.atlbike.etl.util.ETLProperties;

public class Main {

	private static File currentDirectory;
	private static JFrame parentFrame;

	private static JDialog dlg;
	private static CellStyle cellStyleDate = null;
	private static ETLProperties etlProps;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		etlProps = new ETLProperties();
		etlProps.load();

		// Prepare input file(s)
		String inputFileName = null;
		File[] inputFilesCSV = null;
		if (args.length >= 1) {
			int index = 0;
			for (String arg : args) {
				inputFileName = arg;
				inputFilesCSV[index] = new File(arg);
				if (inputFilesCSV[index] == null
						|| !inputFilesCSV[index].exists()) {
					System.err.println("File not found: "
							+ inputFilesCSV[index]);
				}
				index++;
			}
		} else {
			inputFilesCSV = askUserForFileNames(inputFileName);
		}

		parentFrame = new JFrame();
		parentFrame.setSize(500, 150);
		parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		List<Membership> memberships;
		memberships = readMemberships(inputFilesCSV);

		Workbook workbookTemplate;
		workbookTemplate = getWorkbookTemplate();

		// Add the memberships to a new workbook
		populateWorkbook(memberships, workbookTemplate);

		// Write the modified file back out to a new file
		writeWorkbook(workbookTemplate);
		System.exit(0);
	}

	/**
	 * @param workbookTemplate
	 */
	private static void writeWorkbook(Workbook workbookTemplate) {
		String destinationFile = "newFile.xlsx";
		FileOutputStream outputStream = null;
		try {
			String currentPath = currentDirectory.getAbsolutePath();
			System.out.println("Writing file to " + currentPath + "/"
					+ destinationFile);
			outputStream = new FileOutputStream(new File(currentPath + "/"
					+ destinationFile));
			workbookTemplate.write(outputStream);
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param inputFileName
	 * @return
	 */
	private static File[] askUserForFileNames(String inputFileName) {
		currentDirectory = new File(etlProps.getProperty(
				"nb.etl.default.directory", "."));
		File[] inputFiles = null;
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(currentDirectory);
		chooser.setMultiSelectionEnabled(true);
		int option = chooser.showOpenDialog(null);
		if (option == JFileChooser.APPROVE_OPTION) {
			inputFiles = chooser.getSelectedFiles();
			currentDirectory = chooser.getCurrentDirectory();
			if (!currentDirectory.getAbsolutePath().equalsIgnoreCase(
					etlProps.getProperty("nb.etl.default.directory"))) {
				etlProps.setProperty("nb.etl.default.directory",
						currentDirectory.getAbsolutePath());
				try {
					etlProps.store();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			System.exit(0);
		}
		return inputFiles;
	}

	/**
	 * Populates the workbook and displays progress bar as it goes.
	 * 
	 * @param memberships
	 * @param workbookTemplate
	 */
	private static void populateWorkbook(List<Membership> memberships,
			Workbook workbookTemplate) {
		// Prepare progress dialog
		int recordCount = memberships.size();
		int modulo = recordCount / 100;
		JProgressBar progressBar = prepareProgressBar(recordCount);

		// Prepare Date Style for formatting dates
		cellStyleDate = workbookTemplate.createCellStyle();
		CreationHelper creationHelper = workbookTemplate.getCreationHelper();
		cellStyleDate.setDataFormat(creationHelper.createDataFormat()
				.getFormat("yyyy/mm/dd"));

		Sheet dataSheet = workbookTemplate.getSheet("importedData");
		int rowIndex = 1;
		String monthFormula = "DATE(YEAR(INDIRECT(\"RC[-3]\",0)),MONTH(INDIRECT(\"RC[-3]\", 0)),1)";

		for (Membership membership : memberships) {
			int colIndex = 0;
			Row row = dataSheet.getRow(rowIndex);
			if (row == null) {
				dataSheet.createRow(rowIndex);
				row = dataSheet.getRow(rowIndex);
			}
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
			row.getCell(colIndex).setCellStyle(cellStyleDate);
			row.getCell(colIndex).setCellFormula(monthFormula);
			rowIndex++;
			progressBar.setValue(rowIndex);
			// Avoid painting unless the value would appear different
			if (rowIndex % modulo == 0)
				progressBar.setStringPainted(true);
		}
		progressBar.setVisible(false);
		dlg.setVisible(false);
	}

	/**
	 * @param recordCount
	 * @return
	 */
	private static JProgressBar prepareProgressBar(int recordCount) {
		JProgressBar progressBar = null;
		dlg = new JDialog(parentFrame, "Converting to Excel", true);
		progressBar = new JProgressBar(0, recordCount);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		// progressBar.setVisible(true);
		dlg.add(BorderLayout.CENTER, progressBar);
		dlg.add(BorderLayout.NORTH, new JLabel("Conversion Progress ..."));
		dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dlg.setSize(300, 75);
		dlg.setLocationRelativeTo(null);

		// dlg.setVisible(true);
		Thread t = new Thread(new Runnable() {
			public void run() {
				dlg.setVisible(true);
			}
		});
		t.start();
		return progressBar;
	}

	/**
	 * Helper function to check for empty cell before attempting to put
	 * something in it.
	 * 
	 * @param row
	 * @param index
	 * @param String
	 *            value
	 */
	private static void populateCell(Row row, int index, String value) {
		if (row.getCell(index) == null) {
			row.createCell(index);
		}
		row.getCell(index).setCellValue(value);
	}

	/**
	 * Helper function to check for empty cell before attempting to put
	 * something in it.
	 * 
	 * @param row
	 * @param index
	 * @param Integer
	 *            value
	 */
	private static void populateCell(Row row, int index, Integer value) {
		if (row.getCell(index) == null) {
			row.createCell(index);
		}
		row.getCell(index).setCellType(Cell.CELL_TYPE_NUMERIC);
		if (value != null) {
			row.getCell(index).setCellValue(value);
		}
	}

	/**
	 * Helper function to check for empty cell before attempting to put
	 * something in it.
	 * 
	 * @param row
	 * @param index
	 * @param Date
	 *            value
	 */
	private static void populateCell(Row row, int index, Date value) {
		if (row.getCell(index) == null) {
			row.createCell(index);
		}
		row.getCell(index).setCellType(Cell.CELL_TYPE_NUMERIC);
		row.getCell(index).setCellStyle(cellStyleDate);
		if (value != null) {
			row.getCell(index).setCellValue(value);
		}
	}

	/**
	 * Pulls "blank" copy of Excel spreadsheet from resources.
	 */
	private static Workbook getWorkbookTemplate() {
		Workbook workbookTemplate = null;
		String templateFile = "template.xlsx";
		InputStream inputStream = null;
		try {
			inputStream = Main.class.getResourceAsStream(templateFile);
			workbookTemplate = new XSSFWorkbook(inputStream);
		} catch (IOException e) {
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
	 * Given a CSV file with the expected format, read records into a list of
	 * Membership objects.
	 * 
	 * @param inputFilesCSV
	 * @return
	 */
	private static List<Membership> readMemberships(File[] inputFilesCSV) {
		List<Membership> memberships;
		List<Membership> allMemberships = new ArrayList<Membership>();
		ICsvBeanReader beanReader = null;
		for (File inputFile : inputFilesCSV) {

			memberships = new ArrayList<Membership>();
			try {
				beanReader = new CsvBeanReader(new FileReader(inputFile),
						CsvPreference.STANDARD_PREFERENCE);

				// the header elements are used to map the values to the bean
				// (names
				// must match)
				final String[] header = beanReader.getHeader(true);
				final CellProcessor[] processors = getProcessors();

				Membership membership;
				while ((membership = beanReader.read(Membership.class, header,
						processors)) != null) {
					memberships.add(membership);
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
			allMemberships.addAll(memberships);
			System.out.println("Read " + memberships.size() + " records from "
					+ inputFile.getName());
		}
		System.out.println("Read " + allMemberships.size()
				+ " records in total");
		return allMemberships;
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
				new ParseInt(), // nationbuilder_signup_id
				new ParseInt(), // membership_id
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
