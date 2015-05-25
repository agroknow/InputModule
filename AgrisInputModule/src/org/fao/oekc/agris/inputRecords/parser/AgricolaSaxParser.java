package org.fao.oekc.agris.inputRecords.parser;

//import java.io.IOException;
//import java.util.LinkedList;
import java.util.List;

//import jfcutils.files.write.TXTWriter;

import org.fao.oekc.agris.inputRecords.dom.AGRISAttributes;
import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.CheckIndex;
import org.fao.oekc.agris.inputRecords.util.EscapeXML;
import org.fao.oekc.agris.inputRecords.util.IssnCleaner;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses AGRICOLA MODS files
 * @author celli
 *
 */
public class AgricolaSaxParser extends DefaultHandler {

	//parser
	private List<AgrisApDoc> records;
	private AgrisApDoc current;

	//index checker
	private CheckIndex indexChecker;

	//data
	private boolean isTitle;
	private boolean isAlternative;
	private boolean isTitleContent;
	private boolean isSubTitleContent;
	private boolean isAbstract;

	private boolean isRelatedItem;
	private boolean isGenre;
	private boolean isDate;
	private boolean isJournalTitle;
	private boolean isJournalNumber;

	private boolean isISSN;
	private boolean isISBN;
	private boolean isURI;

	private boolean isCreatorPersonal;
	private boolean isCreatorCorporate;
	private boolean isCreatorConference;
	private boolean isCreatorName;
	private boolean isPublisher;
	private boolean isPublisherPlace;

	private boolean isPartOf;
	private boolean isSubject;
	private boolean isTopic;
	private boolean isAgrovoc;
	private boolean isAgrovocSubject;
	private boolean isLanguage;
	private boolean isExtent;
	private boolean isFormat;
	private boolean isNote;

	//to read the entire content
	private StringBuffer buffer; 
	private String tmp;

	/**
	 * Create the parser for the document and start parsing.
	 * All booleans set to false until the corresponding element starts
	 * @param records list of result records
	 */
	public AgricolaSaxParser(List<AgrisApDoc> records) {
		try {
			this.indexChecker = new CheckIndex();
			this.records = records;
			this.tmp = "";

			this.isTitle = false;
			this.isAlternative = false;
			this.isTitleContent = false;
			this.isSubTitleContent = false;
			this.isRelatedItem = false;
			this.isGenre = false;
			this.isDate = false;
			this.isNote = false;
			this.isAbstract = false;

			this.isPartOf = false;
			this.isURI = false;
			this.isCreatorPersonal = false;
			this.isCreatorName = false;
			this.isPublisherPlace = false;
			this.isPublisher = false;
			this.isSubject = false;
			this.isTopic = false;
			this.isAgrovoc = false;
			this.isAgrovocSubject = false;
			this.isLanguage = false;
			this.isExtent = false;
			this.isFormat = false;
			this.isCreatorCorporate = false;
			this.isCreatorConference = false;
			this.isJournalTitle = false;
			this.isJournalNumber = false;
			this.isISSN = false;
			this.isISBN = false;
		}
		catch(Exception e) {
			e.printStackTrace();
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
		if(rawName.equalsIgnoreCase("mods")){
			this.current = new AgrisApDoc();
		}
		else
			if(this.current!=null) {
				//exclude series
				if(rawName.equalsIgnoreCase("genre")){
					String authority = atts.getValue("authority");
					if(authority!=null && authority.equalsIgnoreCase("marc")){
						this.buffer = new StringBuffer();
						this.isGenre = true;
					}	
				}
				else
					//title
					if(rawName.equalsIgnoreCase("titleInfo") && !this.isRelatedItem){
						String type = atts.getValue("type");
						if(type!=null && (type.equalsIgnoreCase("alternative") || type.equalsIgnoreCase("translated")))
							this.isAlternative = true;
						else
							this.isTitle = true;
					}
					else
						//ags:citationTitle
						if(rawName.equalsIgnoreCase("titleInfo") && this.isRelatedItem){
							String type = atts.getValue("type");
							if( (type!=null && type.equalsIgnoreCase("abbreviated") &&this.current.getCitationTitle()==null)
									|| (type==null || type.equalsIgnoreCase("")))
								this.isJournalTitle = true;			
						}
						else
							//main title
							if(rawName.equalsIgnoreCase("title") && (this.isTitle || this.isAlternative || this.isJournalTitle)){
								this.buffer = new StringBuffer();
								this.isTitleContent = true;
							}
							else
								//supplement title
								if(rawName.equalsIgnoreCase("subTitle") && this.isTitle){
									this.buffer = new StringBuffer();
									this.isSubTitleContent = true;
								}
								else
									//citation number
									if(rawName.equalsIgnoreCase("text") && this.isRelatedItem){
										this.buffer = new StringBuffer();
										this.isJournalNumber = true;
									}
									else
										//abstract
										if(rawName.equalsIgnoreCase("abstract")){
											this.buffer = new StringBuffer();
											this.isAbstract = true;
										}
										else
											//related item
											if(rawName.equalsIgnoreCase("relatedItem")){
												this.isRelatedItem = true;
												String type = atts.getValue("type");
												if(type!=null && type.equalsIgnoreCase("preceding"))
													this.isJournalTitle = true;
											}
											else
												//date
												if(rawName.equalsIgnoreCase("dateIssued")){
													String encoding = atts.getValue("encoding");
													String point = atts.getValue("point");
													if(encoding!=null && encoding.equalsIgnoreCase("marc")){
														if(point==null || !point.equalsIgnoreCase("end")){
															this.buffer = new StringBuffer();
															this.isDate = true;
														}
													}	
													else {
														//normal dateIssued
														this.buffer = new StringBuffer();
														this.isDate = true;
													}
												}
												else
													//identifiers
													if(rawName.equalsIgnoreCase("identifier")){
														String type = atts.getValue("type");
														if(type!=null && !type.equalsIgnoreCase("lccn")){
															if(type.equalsIgnoreCase("isbn"))
																this.isISBN = true;
															else if(type.equalsIgnoreCase("issn"))
																this.isISSN = true;
															else if(type.equalsIgnoreCase("uri"))
																this.isURI = true;
															this.buffer = new StringBuffer();
														}
													}
													else
														//creators
														if(rawName.equalsIgnoreCase("name")){
															String type = atts.getValue("type");
															if(type!=null){
																if(type.equalsIgnoreCase("personal"))
																	this.isCreatorPersonal = true;
																else if(type.equalsIgnoreCase("corporate"))
																	this.isCreatorCorporate = true;
																else if(type.equalsIgnoreCase("conference"))
																	this.isCreatorConference = true;
															}
														}
														else
															//creator name
															if(rawName.equalsIgnoreCase("namePart") && (this.isCreatorPersonal || this.isCreatorCorporate || this.isCreatorConference)){
																this.buffer = new StringBuffer();
																this.isCreatorName = true;
															}
															else
																//publisher place
																if(rawName.equalsIgnoreCase("placeTerm")){
																	String type = atts.getValue("type");
																	if(type!=null && type.equalsIgnoreCase("text")){
																		this.buffer = new StringBuffer();
																		this.isPublisherPlace = true;
																	}
																}
																else
																	if(rawName.equalsIgnoreCase("publisher")){
																		this.buffer = new StringBuffer();
																		this.isPublisher = true;
																	}
																	else
																		//medium
																		if(rawName.equalsIgnoreCase("typeOfResource")){
																			this.buffer = new StringBuffer();
																			this.isFormat = true;
																		}
																		else
																			//extent
																			if(rawName.equalsIgnoreCase("extent")){
																				this.buffer = new StringBuffer();
																				this.isExtent = true;
																			}
																			else
																				//subject
																				if(rawName.equalsIgnoreCase("subject")){
																					tmp = atts.getValue("authority");
																					if(tmp!=null && (tmp.equalsIgnoreCase("lcsh") || tmp.equalsIgnoreCase("nal")))
																						this.isSubject = true;
																					else if(tmp!=null && (tmp.equalsIgnoreCase("agrovocf") || tmp.equalsIgnoreCase("agrovoc") || tmp.equalsIgnoreCase("agrovocs")))
																						this.isAgrovocSubject = true;
																					else {
																						this.isSubject = true;
																						this.tmp = "";
																					}
																				}
																				else
																					//topic
																					if(rawName.equalsIgnoreCase("topic") && (this.isSubject || this.isAgrovocSubject)){
																						this.buffer = new StringBuffer();
																						this.isTopic = true;
																					}
																					else
																						//agrovoc
																						if(rawName.equalsIgnoreCase("geographic") && this.isSubject){
																							this.buffer = new StringBuffer();
																							this.isAgrovoc = true;
																						}
																						else
																							//language
																							if(rawName.equalsIgnoreCase("languageTerm")){
																								this.buffer = new StringBuffer();
																								this.isLanguage = true;
																							}
																							else
																								//isPartOf
																								if(rawName.equalsIgnoreCase("url")){
																									this.buffer = new StringBuffer();
																									this.isPartOf = true;
																								}
																								else
																									//note
																									if(rawName.equalsIgnoreCase("note")){
																										this.buffer = new StringBuffer();
																										this.isNote = true;
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
			if(this.isTitleContent || this.isSubTitleContent || this.isGenre || this.isDate || this.isISBN
					|| this.isISSN || this.isURI || this.isCreatorName || this.isPublisherPlace
					|| this.isFormat || this.isExtent || this.isTopic || this.isAgrovoc || this.isAgrovocSubject || this.isLanguage
					|| this.isPartOf || this.isNote || this.isPublisher || this.isAbstract || this.isJournalNumber){
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
			if(rawName.equalsIgnoreCase("mods")){
				//mandatory fields
				if(this.current.getTitle2language().size()>0 && this.current.getDateIssued()!=null)
					this.records.add(current);
			}
			else
				//discard series
				if(rawName.equalsIgnoreCase("genre") && this.isGenre){
					this.isGenre = false;
					String term = new String(this.buffer);
					if(term!=null && !term.trim().equals("")){
						if(term.equalsIgnoreCase("series") || term.equalsIgnoreCase("periodical")
								|| term.equalsIgnoreCase("numeric data") || term.equalsIgnoreCase("statistics") || term.equalsIgnoreCase("handbook")){
							this.current = null;
						}
						else
							this.current.addType(term);
					}
				}
				else
					// TITLE
					if(rawName.equalsIgnoreCase("title") && this.isTitle && this.isTitleContent){
						this.isTitleContent  = false;
						String term = new String(this.buffer);
						if(term!=null && !term.trim().equals("")){
							//search Solr to see if title exists
							int occurrences = this.indexChecker.checkTitle(term);
							if(occurrences==0) {
								term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
								if(term.endsWith(" ="))
									term = term.substring(0, term.length()-2);
								this.current.addTitle(term, "");
							}
							else {
								this.current = null;
							}
						}
					}
					else
						//alternative
						if(rawName.equalsIgnoreCase("title") && this.isAlternative && this.isTitleContent){
							this.isTitleContent  = false;
							String term = new String(this.buffer);
							if(term!=null && !term.trim().equals("")){
								term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
								this.current.addAlternative(term, "");
							}
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
								//abstract
								if(rawName.equalsIgnoreCase("abstract") && this.isAbstract){
									this.isAbstract  = false;
									String term = new String(this.buffer);
									if(term!=null && !term.trim().equals("")){
										term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
										this.current.addAbstract(term, "");
									}
								}
								else
									//journal title
									if(rawName.equalsIgnoreCase("title") && this.isTitleContent && this.isJournalTitle){
										this.isTitleContent  = false;
										String term = new String(this.buffer);
										if(term!=null && !term.trim().equals("")){
											//check for citation number
											if(term.contains(" ; ")){
												int index = term.lastIndexOf(" ; ");
												if(index!=-1 && (index+3)<term.length()){
													String citNumber = term.substring(index+3);
													term = term.substring(0, index);
													this.current.setCitationNumber(citNumber);
												}
											}
											term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
											this.current.setCitationTitle(term);
										}
									}
									else
										//supplement
										if(rawName.equalsIgnoreCase("subTitle") && this.isSubTitleContent && this.isTitle){
											this.isSubTitleContent  = false;
											String term = new String(this.buffer);
											if(term!=null && !term.trim().equals("")){
												term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
												this.current.addSupplement(term);
											}
										}
										else
											//citation number
											if(rawName.equalsIgnoreCase("text") && this.isJournalNumber){
												this.isJournalNumber  = false;
												String term = new String(this.buffer);
												if(term!=null && !term.trim().equals("")){
													this.current.setCitationNumber(term);
												}
											}
											else
												//main_title
												if(rawName.equalsIgnoreCase("titleInfo")){
													this.isAlternative = false;
													this.isTitle = false;
													this.isJournalTitle = false;
												}
												else
													//related item
													if(rawName.equalsIgnoreCase("relatedItem")){
														this.isRelatedItem = false;
													}
													else
														//date
														if(rawName.equalsIgnoreCase("dateIssued") && this.isDate){
															this.isDate  = false;
															String term = new String(this.buffer);
															if(term!=null && !term.trim().equals("")){
																this.current.setDateIssued(term);
															}
														}
														else
															//identifiers
															if(rawName.equalsIgnoreCase("identifier")){
																//isbn
																if(this.isISBN){
																	this.isISBN = false;
																	String term = new String(this.buffer);
																	if(term!=null && !term.trim().equals("")){
																		//check errors, if this is an ISSN
																		if(IssnCleaner.isISSN(term)){
																			this.current.addIssn(term);
																		} 
																		else
																			this.current.addIdentifier(term, "ags:ISBN");
																	}
																}
																else if(this.isISSN){
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
																else if(this.isURI){
																	this.isURI = false;
																	String term = new String(this.buffer);
																	if(term!=null && !term.trim().equals("")){
																		this.current.addIdentifier(term, "dcterms:URI");
																	}
																}
															}
															else
																//creators
																if(rawName.equalsIgnoreCase("name")){
																	this.isCreatorPersonal = false;
																	this.isCreatorCorporate = false;
																	this.isCreatorConference = false;
																}
																else 
																	//creator name
																	if(rawName.equalsIgnoreCase("namePart") && this.isCreatorName){
																		this.isCreatorName  = false;
																		String term = new String(this.buffer);
																		if(term!=null && !term.trim().equals("")){
																			if(this.isCreatorPersonal)
																				this.current.addCreatorPersonal(term);
																			else if(this.isCreatorCorporate)
																				this.current.addCreatorCorporate(term);
																			else if(this.isCreatorConference)
																				this.current.addCreatorConference(term);
																		}
																	}
																	else
																		//publisher place
																		if(rawName.equalsIgnoreCase("placeTerm") && this.isPublisherPlace){
																			this.isPublisherPlace = false;
																			String term = new String(this.buffer);
																			if(term!=null && !term.trim().equals("")){
																				this.current.addPublisherPlace(term);
																			}
																		}
																		else
																			//publisher
																			if(rawName.equalsIgnoreCase("publisher") && this.isPublisher){
																				this.isPublisher = false;
																				String term = new String(this.buffer);
																				if(term!=null && !term.trim().equals("") && !term.equalsIgnoreCase("[s.n.]")){
																					this.current.addPublisherName(term);
																				}
																			}
																			else
																				//typeOfResource
																				if(rawName.equalsIgnoreCase("typeOfResource") && this.isFormat){
																					this.isFormat = false;
																					String term = new String(this.buffer);
																					if(term!=null && !term.trim().equals("")){
																						this.current.setFormat(term);
																					}
																				}
																				else
																					//extent
																					if(rawName.equalsIgnoreCase("extent") && this.isExtent){
																						this.isExtent = false;
																						String term = new String(this.buffer);
																						if(term!=null && !term.trim().equals("")){
																							this.current.setExtent(term);
																						}
																					}
																					else
																						//subject
																						if(rawName.equalsIgnoreCase("subject")){
																							this.isSubject = false;
																							this.tmp = "";
																						}
																						else
																							//topic
																							if(rawName.equalsIgnoreCase("topic") && this.isTopic && !this.isAgrovocSubject){
																								this.isTopic = false;
																								String term = new String(this.buffer);
																								if(term!=null && !term.trim().equals("")){
																									if(!this.tmp.equals("")){
																										AGRISAttributes attr = new AGRISAttributes();
																										attr.setLang("eng");
																										if(this.tmp.equalsIgnoreCase("lcsh"))
																											attr.setScheme("dcterms:LCSH");
																										else if(this.tmp.equalsIgnoreCase("nal"))
																											attr.setScheme("ags:NALT");
																										this.current.addAgrovoc(term, attr);
																									} else
																										this.current.addFreeSubject(term);
																								}
																							}
																							else
																								//agrovoc topic
																								if(rawName.equalsIgnoreCase("topic") && this.isAgrovocSubject && this.isTopic){
																									this.isAgrovocSubject = false;
																									this.isTopic = false;
																									String term = new String(this.buffer);
																									if(term!=null && !term.trim().equals("")){
																										AGRISAttributes attr = new AGRISAttributes();
																										attr.setScheme("ags:AGROVOC");
																										//check language
																										if(this.tmp!=null && this.tmp.length()>0){
																											if(this.tmp.equalsIgnoreCase("agrovoc"))
																												attr.setLang("eng");
																											else if(this.tmp.equalsIgnoreCase("agrovocf"))
																												attr.setLang("fre");
																											else if(this.tmp.equalsIgnoreCase("agrovocs"))
																												attr.setLang("esp");
																										}
																										this.current.addAgrovoc(term, attr);
																									}
																								}
																								else
																									//agrovoc
																									if(rawName.equalsIgnoreCase("geographic") && this.isAgrovoc){
																										this.isAgrovoc = false;
																										String term = new String(this.buffer);
																										if(term!=null && !term.trim().equals("")){
																											this.current.addSpatial(term);
																										}
																									}
																									else
																										//language
																										if(rawName.equalsIgnoreCase("languageTerm")){
																											this.isLanguage = false;
																											String term = new String(this.buffer);
																											if(term!=null && !term.trim().equals("") && !term.trim().equals("lis") && !term.trim().equals("h")){
																												this.current.addLanguage(term, "dcterms:ISO639-2");
																											}
																										}
																										else
																											//isPartOf
																											if(rawName.equalsIgnoreCase("url")){
																												this.isPartOf = false;
																												String term = new String(this.buffer);
																												if(term!=null && !term.trim().equals("")){
																													this.current.addIsPartOfRelation(term, "dcterms:URI");
																												}
																											}
																											else
																												//note
																												if(rawName.equalsIgnoreCase("note")){
																													this.isNote = false;
																													String term = new String(this.buffer);
																													if(term!=null && !term.trim().equals("")){
																														String oldNote = this.current.getDescrNotes();
																														if(oldNote!=null)
																															term = oldNote+" "+term;
																														this.current.setDescrNotes(term);
																													}
																												}
																												
		}
	}

}
