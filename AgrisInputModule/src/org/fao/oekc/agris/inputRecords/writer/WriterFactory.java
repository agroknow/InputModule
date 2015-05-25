package org.fao.oekc.agris.inputRecords.writer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jfcutils.util.StringUtils;

import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.ArnManager;

public class WriterFactory {

	//output path and filename
	private String outputPath;

	//start id for the ARN
	private int startArnId;
	private String arnPrefix;

	//lists of records
	private List<AgrisApDoc> records;
	private int currentFileSuffix;

	//singleton
	private static WriterFactory instance;

	private WriterFactory(String outputPath, String arnPrefix){
		this.outputPath = outputPath;
		this.records = new LinkedList<AgrisApDoc>();
		this.currentFileSuffix = 0;
		//compute initial ARN id
		this.startArnId = (new ArnManager()).getMaxArnCode(arnPrefix);
		this.arnPrefix = arnPrefix;
	}

	/**
	 * Synchronized access to the singleton instance
	 * @param outputPath the path of the output directory
	 * @param arnPrefix the ARN prefix
	 * @return the instance of WriterFactory
	 */
	public static synchronized WriterFactory getInstance(String outputPath, String arnPrefix){
		if(instance==null)
			instance = new WriterFactory(outputPath, arnPrefix);
		return instance;	
	}

	/**
	 * Given a list of AgrisApDoc, it writes a file if the current list is bigger than 100 elements
	 * @param addRecords records to be written
	 */
	public synchronized void addDocumentsAndWrite(List<AgrisApDoc> addRecords){
		//add all records
		this.records.addAll(addRecords);
		//write groups of 100
		if(this.records.size()>=100){
			int count = 0;
			List<AgrisApDoc> toWrite = new ArrayList<AgrisApDoc>();
			for(AgrisApDoc rec: this.records){
				count++;
				toWrite.add(rec);
				if(count==100){
					this.write(toWrite, startArnId);
					count = 0;
					toWrite.clear();
					this.currentFileSuffix = this.currentFileSuffix+100;
					//the ARN will be generated if it does not exist in the record
					this.startArnId = this.startArnId + 100;
				}
			}
			//minus than 100 records
			this.records.clear();
			this.records.addAll(toWrite);
		}
	}

	/**
	 * Write the results, also if less than 100
	 */
	public synchronized void flushEverything(){
		this.write(records, startArnId);
	}

	/*
	 * Create the oputput directory, the name of the file and writes 100 records
	 */
	private void write(List<AgrisApDoc> toWrite, int startArnId) {
		File workingDIR = (new File(this.outputPath));
		if(!workingDIR.exists())
			workingDIR.mkdir();

		//filename: DJ20120 00100 00001.xml
		int numberOfBitsForCounter = (new ArnManager()).getNumberOfBitsForCounter(arnPrefix);
		StringUtils cleaner = new StringUtils();
		String suffixfileName = arnPrefix + cleaner.formatInteger(startArnId,numberOfBitsForCounter)+ cleaner.formatInteger(this.currentFileSuffix/100,numberOfBitsForCounter)+ ".xml"; 

		//write
		WriteAgrisApXml writer = new WriteAgrisApXml(outputPath, suffixfileName, toWrite, arnPrefix, startArnId);
		try {
			writer.writeRecords();
			System.out.println(suffixfileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
