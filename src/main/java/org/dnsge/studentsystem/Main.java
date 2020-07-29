package org.dnsge.studentsystem;

import org.dnsge.studentsystem.sql.MySQLConnector;
import org.dnsge.studentsystem.web.WebServer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) throws Exception {
        MySQLConnector connector = MySQLConnector.getInstance();

        int tries = 0;
        int waitTime = 1000;
        boolean connected = false;
        System.out.print("Connecting to MySQL");
        while (tries < 6) {
            System.out.print(".");
            try {
                Connection c = connector.connect(Environment.mySqlUri(), Environment.mySqlUser(), Environment.mySqlPassword());
                Statement statement = connector.newStatement();
                ResultSet rs = statement.executeQuery("SELECT VERSION()");
                if (rs.next()) {
                    System.out.printf("\nConnected to MySQL db version %s\n", rs.getString(1));
                    connected = true;
                }
            } catch (SQLException ignored) {

            }

            tries++;
            if (!connected) {
                Thread.sleep(waitTime);
                waitTime *= 2;
            } else {
                break;
            }
        }

        if (!connected) {
            throw new RuntimeException("Failed to connect to MySQL database");
        }

        WebServer ws = new WebServer(8080);
        ws.run();
    }

}
