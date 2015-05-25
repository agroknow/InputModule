package org.fao.oekc.agris.inputRecords.parser;

import java.util.List;

import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.CheckIndex;
import org.fao.oekc.agris.inputRecords.util.EscapeXML;
import org.fao.oekc.agris.inputRecords.util.IssnCleaner;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses USAMV ROMANIA XML files
 * @author celli
 *
 */
public class UsamvSaxParser extends DefaultHandler {

	//parser
	private List<AgrisApDoc> records;
	private AgrisApDoc current;

	//index checker
	private CheckIndex indexChecker;

	//data
	private boolean isTitle;
	private boolean isCreatorPersonal;
	private boolean isCreatorPersonalFamily;
	private boolean isAbstract;
	private boolean isLanguage;
	private boolean publisherName;
	private boolean isJournalTitle;
	private boolean isISSN;
	private boolean isDate;
	private boolean isJournalNumber;
	private boolean isJournalPages;

	//to read the entire content
	private StringBuffer buffer;
	private String tmp;

	public UsamvSaxParser(List<AgrisApDoc> records) {
		try {
			this.indexChecker = new CheckIndex();
			this.records = records;
			this.isCreatorPersonal = false;
			this.isCreatorPersonalFamily = false;
			this.isTitle = false;
			this.isAbstract = false;
			this.publisherName = false;
			this.isISSN = false;
			this.isDate = false;
			this.isLanguage = false;
			this.isJournalTitle = false;
			this.isJournalNumber = false;
			this.isJournalPages = false;
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
		if(rawName.equals("Article")){
			this.current = new AgrisApDoc();
		}
		else
			if(this.current!=null) {
				//title: ENGLISH
				if(rawName.equals("ArticleTitle")){
					this.buffer = new StringBuffer();
					this.isTitle = true;
				}
				else
					//author name
					if(rawName.equalsIgnoreCase("FirstName")){
						this.buffer = new StringBuffer();
						this.isCreatorPersonal = true;
					}
					else
						//author surname
						if(rawName.equalsIgnoreCase("LastName")){
							this.buffer = new StringBuffer();
							this.isCreatorPersonalFamily = true;
						}
						else
							//date
							if(rawName.equalsIgnoreCase("Year")){
								this.buffer = new StringBuffer();
								this.isDate = true;
							}
							else
								//abstract
								if(rawName.equalsIgnoreCase("Abstract")){
									this.buffer = new StringBuffer();
									this.isAbstract = true;
								}
								else
									//ISSN
									if(rawName.equalsIgnoreCase("Issn")){
										this.buffer = new StringBuffer();
										this.isISSN = true;
									}
									else
										//language
										if(rawName.equalsIgnoreCase("Language")){
											this.buffer = new StringBuffer();
											this.isLanguage = true;
										}
										else
											//journal title
											if(rawName.equalsIgnoreCase("JournalTitle")){
												this.buffer = new StringBuffer();
												this.isJournalTitle = true;
											}
											else
												//publishername
												if(rawName.equalsIgnoreCase("PublisherName")){
													this.buffer = new StringBuffer();
													this.publisherName = true;
												}
												else
													//citation number volume
													if(rawName.equalsIgnoreCase("Volume")){
														this.buffer = new StringBuffer();
														this.isJournalNumber = true;			
													}
													else
														//citation number pages
														if(rawName.equalsIgnoreCase("FirstPage")){
															this.buffer = new StringBuffer();
															this.isJournalPages = true;			
														}
														else
															//citation last number pages
															if(rawName.equalsIgnoreCase("LastPage")){
																this.buffer = new StringBuffer();
																this.isJournalPages = true;			
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
			if(this.isCreatorPersonal || this.isCreatorPersonalFamily|| this.isTitle || this.isAbstract || this.isLanguage || this.publisherName
					|| this.isJournalTitle || this.isISSN || this.isDate || this.isJournalPages || this.isJournalNumber){
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
			if(rawName.equals("Article")){
				//mandatory fields
				if(this.current.getTitle2language().size()>0)
					this.records.add(current);
			}
			else
				//title: ENGLISH
				if(rawName.equals("ArticleTitle") && this.isTitle){
					this.isTitle = false;
					String term = new String(this.buffer);
					if(term!=null && !term.trim().equals("")){
						//search Solr to see if title exists
						int occurrences = this.indexChecker.checkTitle(term);	
						if(occurrences==0) {
							term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
							this.current.addTitle(term, "en");
						}
						else 
							this.current = null;
					}
				}
				else 
					//creator name
					if(rawName.equalsIgnoreCase("FirstName") && this.isCreatorPersonal){
						this.isCreatorPersonal  = false;
						String term = new String(this.buffer);
						if(term!=null && !term.trim().equals("")){
							this.tmp = term;
						}
					}
					else 
						//creator family name
						if(rawName.equalsIgnoreCase("LastName") && this.isCreatorPersonalFamily){
							this.isCreatorPersonalFamily  = false;
							String term = new String(this.buffer);
							if(term!=null && !term.trim().equals("")){
								if(this.tmp!=null && this.tmp.length()>0)
									this.tmp = term + ", "+this.tmp;
								else
									this.tmp = term;

							}
						}
						else
							//restore tmp
							if(rawName.equalsIgnoreCase("Author")){
								this.current.addCreatorPersonal(this.tmp);
								this.tmp = "";
							}
							else
								//date
								if(rawName.equalsIgnoreCase("Year")){
									this.isDate = false;
									String term = new String(this.buffer);
									if(term!=null && !term.trim().equals(""))
										this.current.setDateIssued(term);
								}
								else
									//abstract
									if(rawName.equalsIgnoreCase("Abstract")){
										this.isAbstract = false;
										String term = new String(this.buffer);
										if(term!=null && !term.trim().equals("")){
											term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
											this.current.addAbstract(term, "en");
										}
									}
									else
										//issn
										if(rawName.equalsIgnoreCase("Issn")){
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
											//language
											if(rawName.equalsIgnoreCase("Language")){
												String term = new String(this.buffer);
												this.isLanguage = false;
												if(term!=null && !term.trim().equals("")){
													this.current.addLanguage(term, "dcterms:ISO639-2");
												}
											}
											else
												//journal title
												if(rawName.equalsIgnoreCase("JournalTitle")){
													String term = new String(this.buffer);
													this.isJournalTitle = false;
													if(term!=null && !term.trim().equals("")){
														this.current.setCitationTitle(term);
													}
												}
												else
													//publisher name
													if(rawName.equalsIgnoreCase("PublisherName")){
														String term = new String(this.buffer);
														this.publisherName = false;
														if(term!=null && !term.trim().equals("")){
															this.current.addPublisherName(term);
														}
													}
													else
														//citation number
														if(rawName.equalsIgnoreCase("Volume") && this.isJournalNumber){
															this.isJournalNumber  = false;
															String term = new String(this.buffer);
															if(term!=null && !term.trim().equals("")){
																this.current.setCitationNumber("v."+term);
															}
														}
														else
															//citation number fist pages
															if(rawName.equalsIgnoreCase("FirstPage") && this.isJournalPages){
																this.isJournalPages  = false;
																String term = new String(this.buffer);
																if(term!=null && !term.trim().equals("")){
																	this.tmp = term;
																}
															} else
																//citation number last pages
																if(rawName.equalsIgnoreCase("LastPage") && this.isJournalPages){
																	this.isJournalPages  = false;
																	String term = new String(this.buffer);
																	if(term!=null && !term.trim().equals("")){
																		if(this.tmp!=null && this.tmp.length()>0)
																			this.tmp = this.tmp + "-"+term;
																		else
																			this.tmp = term;	
																	}
																	//reset tmp
																	this.current.setExtent("p."+this.tmp);
																	this.tmp = "";
																}
		}
	}

}
