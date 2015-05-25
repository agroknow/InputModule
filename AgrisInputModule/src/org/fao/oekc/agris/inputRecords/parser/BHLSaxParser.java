package org.fao.oekc.agris.inputRecords.parser;

import java.util.List;

import jfcutils.util.LanguageTranslation;
import jfcutils.util.StringUtils;

import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.CheckIndex;
import org.fao.oekc.agris.inputRecords.util.EscapeXML;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser for MODS BHL metadata
 * @author celli
 *
 */
public class BHLSaxParser extends DefaultHandler {

	//parser
	private List<AgrisApDoc> records;
	private AgrisApDoc current;

	//index checker
	private CheckIndex indexChecker;


	//data
	private boolean isTitle;
	private boolean isAlternative;
	private boolean isTitleContent;
	private boolean isGenre;
	private boolean isDate;
	private boolean isCreatorName;
	private boolean isCreatorPersonal;
	private boolean isCreatorCorporate;
	private boolean isURI;
	private boolean isPublisher;
	private boolean isPublisherPlace;
	private boolean isTopic;
	private boolean isLanguage;
	private boolean isFormat;
	private boolean isClassification;

	//to read the entire content
	private StringBuffer buffer; 
	private LanguageTranslation langTranslator;

	/**
	 * Create the parser for the document and start parsing.
	 * All booleans set to false until the corresponding element starts
	 * @param records list of result records
	 */
	public BHLSaxParser(List<AgrisApDoc> records) {
		try {
			this.indexChecker = new CheckIndex();
			//this.excludedTitles = new LinkedList<String>();
			this.records = records;

			this.isTitle = false;
			this.isAlternative = false;
			this.isTitleContent = false;
			this.isCreatorPersonal = false;
			this.isCreatorCorporate = false;
			this.isGenre = false;
			this.isDate = false;
			this.isURI = false;
			this.isCreatorName = false;
			this.isPublisherPlace = false;
			this.isPublisher = false;
			this.isTopic = false;
			this.isFormat = false;
			this.isLanguage = false;
			this.isClassification = false;

			this.langTranslator = new LanguageTranslation();
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
					if(authority!=null && authority.equalsIgnoreCase("marcgt")){
						this.buffer = new StringBuffer();
						this.isGenre = true;
					}	
				}
				else
					//title
					if(rawName.equalsIgnoreCase("titleInfo")){
						String type = atts.getValue("type");
						if(type!=null && type.equalsIgnoreCase("abbreviated"))
							this.isAlternative = true;
						else
							this.isTitle = true;
					}
					else
						//main title
						if(rawName.equalsIgnoreCase("title") && (this.isTitle || this.isAlternative)){
							this.buffer = new StringBuffer();
							this.isTitleContent = true;
						}
						else
							//date
							if(rawName.equalsIgnoreCase("dateIssued")){
								String encoding = atts.getValue("encoding");
								String point = atts.getValue("point");
								if(encoding!=null && point!=null){
									//normal dateIssued
									this.buffer = new StringBuffer();
									this.isDate = true;
								}
							}
							else
								//identifiers
								if(rawName.equalsIgnoreCase("identifier")){
									String type = atts.getValue("type");
									if(type!=null && type.equalsIgnoreCase("uri")) {
										this.isURI = true;
										this.buffer = new StringBuffer();
									}
								}
								else
									//identifiers
									if(rawName.equalsIgnoreCase("url")){
										this.isURI = true;
										this.buffer = new StringBuffer();
									}
									else
										//creators
										if(rawName.equalsIgnoreCase("name")){
											String type = atts.getValue("type");
											if(type!=null && type.equalsIgnoreCase("personal")) {
												if(type.equalsIgnoreCase("corporate"))
													this.isCreatorCorporate = true;
												else 
													this.isCreatorPersonal = true; 
											}
										}
										else
											//creator name
											if(rawName.equalsIgnoreCase("namePart") && (this.isCreatorPersonal || this.isCreatorCorporate)){
												String type = atts.getValue("type");
												if(type==null || !type.equalsIgnoreCase("date")) {
													this.buffer = new StringBuffer();
													this.isCreatorName = true;
												}
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
															//topic
															if(rawName.equalsIgnoreCase("topic") || rawName.equalsIgnoreCase("geographic")){
																this.buffer = new StringBuffer();
																this.isTopic = true;
															}
															else
																//language
																if(rawName.equalsIgnoreCase("languageTerm")){
																	this.buffer = new StringBuffer();
																	this.isLanguage = true;
																}
																else
																	//publisher place
																	if(rawName.equalsIgnoreCase("classification")){
																		String auth = atts.getValue("authority");
																		if(auth!=null && auth.equalsIgnoreCase("lcc")){
																			this.buffer = new StringBuffer();
																			this.isClassification = true;
																		}
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
			if(this.isTitleContent || this.isGenre || this.isDate 
					|| this.isURI || this.isCreatorName || this.isPublisherPlace
					|| this.isFormat || this.isTopic || this.isLanguage
					|| this.isPublisher){
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
				//mandatory fields: title
				if(this.current.getTitle2language().size()>0)
					this.records.add(current);
				else
					System.err.println("--- missing title or date...");
			}
			else
				//discard series
				if(rawName.equalsIgnoreCase("genre") && this.isGenre){
					this.isGenre = false;
					String term = new String(this.buffer);
					if(term!=null && !term.trim().equals("")){
						if(term.equalsIgnoreCase("series") || term.equalsIgnoreCase("periodical")){
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
								this.current.addTitle(term, "");
								//search for date at the end of the title
								int lastComma = term.lastIndexOf(",");
								if(lastComma!=-1 && (lastComma+1)<=term.length()){
									String dateString = term.substring(lastComma+1);
									boolean containYear = dateString.matches(".*\\d{4}.*");
									if (containYear) {
										this.current.setDateIssued((new StringUtils()).trimLeft(dateString));
									}
								}
							}
							else {
								//this.excludedTitles.add(term);
								System.err.println("@@@ title already existing: "+term);
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
							//main_title
							if(rawName.equalsIgnoreCase("titleInfo")){
								this.isAlternative = false;
								this.isTitle = false;
							}
							else
								//date
								if(rawName.equalsIgnoreCase("dateIssued") && this.isDate){
									this.isDate  = false;
									String term = new String(this.buffer);
									if(term!=null && !term.trim().equals("")){
										//naive cleaning
										if(term.endsWith(".") && term.length()>1)
											term = term.substring(0, term.length()-1);
										this.current.setDateIssued(term);
									}
								}
								else
									//identifiers
									if(rawName.equalsIgnoreCase("identifier")){
										if(this.isURI){
											this.isURI = false;
											String term = new String(this.buffer);
											if(term!=null && !term.trim().equals("")){
												this.current.addIdentifier(term, "dcterms:URI");
											}
										}
									}
									else
										//identifiers
										if(rawName.equalsIgnoreCase("url") && this.isURI){
											this.isURI = false;
											String term = new String(this.buffer);
											if(term!=null && !term.trim().equals("")){
												this.current.addIdentifier(term, "dcterms:URI");
											}
										}
										else
											//creators
											if(rawName.equalsIgnoreCase("name")){
												this.isCreatorPersonal = false;
											}
											else 
												//creator name
												if(rawName.equalsIgnoreCase("namePart") && this.isCreatorName){
													this.isCreatorName  = false;
													String term = new String(this.buffer);
													if(term!=null && !term.trim().equals("")){
														if(this.isCreatorPersonal){
															//naive cleaning
															if(term.endsWith(",") && term.length()>1)
																term = term.substring(0, term.length()-1);
															this.current.addCreatorPersonal(term);
														}
													}
												}
												else
													//publisher place
													if(rawName.equalsIgnoreCase("placeTerm") && this.isPublisherPlace){
														this.isPublisherPlace = false;
														String term = new String(this.buffer);
														if(term!=null && !term.trim().equals("")){
															//naive cleaning
															if(term.endsWith(" :") && term.length()>2)
																term = term.substring(0, term.length()-2);
															//naive cleaning
															if(term.endsWith(",") && term.length()>1)
																term = term.substring(0, term.length()-1);
															this.current.addPublisherPlace(term);
														}
													}
													else
														//publisher
														if(rawName.equalsIgnoreCase("publisher") && this.isPublisher){
															this.isPublisher = false;
															String term = new String(this.buffer);
															if(term!=null && !term.trim().equals("") && !term.equalsIgnoreCase("[s.n.]")){
																//naive cleaning
																if(term.endsWith(",") && term.length()>1)
																	term = term.substring(0, term.length()-1);
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
																//topic
																if((rawName.equalsIgnoreCase("topic") || rawName.equalsIgnoreCase("geographic")) && this.isTopic){
																	this.isTopic = false;
																	String term = new String(this.buffer);
																	if(term!=null && !term.trim().equals("")){
																		this.current.addFreeSubject(term);
																	}
																}
																else
																	//language
																	if(rawName.equalsIgnoreCase("languageTerm")){
																		this.isLanguage = false;
																		String term = new String(this.buffer);
																		if(term!=null && !term.trim().equals("") && !term.trim().equals("lis") && !term.trim().equals("h")){
																			this.current.addLanguage(this.langTranslator.translate2threedigitsEn(term), "dcterms:ISO639-2");
																		}
																	}
																	else
																		//subject classification
																		if(rawName.equalsIgnoreCase("classification")  && this.isClassification){
																			this.isClassification = false;
																			String term = new String(this.buffer);
																			if(term!=null && !term.trim().equals("")){
																				this.current.addSubjClass(term, "dcterms:LCC");
																			}
																		}
		}
	}

}
