package org.dnsge.studentsystem.web;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.dnsge.studentsystem.Environment;
import org.dnsge.studentsystem.Util;
import org.dnsge.studentsystem.sql.QueryManager;
import org.dnsge.studentsystem.sql.model.User;
import spark.Request;
import spark.Response;

import java.security.Key;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthenticationManager {

    private static final Pattern usernamePattern = Pattern.compile("^(\\w){4,32}$");
    private static final String issuer = "student_system";
    private static final Duration tokenLife = Duration.ofHours(6);
    private static final String authCookie = "ss_auth";

    public static String handleLogin(Request req, Response res) {
        String username = req.queryParamOrDefault("username", "");
        String password = req.queryParamOrDefault("password", "");

        if (username.equals("") || password.equals("")) {
            res.cookie("/login", "error", Util.urlEncodeString("Invalid username or password"), 10, Environment.usingHTTPS());
            res.redirect("/login", 302);
            return "";
        }

        QueryManager qm = QueryManager.getQueryManager();
        User user;
        try {
            Optional<User> u = qm.loginUser(username, password);
            if (u.isEmpty()) {
                res.cookie("/login", "error", Util.urlEncodeString("Invalid username or password"), 10, Environment.usingHTTPS());
                res.redirect("/login", 302);
                return "";
            } else {
                user = u.get();
            }
        } catch (SQLException e) {
            res.status(500);
            res.cookie("/login", "error", Util.urlEncodeString("An unknown error occurred. Try again later"), 10, Environment.usingHTTPS());
            res.redirect("/login", 302);
            System.err.println(e.toString());
            return "";
        }

        String jwt = issueJWT(user);
        res.removeCookie("/login", "error");
        res.cookie("/", authCookie, jwt, (int) tokenLife.toSeconds(), Environment.usingHTTPS(), true);
        res.redirect("/home", 302);
        return "";
    }

    public static String handleLogout(Request req, Response res) {
        res.removeCookie("/", authCookie);
        res.redirect("/login", 302);
        return "";
    }

    public static String handleRegister(Request req, Response res) {
        String username = req.queryParamOrDefault("username", "").trim();
        String password = req.queryParamOrDefault("password", "");
        String type = req.queryParamOrDefault("type", "").trim();
        String firstName = req.queryParamOrDefault("firstName", "").trim();
        String lastName = req.queryParamOrDefault("lastName", "").trim();

        if (username.equals("") || password.equals("") || firstName.equals("") || lastName.equals("") || type.equals("")) {
            res.cookie("/register", "error", Util.urlEncodeString("Enter a username, password, name, and account type"), 10, Environment.usingHTTPS());
            res.redirect("/register", 302);
            return "";
        }

        if (!isValidUsername(username)) {
            res.cookie("/register", "error", Util.urlEncodeString("Your username must be between 4 and 32 characters consisting only of letters, numbers, and underscores."), 10, Environment.usingHTTPS());
            res.redirect("/register", 302);
            return "";
        }

        type = type.toLowerCase();
        if (!type.equals("student") && !type.equals("teacher")) {
            res.cookie("/register", "error", Util.urlEncodeString("Account type must be 'Student' or 'Teacher'"), 10, Environment.usingHTTPS());
            res.redirect("/register", 302);
            return "";
        }

        QueryManager qm = QueryManager.getQueryManager();
        User u;
        try {
            u = qm.createUser(username, password, type.equals("student") ? 's' : 't');
            if (type.equals("student")) {
                qm.createStudent(u.getId(), firstName, lastName);
            } else {
                qm.createTeacher(u.getId(), firstName, lastName);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            res.cookie("/register", "error", Util.urlEncodeString("That username is already taken"), 10, Environment.usingHTTPS());
            res.redirect("/register", 302);
            System.err.println(e.toString());
            return "";
        } catch (SQLException e) {
            res.cookie("/register", "error", Util.urlEncodeString("An unknown error occurred. Try again later"), 10, Environment.usingHTTPS());
            res.redirect("/register", 302);
            System.err.println(e.toString());
            return "";
        }

        String jwt = issueJWT(u);
        res.removeCookie("/register", "error");
        res.cookie("/", authCookie, jwt, (int) tokenLife.toSeconds(), Environment.usingHTTPS(), true);
        res.redirect("/home", 302);
        return "";
    }

    private static boolean isValidUsername(String username) {
        Matcher m = usernamePattern.matcher(username);
        return m.matches();
    }

    private static String issueJWT(User u) {
        Date now = new Date();
        Date exp = Date.from(now.toInstant().plus(tokenLife));

        Key key = Environment.signingKey();
        return Jwts.builder()
                .setSubject(Integer.toString(u.getId()))
                .claim("username", u.getUsername())
                .claim("user_type", u.getType())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key)
                .compact();
    }

    private static Optional<Jws<Claims>> parseJWT(String jwt) {
        Key key = Environment.signingKey();

        try {
            Jws<Claims> jws;
            jws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .requireIssuer(issuer)
                    .build()
                    .parseClaimsJws(jwt);

            return Optional.of(jws);
        } catch (JwtException ex) {
            return Optional.empty();
        }
    }

    public static Optional<User> getAuthenticatedUser(Request req) {
        String jwt = req.cookie(authCookie);
        if (jwt == null || jwt.equals("")) {
            return Optional.empty();
        }

        Optional<Jws<Claims>> decoded = parseJWT(jwt);
        if (decoded.isEmpty()) {
            return Optional.empty();
        }

        Claims claims = decoded.get().getBody();
        User u = new User(
                Integer.parseInt(claims.getSubject()),
                (String) claims.get("username"),
                "",
                ((String) claims.get("user_type")).charAt(0)
        );

        return Optional.of(u);
    }

}
