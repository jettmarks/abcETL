package com.atlbike.etl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.poi.ss.usermodel.Workbook;
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
import com.atlbike.etl.session.NBSession;
import com.atlbike.etl.util.ETLProperties;
import com.atlbike.etl.util.FileUtil;

public class Main {

	private static File currentDirectory;
	private static JFrame parentFrame;

	private static ETLProperties etlProps;
	private static SimpleDateFormat nbDateFormat = new SimpleDateFormat(
			"yyyyMMdd");

	/** If user passes args, we're not in automatic mode. */
	private static boolean haveArgsFlag = false;
	private static File[] inputFilesCSV;

	/**
	 * Entry point for obtaining current membership data and then preparing a
	 * set of reports based on that data.
	 * 
	 * If this is invoked without any arguments, the expectation is the program
	 * will automatically select the NationBuilder website as the source for new
	 * records.
	 * 
	 * If the user provides a pass parameter, it is either a list of files to be
	 * used, or a flag asking that the user be prompted for a set of files that
	 * exist on the local file system.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Setup our properties
		etlProps = ETLProperties.getInstance();
		etlProps.load();
		currentDirectory = new File(etlProps.getProperty(
				"nb.etl.default.directory", "."));

		// Application Window
		parentFrame = new JFrame();
		parentFrame.setSize(500, 150);
		parentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		if (args.length >= 1) {
			haveArgsFlag = true;
		} else {
			if (!etlProps.isAccountDefined()) {
				// Ask user for account credentials
				LoginDialog loginDialog = new LoginDialog();
			}
		}

		// Prepare input file(s) - polls user for local files
		if (haveArgsFlag) {
			// Dialog for selecting list of file names
			inputFilesCSV = getInputFileList(args);
		} else {
			// Retrieve standard set of files from NationBuilder export
			inputFilesCSV = extractFromNationBuilder();
		}

		if (inputFilesCSV == null) {
			System.err.println("No Membership Files to be read - exiting");
			System.exit(-1);
		}

		// Read from the input files into Membership objects
		List<Membership> memberships;
		memberships = readMemberships(inputFilesCSV);

		// Get the Excel Workbook we want to populate
		Workbook workbookTemplate;
		Converter converter = new Converter(parentFrame);
		workbookTemplate = converter.getWorkbookTemplate();

		// Add the memberships to a new workbook
		converter.populateWorkbook(memberships, workbookTemplate);

		// Extract the Date info from the input files to build output file name
		String fileNameSuffix = extractFileNameDate(inputFilesCSV);

		// Write the modified file back out to a new file
		writeWorkbook(workbookTemplate, fileNameSuffix);
		System.exit(0);
	}

	private static File[] extractFromNationBuilder() {
		NBSession nbSession = new NBSession();
		nbSession.login(etlProps.getLoginEmail(), etlProps.getLoginPwd());

		String[] urlList = {
				"https://atlbike.nationbuilder.com/admin/membership_types/3/download",
				"https://atlbike.nationbuilder.com/admin/membership_types/14/download" };
		File[] retrievedFiles = new File[urlList.length];
		int i = 0;
		for (String url : urlList) {
			retrievedFiles[i++] = nbSession.save(url, currentDirectory);
		}
		return retrievedFiles;
	}

	/**
	 * @param args
	 * @return
	 */
	private static File[] getInputFileList(String[] args) {
		File[] inputFilesCSV = null;
		if (args.length >= 1) {
			int index = 0;
			inputFilesCSV = new File[args.length];
			for (String arg : args) {
				inputFilesCSV[index] = new File(arg);
				if (inputFilesCSV[index] == null
						|| !inputFilesCSV[index].exists()) {
					System.err.println("File not found: "
							+ inputFilesCSV[index]);
				}
				index++;
			}
		} else {
			inputFilesCSV = askUserForFileNames();
		}
		return inputFilesCSV;
	}

	/**
	 * Takes the list of input files and checks to see whether a date is part of
	 * the file name so we can re-use it for the output file.
	 * 
	 * If the inputFiles have no date matching the expected pattern, we use the
	 * current date as a default.
	 * 
	 * Package level to make it easier to unit test.
	 * 
	 * @param inputFilesCSV
	 * @return
	 */
	static String extractFileNameDate(File[] inputFilesCSV) {
		String fileNameDate = null;
		Date fnDate = null;
		for (File inputFile : inputFilesCSV) {
			if (inputFile != null) {
				String fileName = inputFile.getName();
				String[] tokens = fileName.split("_");
				String lastToken = tokens[tokens.length - 1];
				if (lastToken != null) {
					// Expected format is <memType>_members_YYYYMMDD.csv
					fnDate = parseForDate(lastToken);
					if (fnDate != null)
						break;
				}
			}
		}
		// If we can't find one, use today's date
		if (fnDate == null) {
			fnDate = new Date();
		}
		fileNameDate = nbDateFormat.format(fnDate);
		return fileNameDate;
	}

	private static Date parseForDate(String token) {
		Date date = null;
		try {
			date = nbDateFormat.parse(token);
		} catch (ParseException e) {
			System.out.println("Unable to parse " + token + " as YYYYmmdd");
			return null;
		}
		return date;
	}

	/**
	 * @param workbookTemplate
	 */
	private static void writeWorkbook(Workbook workbookTemplate,
			String fileNameSuffix) {
		String destinationFile = "member_aging_" + fileNameSuffix + ".xlsx";
		FileOutputStream outputStream = null;
		try {
			File outFile = FileUtil.getIndexedDateFile(currentDirectory,
					destinationFile);
			// String currentPath = currentDirectory.getAbsolutePath();
			// System.out.println("Writing file to " + currentPath +
			// File.separator
			// + destinationFile);
			// outputStream = new FileOutputStream(new File(currentPath +
			// File.separator
			// + destinationFile));
			System.out.println("Writing file to " + outFile.getAbsolutePath());
			outputStream = new FileOutputStream(outFile);
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
	 * Dialog to ask user to choose files to be included in the conversion.
	 * 
	 * @return
	 */
	private static File[] askUserForFileNames() {
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

		// Nothing to do if we have no files to read
		if (inputFilesCSV == null) {
			return null;
		}

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
