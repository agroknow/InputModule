package org.fao.oekc.agris.inputRecords.util;

import java.util.HashMap;
import java.util.Map;

/**
 * This class supplies some methods to unescape special chars according XML specifications
 * Then the content will need a CDATA section
 *
 */
public class EscapeXML {
	
	//singleton
	private static EscapeXML instance;

	private Map<String, String> basic_char;
	
	/**
	 * Build the map to undecode HTML characters. Then, the text needs a CDATA section
	 */
	public EscapeXML(){
		basic_char = new HashMap<String, String>();
		basic_char.put("&quot;","\"");
		basic_char.put("&amp;","&");
		basic_char.put("&lt;","<");
		basic_char.put("&gt;",">");
		basic_char.put("&apos;","'");
		basic_char.put("&#945;","α");
		basic_char.put("&#8211;","–");
		basic_char.put("&#947;","γ");
		basic_char.put("&#948;","δ");
		basic_char.put("&#956;","μ");
	}
	
	public static synchronized EscapeXML getInstance(){
		if(instance==null)
			instance = new EscapeXML();
		return instance;	
	}
	
	/**
	 * Unescape XML characters
	 */
	public String unescapeXML(String text){
		for(String key: basic_char.keySet()){
			text = text.replaceAll(key, basic_char.get(key));
		}
		return text;
	}
	
	/**
	 * Remove common HTML tags in a string
	 * @param text the source string
	 * @return a string that replaces HTML common tags with nothing
	 */
	public String removeCommonHTMLTags(String text){
		text = text.replaceAll("<p>", "");
		text = text.replaceAll("</p>", "");
		text = text.replaceAll("<p/>", "");
		text = text.replaceAll("<it>", "");
		text = text.replaceAll("<i>", "");
		text = text.replaceAll("</i>", "");
		text = text.replaceAll("<I>", "");
		text = text.replaceAll("</I>", "");
		text = text.replaceAll("</it>", "");
		text = text.replaceAll("<b>", "");
		text = text.replaceAll("<br/>", "");
		text = text.replaceAll("</b>", "");
		text = text.replaceAll("<div>", "");
		text = text.replaceAll("</div>", "");
		text = text.replaceAll("\n", "");
		text = text.replaceAll("\r", "");
		text = text.replaceAll("\t", "");
		text = text.replaceAll("    ", " ");
		return text;
	}
	
	/**
	 * Remove common HTML tags in a string and unescape entities
	 * @param text the source string
	 * @return the source string without common HTML tags and entities (the XML can need CDATA section)
	 */
	public String removeHTMLTagsAndUnescape(String text){
		text = this.removeCommonHTMLTags(text);
		return this.unescapeXML(text);
	}
	
	public static void main(String[] args){
		String source = "<![CDATA[<p/> <p>Experiments on use of an agar-gel method for recovery of migrating <it>Ascaris suum </it>larvae from the liver and lungs of pigs were conducted to obtain fast standardized methods. Subsamples of blended tissues of pig liver and lungs were mixed with agar to a final concentration of 1% agar and the larvae allowed to migrate out of the agar-gel into 0.9% NaCl at 38&#176;C. The results showed that within 3 h more than 88% of the recoverable larvae migrated out of the liver agar-gel and more than 83% of the obtained larvae migrated out of the lung agar-gel. The larvae were subsequently available in a very clean suspension which reduced the sample counting time. Blending the liver for 60 sec in a commercial blender showed significantly higher larvae recovery than blending for 30 sec. Addition of gentamycin to reduce bacterial growth during incubation, glucose to increase larval motility during migration or ice to increase sedimentation of migrated larvae did not influence larvae recovery significantly.</p> ]]>";
		System.out.println(EscapeXML.getInstance().removeCommonHTMLTags(source));
	}

}