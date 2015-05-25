package org.fao.oekc.agris.inputRecords.parser;

import java.util.List;
import java.util.Map;

import jfcutils.files.conversions.EscapeCommonXML;

import org.fao.oekc.agris.inputRecords.dom.AGRISAttributes;
import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.CheckIndex;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse ELDIS XML Data
 * http://api.ids.ac.uk/docs/
 * Your access GUID is a278dd90-85a7-465e-aa5f-cdb0adc910a0
 * @author celli
 *
 */
public class EldisSaxParser extends DefaultHandler{

	//parser
	private List<AgrisApDoc> records;
	private AgrisApDoc current;

	//mapping document url -> agrovoc extracted keywords
	private Map<String, List<String>> autotagger;

	//index checker
	private CheckIndex indexChecker; 

	//to read the entire content
	private StringBuffer buffer; 

	//for Agrovoc
	private AGRISAttributes attr;

	//data
	private boolean isTitle;
	private boolean isDate;
	private boolean isUrlListItem;
	private boolean isUrl;
	private boolean isKeywordListItem;
	private boolean isKeyword;
	private boolean isPublisher;
	private boolean isLanguage;
	private boolean isAbstract;
	private boolean isCoverage;
	private boolean isCoverageListItem;
	private boolean isCreator;
	private boolean isCreatorListItem;
	private boolean isNote;

	//say if the record should be discarded
	private boolean discard_rec;
	//list-item level. Level=0 means the root of the document
	private int listitem_lev;

	/**
	 * Create the parser for the document and start parsing.
	 * All booleans set to false until the corresponding element starts
	 * @param records list of result records
	 * @param autotagger map URL_DOC -> list agrovoc keywords
	 * @param agrovocEN complete lowercased agrovoc english terms
	 */
	public EldisSaxParser(List<AgrisApDoc> records, Map<String, List<String>> autotagger) {
		try {
			this.indexChecker = new CheckIndex();
			this.autotagger = autotagger;
			this.records = records;

			attr = new AGRISAttributes();
			attr.setLang("en");
			attr.setScheme("ags:AGROVOC");

			this.isTitle = false;
			this.isDate = false;
			this.isUrlListItem = false;
			this.isUrl = false;
			this.isKeyword = false;
			this.isKeywordListItem = false;
			this.isPublisher = false;
			this.isLanguage = false;
			this.isAbstract = false;
			this.isCoverage = false;
			this.isCoverageListItem = false;
			this.isCreator = false;
			this.isCreatorListItem = false;
			this.isNote = false;

			this.discard_rec = false;
			this.listitem_lev = -1;

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

		//RECORD and increase list-item count level
		if(rawName.equalsIgnoreCase("list-item")){
			this.listitem_lev++;
			if(this.listitem_lev==0) {
				this.current = new AgrisApDoc();
			}
		}

		// not put "else": list-item still needs to be checked
		if(this.current!=null && !discard_rec) {
			//title
			if(rawName.equalsIgnoreCase("title")){
				this.buffer = new StringBuffer();
				this.isTitle = true;
			}
			else
				//date
				if(rawName.equalsIgnoreCase("publication_year")){
					this.buffer = new StringBuffer();
					this.isDate = true;
				}
				else
					//publisher: not ignore CASE
					if(rawName.equals("publisher")){
						this.buffer = new StringBuffer();
						this.isPublisher = true;
					}
					else
						//language
						if(rawName.equalsIgnoreCase("language_name")){
							this.buffer = new StringBuffer();
							this.isLanguage = true;
						}
						else
							//author
							if(rawName.equalsIgnoreCase("author")){
								this.isCreator = true;
							} else
								if(rawName.equalsIgnoreCase("list-item") && this.isCreator){
									this.buffer = new StringBuffer();
									this.isCreatorListItem = true;
								}
								else
									//URLS
									if(rawName.equalsIgnoreCase("urls")){
										this.isUrl = true;
									} else
										if(rawName.equalsIgnoreCase("list-item") && this.isUrl){
											this.buffer = new StringBuffer();
											this.isUrlListItem = true;
										}
										else
											//country_focus
											if(rawName.equalsIgnoreCase("country_focus")){
												this.isCoverage = true;
											} else
												if(rawName.equalsIgnoreCase("list-item") && this.isCoverage){
													this.buffer = new StringBuffer();
													this.isCoverageListItem = true;
												}
												else
													//KEYWORDS
													if(rawName.equalsIgnoreCase("category_theme_path") || rawName.equalsIgnoreCase("keyword")){
														this.isKeyword = true;
													} else
														if(rawName.equalsIgnoreCase("list-item") && this.isKeyword){
															this.buffer = new StringBuffer();
															this.isKeywordListItem = true;
														}
														else
															//note
															if(rawName.equalsIgnoreCase("headline")){
																this.buffer = new StringBuffer();
																this.isNote = true;
															}
															else
																//description
																if(rawName.equalsIgnoreCase("description")){
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
			if(this.isTitle || this.isDate || this.isUrlListItem || this.isKeywordListItem || this.isPublisher
					|| this.isLanguage || this.isNote || this.isAbstract || this.isCreatorListItem || this.isCoverageListItem){
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

		//reset parameters
		if(rawName.equalsIgnoreCase("list-item")){
			//if end record
			if(listitem_lev==0){
				if(!discard_rec){
					//mandatory fields
					if(this.current.getTitle2language().size()>0 && this.current.getDateIssued()!=null)
						this.records.add(current);
				}
				this.current = null;
				this.discard_rec = false;
			}
			//decrease
			this.listitem_lev--;
		}

		// not put "else": list-item still needs to be checked
		if(this.current!=null && !discard_rec) {
			// TITLE
			if(rawName.equalsIgnoreCase("title") && this.isTitle){
				this.isTitle = false;
				String term = new String(this.buffer);
				if(term!=null && !term.trim().equals("")){
					//search Solr to see if title exists
					int occurrences = this.indexChecker.checkTitle(term);			
					if(occurrences==0) {
						term = EscapeCommonXML.getInstance().removeHTMLTagsAndUnescape(term);
						this.current.addTitle(term, "eng");
					}
					else {
						this.discardRecord();
					}
				}
			}
			else 
				//date
				if(rawName.equalsIgnoreCase("publication_year") && this.isDate){
					String term = new String(this.buffer);
					this.isDate = false;
					if(term!=null && !term.trim().equals("")){
						//clean
						String[] dates = term.split(" ");
						if(dates.length>0)
							this.current.setDateIssued(dates[0]);
					}
				}
				else 
					//publisher: not ignore CASE
					if(rawName.equals("publisher") && this.isPublisher){
						String term = new String(this.buffer);
						this.isPublisher = false;
						if(term!=null && !term.trim().equals("")){
							if(!term.equalsIgnoreCase("Food and Agriculture Organization of the United Nations"))
								this.current.addPublisherName(term);
						}
					}
					else 
						//language
						if(rawName.equalsIgnoreCase("language_name") && this.isLanguage){
							String term = new String(this.buffer);
							this.isLanguage = false;
							if(term!=null && !term.trim().equals("")){
								this.current.addLanguage(term, "");
							}
						}
						else
							//URLS
							if(rawName.equalsIgnoreCase("urls")){
								this.isUrl = false;
							}
							else
								if(rawName.equalsIgnoreCase("list-item") && this.isUrl && this.isUrlListItem){
									this.isUrlListItem = false;
									String term = new String(this.buffer);
									if(term!=null && !term.trim().equals("")){
										this.current.addIdentifier(term, "dcterms:URI");
										//check autotagger keys
										List<String> keys = this.autotagger.get(term);
										if(keys!=null){
											for(String s: keys){
												this.current.addAgrovoc(s, this.attr);
											}
										}
									}
								}
								else
									//creator
									if(rawName.equalsIgnoreCase("author")){
										this.isCreator = false;
									}
									else
										if(rawName.equalsIgnoreCase("list-item") && this.isCreator && this.isCreatorListItem){
											this.isCreatorListItem = false;
											String term = new String(this.buffer);
											if(term!=null && !term.trim().equals("")){
												this.current.addCreatorPersonal(term);
											}
										}
										else 
											//keywords
											if(rawName.equalsIgnoreCase("category_theme_path") || rawName.equalsIgnoreCase("keyword")){
												this.isKeyword = false;
											}
											else
												if(rawName.equalsIgnoreCase("list-item") && this.isKeyword && this.isKeywordListItem){
													this.isKeywordListItem = false;
													String term = new String(this.buffer);
													if(term!=null && !term.trim().equals("") && !term.equalsIgnoreCase("tfmimport") || !term.contains(":")){
														this.current.addFreeSubject(term);
													}
												}
												else 
													//note
													if(rawName.equalsIgnoreCase("headline") && this.isNote){
														String term = new String(this.buffer);
														this.isNote = false;
														if(term!=null && !term.trim().equals("")){
															this.current.setDescrNotes(term);
														}
													}
													else 
														//description
														if(rawName.equalsIgnoreCase("description") && this.isAbstract){
															String term = new String(this.buffer);
															this.isAbstract = false;
															if(term!=null && !term.trim().equals("")){
																//remove img
																int indexImg = term.indexOf("<img");
																if(indexImg>0)
																	term = term.substring(0, indexImg);	
																term = EscapeCommonXML.getInstance().removeHTMLTagsAndUnescape(term);
																this.current.addAbstract(term, "eng");
															}
														}
														else
															if(rawName.equalsIgnoreCase("list-item") && this.isCoverage && this.isCoverageListItem){
																this.isCoverageListItem = false;
																String term = new String(this.buffer);
																if(term!=null && !term.trim().equals("")){
																	this.current.addSpatial(term);
																}
															}
															else
																//country_focus 
																if(rawName.equalsIgnoreCase("country_focus")){
																	this.isCoverage = false;
																}
		}
	}

	/*
	 * Set record as to be discarded
	 */
	private void discardRecord(){
		this.current = null;
		this.discard_rec = true;
	}

}
