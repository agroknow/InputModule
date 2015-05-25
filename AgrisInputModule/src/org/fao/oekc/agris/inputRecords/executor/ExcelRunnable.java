package org.fao.oekc.agris.inputRecords.executor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jfcutils.files.read.XLSReader;

import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.parser.WorldBankExcelParser;
import org.fao.oekc.agris.inputRecords.writer.WriterFactory;

public class ExcelRunnable implements Runnable{
	
	//the file to parse
	private File contentFile;
	
	private String arnPrefix;
	private String outputPath;
	private boolean globalDuplRem;
	private String sourceFormat;
	
	/**
	 * Constructor
	 * @param contentFile the File to parse
	 * @param outputPath the path of the output directory
	 * @param arnPrefix countrycode+sub.year+subcentercode
	 * @param globalDuplRem flag for global duplicates removal
	 * @param sourceFormat
	 */
	public ExcelRunnable(File contentFile, String outputPath, String arnPrefix, 
			boolean globalDuplRem, String sourceFormat){
		this.contentFile = contentFile;
		this.arnPrefix = arnPrefix;
		this.globalDuplRem = globalDuplRem;
		this.outputPath = outputPath;
		this.sourceFormat = sourceFormat;
	}
	
	/**
	 * Parse the file and write results
	 */
	public void run() {
		//records cleaed and not yet indexed
		List<AgrisApDoc> records = new ArrayList<AgrisApDoc>();

		//parse the file
		if(sourceFormat.equalsIgnoreCase("worldbank")){
			//read the mapping URL->Agrovoc
			Map<String, String> url2Agrovoc = this.readWbKeywords();
			
			//parse
			try {
				new WorldBankExcelParser(records, url2Agrovoc, contentFile, arnPrefix, globalDuplRem);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}	
		}
		
		//write records
		WriterFactory.getInstance(outputPath, arnPrefix).addDocumentsAndWrite(records);
	}
	
	/*
	 * Read two column excel files that encode the mapping WB URLs -> Agrovoc Keyword
	 */
	private Map<String, String> readWbKeywords(){
		Map<String, String> mapping = new HashMap<String, String>();
		String path = contentFile.getParent()+"/mapping";
		XLSReader xlsReader = new XLSReader();
		File mappingDir = new File(path);
		//all mapping files
		File[] mappingFiles = mappingDir.listFiles();
		for(File f: mappingFiles){
			FileInputStream fs;
			try {
				fs = new FileInputStream(f);
				mapping.putAll(xlsReader.readTwoColumns(fs));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}	
		return mapping;
	}

}
