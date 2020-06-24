package sample.ServerListener;

import sample.ControllerAuthorization;
import sample.ControllerChatView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ServerListener {
    private static final String SERVER_HOSTNAME = "127.0.0.1";
    private static final int SERVER_PORT = 8189;
    private static String authString;
    public static Socket socket = null;
    private static DataInputStream inputStream;
    private static DataOutputStream outputStream;
    private static ControllerChatView controller;
    private static ControllerAuthorization controllerAu;
    private static String userName;
    private static boolean activeSessionFlag = true;

    public ServerListener(String authString, ControllerAuthorization controllerAu) {
        this.authString = authString;
        this.controllerAu = controllerAu;
        launch();
    }

    public static void launch() {
        try {
            socket = new Socket(SERVER_HOSTNAME, SERVER_PORT);
            System.out.println("You join to chat-server.");
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());

            Thread netThread = new Thread(() -> {
                try {
                    try {
                    Thread.sleep(1000);
                     } catch (InterruptedException e) {
                       e.printStackTrace();
                     }

                    authorizationServer(authString);
                    while (true) {
                        String msg = inputStream.readUTF();
                        if (msg.startsWith("/logIn") || msg.startsWith("/singUp")) {
                            String[] param = msg.split("\\s");
                            if (param[1].equals("passed")) {
                                controllerAu.loadChatScreen();
                                break;
                            } else {
                                controllerAu.failedAuthorization();
                            }
                        }
                    }

                    checkUnsents(controller.unsents);

                    while (true) {
                        String msg = inputStream.readUTF();
                        if (msg != "") {
                            controller.receivedMessage(msg);
                        }
                    }

                } catch (IOException e) {
                    System.out.println("Connection was failed.");
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            });
            netThread.setDaemon(true);
            netThread.start();

        } catch (UnknownHostException e) {
            System.out.println("Server not found.");
        } catch (IOException ex) {
            System.out.println("Couldn't connect to server.");
        }
    }

    public static synchronized boolean sendMsg(String msg) {
            try {
                if (socket == null || socket.isClosed()) {
                    while (activeSessionFlag) {
                        launch();
                    }
                    return false;
                } else {
                    outputStream.writeUTF(msg);
                    return true;
                }
            } catch (IOException e) {
                System.out.println("Outputstream not found.");
                return false;
            }
    }

    private static void authorizationServer(String authString) {
        sendMsg(authString);
    }

    private static void checkUnsents(List<String> unsents) {
        List<String> tmp = new ArrayList<>(unsents);
        for (int x = 0; x < unsents.size(); x++) {
            if (sendMsg(unsents.get(x))) {
                tmp.remove(unsents.get(x));
            } else {
                break;
            }
        }
        controller.unsents = new ArrayList<>(tmp);
    }

    public static void closeConnection() {
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Couldn't close connection.");
        }
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userLogin) {
        userName = userLogin;
    }

    public static void setController(ControllerChatView cntr) {
        controller = cntr;
    }

    public static void setSessionFlag(Boolean flag) {
        activeSessionFlag = flag;
    }
}
