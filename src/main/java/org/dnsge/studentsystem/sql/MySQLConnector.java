package org.dnsge.studentsystem.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLConnector {

    private static MySQLConnector instance;

    private Connection connection;

    private MySQLConnector() {

    }

    public Connection connect(String url, String user, String pass) throws SQLException {
        connection = DriverManager.getConnection(url, user, pass);
        return connection;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public Statement newStatement() throws SQLException {
        if (connection == null)
            return null;

        return connection.createStatement();
    }

    public static MySQLConnector getInstance() {
        if (instance == null) {
            instance = new MySQLConnector();
        }

        return instance;
    }


}
