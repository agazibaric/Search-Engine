package hr.fer.zemris.java.hw16.trazilica;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Class represents document processor that is used for 
 * collecting all the words from documents directory into vocabulary.
 * It offers user to do comparisons between documents and gets best results of comparison.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class DocumentProcessor {
	
	/**
	 * All words in vocabulary.
	 */
	private Set<String> vocabulary = new TreeSet<>();
	/**
	 * Document vectors.
	 */
	private List<DocumentVector> docVectors = new LinkedList<>();
	/**
	 * Path of directory that contains all documents.
	 */
	private Path docDirectory;
	/**
	 * Path of the file that contains all stop words.
	 */
	private Path stopwordsPath;
	/**
	 * List of stop words.
	 */
	private List<String> stopwords;
	/**
	 * Charset.
	 */
	private static final Charset CHARSET = StandardCharsets.UTF_8;
	/**
	 * Inverse document frequency vector.
	 */
	private Map<String, Double> IDFVector;
	/**
	 * Words of user's query input.
	 */
	private List<String> queryWords;
	
	/**
	 * Constructor.
	 * 
	 * @param docDirectory  {@link #docDirectory}
	 * @param stopwordsPath {@link #stopwordsPath}
	 */
	public DocumentProcessor(Path docDirectory, Path stopwordsPath) {
		this.docDirectory = docDirectory;
		this.stopwordsPath = stopwordsPath;
		
		fillStopwords();
		process();
	}
	
	/**
	 * Method makes list of stop words.
	 */
	private void fillStopwords() {
		try {
			stopwords = Files.readAllLines(stopwordsPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method initializes {@link DocumentProcessor}.
	 */
	private void process() {
		try {
			Files.walkFileTree(docDirectory, new VocabularyFileVisitor());
			Files.walkFileTree(docDirectory, new DocVectorFileVisitor());
		} catch (IOException e) {
			e.printStackTrace();
		}
		setIDFVector();
	}
	
	/**
	 * Method initializes {@link #IDFVector}.
	 */
	private void setIDFVector() {
		IDFVector = new LinkedHashMap<>();
		int numOfDocs = docVectors.size();
		for (String word : vocabulary) {
			int numOfDocContainsWord = getCountDocContainsWord(word);
			Double value = Math.log10((double)numOfDocs / numOfDocContainsWord);
			IDFVector.put(word, value);
		}
	}
	
	/**
	 * Method returns number of documents that contains given {@code word}.
	 * 
	 * @param word word
	 * @return     number of documents that contains given {@code word}
	 */
	private int getCountDocContainsWord(String word) {
		int count = 0;
		for (DocumentVector doc : docVectors) {
			if (doc.containsWord(word)) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * Method returns list of document vectors.
	 * 
	 * @return list of document vectors
	 */
	public List<DocumentVector> getDocVectors() {
		return docVectors;
	}
	
	/**
	 * Method returns {@link #IDFVector}.
	 * 
	 * @return {@link #IDFVector}
	 */
	public Map<String, Double> getIDFVector() {
		return IDFVector;
	}
	
	/**
	 * Method returns {@link #vocabulary}.
	 * 
	 * @return {@link #vocabulary}
	 */
	public Set<String> getVocabulary() {
		return vocabulary;
	}
	
	/**
	 * Method returns number of words in vocabulary.
	 * 
	 * @return size of vocabulary
	 */
	public int vocabularySize() {
		return vocabulary.size();
	}
	
	public String getQueryWords() {
		return queryWords.toString();
	}
	
	/**
	 * Method returns {@link #stopwords}.
	 * 
	 * @return {@link #stopwords}
	 */
	public List<String> getStopwords() {
		return stopwords;
	}
	
	/**
	 * Method compares two document vectors and returns similarity value.
	 * 
	 * @param d1 first document vector
	 * @param d2 second document vector
	 * @return   similarity value
	 */
	private double compareDocuments(DocumentVector d1, DocumentVector d2) {
		Map<String, Double> TFIDFVectorD1 = d1.getTFIDFVector(IDFVector);
		Map<String, Double> TFIDFVectorD2 = d2.getTFIDFVector(IDFVector);
		double sim = 0.0;
		for (Entry<String, Double> entry : TFIDFVectorD1.entrySet()) {
			sim += entry.getValue() * TFIDFVectorD2.get(entry.getKey());
		}
		sim /= (getNorm(TFIDFVectorD1) * getNorm(TFIDFVectorD2));
		
		return sim;
	}
	
	/**
	 * Method returns norm of the given {@code vector}.
	 * 
	 * @param vector vector whose norm is returned
	 * @return       norm of the vector
	 */
	private double getNorm(Map<String, Double> vector) {
		double norm = 0.0;
		for (Double value : vector.values()) {
			norm += value * value;
		}
		return Math.sqrt(norm);
	}
	
	/**
	 * Method returns most similar documents to the given string content.
	 * 
	 * @param content content whose similar documents are returned
	 * @return        list of similar documents
	 */
	public List<SimResult> getTopSimilarDocuments(String content) {
		List<SimResult> results = new LinkedList<>();
		DocumentVector queryDoc = getDocumentVector(content, null);
		
		for (DocumentVector docVector : docVectors) {
			double simValue = compareDocuments(docVector, queryDoc);
			results.add(new SimResult(docVector.getDocPath(), simValue));
		}
		
		results.removeIf(r -> r.getValue() == 0.0 | Double.isNaN(r.getValue()));
		Collections.sort(results);
		return results;
	}
	
	/**
	 * Method returns document vector from given {@code docContent} and {@code path}.
	 * If path is {@code null}, content represents query input.
	 * 
	 * @param docContent content of the document
	 * @param path       path of the document
	 * @return           document vector
	 */
	private DocumentVector getDocumentVector(String docContent, Path path) {
		List<String> words = new DocumentParser(docContent).getWords();
		words.retainAll(vocabulary);
		Map<String, Integer> wordsFreq = words.stream()
				.collect(Collectors.toMap(w -> w, w -> 1, Integer::sum));
		Map<String, Integer> TFVector = getTFVector(wordsFreq);
		
		if (path == null) {
			this.queryWords = words;
		}
		
		return new DocumentVector(path, TFVector);
	}
	
	/**
	 * Method returns term frequency vector.
	 * 
	 * @param wordsFreq frequency of all the words.
	 * @return          term frequency vector
	 */
	private Map<String, Integer> getTFVector(Map<String, Integer> wordsFreq) {
		Map<String, Integer> docVector = new LinkedHashMap<>();
		for (String word : vocabulary) {
			Integer num = wordsFreq.get(word);
			docVector.put(word, num == null ? 0 : num);
		}
		return docVector;
	}
	
	/**
	 * File visitor that makes list of document vectors.
	 * 
	 * @author Ante Gazibaric
	 * @version 1.0
	 *
	 */
	private class DocVectorFileVisitor extends SimpleFileVisitor<Path> {
		
		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes atr) throws IOException {
			
			byte[] data = Files.readAllBytes(path);
			String docContent = new String(data, CHARSET);
			docVectors.add(getDocumentVector(docContent, path));
			
			return FileVisitResult.CONTINUE;
		}
		
		
	}
	
	/**
	 * File visitor that makes set of all found words in documents.
	 * 
	 * @author Ante Gazibaric
	 * @version 1.0
	 *
	 */
	private class VocabularyFileVisitor extends SimpleFileVisitor<Path> {
		
		@Override
		public FileVisitResult visitFile(Path path, BasicFileAttributes atr) throws IOException {
			
			byte[] data = Files.readAllBytes(path);
			String docContent = new String(data, CHARSET);
			List<String> words = new DocumentParser(docContent).getKeyWords();
			words.removeAll(stopwords);
			vocabulary.addAll(words);
			
			return FileVisitResult.CONTINUE;
		}
		
	}

}
