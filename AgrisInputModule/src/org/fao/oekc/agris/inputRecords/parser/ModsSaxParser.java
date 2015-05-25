package org.fao.oekc.agris.inputRecords.parser;

import java.util.List;

import jfcutils.util.StringUtils;

import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.CheckIndex;
import org.fao.oekc.agris.inputRecords.util.IsbnCleaner;
import org.fao.oekc.agris.inputRecords.util.EscapeXML;
import org.fao.oekc.agris.inputRecords.util.IssnCleaner;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * MODS parser, defined for Embrapa.
 * Duplicates removal based on titles
 * @author celli
 *
 */
public class ModsSaxParser extends DefaultHandler{

	//parser
	private List<AgrisApDoc> records;
	private AgrisApDoc current;

	//index checker
	private CheckIndex indexChecker;

	//to read the entire content
	private StringBuffer buffer; 

	//for ARN generation
	private String countryC;
	private String subCenterCode;
	private String dateCreated;
	private String arnID;

	//flags
	private boolean isTitle;
	private boolean isDateCreated;
	private boolean isLanguage;
	private boolean isSubject;
	private boolean isPublisher;
	private boolean isType;
	private boolean isAbstract;
	private boolean isSerialTitle;
	private boolean isISSN;
	private boolean isISBN;
	private boolean isFullText;
	private boolean isSource;
	private boolean isCreatorCorp;
	private boolean isCreatorPers;
	
	//support variable for authors and corporates
	private boolean isRole;

	/**
	 * Create the parser for the document and start parsing.
	 * All booleans set to false until the corresponding element starts
	 * @param records list of result records
	 */
	public ModsSaxParser(List<AgrisApDoc> records, String countryC, String subCenterCode) {
		try {
			this.indexChecker = new CheckIndex();
			this.records = records;
			this.countryC = countryC;
			this.subCenterCode = subCenterCode;
			//flags
			this.isTitle = false;
			this.isLanguage = false;
			this.isDateCreated = false;
			this.isSubject = false;
			this.isPublisher = false;
			this.isType = false;
			this.isAbstract = false;
			this.isSerialTitle = false;
			this.isISSN = false;
			this.isISBN = false;
			this.isFullText = false;
			this.isSource = false;
			this.isCreatorCorp = false;
			this.isCreatorPers = false;
			this.isRole = false;
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
		if(rawName.equalsIgnoreCase("dmdSec")){
			this.current = new AgrisApDoc();
			//check ID
			String arnId = atts.getValue("ID");
			if(arnId!=null && arnId.length()>0){
				//find the ID from a string like DMD_hdl_doc/89269
				int indexSlash = arnId.indexOf("/");
				if(indexSlash!=-1)
					arnId = arnId.substring(indexSlash+1);
				this.arnID = arnId;
			}
			else
				this.current=null;
		}
		else
			if(this.current!=null) {
				//title
				if(rawName.equalsIgnoreCase("mods:titleInfo")){
					this.buffer = new StringBuffer();
					this.isTitle = true;
				}
				else
					//date created
					if(rawName.equalsIgnoreCase("mods:dateCreated")){
						this.buffer = new StringBuffer();
						this.isDateCreated = true;
					}
					else
						//language
						if(rawName.equalsIgnoreCase("mods:languageTerm")){
							this.buffer = new StringBuffer();
							this.isLanguage = true;
						}
						else
							//Subject
							if(rawName.equalsIgnoreCase("mods:topic")){
								this.buffer = new StringBuffer();
								this.isSubject = true;
							}
							else
								//Publisher
								if(rawName.equalsIgnoreCase("mods:publisher")){
									this.buffer = new StringBuffer();
									this.isPublisher = true;
								}
								else
									//Type
									if(rawName.equalsIgnoreCase("mods:genre")){
										this.buffer = new StringBuffer();
										this.isType = true;
									}
									else
										//Abstract
										if(rawName.equalsIgnoreCase("mods:note")){
											if(atts.getLength()==0){
												this.buffer = new StringBuffer();
												this.isAbstract = true;
											}
										}
										else
											//serial title
											if(rawName.equalsIgnoreCase("mods:relatedItem")){
												String type = atts.getValue("type");
												if(type!=null && type.equalsIgnoreCase("series")){
													this.buffer = new StringBuffer();
													this.isSerialTitle = true;
												}
											}
											else
												//source
												if(rawName.equalsIgnoreCase("mods:relatedItem")){
													String type = atts.getValue("type");
													if(type!=null && type.equalsIgnoreCase("host")){
														this.buffer = new StringBuffer();
														this.isSource = true;
													}
												}
												else
													//ISSN
													if(rawName.equalsIgnoreCase("mods:identifier")){
														String type = atts.getValue("type");
														if(type!=null && type.equalsIgnoreCase("issn")){
															this.buffer = new StringBuffer();
															this.isISSN = true;
														}
													}
													else
														//ISBN
														if(rawName.equalsIgnoreCase("mods:identifier")){
															String type = atts.getValue("type");
															if(type!=null && type.equalsIgnoreCase("isbn")){
																this.buffer = new StringBuffer();
																this.isISBN = true;
															}
														}
														else
															//FULLTEXT
															if(rawName.equalsIgnoreCase("file")){
																String type = atts.getValue("MIMETYPE");
																if(type!=null && type.equalsIgnoreCase("application/pdf")){
																	this.isFullText = true;
																}
															}
															else
																if(rawName.equalsIgnoreCase("FLocat") && this.isFullText){
																	this.isFullText = false;
																	String ft = atts.getValue("xlink:href");
																	if(ft!=null)
																		this.current.addIdentifier(ft, "dcterms:URI");
																}
																else
																	//ROLE: author or corporate
																	if(rawName.equalsIgnoreCase("mods:roleTerm")){
																		this.isRole = true;
																		this.buffer = new StringBuffer();
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
			if(this.isTitle || this.isDateCreated || this.isLanguage || this.isSubject || this.isPublisher
					|| this.isType || this.isAbstract || this.isSerialTitle || this.isISSN
					|| this.isISBN || this.isSource || this.isRole || this.isCreatorPers || this.isCreatorCorp){
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
			if(rawName.equalsIgnoreCase("fileSec")){
				//ARN generation
				if(this.dateCreated!=null && this.arnID!=null){
					String arn = this.countryC + this.dateCreated + this.subCenterCode + this.arnID;
					//check ARN
					int occurrences = this.indexChecker.checkArn(arn);
					if(occurrences!=0)
						System.out.println("-- ERROR: no ARN already in the index "+arn);
					else 
						current.setARN(arn);
					//empty variables
					this.arnID = null;
					this.dateCreated = null;
				}

				//mandatory fields
				if(this.current.getTitle2language().size()>0 && this.current.getARN()!=null)
					this.records.add(current);
			}
			else
				// TITLE
				if(rawName.equalsIgnoreCase("mods:titleInfo") && this.isTitle){
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
					//date created
					if(rawName.equalsIgnoreCase("mods:dateCreated")){
						this.isDateCreated = false;
						String term = new String(this.buffer);
						if(term!=null && !term.trim().equals("")){
							this.current.setDateIssued(term);
							//for ARN generation
							if(term.length()>4)
								term.substring(0, 4);
							this.dateCreated = term;
						}
					}
					else
						//language
						if(rawName.equalsIgnoreCase("mods:languageTerm")){
							this.isLanguage = false;
							String term = new String(this.buffer);
							if(term!=null && !term.trim().equals("")){
								//check EMBRAPA
								if(term.equalsIgnoreCase("pt_BR"))
									this.current.addLanguage("por", "dcterms:ISO639-2");
								else
									this.current.addLanguage(term, "");
							}
						}
						else
							//Subject
							if(rawName.equalsIgnoreCase("mods:topic")){
								this.isSubject = false;
								String term = new String(this.buffer);
								if(term!=null && !term.trim().equals("")){
									this.current.addFreeSubject(term);
								}
							}
							else
								//Publisher
								if(rawName.equalsIgnoreCase("mods:publisher")){
									this.isPublisher = false;
									String term = new String(this.buffer);
									if(term!=null && !term.trim().equals("")){
										//naive cleaning
										//can be: publishername, YYYY. -> publishername.
										term = term.replaceAll(", [0-9][0-9][0-9][0-9]", "");
										this.current.addPublisherName(term);
									}
								}
								else
									//Type
									if(rawName.equalsIgnoreCase("mods:genre")){
										this.isType = false;
										String term = new String(this.buffer);
										if(term!=null && !term.trim().equals("")){
											this.current.addType(term);
										}
									}
									else
										//Abstract
										if(rawName.equalsIgnoreCase("mods:note") && this.isAbstract){
											this.isAbstract = false;
											String term = new String(this.buffer);
											if(term!=null && !term.trim().equals("")){
												this.current.addAbstract(term, "");
											}
										}
										else
											//serial title
											if(rawName.equalsIgnoreCase("mods:relatedItem") && this.isSerialTitle){
												this.isSerialTitle = false;
												String term = new String(this.buffer);
												if(term!=null && !term.trim().equals("")){
													//naive cleaning and extract citationNumber
													if(term.endsWith("."))
														term = term.substring(0, term.length()-1);
													if(term.startsWith("(") && term.endsWith(""))
														term = term.substring(1, term.length()-1);
													//format: Coleção Plantar, 6
													int lastIndexComma = term.lastIndexOf(",");
													if(lastIndexComma!=-1 && term.length()>(lastIndexComma+2)){
														String tmp = term.substring(lastIndexComma+2);
														if((new StringUtils()).isInteger(tmp)){
															term = term.substring(0, lastIndexComma);
															this.current.setCitationNumber("v."+tmp);
														}
													}
													this.current.setCitationTitle(term);
												}
											}
											else
												//ISSN
												if(rawName.equalsIgnoreCase("mods:identifier") && this.isISSN){
													this.isISSN = false;
													String term = new String(this.buffer);
													if(term!=null && !term.trim().equals("")){
														//check format
														if(IssnCleaner.isISSN(term))
															this.current.addIssn(term);
														else {
															//check ISBN
															if(IsbnCleaner.hasIsbnFormat(term))
																this.current.addIdentifier(term, "ags:ISBN");
														}
													}
												}
												else
													//ISBN
													if(rawName.equalsIgnoreCase("mods:identifier") && this.isISBN){
														this.isISBN = false;
														String term = new String(this.buffer);
														if(term!=null && !term.trim().equals("")){
															this.current.addIdentifier(term, "ags:ISBN");
														}
													}
													else
														//source
														if(rawName.equalsIgnoreCase("mods:relatedItem") && this.isSource){
															this.isSource = false;
															String term = new String(this.buffer);
															if(term!=null && !term.trim().equals("")){
																this.current.setSource(term);
															}
														}
														else
															//ROLE: author or corporate
															if(rawName.equalsIgnoreCase("mods:roleTerm")){
																this.isRole = false;
																String term = new String(this.buffer);
																if(term!=null && !term.trim().equals("")){
																	//case1: author
																	if(term.equalsIgnoreCase("author"))
																		this.isCreatorPers = true;
																	//case2: corporate
																	if(term.equalsIgnoreCase("other"))
																		this.isCreatorCorp = true;
																	this.buffer = new StringBuffer();
																}
															}
															else
																//author
																if(rawName.equalsIgnoreCase("mods:namePart") && this.isCreatorPers){
																	this.isCreatorPers = false;
																	String term = new String(this.buffer);
																	if(term!=null && !term.trim().equals("")){
																		term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
																		this.current.addCreatorPersonal(term);
																	}
																}
																else
																	//corporate
																	if(rawName.equalsIgnoreCase("mods:namePart") && this.isCreatorCorp){
																		this.isCreatorCorp = false;
																		String term = new String(this.buffer);
																		if(term!=null && !term.trim().equals("")){
																			term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
																			this.current.addCreatorCorporate(term);
																		}
																	}
		}
	}

}
