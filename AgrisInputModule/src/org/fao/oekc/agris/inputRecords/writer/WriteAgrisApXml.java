package org.fao.oekc.agris.inputRecords.writer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import jfcutils.util.StringUtils;

import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.ArnManager;

/**
 * Write a single XML file as AGRIS AP, given a list of AgrisApDoc
 * @author celli
 *
 */
public class WriteAgrisApXml {

	private String outputDirPath;
	private List<AgrisApDoc> records;
	private String fileName;
	private int startArnId;
	private String arnPrefix;

	/**
	 * Write a single XML file as AGRIS AP, given a list of AgrisApDoc
	 * @param outputDirPath the path of the output dir
	 * @param fileName the name of the file
	 * @param records Agris AP records to be written
	 */
	public WriteAgrisApXml(String outputDirPath, String fileName, List<AgrisApDoc> records, String arnPrefix, int startArnId){
		this.outputDirPath = outputDirPath;
		this.records = records;
		this.fileName = fileName;
		this.startArnId = startArnId;
		this.arnPrefix = arnPrefix;
	}

	/**
	 * Write a single XML file as AGRIS AP, given a list of AgrisApDoc
	 * @throws IOException 
	 */
	public void writeRecords() throws IOException{
		if(records!=null && records.size()>0){
			//file writer
			BufferedWriter out = new BufferedWriter(new FileWriter(outputDirPath+"/"+fileName));
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.newLine();
			out.write("<!DOCTYPE ags:resources SYSTEM \"http://purl.org/agmes/agrisap/dtd/\">");
			out.newLine();
			out.write("<ags:resources xmlns:ags=\"http://purl.org/agmes/1.1/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:agls=\"http://www.naa.gov.au/recordkeeping/gov_online/agls/1.2\" xmlns:dcterms=\"http://purl.org/dc/terms/\">");
			out.newLine();

			//scan records
			for(AgrisApDoc rec: records){
				//if there is no ARN, create it!
				if(rec.getARN()==null || rec.getARN().length()==0){
					int numberOfBitsForCounter = (new ArnManager()).getNumberOfBitsForCounter(arnPrefix);
					String arn = arnPrefix+(new StringUtils()).formatInteger(startArnId,numberOfBitsForCounter);
					startArnId++;
					rec.setARN(arn);
				}
				out.write("<ags:resource ags:ARN=\""+rec.getARN()+"\">");
				out.newLine();

				//title
				if(rec.getTitle2language().keySet().size()>0){
					for(String title: rec.getTitle2language().keySet()){
						String lang = rec.getTitle2language().get(title);
						if(lang!=null && lang.length()>0)
							out.write("\t<dc:title xml:lang=\""+lang.toLowerCase()+"\"><![CDATA["+title+"]]></dc:title>");
						else
							out.write("\t<dc:title><![CDATA["+title+"]]></dc:title>");
						out.newLine();
					}
				}

				//alternative
				if(rec.getAlternativeTitles().keySet().size()>0){
					out.write("\t<dc:title>");
					out.newLine();
					for(String title: rec.getAlternativeTitles().keySet()){
						String lang = rec.getAlternativeTitles().get(title);
						if(lang!=null && lang.length()>0)
							out.write("\t\t<dcterms:alternative xml:lang=\""+lang.toLowerCase()+"\"><![CDATA["+title+"]]></dcterms:alternative>");
						else
							out.write("\t\t<dcterms:alternative><![CDATA["+title+"]]></dcterms:alternative>");
						out.newLine();
					}
					out.write("\t</dc:title>");
					out.newLine();
					
				}

				//supplement
				if(rec.getSupplementTitles().size()>0){
					out.write("\t<dc:title>");
					out.newLine();
					for(String a: rec.getSupplementTitles()){
						out.write("\t\t<ags:titleSupplement><![CDATA["+a+"]]></ags:titleSupplement>");
						out.newLine();
					}
					out.write("\t</dc:title>");
					out.newLine();
				}

				//creator personal
				if(rec.getCreatorPersonal().size()>0){
					out.write("\t<dc:creator>");
					out.newLine();
					for(String a: rec.getCreatorPersonal()){
						out.write("\t\t<ags:creatorPersonal><![CDATA["+a+"]]></ags:creatorPersonal>");
						out.newLine();
					}
					//if no corporate and no conference, close dc:creator
					if(rec.getCreatorCorporate().size()==0 && rec.getCreatorConference().size()==0){
						out.write("\t</dc:creator>");
						out.newLine();
					}
				}

				//creator general
				if(rec.getCreatorGeneral().size()>0){
					for(String a: rec.getCreatorGeneral()){
						out.write("\t<dc:creator><![CDATA["+a+"]]></dc:creator>");
						out.newLine();
					}
				}

				//creator corporate
				if(rec.getCreatorCorporate().size()>0){
					if(rec.getCreatorPersonal().size()==0){
						out.write("\t<dc:creator>");
						out.newLine();
					}
					for(String a: rec.getCreatorCorporate()){
						out.write("\t\t<ags:creatorCorporate><![CDATA["+a+"]]></ags:creatorCorporate>");
						out.newLine();
					}
					//if no conference, close dc:creator
					if(rec.getCreatorConference().size()==0){
						out.write("\t</dc:creator>");
						out.newLine();
					}
				}

				//creator conference
				if(rec.getCreatorConference().size()>0){
					if(rec.getCreatorPersonal().size()==0 && rec.getCreatorCorporate().size()==0){
						out.write("\t<dc:creator>");
						out.newLine();
					}
					for(String a: rec.getCreatorConference()){
						out.write("\t\t<ags:creatorConference><![CDATA["+a+"]]></ags:creatorConference>");
						out.newLine();
					}
					out.write("\t</dc:creator>");
					out.newLine();
				}

				//publisher
				if(rec.getPublisherName().size()>0 || rec.getPublisherPlace().size()>0){
					out.write("\t<dc:publisher>");
					out.newLine();
					if(rec.getPublisherName().size()>0) {
						for(String name: rec.getPublisherName()){
							out.write("\t\t<ags:publisherName><![CDATA["+name+"]]></ags:publisherName>");
							out.newLine();
						}
					}
					if(rec.getPublisherPlace().size()>0) {
						for(String place: rec.getPublisherPlace()){
							out.write("\t\t<ags:publisherPlace><![CDATA["+place+"]]></ags:publisherPlace>");
							out.newLine();
						}
					}
					out.write("\t</dc:publisher>");
					out.newLine();
				}

				//date
				if(rec.getDateIssued()!=null && rec.getDateIssued().length()>0){
					out.write("\t<dc:date><dcterms:dateIssued><![CDATA["+rec.getDateIssued()+"]]></dcterms:dateIssued></dc:date>");
					out.newLine();
				}

				//ASC
				if(rec.getSubjclass2schema().keySet().size()>0){
					out.write("\t<dc:subject>");
					out.newLine();
					for(String asc: rec.getSubjclass2schema().keySet()){
						String schema = rec.getSubjclass2schema().get(asc);
						if(schema!=null && schema.length()>0)
							out.write("\t\t<ags:subjectClassification scheme=\""+schema+"\"><![CDATA["+asc+"]]></ags:subjectClassification>");
						else
							out.write("\t\t<ags:subjectClassification><![CDATA["+asc+"]]></ags:subjectClassification>");
						out.newLine();
					}
					out.write("\t</dc:subject>");
					out.newLine();
				}

				//subjects
				if(rec.getFreeSubject()!=null && rec.getFreeSubject().size()>0){
					for(String s: rec.getFreeSubject()){
						out.write("\t<dc:subject><![CDATA["+s+"]]></dc:subject>");
						out.newLine();
					}
				}

				//agrovoc
				if(rec.getAgrovoc2atts().keySet().size()>0){
					out.write("\t<dc:subject>");
					out.newLine();
					for(String agr: rec.getAgrovoc2atts().keySet()){
						//no URIs
						if(!agr.startsWith("http")){
							String schema = rec.getAgrovoc2atts().get(agr).getScheme();
							String lang = rec.getAgrovoc2atts().get(agr).getLang();
							out.write("\t\t<ags:subjectThesaurus");
							if(lang!=null && lang.length()>0)
								out.write(" xml:lang=\""+lang+"\"");
							if(schema!=null && schema.length()>0)
								out.write(" scheme=\""+schema+"\"");
							out.write("><![CDATA["+agr+"]]></ags:subjectThesaurus>");
							out.newLine();
						}
					}
					out.write("\t</dc:subject>");
					out.newLine();
				}

				//description notes
				if(rec.getDescrNotes()!=null){
					out.write("\t<dc:description>");
					out.newLine();
					out.write("\t\t<ags:descriptionNotes><![CDATA["+rec.getDescrNotes()+"]]></ags:descriptionNotes>");
					out.write("</dc:description>");
					out.newLine();
				}

				//abstract
				if(rec.getAbstract2language().keySet().size()>0){
					out.write("\t<dc:description>");
					for(String abs: rec.getAbstract2language().keySet()){
						//remove existing CDATA section
						if(abs.contains("]]>") || abs.contains("<![CDATA[")){
							abs = abs.replaceAll("<!\\[CDATA\\[", "");
							abs = abs.replaceAll("]]>", "");
						}
						String lang = rec.getAbstract2language().get(abs);
						if(lang!=null && lang.length()>0)
							out.write("<dcterms:abstract xml:lang=\""+lang.toLowerCase()+"\"><![CDATA["+abs+"]]></dcterms:abstract>");
						else
							out.write("<dcterms:abstract><![CDATA["+abs+"]]></dcterms:abstract>");
						out.newLine();
					}
					out.write("\t</dc:description>");
					out.newLine();
				}

				//identifiers
				if(rec.getIdentifier2schema().keySet().size()>0){
					for(String id: rec.getIdentifier2schema().keySet()){
						String schema = rec.getIdentifier2schema().get(id);
						if(schema!=null && schema.length()>0)
							out.write("\t<dc:identifier scheme=\""+schema+"\"><![CDATA["+id+"]]></dc:identifier>");
						else
							out.write("\t<dc:identifier><![CDATA["+id+"]]></dc:identifier>");
						out.newLine();
					}
				}

				//type
				if(rec.getType()!=null && rec.getType().size()>0){
					for(String s: rec.getType()){
						out.write("\t<dc:type><![CDATA["+s+"]]></dc:type>");
						out.newLine();
					}
				}

				//format
				if(rec.getFormat()!=null && rec.getFormat().length()>0){
					out.write("\t<dc:format><dcterms:medium><![CDATA["+rec.getFormat()+"]]></dcterms:medium></dc:format>");
					out.newLine();
				}

				//extent
				if(rec.getExtent()!=null && rec.getExtent().length()>0){
					out.write("\t<dc:format><dcterms:extent><![CDATA["+rec.getExtent()+"]]></dcterms:extent></dc:format>");
					out.newLine();
				}

				//language
				if(rec.getLanguage2schema().keySet().size()>0){
					for(String lang: rec.getLanguage2schema().keySet()){
						String schema = rec.getLanguage2schema().get(lang);
						if(schema!=null && schema.length()>0)
							out.write("\t<dc:language scheme=\""+schema+"\"><![CDATA["+lang+"]]></dc:language>");
						else
							out.write("\t<dc:language><![CDATA["+lang+"]]></dc:language>");
						out.newLine();
					}
				}

				//relation
				if(rec.getRelation2schema().keySet().size()>0){
					for(String rel: rec.getRelation2schema().keySet()){
						String schema = rec.getRelation2schema().get(rel);
						out.write("\t<dc:relation><dcterms:isPartOf scheme=\""+schema+"\"><![CDATA["+rel+"]]></dcterms:isPartOf></dc:relation>");
						out.newLine();
					}
				}
				
				//isreferencedby
				if(rec.getIsReferencedBy()!=null && rec.getIsReferencedBy().length()>0){
					out.write("\t<dc:relation><dcterms:isReferencedBy scheme=\"dcterms:URI\"><![CDATA["+rec.getIsReferencedBy()+"]]></dcterms:isReferencedBy></dc:relation>");
					out.newLine();
				}

				//availability
				if(rec.getOldArn()!=null && rec.getOldArn().length()>2){
					out.write("\t<agls:availability>");
					out.newLine();
					out.write("\t\t<ags:availabilityLocation><![CDATA["+rec.getOldArn().substring(0, 2)+"]]></ags:availabilityLocation>");
					out.newLine();
					out.write("\t\t<ags:availabilityNumber><![CDATA["+rec.getOldArn().substring(2)+"]]></ags:availabilityNumber>");
					out.newLine();
					out.write("\t</agls:availability>");
					out.newLine();
				}

				//source
				if(rec.getSource()!=null && rec.getSource().length()>0){
					out.write("\t<dc:source><![CDATA["+rec.getSource()+"]]></dc:source>");
					out.newLine();
				}

				//coverage
				if(rec.getCoverage()!=null && rec.getCoverage().size()>0){
					for(String coverage: rec.getCoverage()){
						out.write("\t<dc:coverage><![CDATA["+coverage+"]]></dc:coverage>");
						out.newLine();
					}
				}

				//spatial agrovoc
				if(rec.getSpatial()!=null){
					for(String s: rec.getSpatial()){
						out.write("\t<dc:coverage><dcterms:spatial xml:lang=\"eng\" scheme=\"ags:AGROVOC\"><![CDATA["+s+"]]></dcterms:spatial></dc:coverage>");
						out.newLine();
					}
				}
				
				//spatial no chema
				if(rec.getSpatialSimple()!=null){
					for(String s: rec.getSpatialSimple()){
						out.write("\t<dc:coverage><dcterms:spatial><![CDATA["+s+"]]></dcterms:spatial></dc:coverage>");
						out.newLine();
					}
				}
				
				//rights
				if(rec.getRights()!=null && rec.getRights().length()>0){
					out.write("\t<dc:rights><![CDATA["+rec.getRights()+"]]></dc:rights>");
					out.newLine();
				}

				//citation
				if((rec.getCitationTitle()!=null && rec.getCitationTitle().length()>0) ||
						rec.getIssns().size()>0 || rec.getEissns().size()>0){
					out.write("\t<ags:citation>");
					out.newLine();
					if(rec.getCitationTitle()!=null && rec.getCitationTitle().length()>0){
						out.write("\t\t<ags:citationTitle><![CDATA["+rec.getCitationTitle()+"]]></ags:citationTitle>");
						out.newLine();
					}
					if(rec.getIssns().size()>0){
						for(String issn: rec.getIssns()){
							out.write("\t\t<ags:citationIdentifier scheme=\"ags:ISSN\">"+issn+"</ags:citationIdentifier>");
							out.newLine();
						}
					}
					if(rec.getEissns().size()>0){
						for(String issn: rec.getEissns()){
							out.write("\t\t<ags:citationIdentifier scheme=\"bibo:eissn\">"+issn+"</ags:citationIdentifier>");
							out.newLine();
						}
					}
					if(rec.getCitationNumber()!=null && rec.getCitationNumber().length()>0){
						out.write("\t\t<ags:citationNumber><![CDATA["+rec.getCitationNumber()+"]]></ags:citationNumber>");
						out.newLine();
					}
					if(rec.getCitationChronology()!=null && rec.getCitationChronology().length()>0){
						out.write("\t\t<ags:citationChronology><![CDATA["+rec.getCitationChronology()+"]]></ags:citationChronology>");
						out.newLine();
					}
					out.write("\t</ags:citation>");
					out.newLine();
				}

				out.write("</ags:resource>");
				out.newLine();
			}

			out.write("</ags:resources>");
			out.flush();
		}
	}

}
