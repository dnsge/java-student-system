package org.dnsge.studentsystem;

import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;

public class Environment {

    public static Key signingKey() {
        String source = System.getenv("SIGNING_KEY");
        return Keys.hmacShaKeyFor(source.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean usingHTTPS() {
        return false;
    }

    public static boolean enforcePermissions() {
        return false;
    }

}
