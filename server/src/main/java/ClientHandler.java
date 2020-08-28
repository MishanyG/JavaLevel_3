import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final DataInputStream is;
    private final DataOutputStream os;
    private final IOServer server;
    private final Socket socket;
    private static int counter = 0;
    private final String name;
    private static final String PATH = "server/Out/";

    public ClientHandler(Socket socket, IOServer ioServer) throws IOException {
        server = ioServer;
        this.socket = socket;
        counter++;
        name = "user#" + counter;
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        System.out.println("Client handled: ip = " + socket.getInetAddress());
        System.out.println("Nick:" + name);
    }

    public void sendMessage(String message) throws IOException {
        os.writeUTF(message);
        os.flush();
    }

    public void run() {
        while (true) {
            try {
                String message = is.readUTF();
                if (message.equals("quit")) {
                    os.writeUTF("disconnected");
                    Thread.sleep(1000);
                    os.close();
                    is.close();
                    socket.close();
                    System.out.println("client " + name + " disconnected");
                    break;
                }
                if (message.equals("./upload")) {
                    String fileName = is.readUTF();
                    long fileLength = is.readLong();
                    File file = new File(PATH + fileName);
                    file.createNewFile();
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[256];
                        if (fileLength < 256) {
                            fileLength += 256;
                        }
                        int read = 0;
                        for (int i = 0; i < fileLength / 256; i++) {
                            read = is.read(buffer);
                            fos.write(buffer, 0, read);
                        }
                        os.writeUTF("OK");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    String fileName = is.readUTF();
                    if (message.equals ("mess")) {
                        os.writeUTF (fileName);
                        System.out.println ("message from " + name + ": " + fileName);
                    } else {
                        File file = new File (PATH + fileName);
                        if (file.exists ()) {
                            os.writeUTF ("OK");
                        } else {
                            os.writeUTF ("WRONG");
                        }
                        try (FileInputStream fis = new FileInputStream (file)) {
                            os.writeLong (file.length ());
                            byte[] buffer = new byte[256];
                            int read;
                            while ((read = fis.read (buffer)) != - 1) {
                                os.write (buffer, 0, read);
                            }
                            os.flush ();
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }