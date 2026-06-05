package com.library.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Өгөгдлийн сангийн мэдээлэл
    private static final String URL = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; 
    private static final String PASSWORD = "T86890174t.";

    private static Connection connection = null;

    // Гаднаас дуудаж шинээр үүсгэхийг хориглоно (Singleton)
    private DBConnection() {}

    public static Connection getConnection() {
        try {
            // Хэрэв холболт үүсээгүй эсвэл хаагдсан байвал шинээр холбоно
            if (connection == null || connection.isClosed()) {
                // MySQL драйверийг ачаалах
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Холболт тогтоох
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("MySQL connection established.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL driver not found! Please check your pom.xml.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("DB connection error! Password or url is incorrect.");
            e.printStackTrace();
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("DB connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}