package org.fao.oekc.agris.inputRecords.test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;
import org.fao.oekc.agris.inputRecords.parser.AgrisApSaxParser;
import org.xml.sax.SAXException;

public class TestAgrisApSaxParser {
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException{
		
		//prepare the parsing
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		spf.setNamespaceAware(false);
		SAXParser saxParser = spf.newSAXParser();
		
		List<AgrisApDoc> records = new LinkedList<AgrisApDoc>();
		File contentFile = new File("C:/Users/celli/Documents/workspace_agris/agris_input/tmp/DO20120130101054914.xml");
		AgrisApSaxParser parser = new AgrisApSaxParser(records, true);
		
		saxParser.parse(contentFile, parser);
		
		System.out.println(records.size());
	}

}
