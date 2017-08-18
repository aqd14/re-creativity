package org.re.controller;

import java.io.File;

public abstract class BaseController {
    // view resources locations
    public static final String TOPIC_MODELING_VIEW = "view" + File.separatorChar + "TopicModeling.fxml";
    public static final String REQUIREMENT_VIEW = "view"  + File.separatorChar + "RequirementView.fxml";
    public static final String SETUP_VIEW = "view" + File.separatorChar + "Setup.fxml";
    
    public BaseController() {
        
    }
}
