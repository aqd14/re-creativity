package org.re.utils;

import javafx.stage.Stage;

public class StageFactory {
    /**
     * Generate new stage
     * @param title Stage's title
     * @return new stage
     */
    public static Stage generateStage(String title) {
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setResizable(false);
        return stage;
    }
}
