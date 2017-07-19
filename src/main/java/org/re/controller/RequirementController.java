package org.re.controller;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.re.common.POS;
import org.re.common.SoftwareSystem;
import org.re.model.Requirement;
import org.re.model.Topic;
import org.re.model.TopicWord;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RequirementController {
    
    // Logger
    final static Logger logger = Logger.getLogger(RequirementController.class);
    
    ObservableList<Requirement> requirements = FXCollections.observableArrayList();

    public RequirementController() {
        
    }
    
    /**
     * Cross-combine topic words to make requirements from unfamiliar VERB-NOUN or NOUN-VERB pair  
     * @param sys
     * @param topics
     */
    public void constructRequirements(SoftwareSystem sys, ObservableList<Topic> topics) {
        // Sort topics' words based on their weights
        for (Topic t : topics) {
            Collections.sort(t.getTopicWords());
        }
        
        for (int i = 0; i < topics.size()-1; i++) {
            for (int j = i+1; j < topics.size(); j++) {
                Topic t1 = topics.get(i);
                Topic t2 = topics.get(j);
                exploreTopicWords(sys, t1.getTopicWords(), t2.getTopicWords());
            }
        }
    }
    
    /**
     * Iterate through the word list to select the pair of most dominant nouns and verbs
     * @param sys
     * @param wordList1
     * @param wordList2
     */
    private void exploreTopicWords(SoftwareSystem sys, ArrayList<TopicWord> wordList1, ArrayList<TopicWord> wordList2) {
        for (TopicWord tw1 : wordList1) {
            for (TopicWord tw2 : wordList2) {
                // Only combine topics words if 1st word is NOUN && 2st word is VERB and vice versa
                POS pos1 = tw1.getWord().getPos();
                POS pos2 = tw2.getWord().getPos();
                if ((pos1 == POS.NOUN && pos2 == POS.VERB) || (pos1 == POS.VERB && pos2 == POS.NOUN)) {
                    combineTopicWords(sys, tw1, tw2);
                }
            }
        }
    }
    
    /**
     * <p>
     * Create requirement from pair of topic words. Before generating requirements,
     * check tw1's and tw2's part of speech to flip their part-of-speeches
     * </p>
     * @param sys
     * @param tw1
     * @param tw2
     */
    private void combineTopicWords(SoftwareSystem sys, TopicWord tw1, TopicWord tw2) {
        Requirement r = null;
        if (tw1.getWord().getPos() == POS.NOUN && tw2.getWord().getPos() == POS.VERB) {
            // Switch part-of-speeches of tw1 and tw2
            // tw1 now becomes a VERB and tw2 plays a role as a NOUN
            r = new Requirement(sys, tw1.getWord(), tw2.getWord());
        } else if (tw1.getWord().getPos() == POS.VERB && tw2.getWord().getPos() == POS.NOUN) {
            // Switch part-of-speeches of tw1 and tw2
            // tw1 now becomes a NOUN and tw2 plays a role as a VERB
            r = new Requirement(sys, tw2.getWord(), tw1.getWord());
        } else {
            logger.error("Invalid word selection: Should be a VERB-NOUN or NOUN-VERB pair. \n Actual: \n +tw1's: "
                    + tw1.getWord().getPos() + "\n+tw2's: " + tw2.getWord().getPos());
        }
        requirements.add(r);
    }
    
    /**
     * @return requirements list
     */
    public ObservableList<Requirement> getRequirements() {
        return requirements;
    }
    
    /**
     * Set requirements list
     * @param requirements
     */
    public void setRequirements(ObservableList<Requirement> requirements) {
        this.requirements = requirements;
    }
}
