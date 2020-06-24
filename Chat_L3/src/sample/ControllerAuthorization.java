package sample;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import sample.ServerListener.ServerListener;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ControllerAuthorization implements Initializable {
    @FXML public TextField loginField;
    @FXML public PasswordField passField;
    private ServerListener serverListener;
    private ControllerChatView controller;

    final PseudoClass errorClass = PseudoClass.getPseudoClass("error");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> loginField.requestFocus());
    }

    public void singUp(javafx.event.ActionEvent actionEvent) {
        String login = loginField.getText();
        String pass = passField.getText();

        serverListenerLauncher("/singUp " + login + " " + pass);
        ServerListener.setUserName(login);
    }

    public void logIn(javafx.event.ActionEvent actionEvent) {
        String login = loginField.getText();
        String pass = passField.getText();

        serverListenerLauncher("/logIn " + login + " " + pass);
        ServerListener.setUserName(login);
    }

    public void failedAuthorization() {
        loginField.pseudoClassStateChanged(errorClass, true);
        passField.pseudoClassStateChanged(errorClass, true);
    }
    public void loadChatScreen() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/resources/views/sample.fxml"));
                    Parent window = (Pane) fmxlLoader.load();
                    controller = fmxlLoader.<ControllerChatView>getController();
                    ServerListener.setController(controller);
                    Stage stage = (Stage) loginField.getScene().getWindow();
                    stage.setResizable(true);
                    stage.setOnCloseRequest(e -> {
                        try {
                            controller.saveHistory();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                        Platform.exit();
                        System.exit(0);
                    });
                    Scene scene = new Scene(window);
                    stage.setScene(scene);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void serverListenerLauncher(String authString) {
        new Thread( () -> {
            serverListener = new ServerListener(authString, this);
        }).start();
    }
}
