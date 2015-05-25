package org.fao.oekc.agris.test_applications.eldis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jfcutils.files.write.TXTWriter;
import jfcutils.http.GETHttpRequest;

/**
 * Download IDS Data
 * http://api.ids.ac.uk/docs/
 * Your access GUID is a278dd90-85a7-465e-aa5f-cdb0adc910a0
 * @author celli
 *
 */
public class IDSWebAPI {
	
	/**
	 * Download data
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException{
		//http reader
		GETHttpRequest req = new GETHttpRequest();
		
		//txt writer 
		TXTWriter writer = new TXTWriter();
		String base_output_filepath = "C:/Users/celli/Documents/workspace_agris/agris_input/eldis/ids/ids_";
		
		//http options
		Map<String, String> http_opt = new HashMap<String, String>();
		http_opt.put("Accept", "application/xml");
		http_opt.put("Token-Guid", "a278dd90-85a7-465e-aa5f-cdb0adc910a0");
		
		//base_url
		String URL = "http://api.ids.ac.uk/openapi/eldis/search/documents/full?theme=C38&num_results=1000&start_offset=";
		int total_result = 4580;
		int start_offset = 0;
		int num_results=1000;
		
		while(start_offset<total_result){
			String url_to_use = URL+start_offset;
			String data = req.dereferenceURI(url_to_use, http_opt, 0);
			writer.writeString(data, base_output_filepath+start_offset+".xml");
			System.out.println("Written: "+base_output_filepath+start_offset+".xml");
			start_offset += num_results;
		}
		
	}

}
