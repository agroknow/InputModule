package org.fao.oekc.agris.inputRecords.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import jfcutils.util.LanguageTranslation;
import jfcutils.util.StringUtils;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import org.fao.oekc.agris.inputRecords.dom.AGRISAttributes;
import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.util.CheckIndex;
import org.fao.oekc.agris.inputRecords.util.EscapeXML;


/**
 * Parse the world banck source excel file creating AGRIS AP objects
 * @author celli
 *
 */
public class WorldBankExcelParser {
	
	//result
	private List<AgrisApDoc> records;
	private String arnprefix; // country code (2), year (4), subcode (1)
	
	//if true, remove records whose ISSN is in the index, if false remove records if the ISSN is in the index associated to the same country code
	private boolean globalDuplicatesRemoval;	
	
	//mapping URLs->Agrovoc
	private Map<String, String> url2Agrovoc;
	
	//index checker
	private CheckIndex indexChecker;
	
	//add WB keywords
	private boolean wbKeys;
	
	//the file to parse
	private InputStream fileInputStream;
	
	//tmp language
	private String language;
	
	/**
	 * Parse the world banck source excel file
	 * @param records
	 * @param url2Agrovoc
	 * @param fileToParse
	 * @param arnprefix
	 * @param globalDuplicatesRemoval
	 * @throws FileNotFoundException
	 */
	public WorldBankExcelParser(List<AgrisApDoc> records, Map<String, String> url2Agrovoc, File fileToParse, String arnprefix, boolean globalDuplicatesRemoval) throws FileNotFoundException{
		this.url2Agrovoc = url2Agrovoc;
		this.fileInputStream = new FileInputStream(fileToParse);
		this.records = records;
		this.arnprefix = arnprefix;
		this.globalDuplicatesRemoval = globalDuplicatesRemoval;
		this.indexChecker = new CheckIndex();
		//start the parsing
		this.parse();
	}
	
	private void parse() {
		WorkbookSettings ws = null;
		Workbook workbook = null;
		Sheet s = null;
		Cell rowData[] = null;
		int rowCount = 0;

		try {
			ws = new WorkbookSettings();
			ws.setEncoding("ISO-8859-1");
			workbook = Workbook.getWorkbook(fileInputStream, ws);

			//Getting Default Sheet 0
			s = workbook.getSheet(0);

			//Total No Of Rows in Sheet, will return you no of rows that are occupied with some data
			rowCount = s.getRows();

			//Reading Individual Row Content, not the header
			for (int i = 1; i < rowCount; i++) {
				//Get Individual Row
				rowData = s.getRow(i);
				//the document
				AgrisApDoc doc = new AgrisApDoc();
				this.wbKeys = false;
				
				//scan row: skip doc ID
				for (int j = 0; j < rowData.length; j++) {
					String content = rowData[j].getContents();
					//null value
					if(content.trim().length()==0)
						content = null;
					//create the journal
					if(j==0 && content!=null) {
						//skip doc ID
					}
					else if(j==1 && content!=null){
						//dc:type
						doc.addType(content);
					}
					else if(j==2 && content!=null){
						//abstract
						doc.addAbstract(EscapeXML.getInstance().removeHTMLTagsAndUnescape(content), null);
					}
					else if(j==3 && content!=null){
						//author general
						String[] auths = content.split("\n");
						for(String c: auths)
							doc.addCreatorGeneral(EscapeXML.getInstance().removeHTMLTagsAndUnescape(c));
					}
					else if(j==4 && content!=null){
						//collection title, can be citation title if we founf issn later
						doc.setSource(content);
					}
					else if(j==5 && content!=null){
						//document date: EMPTY
					}
					else if(j==6 && content!=null){
						//cleaning ISBN
						content = content.replaceAll("\"", "");
						content = content.replaceAll("\\*", "; ");
						String[] splitC = content.split("; ");
						for(String c: splitC){
							//ISBN or ISSN
							if(c.startsWith("ISBN"))
								doc.addIdentifier(c.replace("ISBN ", ""), "ags:ISBN");
							else if(c.startsWith("BAN") || c.startsWith("SAM")){
									//ignore
								}
							else if(c.startsWith("ISSN ")) {
								String term = c.replace("ISSN ", "");
								//search Solr if it is an ISSN
								int occurrences = this.indexChecker.checkISSNtoARN(term, this.arnprefix, globalDuplicatesRemoval);			
								if(occurrences==0) {
									doc.addIssn(term);
									doc.setCitationTitle(doc.getSource());
									doc.setSource(null);
								}
								else {
									doc = null;
									break;
								}
							}
							else {
								//ISBN
								doc.addIdentifier(c, "ags:ISBN");
							}
						}
					}
					else if(j==7 && content!=null){
						//language
						content = (new LanguageTranslation()).translate2threedigitsEn(content);
						if(content!=null && content.length()!=3)
							doc.addLanguage(content, "");
						else if(content!=null && content.length()==3){
							doc.addLanguage(content, "dcterms:ISO639-2");
							//tmp language for the title
							this.language = content;
						}
					}	
					else if(j==8 && content!=null){
						//region not relevant	
					}
					
					else if(j==9 && content!=null){
						//report name
						doc.addSupplement(EscapeXML.getInstance().removeHTMLTagsAndUnescape(content));
					}
					else if(j==10 && content!=null){
						//report number
						doc.addIdentifier(EscapeXML.getInstance().removeHTMLTagsAndUnescape(content), "ags:RN");
					}
					else if(j==11 && content!=null){
						//unit
						doc.addCreatorCorporate(content);
					}
					else if(j==12 && content!=null){
						//volume
						doc.setSource(content);
					}
					else if(j==13 && content!=null){
						//country
						String[] countries = content.split("\n");
						for(String c: countries)
							doc.addSpatial(c);
					}
					else if(j==14 && content!=null){
						//doc name: TITLE
						String term = EscapeXML.getInstance().removeHTMLTagsAndUnescape(content);
						//search Solr to see if title exists
						int occurrences = this.indexChecker.checkTitle(term);			
						if(occurrences==0) {
							//check language
							if(this.language!=null){
								doc.addTitle(term, this.language);
								this.language = null;
							}
							else
								doc.addTitle(term, null);	
						}
						else {
							System.out.println(term);
							doc = null;
							break;
						}
					}
					else if(j==15 && content!=null){
						//url
						content = (new StringUtils()).trimLeft(content);
						content = (new StringUtils()).trimRight(content);
						doc.addIdentifier(content, "dcterms:URI");
						//get agrovoc
						String agrovoc = this.url2Agrovoc.get(content);
						//if no agrovoc
						if(agrovoc==null || agrovoc.length()==0){
							//use world bank keywords
							this.wbKeys = true;
						}
						else {
							String[] keys = agrovoc.split(", ");
							for(String k: keys){
								AGRISAttributes attr = new AGRISAttributes();
								attr.setLang("en");
								attr.setScheme("ags:AGROVOC");
								doc.addAgrovoc(k, attr);
							}
						}
					}
					else if(j==16 && content!=null && this.wbKeys){
						//proj id
						doc.setDescrNotes(EscapeXML.getInstance().removeHTMLTagsAndUnescape(content));
					}			
					else if(j==17 && content!=null && this.wbKeys){
						//topic1
						if(content.length()>0)
							doc.addFreeSubject(content);
					}				
					else if(j==18 && content!=null && this.wbKeys){
						//subtopic1
						if(content.length()>0)
							doc.addFreeSubject(content);
					}	
					else if(j==19 && content!=null && this.wbKeys){
						//topic2
						if(content.length()>0)
							doc.addFreeSubject(content);
					}
					else if(j==20 && content!=null && this.wbKeys){
						//subtopic2
						if(content.length()>0)
							doc.addFreeSubject(content);
					}
					else if(j==21 && content!=null && this.wbKeys){
						//topic3
						if(content.length()>0)
							doc.addFreeSubject(content);
					}
					else if(j==22 && content!=null && this.wbKeys){
						//subtopic3
						if(content.length()>0)
							doc.addFreeSubject(content);
					}
					
				}
				if(doc!=null && doc.getTitle2language().size()>0) {
					doc.addPublisherName("The World Bank");
					doc.addPublisherPlace("Washington, DC (USA)");
					this.records.add(doc);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BiffException e) {
			e.printStackTrace();
		}
	}

}
