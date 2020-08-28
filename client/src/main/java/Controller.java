import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public ListView<String> listView;
    public ListView<String> listFiles;
    public TextField text;
    public Button send;
    private Socket socket;
    private static DataInputStream is;
    private static DataOutputStream os;
    private String clientPath = "client/ClientStorage/";

    public static void stop() {
        try {
            os.writeUTF("quit");
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(ActionEvent actionEvent) {
        String message = text.getText();
        try {
            os.writeUTF(message);
            os.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        text.clear();
    }

    public void sendFile(ActionEvent actionEvent) throws IOException {
        os.writeUTF("sendFiles#" + text.getText ());
        is = new DataInputStream (new FileInputStream(clientPath + text.getText ()));
        os = new DataOutputStream (socket.getOutputStream());
        byte[] byteArray = new byte[8192];
        int i;
        while ((i = is.read(byteArray)) != -1){
            os.write(byteArray,0,i);
        }
        os.flush ();
        is = new DataInputStream(socket.getInputStream());
        text.clear ();
    }

    public void initialize(URL location, ResourceBundle resources) {
        text.setOnAction(this::sendMessage);
        File dir = new File(clientPath);
        for (File file : Objects.requireNonNull (dir.listFiles ())) {
            listFiles.getItems().add(file.getName());
        }
        try {
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                while (true) {
                    try {
                        listFiles.getSelectionModel().selectedItemProperty().addListener(
                                (observable, oldValue, newValue) ->
                                        text.setText(newValue));
                        String message = is.readUTF ();
                        if (message.equals ("quit")) {
                            break;
                        }
                        Platform.runLater (() -> listView.getItems ().add (message));
                    } catch (EOFException e) {
                    } catch (IOException e) {
                        e.printStackTrace ();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}