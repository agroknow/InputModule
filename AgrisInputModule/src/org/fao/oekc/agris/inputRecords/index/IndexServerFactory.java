package org.fao.oekc.agris.inputRecords.index;

import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.BinaryRequestWriter;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.fao.oekc.agris.Defaults;

/**
 * This class builds an instance of the Solr server to query data
 * @author celli
 *
 */
public class IndexServerFactory {
	
	//instance
	private static SolrServer server;
	
	//singleton for the default server
	public static synchronized SolrServer startSolr() throws MalformedURLException{
		if(server==null)
			startRemoteSolr();
		return server;
	}
	
	//starts the connection to a remote solr server
	private static void startRemoteSolr() throws MalformedURLException{
		startRemoteSolr(Defaults.getString("SolrServer"));
	}
	
	private static void startRemoteSolr(String remotePath) throws MalformedURLException{
		CommonsHttpSolrServer solr = new CommonsHttpSolrServer(remotePath);
		solr.setRequestWriter(new BinaryRequestWriter());	//java binary format
		server = solr;
	}

}
