package sample;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    String message;
    String name;
    String time;
    String type;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
    LocalDateTime localTime;

    public Message() {
        localTime = LocalDateTime.now();
        time = dtf.format(localTime);

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
