package org.fao.oekc.agris.inputRecords.parser;

import java.util.List;

import jfcutils.util.StringUtils;

import org.fao.oekc.agris.inputRecords.customizations.Customization;
import org.fao.oekc.agris.inputRecords.customizations.DCCustomizations;
import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.CheckIndex;
import org.fao.oekc.agris.inputRecords.util.EscapeXML;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses Simple DC XML files, producing objects if title is not in the Solr index and the ARN does not exist and there is no exact match title
 * @author celli
 *
 */
public class SimpleDCSaxParser extends DefaultHandler{

	//parser
	private List<AgrisApDoc> records;
	private AgrisApDoc current;

	//duplicates removal inside the set
	private List<String> titles;

	//for exceptions
	private Customization dcexception;

	//index checker to remove global duplicates
	private CheckIndex indexChecker; 

	//data
	private boolean isTitle;
	private boolean creatorPersonal;
	private boolean freeSubject;
	private boolean isAbstract;
	private boolean isPublisher;
	private boolean isDate;
	private boolean isType;
	private boolean isFormat;
	private boolean isSource;
	private boolean isLanguage;
	private boolean isIdentifier;
	private boolean isRelation;
	private boolean isCoverage;
	private boolean isRight;
	private boolean isContributor;

	//to read the entire content
	private StringBuffer buffer; 

	/**
	 * Create the parser for the document and start parsing.
	 * All booleans set to false until the corresponding element starts
	 * @param records list of result records
	 */
	public SimpleDCSaxParser(List<AgrisApDoc> records, String arnPrefix, List<String> titles) {
		try {
			this.titles = titles;
			this.indexChecker = new CheckIndex();
			this.records = records;
			this.isTitle = false;
			this.creatorPersonal = false;
			this.freeSubject = false;
			this.isDate = false;
			this.isLanguage = false;
			this.isType = false;
			this.isCoverage = false;
			this.isFormat = false;
			this.isIdentifier = false;
			this.isRelation = false;
			this.isAbstract = false;
			this.isPublisher = false;
			this.isRight = false;
			this.isSource = false;
			this.isContributor = false;
			this.dcexception = new DCCustomizations(arnPrefix);
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
		if(rawName.equalsIgnoreCase("oai_dc:dc") || rawName.equalsIgnoreCase("oai_qdc:qualifieddc")){
			//System.out.println("found");
			this.current = new AgrisApDoc();
		}
		else
			if(this.current!=null) {

				//title
				if(rawName.equalsIgnoreCase("dc:title")){
					this.buffer = new StringBuffer();
					this.isTitle = true;
				}
				else
					//authors
					if(rawName.equalsIgnoreCase("dc:creator")){
						this.buffer = new StringBuffer();
						this.creatorPersonal = true;
					}
					else
						//publisher
						if(rawName.equalsIgnoreCase("dc:publisher")){
							this.buffer = new StringBuffer();
							this.isPublisher = true;
						}
						else
							//contributor
							if(rawName.equalsIgnoreCase("dc:contributor")){
								this.buffer = new StringBuffer();
								this.isContributor = true;
							}
							else
								//date
								if(rawName.equalsIgnoreCase("dc:date") || rawName.equalsIgnoreCase("dcterms:issued")){
									this.isDate = true;
								}
								else
									//language
									if(rawName.equalsIgnoreCase("dc:language")){
										this.isLanguage = true;
									}
									else
										//other keywords
										if(rawName.equalsIgnoreCase("dc:subject")){
											this.buffer = new StringBuffer();
											this.freeSubject = true;
										}
										else
											//identifiers
											if(rawName.equalsIgnoreCase("dc:identifier")){
												this.buffer = new StringBuffer();
												this.isIdentifier = true;
											}
											else
												//relation
												if(rawName.equalsIgnoreCase("dc:relation")){
													this.buffer = new StringBuffer();
													this.isRelation = true;
												}
												else
													//type
													if(rawName.equalsIgnoreCase("dc:type")){
														this.isType = true;
													}
													else
														//source
														if(rawName.equalsIgnoreCase("dc:source")){
															this.isSource = true;
														}
														else
															//rights
															if(rawName.equalsIgnoreCase("dc:rights")){
																this.isRight = true;
															}
															else
																//format
																if(rawName.equalsIgnoreCase("dc:format")){
																	this.isFormat = true;
																}
																else
																	//coverage
																	if(rawName.equalsIgnoreCase("dc:coverage")){
																		this.isCoverage = true;
																	}
																	else
																		//abstract
																		if(rawName.equalsIgnoreCase("dc:description")){
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
			if(this.creatorPersonal || this.isIdentifier || this.freeSubject || this.isContributor || this.isRight
					|| this.isTitle || this.isRelation || this.isAbstract || this.isPublisher || this.isSource){
				this.buffer.append(ch, start, length);
			}
			else
				//date
				if(this.isDate){
					String term = new String(ch, start, length);
					this.isDate = false;
					if(term!=null && !term.trim().equals("")){
						//apply exception
						if(!this.dcexception.dateException(this.current, term))
							this.current.setDateIssued(term);
					}
				}
				else
					//language
					if(this.isLanguage){
						String term = new String(ch, start, length);
						this.isLanguage = false;
						if(term!=null && !term.trim().equals("")){
							//apply exception
							if(!this.dcexception.languageException(this.current, term))
								this.current.addLanguage(term, null);
						}
					}
					else
						//coverage
						if(this.isCoverage){
							String term = new String(ch, start, length);
							this.isCoverage = false;
							if(term!=null && !term.trim().equals("")){
								//apply exception
								if(!this.dcexception.coverageException(this.current, term))
									this.current.addCoverage(term);
							}
						}
						else
							//extent
							if(this.isFormat){
								String term = new String(ch, start, length);
								this.isFormat = false;
								if(term!=null && !term.trim().equals("")){
									//apply exception
									if(!this.dcexception.formatException(this.current, term))
										this.current.setFormat(term);
								}
							}
							else
								//type
								if(this.isType){
									String term = new String(ch, start, length);
									this.isType = false;
									if(term!=null && !term.trim().equals("")){
										this.current.addType(term);
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
			if(rawName.equalsIgnoreCase("oai_dc:dc") || rawName.equalsIgnoreCase("oai_qdc:qualifieddc")){
				//mandatory fields
				if(this.current.getTitle2language().size()>0 ) //&& this.current.getDateIssued()!=null)
					this.records.add(current);
			}
			else
				//ABSTRACT
				if(rawName.equalsIgnoreCase("dc:description")){
					this.isAbstract = false;
					String abs = new String(this.buffer);
					if(abs!=null)
						abs = EscapeXML.getInstance().removeHTMLTagsAndUnescape(abs);
					//apply exception
					if(!this.dcexception.abstractException(this.current, abs))
						this.current.addAbstract(abs, "");
				}
				else
					//PUBLISHER
					if(rawName.equalsIgnoreCase("dc:publisher") && this.isPublisher){
						this.isPublisher = false;
						String abs = new String(this.buffer);
						this.current.addPublisherName(abs);
					}
					else
						//CONTRIBUTOR
						if(rawName.equalsIgnoreCase("dc:contributor") && this.isContributor){
							this.isContributor = false;
							String abs = new String(this.buffer);
							this.current.addCreatorCorporate(abs);
						}
						else
							// TITLE
							if(rawName.equalsIgnoreCase("dc:title") && this.isTitle){
								this.isTitle = false;
								String term = new String(this.buffer);
								//System.out.println(term);
								if(term!=null && !term.trim().equals("")){

									//search Solr to see if title exists
									int occurrences = this.indexChecker.checkTitle(term);			
									if(occurrences==0) {
										term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
										//check title inside the set of the current center
										if(!this.titles.contains(term)){
											this.titles.add(term);
											this.current.addTitle(term, "");
										}
										else  {
											this.current = null;
										}
									}
									else {
										this.current = null;
									}
								}
							}
							else
								//authors
								if(rawName.equalsIgnoreCase("dc:creator")){
									this.creatorPersonal = false;
									String term = new String(this.buffer);
									if(term!=null && !term.trim().equals("")){
										if(this.dcexception.hasFAOtoBeExcluded(term))
											this.current = null;
										else {
											term = term.replaceAll("\n", " ");
											term = term.replaceAll("\r", " ");
											//try to split something
											String[] splitted = term.split(" and ");
											if(splitted.length==1)
												splitted = term.split(";");
											for(String c: splitted){
												if(c!=null && !c.equalsIgnoreCase("-"))
													this.current.addCreatorPersonal((new StringUtils()).trimLeft(c));		
											}
										}
									}
								}
								else
									//identifiers
									if(rawName.equalsIgnoreCase("dc:identifier")){
										this.isIdentifier = false;
										String term = new String(this.buffer);
										if(term!=null && !term.trim().equals("")){
											//apply exception
											if(!this.dcexception.identifierException(this.current, term))
												this.current.addIdentifier(term, "dcterms:URI");		
										}
									}
									else
										//relation
										if(rawName.equalsIgnoreCase("dc:relation")){
											this.isRelation = false;
											String term = new String(this.buffer);
											if(term!=null && !term.trim().equals("")){
												//apply exception
												if(!this.dcexception.relationException(this.current, term))
													this.current.addIsPartOfRelation(term, "dcterms:URI");
											}
										}
										else
											//freeSubject: add if keywords lenght is grater than 2
											if(rawName.equalsIgnoreCase("dc:subject")){
												this.freeSubject = false;
												String term = new String(this.buffer);
												if(term!=null && term.length()>2){
													//split on slash
													String[] splittedSubj = term.split("/");
													//chech if splits by /
													if(splittedSubj.length==1)
														splittedSubj = term.split(",");
													//chech if splits by ,
													if(splittedSubj.length==1)
														splittedSubj = term.split(";");
													for(String s: splittedSubj){
														this.current.addFreeSubject(s);
													}
												}
											}
											else
												//SOURCE
												if(rawName.equalsIgnoreCase("dc:source") && this.isSource){
													this.isSource = false;
													String term = new String(this.buffer);
													if(term!=null && !term.trim().equals("")){
														//apply exception
														if(!this.dcexception.sourceException(this.current, term))
															this.current.setSource(term);
													}
												}
												else
													//RIGHT
													if(rawName.equalsIgnoreCase("dc:rights") && this.isRight){
														this.isRight = false;
														String term = new String(this.buffer);
														if(term!=null && !term.trim().equals("")){
															this.current.setRights(term);
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
