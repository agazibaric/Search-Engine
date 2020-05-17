package hr.fer.zemris.java.hw16.trazilica;

import java.nio.file.Path;

/**
 * Class represents results of similarity comparison between documents.
 * 
 * @author Ante Gazibaric
 * @version 1.0
 *
 */
public class SimResult implements Comparable<SimResult> {

	/**
	 * Path of the file.
	 */
	private Path path;
	/**
	 * Value of similarity.
	 */
	private Double value;

	/**
	 * Constructor.
	 * 
	 * @param path  file path
	 * @param value similarity value
	 */
	public SimResult(Path path, Double value) {
		this.path = path;
		this.value = value;
	}

	/**
	 * Method returns file path.
	 * 
	 * @return file path
	 */
	public Path getPath() {
		return path;
	}

	/**
	 * Method returns similarity value.
	 * 
	 * @return similarity value
	 */
	public Double getValue() {
		return value;
	}

	@Override
	public int compareTo(SimResult r) {
		return Double.compare(r.value, this.value);
	}	
	
}
