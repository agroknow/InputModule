package org.fao.oekc.agris.inputRecords.index;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * Common query methods to delete/modify part of the index
 * @author celli
 *
 */
public class QueryUtil {
	
	//delete all index
	public static void deleteAll(SolrServer solr) throws SolrServerException, IOException{
		solr.deleteByQuery("*:*");
		solr.commit();
	}
	
	//optimize index
	public static void optimize(SolrServer solr) throws SolrServerException, IOException{
		solr.optimize(true, true, 2);
	}
	
	//delete by ARN
	public static void deleteByArn(SolrServer solr, String arn) throws SolrServerException, IOException{
		solr.deleteById(arn);
		solr.commit();
	}
	
	//delete by query
	public static void deleteByQuery(SolrServer solr, String query) throws SolrServerException, IOException{
		solr.deleteByQuery(query);
		solr.commit();
	}
	
	/**
	 * Query the index and count results number
	 * @param solr the SolrServer
	 * @param query the query to be executed
	 * @return number of results
	 * @throws SolrServerException
	 * @throws IOException
	 */
	public static int countResultQuery(SolrServer solr, String query) throws SolrServerException, IOException{
		//build the query	
		SolrQuery squery = new SolrQuery(query);
		squery.setStart(0);
		squery.setRows(10);
		
		//search
		return QueryUtil.getSolrDocumentResult(solr, squery).size();
	}
	
	/**
	 * Find the max value in a given field for a given query
	 * @param solr the SolrServer
	 * @param query the query to be executed (usually a regex, e.g. ARN:JP20060* or *:*)
	 * @param field the field the field in which we need to find the maximum
	 * @return the max value in the given field for the given query
	 * @throws SolrServerException
	 */
	public static String selectMaxValue(SolrServer solr, String query, String field) throws SolrServerException{
		//build the query	
		SolrQuery squery = new SolrQuery(query);
		squery.setStart(0);
		squery.setRows(1);
		
		//order by ARN descending (max first)
		SolrQuery.ORDER order = SolrQuery.ORDER.desc;
		squery.addSortField(field, order);
		
		//search
		SolrDocumentList results = QueryUtil.getSolrDocumentResult(solr, squery);
		for(SolrDocument doc : results){
			return (String) doc.getFieldValue("ARN");
		}
		return null;
	}
	
	/**
	 * Return the results of a query
	 * @param solr the SolrServer
	 * @param squery the Solr Query
	 * @return the results of a query as SolrDocumentList
	 * @throws SolrServerException
	 */
	public static SolrDocumentList getSolrDocumentResult(SolrServer solr, SolrQuery squery) throws SolrServerException{
		//search
		QueryResponse response = solr.query(squery);
		return response.getResults();
	}
	
	/*
	 * TEST
	 */
	public static void main(String[] args) throws MalformedURLException, SolrServerException, IOException{
		
		//QueryUtil.optimize(IndexServerFactory.startSolr());
		 /*String query = "+center:GB +date:2012";
		 QueryUtil.deleteByQuery(IndexServerFactory.startSolr(), query);*/
		 
		String max = QueryUtil.selectMaxValue(IndexServerFactory.startSolr(), "ARN:TR20141*", "ARN");
		System.out.println(max);
	}

}
