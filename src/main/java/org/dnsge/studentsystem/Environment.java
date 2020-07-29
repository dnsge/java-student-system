package org.dnsge.studentsystem;

import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;

public class Environment {

    public static String mySqlUri() {
        return System.getenv("MYSQL_URI");
    }

    public static String mySqlUser() {
        return System.getenv("MYSQL_USER");
    }

    public static String mySqlPassword() {
        return System.getenv("MYSQL_PASSWORD");
    }

    public static Key signingKey() {
        String source = System.getenv("SIGNING_KEY");
        return Keys.hmacShaKeyFor(source.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean usingHTTPS() {
        return System.getenv().containsKey("USE_HTTPS");
    }

    public static boolean enforcePermissions() {
        return System.getenv().containsKey("ENFORCE_PERMISSIONS");
    }

}
