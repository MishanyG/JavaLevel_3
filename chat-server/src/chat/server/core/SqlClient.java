package chat.server.core;

import java.sql.*;

public class SqlClient {

    private static Connection connection;
    private static Statement statement;

    synchronized static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:chat-server/chat_bd.db");
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    synchronized static void disconnect() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throw new RuntimeException(throwables);
        }
    }

    synchronized static String getNickname(String login, String password) {     // Получаем из БД пользователя по логину и паролю
        try {
            ResultSet rs = statement.executeQuery(
                    String.format("select nickname from users_bd where login = '%s' and password = '%s'",
                            login, password));
            if (rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    synchronized static void changeNickname(String login, String password, String newNick) {    // Меняем никнейм
        try {
            statement.executeUpdate(String.format("UPDATE users_bd SET nickname = '%s' where login = '%s' and password = '%s';", newNick, login, password));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
