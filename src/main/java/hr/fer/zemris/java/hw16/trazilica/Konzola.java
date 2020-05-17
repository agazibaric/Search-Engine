package hr.fer.zemris.java.hw16.trazilica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Program that allows user to search for documents by inputing a query.
 * Documents that are listed out are the most similar documents for given input.<br>
 * 
 * Commands that are supported are:
 * <li>query</li>
 * <li>type</li>
 * <li>results</li>
 * <li>exit</li>
 * <p>
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class Konzola {
	
	/**
	 * Default path of directory that contains all documents.
	 */
	private static final String DEFAULT_DOC_PATH = "./src/main/resources/clanci";
	/**
	 * Path of stopwords file.
	 */
	private static final String STOPWORDS_PATH = "./src/main/resources/hrvatski_stoprijeci.txt";
	/**
	 * Document processor object.
	 */
	private static DocumentProcessor processor;
	/**
	 * Name of exit command.
	 */
	private static String exitCommand = "exit";
	/**
	 * Name of query command.
	 */
	private static String queryCommand = "query";
	/**
	 * Name of type command.
	 */
	private static String typeCommend = "type";
	/**
	 * Name of results command.
	 */
	private static String resultsCommand = "results";
	/**
	 * Prompt string that is printed out before every user input.
	 */
	private static String promptString = "Enter command > ";
	/**
	 * Results of query inputed by user.
	 */
	private static List<SimResult> results;
	
	/**
	 * Main method. Accepts no arguments.
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		
		Path docPath = null;
		int argsLen = args.length;
		
		if (argsLen == 1) {
			docPath = Paths.get(args[0]);
			if (!Files.isDirectory(docPath)) {
				System.err.println("Given path is not valid directory that contains documents.\n"
						+ "Path was: " + args[0]);
				return;
			}
		} else if (argsLen == 0){
			docPath = Paths.get(DEFAULT_DOC_PATH);
		} else {
			System.err.println("Invalid number of arguments.\n"
					+ "Was: " + argsLen + ", expected: 1");
			return;
		}
		
		processor = new DocumentProcessor(docPath, Paths.get(STOPWORDS_PATH));
		try (Scanner sc = new Scanner(System.in)) {
			getUserInput(sc);
		}
		
	}
	
	/**
	 * Method gets input from user.
	 * 
	 * @param sc Scanner object
	 */
	private static void getUserInput(Scanner sc) {
		System.out.println("Veličina riječnika je " + processor.vocabularySize() + " riječi.");
		while(true) {
			try {
				printPromptMessage();
				String input = sc.nextLine().trim();
				if (input == null)
					break;
				if (input.isEmpty())
					continue;
				String[] queryParts = getQueryParts(input);
				String command = queryParts[0].toLowerCase();
				if (command.equals(exitCommand))
					break;
				if (command.equals(queryCommand)) {
					processQuery(queryParts);
					continue;
				}
				if (command.equals(typeCommend)) {
					processTypeCommand(queryParts);
					continue;
				}
				if (command.equals(resultsCommand)) {
					processResultsCommand(queryParts);
					continue;
				}
				// else: command is unknown
				System.out.println("Nepoznata naredba");
			} catch (IllegalArgumentException ex) {
				System.err.println(ex.getMessage());
			}
		}
	}
	
	/**
	 * Method processes result command.
	 * 
	 * @param queryParts parts of user's input
	 */
	private static void processResultsCommand(String[] queryParts) {
		if (results == null) 
			throw new IllegalArgumentException("Invalid 'results' command call.\n "
					+ "Before 'results' command you must enter query");
		if (queryParts.length > 1)
			throw new IllegalArgumentException("'results' command accepts no extra arguments.");
		
		printResults();
	}
	
	/**
	 * Method processes type command.
	 * 
	 * @param queryParts parts of user's input
	 */
	private static void processTypeCommand(String[] queryParts) {
		if (results == null) 
			throw new IllegalArgumentException("Invalid 'type' command call.\n "
					+ "Before type command you must enter query");
		if (queryParts.length == 1) 
			throw new IllegalArgumentException("Invalid input for type command. \n"
					 + "Missing input after 'type'.");
		Integer index = null;
		try {
			index = Integer.parseInt(queryParts[1].trim());
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Invalid input for type command.\n "
					+ "Second argument must be integer.");
		}
		
		if (index >= results.size() || index > 10)
			throw new IllegalArgumentException("Invalid input for type command.\n"
					+ "Given index is out of range. Was: " + index);
		
		Path path = results.get(index).getPath().toAbsolutePath();
		System.out.println("Document: " + path);
		try {
			byte[] bytes = Files.readAllBytes(path);
			System.out.println(new String(bytes).trim());
		} catch (IOException e) {
			System.err.println("Error occurred during reading from file");
		}
	}
	
	/**
	 * Method processes query command.
	 * 
	 * @param queryParts parts of user's input
	 */
	private static void processQuery(String[] queryParts) {
		if (queryParts.length == 1)
			throw new IllegalArgumentException("Invalid input for query command. \n"
											 + "Missing input after 'query'.");
		results = processor.getTopSimilarDocuments(queryParts[1].trim());
		printResults();
	}
	
	/**
	 * Method prints out results of query to standard output.
	 */
	private static void printResults() {
		int n = results.size() < 10 ? results.size() : 10;
		if (n == 0) {
			System.out.println("There are no similar documents.");
		} else {
			System.out.println("Query is: " + processor.getQueryWords());
			System.out.println("Najboljih 10 rezultata:");
			for (int i = 0; i < n; i++) {
				SimResult result = results.get(i);
				System.out.format("[%2d] (%.4f) %s%n", i, result.getValue(), result.getPath().toAbsolutePath());
			}
		}
	}
	
	/**
	 * Method returns user's input in parts as string array.
	 * 
	 * @param input user's input
	 * @return      input parts as string array
	 */
	private static String[] getQueryParts(String input) {
		String[] queryParts = input.split("\\s+", 2);
		return queryParts;
	}
	
	/**
	 * Method prints out propt string.
	 */
	private static void printPromptMessage() {
		System.out.format("%n%s", promptString);
	}
	
}
