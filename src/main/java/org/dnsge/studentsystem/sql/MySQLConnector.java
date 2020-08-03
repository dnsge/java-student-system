package org.dnsge.studentsystem.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLConnector {

    private static MySQLConnector instance;

    private final String url, user, pass;
    private Connection connection;

    private MySQLConnector(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    public Connection connect() throws SQLException {
        connection = DriverManager.getConnection(url, user, pass);
        return connection;
    }

    public Connection getConnection() throws SQLException {
        // Check if connection is still open & valid
        boolean valid;
        try {
            valid = this.connection.isValid(1);
        } catch (SQLException e) {
            valid = false;
        }

        // Reconnect if not valid
        if (!valid) {
            return this.connect();
        }

        return this.connection;
    }

    public Statement newStatement() throws SQLException {
        if (connection == null)
            return null;

        return connection.createStatement();
    }

    public static MySQLConnector getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MySQLConnector is not initialized");
        }

        return instance;
    }

    public static MySQLConnector init(String url, String user, String pass) {
        if (instance != null)
            throw new IllegalStateException("MySQLConnector is already initialized");
        instance = new MySQLConnector(url, user, pass);
        return instance;
    }

}
