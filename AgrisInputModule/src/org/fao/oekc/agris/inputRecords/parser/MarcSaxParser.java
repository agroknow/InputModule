package org.fao.oekc.agris.inputRecords.parser;

import java.util.List;

import jfcutils.util.StringUtils;

import org.fao.oekc.agris.inputRecords.dom.AGRISAttributes;
import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.CheckIndex;
import org.fao.oekc.agris.inputRecords.util.EscapeXML;
import org.fao.oekc.agris.inputRecords.util.IssnCleaner;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Marc XML parser (Vikii)
 * @author celli
 *
 */
public class MarcSaxParser extends DefaultHandler {

	//parser
	private List<AgrisApDoc> records;
	private AgrisApDoc current;

	//index checker
	private CheckIndex indexChecker;

	//data
	private boolean isTitle;
	private boolean isCreatorPersonal;
	private boolean isAbstract;
	private boolean isLanguage;
	private boolean publisherName;
	private boolean isJournalTitle;
	private boolean isISSN;
	private boolean isDate;
	private boolean isIdentifier;
	private boolean isJournalNumber;
	private boolean isSubject;

	private boolean isContent;

	//to read the entire content
	private StringBuffer buffer;
	private String subfieldCode;
	private String tmpBuffer;

	public MarcSaxParser(List<AgrisApDoc> records) {
		try {
			this.indexChecker = new CheckIndex();
			this.records = records;
			this.isCreatorPersonal = false;
			this.isIdentifier = false;
			this.isTitle = false;
			this.isAbstract = false;
			this.publisherName = false;
			this.isISSN = false;
			this.isDate = false;
			this.isLanguage = false;
			this.isJournalTitle = false;
			this.isJournalNumber = false;
			this.isContent = false;
			this.isSubject = false;
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
		if(rawName.equals("record")){
			this.current = new AgrisApDoc();
		}
		else
			if(this.current!=null) {
				//datafield to detect the entiry
				if(rawName.equals("datafield")){
					String tag = atts.getValue("tag");
					//map fields
					if(tag.equals("245"))
						//title
						this.isTitle = true;
					else if(tag.equals("700") || tag.equals("100"))
						//authors
						this.isCreatorPersonal = true;
					else if(tag.equals("041"))
						//language
						this.isLanguage = true;
					else if(tag.equals("773")){
						//journal
						this.isJournalTitle = true;
						this.isISSN = true;
						this.isJournalNumber = true;
					}
					else if(tag.equals("520"))
						//abstract
						this.isAbstract = true;
					else if(tag.equals("260")) {
						//publisher name and date
						this.publisherName = true;
						this.isDate = true;
					}
					else if(tag.equals("856"))
						//identifier
						this.isIdentifier = true;
					else if(tag.equals("650"))
						//subject
						this.isSubject = true;
				}
				else
					//value
					if(rawName.equalsIgnoreCase("subfield")){
						this.subfieldCode = atts.getValue("code");
						//map content
						if((this.subfieldCode.equals("a") && (this.isTitle || this.isCreatorPersonal || this.isLanguage || this.isAbstract || this.isSubject))
								|| (this.subfieldCode.equals("t") && (this.isJournalTitle))
								|| (this.subfieldCode.equals("b") && (this.publisherName))
								|| (this.subfieldCode.equals("c") && (this.isDate))
								|| (this.subfieldCode.equals("u") && (this.isIdentifier))
								|| (this.subfieldCode.equals("g") && (this.isJournalNumber))
								|| (this.subfieldCode.equals("x") && (this.isISSN))
								|| (this.subfieldCode.equals("2") && (this.isSubject))){
							this.buffer = new StringBuffer();
							this.isContent = true;
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
			if(this.isContent && (this.isCreatorPersonal || this.isIdentifier|| this.isTitle || this.isAbstract || this.isLanguage || this.publisherName
					|| this.isJournalTitle || this.isISSN || this.isDate || this.isJournalNumber || this.isSubject)){
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
			if(rawName.equals("record")){
				//mandatory fields
				if(this.current.getTitle2language().size()>0)
					this.records.add(current);
			}
			else 
				//subfield
				if(rawName.equalsIgnoreCase("subfield") && this.isContent){
					this.isContent  = false;
					String term = new String(this.buffer);
					
					if(term!=null && !term.trim().equals("")){
						//case: title in English
						if(this.isTitle) {
							this.isTitle = false;
							//clean title
							if(term.endsWith(":") && term.length()>1){
								term = term.substring(0, term.length()-1);
								term = (new StringUtils()).trimRight(term);
							}
							//search Solr to see if title exists
							int occurrences = this.indexChecker.checkTitle(term);	
							if(occurrences==0) {
								term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
								this.current.addTitle(term, "en");
							}
							else 
								this.current = null;
						}
						//creator personal
						else if(this.isCreatorPersonal){
							this.isCreatorPersonal = false;
							if(term.endsWith(".") && term.length()>1)
								term = term.substring(0, term.length()-1);
							this.current.addCreatorPersonal(term);
						}
						//language
						else if(this.isLanguage){
							this.isLanguage = false;
							this.current.addLanguage(term, "dcterms:ISO639-2");
						}
						//ciatation title
						else if(this.isJournalTitle && this.subfieldCode.equals("t")){
							this.isJournalTitle = false;
							//clean title
							if(term.length()>2 && term.endsWith("-")){
								term = term.substring(0, term.length()-1);
								term = (new StringUtils()).trimRight(term);
							}
							this.current.setCitationTitle(term);
						}
						//abstract: English
						else if(this.isAbstract){
							this.isAbstract = false;
							term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(term);
							this.current.addAbstract(term, "en");
						}
						//publisher name
						else if(this.publisherName && this.subfieldCode.equals("b")){
							this.publisherName = false;
							this.current.addPublisherName(term);
						}
						//date
						else if(this.isDate && this.subfieldCode.equals("c")){
							this.isDate = false;
							this.current.setDateIssued(term);
						}
						//identifier
						else if(this.isIdentifier){
							this.isIdentifier = false;
							this.current.addIdentifier(term, "dcterms:URI");
						}
						//eissn
						else if(this.isISSN && this.subfieldCode.equals("x")){
							this.isISSN = false;
							//clean the string 
							if(term.length()>3 && term.endsWith(". -")){
								term = term.substring(0, term.length()-3);
								term = (new StringUtils()).trimRight(term);
							}
							//naive issn cleaning
							if(!IssnCleaner.isISSN(term))
								term = IssnCleaner.cleanISSN(term);
							if(IssnCleaner.isISSN(term))
								this.current.addEissn(term);
						}
						//journal number, volume
						else if(this.isJournalNumber && this.subfieldCode.equals("g")){
							this.isJournalNumber = false;
							//split format: <<21 (2012) : 4, s. 325-331>>
							//issue (year) space : space volume, s. pages  
							String[] split = term.split(" : ");
							if(split.length==2){
								String left = split[0];	//issue (year)
								String right = split[1];	//volume, s. pages
								String tmp = "";
								//date
								int indexParent = left.indexOf("(");
								if(indexParent!=-1){
									String date = left.substring(indexParent+1, left.length()-1);
									if(this.current.getDateIssued()==null)
										this.current.setDateIssued(date);
									//issue
									tmp = left.substring(0, indexParent);
								}
								//pages
								int indexPages = right.indexOf("s. ");
								if(indexPages!=-1 && (right.length()>indexPages+3))
									this.current.setExtent("p. "+right.substring(indexPages+3));
								//volume
								int indexVol = right.indexOf(",");
								if(indexVol!=-1){
									String vol = "v. "+right.substring(0, indexVol);
									if(tmp.length()>0)
										vol = vol + " ("+tmp + ")";
									this.current.setCitationNumber(vol);
								}
							}
						}
						//subject
						else if(this.isSubject){
							//read the string
							if(this.subfieldCode.equals("a"))
								this.tmpBuffer = term;
							//condition
							else if(this.subfieldCode.equals("2")){
								if(term.equalsIgnoreCase("agrovoc") && this.tmpBuffer!=null){
									this.isSubject = false;
									this.current.addAgrovoc(this.tmpBuffer, new AGRISAttributes("ags:AGROVOC","eng"));
									this.tmpBuffer = null;
								}
							}
						}
					}
				}
		}
	}


}
