package sample;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import sample.ServerListener.ServerListener;

import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.*;

public class ControllerChatView implements Initializable {

    @FXML private VBox chatField;
    @FXML private VBox contactsPane;
    @FXML private VBox mainChatPane;
    @FXML private HBox message_entry_pane;
    @FXML private HBox mainPane;
    @FXML private HBox chatTopPanel;
    @FXML private BorderPane userPanel;
    @FXML private TextArea userMessageField;
    @FXML private TextField fieldSearch;
    @FXML private ScrollPane chatScrollPane;
    @FXML private MenuItem logOut;
    private ObservableList<Node> messageBubbles = FXCollections.observableArrayList();
    private Connection conn;
    private String userName;
    private PreparedStatement ps;
    private static boolean acitveSessionFlag = true;
    Separator separator;

    private List<String> comands = Arrays.asList("/status", "/w");
    private List<String> contacts;
    private List<ContactTab> listContactTabs;
    public static List<String> unsents = new ArrayList<>();

    private static ContactTab currentActivContact = null;
    private static int tmp = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Resize main elements 1 x 1 when user try resize window.. Need realize in fxml...
        contactsPane.prefWidthProperty().bind(mainPane.widthProperty().divide(1));
        chatField.prefWidthProperty().bind(mainPane.widthProperty().divide(1));
        chatField.prefHeightProperty().bind(mainPane.heightProperty().divide(1));
        mainChatPane.prefHeightProperty().bind(chatField.heightProperty().divide(1));
        chatScrollPane.prefHeightProperty().bind(chatField.heightProperty().divide(1));
        message_entry_pane.prefWidthProperty().bind(chatField.widthProperty().divide(1));
        chatTopPanel.prefWidthProperty().bind(chatField.widthProperty().divide(1));
        // End block resizable-----------------------------------------------------------------

        Bindings.bindContentBidirectional(messageBubbles, mainChatPane.getChildren());
        userName = ServerListener.getUserName();

        try {
            loadContacts();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        contactsListener();
        Platform.runLater(() -> userMessageField.requestFocus());

        userMessageField.setOnKeyPressed(e -> sendMessageByKeyboard(e));

        /*Drawing user avatar.. */
        Circle circle = new Circle(24);
        User ownerUser = new User();
        ownerUser.setImage("/resources/images/user.png");
        ImagePattern pattern = new ImagePattern(new Image(ownerUser.getImage()));
        circle.setFill(pattern);
        userPanel.setLeft(circle);
    }

    private void sendUserMessage() {
        if (!userMessageField.getText().equals("")) {
            String userMessage = userMessageField.getText().trim();
            Message message = new Message();
            message.setName(currentActivContact.getName());
            message.setMessage(userMessage);
            currentActivContact.getMessageBubbles().add(new MessageBubble(message, SpeechDirection.RIGHT));
            userMessageField.setText("");
            userMessage = "/w " + currentActivContact.getName() + " [%msg]" + message.getMessage();
            if (ServerListener.socket == null || ServerListener.socket.isClosed()) {
               unsents.add(userMessage);
            } else {
              ServerListener.sendMsg(userMessage);
            }
        }
    }

    public void receivedMessage(String msg) {
        for (String comand : comands) {
            if (msg.startsWith(comand)) {
                switch (comand) {
                    case "/status":
                        setStatusContacts(msg);
                        break;
                    case "/w":
                        showMessageOnScreen(msg);
                        break;
                }
            }
        }
    }

    private void receivedMessageShow(String msg, ContactTab contact) {
        Message message = new Message();
        message.setName(contact.getName());
        message.setMessage(msg);
        if (!message.getMessage().equals("")) {
            new Thread( () -> {
                Platform.runLater(() -> {
                    contact.getMessageBubbles().add(new MessageBubble(message, SpeechDirection.LEFT));
                    });
            }).start();
                if (currentActivContact == null || (!contact.getName().equals(currentActivContact.getName()))) {
                    new Thread(() ->
                    {
                        Platform.runLater(() -> {
                            contact.setNewMessageFlagVisible(true);
                        });
                    }).start();
                }
            userMessageField.setText("");
        }
    }

    public void sendMessageByButton(javafx.event.ActionEvent actionEvent) {
        if (currentActivContact != null) {
            sendUserMessage();
        }
    }

    // Send message on press Alt + Enter
    public void sendMessageByKeyboard(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER && e.isAltDown()) {
            if (currentActivContact != null) {
                sendUserMessage();
            }
        }
    }


    /**
     *  Upadate the chat`s pane with message for showing new messages.
     *  This process made as Daemon to background mode.
     */
    private void updateChatWindow() {
        tmp = 0;
        Thread tread = new Thread( () -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (currentActivContact.getMessageBubbles().size() > tmp) {
                    chatScrollPane.setVvalue(1);
                    tmp = currentActivContact.getMessageBubbles().size();
                }
            }
        });
        tread.setDaemon(true);
        tread.start();
    }

    /**
     * Get contacts using current user login and looking for contacts if database
     *
     * @return  the list of contacts of current user
     */
    private List<String> readContacts() {
        List<String> contacts = new ArrayList<>();

        try {
            conn = DriverManager.getConnection("jdbc:sqlite:Chat_L3/Contacts.db");
            conn.setAutoCommit(false);
            ps = conn.prepareStatement("SELECT * FROM contacts WHERE login = ?");
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            conn.commit();
            while (rs.next()) {
                contacts.add(rs.getString(2));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return contacts;
    }

    /**
     * Construct contacts pane using separated contact`s badges
     */
    private void loadContacts() throws IOException, ClassNotFoundException, SQLException {
        List<Node> historyNodes = null;
        ObservableList<Node> obsNodes = null;
        listContactTabs = new ArrayList<>();
        this.contacts = readContacts();

        for (String contact : contacts) {
            ContactTab tab = new ContactTab(contact);
            if (!loadHistory(contact).isEmpty()) {
                historyNodes = loadHistory(contact);
                int count = 0;
                for (Node node : historyNodes) {
                    count++;
                    MessageBubble messageBubble = (MessageBubble) node;
                    Message message = new Message();
                    message.setName(tab.getName());
                    message.setMessage(messageBubble.getMessage());
                    System.out.println(messageBubble.getMessage());
                    tab.getMessageBubbles().add(new MessageBubble(message, messageBubble.getSpeechDirection()));
                    if (count == 100) break;
                }
            }
            listContactTabs.add(tab);
            tab.addEventHandler(MouseEvent.MOUSE_PRESSED, (event)
                    -> selectContact(tab));
            contactsPane.getChildren().add(tab);
            separator = new Separator();
            separator.setId("separatorContact");
            contactsPane.getChildren().add(separator);
        }
    }

    /**
     * Select chosen contact use FX border and open chat pane of it
     * We check other contacts and remove the selection on them if there was one
     *
     * @param contact       the contact that was selected
     */
    private void selectContact(ContactTab contact) {
        contact.setNewMessageFlagVisible(false);
        contact.setBorder(new Border(new BorderStroke(Color.web("#ffaf20"),
                BorderStrokeStyle.SOLID, null, new BorderWidths(0,10,0,0))));

        currentActivContact = contact;
        Bindings.bindContent(mainChatPane.getChildren(), currentActivContact.getMessageBubbles());
        updateChatWindow();

        for (ContactTab contactTab : listContactTabs) {
            if (!contactTab.getName().equals(contact.getName())) {
                contactTab.setBorder((new Border(new BorderStroke(Color.TRANSPARENT,
                        BorderStrokeStyle.SOLID, null, new BorderWidths(0,0,0,0)))));
            }
        }
        chatScrollPane.setVvalue(1);
    }

    /**
     * Check our contacts by interviewing the server
     * We send on server list with logins our contacts by constructing it in one string
     */
    private void contactsListener() {
        new Thread( () -> {
            while (acitveSessionFlag) {
                String listContacts = "/getStatus ";
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (String contact : contacts) {
                    listContacts += contact + " ";
                }
                if (!ServerListener.sendMsg(listContacts)) {
                    setStatusContacts("/status");
                }
            }
        }).start();
    }

    private void setStatusContacts(String msg) {
        List<String> nicks = Arrays.asList(msg.split("\\s"));
        List<ContactTab> contactsOnline = new ArrayList<>();

        for (ContactTab tab : listContactTabs) {
            for (String nick : nicks) {
                if (tab.getName().equals(nick)) {
                    tab.setCurrentStatus("online");
                    contactsOnline.add(tab);
                }
            }
        }

        for (ContactTab contact : listContactTabs) {
            if (!contactsOnline.contains(contact)) {
                contact.setCurrentStatus("offline");
            }
        }
    }

    private void showMessageOnScreen(String msg) {
        List<String> data = Arrays.asList(msg.split("\\s"));
        List<String> message = Arrays.asList(msg.split("\\[%msg\\]"));

        for (ContactTab tab : listContactTabs) {
            if (tab.getName().equals(data.get(1))) {
                receivedMessageShow(String.valueOf(message.get(1)), tab);
            }
        }
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void logOutTopMenu(javafx.event.ActionEvent actionEvent) throws IOException, SQLException, InterruptedException {
        saveHistory();
        acitveSessionFlag = false;
        ServerListener.setSessionFlag(false);
        ServerListener.closeConnection();
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/resources/views/authorization.fxml"));
                    Parent window = (Pane) fmxlLoader.load();
                    Stage stage = (Stage) chatField.getScene().getWindow();
                    stage.setResizable(false);
                    Scene scene = new Scene(window);
                    stage.setScene(scene);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Save history of chat in database use userName and contact tabs;
     * If the file with that parameters is already exists it will be delete and saved again with tha same name.
     * @throws IOException
     * @throws SQLException
     */
    public void saveHistory() throws IOException, SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:Chat_L3/History.db");
        conn.setAutoCommit(false);
        FileOutputStream fileOutStream;
        ObjectOutputStream objOutStream;
        File file;
        Random rnd = new Random();
        for (ContactTab tab : listContactTabs) {
            ps = conn.prepareStatement("SELECT * FROM history WHERE login = ? AND contact = ?");
            ps.setString(1, userName);
            ps.setString(2, tab.getName());
            ResultSet rs = ps.executeQuery();
            conn.commit();
            String fileName = "none";
            if (rs.next()) {
                fileName = rs.getString(3);
                file = new File("src/data/history/" + fileName);
                file.delete();
            } else {
                fileName = rnd.nextInt(10000) + ".txt";
                ps = conn.prepareStatement("INSERT INTO history (login, contact, file_name) VALUES (?,?,?)");
                ps.setString(1, userName);
                ps.setString(2, tab.getName());
                ps.setString(3, fileName);
                ps.executeUpdate();
                conn.commit();
            }
            fileOutStream = new FileOutputStream("src/data/history/" + fileName);
            objOutStream = new ObjectOutputStream(fileOutStream);
            objOutStream.writeObject(new ArrayList<Node>(tab.getMessageBubbles()));
            objOutStream.flush();
        }
        conn.close();
    }

    private List<Node> loadHistory(String contact) throws SQLException, IOException, ClassNotFoundException {
        List<Node> historyList = null;
        conn = DriverManager.getConnection("jdbc:sqlite:Chat_L3/History.db");
        conn.setAutoCommit(false);
        ps = conn.prepareStatement("SELECT * FROM history WHERE login = ? AND contact = ?");
        ps.setString(1, userName);
        ps.setString(2, contact);
        ResultSet rs = ps.executeQuery();
        conn.commit();
        if (rs.next()) {
            String fileName = rs.getString(3);
            FileInputStream fileInputStream = new FileInputStream("Chat_L3/src/data/history/" + fileName);
            ObjectInputStream objInputStream = new ObjectInputStream(fileInputStream);
            historyList = (List<Node>) objInputStream.readObject();
            objInputStream.close();
        }
        return historyList;
    }

}
