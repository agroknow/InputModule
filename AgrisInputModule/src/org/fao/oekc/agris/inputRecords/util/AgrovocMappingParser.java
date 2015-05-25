package org.fao.oekc.agris.inputRecords.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import jfcutils.files.FileSearcher;

/**
 * This class is responsible to parse the mapping agrovoc_english -> URI
 * @author celli
 *
 */
public class AgrovocMappingParser {

	//already parsed map
	private Map<String, String> mapping;

	// Singleton
	private static AgrovocMappingParser instance;

	public static synchronized AgrovocMappingParser getInstance() {
		/* Singleton: lazy creation */
		if(instance == null)
			instance = new AgrovocMappingParser();
		return instance;
	}

	public AgrovocMappingParser(){
		this.mapping = null;
	}

	/**
	 * Builds the map with the mapping agrovoc_key_lower_case -> URI
	 * @param fileToSearch the name of the file, to be searched
	 * @param language the language of agrovoc, that can be EN, IT, ES, PT. The default is EN
	 * @return the map with the mapping agrovoc_key_lower_case -> URI
	 * @throws FileNotFoundException
	 */
	public synchronized Map<String, String> mappingAgrovocUri(String fileToSearch, String language) throws FileNotFoundException{
		if(this.mapping==null){
			//search the file
			FileSearcher fileSearch = new FileSearcher();
			File fileFound = fileSearch.searchFile("../", fileToSearch);
			if(fileFound!=null) {
				this.mapping = new HashMap<String, String>();
				//Note that FileReader is used, not File, since File is not Closeable
				Scanner scanner = new Scanner(new FileReader(fileFound));
				try {
					//first use a Scanner to get each line
					while (scanner.hasNextLine()){
						String line = scanner.nextLine();
						String[] agrUri = line.split("|");
						String key = "";
						if(language.equalsIgnoreCase("IT"))
							key = agrUri[2].toLowerCase();
						else if(language.equalsIgnoreCase("ES"))
							key = agrUri[3].toLowerCase();
						else if(language.equalsIgnoreCase("PT"))
							key = agrUri[4].toLowerCase();
						else
							//default: EN
							key = agrUri[1].toLowerCase();
						this.mapping.put(key, agrUri[0]);
					}
					System.out.println("++ Agrovoc Loaded");
				}
				finally {
					//ensure the underlying stream is always closed
					//this only has any effect if the item passed to the Scanner
					//constructor implements Closeable (which it does in this case).
					scanner.close();
				}
			}
			else
				System.out.println("-- Not Found: "+fileToSearch);
		}
		return this.mapping;
	}

}
