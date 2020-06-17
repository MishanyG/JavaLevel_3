package chat.server.core;

import chat.library.Library;
import chat.network.ServerSocketThread;
import chat.network.ServerSocketThreadListener;
import chat.network.SocketThread;
import chat.network.SocketThreadListener;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class ChatServer implements ServerSocketThreadListener, SocketThreadListener {

    ChatServerListener listener;
    private Vector<SocketThread> clients = new Vector<>();

    public ChatServer(ChatServerListener listener) {
        this.listener = listener;
    }

    private ServerSocketThread server;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");


    public void start(int port) {
        if (server == null || !server.isAlive())
            server = new ServerSocketThread(this, "Server", port, 2000);
        else
            putLog("Server already started");
    }

    public void stop() {
        if (server == null || !server.isAlive()) {
            putLog("Server is not running");
        } else {
            server.interrupt();
        }
    }

    private void putLog(String msg) {
        msg = DATE_FORMAT.format(System.currentTimeMillis()) +
                Thread.currentThread().getName() + ": " + msg;
        listener.onChatServerMessage(msg);
    }

    @Override
    public void onServerStart(ServerSocketThread thread) {
        putLog("Server started");
        SqlClient.connect();
        putLog(SqlClient.getNickname("Ivanov", "1234"));

    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        putLog("Server stopped");
        SqlClient.disconnect();
        for (SocketThread client : clients) {
            client.close();
        }
    }

    @Override
    public void onServerCreated(ServerSocketThread thread, ServerSocket server) {
        putLog("Server created");
    }

    @Override
    public void onServerTimeout(ServerSocketThread thread, ServerSocket server) {
    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket) {
        putLog("Client connected");
        String name = "SocketThread " + socket.getInetAddress() + ":" + socket.getPort();
        new ClientThread(name, this, socket);
    }

    @Override
    public void onServerException(ServerSocketThread thread, Throwable throwable) {
        throwable.printStackTrace();

    }

    @Override
    public synchronized void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Client connected");
    }

    @Override
    public synchronized void onSocketStop(SocketThread thread) {
        ClientThread client = (ClientThread) thread;
        clients.remove(thread);
        if (client.isAuthorized() && !client.isReconnecting()) {
            sendToAllAuthorizedClients(Library.getTypeBroadcast("Server",
                    client.getNickname() + " disconnected"));
        }
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));
    }

    @Override
    public synchronized void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Client is ready to chat");
        clients.add(thread);
    }

    @Override
    public synchronized void onReceiveString(SocketThread thread, Socket socket, String msg) throws SocketException {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized()) {            // Проверяем, авторизовался ли клиент
            handleAuthMessage(client, msg);
        } else {
            socket.setSoTimeout(120000);        // Если не авторизовался, ждём 2 мин и отключаем
            handleNonAuthMessage(client, msg);
        }
        if (client.isAuthorized()) socket.setSoTimeout(0);      // Если вдруг клиент всё-таки авторизовался, подключаем его
    }

    private void handleNonAuthMessage(ClientThread newClient, String msg) {
        String[] arr = msg.split(Library.DELIMITER);                // Разбираем сообщение
        if (arr.length != 3 || !arr[0].equals(Library.AUTH_REQUEST)) {      // Если сообщение не на авторизацию
            if (!arr[0].equals(Library.CHANGE_NICKNAME)) {                  // И не на смену никнейма
                newClient.msgFormatError(msg);                              // То выводим сообщение об ошибке и завершаем сессию
                return;                                                     // И дальше продолжать не будем
            }
        }
        String login = arr[1];                                     // Берём логин
        String password = arr[2];                                  // Берём пароль
        String nickname = SqlClient.getNickname(login, password);  // Запрос у БД никнейм, если пользователь найден в базе, получаем ник
        if (nickname == null) {                                    // Если пользователь не найден, выходим
            return;
        } else {
            ClientThread oldClient = findClientByNickname(nickname);        // Проверяем что он не подключен
            if (arr[0].equals(Library.CHANGE_NICKNAME)) {
                String newNick = arr[3];
                SqlClient.changeNickname(login, password, newNick);
                nickname = newNick;
            }
            newClient.authAccept(nickname);                         // Если пользователь найден, авторизируем его
            if (oldClient == null) {
                sendToAllAuthorizedClients(Library.getTypeBroadcast("Server", nickname + " connected"));
            } else {                                                // Если пользователь уже подключен на другом устройстве
                oldClient.reconnect();                              // Переподключим его
                clients.remove(oldClient);
            }
        }
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));
    }

    private void handleAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Library.CLIENT_MSG_BROADCAST:
                sendToAllAuthorizedClients(Library.getTypeBroadcast(
                        client.getNickname(), arr[1]));
                break;
            default:
                client.sendMessage(Library.getMsgFormatError(msg));
        }
    }

    private void sendToAllAuthorizedClients(String msg) {
        for (SocketThread socketThread : clients) {
            ClientThread client = (ClientThread) socketThread;
            if (!client.isAuthorized()) continue;
            client.sendMessage(msg);
        }
    }

    @Override
    public synchronized void onSocketException(SocketThread thread, Throwable throwable) {
        throwable.printStackTrace();
        thread.close();
    }

    private synchronized String getUsers() {
        StringBuilder sb = new StringBuilder();
        for (SocketThread socketThread : clients) {
            ClientThread client = (ClientThread) socketThread;
            if (!client.isAuthorized()) continue;
            sb.append(client.getNickname()).append(Library.DELIMITER);
        }
        return sb.toString();
    }

    private synchronized ClientThread findClientByNickname(String nickname) {
        for (SocketThread socketThread : clients) {
            ClientThread client = (ClientThread) socketThread;
            if (!client.isAuthorized()) continue;
            if (client.getNickname().equals(nickname))
                return client;
        }
        return null;
    }
}
