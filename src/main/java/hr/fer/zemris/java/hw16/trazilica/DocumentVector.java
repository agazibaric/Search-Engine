package hr.fer.zemris.java.hw16.trazilica;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class represents document vector that is described 
 * by document path and term frequency vector.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class DocumentVector {
	
	/**
	 * Document's path.
	 */
	private Path docPath;
	/**
	 * Term frequency vector.
	 */
	private Map<String, Integer> TFVector;

	/**
	 * Constructor.
	 * 
	 * @param docPath  {@link #docPath}
	 * @param TFVector {@link #TFVector}
	 */
	public DocumentVector(Path docPath, Map<String, Integer> TFVector) {
		this.docPath = docPath;
		this.TFVector = TFVector;
	}
	
	/**
	 * Method checks of this document contains given word.
	 * 
	 * @param word word that is checked
	 * @return     <code>true</code> if document contains given word, otherwise <code>false</code>
	 */
	public boolean containsWord(String word) {
		Integer num = TFVector.get(word);
		return !(num == null || num == 0);
	}

	/**
	 * Method returns path of the document.
	 * 
	 * @return path of this document
	 */
	public Path getDocPath() {
		return docPath;
	}
	
	/**
	 * Method returns document's term frequency vector.
	 * 
	 * @return document's term frequency vector
	 */
	public Map<String, Integer> getTFVector() {
		return TFVector;
	}
	
	/**
	 * Method returns term frequency-inverse document frequency for this document.
	 * 
	 * @param IDFVector inverse document frequency vector
	 * @return          term frequency-inverse document frequency for this document
	 */
	public Map<String, Double> getTFIDFVector(Map<String, Double> IDFVector) {
		Map<String, Double> TFIDFVector = new LinkedHashMap<>();
		TFVector.forEach((k, v) -> {
			Double value = v * IDFVector.get(k);
			TFIDFVector.put(k, value);
		});
		
		return TFIDFVector;
	}
	
}
