package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBUtil {

    private static final String URL =
            "jdbc:mysql://localhost:3306/warehouse_replenishment";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        // Усередині Tomcat драйвер із WEB-INF/lib автоматично не реєструється, тому завантажуємо його явно
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC-драйвер MySQL не знайдено", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
