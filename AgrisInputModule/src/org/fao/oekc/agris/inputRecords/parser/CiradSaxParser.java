package org.fao.oekc.agris.inputRecords.parser;

import java.util.List;

import org.fao.oekc.agris.inputRecords.dom.AGRISAttributes;
import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.CheckIndex;
import org.fao.oekc.agris.inputRecords.util.EscapeXML;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses CIRAD XML files
 * @author celli
 *
 */
public class CiradSaxParser extends DefaultHandler {
	
	//parser
	private List<AgrisApDoc> records;
	private AgrisApDoc current;

	//index checker
	private CheckIndex indexChecker;
	
	//data
	private boolean isTitle;
	private boolean creatorPersonal;
	private boolean isDate;
	private boolean isAgrovoc;
	private boolean isAbstract;
	private boolean isSource;
	private boolean isASC;
	private boolean isJournalSubElement;
	private boolean isJournalTitle;
	private boolean isCitationNumber;
	private boolean isExtent;
	private boolean isPublisherSubElement;
	private boolean publisherName;
	private boolean publisherPlace;
	private boolean isDoi;
	private boolean isConference;
	private boolean isType;
	private boolean isURI;
	
	//to read the entire content
	private StringBuffer buffer;
	
	public CiradSaxParser(List<AgrisApDoc> records) {
		try {
			this.indexChecker = new CheckIndex();
			this.records = records;
			this.isAgrovoc = false;
			this.isTitle = false;
			this.isAbstract = false;
			this.isSource = false;
			this.creatorPersonal = false;
			this.publisherName = false;
			this.isASC = false;
			this.isDate = false;
			this.isCitationNumber = false;
			this.isExtent = false;
			this.publisherPlace = false;
			this.isDoi = false;
			this.isConference = false;
			this.isJournalTitle = false;
			this.isType = false;
			this.isURI = false;
			this.isJournalSubElement = false;
			this.isPublisherSubElement = false;
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
		if(rawName.equals("Titre")){
			this.current = new AgrisApDoc();
		}
		else
			if(this.current!=null) {
				//title
				if(rawName.equals("titre") && !this.isJournalSubElement){
					this.buffer = new StringBuffer();
					this.isTitle = true;
				}
				else
					//authors
					if(rawName.equalsIgnoreCase("auteursTXT")){
						this.buffer = new StringBuffer();
						this.creatorPersonal = true;
					}
					else
						//date
						if(rawName.equalsIgnoreCase("annee")){
							this.buffer = new StringBuffer();
							this.isDate = true;
						}
						else
							//agrovoc
							if(rawName.equalsIgnoreCase("motsClesTXT")){
								this.buffer = new StringBuffer();
								this.isAgrovoc = true;
							}
							else
								//abstract
								if(rawName.equalsIgnoreCase("resume")){
									this.buffer = new StringBuffer();
									this.isAbstract = true;
								}
								else
									//source
									if(rawName.equalsIgnoreCase("axeStrategique") || rawName.equalsIgnoreCase("txt")){
										this.buffer = new StringBuffer();
										this.isSource = true;
									}
									else
										//ASC
										if(rawName.equalsIgnoreCase("agris")){
											this.buffer = new StringBuffer();
											this.isASC = true;
										}
										else
											//extent
											if(rawName.equalsIgnoreCase("pagination")){
												this.buffer = new StringBuffer();
												this.isExtent = true;
											}
											else
												//DOI
												if(rawName.equalsIgnoreCase("acces")){
													this.buffer = new StringBuffer();
													this.isDoi = true;
												}
												else
													//conference
													if(rawName.equalsIgnoreCase("congres_affich")){
														this.buffer = new StringBuffer();
														this.isConference = true;
													}
													else
														//type
														if(rawName.equalsIgnoreCase("libelleGroupe")){
															this.buffer = new StringBuffer();
															this.isType = true;
														}
														else
															//URI
															if(rawName.equalsIgnoreCase("urlNoticeCIRAD")){
																this.buffer = new StringBuffer();
																this.isURI = true;
															}
															else
																//journal subelement
																if(rawName.equalsIgnoreCase("Revue")){
																	this.isJournalSubElement = true;
																}
																else
																	//journal title
																	if(rawName.equalsIgnoreCase("titre") && this.isJournalSubElement){
																		this.buffer = new StringBuffer();
																		this.isJournalTitle = true;
																	}
																	else
																		//journal subelement
																		if(rawName.equalsIgnoreCase("volume") && this.isJournalSubElement){
																			this.buffer = new StringBuffer();
																			this.isCitationNumber = true;
																		}
																		else
																			//journal subelement
																			if(rawName.equalsIgnoreCase("numero") && this.isJournalSubElement){
																				this.buffer = new StringBuffer();
																				this.isCitationNumber = true;
																			}
																			else
																				//journal subelement
																				if(rawName.equalsIgnoreCase("Editeur")){
																					this.isPublisherSubElement = true;
																				}
																				else
																					//journal title
																					if(rawName.equalsIgnoreCase("ville") && this.isPublisherSubElement){
																						this.buffer = new StringBuffer();
																						this.publisherPlace = true;
																					}
																					else
																						//journal subelement
																						if(rawName.equalsIgnoreCase("nom") && this.isPublisherSubElement){
																							this.buffer = new StringBuffer();
																							this.publisherName = true;
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
			if(this.creatorPersonal || this.isTitle || this.isAbstract || this.isDoi || this.publisherName
					|| this.isJournalTitle || this.isURI || this.isType || this.isConference || this.isExtent
					|| this.isASC || this.isAgrovoc || this.isCitationNumber || this.publisherPlace
					|| this.isSource || this.isDate){
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
			if(rawName.equals("Titre")){
				//mandatory fields
				if(this.current.getTitle2language().size()>0)
					this.records.add(current);
			}
			else
				//title
				if(rawName.equals("titre") && this.isTitle){
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
					if(rawName.equalsIgnoreCase("auteursTXT") && this.creatorPersonal){
						this.creatorPersonal = false;
						String term = new String(this.buffer);
						if(term!=null && !term.trim().equals("")){
							String[] auths = term.split(", ");
							for(String s: auths)
								this.current.addCreatorPersonal(s);
						}
					}
					else
						//date
						if(rawName.equalsIgnoreCase("annee")){
							this.isDate = false;
							String term = new String(this.buffer);
							if(term!=null && !term.trim().equals("") && !term.trim().equals("s.d."))
								this.current.setDateIssued(term);
						}
						else
							//agrovoc
							if(rawName.equalsIgnoreCase("motsClesTXT")){
								this.isAgrovoc = false;
								String term = new String(this.buffer);
								if(term!=null && !term.trim().equals("")){
									String[] agros = term.split("; ");
									AGRISAttributes attr = new AGRISAttributes();
									attr.setScheme("ags:AGROVOC");
									for(String s: agros) {
										this.current.addAgrovoc(s, attr);
									}
								}
							}
							else
								//abstract
								if(rawName.equalsIgnoreCase("resume")){
									this.isAbstract = false;
									String term = new String(this.buffer);
									if(term!=null && !term.trim().equals("")){
										term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
										this.current.addAbstract(term, "");
									}
								}
								else
									//source
									if(rawName.equalsIgnoreCase("axeStrategique") || rawName.equalsIgnoreCase("txt")){
										this.isSource = false;
										String term = new String(this.buffer);
										if(term!=null && !term.trim().equals("")){
											this.current.setSource(term);
										}
									}
									else
										//ASC
										if(rawName.equalsIgnoreCase("agris")){
											this.isASC = false;
											String term = new String(this.buffer);
											if(term!=null && !term.trim().equals("")){
												String[] agros = term.split("; ");
												for(String s: agros)
													this.current.addSubjClass(s, "ags:ASC");
											}
										}
										else
											//extent
											if(rawName.equalsIgnoreCase("pagination")){
												String term = new String(this.buffer);
												this.isExtent = false;
												if(term!=null && !term.trim().equals("")){
													this.current.setExtent(term);
												}
											}
											else
												//DOI
												if(rawName.equalsIgnoreCase("acces")){
													String term = new String(this.buffer);
													this.isDoi = false;
													if(term!=null && !term.trim().equals("")){
														int index = term.indexOf("http");
														if(index!=-1 && index<term.length())
															term = term.substring(index);
														this.current.addIdentifier(term, "ags:DOI");
													}
												}
												else
													//conference
													if(rawName.equalsIgnoreCase("congres_affich")){
														String term = new String(this.buffer);
														this.isConference = false;
														if(term!=null && !term.trim().equals("")){
															this.current.addCreatorConference(term);
														}
													}
													else
														//type
														if(rawName.equalsIgnoreCase("libelleGroupe")){
															String term = new String(this.buffer);
															this.isType = false;
															if(term!=null && !term.trim().equals("")){
																this.current.addType(term);
															}
														}
														else
															//URI
															if(rawName.equalsIgnoreCase("urlNoticeCIRAD")){
																String term = new String(this.buffer);
																this.isURI = false;
																if(term!=null && !term.trim().equals("")){
																	this.current.addIdentifier(term, "dcterms:URI");
																}
															}
															else
																//journal subelement
																if(rawName.equalsIgnoreCase("Revue")){
																	this.isJournalSubElement = false;
																}
																else
																	//journal title
																	if(rawName.equalsIgnoreCase("titre") && this.isJournalSubElement){
																		String term = new String(this.buffer);
																		this.isJournalTitle = false;
																		if(term!=null && !term.trim().equals("")){
																			this.current.setCitationTitle(term);
																		}
																	}
																	else
																		//journal subelement
																		if(rawName.equalsIgnoreCase("volume") && this.isJournalSubElement){
																			String term = new String(this.buffer);
																			this.isCitationNumber = false;
																			if(term!=null && !term.trim().equals("")){
																				term = "v."+term;
																				this.current.setCitationNumber(term);
																			}
																		}
																		else
																			//journal subelement
																			if(rawName.equalsIgnoreCase("numero") && this.isJournalSubElement){
																				String term = new String(this.buffer);
																				this.isCitationNumber = false;
																				if(term!=null && !term.trim().equals("")){
																					term = "("+term+")";
																					String tmp = this.current.getCitationNumber();
																					if(tmp!=null)
																						term = tmp + term;
																					this.current.setCitationNumber(term);
																				}
																			}
																			else
																				//publisher subelement
																				if(rawName.equalsIgnoreCase("Editeur")){
																					this.isPublisherSubElement = false;
																				}
																				else
																					//publisher place
																					if(rawName.equalsIgnoreCase("ville") && this.isPublisherSubElement){
																						String term = new String(this.buffer);
																						this.publisherPlace = false;
																						if(term!=null && !term.trim().equals("") && !term.equalsIgnoreCase("s.l.")){
																							this.current.addPublisherPlace(term);
																						}
																					}
																					else
																						//publisher name
																						if(rawName.equalsIgnoreCase("nom") && this.isPublisherSubElement){
																							String term = new String(this.buffer);
																							this.publisherName = false;
																							if(term!=null && !term.trim().equals("") && !term.equalsIgnoreCase("s.n.")){
																								this.current.addPublisherName(term);
																							}
																						}
		}
	}

}
