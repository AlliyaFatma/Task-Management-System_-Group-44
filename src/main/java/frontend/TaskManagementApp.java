package frontend;

import javafx.application.Application;
import javafx.stage.Stage;

//Method Overriding
public class TaskManagementApp extends Application {   //Base class inheritance
    @Override
    public void start(Stage primaryStage) {
        new TaskManagerLogin().start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}