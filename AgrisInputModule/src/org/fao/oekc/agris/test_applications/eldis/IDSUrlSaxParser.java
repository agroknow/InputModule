package org.fao.oekc.agris.test_applications.eldis;

import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Sax Parser for Eldis XML records to extract the list of fulltexts
 * @author celli
 *
 */
public class IDSUrlSaxParser extends DefaultHandler {

	//output: list of fulltext URLs
	private List<String> fulltextURLs;

	//data
	private boolean isUrlListItem;
	private boolean isUrl;
	//buffer to read the entire content
	private StringBuffer buffer; 

	/**
	 * Initialize the parser
	 * @param fulltextURLs the list to be filled
	 */
	public IDSUrlSaxParser(List<String> fulltextURLs){
		this.fulltextURLs = fulltextURLs;
		this.isUrlListItem = false;
		this.isUrl = false;
	}

	////////////////////////////////////////////////////////////////////
	// Event handlers.
	////////////////////////////////////////////////////////////////////
	/**
	 * Recognize an element
	 */
	public void startElement (String namespaceURI, String localName, String rawName, Attributes atts) {		

		if(rawName.equalsIgnoreCase("urls")){
			this.isUrl = true;
		} else
			if(rawName.equalsIgnoreCase("list-item") && this.isUrl){
				this.buffer = new StringBuffer();
				this.isUrlListItem = true;
			}
	}
	
	/**
	 * Extract content from XML
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) {
			//BUFFER reader
			if(this.isUrlListItem){
				this.buffer.append(ch, start, length);
			}
	}
	
	/**
	 * The end of an element. For big elements like ABSTRACT, to allow the buffering of all content
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceUri, String localName, String rawName)
	throws SAXException {
		if(rawName.equalsIgnoreCase("urls")){
			this.isUrl = false;
		}
		else
			if(rawName.equalsIgnoreCase("list-item") && this.isUrl && this.isUrlListItem){
				this.isUrlListItem = false;
				String term = new String(this.buffer);
				if(term!=null && !term.trim().equals("")){
					this.fulltextURLs.add(term);
				}
			}
	}

}
