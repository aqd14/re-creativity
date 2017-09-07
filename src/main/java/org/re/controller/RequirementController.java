package org.re.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import org.apache.log4j.Logger;
import org.re.common.Message;
import org.re.common.SoftwareSystem;
import org.re.model.Requirement;
import org.re.model.Topic;
import org.re.model.TopicWord;
import org.re.model.WordPair;
import org.re.utils.AlertFactory;
import org.re.utils.CosineDocumentSimilarity;
import org.re.utils.ExporterUtils;
import org.re.utils.Utils;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

public class RequirementController extends BaseController implements Initializable, ITable{
    
    // Logger
    final static Logger logger = Logger.getLogger(RequirementController.class);
    
    @FXML private StackPane tablePane;
//    @FXML private JFXTreeTableView<Requirement> requirementTableView;
//    @FXML private TreeTableColumn<Requirement, String> idCol;
//    @FXML private TreeTableColumn<Requirement, String> requirementCol;
    @FXML private JFXComboBox<Double> cosineThreshold; // Cosine similarity threshold to filter most unfamiliar word pairs 
    @FXML private JFXButton updateTableBT; // Refresh table with user-selected requirements 
    @FXML private JFXButton finishBT;      // Separately save selected and un-selected requirements to files
    
    // Maintain a list of requirements that user prefers to keep.
    private ObservableList<Requirement> pickedRequirements;
    
    // Maintain a list of requirements that user might not want to keep
    // at the moment but might consider in the future
    private ObservableList<Requirement> unpickedRequirements;
    
    // Cosine similarity threshold to filter unfamiliar word pair
    private double COSINE_THRESHOLD = 0.15; // default value
    
    ObservableList<Requirement> requirements = FXCollections.observableArrayList();
    private HashSet<WordPair> wordPairs = new HashSet<>();
    
    int id; // Temporary requirement id. Increase for each eligible requirement
    
    private double highestCosine = Double.MIN_NORMAL;
    
    public RequirementController() {
        
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        wordPairs = new HashSet<>();
        // Initialize tree table view
        requirements = FXCollections.observableArrayList();
        pickedRequirements = FXCollections.observableArrayList();
        unpickedRequirements = FXCollections.observableArrayList();
//        TreeItem<Requirement> root = new RecursiveTreeItem<Requirement>(requirements, RecursiveTreeObject::getChildren);
//        requirementTableView.setRoot(root);
//        requirementTableView.setShowRoot(false);
//        requirementTableView.setEditable(true);
        
        // Setup property for cosine threshold combobox
        ObservableList<Double> options = FXCollections.observableArrayList(0.1, 0.15, 0.2, 0.3, 0.4);
        cosineThreshold.setItems(options);
        cosineThreshold.getSelectionModel().select(COSINE_THRESHOLD);
        
        // Update requirement list based on the change in cosine similarity
        cosineThreshold.valueProperty().addListener(new ChangeListener<Double>() {
            @Override
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
                if (oldValue != newValue) {
                    logger.info("Change cosine threshold from " + oldValue + " to " + newValue);
                    // Make a copy of list of word pairs and filter out the one with values below threshold
                    // This means that those pairs are the most unfamiliar ones with existing requirements
                    HashSet<WordPair> copy = makeCopyWordPair();
                    removeIfGreaterOrEqual(copy, newValue*getHighestCosine());
                    requirements.clear();
//                    
//                    // Now, ready to generate creative requirements from most unfamiliar word pairs
//                    for (WordPair wp : copy) {
//                        Requirement r = new Requirement(String.valueOf(++id), wp.getSystem(), wp.getVerb().getWord(), wp.getNoun().getWord());
//                        requirements.add(r);
//                    }
//                    // Re-construct table view of new requirements
//                    constructTableView();
                }
            }
        });
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
        calculateCosineSimilarity(system);
        // Filter out those word pairs with cosine <= 0.15*highest_cosine
        // This means that those pairs are the most unfamiliar ones with existing requirements
        removeIfGreaterOrEqual(wordPairs, COSINE_THRESHOLD*getHighestCosine());
        // Now, ready to generate creative requirements from most unfamiliar word pairs
        for (WordPair wp : wordPairs) {
            Requirement r = new Requirement(String.valueOf(++id), wp.getSystem(), wp.getVerb().getWord(), wp.getNoun().getWord());
            requirements.add(r);
        }
        // Serialize requirements
        String path;
        switch (system) {
            case FIREFOX:
                path = ExporterUtils.FIREFOX_REQUIREMENTS_SERIALIZATION;
                break;
            case MYLYN:
                path = ExporterUtils.MYLYN_REQUIREMENTS_SERIALIZATION;
                break;
            default:
                throw new IllegalArgumentException("Invalid system: " + system);
        }
        // Serialize generated requirements
        ExporterUtils.writeSerializedRequirements(path, requirements); 
    }
    
    /**
     * Load previously generated requirements. Usually, this methods will
     * deserialize objects in the directory determined by software system.
     * 
     * @param system selected software system
     */
    public void loadRequirements(SoftwareSystem system) {
        File f;
        switch (system) {
            case FIREFOX:
                f = new File(ExporterUtils.FIREFOX_REQUIREMENTS_SERIALIZATION);
                break;
            case MYLYN:
                f = new File(ExporterUtils.MYLYN_REQUIREMENTS_SERIALIZATION);
                break;
            default:
                throw new IllegalArgumentException("Invalid system: " + system);
        }
        
        // Check if the serialized requirements file already exists
        if (!f.exists()) {
            Alert alert = AlertFactory.generateAlert(AlertType.INFORMATION, Message.NO_REQUIREMENTS_AVAILABLE);
            alert.show();
            return;
        }
        
        requirements = ExporterUtils.readSerializedRequirements(f);
        // Something wrong! Can't read serialized requirement objects
        if (requirements == null) {
            logger.error("Failed loading serialized requirements!");
            Alert alert = AlertFactory.generateAlert(AlertType.ERROR, Message.CANT_LOAD_REQUIREMENTS);
            alert.show();
        }
        // Construct table view with current list of requirements
        constructTableView();
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
     * Calculate cosine similarity for each {@link org.re.model.WordPair} and requirement list 
     * @throws IOException 
     */
    public void calculateCosineSimilarity(SoftwareSystem system) throws IOException {
        String path;
        switch (system) {
            case FIREFOX:
                path = ExporterUtils.FIREFOX_REQUIREMENTS;
                break;
            case MYLYN:
                path = ExporterUtils.MYLYN_REQUIREMENTS;
                break;
            default:
                throw new IllegalArgumentException("Invalid system: " + system);
        }
        
        // Get list of existing requirements
        String requirements = Utils.readFile(path);
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
     * Get highest cosine similarity among word pairs
     * 
     * @return highest cosine of a word pair in the set
     */
    private double getHighestCosine() {
        if (highestCosine == Double.MIN_VALUE) {
            for (WordPair wp : wordPairs) {
                if (wp.getCosineSimilarity() > highestCosine) {
                    highestCosine = wp.getCosineSimilarity();
                }
            }
        }
        return highestCosine;
    }
    
    /**
     * Remove all {@link org.re.model.WordPair} elements that are equal or greater than threshold
     * 
     * @param threshold
     */
    public void removeIfGreaterOrEqual(HashSet<WordPair> wps, double threshold) {
        Predicate<WordPair> pre = p->p.getCosineSimilarity() >= threshold;
        wps.removeIf(pre);
    }
    
    /**
     * Remove all {@link org.re.model.WordPair} elements that are lesser than threshold
     * 
     * @param threshold
     */
    public void removeIfLesser(HashSet<WordPair> wps, double threshold) {
        Predicate<WordPair> pre = p->p.getCosineSimilarity() < threshold;
        wps.removeIf(pre);
    }
    
//    private void setCellValueRequirementId() {
//        idCol.setCellValueFactory(
//                param -> new ReadOnlyStringWrapper(param.getValue().getValue().getId()));
//        idCol.setStyle("-fx-alignment: center;");
//    }
//    
//    private void setCellValueRequirement() {
//        requirementCol.setCellValueFactory(
//                param -> new ReadOnlyStringWrapper(param.getValue().getValue().toString()));
//        requirementCol.setStyle("-fx-alignment: center-left;");
//    }
    
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
        TableView<Requirement> table = createRequirementTable();
//        requirements = selectRequirements();
        table.setItems(requirements);
        
        // Add table to the pane for displaying
        tablePane.getChildren().clear();
        tablePane.getChildren().add(table);
    }
    
//    @Override
//    public void initializeCellValues() {
//        setCellValueRequirementId();
//        setCellValueRequirement();
//    }
    
    private ObservableList<Requirement> selectRequirements() {
        boolean getPicked = true;
        boolean getUnpicked = true;
        boolean getBoth = true;
        if (getPicked) {
            return pickedRequirements;
        }
        
        if (getUnpicked) {
            return unpickedRequirements;
        }
        
        if (getBoth) {
            pickedRequirements.addAll(unpickedRequirements);
        }
        
        return pickedRequirements;
    }
    
    @SuppressWarnings("unchecked")
    private TableView<Requirement> createRequirementTable() {

        TableView<Requirement> table = new TableView<>();
        table.setEditable(true);
        
        TableColumn<Requirement, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getId()));
        idCol.setPrefWidth(100);
        
        TableColumn<Requirement, String> requirementCol = new TableColumn<>("Requirements");
        requirementCol.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().toString()));
        requirementCol.setPrefWidth(500);
        
        TableColumn<Requirement, Boolean> selectReqCol = new TableColumn<>();
        selectReqCol.setGraphic(new CheckBox());
        
        selectReqCol.setCellValueFactory(new PropertyValueFactory<Requirement, Boolean>("selected"));
        // Add event handler when users select a check box on table
        selectReqCol.setCellFactory(CheckBoxTableCell.forTableColumn(new Callback<Integer, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(Integer index) {
                Requirement item = table.getItems().get(index);
//                if (item != null) {
                    // Add/Remove item from the selected list
//                    if (true == item.isSelected()) {
//                        pickedRequirements.add(item);
//                    } else {
//                        pickedRequirements.remove(item);
//                    }
//                    return item.selectedProperty();
//                } 
                return new SimpleBooleanProperty(false);
            }
        }));
        selectReqCol.setEditable(true);

        table.getColumns().addAll(selectReqCol, idCol, requirementCol);
        return table;
    }
    
    private HashSet<WordPair> makeCopyWordPair() {
        HashSet<WordPair> copy = new HashSet<>(wordPairs);
        return copy;
    }
    
    /**
     * Refresh table view of requirements
     * 
     * @param performingTransactions
     */
//    private void refreshTableView() {
//        for (Requirement r : requirements) {
//            if (requirementTableView.getItems().contains(t)) { // Remove sold stock (transaction)
//                portfolioTable.getItems().remove(t);
//            }
//        }
//        // Refresh transaction history by pulling out data from database again and redraw table
//        // TODO: It might take time, consider the way to update table without accessing database
//        initTransactionHistory();
//        initTransactionSummary();
//    }
}
