package com.atlbike.etl;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.swing.JDialog;
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

import com.atlbike.etl.domain.Membership;

/**
 * Knows how to take Membership records and write them to Excel spreadsheet
 * template.
 * 
 * TODO: unwanted dependency on the UI.
 * 
 * @author a8l8f
 */
public class Converter {

	private static CellStyle cellStyleDate = null;

	private static JDialog dlg;

	private static JFrame parentFrame;

	public Converter(JFrame parentFrame) {
		Converter.parentFrame = parentFrame;
	}

	/**
	 * Populates the workbook and displays progress bar as it goes.
	 * 
	 * @param memberships
	 * @param workbookTemplate
	 */
	public void populateWorkbook(List<Membership> memberships,
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
	private JProgressBar prepareProgressBar(int recordCount) {
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
	private void populateCell(Row row, int index, String value) {
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
	private void populateCell(Row row, int index, Integer value) {
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
	private void populateCell(Row row, int index, Date value) {
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
	public Workbook getWorkbookTemplate() {
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

}
