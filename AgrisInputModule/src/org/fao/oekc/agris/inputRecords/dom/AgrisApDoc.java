package org.fao.oekc.agris.inputRecords.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jfcutils.util.StringUtils;

/**
 * This object contains all information of an AGRIS AP record
 * @author celli
 *
 */
public class AgrisApDoc {

	private String ARN;
	private String oldArn; 
	private Map<String, String> title2language;
	private Map<String, String> alternativeTitles;
	private List<String> supplementTitles;
	private Map<String, String> abstract2language;
	private Map<String, String> identifier2schema;
	private Map<String, String> language2schema;
	private Map<String, String> relation2schema;
	private List<String> creatorGeneral;
	private List<String> creatorPersonal;
	private List<String> creatorCorporate;
	private List<String> creatorConference;
	private List<String> coverage;
	private List<String> publisherName;
	private List<String> publisherPlace;
	private Set<String> freeSubject;
	private Map<String, String> subjclass2schema;
	private Map<String, AGRISAttributes> agrovoc2atts;
	private String dateIssued;
	private String extent;
	private List<String> type;
	private String format;
	private String source;
	private String descrNotes;
	private String citationTitle;
	private String citationNumber;
	private String citationChronology;
	private String isReferencedBy;
	private String rights;
	private List<String> spatialAgrovoc;
	private List<String> spatialSimple;
	private List<String> issns;
	private List<String> eissns;

	//---------------------------------
	// Constructor
	//---------------------------------
	public AgrisApDoc() {
		this.title2language = new HashMap<String, String>();
		this.abstract2language = new HashMap<String, String>();
		this.identifier2schema = new HashMap<String, String>();
		this.language2schema = new HashMap<String, String>();
		this.subjclass2schema = new HashMap<String, String>();
		this.relation2schema = new HashMap<String, String>();
		this.agrovoc2atts = new HashMap<String, AGRISAttributes>();
		this.creatorPersonal = new ArrayList<String>();
		this.creatorCorporate = new ArrayList<String>();
		this.creatorConference = new ArrayList<String>();
		this.creatorGeneral = new ArrayList<String>();
		this.publisherName = new ArrayList<String>();
		this.publisherPlace = new ArrayList<String>();
		this.freeSubject = new HashSet<String>();
		this.coverage = new ArrayList<String>();
		this.issns = new ArrayList<String>();
		this.type = new ArrayList<String>();
		this.spatialAgrovoc = new ArrayList<String>();
		this.spatialSimple = new ArrayList<String>();
		this.eissns = new ArrayList<String>();
		this.alternativeTitles = new HashMap<String, String>();
		this.supplementTitles = new ArrayList<String>();
	}

	//---------------------------------
	// for lists
	//---------------------------------
	public void addTitle(String title, String language){
		if(language==null)
			language = "";
		if(title!=null)
			this.title2language.put(title, language);
	}
	
	public void addAlternative(String title, String language){
		if(language==null)
			language = "";
		if(title!=null)
			this.alternativeTitles.put(title, language);
	}
	
	public void addSupplement(String title){
		this.supplementTitles.add(title);
	}
	
	public void addCreatorPersonal(String creator){
		this.creatorPersonal.add(creator);
	}
	
	public void addCreatorGeneral(String creator){
		this.creatorGeneral.add(creator);
	}

	public void addCreatorCorporate(String creator){
		this.creatorCorporate.add(creator);
	}
	
	public void addCreatorConference(String creator){
		this.creatorConference.add(creator);
	}

	public void addPublisherName(String publisherName){
		this.publisherName.add(publisherName);
	}
	
	public void addCoverage(String coverage){
		this.coverage.add(coverage);
	}
	
	public void addType(String type){
		StringUtils cleaner = new StringUtils();
		type = cleaner.trimLeft(type);
		type = cleaner.onlyCharacterString(type);
		this.type.add(type);
	}
	
	public void addPublisherPlace(String publisherPlace){
		this.publisherPlace.add(publisherPlace);
	}

	public void addFreeSubject(String freeSubject){
		this.freeSubject.add(freeSubject);
	}

	public void addIssn(String issn){
		this.issns.add(issn);
	}

	public void addEissn(String eissn){
		this.eissns.add(eissn);
	}
	
	public void addAgrovoc(String term, AGRISAttributes atts){
		if(term!=null && atts!=null)
			this.agrovoc2atts.put(term, atts);
	}
	
	public void addAbstract(String astratto, String language){
		if(language==null)
			language = "";
		if(astratto!=null)
			this.abstract2language.put(astratto, language);
	}
	/**
	 * this.current.addIdentifier(term, "dcterms:URI");
	 * this.current.addIdentifier(term, "ags:ISBN");
	 * @param identifier
	 * @param schema
	 */
	public void addIdentifier(String identifier, String schema){
		if(schema==null)
			schema = "";
		if(identifier!=null)
			this.identifier2schema.put(identifier, schema);
	}
	
	public void addLanguage(String language, String schema){
		if(schema==null)
			schema = "";
		if(language!=null)
			this.language2schema.put(language, schema);
	}
	
	public void addIsPartOfRelation(String relation, String schema){
		if(schema==null)
			schema = "dcterms:URI";
		if(relation!=null)
			this.relation2schema.put(relation, schema);
	}
	
	public void addSubjClass(String subject, String schema){
		if(schema==null)
			schema = "";
		if(subject!=null)
			this.subjclass2schema.put(subject, schema);
	}
	
	
	public void addSpatial(String spatial){
		this.spatialAgrovoc.add(spatial);
	}
	
	public void addSpatialSimple(String spatial){
		this.spatialSimple.add(spatial);
	}

	//---------------------------------
	// setters/getters
	//---------------------------------
	public String getARN() {
		return ARN;
	}

	public void setARN(String aRN) {
		ARN = aRN;
	}

	public Map<String, String> getTitle2language() {
		return title2language;
	}

	public void setTitle2language(Map<String, String> title2language) {
		this.title2language = title2language;
	}

	public Map<String, String> getAbstract2language() {
		return abstract2language;
	}

	public void setAbstract2language(Map<String, String> abstract2language) {
		this.abstract2language = abstract2language;
	}

	public Map<String, String> getIdentifier2schema() {
		return identifier2schema;
	}

	public void setIdentifier2schema(Map<String, String> identifier2schema) {
		this.identifier2schema = identifier2schema;
	}

	public List<String> getCreatorPersonal() {
		return creatorPersonal;
	}

	public void setCreatorPersonal(List<String> creatorPersonal) {
		this.creatorPersonal = creatorPersonal;
	}

	public List<String> getCreatorGeneral() {
		return creatorGeneral;
	}

	public void setCreatorGeneral(List<String> creatorGeneral) {
		this.creatorGeneral = creatorGeneral;
	}

	public List<String> getCreatorCorporate() {
		return creatorCorporate;
	}

	public void setCreatorCorporate(List<String> creatorCorporate) {
		this.creatorCorporate = creatorCorporate;
	}

	public List<String> getSpatial() {
		return spatialAgrovoc;
	}

	public void setSpatial(List<String> spatial) {
		this.spatialAgrovoc = spatial;
	}

	public List<String> getSpatialSimple() {
		return spatialSimple;
	}

	public void setSpatialSimple(List<String> spatialSimple) {
		this.spatialSimple = spatialSimple;
	}

	public List<String> getPublisherName() {
		return publisherName;
	}

	public void setPublisherName(List<String> publisherName) {
		this.publisherName = publisherName;
	}

	public Set<String> getFreeSubject() {
		return freeSubject;
	}

	public void setFreeSubject(Set<String> freeSubject) {
		this.freeSubject = freeSubject;
	}

	public String getDateIssued() {
		return dateIssued;
	}

	public void setDateIssued(String dateIssued) {
		this.dateIssued = dateIssued;
	}

	public String getExtent() {
		return extent;
	}

	public void setExtent(String extent) {
		this.extent = extent;
	}

	public String getCitationTitle() {
		return citationTitle;
	}

	public void setCitationTitle(String citationTitle) {
		this.citationTitle = citationTitle;
	}

	public String getCitationNumber() {
		return citationNumber;
	}

	public void setCitationNumber(String citationNumber) {
		this.citationNumber = citationNumber;
	}

	public List<String> getIssns() {
		return issns;
	}

	public void setIssns(List<String> issns) {
		this.issns = issns;
	}

	public List<String> getEissns() {
		return eissns;
	}

	public void setEissns(List<String> eissns) {
		this.eissns = eissns;
	}

	public String getOldArn() {
		return oldArn;
	}

	public void setOldArn(String oldArn) {
		this.oldArn = oldArn;
	}

	public Map<String, AGRISAttributes> getAgrovoc2atts() {
		return agrovoc2atts;
	}

	public void setAgrovoc2atts(Map<String, AGRISAttributes> agrovoc2atts) {
		this.agrovoc2atts = agrovoc2atts;
	}

	public Map<String, String> getLanguage2schema() {
		return language2schema;
	}

	public void setLanguage2schema(Map<String, String> language2schema) {
		this.language2schema = language2schema;
	}

	public List<String> getCreatorConference() {
		return creatorConference;
	}

	public void setCreatorConference(List<String> creatorConference) {
		this.creatorConference = creatorConference;
	}

	public Map<String, String> getSubjclass2schema() {
		return subjclass2schema;
	}

	public void setSubjclass2schema(Map<String, String> subjclass2schema) {
		this.subjclass2schema = subjclass2schema;
	}

	public String getCitationChronology() {
		return citationChronology;
	}

	public void setCitationChronology(String citationChronology) {
		this.citationChronology = citationChronology;
	}

	public List<String> getSupplementTitles() {
		return supplementTitles;
	}

	public void setSupplementTitles(List<String> supplementTitles) {
		this.supplementTitles = supplementTitles;
	}

	public List<String> getPublisherPlace() {
		return publisherPlace;
	}

	public void setPublisherPlace(List<String> publisherPlace) {
		this.publisherPlace = publisherPlace;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDescrNotes() {
		return descrNotes;
	}

	public void setDescrNotes(String descrNotes) {
		this.descrNotes = descrNotes;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public List<String> getCoverage() {
		return coverage;
	}

	public void setCoverage(List<String> coverage) {
		this.coverage = coverage;
	}

	public Map<String, String> getRelation2schema() {
		return relation2schema;
	}

	public void setRelation2schema(Map<String, String> relation2schema) {
		this.relation2schema = relation2schema;
	}

	public List<String> getType() {
		return type;
	}

	public void setType(List<String> type) {
		this.type = type;
	}

	public Map<String, String> getAlternativeTitles() {
		return alternativeTitles;
	}

	public void setAlternativeTitles(Map<String, String> alternativeTitles) {
		this.alternativeTitles = alternativeTitles;
	}

	public String getIsReferencedBy() {
		return isReferencedBy;
	}

	public void setIsReferencedBy(String isReferencedBy) {
		this.isReferencedBy = isReferencedBy;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

	
}
