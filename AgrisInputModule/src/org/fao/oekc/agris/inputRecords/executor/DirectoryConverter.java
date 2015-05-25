package org.fao.oekc.agris.inputRecords.executor;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/**
 * This object recursively looks inside a directory to parse files, calling the right parser for the source type of file (XML, Excel...).
 * It takes parameters form org.fao.oekc.agris.inputRecords.MainInputRecords and executes the job.
 * @author celli
 *
 */
public class DirectoryConverter {
	
	private SAXParserFactory spf;
	private ExecutorService executor;
	private String sourceFormat;
	private String arnPrefix;
	private String outpurDir;
	private boolean globalDuplicatesRemoval;
	private Set<String> lines;
	private List<String> titlesAdded;
	
	/**
	 * Called by org.fao.oekc.agris.inputRecords.MainInputRecords
	 * @param spf SAXParserFactory
	 * @param executor a FixedThreadPool
	 * @param arnPrefix 3rd + 2nd + 4th parameter
	 * @param sourceFormat 6th command line parameter
	 * @param outpurDir sourceDit (1st parameter) + "/OUTPUT"
	 * @param globalDuplicatesRemoval 5th command line parameter
	 * @param lines could be null if 7th parameter is empty
	 * @param titles empty list for internal duplicates removal
	 */
	public DirectoryConverter(SAXParserFactory spf, ExecutorService executor, String arnPrefix, String sourceFormat,
			String outpurDir, boolean globalDuplicatesRemoval, Set<String> lines, List<String> titles){
		this.spf = spf;
		this.executor = executor;
		this.arnPrefix = arnPrefix;
		this.sourceFormat = sourceFormat;
		this.lines = lines;
		this.globalDuplicatesRemoval = globalDuplicatesRemoval;
		this.outpurDir = outpurDir;
		this.titlesAdded = titles;
	}
	
	/**
	 * Look for XML, Excel files in the current directory, calling a Runnable to parse each one
	 */
	public void convertDir(File sourceDir) throws ParserConfigurationException, SAXException {

		//look for XML files in the current directory
		File[] listFiles = sourceDir.listFiles();
		//directories
		Set<File> listDirs = new TreeSet<File>();
		//scan files
		if(listFiles!=null){
			//scan files
			for(File contentFile: listFiles){
				String filename = contentFile.getName().toLowerCase();
				//XML files
				if(filename.endsWith(".xml") || filename.endsWith(".mods")){
					SAXParser saxParser = spf.newSAXParser();
					//System.out.println("++ start: "+filename);
					//DO THE JOB
					Runnable worker = new XMLRunnable(contentFile, outpurDir, saxParser, arnPrefix, globalDuplicatesRemoval, sourceFormat, lines, titlesAdded);
					executor.execute(worker);
				}
				else if(filename.endsWith(".xls")) {
					//DO THE JOB
					Runnable worker = new ExcelRunnable(contentFile, outpurDir, arnPrefix, globalDuplicatesRemoval, sourceFormat);
					executor.execute(worker);
				}
				else {
					//add a directory
					if(contentFile.isDirectory() && !contentFile.getName().equals("mapping") && !contentFile.getName().equals("OUTPUT"))
						listDirs.add(contentFile);
				}
			}
		}
		//recursive application
		listFiles = null;
		for(File subDir: listDirs){
			this.convertDir(subDir);
		}	
	}
	

}
