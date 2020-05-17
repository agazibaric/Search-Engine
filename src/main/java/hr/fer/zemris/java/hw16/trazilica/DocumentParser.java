package hr.fer.zemris.java.hw16.trazilica;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Class offers methods for words parsing.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class DocumentParser {
	
	/**
	 * List of words that are parsed.
	 */
	private List<String> words;
	
	/**
	 * Constructor.
	 * 
	 * @param content content that is parsed
	 */
	public DocumentParser(String content) {
		Objects.requireNonNull(content, "Content that is parsed must not be null");
		parse(content);
	}
	
	/**
	 * Method parses given content.
	 * 
	 * @param content content that is parsed
	 */
	private void parse(String content) {
		words = new LinkedList<>();
		char[] chars = content.toCharArray();
		int index = 0;
		while (index < chars.length) {
			if (Character.isAlphabetic(chars[index])) {
				int startIndex = index;
				while (index < chars.length && Character.isAlphabetic(chars[index])) {
					index++;
				}
				words.add(new String(chars, startIndex, index - startIndex).toLowerCase());
			}
			index++;
		}
	}
	
	/**
	 * Method returns unique words from given content.
	 * 
	 * @return unique list of words
	 */
	public List<String> getKeyWords() {
		return new LinkedList<>(new TreeSet<>(words));
	}
	
	/**
	 * Method returns all words that are parsed from given content.
	 * 
	 * @return all words that are parsed from given content
	 */
	public List<String> getWords() {
		return words;
	}

}
