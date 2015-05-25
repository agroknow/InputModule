package org.fao.oekc.agris.inputRecords;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import jfcutils.files.read.TXTReader;
import jfcutils.util.DateTime;

import org.fao.oekc.agris.inputRecords.executor.DirectoryConverter;
import org.fao.oekc.agris.inputRecords.writer.WriterFactory;
import org.xml.sax.SAXException;

public class MainInputRecords {

	/**
	 * Application entry point. The application should take 6 inputs:
	 * - the source directory recursively containing source XML files
	 * - the submission year of source records
	 * - the country code
	 * - the subcenter code
	 * - a boolean flag, the behavor is related to the type of the input
	 * - the source XML format (e.g. agrisap, simpledc...)
	 * - (optional) the path of a text file, for example to limit the ISSNs to be considered
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, FileNotFoundException{

		String startDate = DateTime.getDateTime();	
		//printParameters(args);

		//check number of arguments
		if(args.length>=6) {

			//arguments
			String sourceLoc = args[0];
			File sourceDir = new File(sourceLoc);

			String recYear = args[1];
			String countryCode = args[2];
			String subCenterCode = args[3];

			//check input partameter consistence
			if(recYear!=null && recYear.length()==4 && countryCode!=null 
					&& countryCode.length()==2 && subCenterCode!=null && subCenterCode.length()==1){
				String arnPrefix = countryCode+recYear+subCenterCode;

				//duplicates removal
				String boleanFlag = args[4];
				boolean globalDuplicatesRemoval = true;
				if(boleanFlag.equalsIgnoreCase("false"))
					globalDuplicatesRemoval = false;

				//the format
				String format = args[5];

				//eventually reads a file of lines
				Set<String> lines = null;
				if(args.length==7){
					String textFileLoc = args[6];
					TXTReader reader = new TXTReader();
					lines = reader.parseTxt(textFileLoc);
				}

				//threads pool for parsing source XMLs
				ExecutorService executor = Executors.newFixedThreadPool(6);

				//prepare the parsing
				SAXParserFactory spf = SAXParserFactory.newInstance();
				spf.setValidating(false);
				spf.setNamespaceAware(false);

				//outpur dir
				String outpurDir = sourceDir.getAbsolutePath()+"/OUTPUT";

				//for internal duplicates removal
				List<String> titles = new LinkedList<String>();

				//recursive execution on the source directory
				(new DirectoryConverter(spf, executor, arnPrefix, format, outpurDir, globalDuplicatesRemoval, lines, titles)).convertDir(sourceDir);

				// This will make the executor accept no new threads
				// and finish all existing threads in the queue
				executor.shutdown();
				// Wait until all threads are finish
				while (!executor.isTerminated()) {}

				//write last records
				WriterFactory.getInstance(outpurDir, arnPrefix).flushEverything();
			}
			else {
				System.out.println("Please, check parameters format. The second parameter should be a year, in the format YYYY; " +
						"The third parameter is the country code, in the format CC; the fourth parameter is the subcenter code, in the format S.");
			}

		} else {
			System.out.println("Missing arguments. You need to specify following parameters: " +
			"source directory, records year, country code, subcenter code, true or false to perform a global duplicates removal, source XML format");
		}

		System.out.println(startDate + " -------- " + DateTime.getDateTime());
	}

	protected static void printParameters(String[] args){
		for(String s: args)
			System.out.println(s);
	}

}
