package org.dnsge.studentsystem.sql.model;

import org.dnsge.studentsystem.Util;

public class User {
    private final int id;
    private final char type;
    private String username, passwordHash;

    public User(int id, String username, String passwordHash, char type) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void updatePassword(String pass) {
        this.passwordHash = Util.hashPass(pass);
    }

    public char getType() {
        return type;
    }

    public boolean verifyPassword(String pass) {
        try {
            return Util.verifyPass(pass, this.passwordHash);
        } catch (Exception e) {
            return false;
        }
    }
}
