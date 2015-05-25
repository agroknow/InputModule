package org.fao.oekc.agris.inputRecords.util;

/**
 * Naive cleaning of ISBN
 * @author celli
 *
 */
public class IsbnCleaner {
	
	/**
	 * Return a cleaned isbn
	 * @param isbn the isbn to clean
	 * @return a cleaned isbn
	 */
	public static String cleanISBN(String isbn){
		String result = isbn.replaceAll("[^0-9\\-\\ xX]+", "");
		result = result.trim();
		result = result.replaceAll("^\\-+", "");
		result = result.trim();
		//replace wrong concateneted strings
		int indexLast = result.indexOf("  ");
		if(indexLast!=-1)
			result = result.substring(0, indexLast);
		indexLast = result.indexOf(" -");
		if(indexLast!=-1)
			result = result.substring(0, indexLast);
		//replace spaces with dashes
		result = result.replaceAll("\\ ", "-");
		//check length
		if(result.length()<9)
			result = null;
		return result;
	}
	
	/**
	 * check if the String contains 9, 10 or 13 numbers and x
	 * @param isbn the original isbn
	 * @return true if the string contains 9, 10 or 13 numbers and x
	 */
	public static boolean hasIsbnFormat(String isbn){
		String tmp = isbn.replaceAll("[^0-9xX]+", "");
		if(tmp.length()==9 || tmp.length()==10 || tmp.length()==13)
			return true;
		return false;
	}

}
