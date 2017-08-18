package org.re.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.re.common.Message;
import org.re.common.SoftwareSystem;
import org.re.common.View;
import org.re.scrape.BaseScraper;
import org.re.scrape.FirefoxScraper;
import org.re.scrape.MylynScraper;
import org.re.utils.AlertFactory;
import org.re.utils.ExporterUtils;
import org.re.utils.StageFactory;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

/**
 * Setup system and range of issue ids to generate requirements
 * 
 * @author Anh Quoc Do
 *
 */
public class SetupController extends BaseController implements Initializable, IController {
    // Attributes
    @FXML private JFXComboBox<String> systemSelectionCB;    // Selected system to generate requirements
    @FXML private JFXTextField fromId;                      // Starting issue id to start scraping
    @FXML private JFXTextField toId;                        // Ending issue id
    // Buttons
    @FXML private JFXButton skipSetupButton;
    @FXML private JFXButton confirmSetupButton;
    
    private SoftwareSystem system = SoftwareSystem.FIREFOX;
    
    static final Logger logger = Logger.getLogger(SetupController.class);
    /**
     * 
     */
    public SetupController() {
        
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize available systems for generating requirements
        ObservableList<String> systems = FXCollections.observableArrayList("Firefox", "Mylyn");
        systemSelectionCB.setItems(systems);
        systemSelectionCB.getSelectionModel().selectFirst();
        systemSelectionCB.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!oldValue.equals(newValue)) {
                    switch (newValue) {
                        case "Firefox":
                            system = SoftwareSystem.FIREFOX;
                            break;
                        case "Mylyn":
                            system = SoftwareSystem.MYLYN;
                            break;
                        default:
                            system = SoftwareSystem.FIREFOX;
                            break;
                    }
                }
            }
            
        });
        
        // Only accept numeric characters when users enter issue id numbers
        fromId.textProperty().addListener(acceptNumericOnly(fromId));
        toId.textProperty().addListener(acceptNumericOnly(toId));
    }

    @Override
    public void makeNewView(View target, String title, String url) {
        Stage newStage = StageFactory.generateStage(title);
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(url));
        
        BaseScraper scraper;
        String graphInfo;
        switch (system) {
            case FIREFOX:
                scraper = new FirefoxScraper();
                graphInfo = ExporterUtils.FIREFOX_GRAPH_INFO;
                break;
            case MYLYN:
                scraper = new MylynScraper();
                graphInfo = ExporterUtils.MYLYN_GRAPH_INFO;
                break;
            default:
                throw new IllegalArgumentException("Invalid system: " + system);
        }
        
        Parent root = null;
        try {
            root = (Parent)loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        switch (target) {
            case TOPIC_VIEW:
                // When the confirm button is enabled, all the values are ready to be parsed
                int from = Integer.parseInt(fromId.getText());
                int to = Integer.parseInt(toId.getText());
                TopicModelingController tmController = loader.<TopicModelingController>getController();
                
                // prepare parameters for scraping
                tmController.setSystem(system);
                tmController.setFrom(from);
                tmController.setTo(to);
                tmController.setGraphInfo(graphInfo);
                tmController.setScraper(scraper);
                break;
            case REQUIREMENT_VIEW:
                RequirementController rController = loader.<RequirementController>getController();
                rController.loadRequirements(system);
                break;
            default:
                throw new IllegalArgumentException("View should be either Topic or Requirement!");
        }
        // Show view
        newStage.setScene(new Scene(root));
        newStage.show();
    }

    /*
     * Skip setup. Try to load latest generated requirements list if any
     */
    @FXML
    private void skipSetup(ActionEvent e) {
        // Load previously generated requirements
        makeNewView(View.REQUIREMENT_VIEW, "", REQUIREMENT_VIEW);
    }
    
    /*
     * If user hasn't specified system, use default system (Firefox).
     * If user hasn't specified issues id range, load latest generated requirements
     * Otherwise, scrape comments and software artifacts from issue tracking system and generate new requirements 
     */
    @FXML
    private void confirmSetup(ActionEvent e) {
        // Scrape data and extract new requirements
        if (verifyInputId()) {
            makeNewView(View.TOPIC_VIEW, "Topic Modeling", TOPIC_MODELING_VIEW);
        }
    }
    
    private boolean verifyInputId() {
        Alert alert = null;
        if (fromId.getText().equals("") || toId.getText().equals("")) {
            alert = AlertFactory.generateAlert(AlertType.ERROR, Message.INVALID_ISSUE_ID_RANGE);
            alert.show();
        } else if (Integer.parseInt(fromId.getText()) > Integer.parseInt(toId.getText())) {
            alert = AlertFactory.generateAlert(AlertType.ERROR, Message.INVALID_ISSUE_ID_RANGE2);
            alert.show();
        } else {
            return true;
        }
        return false;
    }
    
    /*
     * Only accept numeric character when user inputs issue id. Disable confirm
     * button if the input value is invalid
     */
    private ChangeListener<String> acceptNumericOnly(JFXTextField tf) {
        return new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    tf.setText(newValue.replaceAll("[^\\d]", ""));
                } else if (!tf.getText().isEmpty()) {
                    confirmSetupButton.setDisable(false);
                } else {
                    confirmSetupButton.setDisable(true);
                }
            }
        };
    }
}
