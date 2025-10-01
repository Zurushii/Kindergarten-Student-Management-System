package com.example.kindergarden;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load FXML layout file for the main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student-view.fxml"));
            
            // Load the root pane from FXML
            BorderPane root = loader.load();

            // Create a scene using the root pane with specific width and height
            Scene scene = new Scene(root, 1050, 850);

            // Set the title of the main application window
            primaryStage.setTitle("Kindergarten Student Management System");
            
            // Set the scene to the primary stage
            primaryStage.setScene(scene);
            
            // Display the primary stage
            primaryStage.show();

        } catch (Exception e) {
            // Print any exceptions that occur during loading or setup
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}
