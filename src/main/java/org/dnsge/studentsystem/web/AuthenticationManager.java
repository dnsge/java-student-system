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
import java.time.Duration;
import java.util.Date;
import java.util.Optional;

public class AuthenticationManager {

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
