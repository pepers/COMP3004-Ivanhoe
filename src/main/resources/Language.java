/*
 * - holds dictionary of word translations
 * - can use this to translate chat messages, based on dialect chosen
 */

package main.resources;

import java.util.HashMap;
import java.util.Map;

public class Language {

	// dialects to choose from:
	public enum Dialect { none, oldEnglish } 
	
	Map<String, String> dictionary = new HashMap<String, String>(); // holds the translations
	private Language.Dialect dialect;                               // dialect chosen
	
	
	public Language (Language.Dialect dialect) {
		this.dialect = dialect;
		
		// populate dictionary based on chosen dialect
		switch (dialect) {
			case oldEnglish: oldEnglish();
				break;
			case none: default:
				break;	
		}
	}
	
	/*
	 * return the chosen dialect 
	 */
	public Language.Dialect getDialect () {
		return dialect;
	}
	
	/*
	 * translate a string
	 */
	public String translate (String input) {
		String result = input; // translated string to return
		
		if (dialect.equals(Language.Dialect.none)) {
			return input;
		}
		
		// translate each word 
		for (Map.Entry<String, String> entry : dictionary.entrySet()) {
			String key = entry.getKey();
			// "\\b" is a word boundary, so we don't replace only parts of words
			String newResult = result.replaceAll("\\b" + key + "\\b", entry.getValue());
			result = newResult;
		}
		return result;
	}
	
	/*
	 * populate dictionary with fake Old English translations
	 */
	private void oldEnglish () {
		dictionary.put("old", "olde");
		dictionary.put("you", "thou");
		dictionary.put("your", "thy");
	}
}
