import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private DataInputStream is;
    private DataOutputStream os;
    private InputStream fis;
    private OutputStream fos;
    private final IOServer server;
    private final Socket socket;
    private final String name = "User";
    private FileOutputStream wfile;
    public ClientHandler (Socket socket, IOServer ioServer) throws IOException {
        server = ioServer;
        this.socket = socket;
        is = new DataInputStream (socket.getInputStream ());
        os = new DataOutputStream (socket.getOutputStream ());
    }

    public void sendMessage (String message) throws IOException {
        os.writeUTF (message);
        os.flush ();
    }

    private void connection (String s) throws IOException{
        File file = new File ("server/Out/" + s);
        wfile = new FileOutputStream(file);
        int readedBytesCount = 0;
        byte[] buf = new byte[2 * 1024];
        while (true) {
            readedBytesCount = is.read (buf);
            if (readedBytesCount == - 1) {
                break;
            }
            if (readedBytesCount > 0) {
                wfile.write (buf, 0, readedBytesCount);
            }
            wfile.flush ();
            wfile.close ();
            break;
        }
    }

    public void run() {
        while (true) {
            try {
                String message = is.readUTF ();
                System.out.println ("message from " + name + ": " + message);
                server.broadCastMessage (message);
                String[] s = message.split ("sendFiles#", 2);
                if (s.length > 1)
                    if (s[0].equals (""))
                        connection (String.valueOf (s[1]));
                if (message.equals ("quit")) {
                    server.kick (this);
                    os.close ();
                    is.close ();
                    socket.close ();
                    System.out.println ("client " + name + " disconnected");
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }
    }
}