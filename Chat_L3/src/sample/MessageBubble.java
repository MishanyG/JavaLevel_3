package sample;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.Serializable;

enum SpeechDirection  implements Serializable{
    LEFT, RIGHT
}

public class MessageBubble extends HBox implements Serializable {
    private static final long serialVersionUID = 1L;
    private String message;
    private String name;
    private String type;
    private String time;
    private SpeechDirection direction;

    transient private Label displayedMessage;
    transient private Label displayedName;
    transient private Label displayedTime;

    public MessageBubble(Message message, SpeechDirection direction) {
        this.message = message.getMessage();
        this.name = message.getName();
        this.type = message.getType();
        this.time = message.getTime();
        this.direction = direction;
        setupElements();
    }

    private void setupElements() {
        this.name = name.toUpperCase();
        displayedTime = new Label(time);
        displayedMessage = new Label(message);

        if (direction == SpeechDirection.LEFT) {
            configurationReceiver();
        } else {
            configurationSender();
        }
    }

    private void configurationSender() {
        displayedMessage.setBackground(new Background(
                new BackgroundFill(Color.web("#fcdb14"), new CornerRadii(5,5,5,5,false), new Insets(-5,-8,-5,-8))));
        displayedMessage.setAlignment(Pos.CENTER_RIGHT);
        displayedMessage.setWrapText(true);

        displayedTime.setPadding(new Insets(8, 0, 0, 0));
        displayedTime.setTextFill(Color.web("#9c9c9c"));
        displayedTime.setFont(new javafx.scene.text.Font("Calibri", 12));
        displayedTime.setAlignment(Pos.CENTER_RIGHT);
        displayedTime.setWrapText(true);

        VBox container = new VBox(displayedMessage, displayedTime);
        container.maxWidthProperty().bind(widthProperty().multiply(0.75));
        getChildren().setAll(container);
        setAlignment(Pos.CENTER_LEFT);
    }

     private void configurationReceiver() {
        displayedMessage.setBackground(new Background(
                new BackgroundFill(Color.web("#ffffff"), new CornerRadii(5,5,5,5,false), new Insets(-5,-8,-5,-8))));
        displayedMessage.setAlignment(Pos.CENTER_RIGHT);
        displayedMessage.setWrapText(true);

        displayedTime.setPadding(new Insets(8, 0, 0, 0));
        displayedTime.setTextFill(Color.web("#9c9c9c"));
        displayedTime.setFont(new javafx.scene.text.Font("Calibri", 12));
        displayedTime.setAlignment(Pos.CENTER_RIGHT);
        displayedTime.setWrapText(true);

        VBox container = new VBox(displayedMessage, displayedTime);
        container.maxWidthProperty().bind(widthProperty().multiply(0.75));
        getChildren().setAll(container);
        setAlignment(Pos.CENTER_RIGHT);
    }

    public String getMessage() {
        return message;
    }
    public SpeechDirection getSpeechDirection() {
        return direction;
    }
}
