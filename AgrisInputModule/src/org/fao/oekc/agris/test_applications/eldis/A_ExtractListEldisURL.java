package org.fao.oekc.agris.test_applications.eldis;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import jfcutils.files.write.TXTWriter;

import org.xml.sax.SAXException;

/**
 * Read all IDS XML_records and extract in a TXT file the list of URLs of fulltext, taken from <urls><list-item>
 * We need the list to give as AgrovocTagger parameter
 * @author celli
 *
 */
public class A_ExtractListEldisURL {
	
	/**
	 * Start the extraction
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException{
		A_ExtractListEldisURL extractor = new A_ExtractListEldisURL();
		//extract the list of URLs
		List<String> listURLs = new LinkedList<String>();
		String locationOfFiles = "C:/Users/celli/Documents/workspace_agris/agris_input/eldis/ids";
		extractor.convertDir(new File(locationOfFiles), listURLs);
		//write in a txt file
		TXTWriter writer = new TXTWriter();
		writer.writeLines(listURLs, "C:/Users/celli/Documents/workspace_agris/agris_input/eldis/eldis_urls.txt");
	}
	
	/*
	 * Recursively reads the root directory and parses all Eldis XML files
	 */
	private void convertDir(File root, List<String> listURLs) throws ParserConfigurationException, SAXException, IOException{
		//look for XML files in the current directory
		File[] listFiles = root.listFiles();
		//directories
		Set<File> listDirs = new TreeSet<File>();
		//scan files
		if(listFiles!=null){
			//scan files
			for(File contentFile: listFiles){
				String filename = contentFile.getName().toLowerCase();
				//XML files
				if(filename.endsWith(".xml")){
					SAXParserFactory  spf = SAXParserFactory.newInstance();
					spf.setValidating(false);
					spf.setNamespaceAware(false);
					SAXParser saxParser = spf.newSAXParser();
					saxParser.parse(contentFile ,new IDSUrlSaxParser(listURLs));
				}
				else {
					if(contentFile.isDirectory())
						listDirs.add(contentFile);
				}
			}
		}
		//recursive application
		listFiles = null;
		for(File subDir: listDirs){
			this.convertDir(subDir, listURLs);
		}	
	}

}
