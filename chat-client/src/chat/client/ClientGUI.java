package chat.client;

import chat.library.Library;
import chat.network.SocketThread;
import chat.network.SocketThreadListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 300;

    private final JTextArea log = new JTextArea();
    private final JPanel panelTop = new JPanel(new GridLayout(3, 3));
    private final JTextField tfIPAddress = new JTextField("127.0.0.1");
    private final JTextField tfPort = new JTextField("8189");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top");
    private final JTextField tfLogin = new JTextField("Ivanov");
    private final JPasswordField tfPassword = new JPasswordField("1234");
    private final JButton btnLogin = new JButton("Login");
    private final JButton btnChange = new JButton("Change nickname");
    private JDialog dialog;
    private JTextField tfNick;
    private JButton btnChangeNick;
    private boolean newNick = false;

    private final JPanel panelBottom = new JPanel(new BorderLayout());
    private final JButton btnDisconnect = new JButton("<html><b>Disconnect</b></html>");
    private final JTextField tfMessage = new JTextField();
    private final JButton btnSend = new JButton("Send");

    private final JList<String> userList = new JList<>();
    private boolean shownIoErrors = false;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");

    private SocketThread socketThread;
    private static final String WINDOW_TITLE = "Chat";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }

    private ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        setTitle(WINDOW_TITLE);
        log.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(log);
        JScrollPane scrollUser = new JScrollPane(userList);
        scrollUser.setPreferredSize(new Dimension(100, 0));
        cbAlwaysOnTop.addActionListener(this);
        btnSend.addActionListener(this);
        tfMessage.addActionListener(this);
        btnLogin.addActionListener(this);
        btnChange.addActionListener(this);
        btnDisconnect.addActionListener(this);

        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnLogin);
        panelTop.add(btnChange);

        panelBottom.add(btnDisconnect, BorderLayout.WEST);
        panelBottom.add(tfMessage, BorderLayout.CENTER);
        panelBottom.add(btnSend, BorderLayout.EAST);
        panelBottom.setVisible(false);

        add(scrollLog, BorderLayout.CENTER);
        add(scrollUser, BorderLayout.EAST);
        add(panelTop, BorderLayout.NORTH);
        add(panelBottom, BorderLayout.SOUTH);

        setVisible(true);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (src == btnSend || src == tfMessage) {
            sendMessage();
        } else if (src == btnLogin) {
            connect();
        } else if (src == btnDisconnect) {
            socketThread.close();
        } else if (src == btnChange) {
            changeWindow();
        } else {
            throw new RuntimeException("Unknown source: " + src);
        }
    }

    private void changeWindow() {
        dialog = new JDialog(this);
        tfNick = new JTextField("");
        btnChangeNick = new JButton("Save");
        dialog.setTitle("Change nickname");
        dialog.setLocationRelativeTo(null);
        btnChangeNick.addActionListener(e -> connect());
        dialog.add(new JPanel() {
            {
                add(tfNick);
                tfNick.setPreferredSize(new Dimension(150, 24));
                add(btnChangeNick);
            }
        });
        dialog.setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        dialog.setSize(250, 80);
        newNick = true;
    }

    private void connect() {
        try {
            Socket socket = new Socket(tfIPAddress.getText(), Integer.parseInt(tfPort.getText()));
            socketThread = new SocketThread("Client", this, socket);
        } catch (IOException exception) {
            showException(Thread.currentThread(), exception);
        }
    }

    private void sendMessage() {
        String msg = tfMessage.getText();
        if ("".equals(msg)) return;
        tfMessage.setText(null);
        tfMessage.grabFocus();
        socketThread.sendMessage(Library.getTypeBcastClient(msg));
    }

    private void wrtMsgToLogFile(String msg) {
        try (FileWriter out = new FileWriter("log.txt", true)) {    // Откроем файл для записи
            if (msg.contains(": : Server:") || msg.equals("Start") || msg.equals("Ready")) {    // Пропустим данные строки, чтобы сохранять переписку
                out.flush();
                return;
            }
            out.write(msg);     // Записываем данные в лог-файл
            out.flush();
        } catch (IOException e) {
            if (!shownIoErrors) {
                shownIoErrors = true;
                showException(Thread.currentThread(), e);
            }
        }
    }
    private void readingFromLog() {
        String file = "log.txt";
        StringBuilder text = new StringBuilder();
        LineNumberReader linNum;
        try (RandomAccessFile randomAcc = new RandomAccessFile(file, "r")) {    // Откроем файл для установки каретки в необходимую позицию
            linNum = new LineNumberReader(new FileReader(file));    // Откроем файл для перемешения по строкам
            String str = "";
            int linesCount = 0;                     // Здесь хранится количество строк
            int number = 0;                         // Здесь хранится позиция каретки
            while(null != linNum.readLine()) {      // Считаем количество строк в файле
                linesCount++;
            }
            if (!(linesCount < 100)) {              // Проверяем лог на наличие 100 строк, если в файле присутствует
                linesCount -= 100;                  // данное количество строк, отступим их
                for (int i = 0; i < linesCount; i++) {
                    number += randomAcc.readLine().length() + 1;    // Высчитаем необходимую позицию каретки
                }
                randomAcc.seek(number - 1);     // устанавливаем каретку в сотую снизу строку
                for (int i = 0; i <= 100; i++) {
                    text.append(randomAcc.readLine()).append("\n"); // записываем 100 строк
                }
                randomAcc.close();
            } else {                                // Если в лог-файле меньше 100 строк
                for (int i = 0; i < linesCount; i++) {
                    text.append(randomAcc.readLine()).append("\n");     // записываем сколько есть
                }
            }
            str = new String(text.toString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);    // меняем кодировку, для отображения Русских символов
            if (!str.equals(""))    // Проверяем на пустоту
                log.append(str);    // Выводим в чат
        } catch (FileNotFoundException f) {
            f.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void putLog(String msg) {
        if ("".equals(msg)) return;
        SwingUtilities.invokeLater(() -> {
            log.append(msg + "\n");
            log.setCaretPosition(log.getDocument().getLength());
            wrtMsgToLogFile(msg);       // Всё что пишется в чат, отправляем на запись в лог-файл
        });
    }

    private void showException(Thread t, Throwable e) {
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        if (ste.length == 0)
            msg = "Empty Stacktrace";
        else {
            msg = String.format("Exception in \"%s\" %s: %s\n\tat %s",
                    t.getName(), e.getClass().getCanonicalName(), e.getMessage(), ste[0]);
        }
        JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        showException(t, e);
        System.exit(1);
    }

    @Override
    public void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Start");
    }

    @Override
    public void onSocketStop(SocketThread thread) {
        panelBottom.setVisible(false);
        panelTop.setVisible(true);
        setTitle(WINDOW_TITLE);
        userList.setListData(new String[0]);
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Ready");
        panelBottom.setVisible(true);
        panelTop.setVisible(false);
        String login = tfLogin.getText();
        String password = new String(tfPassword.getPassword());
        if (newNick && !"".equals(tfNick.getText())) {          // Проверяем, не хочет ли клиент поменять никнейм и не пустое ли поле
            thread.sendMessage(Library.getChangeNickname(login, password, tfNick.getText()));   // Формируем сообщение для смены никнейма
            dialog.setVisible(false);
            newNick = false;
        } else {
            thread.sendMessage(Library.getAuthRequest(login, password));    // Если клиент подключается, формируем сообщение для авторизации
            newNick = false;
            readingFromLog();   // После авторизации клиента, выводим 100 строк его переписки
        }
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        handleMessage(msg);
    }

    void handleMessage(String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Library.AUTH_ACCEPT:
                setTitle(WINDOW_TITLE + ": " + arr[1]);
                break;
            case Library.AUTH_DENIED:
                putLog("Wrong login/password");
                break;
            case Library.MSG_FORMAT_ERROR:
                putLog(msg);
                socketThread.close();
                break;
            case Library.TYPE_BROADCAST:
                putLog(DATE_FORMAT.format(Long.parseLong(arr[1])) + ": " + arr[2] + ": " + arr[3] + "\n");
                break;
            case Library.USER_LIST:
                String users = msg.substring(Library.USER_LIST.length() + Library.DELIMITER.length());
                String[] usersArr = users.split(Library.DELIMITER);
                Arrays.sort(usersArr);
                userList.setListData(usersArr);
                break;
            default:
                throw new RuntimeException("Unknown message type: " + msg);
        }
    }

    @Override
    public void onSocketException(SocketThread thread, Throwable throwable) {
        thread.close();
    }
}
