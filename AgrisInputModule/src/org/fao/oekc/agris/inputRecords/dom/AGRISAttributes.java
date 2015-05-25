package org.fao.oekc.agris.inputRecords.dom;

/**
 * This is a couple language/scheme of AGRIS AP attributes
 * @author celli
 *
 */
public class AGRISAttributes {
	
	private String scheme;
	private String lang;
	
	public AGRISAttributes(String scheme, String lang){
		this.scheme = scheme;
		this.lang = lang;
	}
	
	public AGRISAttributes(){}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		if(scheme!=null && scheme.length()>0)
			this.scheme = scheme;
		else
			this.scheme = "";
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		if(lang!=null && lang.length()>0)
			this.lang = lang;
		else
			this.lang = "";
	}

}
