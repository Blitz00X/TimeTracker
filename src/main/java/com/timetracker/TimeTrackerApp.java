package com.timetracker;

import com.timetracker.db.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TimeTrackerApp extends Application {

    @Override
    public void init() {
        DatabaseInitializer.initialize();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/timetracker/view/main-view.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("TimeTracker+");
        primaryStage.setScene(new Scene(root));
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
