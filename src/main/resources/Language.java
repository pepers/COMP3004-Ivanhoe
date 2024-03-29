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
	private final Language.Dialect dialect;                         // dialect chosen
	private boolean censor;                                         // censor bad words if true
	
	// bad words that can be censored
	final String[] badWords = {"ass", "bitch", "fuck", "shit"};               
	
	public Language (Language.Dialect dialect, boolean censor) {
		this.dialect = dialect;
		this.censor = censor;
		
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
	 * return if censoring is on/off
	 */
	public boolean isCensored() {
		return censor;
	}
	
	/*
	 * translate a string
	 */
	public String translate (String input) {
		String result = input; // translated string to return
		
		// censor words
		if (censor) {
			for (int i=0; i<badWords.length; i++) {
				StringBuilder blank = new StringBuilder(); // will blank over bad word with *'s
				for (int j=0; j<badWords[i].length(); j++) { blank.append('*');	}
				String censored = result.replaceAll("\\b" + badWords[i] + "\\b", blank.toString());
				result = censored;
			}
		}
		
		// exit early if dialect is set to 'none'
		if (dialect.equals(Language.Dialect.none)) {
			return result;
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
		dictionary.put("a lot", "overmany");
		dictionary.put("are", "art");
		dictionary.put("ask", "beseech");
		dictionary.put("between", "betwixt");
		dictionary.put("can", "canst");
		dictionary.put("come", "comest");
		dictionary.put("comes", "cometh");
		dictionary.put("do", "dost");
		dictionary.put("does", "dost");
		dictionary.put("from where", "whence");
		dictionary.put("give", "bequeath");
		dictionary.put("has", "hath");
		dictionary.put("here", "hither");
		dictionary.put("know", "wit");
		dictionary.put("none", "nary");
		dictionary.put("nothing", "naught");
		dictionary.put("old", "olde");
		dictionary.put("rich", "fullsome");
		dictionary.put("there", "thither");
		dictionary.put("to where", "whither");
		dictionary.put("will", "shalt");
		dictionary.put("years ago", "yore");
		dictionary.put("you", "thou");
		dictionary.put("your", "thy");
		dictionary.put("you're", "thou art");
	}
}
