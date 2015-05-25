package org.fao.oekc.agris.inputRecords.customizations;

import org.fao.oekc.agris.inputRecords.dom.AgrisApDoc;

/**
 * Define customizations for an input format
 * @author celli
 *
 */
public interface Customization {
	
	public boolean abstractException(AgrisApDoc currentDoc, String astratto);
	public boolean dateException(AgrisApDoc currentDoc, String date);
	public boolean identifierException(AgrisApDoc currentDoc, String id);
	public boolean relationException(AgrisApDoc currentDoc, String rel);
	public boolean formatException(AgrisApDoc currentDoc, String format);
	public boolean languageException(AgrisApDoc currentDoc, String lang);
	public boolean coverageException(AgrisApDoc currentDoc, String cover);
	public boolean sourceException(AgrisApDoc currentDoc, String source);
	public boolean hasFAOtoBeExcluded(String term);

}
