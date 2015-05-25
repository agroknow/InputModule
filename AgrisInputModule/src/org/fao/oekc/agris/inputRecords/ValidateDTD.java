package org.fao.oekc.agris.inputRecords;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Validate a directory of XML files against a DTD
 * @author celli
 *
 */
public class ValidateDTD {

	/**
	 * Recursively validate a directory of XML files against a DTD
	 * @param sourceDir the root directory
	 * @param spf the sax parser
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public void validateDir(File sourceDir, DocumentBuilderFactory factory) throws SAXException, ParserConfigurationException {

		//look for XML files in the current directory
		File[] listFiles = sourceDir.listFiles();
		System.out.println("  -- reading: "+sourceDir.getName());
		//directories
		Set<File> listDirs = new TreeSet<File>();
		//scan files
		if(listFiles!=null){
			//scan files
			for(File contentFile: listFiles){
				String filename = contentFile.getName().toLowerCase();
				//XML files
				if(filename.contains(".rdf") || filename.contains(".xml")){
					//System.out.println(contentFile.getAbsolutePath());
					DocumentBuilder builder = factory.newDocumentBuilder();
					builder.setErrorHandler(new SimpleErrorHandler());    
					// the "parse" method also validates XML, will throw an exception if misformatted
					try {
						builder.parse(new InputSource(contentFile.getAbsolutePath()));
					} catch (Exception e) {
						System.out.println(" !!!!!! "+contentFile.getAbsolutePath());
					}
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
			this.validateDir(subDir, factory);
		}	
	}

	public static void main(String[] args) throws SAXException, ParserConfigurationException {

		if(args.length>=1) {
			ValidateDTD val = new ValidateDTD();
			File sourceDir = new File(args[0]);

			//prepare the parsing
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(false);

			val.validateDir(sourceDir, factory);
		}
		else
			System.out.println("Please, specify source folder");
	}

}
