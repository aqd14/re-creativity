package org.re.model;

import java.util.ArrayList;

import org.re.utils.Utils;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

public class Topic extends RecursiveTreeObject<Topic> implements Comparable<Topic> {
    private int topicNumber;
    private double topicDistribution;
    private ArrayList<TopicWord> topicWords;

    public Topic() {
        // TODO Auto-generated constructor stub
    }

    public Topic(int topicNumber, double topicDistribution, ArrayList<TopicWord> topicWords) {
        this.topicNumber = topicNumber;
        this.topicDistribution = Utils.round(topicDistribution, 4);
        this.topicWords = topicWords;
    }

    /**
     * @return the topicNumber
     */
    public int getTopicNumber() {
        return topicNumber;
    }

    // public String getTopicNumberString() {
    // }

    /**
     * @param topicNumber
     *            the topicNumber to set
     */
    public void setTopicNumber(int topicNumber) {
        this.topicNumber = topicNumber;
    }

    /**
     * @return the topicDistribution
     */
    public double getTopicDistribution() {
        return topicDistribution;
    }

    /**
     * @param topicDistribution
     *            the topicDistribution to set
     */
    public void setTopicDistribution(double topicDistribution) {
        this.topicDistribution = topicDistribution;
    }

    /**
     * @return the topicWords
     */
    public ArrayList<TopicWord> getTopicWords() {
        return topicWords;
    }

    /**
     * @param topicWords
     *            the topicWords to set
     */
    public void setTopicWords(ArrayList<TopicWord> topicWords) {
        this.topicWords = topicWords;
    }

    @Override
    public String toString() {
        if (topicWords == null) {
            return null;
        }
        StringBuilder bd = new StringBuilder();
        for (TopicWord tw : topicWords) {
            bd.append(tw.getWord().getContent()).append(" (").append(tw.getWeight()).append(")\n");
        }
        String content = bd.toString();
        // Replace the last comma
        return Utils.replaceLastOccurrence(content, ",", "");
    }

    /**
     * Topics are ranked based on their distribution across a corpus, which is
     * how likely the topic appears in a document
     */
    @Override
    public int compareTo(Topic o) {
        if (this.topicDistribution > o.topicDistribution) {
            return 1;
        }
        if (this.topicDistribution < o.topicDistribution) {
            return -1;
        }
        return 0;
    }
}
