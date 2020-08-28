import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class MyServer {
    private final int PORT = 8189;

    private List<ClientHandler> clients;
    private Map<String, ClientHandler> unsents = new HashMap<>();
    static final Logger LOGGER = LogManager.getLogger();

    public MyServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            clients = new ArrayList<>();

            while (true) {
                LOGGER.info("The Server was launched.\nWaiting for connection...");
                LOGGER.info("Сервер запущен, ожидание соединения ...");
                Socket socket = serverSocket.accept();
                LOGGER.info("User is connected.");
                LOGGER.info("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            LOGGER.error("Server error...");
            LOGGER.error("Произошла ошибка: ошибка сервера ...");
        } finally {
        }
    }

    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

    public synchronized boolean chooseClientForMessage(String msg, ClientHandler owner) {
        List<String> data = Arrays.asList(msg.split("\\s"));
        List<String> message = Arrays.asList(msg.split("\\[%msg\\]"));

        for (ClientHandler client : clients) {
            if (client.getName().equals(data.get(1))) {
                client.sendMessage("/w " + owner.getName() + " [%msg]" + message.get(1));
                LOGGER.info("Клиент прислал сообщение");
                return true;
            }
        }
        unsents.put(msg, owner);
        return false;
    }

    public List<String> getOnlineClients(List<String> list) {
        List<String> onlineClients = new ArrayList<>();
        for (ClientHandler client : clients) {
            for (String nick : list) {
                if (client.getName().equals(nick)) {
                    onlineClients.add(nick);
                }
            }
        }
        return onlineClients;
    }

    public void checkUnsents() {
        try {
            Thread.sleep(1000);
            Map<String, ClientHandler> tmp = new HashMap<>(unsents);
            unsents.forEach( (key, value) -> {
                if (chooseClientForMessage(key, value)) {
                    tmp.remove(key, value);
                } else {
                    return;
                }
            });
            unsents = new HashMap<>(tmp);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
    }
}
