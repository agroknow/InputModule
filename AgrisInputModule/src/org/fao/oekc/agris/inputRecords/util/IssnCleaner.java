package org.fao.oekc.agris.inputRecords.util;

import java.util.regex.Pattern;

/**
 * Naive cleaning of ISSN
 * @author celli
 *
 */
public class IssnCleaner {

	/**
	 * Return a cleaned issn
	 * @param issn the issn to clean
	 * @return a cleaned issn
	 */
	public static String cleanISSN(String issn){
		String result = issn.replace("(", "");
		result = result.replace(")", "");
		result = result.replace("/", "");
		result = result.replace(".", "");
		result = result.replace("Print", "");
		result = result.replace("print", "");
		result = result.replace("online", "");
		result = result.replace("Online", "");
		result = result.replace(" ", "");
		result = result.replace("\t", "");
		result = result.replace("_", "-");
		result = result.replace("O", "0");
		result = result.trim();
		result = result.toUpperCase();
		if(result.length()==10){
			int pos = result.lastIndexOf("-");
			if(pos!=-1)
				result = result.substring(0, pos)+result.substring(pos+1, result.length());
		}
		if(result.length()==8 && result.indexOf("-")==-1){
			result = result.substring(0, 4)+"-"+result.substring(4, result.length());
		}
		return result;
	}
	
	/**
	 * Check if the issn is syntactically correct
	 * @param issn the issn to check
	 * @return true if the issn is syntactically correct
	 */
	public static boolean isISSN(String issn){
		String pattern = "[0-9][0-9][0-9][0-9][/-][0-9][0-9][0-9][0-9[X]]";
		return Pattern.matches(pattern, issn);
	}
	
	public static void main(String[] args){
		String data = "Nov 2007.";
		String pattern = "[A-Za-z][A-Za-z][A-Za-z].[0-9][0-9][0-9][0-9][/.]";
		System.out.println(Pattern.matches(pattern, data));
		
		String p = "-X";
		p = p.replaceAll("-X", "X");
		System.out.println(p);
	}
	
}
