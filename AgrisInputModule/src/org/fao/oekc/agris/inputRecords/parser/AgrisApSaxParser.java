package org.fao.oekc.agris.inputRecords.parser;

import java.util.List;

import org.fao.oekc.agris.inputRecords.dom.AGRISAttributes;
import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.CheckIndex;
import org.fao.oekc.agris.inputRecords.util.EscapeXML;
import org.fao.oekc.agris.inputRecords.util.IssnCleaner;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses Agris AP XML files.
 * If the parameter globalDuplicatesRemoval is true, the parser checks for duplicates (based on titles) and generate new ARNS. 
 * If false, it does not check for duplicates and does not generate ARNs (which should be already in the data)
 * @author celli
 *
 */
public class AgrisApSaxParser extends DefaultHandler {

	//parser
	private List<AgrisApDoc> records;
	private AgrisApDoc current;

	//index checker
	private CheckIndex indexChecker; 

	//data
	private boolean isTitle;
	private boolean isAlternative;
	private boolean isAbstract;
	private boolean identifier2schema;
	private boolean creatorPersonal;
	private boolean creatorCorporate;
	private boolean creatorConference;
	private boolean publisherName;
	private boolean publisherPlace;
	private boolean freeSubject;
	private boolean isSubjectClass;
	private boolean isAgrovoc;
	private boolean isDate;
	private boolean isLanguage;
	private boolean extent;
	private boolean isType;
	private boolean isSource;
	private boolean isJournalTitle;
	private boolean citationNumber;
	private boolean citationChron;
	private boolean isISSN;
	private boolean isEISSN;

	private StringBuffer buffer; //to read the entire content
	private String langAbstract="";
	private String langTitle="";
	private String schemeID="";

	//if true, remove records whose ISSN is in the index, if false remove records if the ISSN is in the index associated to the same country code
	private boolean globalDuplicatesRemoval;	

	/**
	 * Create the parser for the document and start parsing.
	 * All booleans set to false until the corresponding element starts
	 * @param records list of result records
	 * @param globalDuplicatesRemoval if true, check duplicates based on titles and generate new ARNS. If false, does not check for duplicates and does not generate ARNs
	 */
	public AgrisApSaxParser(List<AgrisApDoc> records, boolean globalDuplicatesRemoval) {
		try {
			this.indexChecker = new CheckIndex();
			this.records = records;
			this.isTitle = false;
			this.isAlternative = false;
			this.isAbstract = false;
			this.identifier2schema = false;
			this.creatorPersonal = false;
			this.creatorCorporate = false;
			this.creatorConference = false;
			this.publisherName = false;
			this.publisherPlace = false;
			this.freeSubject = false;
			this.isAgrovoc = false;
			this.isSubjectClass = false;
			this.isDate = false;
			this.isLanguage = false;
			this.extent = false;
			this.isType = false;
			this.isSource = false;
			this.isJournalTitle = false;
			this.citationNumber = false;
			this.citationChron = false;
			this.isISSN = false;
			this.isEISSN = false;
			this.globalDuplicatesRemoval = globalDuplicatesRemoval;
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
		if(rawName.equalsIgnoreCase("ags:resource")){
			this.current = new AgrisApDoc();

			//check ARN in the index
			if(!this.globalDuplicatesRemoval){
				String arn = atts.getValue("ags:ARN");
				if(arn!=null && arn.length()>0){
					int occurrences = this.indexChecker.checkArn(arn);
					if(occurrences!=0){
						this.current=null;
						System.out.println("!! ARN "+arn+" already exists in the index!");
					}
					else
						this.current.setARN(arn);
				}
			}
		}
		else
			if(this.current!=null) {

				//title
				if(rawName.equalsIgnoreCase("dc:title")){
					String lang = atts.getValue("xml:lang");
					//check language: WUR
					if(lang!=null && lang.equalsIgnoreCase("und"))
						lang = null;
					//check existence
					if(lang!=null && lang.length()>0)
						this.langTitle = lang.toLowerCase();
					else
						this.langTitle = null;
					this.buffer = new StringBuffer();
					this.isTitle = true;
				}
				else
					//alternative
					if(rawName.equalsIgnoreCase("dcterms:alternative")){
						this.buffer = new StringBuffer();
						this.isTitle = false;
						this.isAlternative = true;
					}
					else
						//authors
						if(rawName.equalsIgnoreCase("ags:creatorPersonal")){
							this.buffer = new StringBuffer();
							this.creatorPersonal = true;
						}
						else
							//corporate authors
							if(rawName.equalsIgnoreCase("ags:creatorCorporate")){
								this.buffer = new StringBuffer();
								this.creatorCorporate = true;
							}
							else
								//conference
								if(rawName.equalsIgnoreCase("ags:creatorConference")){
									this.buffer = new StringBuffer();
									this.creatorConference = true;
								}
								else
									//date
									if(rawName.equalsIgnoreCase("dcterms:dateIssued")){
										this.isDate = true;
										this.buffer = new StringBuffer();
									}
									else
										//language
										if(rawName.equalsIgnoreCase("dc:language")){
											this.isLanguage = true;
											this.schemeID = atts.getValue("scheme");
											this.buffer = new StringBuffer();
										}
										else
											//other keywords
											if(rawName.equalsIgnoreCase("dc:subject")){
												this.freeSubject = true;
												this.buffer = new StringBuffer();
											}
											else
												//ASC
												if(rawName.equalsIgnoreCase("ags:subjectClassification")){
													this.isSubjectClass = true;
													this.freeSubject = false;
													this.schemeID = atts.getValue("scheme");
													this.buffer = new StringBuffer();
												}
												else
													//AGROVOC
													if(rawName.equalsIgnoreCase("ags:subjectThesaurus")){
														this.isAgrovoc = true;
														this.freeSubject = false;
														this.schemeID = atts.getValue("scheme");
														this.langTitle = atts.getValue("xml:lang");
														this.buffer = new StringBuffer();
													}
													else
														//journalTitle
														if(rawName.equalsIgnoreCase("ags:citationTitle")){
															this.buffer = new StringBuffer();
															this.isJournalTitle = true;
														}
														else
															//publisherName
															if(rawName.equalsIgnoreCase("ags:publisherName")){
																this.buffer = new StringBuffer();
																this.publisherName = true;
															}
															else
																//publisherPlace
																if(rawName.equalsIgnoreCase("ags:publisherPlace")){
																	this.buffer = new StringBuffer();
																	this.publisherPlace = true;
																}
																else
																	//ISSN
																	if(rawName.equalsIgnoreCase("ags:citationIdentifier")){
																		String scheme = atts.getValue("scheme");
																		if(scheme!=null && scheme.equalsIgnoreCase("ags:ISSN")){
																			this.buffer = new StringBuffer();
																			this.isISSN = true;
																		}
																	}
																	else
																		//eISSN
																		if(rawName.equalsIgnoreCase("ags:citationIdentifier")){
																			String scheme = atts.getValue("scheme");
																			if(scheme!=null && scheme.equalsIgnoreCase("bibo:eissn")){
																				this.buffer = new StringBuffer();
																				this.isEISSN = true;
																			}
																		}
																		else
																			//identifiers
																			if(rawName.equalsIgnoreCase("dc:identifier")){
																				String scheme = atts.getValue("scheme");
																				if(scheme!=null && scheme.length()>0){
																					this.schemeID = scheme;
																					this.buffer = new StringBuffer();
																					this.identifier2schema = true;
																				}
																			}
																			else
																				//citation number
																				if(rawName.equalsIgnoreCase("ags:citationNumber")){
																					this.citationNumber = true;
																					this.buffer = new StringBuffer();
																				}
																				else
																					//citation chronology
																					if(rawName.equalsIgnoreCase("ags:citationChronology")){
																						this.citationChron = true;
																						this.buffer = new StringBuffer();
																					}
																					else
																						//extent
																						if(rawName.equalsIgnoreCase("dcterms:extent")){
																							this.extent = true;
																							this.buffer = new StringBuffer();
																						}
																						else
																							//type
																							if(rawName.equalsIgnoreCase("dc:type")){
																								this.isType = true;
																								this.buffer = new StringBuffer();
																							}
																							else
																								//source
																								if(rawName.equalsIgnoreCase("dc:source")){
																									this.buffer = new StringBuffer();
																									this.isSource = true;
																								}
																								else
																									//abstract
																									if(rawName.equalsIgnoreCase("dcterms:abstract")){
																										String lang = atts.getValue("xml:lang");
																										this.buffer = new StringBuffer();
																										//check language: WUR
																										if(lang!=null && lang.equalsIgnoreCase("und"))
																											lang = null;
																										//check existence
																										if(lang!=null && lang.length()>0)
																											this.langAbstract = lang.toLowerCase();
																										else
																											this.langAbstract = null;
																										this.isAbstract = true;
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
			if(this.creatorPersonal || this.creatorCorporate || this.isJournalTitle || this.isISSN || this.isEISSN
					|| this.isTitle || this.publisherName || this.identifier2schema || this.isAbstract || this.isAlternative
					|| this.creatorConference || this.publisherPlace || this.isSource || this.isDate
					|| this.isLanguage || this.freeSubject || this.isSubjectClass || this.isAgrovoc || this.citationNumber
					|| this.citationChron || this.extent || this.isType){
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
			if(rawName.equalsIgnoreCase("ags:resource")){
				//mandatory fields
				if(this.current.getTitle2language().size()>0 && this.current.getDateIssued()!=null)
					this.records.add(current);
			}
			else
				// ABSTRACT
				if(rawName.equalsIgnoreCase("dcterms:abstract")){
					this.isAbstract = false;
					String abs = new String(this.buffer);
					if(abs!=null)
						abs = EscapeXML.getInstance().removeHTMLTagsAndUnescape(abs);
					this.current.addAbstract(abs, this.langAbstract);
				}
				else
					// TITLE
					if(rawName.equalsIgnoreCase("dc:title") && this.isTitle){
						this.isTitle = false;
						String term = new String(this.buffer);
						if(term!=null && !term.trim().equals("")){
							//search Solr to see if title exists
							if(globalDuplicatesRemoval){
								int occurrences = this.indexChecker.checkTitle(term);			
								if(occurrences==0) {
									term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
									this.current.addTitle(term, this.langTitle);
								}
								else {
									System.out.println("!! title "+term+" already exists in the index!");
									this.current = null;
								}
							} else {
								this.current.addTitle(term, null);
							}
						}
					}
					else
						// ALTERNATIVE
						if(rawName.equalsIgnoreCase("dcterms:alternative") && this.isAlternative){
							this.isAlternative = false;
							String term = new String(this.buffer);
							if(term!=null && !term.trim().equals("")){
								//search Solr to see if title exists
								if(globalDuplicatesRemoval){
									int occurrences = this.indexChecker.checkTitle(term);			
									if(occurrences==0) {
										term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
										this.current.addAlternative(term, null);
									}
									else {
										System.out.println("!! title "+term+" already exists in the index!");
										this.current = null;
									}
								} else {
									this.current.addAlternative(term, null);
								}
							}
						}
						else
							// JOURNAL TITLE
							if(rawName.equalsIgnoreCase("ags:citationTitle")){
								this.isJournalTitle = false;
								String term = new String(this.buffer);
								if(term!=null && !term.trim().equals("")){
									this.current.setCitationTitle(term);
								}
							}
							else
								//authors
								if(rawName.equalsIgnoreCase("ags:creatorPersonal")){
									this.creatorPersonal = false;
									String term = new String(this.buffer);
									if(term!=null && !term.trim().equals("")){
										this.current.addCreatorPersonal(term);
									}
								}
								else
									//corporate authors
									if(rawName.equalsIgnoreCase("ags:creatorCorporate")){
										this.creatorCorporate = false;
										String term = new String(this.buffer);
										if(term!=null && !term.trim().equals("")){
											this.current.addCreatorCorporate(term);
										}
									}
									else
										//conference
										if(rawName.equalsIgnoreCase("ags:creatorConference")){
											this.creatorConference = false;
											String term = new String(this.buffer);
											if(term!=null && !term.trim().equals("")){
												this.current.addCreatorConference(term);
											}
										}
										else
											//identifiers
											if(rawName.equalsIgnoreCase("dc:identifier")){
												this.identifier2schema = false;
												String term = new String(this.buffer);
												if(term!=null && !term.trim().equals("") &&
														//cable WUR wrong links
														!term.contains("WebQuery")){
													this.current.addIdentifier(term, schemeID);
												}
											}
											else
												//publisherName
												if(rawName.equalsIgnoreCase("ags:publisherName")){
													this.publisherName = false;
													String term = new String(this.buffer);
													if(term!=null && !term.trim().equals("")){
														this.current.addPublisherName(term);
													}
												}
												else
													//publisherPlace
													if(rawName.equalsIgnoreCase("ags:publisherPlace")){
														this.publisherPlace = false;
														String term = new String(this.buffer);
														if(term!=null && !term.trim().equals("")){
															this.current.addPublisherPlace(term);
														}
													}
													else
														//ISSN
														if(rawName.equalsIgnoreCase("ags:citationIdentifier") && this.isISSN){
															this.isISSN = false;
															String term = new String(this.buffer);			
															if(term!=null && !term.trim().equals("")){
																//naive issn cleaning
																if(!IssnCleaner.isISSN(term))
																	term = IssnCleaner.cleanISSN(term);
																this.current.addIssn(term);
															}
														}
														else
															//EISSN
															if(rawName.equalsIgnoreCase("ags:citationIdentifier") && this.isEISSN){
																this.isEISSN = false;
																String term = new String(this.buffer);
																if(term!=null && !term.trim().equals("")){
																	//naive issn cleaning
																	if(!IssnCleaner.isISSN(term))
																		term = IssnCleaner.cleanISSN(term);
																	if(IssnCleaner.isISSN(term))
																		this.current.addEissn(term);
																}
															}
															else
																//source
																if(rawName.equalsIgnoreCase("dc:source") && this.isSource){
																	this.isSource = false;
																	String term = new String(this.buffer);

																	//validate: WUR
																	if(term!=null && 
																			(term.contains(this.current.getDateIssued()) ||
																					term.contains("ISSN") || term.contains("ISBN"))){
																		if(term.contains("ISBN"))
																			this.current.addIdentifier(term.replace("ISBN: ", ""), "ags:ISBN");
																		term = null;
																	}

																	//check existence
																	if(term!=null && !term.trim().equals("")){
																		this.current.setSource(term);
																	}
																}
																else
																	//date
																	if(rawName.equalsIgnoreCase("dcterms:dateIssued") && this.isDate){
																		String term = new String(this.buffer);
																		this.isDate = false;
																		if(term!=null && !term.trim().equals("")){
																			//naive cleaning
																			if(term.startsWith("(") && term.endsWith(")"))
																				term = term.substring(1, term.length()-1);
																			this.current.setDateIssued(term);
																		}
																	}
																	else
																		//language
																		if(rawName.equalsIgnoreCase("dc:language") && this.isLanguage){
																			String term = new String(this.buffer);
																			this.isLanguage = false;
																			if(term!=null && !term.trim().equals("") && !term.equalsIgnoreCase("und")){
																				this.current.addLanguage(term, schemeID);
																			}
																		}
																		else
																			//freeSubject: add if keywords lenght is grater than 2
																			if(rawName.equalsIgnoreCase("dc:subject") && this.freeSubject){
																				String term = new String(this.buffer);
																				this.freeSubject = false;
																				if(term!=null && term.length()>2){
																					this.current.addFreeSubject(term);
																				}
																			}
																			else
																				//ASC: add if keywords lenght is grater than 1 (e.g. F01)
																				if(rawName.equalsIgnoreCase("ags:subjectClassification") && this.isSubjectClass){
																					String term = new String(this.buffer);
																					this.isSubjectClass = false;
																					if(term!=null && term.length()>1){
																						this.current.addSubjClass(term, schemeID);
																					}
																				}
																				else
																					//agrovoc
																					if(rawName.equalsIgnoreCase("ags:subjectThesaurus") && this.isAgrovoc){
																						String term = new String(this.buffer);
																						this.isAgrovoc = false;
																						if(term!=null && term.length()>1){
																							AGRISAttributes attr = new AGRISAttributes();
																							attr.setLang(langTitle);
																							attr.setScheme(this.schemeID);
																							this.current.addAgrovoc(term, attr);
																						}
																					}
																					else
																						//citation number
																						if(rawName.equalsIgnoreCase("ags:citationNumber") && this.citationNumber){
																							String term = new String(this.buffer);
																							this.citationNumber = false;
																							if(term!=null && !term.trim().equals("")){
																								this.current.setCitationNumber(term);
																							}
																						}
																						else
																							//citation chronology
																							if(rawName.equalsIgnoreCase("ags:citationChronology") && this.citationChron){
																								String term = new String(this.buffer);
																								this.citationChron = false;
																								if(term!=null && !term.trim().equals("")){
																									this.current.setCitationChronology(term);
																								}
																							}
																							else
																								//extent
																								if(rawName.equalsIgnoreCase("dcterms:extent") && this.extent){
																									String term = new String(this.buffer);
																									this.extent = false;
																									if(term!=null && !term.trim().equals("")){
																										this.current.setExtent(term);
																									}
																								}
																								else
																									//type
																									if(rawName.equalsIgnoreCase("dc:type") && this.isType){
																										String term = new String(this.buffer);
																										this.isType = false;
																										if(term!=null && !term.trim().equals("")){
																											this.current.addType(term);
																										}
																									}

		}
	}

	/**
	 * Finalize information to be displayed
	 */
	public void endDocument() {
	}


}
