package org.fao.oekc.agris.inputRecords.executor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.parser.AJOLSaxParser;
import org.fao.oekc.agris.inputRecords.parser.AgricolaSaxParser;
import org.fao.oekc.agris.inputRecords.parser.AgrisApSaxParser;
import org.fao.oekc.agris.inputRecords.parser.BHLSaxParser;
import org.fao.oekc.agris.inputRecords.parser.CiradSaxParser;
import org.fao.oekc.agris.inputRecords.parser.DOAJSaxParser;
import org.fao.oekc.agris.inputRecords.parser.EldisSaxParser;
import org.fao.oekc.agris.inputRecords.parser.MarcSaxParser;
import org.fao.oekc.agris.inputRecords.parser.ModsOrbiSaxParser;
import org.fao.oekc.agris.inputRecords.parser.ModsSaxParser;
import org.fao.oekc.agris.inputRecords.parser.OvidSaxParser;
import org.fao.oekc.agris.inputRecords.parser.SimpleDCSaxParser;
import org.fao.oekc.agris.inputRecords.parser.UsamvSaxParser;
import org.fao.oekc.agris.inputRecords.util.ReadCommaSeparatedTxtArray;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Choose the right parser for the XML source format
 * @author celli
 *
 */
public class XMLDispatcher {

	/**
	 * Dispatcher
	 * @param sourceFormat the source format like simpledc, ovid...
	 * @param records records cleaed and not yet indexed
	 * @param arnPrefix countrycode+sub.year+subcentercode
	 * @param titlesAdded list of titles already added for the current set
	 * @param globalDuplRem flag for global duplicates removal
	 * @param lines lines of the input text file (also null, it is optional), used for example to filter ISSNs
	 * @return the DefaultHandler related to the sourceFormat
	 */
	public DefaultHandler dispatchXML(String sourceFormat, List<AgrisApDoc> records, String arnPrefix,
			List<String> titlesAdded, boolean globalDuplRem, Set<String> lines){
		//parse the file
		if(sourceFormat.equalsIgnoreCase("agrisap")){
			//System.out.println("AGRIS AP");
			return new AgrisApSaxParser(records, globalDuplRem);
		} 
		else 
			if(sourceFormat.equalsIgnoreCase("simpledc")){
				//System.out.println("Simple DC");
				return new SimpleDCSaxParser(records, arnPrefix, titlesAdded);
			}
			else 
				if(sourceFormat.equalsIgnoreCase("doaj")){
					return new DOAJSaxParser(records, globalDuplRem, lines);
					//System.out.println("DOAJ");
				}
				else 
					if(sourceFormat.equalsIgnoreCase("ovid")){
						//System.out.println("OVID");
						return new OvidSaxParser(records, lines);
					}
					else 
						if(sourceFormat.equalsIgnoreCase("mods")){
							return new ModsSaxParser(records, arnPrefix.substring(0, 2), arnPrefix.substring(6, 7));
							//System.out.println("MODS");
						}
						else
							if(sourceFormat.equalsIgnoreCase("eldis")){
								//create map of agrovoc keywords
								Map<String, List<String>> autotagger = ReadCommaSeparatedTxtArray.getInstance().readCommaSeparatedTxtArray(lines);
								return new EldisSaxParser(records, autotagger);
								//System.out.println("ELDIS");
							}
							else 
								if(sourceFormat.equalsIgnoreCase("ajol")){
									return new AJOLSaxParser(records, lines);
									//System.out.println("ajol");
								}
								else 
									if(sourceFormat.equalsIgnoreCase("agricola")){
										return new AgricolaSaxParser(records);
									}
									else 
										if(sourceFormat.equalsIgnoreCase("cirad")){
											return new CiradSaxParser(records);
										}
										else 
											if(sourceFormat.equalsIgnoreCase("orbi")){
												return new ModsOrbiSaxParser(records, lines);
											}
											else 
												if(sourceFormat.equalsIgnoreCase("usamv")){
													return new UsamvSaxParser(records);
												}
												else 
													if(sourceFormat.equalsIgnoreCase("marcxml")){
														return new MarcSaxParser(records);
													}
													else 
														if(sourceFormat.equalsIgnoreCase("bhl")){
															return new BHLSaxParser(records);
														}
									else{
										return null;
									}
	}

}
