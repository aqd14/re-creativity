/**
 * 
 */
package org.re.model;

/**
 * @author doquocanh-macbook
 *
 */
public class TopicWord {
	// Attributes
	private Word word;
	private double weight;		//
	
	/**
	 * A word represents for its topic
	 */
	public TopicWord() {
		this.weight = 0.0;
	}
	
	public TopicWord(Word word, double weight) {
		this.word = word;
		this.weight = weight;
	}

	/**
	 * @return the word
	 */
	public Word getWord() {
		return word;
	}

	/**
	 * @param word the word to set
	 */
	public void setWord(Word word) {
		this.word = word;
	}

	/**
	 * @return the occurrence
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * @param occurrence the occurrence to set
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	@Override
	public String toString() {
		return this.word.toString() + " (" + this.weight + ")";
	}
}
