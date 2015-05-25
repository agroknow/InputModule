package org.fao.oekc.agris.inputRecords.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import jfcutils.util.Printer;
import jfcutils.util.StringUtils;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

/**
 * Read a file Excel with two columns
 * @author celli
 *
 */
public class ReadTwoColumnsExcel {

	/**
	 * Read a file Excel with two columns. The first column must contain a value, else discard the row.
	 * @param fileInputStream the file excel
	 * @return the map of lines as strings
	 */
	public static Map<String, String> readColumns(InputStream fileInputStream){
		Map<String, String> data = new HashMap<String, String>();
		WorkbookSettings ws = null;
		Workbook workbook = null;
		Sheet s = null;
		Cell rowData[] = null;
		int rowCount = 0;

		try {
			ws = new WorkbookSettings();
			ws.setEncoding("ISO-8859-1");
			workbook = Workbook.getWorkbook(fileInputStream, ws);

			//Getting Default Sheet 0
			s = workbook.getSheet(0);

			//Total No Of Rows in Sheet, will return you no of rows that are occupied with some data
			rowCount = s.getRows();
			
			//cleaner utility
			StringUtils cleaner = new StringUtils();

			//Reading Individual Row Content, not the header
			for (int i = 1; i < rowCount; i++) {
				//Get Individual Row
				rowData = s.getRow(i);
				String content0 = rowData[0].getContents();
				if(content0!=null && !content0.trim().equals("")){
					content0 = cleaner.trimRight(content0);
					content0 = cleaner.trimLeft(content0);
					String content1 = rowData[1].getContents();
					content1 = cleaner.trimRight(content1);
					content1 = cleaner.trimLeft(content1);
					data.put(content0, content1);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		}

		return data;
	}

	public static void main(String[] args) throws IOException{
		String filenameComplete = "../Journals_erna.xls";
		FileInputStream fs = new FileInputStream(new File(filenameComplete));
		Map<String, String> data = ReadTwoColumnsExcel.readColumns(fs);
		Printer.printGeneralMap(data);
	}

}
