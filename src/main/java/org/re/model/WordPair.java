/**
 * 
 */
package org.re.model;

/**
 * @author doquocanh-macbook
 *
 *	A word pair contains a most common verb and most common noun. 
 *	The pair can be reversed to make an unfamiliar pair. 
 */
public class WordPair implements Comparable<WordPair>{
	// Attributes
	private TopicWord firstPart;  	// Initially a verb then will become noun
	private TopicWord secondPart; 	// Initially a noun then will become verb
	private boolean isFlipped; 		// A flag to keep track if the pair has been flipped
	private int ranking; 	   		// Ranks how common the pair is
	/**
	 * 
	 */
	public WordPair() {
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return the firstPart
	 */
	public TopicWord getFirstPart() {
		return firstPart;
	}
	/**
	 * @param firstPart the firstPart to set
	 */
	public void setFirstPart(TopicWord firstPart) {
		this.firstPart = firstPart;
	}
	/**
	 * @return the secondPart
	 */
	public TopicWord getSecondPart() {
		return secondPart;
	}
	/**
	 * @param secondPart the secondPart to set
	 */
	public void setSecondPart(TopicWord secondPart) {
		this.secondPart = secondPart;
	}
	/**
	 * @return the isFlipped
	 */
	public boolean isFlipped() {
		return isFlipped;
	}
	/**
	 * @param isFlipped the isFlipped to set
	 */
	public void setFlipped(boolean isFlipped) {
		this.isFlipped = isFlipped;
	}
	/**
	 * @return the ranking
	 */
	public int getRanking() {
		return ranking;
	}
	/**
	 * @param ranking the ranking to set
	 */
	public void setRanking(int ranking) {
		this.ranking = ranking;
	}
	@Override
	public int compareTo(WordPair o) {
		// TODO Auto-generated method stub
		if (this.ranking > o.ranking)
			return 1;
		if (this.ranking < o.ranking)
			return -1;
		return 0;
	}
}
