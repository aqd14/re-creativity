package org.re.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.re.common.SoftwareSystem;
import org.re.common.View;
import org.re.model.Requirement;
import org.re.model.Topic;
import org.re.model.TopicWord;
import org.re.utils.Utils;

import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;

public class RequirementController implements Initializable, IController{
    
    // Logger
    final static Logger logger = Logger.getLogger(RequirementController.class);
    
    ObservableList<Requirement> requirements = FXCollections.observableArrayList();
    
    @FXML
    private JFXTreeTableView<Requirement> requirementTableView;
    @FXML
    private TreeTableColumn<Requirement, String> idCol;
    @FXML
    private TreeTableColumn<Requirement, String> requirementCol;
    
    int id; // Temporary requirement id. Increase for each eligible requirement
    
    public RequirementController() {
        
    }
    
    /**
     * Cross-combine topic words to make requirements from unfamiliar VERB-NOUN or NOUN-VERB pair  
     * @param sys
     * @param topics
     */
    public void constructRequirements(SoftwareSystem sys, ObservableList<Topic> topics) {
        // Reset requirement id every time constructing requirements
        id = 0;
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
                combineTopicWords(sys, tw1, tw2);
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
        if (Utils.isNoun(tw1.getWord().getPos()) && Utils.isVerb(tw2.getWord().getPos())) {
            // Switch part-of-speeches of tw1 and tw2
            // tw1 now becomes a VERB and tw2 plays a role as a NOUN
            r = new Requirement(String.valueOf(++id), sys, tw1.getWord(), tw2.getWord());
        } else if (Utils.isVerb(tw1.getWord().getPos()) && Utils.isNoun(tw2.getWord().getPos())) {
            // Switch part-of-speeches of tw1 and tw2
            // tw1 now becomes a NOUN and tw2 plays a role as a VERB
            r = new Requirement(String.valueOf(++id), sys, tw2.getWord(), tw1.getWord());
        } else {
            logger.info("Invalid word selection: Should be a VERB-NOUN or NOUN-VERB pair. \n Actual: \n +tw1's: "
                    + tw1.getWord().getPos() + "\n+tw2's: " + tw2.getWord().getPos());
            return;
        }
        requirements.add(r);
    }
    
    private void setCellValueRequirementId() {
        idCol.setCellValueFactory(
                param -> new ReadOnlyStringWrapper(param.getValue().getValue().getId()));
        idCol.setStyle("-fx-alignment: center;");
    }
    
    private void setCellValueRequirement() {
        requirementCol.setCellValueFactory(
                param -> new ReadOnlyStringWrapper(param.getValue().getValue().toString()));
        requirementCol.setStyle("-fx-alignment: center-left;");
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

    @Override
    public void constructTableView() {
        TreeItem<Requirement> root = new RecursiveTreeItem<Requirement>(requirements, RecursiveTreeObject::getChildren);
        requirementTableView.setRoot(root);
    }

    @Override
    public void initializeCellValues() {
        setCellValueRequirementId();
        setCellValueRequirement();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize tree table view
        requirements = FXCollections.observableArrayList();
        TreeItem<Requirement> root = new RecursiveTreeItem<Requirement>(requirements, RecursiveTreeObject::getChildren);
        requirementTableView.setRoot(root);
        requirementTableView.setShowRoot(false);
        requirementTableView.setEditable(true);
//        constructTableView();
        initializeCellValues();
    }

    @Override
    public void makeNewView(View target, String title, String url) {
        
    }
}
