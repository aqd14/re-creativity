/**
 * 
 */
package org.re.model;

import org.re.common.SoftwareSystem;

/**
 * @author doquocanh-macbook
 *
 *         A word pair contains a most common verb and most common noun. The
 *         pair can be reversed to make an unfamiliar pair.
 */
public class WordPair implements Comparable<WordPair> {
    // Attributes
    private SoftwareSystem system;
    private TopicWord verb; // Initially a noun before flipping pos
    private TopicWord noun; // Initially a verb before flipping pos
    private double cosineSimilarity; // Ranks how similar word pair is to list of requirements
    
    public WordPair(SoftwareSystem system, TopicWord verb, TopicWord noun) {
        this(system, verb, noun, Double.NaN);
    }
    
    /**
     * 
     */
    public WordPair(SoftwareSystem system, TopicWord verb, TopicWord noun, double cosineSimilarity) {
        this.setSystem(system);
        this.verb = verb;
        this.noun = noun;
        this.cosineSimilarity = cosineSimilarity;
    }

    /**
     * @return the firstPart
     */
    public TopicWord getVerb() {
        return verb;
    }

    /**
     * @param firstPart
     *            the firstPart to set
     */
    public void setVerb(TopicWord firstPart) {
        this.verb = firstPart;
    }

    /**
     * @return the secondPart
     */
    public TopicWord getNoun() {
        return noun;
    }

    /**
     * @param secondPart
     *            the secondPart to set
     */
    public void setNoun(TopicWord secondPart) {
        this.noun = secondPart;
    }

    /**
     * @return the cosineSimilarity
     */
    public double getCosineSimilarity() {
        return cosineSimilarity;
    }

    /**
     * @param cosineSimilarity the cosineSimilarity to set
     */
    public void setCosineSimilarity(double cosineSimilarity) {
        this.cosineSimilarity = cosineSimilarity;
    }

    /**
     * @return the system
     */
    public SoftwareSystem getSystem() {
        return system;
    }

    /**
     * @param system the system to set
     */
    public void setSystem(SoftwareSystem system) {
        this.system = system;
    }

    @Override
    public int compareTo(WordPair o) {
        // TODO Auto-generated method stub
        if (this.cosineSimilarity > o.cosineSimilarity)
            return 1;
        if (this.cosineSimilarity < o.cosineSimilarity)
            return -1;
        return 0;
    }
}
