package org.dnsge.studentsystem;

import org.dnsge.studentsystem.sql.MySQLConnector;
import org.dnsge.studentsystem.sql.QueryManager;
import org.dnsge.studentsystem.sql.model.User;
import org.dnsge.studentsystem.web.WebServer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/studentsystem?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        String user = "system";
        String password = "b69efcefb8c4c9c7";

        MySQLConnector connector = MySQLConnector.getInstance();
        try {
            connector.connect(url, user, password);
            Statement statement = connector.newStatement();
            ResultSet rs = statement.executeQuery("SELECT VERSION()");
            if (rs.next()) {
                System.out.println(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

//        QueryManager qm = new QueryManager(connector.getConnection());
//        User u = qm.createUser("mhurray", "password", 't');

        WebServer ws = new WebServer(8080);
        ws.run();
    }

}
