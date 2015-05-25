package org.fao.oekc.agris.inputRecords.util;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrServerException;
import org.fao.oekc.agris.inputRecords.index.IndexServerFactory;
import org.fao.oekc.agris.inputRecords.index.QueryUtil;

/**
 * Check duplicates in the Solr index
 * @author celli
 *
 */
public class CheckIndex {

	/**
	 * Check in the index if the ISSN is present. 
	 * If the globalDuplicatesRemoval is set to true, checks if the ISSN is in the index
	 * @param term the ISSN
	 * @param arnprefix center code + year
	 * @param globalDuplicatesRemoval flag for global duplicates check
	 * @return number of occurrences of the ISSN in the index
	 */
	public int checkISSNtoARN(String term, String arnprefix, boolean globalDuplicatesRemoval) {
		if(IssnCleaner.isISSN(term) && globalDuplicatesRemoval) {
			String query = "+ISSN:("+term+")";
			//if(!globalDuplicatesRemoval)
			//	query = query + " +ARN:("+arnprefix.substring(0, 2)+"*)";
			try {
				return QueryUtil.countResultQuery(IndexServerFactory.startSolr(), query);							
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (SolrServerException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	/**
	 * Check if an ARN is in the index
	 * @param term the ARN
	 * @return number of occurrence of the ARN
	 */
	public int checkArn(String term) {
		String query = "+ARN:("+term+")";
		try {
			return QueryUtil.countResultQuery(IndexServerFactory.startSolr(), query);							
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * check if the title is in the index (exact match)
	 * @param title the title 
	 * @return number of occurrence of the title
	 */
	public int checkTitle(String title){
		String query = "+title:(\""+title+"\")";
		try {
			return QueryUtil.countResultQuery(IndexServerFactory.startSolr(), query);							
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			//no action
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return -1;
	}
	
	/**
	 * check if the title is in the index (exact match) and associated to a record with fulltext
	 * an optional search term can be specified
	 * @param title the title 
	 * @param add2searchQuery an optional search term that can be specified
	 * @return number of occurrence of the title
	 */
	public int checkTitleWithFullText(String title, String add2searchQuery){
		String query = "+title:(\""+title+"\") +fulltext:[* TO *]";
		if(add2searchQuery!=null)
			query = query + " " + add2searchQuery;
		try {
			return QueryUtil.countResultQuery(IndexServerFactory.startSolr(), query);							
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SolrServerException e) {
			//no action
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return -1;
	}
	
	public static void main(String[] args){
		CheckIndex ck = new CheckIndex();
		String title = "EXAFS structural study of Fx, the low-potential Fe-S center in photosystem I.";
		String title2 = "EXAFS structural study of Fx, the low-potential Fe-S center in photosystem I";
		
		int occurrences = ck.checkTitle(title);
		//check ending dot
		if(title.endsWith(".")){
			String tmp = title.substring(0, title.length()-1);
			occurrences += ck.checkTitle(tmp);	
		}
		System.out.println(occurrences);
		System.out.println(ck.checkTitle(title2));
		
		String title3 = "Successful implementation of maximum-yield winter grain experiments on farms of Rostock and Schwerin regions, 1986 [German D.R.]";
		System.out.println(ck.checkTitle(title3));
	}

}
