package org.fao.oekc.agris.inputRecords.parser;

import java.util.List;
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
 * Parses AJOL XML files, producing objects if title is not in the Solr index and the journal's title is on a list given as parameter
 * @author celli
 *
 */
public class AJOLSaxParser extends DefaultHandler {

	//parser
	private List<AgrisApDoc> records;
	private AgrisApDoc current;

	//filter file
	private Set<String> journalsToBeIncluded;

	//index checker to remove global duplicates
	private CheckIndex indexChecker; 

	//data
	private boolean isTitle;
	private boolean isCreator;
	private boolean isCreatorSurname;
	private boolean isCreatorGivenName;
	private boolean freeSubject;
	private boolean isJournalTitle;
	private boolean isISSN;
	private boolean isEISSN;
	private boolean isAbstract;
	private boolean isDate;
	private boolean isYear;
	private boolean isPublisher;
	private boolean isRight;
	private boolean isDoi;

	//to read the entire content
	private StringBuffer buffer; 
	private String temp;

	/**
	 * Create the parser for the document and start parsing.
	 * All booleans set to false until the corresponding element starts
	 * @param records list of result records
	 * @param journalsToBeIncluded lines of the input text file (also null, it is optional), used to filter journals' titles
	 */
	public AJOLSaxParser(List<AgrisApDoc> records, Set<String> journalsToBeIncluded) {
		try {
			this.indexChecker = new CheckIndex();
			this.records = records;
			this.journalsToBeIncluded = journalsToBeIncluded;
			this.isTitle = false;
			this.isCreator = false;
			this.isCreatorSurname = false;
			this.isCreatorGivenName = false;
			this.freeSubject = false;
			this.isJournalTitle = false;
			this.isISSN = false;
			this.isEISSN = false;
			this.isAbstract = false;
			this.isDate = false;
			this.isYear = false;
			this.isPublisher = false;
			this.isRight = false;
			this.isDoi = false;
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
		if(rawName.equalsIgnoreCase("metadata")){
			this.current = new AgrisApDoc();
		}
		else
			if(this.current!=null) {

				//title
				if(rawName.equalsIgnoreCase("article-title")){
					this.buffer = new StringBuffer();
					this.isTitle = true;
				} else
					//authors
					if(rawName.equalsIgnoreCase("contrib")){
						String type = atts.getValue("contrib-type");
						if(type!=null && type.equalsIgnoreCase("author"))
							this.isCreator = true;
					} else
						if(rawName.equalsIgnoreCase("surname") && this.isCreator){
							this.buffer = new StringBuffer();
							this.isCreatorSurname = true;
						}
						else
							if(rawName.equalsIgnoreCase("given-names") && this.isCreator){
								this.buffer = new StringBuffer();
								this.isCreatorGivenName = true;
							}
							else
								//other keywords
								if(rawName.equalsIgnoreCase("kwd")){
									this.buffer = new StringBuffer();
									this.freeSubject = true;
								}
								else
									//journal title
									if(rawName.equalsIgnoreCase("journal-title")){
										this.buffer = new StringBuffer();
										this.isJournalTitle = true;
									}
									else
										//ISSN
										if(rawName.equalsIgnoreCase("issn")){
											this.buffer = new StringBuffer();
											String type = atts.getValue("pub-type");
											if(type!=null && type.equalsIgnoreCase("epub"))
												this.isEISSN = true;
											else if(type!=null && type.equalsIgnoreCase("ppub"))
												this.isISSN = true;
										}
										else
											//pub-date
											if(rawName.equalsIgnoreCase("pub-date")){
												String type = atts.getValue("pub-type");
												if(type.equals("epub"))
													this.isDate = true;
											}
											else
												if(rawName.equalsIgnoreCase("year") && this.isDate){
													this.isYear = true;
													this.buffer = new StringBuffer();
												}
												else
													//publisher
													if(rawName.equalsIgnoreCase("publisher-name")){
														this.isPublisher = true;
														this.buffer = new StringBuffer();
													}
													else
														//copyright
														if(rawName.equalsIgnoreCase("copyright-statement")){
															this.isRight = true;
															this.buffer = new StringBuffer();
														}
														else
															//doi
															if(rawName.equalsIgnoreCase("article-id")){
																String type = atts.getValue("pub-id-type");
																if(type!=null && type.equals("doi")){
																	this.isDoi = true;
																	this.buffer = new StringBuffer();
																}
															}
															else
																//self-uri isPartOf
																if(rawName.equalsIgnoreCase("self-uri")){
																	String type = atts.getValue("content-type");
																	String uri = atts.getValue("xlink:href");
																	if(uri!=null)
																		if(type!=null && type.equals("application/pdf"))
																			this.current.addIdentifier(uri, "dcterms:URI");
																		else
																			this.current.addIsPartOfRelation(uri, "dcterms:URI");
																}
																else
																	//abstract
																	if(rawName.equalsIgnoreCase("abstract")){
																		this.buffer = new StringBuffer();
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
			if(this.isTitle || this.isCreatorGivenName || this.isCreatorSurname || this.freeSubject ||
					this.isJournalTitle || this.isISSN || this.isEISSN || this.isAbstract || this.isYear
					|| this.isPublisher || this.isRight || this.isDoi){
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
				if(this.current.getTitle2language().size()>0 && this.current.getDateIssued()!=null){
					this.current.addType("Article");
					this.records.add(current);
				}
			}
			else 
				// TITLE
				if(rawName.equalsIgnoreCase("article-title") && this.isTitle){
					this.isTitle = false;
					String term = new String(this.buffer);
					if(term!=null && !term.trim().equals("")){

						//search Solr to see if title exists
						int occurrences = this.indexChecker.checkTitle(term);			
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
					if(rawName.equalsIgnoreCase("contrib") && this.isCreator){
						this.isCreator = false;
						this.temp = null;
					} else
						if(rawName.equalsIgnoreCase("surname") && this.isCreator){
							this.isCreatorSurname = false;
							String term = new String(this.buffer);
							if(term!=null && !term.trim().equals("")){
								this.temp = term + ", ";
							}
						}
						else
							if(rawName.equalsIgnoreCase("given-names") && this.isCreator){
								this.isCreatorGivenName = false;
								String term = new String(this.buffer);
								if(term!=null && !term.trim().equals("")){
									if(this.temp!=null)
										this.current.addCreatorPersonal(this.temp+term);
									else
										this.current.addCreatorPersonal(term);
								}
							}
							else
								//freeSubject: add if keywords lenght is grater than 2
								if(rawName.equalsIgnoreCase("kwd")){
									this.freeSubject = false;
									String term = new String(this.buffer);
									if(term!=null && term.length()>2){
										//split on slash
										String[] splittedSubj = term.split(",");
										StringUtils cleaner = new StringUtils();
										for(String s: splittedSubj){
											s = cleaner.trimLeft(s);
											this.current.addFreeSubject(s);
										}
									}
								}
								else
									//journal title
									if(rawName.equalsIgnoreCase("journal-title")){
										this.isJournalTitle = false;
										String term = new String(this.buffer);
										if(term!=null && !term.trim().equals("")){
											if(this.journalsToBeIncluded.contains(term))
												this.current.setCitationTitle(term);
											else
												this.current = null;
										}
									}
									else
										//ISSN
										if(rawName.equalsIgnoreCase("issn")){
											String term = new String(this.buffer);			
											if(term!=null && !term.trim().equals("")){
												//naive issn cleaning and add -
												if(!IssnCleaner.isISSN(term))
													term = IssnCleaner.cleanISSN(term);
												if(IssnCleaner.isISSN(term)){
													//search Solr if it is an ISSN
													if(this.isISSN)
														this.current.addIssn(term);
													else
														if(this.isEISSN)
															this.current.addEissn(term);
												}
											}
											this.isISSN = false;
											this.isEISSN = false;
										}
										else
											//date
											if(rawName.equalsIgnoreCase("pub-date") && this.isDate){
												this.isDate = false;
											} else
												if(rawName.equalsIgnoreCase("year") && this.isYear){
													this.isYear = false;
													String term = new String(this.buffer);
													if(term!=null && !term.trim().equals("")){
														this.current.setDateIssued(term);
													}
												}
												else
													//publisher
													if(rawName.equalsIgnoreCase("publisher-name")){
														this.isPublisher = false;
														String term = new String(this.buffer);
														if(term!=null && !term.trim().equals("")){
															this.current.addPublisherName(term);
														}
													}
													else
														//copyright
														if(rawName.equalsIgnoreCase("copyright-statement")){
															this.isRight = false;
															String term = new String(this.buffer);
															if(term!=null && !term.trim().equals("")){
																this.current.setRights(term);
															}
														}
														else
															//doi
															if(rawName.equalsIgnoreCase("article-id") && this.isDoi){
																this.isDoi = false;
																String term = new String(this.buffer);
																if(term!=null && !term.trim().equals("")){
																	this.current.addIdentifier(term, "ags:DOI");
																}
															}
															else
																//ABSTRACT
																if(rawName.equalsIgnoreCase("abstract")){
																	this.isAbstract = false;
																	String abs = new String(this.buffer);
																	if(abs!=null && !abs.equalsIgnoreCase("<p>No Abstract.</p>")) {
																		abs = EscapeXML.getInstance().removeHTMLTagsAndUnescape(abs);
																		this.current.addAbstract(abs, this.temp);
																	}
																}
		}
	}

}
