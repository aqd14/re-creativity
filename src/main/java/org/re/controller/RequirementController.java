package org.re.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.function.Predicate;

import org.apache.log4j.Logger;
import org.re.common.SoftwareSystem;
import org.re.common.View;
import org.re.model.Requirement;
import org.re.model.Topic;
import org.re.model.TopicWord;
import org.re.model.WordPair;
import org.re.utils.CosineDocumentSimilarity;
import org.re.utils.SerializationUtils;
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
    
    // List of FireFox requirements
    private final String FIREFOX_REQUIREMENTS = "requirements/firefox_requirements.txt";
    // Serialized requirements file
    private final String SERIALIZED_REQUIREMENTS = "requirements.ser";
    // Cosine similarity threshold to filter unfamiliar word pair
    private final double COSINE_THRESHOLD = 0.15;
    
    ObservableList<Requirement> requirements = FXCollections.observableArrayList();
    private HashSet<WordPair> wordPairs = new HashSet<>();
    
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
    public void buildWordPairs(SoftwareSystem sys, ObservableList<Topic> topics) {
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
                HashSet<WordPair> wp = exploreTopicWords(sys, t1.getTopicWords(), t2.getTopicWords());
                wordPairs.addAll(wp);
            }
        }
    }
    
    /**
     * Iterate through the word list to select the pair of most dominant nouns and verbs
     * @param sys
     * @param wordList1
     * @param wordList2
     */
    private HashSet<WordPair> exploreTopicWords(SoftwareSystem sys, ArrayList<TopicWord> wordList1, ArrayList<TopicWord> wordList2) {
        HashSet<WordPair> wps = new HashSet<>();
        for (TopicWord tw1 : wordList1) {
            for (TopicWord tw2 : wordList2) {
                WordPair wp = makeWordPair(sys, tw1, tw2);
                if (wp != null) {
                    wps.add(wp);
                }
            }
        }
        return wps;
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
    public WordPair makeWordPair(SoftwareSystem sys, TopicWord tw1, TopicWord tw2) {
        WordPair wp = null;
        if (Utils.isNoun(tw1.getWord().getPos()) && Utils.isVerb(tw2.getWord().getPos())) {
            // Switch part-of-speeches of tw1 and tw2
            // tw1 now becomes a VERB and tw2 plays a role as a NOUN
//            r = new Requirement(String.valueOf(++id), sys, tw1.getWord(), tw2.getWord());
            wp = new WordPair(sys, tw1, tw2);
        } else if (Utils.isVerb(tw1.getWord().getPos()) && Utils.isNoun(tw2.getWord().getPos())) {
            // Switch part-of-speeches of tw1 and tw2
            // tw1 now becomes a NOUN and tw2 plays a role as a VERB
//            r = new Requirement(String.valueOf(++id), sys, tw2.getWord(), tw1.getWord());
            wp = new WordPair(sys, tw2, tw1);
        } else {
            logger.info("Invalid word selection: Should be a VERB-NOUN or NOUN-VERB pair. \n Actual: \n +tw1's: "
                    + tw1.getWord().getPos() + "\n+tw2's: " + tw2.getWord().getPos());
//            return null;
        }
//        requirements.add(r);
        return wp;
    }
    
    /**
     * Convert from set of most unfamiliar word pairs to corresponding requirements
     * 
     * @param system
     * @param topics
     * @throws IOException 
     */
    public void toRequirements(SoftwareSystem system, ObservableList<Topic> topics) throws IOException {
        // Build flipping noun-verb word pairs from topics list
        buildWordPairs(system, topics);
        // Calculate cosine similarity for each word pair
        calculateCosineSimilarity();
        // Filter out those word pairs with cosine <= 0.15*highest_cosine
        // This means that those pairs are the most unfamiliar ones with existing requirements
        removeIfGreaterOrEqual(COSINE_THRESHOLD*getHighestCosine());
        // Now, ready to generate creative requirements from most unfamiliar word pairs
        for (WordPair wp : wordPairs) {
            Requirement r = new Requirement(String.valueOf(++id), wp.getSystem(), wp.getVerb().getWord(), wp.getNoun().getWord());
            requirements.add(r);
        }
        // Serialize requirements
        StringBuilder pathBuilder = new StringBuilder();
        pathBuilder.append(getClass().getClassLoader().getResource("serialization").toString().replace("file:", ""));
        pathBuilder.append(File.separatorChar).append(SERIALIZED_REQUIREMENTS);
        SerializationUtils.writeSerializedRequirements(pathBuilder.toString(), requirements);
    }
    
    /**
     * Calculate cosine similarity for each {@link org.re.model.WordPair} and requirement list 
     * @throws IOException 
     */
    public void calculateCosineSimilarity() throws IOException {
        // Get list of existing requirements
        String requirements = readFile(getClass().getClassLoader().getResource(FIREFOX_REQUIREMENTS).getPath());
        // Only calculate requirement vector once
        // Iterate through each word pair to calculate cosine tf-idf
        CosineDocumentSimilarity cds = new CosineDocumentSimilarity(requirements);
        for (WordPair wp : wordPairs) {
            StringBuilder bd = new StringBuilder();
            String verb = wp.getVerb().getWord().getContent();
            String noun = wp.getNoun().getWord().getContent();
            bd.append(verb).append(" ").append(noun);
            // Calculate cosine similarity for each word pair
            int id = cds.addDocument(CosineDocumentSimilarity.QUERY_FIELD, bd.toString());
            double cosine = cds.calculateCosineSimilarity(id);
            wp.setCosineSimilarity(cosine);
        }
    }
    
    /**
     * Read file's content and store into a string
     * 
     * @param path File path
     * @return  Content of file as a string
     * @throws IOException
     */
    public String readFile(String path) {
        Scanner sc = null;
        StringBuilder bd = new StringBuilder();
        try (FileInputStream inputStream = new FileInputStream(path)){
            sc = new Scanner(inputStream, "UTF-8");
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                bd.append(line).append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bd.toString();
    }
    
    /**
     * Get highest cosine similarity among word pairs
     * 
     * @return highest cosine of a word pair in the set
     */
    private double getHighestCosine() {
        double highest = -1;
        for (WordPair wp : wordPairs) {
            if (wp.getCosineSimilarity() > highest) {
                highest = wp.getCosineSimilarity();
            }
        }
        return highest;
    }
    
    /**
     * Remove all {@link org.re.model.WordPair} elements that are equal or greater than threshold
     * 
     * @param threshold
     */
    public void removeIfGreaterOrEqual(double threshold) {
        Predicate<WordPair> pre = p->p.getCosineSimilarity() >= threshold;
        wordPairs.removeIf(pre);
    }
    
    /**
     * Remove all {@link org.re.model.WordPair} elements that are lesser than threshold
     * 
     * @param threshold
     */
    public void removeIfLesser(double threshold) {
        Predicate<WordPair> pre = p->p.getCosineSimilarity() < threshold;
        wordPairs.removeIf(pre);
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
        wordPairs = new HashSet<>();
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
