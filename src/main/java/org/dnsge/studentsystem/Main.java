package org.dnsge.studentsystem;

import org.dnsge.studentsystem.sql.MySQLConnector;
import org.dnsge.studentsystem.web.WebServer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) {
        MySQLConnector connector = MySQLConnector.getInstance();
        try {
            connector.connect(Environment.mySqlUri(), Environment.mySqlUser(), Environment.mySqlPassword());
            Statement statement = connector.newStatement();
            ResultSet rs = statement.executeQuery("SELECT VERSION()");
            if (rs.next()) {
                System.out.printf("Connected to MySQL db version %s\n", rs.getString(1));
            } else {
                throw new RuntimeException("Failed to connect to MySQL database");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        WebServer ws = new WebServer(8080);
        ws.run();
    }

}
