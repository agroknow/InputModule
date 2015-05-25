package org.fao.oekc.agris.inputRecords.util;

import java.net.MalformedURLException;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrServerException;
import org.fao.oekc.agris.inputRecords.index.IndexServerFactory;
import org.fao.oekc.agris.inputRecords.index.QueryUtil;

/**
 * Manage ARN, i.e. uniqueness, check existence...
 * @author celli
 *
 */
public class ArnManager {
	
	/**
	 * Given an ARN prefix, extract from the index the last assigned ID as last 5 ARN chars, to compute the new usable ID
	 * @param arnPrefix the ARN prefix
	 * @return the new usable ID as last 5 ARN chars
	 */
	public int getMaxArnCode(String arnPrefix){
		//look for last ID number
		int startId = 0;
		try {
			String maxARN = QueryUtil.selectMaxValue(IndexServerFactory.startSolr(), "ARN:"+arnPrefix+"*", "ARN");
			if(maxARN!=null && maxARN.length()>0){
				//take last 5 chars if ARN legnth is less equals 12
				if(maxARN.length()>5 && maxARN.length()<=12) {
					maxARN = maxARN.substring(maxARN.length()-5, maxARN.length());
					startId = Integer.parseInt(maxARN);
					startId = startId+1;
				}
				else
					if(maxARN.length()> 12) {
						//remove 7 bits, which are used to build the prefix
						int numberOfIncremental = maxARN.length() - 7;
						maxARN = maxARN.substring(maxARN.length()- numberOfIncremental, maxARN.length());
						startId = Integer.parseInt(maxARN);
						startId = startId+1;
					}
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (SolrServerException e1) {
			e1.printStackTrace();
		}
		//String arn = arnprefix+StringCleaning.formatInteger(startArnIdNumber,5);
		return startId;
	}
	
	/**
	 * Compute the number of digits to use for ARN counter based on the ARN prefix
	 * @param arnPrefix the ARN prefix, to manage exceptions
	 * @return the number of digits to use for ARN counter
	 */
	public int getNumberOfBitsForCounter(String arnPrefix){
		arnPrefix = arnPrefix.toUpperCase();
		//apply exceptions
		String[] seventBits = {"US[1-2][0-9][0-9][0-9]0"};
		for(String s: seventBits){
			if(Pattern.matches(s, arnPrefix))
				return 7;
		}
		return 5;
	}
	
	public static void main(String[] args){
		ArnManager m = new ArnManager();
		System.out.println(m.getMaxArnCode("US20130"));
	}

}
