package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/resources/views/authorization.fxml"));
        primaryStage.setTitle("Demo Chat");
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(300);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
    }

    public static void main(String[] args) {
        launch(args);
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
