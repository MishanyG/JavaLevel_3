import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class ClientHandler implements Runnable {
    private final MyServer server;
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private Connection conn;
    private PassHandler passHandler;

    private String name;
    private final List<String> comands = Arrays.asList("/getStatus", "/w");

    public ClientHandler(MyServer server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.name = "";

            Server.executorServiceClient.execute(new Thread(this));
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента.");
        }
    }

    @Override
    public void run() {
        {
            try {
                if (authentication()) {
                    readMessages();
                }
            } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
                System.out.println("Client error...");
            } finally {
                closeConnection();
            }
        }
    }

    public String getName() {
        return name;
    }

    public boolean authentication() throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        String str;
        while ((str = inputStream.readUTF()) != null) {
            if (str.startsWith("/singUp")) {
                if (singUp(str)) {
                    sendMessage("/singUp passed");
                    break;
                } else {
                    sendMessage("/singUp failed");
                }
            }
            if (str.startsWith("/logIn")) {
                if (logIn(str)) {
                    sendMessage("/logIn passed");
                    break;
                } else {
                    sendMessage("/logIn failed");
                }
            }
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        server.checkUnsents();
        return true;
    }

    public synchronized void readMessages() throws IOException {
        while (true) {
            String msgFromClient = inputStream.readUTF();
            for (String comand : comands) {
                if (msgFromClient.startsWith(comand)) {
                    switch (comand) {
                        case "/getStatus":
                            checkStatusContacts(msgFromClient);
                            break;
                        case "/w":
                            server.chooseClientForMessage(msgFromClient, this);
                            break;
                    }
                }
            }
        }

    }

    private void checkStatusContacts(String msg) {
        List<String> nicks = Arrays.asList(msg.split("\\s"));
        String response = "/status ";
        nicks = server.getOnlineClients(nicks);

        for (String nick : nicks) {
            response += nick + " ";
        }
        sendMessage(response);
    }

    public void sendMessage(String msg) {
        try {
            outputStream.writeUTF(msg);
        } catch (IOException e) {
            System.out.println("Problem with OutputStream...");
        }
    }

    private void closeConnection() {
        server.unsubscribe(this);
        server.broadcastMsg(name + " has left this chat.");
        System.out.println(name + " has left this chat");

        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing the connection...");
        }
    }

    /**
     * Create new account in database
     *
     * @param str the string with user`s data
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    private boolean singUp(String str) throws InvalidKeySpecException, NoSuchAlgorithmException {
        passHandler = new PassHandler();
        String[] parts = str.split("\\s");
        String hashString = passHandler.createHash(parts[2]);
        String[] param = hashString.split(":");
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:Server_L3/Account.db");
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("INSERT INTO accounts (login, iteration, salt, hash) VALUES (?,?,?,?)");
            ps.setString(1, parts[1]);
            ps.setString(2, param[0]);
            ps.setString(3, param[1]);
            ps.setString(4, param[2]);
            ps.executeUpdate();
            conn.commit();
            conn.close();
        } catch (SQLException throwables) {
            return false;
        }
        return true;
    }

    /**
     * Validate login and pass
     *
     * @param str      the string with user`s data
     * @return         true if login and pass are correct, false if not
     */
    private boolean logIn(String str) {
        passHandler = new PassHandler();
        String[] parts = str.split("\\s");
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:Server_L3/Account.db");
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM" +
                    " accounts WHERE login = ?");
            ps.setString(1, parts[1]);
            ResultSet rs = ps.executeQuery();
            conn.commit();
            if (passHandler.validatePassword(parts[2], rs.getInt(2), rs.getString(3), rs.getString(4))) {
                sendMessage("/auth ok " + parts[1]);
                name = parts[1];
                System.out.println(name + " entered to the chat.");
                server.subscribe(this);
                return true;
            }
            conn.close();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return false;
    }
}

