package org.fao.oekc.agris.inputRecords.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Read a text files in which each line contains a key separated by equals "=" from comma-space-separated values. 
 * @author celli
 *
 */
public class ReadCommaSeparatedTxtArray {

	//already parsed map
	private Map<String, List<String>> mapping;

	// Singleton
	private static ReadCommaSeparatedTxtArray instance;

	public static synchronized ReadCommaSeparatedTxtArray getInstance() {
		/* Singleton: lazy creation */
		if(instance == null)
			instance = new ReadCommaSeparatedTxtArray();
		return instance;
	}

	public ReadCommaSeparatedTxtArray(){
		this.mapping = null;
	}


	/**
	 * Extract the map of a text files in which each line contains a map: the key is a value separated by an = symbol,
	 * then values are comma-space-separated. 
	 * @param lines 
	 * @return the map of a text files in which each line contains a map: the key is a value separated by an = symbol, then values are comma-space-separated. 
	 */
	public synchronized Map<String, List<String>> readCommaSeparatedTxtArray(Set<String> lines){
		if(this.mapping==null){
			this.mapping = new HashMap<String, List<String>>();
			for(String s: lines){
				String[] array = s.split("=");
				if(array.length==2){
					List<String> tmp = new ArrayList<String>();
					String[] array2 = array[1].split(", ");
					for(int i=0; i<array2.length; i++)
						tmp.add(array2[i]);
					this.mapping.put(array[0], tmp);
				}
			}
			System.out.println("++ mapping URL->Agrovoc loaded");
		}
		return this.mapping;
	}

}
