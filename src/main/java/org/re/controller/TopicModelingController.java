package org.re.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.re.common.Message;
import org.re.common.SoftwareSystem;
import org.re.common.View;
import org.re.model.Topic;
import org.re.model.TopicWord;
import org.re.model.Word;
import org.re.scrape.BaseScraper;
import org.re.scrape.model.AdjacencyMatrixGraph;
import org.re.scrape.model.Product;
import org.re.scrape.model.StakeHolderGraph;
import org.re.utils.AlertFactory;
import org.re.utils.ExporterUtils;
import org.re.utils.StageFactory;
import org.re.utils.Utils;
import org.re.utils.cluster.Cluster;
import org.re.utils.cluster.KMedoids;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * @author doquocanh-macbook
 *
 */
public class TopicModelingController extends BaseController implements Initializable, IController, ITable {
    // Topic modeling
    private ParallelTopicModel model;
    private InstanceList instances;

    // FXML elements associate with GUI
    @FXML private AnchorPane mainAP;
    @FXML private Button browseFileBT;
    @FXML private JFXButton generateTopicBT;
    
    @FXML private JFXTextField filePathTF;

    // LDA topic modeling parameters
    private int NUMBER_OF_TOPICS = 10;
    private int NUMBER_OF_ITERATIONS = 100;
    private int NUMBER_OF_THREADS = 2;

    @FXML private JFXComboBox<Integer> topicCB;
    @FXML private JFXComboBox<Integer> iterationCB;
    @FXML private JFXComboBox<Integer> threadCB;

    // Topic tree table view
    @FXML private JFXTreeTableView<Topic> topicTableView;
    @FXML private TreeTableColumn<Topic, String> topicNumberCol;
    @FXML private TreeTableColumn<Topic, String> topicDistributionCol;
    @FXML private TreeTableColumn<Topic, String> topicDetailsCol;

    private File selectedFile; // User selects file to extract topics

    private ObservableList<Topic> topics; // List of generated topics
    
    private BaseScraper webScraper;
    
    private SoftwareSystem system;
    
    private int from; // Starting issue id
    private int to;   // Ending issue id
    
    private String graphInfo; // File path of graph info file
    
    static final Logger logger = Logger.getLogger(TopicModelingController.class);

    // Default constructor
    public TopicModelingController() {
        topics = FXCollections.observableArrayList();
    }
    
    public ObservableList<Topic> getTopics() {
        return topics;
    }
    
    public void setSelectedFile(File file) {
        this.selectedFile = file;
    }
    
    public void setScraper(BaseScraper scraper) {
        this.webScraper = scraper;
    }
    
    public void setFrom(int from) {
        this.from = from;
    }
    
    public void setTo(int to) {
        this.to = to;
    }
    
    public void setGraphInfo(String graphInfo) {
        this.graphInfo = graphInfo;
    }
    
    public void setSystem(SoftwareSystem system) {
        this.system = system;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        browseFileBT.setOnAction(event -> {
            selectedFile = loadFile();
            if (selectedFile != null) {
                if (isFileEligible(selectedFile)) {
                    filePathTF.setText(selectedFile.getAbsolutePath());
                } else {
                    Alert alert = AlertFactory.generateAlert(AlertType.WARNING, Message.INVALID_FILE_TYPE);
                    alert.show();
                    // Re-assign selected file to null
                    selectedFile = null;
                    // Clear text represented for selected file
                    filePathTF.clear();
                }
            } else {
                // User didn't select any file. Do nothing.
            }
        });
        
        generateTopicBT.setOnAction(event -> {
            Task<Product> task = new Task<Product>() {
                @Override
                protected Product call() throws Exception {
                    webScraper.scrape(from, to);
                    // Export scraped data to files
                    ExporterUtils.exportAll(webScraper);
                    return webScraper.getProduct();
                }
            };
            
            task.setOnSucceeded(e -> {
                topics = FXCollections.observableArrayList();
                constructTableView();
                
                Product p = task.getValue();
                // Construct stakeholders graph, cluster as related groups then collect
                // all comments, issue description made by stakeholders
                StakeHolderGraph shGraph;
                try {
                    shGraph = new StakeHolderGraph(graphInfo);
                    KMedoids<AdjacencyMatrixGraph> kmedoids = new KMedoids<>(3, 10);
                    ArrayList<Cluster<Integer>> clusters = kmedoids.cluster((AdjacencyMatrixGraph)shGraph.G());
                    
                    StringBuilder bd = new StringBuilder();
                    // Determine part-of-speech for each topic words
                    PosController pc = new PosController();
                    ArrayList<Word> words = new ArrayList<Word>();
                    
                    for (Cluster<Integer> cluster : clusters) {
                        logger.info(cluster);
                        bd.append(p.toCorpus(cluster, shGraph));
                        selectedFile = Utils.makeTempFile(bd.toString());
                        // Extract topics and construct table view
                        extractTopics(selectedFile, NUMBER_OF_TOPICS, NUMBER_OF_ITERATIONS, NUMBER_OF_THREADS);
                        extractTopicInfo(5);
                        words.addAll(pc.getTaggedWords(selectedFile));
                    }
                    
                    for (Topic t : topics) {
                        pc.assignPOS(t, words);
                    }
                    // Generate list of requirements from extracted topics
                    makeNewView(View.REQUIREMENT_VIEW, "", REQUIREMENT_VIEW);
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            });
            
            new Thread(task).start();
        });
        
        // Initialize topic modeling parameters
        initializeParameters();

        // Initialize tree table view
        topics = FXCollections.observableArrayList();
        TreeItem<Topic> root = new RecursiveTreeItem<Topic>(topics, RecursiveTreeObject::getChildren);
        topicTableView.setRoot(root);
        topicTableView.setShowRoot(false);
        topicTableView.setEditable(true);
        initializeCellValues();
    }

    /**
     * <p>
     * Extract topic modeling information. 
     * Each TopicWord object contain the words and their corresponding weights
     * </p>
     * @param wordsPerTopic Number of words per topic to be extracted
     */
    private void extractTopicInfo(int wordsPerTopic) {
        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();
        // Estimate the topic distribution of the first instance,
        // given the current Gibbs state.
        double[] topicDistribution = model.getTopicProbabilities(0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

        // Show top 5 words in topics with proportions for the first document
        for (int topicNumber = 0; topicNumber < NUMBER_OF_TOPICS; topicNumber++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topicNumber).iterator();

            // @SuppressWarnings("resource")
            // Formatter out = new Formatter(new StringBuilder(), Locale.US);
            int rank = 0;
            ArrayList<TopicWord> topicWords = new ArrayList<>();
            // Loop through each word in current topic and create a list of
            // words
            while (iterator.hasNext() && rank < wordsPerTopic) {
                IDSorter idCountPair = iterator.next();
                TopicWord tw = new TopicWord();
                tw.setWord(new Word(dataAlphabet.lookupObject(idCountPair.getID()).toString()));
                tw.setWeight(idCountPair.getWeight());
                topicWords.add(tw);
                rank++;
            }

            Topic t = new Topic(topicNumber + 1, topicDistribution[topicNumber], topicWords);
            topics.add(t);
        }
    }

    /**
     * 
     * @param f
     *            Text file contains content needed to extract topics from
     * @param numTopics
     *            Number of extracted topics
     * @param numIteration
     *            Number of iteration running to extract topics. For real
     *            applications, use 1000 to 2000 iterations)
     * @param numThreads
     *            Number of threads running to extract topics
     * @throws IOException
     * @throws URISyntaxException
     */
    private void extractTopics(File f, int numTopics, int numIteration, int numThreads)
            throws IOException, URISyntaxException {
        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));

//        String stoplist = getClass().getClassLoader().getResource("stoplists/en.txt").getFile();
        File stopWords = Utils.loadResourcesAsFile(this.getClass(), "/stoplists/en.txt"); //new File(stoplist);

        pipeList.add(new TokenSequenceRemoveStopwords(stopWords, "UTF-8", false, false, false));
        pipeList.add(new TokenSequence2FeatureSequence());

        instances = new InstanceList(new SerialPipes(pipeList));

        Reader fileReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
        // data, label, name, fields
        instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1));

        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        // Note that the first parameter is passed as the sum over topics, while
        // the second is the parameter for a single dimension of the Dirichlet prior.
        model = new ParallelTopicModel(numTopics, 1.0, 0.01);
        model.addInstances(instances);
        // Use two parallel samplers, which each look at one half the corpus and 
        // combine statistics after every iteration.
        model.setNumThreads(numThreads);
        model.setNumIterations(numIteration);
        model.estimate();
    }

    /**
     * Load target file from a directory for extracting potential topics is that
     * file
     * 
     * @return selected file in disk drive
     */
    private File loadFile() {
        FileChooser fc = new FileChooser();
        File selectedFile = fc.showOpenDialog(mainAP.getScene().getWindow());// dc.showDialog(mainAP.getScene().getWindow());
        return selectedFile;
    }

    /**
     * Currently only support topic modeling for text file
     * 
     * @param f checked file
     * @return null if not text file
     */
    private boolean isFileEligible(File f) {
        String filename = f.getName();
        if (filename.endsWith(".txt"))
            return true;
        return false;
    }

    /**
     * Generate topic modeling and initialize contents of topic tree table view
     */
    @Override
    public void constructTableView() {
        TreeItem<Topic> root = new RecursiveTreeItem<Topic>(topics, RecursiveTreeObject::getChildren);
        topicTableView.setRoot(root);
    }
    
    public void initializeCellValues() {
        setCellValueTopicNumber();
        setCellValueTopicDistribution();
        setCellValueTopicDetails();
    }

    private void setCellValueTopicNumber() {
        topicNumberCol.setCellValueFactory(
                param -> new ReadOnlyStringWrapper(String.valueOf(param.getValue().getValue().getTopicNumber())));
        topicNumberCol.setStyle("-fx-alignment: center;");
    }

    private void setCellValueTopicDistribution() {
        topicDistributionCol.setCellValueFactory(
                param -> new ReadOnlyStringWrapper(String.valueOf(param.getValue().getValue().getTopicDistribution())));
        topicDistributionCol.setStyle("-fx-alignment: center;");
    }

    private void setCellValueTopicDetails() {
        topicDetailsCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().toString()));
        topicDetailsCol.setStyle("-fx-alignment: center-left;");
    }

    /**
     * <p>
     * Initialize combo box values that represents for parameters in topic
     * modeling The parameters include:
     * <li>Number of generated topics</li>
     * <li>Number of iterations through training models</li>
     * <li>Number of threads running</li>
     */
    private void initializeParameters() {
        // Initialize topic modeling parameters
        ObservableList<Integer> topicOptions = FXCollections.observableArrayList(10, 15, 20, 30, 50);
        topicCB.setItems(topicOptions);
        topicCB.setValue(NUMBER_OF_TOPICS); // Default value
        // Add listener
        topicCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                NUMBER_OF_TOPICS = newValue;
            }
        });

        ObservableList<Integer> iterationOptions = FXCollections.observableArrayList(50, 100, 500, 1000, 2000);
        iterationCB.setItems(iterationOptions);
        iterationCB.setValue(NUMBER_OF_ITERATIONS); // Default value
        // Add listener
        iterationCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                NUMBER_OF_ITERATIONS = newValue;
            }
        });

        ObservableList<Integer> threadOptions = FXCollections.observableArrayList(1, 2, 3, 4, 5);
        threadCB.setItems(threadOptions);
        threadCB.setValue(NUMBER_OF_THREADS); // Default value
        // Add listener
        threadCB.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                NUMBER_OF_THREADS = newValue;
            }
        });
    }

    @Override
    public void makeNewView(View target, String title, String url) {
        Stage newStage = StageFactory.generateStage(title);
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(url));
        Parent root = null;
        try {
            root = (Parent)loader.load();
        } catch (IOException e) {
            logger.error("Could not load url: " + url);
            e.printStackTrace();
            return;
        }
        switch(target) {
            case REQUIREMENT_VIEW:
                RequirementController requirementController = loader.<RequirementController>getController();
                try {
                    requirementController.toRequirements(system, topics);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                requirementController.constructTableView();
                break;
            default:
                return;
        }
        newStage.setScene(new Scene(root));
        newStage.show();
    }
}
