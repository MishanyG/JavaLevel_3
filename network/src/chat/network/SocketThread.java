package chat.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SocketThread extends Thread {

    private final SocketThreadListener listener;
    private final Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public SocketThread(String name, SocketThreadListener listener, Socket socket) {
        super(name);
        this.listener = listener;
        this.socket = socket;
        start();
    }

    @Override
    public void run() {
        try {
            listener.onSocketStart(this, socket);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            listener.onSocketReady(this, socket);
            while (!isInterrupted()) {
                String msg = in.readUTF();
                listener.onReceiveString(this, socket, msg);
            }
        } catch (SocketTimeoutException s) {
            close();
        } catch (IOException exception) {
            listener.onSocketException(this, exception);
        } finally {
            close();
        }
    }

    public boolean sendMessage(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
            return true;
        } catch (IOException exception) {
            listener.onSocketException(this, exception);
            close();
            return false;
        }
    }

    public void close() {
        try {
            in.close();
        } catch (IOException exception) {
            listener.onSocketException(this, exception);
        }
        interrupt();
        try {
            socket.close();
        } catch (IOException exception) {
            listener.onSocketException(this, exception);
        }
        listener.onSocketStop(this);
    }

}
