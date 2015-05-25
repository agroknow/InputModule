package org.fao.oekc.agris.inputRecords.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import jfcutils.util.LanguageTranslation;

import org.fao.oekc.agris.inputRecords.dom.AGRISAttributes;
import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.CheckIndex;
import org.fao.oekc.agris.inputRecords.util.EscapeXML;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * ARN should be defined here, not in the writer class
 * Duplicates removal based on titles
 * @author celli
 *
 */
public class OvidSaxParser extends DefaultHandler{
	
	private String yearRange = "2005";

	//parser
	private List<AgrisApDoc> records;
	private AgrisApDoc current;

	//index checker
	private CheckIndex indexChecker; 

	//center mapping: name lowercased TO center code
	private Map<String, String> centerName2Code;

	//data
	private boolean isTitle;
	private boolean isAn;
	private boolean isCi;
	private boolean isSubjectcateg;
	private boolean creatorPersonal;
	private boolean isAbstract;
	private boolean isAgrovoc;
	private boolean isIdentifier;
	private boolean isType;
	private boolean isLanguage;
	private boolean isnote;
	private boolean isSerialTitle;
	private boolean isSerialNumber;
	private boolean isDate;
	private boolean isInstitution;
	private boolean isConference;
	private boolean isPublisher;
	private boolean isSource;

	//support variables
	private String an;
	private String ci;
	private String ip;//issue for extent
	private String ca;//conference or corporate, depending on <pt>
	private String lang;
	
	private List<String> ti;
	private List<String> ot;

	private StringBuffer buffer; //to read the entire content

	/**
	 * Create the parser for the document and start parsing.
	 * All booleans set to false until the corresponding element starts
	 * @param records list of result records
	 * @param centersMapping set of string in the format CENTER_CODE\tCENTER_NAME
	 */
	public OvidSaxParser(List<AgrisApDoc> records, Set<String> centersMapping) {
		try {
			this.indexChecker = new CheckIndex();
			this.ti = new ArrayList<String>();
			this.ot = new ArrayList<String>();
			
			this.records = records;
			this.isTitle = false;
			this.isAn = false;
			this.isCi = false;
			this.isSubjectcateg = false;
			this.isAbstract = false;
			this.isAgrovoc = false;
			this.creatorPersonal = false;
			this.isIdentifier = false;
			this.isType = false;
			this.isnote = false;
			this.isLanguage = false;
			this.isSerialTitle = false;
			this.isSerialNumber = false;
			this.isDate = false;
			this.isConference = false;
			this.isInstitution = false;
			this.isPublisher = false;
			this.isSource = false;
			//extract centers mapping
			this.extractCenterMapping(centersMapping);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Center mapping builder
	 */
	private void extractCenterMapping(Set<String> centersMapping){
		//build the output
		this.centerName2Code = new HashMap<String, String>();
		//scan lines
		for(String s: centersMapping){
			String[] code2name = s.split("\t");
			if(code2name.length==2)
				this.centerName2Code.put(code2name[0], code2name[1].toLowerCase());
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
		if(rawName.equalsIgnoreCase("ovidrec")){
			this.current = new AgrisApDoc();
			this.ci = null;
			this.an = null;
			this.ti = new ArrayList<String>();
			this.ot = new ArrayList<String>();
			this.lang = null;
		}
		else
			if(this.current!=null) {
				//title
				if(rawName.equalsIgnoreCase("ti") || rawName.equalsIgnoreCase("ot")){
					this.buffer = new StringBuffer();
					this.isTitle = true;
				}
				else
					if(rawName.equalsIgnoreCase("an")){
						this.buffer = new StringBuffer();
						this.isAn = true;
					}
					else
						if(rawName.equalsIgnoreCase("ci")){
							this.buffer = new StringBuffer();
							this.isCi = true;
						}
						else
							//creator personal
							if(rawName.equalsIgnoreCase("au")){
								this.buffer = new StringBuffer();
								this.creatorPersonal = true;
							}
							else
								//subject categories
								if(rawName.equalsIgnoreCase("pc")){
									this.buffer = new StringBuffer();
									this.isSubjectcateg = true;
								}
								else
									//abstract
									if(rawName.equalsIgnoreCase("ea") || rawName.equalsIgnoreCase("fa") || rawName.equalsIgnoreCase("sa")
											|| rawName.equalsIgnoreCase("ao")){
										this.buffer = new StringBuffer();
										this.isAbstract = true;
									}
									else
										//agrovoc
										if(rawName.equalsIgnoreCase("ie") || rawName.equalsIgnoreCase("fm") || rawName.equalsIgnoreCase("sm")){
											this.buffer = new StringBuffer();
											this.isAgrovoc = true;
										}
										else
											//issn - isbn
											if(rawName.equalsIgnoreCase("ib") || rawName.equalsIgnoreCase("is") || rawName.equalsIgnoreCase("pn")
													|| rawName.equalsIgnoreCase("rn")){
												this.buffer = new StringBuffer();
												this.isIdentifier = true;
											}
											else
												//type
												if(rawName.equalsIgnoreCase("pt")){
													this.buffer = new StringBuffer();
													this.isType = true;
												}
												else
													//language
													if(rawName.equalsIgnoreCase("lg")){
														this.buffer = new StringBuffer();
														this.isLanguage = true;
													}
													else
														//note
														if(rawName.equalsIgnoreCase("nt")){
															this.buffer = new StringBuffer();
															this.isnote = true;
														}
														else
															//serial title
															if(rawName.equalsIgnoreCase("st") || rawName.equalsIgnoreCase("jn")){
																this.buffer = new StringBuffer();
																this.isSerialTitle = true;
															}
															else
																//serial number
																if(rawName.equalsIgnoreCase("vo")){
																	this.buffer = new StringBuffer();
																	this.isSerialNumber = true;
																}
																else
																	//extent
																	if(rawName.equalsIgnoreCase("pg")){
																		this.buffer = new StringBuffer();
																		this.isSerialNumber = true;
																	}
																	else
																		//issue
																		if(rawName.equalsIgnoreCase("ip")){
																			this.buffer = new StringBuffer();
																			this.isSerialNumber = true;
																		}
															else
																//date
																if(rawName.equalsIgnoreCase("dp") || rawName.equalsIgnoreCase("py")
																		|| rawName.equalsIgnoreCase("yr")){
																	this.buffer = new StringBuffer();
																	this.isDate = true;
																}
																else
																	//conference
																	if(rawName.equalsIgnoreCase("ca")){
																		this.buffer = new StringBuffer();
																		this.isConference = true;
																	}
																	else
																		//institution
																		if(rawName.equalsIgnoreCase("in")){
																			this.buffer = new StringBuffer();
																			this.isInstitution = true;
																		}
																	else
																		//publisher
																		if(rawName.equalsIgnoreCase("pu")){
																			this.buffer = new StringBuffer();
																			this.isPublisher = true;
																		}
																		else
																			//source
																			if(rawName.equalsIgnoreCase("os")){
																				this.buffer = new StringBuffer();
																				this.isSource = true;
																			}
			}


	}

	/**
	 * Extract content from XML
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) {

		if(this.current!=null) {
			//BUFFER reader
			if(this.creatorPersonal || this.isTitle || isAn || isCi || this.isSubjectcateg || isAbstract
					|| this.isAgrovoc || this.isIdentifier || this.isType || this.isLanguage || this.isnote
					|| this.isSerialTitle || this.isDate || this.isConference || this.isPublisher
					|| this.isSource || this.isSerialNumber || isInstitution){
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
			if(rawName.equalsIgnoreCase("ovidrec")){
				//ARN generation
				if(this.an!=null && this.ci!=null){
					//read center code
					String code = this.centerName2Code.get(this.ci.toLowerCase());
					
					if(code!=null && code.length()>0) {
						
						//check 7 bit
						if(code.length()==2)
							this.an = this.an.replace("-", "0");
						else {
							String sevenBit = code.substring(2);
							code = code.substring(0, 2);
							this.an = this.an.replace("-", sevenBit);
						}
						String arn = code.toUpperCase()+this.an;
						
						//search Solr to see if ARN exists
						int occurrences = this.indexChecker.checkArn(arn);		
						if(occurrences==0) {
							current.setARN(arn);
						}
						else
							System.out.println("-- ERROR: no ARN already in the index "+arn);
					}
					else {
						System.out.println("-- ERROR: no ARN for "+this.ci);
					}
				}
				
				//TITLES
				if(this.ot.size()>0){
					//ot in titles and ti in alternative
					for(String t: this.ot){
						this.current.addTitle(t, lang);
					}
					for(String t: this.ti){
						this.current.addAlternative(t, "eng");
					}
				}
				else {
					for(String t: this.ti){
						this.current.addTitle(t, "eng");
					}
				}
				
				//check CORPORATE
				if(this.ca!=null)
					this.current.addCreatorCorporate(this.ca);
				
				//mandatory fields
				if(this.current.getTitle2language().size()>0 && this.current.getARN()!=null)
					this.records.add(current);
			}
			else
				// TITLE
				if(rawName.equalsIgnoreCase("ti") && this.isTitle){
					this.isTitle = false;
					String term = new String(this.buffer);
					if(term!=null && !term.trim().equals("")){
						//search Solr to see if title exists
						int occurrences = this.indexChecker.checkTitle(term);	
						//check ending dot
						if(occurrences<0)
							occurrences = 0;
						if(term.endsWith(".")){
							String tmp = term.substring(0, term.length()-1);
							occurrences += this.indexChecker.checkTitle(tmp);	
						}
						if(occurrences==0) {
							term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
							this.ti.add(term);
						}
						else 
							this.current = null;
					}
				}
				else
					// ALTERNATIVR TITLE
					if(rawName.equalsIgnoreCase("ot") && this.isTitle){
						this.isTitle = false;
						String term = new String(this.buffer);
						if(term!=null && !term.trim().equals("")){
							//search Solr to see if title exists
							int occurrences = this.indexChecker.checkTitle(term);	
							//check ending dot
							if(occurrences<0)
								occurrences = 0;
							if(term.endsWith(".")){
								String tmp = term.substring(0, term.length()-1);
								occurrences += this.indexChecker.checkTitle(tmp);	
							}
							if(occurrences==0) {
								term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
								this.ot.add(term);
							}
							else 
								this.current = null;
						}
					}
					else
						//AN: second part of the ARN
						if(rawName.equalsIgnoreCase("an") && this.isAn){
							this.isAn = false;
							String term = new String(this.buffer);
							if(term!=null && !term.trim().equals("")){
								this.an = term;
							}
							//check year
							if(this.an!=null){
								String year = this.an.substring(0, 4);
								if(this.yearRange.compareTo(year)<=0)
									this.current = null;
							}
						}
						else
							//CI: first part of the ARN
							if(rawName.equalsIgnoreCase("ci") && this.isCi){
								this.isCi = false;
								String term = new String(this.buffer);
								if(term!=null && !term.trim().equals("")){
									this.ci = term;
								}
							}
							else
								//AU: author
								if(rawName.equalsIgnoreCase("au") && this.creatorPersonal){
									this.creatorPersonal = false;
									String term = new String(this.buffer);
									if(term!=null && !term.trim().equals("")){
										this.current.addCreatorPersonal(term);
									}
								}
								else
									//CC: subject cat
									if(rawName.equalsIgnoreCase("pc") && this.isSubjectcateg){
										this.isSubjectcateg = false;
										String term = new String(this.buffer);
										if(term!=null && !term.trim().equals("")){
											//split on semicolon
											String[] cat = term.split("; ");
											for(String c: cat){
												if(c.length()<=3)
													this.current.addSubjClass(c, "ags:ASC");
											}
										}
									}
									else
										//EA: English Abstract
										if(rawName.equalsIgnoreCase("ea") && this.isAbstract){
											this.isAbstract = false;
											String term = new String(this.buffer);
											if(term!=null && !term.trim().equals("")){
												this.current.addAbstract(term, "eng");
											}
										}
										else
											//FA: French Abstract
											if(rawName.equalsIgnoreCase("fa") && this.isAbstract){
												this.isAbstract = false;
												String term = new String(this.buffer);
												if(term!=null && !term.trim().equals("")){
													this.current.addAbstract(term, "fra");
												}
											}
											else
												//SA: Spanish Abstract
												if(rawName.equalsIgnoreCase("sa") && this.isAbstract){
													this.isAbstract = false;
													String term = new String(this.buffer);
													if(term!=null && !term.trim().equals("")){
														this.current.addAbstract(term, "esp");
													}
												}
												else
													//AO: Other Abstract
													if(rawName.equalsIgnoreCase("ao") && this.isAbstract){
														this.isAbstract = false;
														String term = new String(this.buffer);
														if(term!=null && !term.trim().equals("")){
															this.current.addAbstract(term, null);
														}
													}
													else
														//IE: English Agrovoc
														if(rawName.equalsIgnoreCase("ie") && this.isAgrovoc){
															this.isAgrovoc = false;
															String term = new String(this.buffer);
															if(term!=null && !term.trim().equals("")){
																this.current.addAgrovoc(term, new AGRISAttributes("ags:AGROVOC","eng"));
															}
														}
														else
															//FM: French Agrovoc
															if(rawName.equalsIgnoreCase("fm") && this.isAgrovoc){
																this.isAgrovoc = false;
																String term = new String(this.buffer);
																if(term!=null && !term.trim().equals("")){
																	this.current.addAgrovoc(term, new AGRISAttributes("ags:AGROVOC","fra"));
																}
															}
															else
																//SM: Spanish Agrovoc
																if(rawName.equalsIgnoreCase("sm") && this.isAgrovoc){
																	this.isAgrovoc = false;
																	String term = new String(this.buffer);
																	if(term!=null && !term.trim().equals("")){
																		this.current.addAgrovoc(term, new AGRISAttributes("ags:AGROVOC","esp"));
																	}
																}
																else
																	//ISBN
																	if(rawName.equalsIgnoreCase("ib") && this.isIdentifier){
																		this.isIdentifier = false;
																		String term = new String(this.buffer);
																		if(term!=null && !term.trim().equals("")){
																			this.current.addIdentifier(term, "ags:ISBN");
																		}
																	}
																	else
																		//RN
																		if(rawName.equalsIgnoreCase("rn") && this.isIdentifier){
																			this.isIdentifier = false;
																			String term = new String(this.buffer);
																			if(term!=null && !term.trim().equals("")){
																				this.current.addIdentifier(term, "ags:RN");
																			}
																		}
																		else
																			//PN
																			if(rawName.equalsIgnoreCase("pn") && this.isIdentifier){
																				this.isIdentifier = false;
																				String term = new String(this.buffer);
																				if(term!=null && !term.trim().equals("")){
																					this.current.addIdentifier(term, "ags:PN");
																				}
																			}
																			else
																				//ISSN
																				if(rawName.equalsIgnoreCase("is") && this.isIdentifier){
																					this.isIdentifier = false;
																					String term = new String(this.buffer);
																					if(term!=null && !term.trim().equals("")){
																						this.current.addIssn(term);
																					}
																				}
																				else
																					//type
																					if(rawName.equalsIgnoreCase("pt") && this.isType){
																						this.isType = false;
																						String term = new String(this.buffer);
																						if(term!=null && !term.trim().equals("")){
																							this.current.addType(term);
																						}
																						//check conference or corporate
																						if(this.ca!=null){
																							if(term.equalsIgnoreCase("Conference")){
																								this.current.addCreatorConference(this.ca);
																								this.ca = null;
																							}
																						}
																					}
																					else
																						//language
																						if(rawName.equalsIgnoreCase("lg") && this.isLanguage){
																							this.isLanguage = false;
																							String term = new String(this.buffer);
																							if(term!=null && !term.trim().equals("")){
																								//try to translate to 3 digits
																								term = (new LanguageTranslation()).translate2threedigitsEn(term);
																								if(term!=null && term.length()!=3)
																									this.current.addLanguage(term, "");
																								else if(term!=null && term.length()==3){
																									this.current.addLanguage(term, "dcterms:ISO639-2");
																									this.lang = term;
																								}
																							}
																						}
																						else
																							//note
																							if(rawName.equalsIgnoreCase("nt") && this.isnote){
																								this.isnote = false;
																								String term = new String(this.buffer);
																								if(term!=null && !term.trim().equals("")){
																									this.current.setDescrNotes(term);
																								}
																							}
																							else
																								//date
																								if(rawName.equalsIgnoreCase("dp") && this.isDate){
																									this.isDate = false;
																									String term = new String(this.buffer);
																									if(term!=null && !term.trim().equals("")
																											&& this.current.getDateIssued()==null){
																										this.current.setDateIssued(term);
																									}
																								}
																								else
																									//date
																									if(rawName.equalsIgnoreCase("py") && this.isDate
																											&& this.current.getDateIssued()==null){
																										this.isDate = false;
																										if(this.current.getDateIssued()==null){
																											String term = new String(this.buffer);
																											if(term!=null && !term.trim().equals("")){
																												this.current.setDateIssued(term);
																											}
																										}
																									}
																									else
																										//date
																										if(rawName.equalsIgnoreCase("yr") && this.isDate){
																											this.isDate = false;
																											String term = new String(this.buffer);
																											if(term!=null && !term.trim().equals("")){
																												this.current.setDateIssued(term);
																											}
																										}
																										else
																											//serial title
																											if(rawName.equalsIgnoreCase("st") && this.isSerialTitle){
																												this.isSerialTitle = false;
																												String term = new String(this.buffer);
																												if(term!=null && !term.trim().equals("")){
																													String[] t = term.split(", ");
																													this.current.setCitationTitle(t[0]);
																													//check citation number
																													if(t.length>1)
																														this.current.setCitationNumber(t[1]);
																												}
																											}
																											else
																												//serial title
																												if(rawName.equalsIgnoreCase("jn") && this.isSerialTitle){
																													this.isSerialTitle = false;
																													String term = new String(this.buffer);
																													if(term!=null && !term.trim().equals("")){
																														this.current.setCitationTitle(term);
																													}
																												}
																												else
																													//serial number
																													if(rawName.equalsIgnoreCase("vo") && this.isSerialNumber){
																														this.isSerialNumber = false;
																														String term = new String(this.buffer);
																														if(term!=null && !term.trim().equals("")){
																															//naive cleaning
																															term = term.replace("<", "(");
																															term = term.replace(">", ")");
																															this.current.setCitationNumber(term);
																														}
																													}
																													else
																														//extent
																														if(rawName.equalsIgnoreCase("pg") && this.isSerialNumber){
																															this.isSerialNumber = false;
																															String term = new String(this.buffer);
																															if(term!=null && !term.trim().equals("")){
																																term = "v."+term;
																																if(this.ip!=null){
																																	term = term + "("+this.ip+")";
																																	this.ip = null;
																																}
																																this.current.setExtent(term); 
																															}
																														}
																														else
																															//issue
																															if(rawName.equalsIgnoreCase("ip") && this.isSerialNumber){
																																this.isSerialNumber = false;
																																String term = new String(this.buffer);
																																if(term!=null && !term.trim().equals("")){
																																	this.ip = term; 
																																}
																															}
																												else
																													//CA conference or corporate
																													if(rawName.equalsIgnoreCase("ca") && this.isConference){
																														this.isConference = false;
																														String term = new String(this.buffer);
																														if(term!=null && !term.trim().equals("")){
																															this.ca = term;
																														}
																													}
																													else
																														//in: institution
																														if(rawName.equalsIgnoreCase("in") && this.isInstitution){
																															this.isInstitution = false;
																															String term = new String(this.buffer);
																															if(term!=null && !term.trim().equals("")){
																																this.current.addCreatorCorporate(term);
																															}
																														}
																													else
																														//PU: publisher
																														if(rawName.equalsIgnoreCase("pu") && this.isPublisher){
																															this.isPublisher = false;
																															String term = new String(this.buffer);
																															if(term!=null && !term.trim().equals("")){
																																//case 1: it is simply a date
																																String express = "[A-Za-z][A-Za-z][A-Za-z].[0-9][0-9][0-9][0-9][/.]";
																																if(Pattern.matches(express, term))
																																	//date
																																	if(this.current.getDateIssued()==null)
																																		this.current.setDateIssued(term.substring(0, term.length()-1));
																																	else
																																		this.current.addPublisherName(term);
																															}
																														}
																														else
																															//OS: source
																															if(rawName.equalsIgnoreCase("os") && this.isSource){
																																this.isSource = false;
																																String term = new String(this.buffer);
																																if(term!=null && !term.trim().equals("")){
																																	this.current.setSource(term);
																																}
																															}

		}
	}

}
