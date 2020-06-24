import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class MyServer {
    private final int PORT = 8189;

    private List<ClientHandler> clients;
    private Map<String, ClientHandler> unsents = new HashMap<>();

    public MyServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            clients = new ArrayList<>();

            while (true) {
                System.out.println("The Server was launched.\nWaiting for connection...");
                Socket socket = serverSocket.accept();
                System.out.println("User is connected.");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            System.out.println("Server error...");
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
