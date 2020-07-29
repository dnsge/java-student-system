package org.dnsge.studentsystem;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategy;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class Util {

    private static final BCrypt.Version bcryptVersion = BCrypt.Version.VERSION_2Y;
    private static final LongPasswordStrategy strategy = LongPasswordStrategies.hashSha512(bcryptVersion);

    public static String hashPass(String pass) {
        return BCrypt.with(bcryptVersion, strategy).hashToString(6, pass.toCharArray());
    }

    public static boolean verifyPass(String pass, String hash) {
        return BCrypt.verifyer(bcryptVersion, strategy).verify(pass.toCharArray(), hash.toCharArray()).verified;
    }

    public static String urlEncodeString(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    public static String urlDecodeString(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

}
