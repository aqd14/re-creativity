package org.re.controller;

import org.re.common.View;

public interface IController {
    public void constructTableView();
    public void initializeCellValues();
    
    /**
     * Create new stage besides primary one. That means there are more than one views displayed 
     * on the screen.
     * 
     * @param target The view that user wants to switch to
     * @param stageTitle The title of created stage
     * @param url <code>URL</code> to FXML file
     */
    public abstract void makeNewView(View target, String title, String url);
}
