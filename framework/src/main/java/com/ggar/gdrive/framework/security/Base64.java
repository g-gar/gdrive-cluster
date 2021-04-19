package com.ggar.gdrive.framework.security;

public class Base64 {

    public static String encode(String str) {
        return java.util.Base64.getEncoder().encodeToString(str.getBytes());
    }

    public static String decode(String str) {
        return new String(java.util.Base64.getDecoder().decode(str));
    }
}
