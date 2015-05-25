package org.fao.oekc.agris.inputRecords.parser;

import java.util.List;
import java.util.Set;

import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.CheckIndex;
import org.fao.oekc.agris.inputRecords.util.EscapeXML;
import org.fao.oekc.agris.inputRecords.util.IssnCleaner;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses DOAJ XML files, producing objects if ISSN is not in the Solr index and the ARN does not exist and there is no exact match title
 * @author celli
 *
 */
public class DOAJSaxParser extends DefaultHandler {

	//parser
	private List<AgrisApDoc> records;
	private AgrisApDoc current;

	//index checker
	private CheckIndex indexChecker;

	//data
	private boolean isTitle;
	private boolean isAbstract;
	private boolean creatorPersonal;
	private boolean publisherName;
	private boolean freeSubject;
	private boolean isDate;
	private boolean isJournalTitle;
	private boolean isISSN;
	private boolean isEISSN;
	private boolean isLanguage;
	private boolean isvolume;
	private boolean isIssue;
	private boolean isStPage;
	private boolean isEnPage;
	private boolean identifier2schema;

	private StringBuffer buffer; //to read the entire content
	private Set<String> issnToBeIncluded;

	//temp variables
	private String volume = "";
	private String issue = "";
	private String startPage = "";
	private String endPage = "";

	//if true, excludes DOAJ from duplicates removal
	private boolean globalDuplicatesRemoval;	

	/**
	 * Create the parser for the document and start parsing.
	 * All booleans set to false until the corresponding element starts
	 * @param records list of result records
	 * @param globalDuplicatesRemoval if true, check duplicates based on titles and generate new ARNS. If false, does not check for duplicates and does not generate ARNs
	 * @param issnToBeIncluded lines of the input text file (also null, it is optional), used for example to filter ISSNs
	 */
	public DOAJSaxParser(List<AgrisApDoc> records, boolean globalDuplicatesRemoval, Set<String> issnToBeIncluded) {
		try {
			this.indexChecker = new CheckIndex();
			this.records = records;
			this.issnToBeIncluded = issnToBeIncluded;
			this.isTitle = false;
			this.isAbstract = false;
			this.identifier2schema = false;
			this.creatorPersonal = false;
			this.publisherName = false;
			this.freeSubject = false;
			this.isDate = false;
			this.isLanguage = false;
			this.isvolume = false;
			this.isIssue = false;
			this.isStPage = false;
			this.isEnPage = false;
			this.isJournalTitle = false;
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
		if(rawName.equalsIgnoreCase("record")){
			this.current = new AgrisApDoc();
		}
		else
			if(this.current!=null) {

				//title
				if(rawName.equalsIgnoreCase("title")){
					this.buffer = new StringBuffer();
					this.isTitle = true;
				}
				else
					//authors
					if(rawName.equalsIgnoreCase("name")){
						this.buffer = new StringBuffer();
						this.creatorPersonal = true;
					}
					else
						//date
						if(rawName.equalsIgnoreCase("publicationDate")){
							this.isDate = true;
						}
						else
							//other keywords
							if(rawName.equalsIgnoreCase("keyword")){
								this.freeSubject = true;
							}
							else
								//identifiers FTEXT
								if(rawName.equalsIgnoreCase("fullTextUrl")){
									this.buffer = new StringBuffer();
									this.identifier2schema = true;
								}
								else
									//identifiers DOI
									if(rawName.equalsIgnoreCase("doi")){
										this.buffer = new StringBuffer();
										this.identifier2schema = true;
									}
									else
										//abstract
										if(rawName.equalsIgnoreCase("abstract")){
											this.buffer = new StringBuffer();
											this.isAbstract = true;
										}
										else
											//publisher name
											if(rawName.equalsIgnoreCase("publisher")){
												this.buffer = new StringBuffer();
												this.publisherName = true;
											}
											else
												//journal title
												if(rawName.equalsIgnoreCase("journalTitle")){
													this.buffer = new StringBuffer();
													this.isJournalTitle = true;
												}
												else
													//ISSN
													if(rawName.equalsIgnoreCase("issn")){
														this.buffer = new StringBuffer();
														this.isISSN = true;
													}
													else
														//EISSN
														if(rawName.equalsIgnoreCase("eissn")){
															this.buffer = new StringBuffer();
															this.isEISSN = true;
														}
														else
															//volume
															if(rawName.equalsIgnoreCase("volume")){
																this.isvolume = true;
															}
															else
																//issue
																if(rawName.equalsIgnoreCase("issue")){
																	this.isIssue = true;
																}
																else
																	//start page
																	if(rawName.equalsIgnoreCase("startPage")){
																		this.isStPage = true;
																	}
																	else
																		//end page
																		if(rawName.equalsIgnoreCase("endPage")){
																			this.isEnPage = true;
																		}
																		else
																			//language
																			if(rawName.equalsIgnoreCase("language")){
																				this.buffer = new StringBuffer();
																				this.isLanguage = true;
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
			if(this.creatorPersonal || this.isTitle || this.isAbstract || this.identifier2schema || this.publisherName
					|| this.isJournalTitle || this.isISSN || this.isEISSN || this.isLanguage){
				this.buffer.append(ch, start, length);
			}
			else
				//date
				if(this.isDate){
					String term = new String(ch, start, length);
					this.isDate = false;
					if(term!=null && !term.trim().equals("")){
						//naive cleaning
						if(term.startsWith("(") && term.endsWith(")"))
							term = term.substring(1, term.length()-1);
						//chenge format from 2006-01-17T11:02:00 to 2006-01-17
						if(term.contains("T"))
							term = term.substring(0, term.indexOf("T"));
						this.current.setDateIssued(term);
					}
				}
				else
					//freeSubject: add if keywords lenght is grater than 2
					if(this.freeSubject){
						String term = new String(ch, start, length);
						this.freeSubject = false;
						if(term!=null && term.length()>2){
							this.current.addFreeSubject(term);
						}
					}
					else
						//volume
						if(this.isvolume){
							String term = new String(ch, start, length);
							this.isvolume = false;
							if(term!=null && term.length()>0){
								this.volume = term;
							}
						}
						else
							//issue
							if(this.isIssue){
								String term = new String(ch, start, length);
								this.isIssue = false;
								if(term!=null && term.length()>0){
									this.issue = term;
								}
							}
							else
								//start page
								if(this.isStPage){
									String term = new String(ch, start, length);
									this.isStPage = false;
									if(term!=null && term.length()>2){
										this.startPage = term;
									}
								}
								else
									//end page
									if(this.isEnPage){
										String term = new String(ch, start, length);
										this.isEnPage = false;
										if(term!=null && term.length()>2){
											this.endPage = term;
										}
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
			if(rawName.equalsIgnoreCase("record")){
				//build extend and citationNumber
				if(this.startPage!=null && this.endPage!=null && this.startPage.length()>0 && this.endPage.length()>0){
					String extent = "p."+this.startPage+"-"+this.endPage;
					current.setExtent(extent);
				}
				if(this.issue!=null && this.volume!=null && this.issue.length()>0 && this.volume.length()>0){
					String citationNumber = "v."+this.volume+"("+this.issue+")";
					current.setCitationNumber(citationNumber);
				}

				//mandatory fields
				if(this.current.getTitle2language().size()>0 && this.current.getDateIssued()!=null)
					this.records.add(current);
			}
			else
				//ABSTRACT
				if(rawName.equalsIgnoreCase("abstract")){
					this.isAbstract = false;
					String abs = new String(this.buffer);
					if(abs!=null)
						abs = EscapeXML.getInstance().removeHTMLTagsAndUnescape(abs);
					//check length > 20
					if(abs.length()>20)
						this.current.addAbstract(abs, "");
				}
				else
					// TITLE
					if(rawName.equalsIgnoreCase("title") && this.isTitle){
						this.isTitle = false;
						String term = new String(this.buffer);
						if(term!=null && !term.trim().equals("")){
							//search Solr to see if title exists
							int occurrences = 0;
							if(globalDuplicatesRemoval)
								occurrences = this.indexChecker.checkTitle(term);	
							if(occurrences==0) {
								term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
								this.current.addTitle(term, "");
							}
							else 
								this.current = null;	
						}
					}
					else
						//authors
						if(rawName.equalsIgnoreCase("name")){
							this.creatorPersonal = false;
							String term = new String(this.buffer);
							if(term!=null && !term.trim().equals("")){
								this.current.addCreatorPersonal(term);
							}
						}
						else
							//identifiers FTEXT
							if(rawName.equalsIgnoreCase("fullTextUrl")){
								this.identifier2schema = false;
								String term = new String(this.buffer);
								if(term!=null && !term.trim().equals("")){
									this.current.addIdentifier(term, "dcterms:URI");
								}
							}
							else
								//identifiers DOI
								if(rawName.equalsIgnoreCase("doi")){
									this.identifier2schema = false;
									String term = new String(this.buffer);
									if(term!=null && !term.trim().equals("")){
										this.current.addIdentifier(term, "ags:DOI");
									}
								}
								else
									//publisher name
									if(rawName.equalsIgnoreCase("publisher")){
										this.publisherName = false;
										String term = new String(this.buffer);
										if(term!=null && !term.trim().equals("")){
											this.current.addPublisherName(term);
										}
									}
									else
										//journal title
										if(rawName.equalsIgnoreCase("journalTitle")){
											this.isJournalTitle = false;
											String term = new String(this.buffer);
											if(term!=null && !term.trim().equals("")){
												this.current.setCitationTitle(term);
											}
										}
										else
											//ISSN
											if(rawName.equalsIgnoreCase("issn") && this.isISSN){
												this.isISSN = false;
												String term = new String(this.buffer);			
												if(term!=null && !term.trim().equals("")){
													//naive issn cleaning and add -
													if(!IssnCleaner.isISSN(term))
														term = IssnCleaner.cleanISSN(term);
													//check if the ISSN has to be included
													boolean isRightISSN = true;
													if(this.issnToBeIncluded!=null && this.issnToBeIncluded.size()>0){
														if(!this.issnToBeIncluded.contains(term))
															isRightISSN = false;
													}
													if(isRightISSN){
														//search Solr if it is an ISSN
														this.current.addIssn(term);
													}
													else
														this.current = null;
												}
											}
											else
												//EISSN
												if(rawName.equalsIgnoreCase("eissn") && this.isEISSN){
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
													//language
													if(rawName.equalsIgnoreCase("language")){
														this.isLanguage = false;
														String term = new String(this.buffer);
														if(term!=null && !term.trim().equals("")){
															this.current.addLanguage(term, "dcterms:ISO639-2");
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
