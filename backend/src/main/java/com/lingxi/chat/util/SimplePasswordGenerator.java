package com.lingxi.chat.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SimplePasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String hashedPassword = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hashed: " + hashedPassword);
        
        // 验证
        boolean matches = encoder.matches(password, hashedPassword);
        System.out.println("Verification: " + matches);
    }
}