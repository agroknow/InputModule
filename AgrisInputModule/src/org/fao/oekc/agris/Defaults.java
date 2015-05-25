package org.fao.oekc.agris;

import java.util.ResourceBundle;

/**
 * Loads parameters from default.properties file
 * @author celli
 *
 */
public class Defaults {
	
	private static final String BUNDLE_NAME = "default";

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Defaults() {}
	
	public static String getString(String key) {
		return RESOURCE_BUNDLE.getString(key);
	}

}
