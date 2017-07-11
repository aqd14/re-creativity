package org.re;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
	    	FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/TopicModeling.fxml"));
	        Parent root = (Parent)loader.load();
	        primaryStage.setTitle("RE Creativity");
	        primaryStage.setScene(new Scene(root));
//	        primaryStage.setResizable(false);
	        primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
