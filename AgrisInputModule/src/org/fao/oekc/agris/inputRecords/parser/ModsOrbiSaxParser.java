package org.fao.oekc.agris.inputRecords.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jfcutils.util.StringUtils;

import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.CheckIndex;
import org.fao.oekc.agris.inputRecords.util.EscapeXML;
import org.fao.oekc.agris.inputRecords.util.IssnCleaner;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses ORBI MODS files
 * @author celli
 *
 */
public class ModsOrbiSaxParser extends DefaultHandler {

	//parser
	private List<AgrisApDoc> records;
	private AgrisApDoc current;
	
	private Set<String> codesToInclude;
	private Map<String,String> codes2keys;	//keys separated by &

	//index checker
	private CheckIndex indexChecker;

	//data
	private boolean isTitle;
	private boolean isAlternative;
	private boolean isTitleContent;
	private boolean isDate;
	private boolean isCreatorPersonal;
	private boolean isCreatorPersonalFamily;
	private boolean isLanguage;
	private boolean isPublisher;
	private boolean isSubject;
	private boolean isISSN;
	private boolean isJournalTitle;
	private boolean isJournalNumber;
	private boolean isJournalPages;
	private boolean isType;

	//to read the entire content
	private StringBuffer buffer; 
	private String tmp;
	private StringUtils cleaner;

	/**
	 * Create the parser for the document and start parsing.
	 * All booleans set to false until the corresponding element starts
	 * @param records list of result records
	 */
	public ModsOrbiSaxParser(List<AgrisApDoc> records, Set<String> subjectToBeIncluded) {
		try {
			this.cleaner = new StringUtils();
			this.indexChecker = new CheckIndex();
			this.codesToInclude = new HashSet<String>();
			this.codes2keys = new HashMap<String, String>();
			
			this.records = records;
			this.tmp = "";

			this.isTitle = false;
			this.isAlternative = false;
			this.isTitleContent = false;
			this.isDate = false;
			this.isCreatorPersonal = false;
			this.isCreatorPersonalFamily = false;
			this.isPublisher = false;
			this.isSubject = false;
			this.isLanguage = false;
			this.isJournalTitle = false;
			this.isJournalNumber = false;
			this.isISSN = false;
			this.isJournalPages = false;
			this.isType = false;
			
			this.checkList(subjectToBeIncluded);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void checkList(Set<String> subjectToBeIncluded){
		for(String s: subjectToBeIncluded){
			String[] split = s.split("_");
			if(split.length==2){
				this.codesToInclude.add(split[0]);
				this.codes2keys.put(split[0], split[1]);
			}
		}
	}

	////////////////////////////////////////////////////////////////////
	// Event handlers.
	////////////////////////////////////////////////////////////////////
	/**
	 * Recognize an element
	 */
	public void startElement (String namespaceURI, String localName, String rawName, Attributes atts) {		

		//RECORD
		if(rawName.equalsIgnoreCase("metadata")){
			this.current = new AgrisApDoc();
		}
		else
			//title
			if(rawName.equalsIgnoreCase("mods:titleInfo")){
				String type = atts.getValue("type");
				if(type!=null && (type.equalsIgnoreCase("alternative") || type.equalsIgnoreCase("translated")))
					this.isAlternative = true;
				else
					this.isTitle = true;
			}
			else
				//main title
				if(rawName.equalsIgnoreCase("mods:title") && (this.isTitle || this.isAlternative)){
					this.buffer = new StringBuffer();
					this.isTitleContent = true;
				}
				else
					//ags:citationTitle
					if(rawName.equalsIgnoreCase("mods:detail")){
						String type = atts.getValue("type");
						if(type!=null && type.equalsIgnoreCase("series")){
							this.buffer = new StringBuffer();
							this.isJournalTitle = true;			
						}
					}
					else
						//citation number volume
						if(rawName.equalsIgnoreCase("mods:detail")){
							String type = atts.getValue("type");
							if(type!=null && type.equalsIgnoreCase("volume")){
								this.buffer = new StringBuffer();
								this.isJournalNumber = true;			
							}
						}
						else
							//citation number pages
							if(rawName.equalsIgnoreCase("mods:extent")){
								String type = atts.getValue("unit");
								if(type!=null && type.equalsIgnoreCase("pages")){
									this.buffer = new StringBuffer();
									this.isJournalPages = true;			
								}
							}
							else
								//date
								if(rawName.equalsIgnoreCase("mods:dateIssued")){
									//normal dateIssued
									this.buffer = new StringBuffer();
									this.isDate = true;
								}
								else
									//identifiers
									if(rawName.equalsIgnoreCase("mods:identifier")){
										String type = atts.getValue("type");
										if(type!=null && type.equalsIgnoreCase("issn")){
											this.isISSN = true;
											this.buffer = new StringBuffer();
										}
									}
									else
										//creators
										if(rawName.equalsIgnoreCase("mods:namePart")){
											String type = atts.getValue("type");
											if(type!=null){
												this.buffer = new StringBuffer();
												if(type.equalsIgnoreCase("given"))
													this.isCreatorPersonal = true;
												if(type.equalsIgnoreCase("family"))
													this.isCreatorPersonalFamily = true;
											}
										}
										else
											if(rawName.equalsIgnoreCase("mods:publisher")){
												this.buffer = new StringBuffer();
												this.isPublisher = true;
											}
											else
												//subject
												if(rawName.equalsIgnoreCase("mods:classification")){
													this.isSubject = true;
													this.buffer = new StringBuffer();
												}
												else
													//language
													if(rawName.equalsIgnoreCase("mods:languageTerm")){
														this.buffer = new StringBuffer();
														this.isLanguage = true;
													}
													else
														//type
														if(rawName.equalsIgnoreCase("mods:genre")){
															this.buffer = new StringBuffer();
															this.isType = true;
														}
	}

	/**
	 * Extract content from XML
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) {

		if(this.current!=null) {
			//BUFFER reader
			if(this.isTitleContent || this.isDate || this.isJournalTitle || this.isJournalPages || isCreatorPersonalFamily || this.isSubject
					|| this.isISSN || this.isLanguage || this.isPublisher || this.isJournalNumber || this.isCreatorPersonal || this.isType){
				this.buffer.append(ch, start, length);
			}
		}
	}

	/**
	 * The end of an element. For big elements like ABSTRACT, to allow the buffering of all content
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceUri, String localName, String rawName)
	throws SAXException {

		if(this.current!=null) {

			//RECORD
			if(rawName.equalsIgnoreCase("metadata")){
				//mandatory fields
				if(this.current.getTitle2language().size()>0 && this.current.getDateIssued()!=null)
					this.records.add(current);
			}
			else
				// TITLE
				if(rawName.equalsIgnoreCase("mods:title") && this.isTitle && this.isTitleContent){
					this.isTitleContent  = false;
					String term = new String(this.buffer);
					if(term!=null && !term.trim().equals("") && !cleaner.isInteger(term)){
						//search Solr to see if title exists
						int occurrences = this.indexChecker.checkTitle(term);
						if(occurrences==0) {
							term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
							this.current.addTitle(term, "");
						}
						else {
							//this.excludedTitles.add(term);
							this.current = null;
						}
					}
				}
				else
					//alternative
					if(rawName.equalsIgnoreCase("mods:title") && this.isAlternative && this.isTitleContent){
						this.isTitleContent  = false;
						String term = new String(this.buffer);
						if(term!=null && !term.trim().equals("") && !cleaner.isInteger(term)){
							term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
							this.current.addAlternative(term, "");
						}
					}
					else
						//main_title
						if(rawName.equalsIgnoreCase("titleInfo")){
							this.isAlternative = false;
							this.isTitle = false;
						}
						else
							//journal
							if(rawName.equalsIgnoreCase("title") && this.isJournalTitle && this.isTitleContent){
								this.isTitleContent  = false;
								String term = new String(this.buffer);
								if(term!=null && !term.trim().equals("")){
									term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
									this.current.setCitationTitle(term);
								}
							}
							else
								//journal title
								if(rawName.equalsIgnoreCase("mods:detail") && this.isJournalTitle){
									this.isJournalTitle  = false;
									String term = new String(this.buffer);
									if(term!=null && !term.trim().equals("")){
										term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
										this.current.setCitationTitle(term);
									}
								}
								else
									//citation number
									if(rawName.equalsIgnoreCase("mods:detail") && this.isJournalNumber){
										this.isJournalNumber  = false;
										String term = new String(this.buffer);
										if(term!=null && !term.trim().equals("")){
											this.current.setCitationNumber("v."+term);
										}
									}
									else
										//citation number pages
										if(rawName.equalsIgnoreCase("mods:extent") && this.isJournalPages){
											this.isJournalPages  = false;
											String term = new String(this.buffer);
											if(term!=null && !term.trim().equals("")){
												this.current.setExtent("p."+term);
											}
										}
										else
											//date
											if(rawName.equalsIgnoreCase("mods:dateIssued") && this.isDate){
												this.isDate  = false;
												String term = new String(this.buffer);
												if(term!=null && !term.trim().equals("")){
													this.current.setDateIssued(term);
												}
											}
											else
												//identifiers
												if(rawName.equalsIgnoreCase("mods:identifier") && this.isISSN){
													this.isISSN = false;
													String term = new String(this.buffer);
													if(term!=null && !term.trim().equals("")){
														//naive issn cleaning and add -
														if(!IssnCleaner.isISSN(term))
															term = IssnCleaner.cleanISSN(term);
														if(IssnCleaner.isISSN(term))
															this.current.addIssn(term);
													}
												}
												else 
													//creator name
													if(rawName.equalsIgnoreCase("mods:namePart") && this.isCreatorPersonal){
														this.isCreatorPersonal  = false;
														String term = new String(this.buffer);
														if(term!=null && !term.trim().equals("") && !cleaner.isInteger(term)){
															this.tmp = term;
														}
													}
													else 
														//creator family name
														if(rawName.equalsIgnoreCase("mods:namePart") && this.isCreatorPersonalFamily){
															this.isCreatorPersonalFamily  = false;
															String term = new String(this.buffer);
															if(term!=null && !term.trim().equals("") && !cleaner.isInteger(term)){
																if(this.tmp!=null && this.tmp.length()>0)
																	this.tmp = term + ", "+this.tmp;
																else
																	this.tmp = term;
																this.current.addCreatorPersonal(this.tmp);
															}
														}
														else
															//restore tmp
															if(rawName.equalsIgnoreCase("mods:name"))
																this.tmp = "";
															else
																//publisher
																if(rawName.equalsIgnoreCase("mods:publisher") && this.isPublisher){
																	this.isPublisher = false;
																	String term = new String(this.buffer);
																	if(term!=null && !term.trim().equals("")){
																		this.current.addPublisherName(term);
																	}
																}
																else
																	//subject
																	if(rawName.equalsIgnoreCase("mods:classification") && this.isSubject){
																		this.isSubject = false;
																		String term = new String(this.buffer);
																		if(term!=null && !term.trim().equals("")){
																			//extract code
																			if(term.endsWith("]") && term.length()>=5){
																				String code = term.substring(term.length()-4, term.length()-1);
																				if(this.codesToInclude.contains(code.toUpperCase())){
																					String keys = this.codes2keys.get(code.toUpperCase());
																					String[] split = keys.split(" & ");
																					for(String s: split)
																						this.current.addFreeSubject(s);
																				}
																				else
																					this.current = null;
																			}
																		}
																	}
																	else
																		//language
																		if(rawName.equalsIgnoreCase("mods:languageTerm") && this.isLanguage){
																			this.isLanguage = false;
																			String term = new String(this.buffer);
																			if(term!=null && !term.trim().equals("")){
																				this.current.addLanguage(term, "dcterms:ISO639-2");
																			}
																		}
																		else
																			//type
																			if(rawName.equalsIgnoreCase("mods:genre") && this.isType){
																				this.isType = false;
																				String term = new String(this.buffer);
																				if(term!=null && !term.trim().equals("")){
																					this.current.addType(term);
																				}
																			}
		}
	}

}
