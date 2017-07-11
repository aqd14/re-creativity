/**
 * 
 */
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

import org.re.common.Message;
import org.re.model.Topic;
import org.re.model.TopicWord;
import org.re.model.Word;
import org.re.utils.AlertFactory;

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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

/**
 * @author doquocanh-macbook
 *
 */
public class TopicModelingController implements Initializable {
	// Topic modeling
	private ParallelTopicModel model;
	private InstanceList instances;
	
	// FXML elements associate with GUI
	@FXML private AnchorPane mainAP;
	@FXML private Button browseFileBT;
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
	
	private File SELECTED_FILE; // User selects file to extract topics
	
	private ObservableList<Topic> topics; // List of generated topics
	
	// Default constructor
	public TopicModelingController() {
	
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Load POS tagger
		loadPOSTagger();
		
		browseFileBT.setOnAction(event -> {
			SELECTED_FILE = loadFile();
			if (SELECTED_FILE != null) {
				if (isFileEligible(SELECTED_FILE)) {
					filePathTF.setText(SELECTED_FILE.getAbsolutePath());
				} else {
					Alert alert = AlertFactory.generateAlert(AlertType.WARNING, Message.INVALID_FILE_TYPE);
					alert.show();
					// Re-assign selected file to null
					SELECTED_FILE = null;
					// Clear text represented for selected file
					filePathTF.clear();
				}
			} else {
				// User didn't select any file. Do nothing.
			}
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
	
	@FXML private void generateTopics() {
		// If user hasn't selected any file, do nothing
		if (SELECTED_FILE == null) {
			Alert alert = AlertFactory.generateAlert(AlertType.WARNING, Message.NOT_SELECTED_ANY_FILE);
			alert.show();
			return;
		}
		try {
			// Erase current content data for table view first
			topics = FXCollections.observableArrayList();
			// Extract topics and construct table view
			extractTopics(SELECTED_FILE, NUMBER_OF_TOPICS, NUMBER_OF_ITERATIONS, NUMBER_OF_THREADS);
			extractTopicInfo();
			contructTopicTableView();
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Extract needed topic modeling information
	 */
	private void extractTopicInfo() {
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

//			@SuppressWarnings("resource")
//			Formatter out = new Formatter(new StringBuilder(), Locale.US);
			int rank = 0;
			ArrayList<TopicWord> topicWords = new ArrayList<>();
			// Loop through each word in current topic and create a list of words
			while (iterator.hasNext() && rank < 5) {
				IDSorter idCountPair = iterator.next();
				TopicWord tw = new TopicWord();
				tw.setWord(new Word(dataAlphabet.lookupObject(idCountPair.getID()).toString()));
				tw.setWeight(idCountPair.getWeight());
				topicWords.add(tw);
				rank++;
			}

			Topic t = new Topic(topicNumber+1, topicDistribution[topicNumber], topicWords);
			topics.add(t);
		}
	}
	
	/**
	 * 
	 * @param f				Text file contains content needed to extract topics from
	 * @param numTopics		Number of extracted topics
	 * @param numIteration 	Number of iteration running to extract topics. 
          				   	For real applications, use 1000 to 2000 iterations)
	 * @param numThreads	Number of threads running to extract topics
	 * @throws IOException	
	 * @throws URISyntaxException
	 */
	private void extractTopics(File f, int numTopics, int numIteration, int numThreads) throws IOException, URISyntaxException {
		 // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );

        String stoplist = getClass().getClassLoader().getResource("stoplists/en.txt").toExternalForm().replace("file:", "");
        System.out.println("Resources: " + stoplist);
        File stopWords = new File(stoplist);
        
        pipeList.add( new TokenSequenceRemoveStopwords(stopWords, "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        instances = new InstanceList (new SerialPipes(pipeList));

        Reader fileReader = new InputStreamReader(new FileInputStream(f), "UTF-8");
        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                                               3, 2, 1)); // data, label, name fields

        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        model = new ParallelTopicModel(numTopics, 1.0, 0.01);
        model.addInstances(instances);
        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(numThreads);
        model.setNumIterations(numIteration);
        model.estimate();
       
//        int[][] typeTopics = model.getTypeTopicCounts();
//        int[] tokens = model.getTokensPerTopic();
//        System.out.println(model.displayTopWords(1, true));
	}
	
	/**
	 * Load target file from a directory for extracting potential topics is that file
	 * 
	 * @return selected file in disk drive
	 */
	private File loadFile() {
		FileChooser fc = new FileChooser();
		File selectedFile = fc.showOpenDialog(mainAP.getScene().getWindow());//dc.showDialog(mainAP.getScene().getWindow());
		return selectedFile;
	}
	
	/**
	 * Currently only support topic modeling for text file
	 * 
	 * @param f	checked file
	 * @return 	null if not text file
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
	private void contructTopicTableView() {
		TreeItem<Topic> root = new RecursiveTreeItem<Topic>(topics, RecursiveTreeObject::getChildren);
		topicTableView.setRoot(root);
	}
	
	private void initializeCellValues() {
		setCellValueTopicNumber();
		setCellValueTopicDistribution();
		setCellValueTopicDetails();
	}
	
	private void setCellValueTopicNumber() {
		topicNumberCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(String.valueOf(param.getValue().getValue().getTopicNumber())));
		topicNumberCol.setStyle( "-fx-alignment: center;");
	}
	
	private void setCellValueTopicDistribution() {
		topicDistributionCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(String.valueOf(param.getValue().getValue().getTopicDistribution())));
		topicDistributionCol.setStyle( "-fx-alignment: center;");
	}
	
	private void setCellValueTopicDetails() {
		topicDetailsCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().toString()));
		topicDetailsCol.setStyle( "-fx-alignment: center-left;");
	}
	
	/**
	 * <p>
	 * Initialize combo box values that represents for parameters in topic modeling
	 * The parameters include:
	 * <li> Number of generated topics </li>
	 * <li> Number of iterations through training models </li>
	 * <li> Number of threads running </li>
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
	
	private void loadPOSTagger() {
//		InputStream is = getClass().getClassLoader().getResourceAsStream("models/english-left3words-distsim.tagger");
//		MaxentTagger tagger = new MaxentTagger(is);
//		   // The sample string
//        String sample = "The run lasted thirty minutes";
//        String sample2 = "We run three miles everyday";
//        
//        // Output the result
//        System.out.println(tagger.tagString(sample));
//        System.out.println(tagger.tagString("run"));
	}
}