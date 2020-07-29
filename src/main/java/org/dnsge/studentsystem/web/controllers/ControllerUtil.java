package org.dnsge.studentsystem.web.controllers;

import org.dnsge.studentsystem.sql.model.User;
import org.dnsge.studentsystem.web.AuthenticationManager;
import spark.Request;
import spark.Response;

import java.util.Optional;
import java.util.function.Function;

public class ControllerUtil {

    public static String mustGetUser(Request req, Response res, Function<User, String> cb) {
        Optional<User> authUser = AuthenticationManager.getAuthenticatedUser(req);
        if (authUser.isEmpty()) {
            res.redirect("/login", 302);
            return "";
        }
        return cb.apply(authUser.get());
    }
}
