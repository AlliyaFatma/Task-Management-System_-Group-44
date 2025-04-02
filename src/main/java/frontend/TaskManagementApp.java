package frontend;

import javafx.application.Application;
import javafx.stage.Stage;

public class TaskManagementApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        new TaskManagerLogin().start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}