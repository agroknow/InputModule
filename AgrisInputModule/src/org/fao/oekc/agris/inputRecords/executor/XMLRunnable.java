package org.fao.oekc.agris.inputRecords.executor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParser;

import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.writer.WriterFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Runnable that parses an XML Agris AP file to clean, remove duplicates and write an XM AGRIS AP output file
 * @author celli
 *
 */
public class XMLRunnable implements Runnable{

	//the file to parse
	private File contentFile;
	private SAXParser saxParser;
	private String arnPrefix;
	private String outputPath;
	private boolean globalDuplRem;
	private String sourceFormat;
	private Set<String> lines;
	private List<String> titlesAdded;

	/**
	 * Constructor
	 * @param contentFile the File to parse
	 * @param outputPath the path of the output directory
	 * @param saxParser the sax parser instance
	 * @param arnPrefix countrycode+sub.year+subcentercode
	 * @param globalDuplRem flag for global duplicates removal
	 * @param sourceFormat the source format like simpledc, ovid...
	 * @param lines lines of the input text file (also null, it is optional), used for example to filter ISSNs
	 * @param titlesAdded list of titles already added for the current set
	 */
	public XMLRunnable(File contentFile, String outputPath, SAXParser saxParser, String arnPrefix, 
			boolean globalDuplRem, String sourceFormat, Set<String> lines, List<String> titlesAdded){
		this.contentFile = contentFile;
		this.saxParser = saxParser;
		this.arnPrefix = arnPrefix;
		this.globalDuplRem = globalDuplRem;
		this.outputPath = outputPath;
		this.sourceFormat = sourceFormat;
		this.lines = lines;
		this.titlesAdded = titlesAdded;
	}

	/**
	 * Parse the file and write results
	 */
	public void run() {
		//records cleaed and not yet indexed
		//this is the output of the parser, to be written on a file
		List<AgrisApDoc> records = new ArrayList<AgrisApDoc>();

		//parser
		DefaultHandler parser = (new XMLDispatcher()).dispatchXML(sourceFormat, records, arnPrefix, titlesAdded, globalDuplRem, lines);

		try {
			if(parser!=null)
				saxParser.parse(contentFile, parser);
			else
				System.out.println("No existing parser for this source format: "+sourceFormat);
		} catch (SAXException e) {
			System.out.println("!!ERROR "+contentFile.getName());
			System.out.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//write records
		WriterFactory.getInstance(outputPath, arnPrefix).addDocumentsAndWrite(records);
	}

}
