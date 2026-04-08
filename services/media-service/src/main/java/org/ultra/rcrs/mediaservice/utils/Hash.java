package org.ultra.rcrs.mediaservice.utils;

import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class Hash {

    @SneakyThrows
    public static String sha1(byte[] data) {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(data);

        StringBuilder hex = new StringBuilder();

        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }

    @SneakyThrows
    public static String sha1Base64(String data) {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(hash);
    }
}