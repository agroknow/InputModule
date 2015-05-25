package org.fao.oekc.agris.inputRecords.customizations;

import jfcutils.util.StringUtils;

import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;

/**
 * Define customizations in DC data
 * @author celli
 *
 */
public class DCCustomizations implements Customization{

	//to identify the center
	private String countrycode;
	private String centercode;
	private StringUtils cleaner;

	public DCCustomizations(String arnPrefix){
		cleaner = new StringUtils();
		if(arnPrefix!=null && arnPrefix.length()>=2){
			this.countrycode=arnPrefix.substring(0, 2).toUpperCase();
			if(arnPrefix.length()==7)
				this.centercode=arnPrefix.substring(6);
		}
		else{
			this.countrycode = "";
			this.centercode= "";
		}
	}

	/**
	 * Exceptions for dc:description
	 * @param currentDoc
	 * @param astratto
	 * @return true if the exception has been applied
	 */
	public boolean abstractException(AgrisApDoc currentDoc, String astratto) {
		//DFID
		if(this.countrycode.equals("GB")){
			//check length > 20
			if(astratto.length()>20){
				//split for different languages
				String[] abstracts = astratto.split("ZUSAMMENFASSUNG");
				for(String s: abstracts)
					currentDoc.addAbstract(s, "");
			}
			return true;
		}
		//AVANO
		if(this.countrycode.equals("AV")){
			if(astratto.length()>=50){
				astratto = astratto.replaceAll("\n", "");
				astratto = astratto.replaceAll("\r", "");
				astratto = astratto.replaceAll("\t", "");
				currentDoc.addAbstract(astratto, "");
			}
			else
				currentDoc.setDescrNotes(astratto);
			return true;
		}
		return false;
	}

	/**
	 * Exceptions for dc:date
	 * @param currentDoc
	 * @param date
	 * @return true if the exception has been applied
	 */
	public boolean dateException(AgrisApDoc currentDoc, String date) {
		//DFID
		if(this.countrycode.equals("GB")){
			//naive cleaning
			if(date.startsWith("(") && date.endsWith(")"))
				date = date.substring(1, date.length()-1);
			//chenge format from 2006-01-17T11:02:00 to 2006-01-17
			if(date.contains("T"))
				date = date.substring(0, date.indexOf("T"));
			currentDoc.setDateIssued(date);
			return true;
		}
		//AVANO
		if(this.countrycode.equals("AV")){
			if(!date.endsWith("Z") && !date.endsWith("z"))
				currentDoc.setDateIssued(date);
			return true;
		}
		return false;
	}
	
	/**
	 * Exceptions for dc:identifier
	 * @param currentDoc
	 * @param id
	 * @return true if the exception has been applied
	 */
	public boolean identifierException(AgrisApDoc currentDoc, String id) {
		//DFID
		if(this.countrycode.equals("GB")){
			//consider only PDF for DFID
			if(id.contains("dfid.gov.uk") && !id.toUpperCase().contains("PDF"))
				id = null;
			//discard if it is a simple number
			if(id!=null && !cleaner.isInteger(id))
				currentDoc.addIdentifier(id, "dcterms:URI");
			return true;
		}
		//AVANO
		if(this.countrycode.equals("AV")){
			if(id.toLowerCase().endsWith("pdf"))
				currentDoc.addIdentifier(id, "dcterms:URI");
			else if(id.startsWith("URN:ISBN:") && id.length()>9)
				currentDoc.addIdentifier(id.substring(9), "ags:ISBN");
			else if(id.startsWith("ISSN:") && id.length()>5)
				currentDoc.addIssn(id.substring(5));
			else if(id.startsWith("oai:"))
				return true;
			else if(id.contains("handle"))
				currentDoc.setIsReferencedBy(id);
			else
				currentDoc.addIdentifier(id, null);
			return true;
		}
		return false;
	}
	
	/**
	 * Exceptions for dc:relation
	 * @param currentDoc
	 * @param rel
	 * @return true if the exception has been applied
	 */
	public boolean relationException(AgrisApDoc currentDoc, String rel) {
		//DFID
		if(this.countrycode.equals("GB")){
			if(rel.contains("dfid.gov.uk"))
				rel = rel + "Default.aspx";
			currentDoc.addIsPartOfRelation(rel, "dcterms:URI");
			return true;
		}
		//AGECON
		if(this.countrycode.equals("US")){
			currentDoc.setSource(rel);
			return true;
		}
		//AVANO
		if(this.countrycode.equals("AV")){
			if(rel.startsWith("http") && rel.endsWith("pdf"))
				currentDoc.addIdentifier(rel, "dcterms:URI");
			else
				if(rel.startsWith("http"))
					currentDoc.addIsPartOfRelation(rel, "dcterms:URI");
			return true;
		}
		return false;
	}
	
	/**
	 * Exceptions for dc:format
	 * @param currentDoc
	 * @param format
	 * @return true if the exception has been applied
	 */
	public boolean formatException(AgrisApDoc currentDoc, String format) {
		//DFID
		if(this.countrycode.equals("GB")){
			if(cleaner.isInteger(format))
				currentDoc.setExtent("p."+format);
			else
				currentDoc.setFormat(format);
			return true;
		}
		return false;
	}
	
	/**
	 * Exceptions for dc:language
	 * @param currentDoc
	 * @param lang
	 * @return true if the exception has been applied
	 */
	public boolean languageException(AgrisApDoc currentDoc, String lang) {
		//AGECON
		if(this.countrycode.equals("US")){
			//only 2 chars language, AgECON
			if(lang.length()==2)
				currentDoc.addLanguage(lang, "dcterms:ISO639-2");
			return true;
		}
		return false;
	}
	
	/**
	 * Exceptions for dc:coverage
	 * @param currentDoc
	 * @param cover
	 * @return true if the exception has been applied
	 */
	public boolean coverageException(AgrisApDoc currentDoc, String cover) {
		//AVANO
		if(this.countrycode.equals("AV")){
			currentDoc.addSpatialSimple(cover);
			return true;
		}
		return false;
	}
	
	/**
	 * Exceptions for dc:source
	 * @param currentDoc
	 * @param source
	 * @return true if the exception has been applied
	 */
	public boolean sourceException(AgrisApDoc currentDoc, String source) {
		//AVANO
		if(this.countrycode.equals("AV")){
			if(!source.startsWith("http"))
				currentDoc.setSource(source);
			return true;
		}
		return false;
	}
	
	/**
	 * Say if FAO records should be excluded
	 * @param term
	 * @return true if the FAO record has to be excluded
	 */
	public boolean hasFAOtoBeExcluded(String term) {
		//AVANO
		if(this.countrycode.equals("AV") && term.startsWith("FAO"))
			return true;
		return false;
	}

	/*
	 * GETTER/SETTER
	 */
	public String getCountrycode() {
		return countrycode;
	}

	public void setCountrycode(String countrycode) {
		this.countrycode = countrycode;
	}

	public String getCentercode() {
		return centercode;
	}

	public void setCentercode(String centercode) {
		this.centercode = centercode;
	}

}
