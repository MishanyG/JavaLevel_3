package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

public class ContactTab extends HBox {
    private Circle circle;
    private Circle flagNewMessage;
    private ImagePattern pattern;
    private ObservableList<Node> messageBubbles;

    private User user;

    private Label displayedName;
    private Label displayedStatus;


    public ContactTab(String name) {
        this.user = new User();
        this.user.setName(name);
        this.user.setStatus("offline");
        this.user.setImage("/resources/images/user.png");
        this.messageBubbles = FXCollections.observableArrayList();
        setupElements();
    }

    private void setupElements() {
        circle = new Circle(24);
        pattern = new ImagePattern(new Image(user.getImage()));
        circle.setFill(pattern);
        flagNewMessage = new Circle(3);
        flagNewMessage.setFill(Color.web("#4dcdc4"));
        flagNewMessage.setVisible(false);
        displayedName = new Label(user.getName().toUpperCase());
        displayedStatus = new Label(user.getStatus());

        VBox container = new VBox(displayedName, displayedStatus);
        container.setSpacing(5);
        container.setMinWidth(150);
        container.maxWidthProperty().bind(widthProperty().multiply(0.9));
        getChildren().setAll(container);
        setAlignment(Pos.CENTER_LEFT);
        this.getChildren().setAll(circle, container, flagNewMessage);

        configurationTab();
    }

    private void configurationTab() {
        displayedStatus.setFont(new Font("Calibri", 10.5));
        displayedName.setId("contactTabName");
        this.setId("contactTab");
    }
    public String getName() {
        return user.getName();
    }

    public void setCurrentStatus(String status) {
        user.setStatus(status);
        new Thread(()->
        {
            Platform.runLater(() -> {
                displayedStatus.setText(user.getStatus());
                if (user.getStatus().equals("online")) {
                    displayedStatus.setTextFill(Color.web("#4dcdc4"));
                } else {
                    displayedStatus.setTextFill(Color.web("#042a38"));
                }
            });
        }).start();
    }

    public void setNewMessageFlagVisible(Boolean flag) {
        flagNewMessage.setVisible(flag);
    }

    public ObservableList<Node> getMessageBubbles() {
        return messageBubbles;
    }
}
