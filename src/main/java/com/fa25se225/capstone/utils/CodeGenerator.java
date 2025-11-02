package com.fa25se225.capstone.utils;

import java.util.Random;

public class CodeGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random RANDOM = new Random();


    public static String generateRandomCode(int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return result.toString();
    }

    public static String generateRandomCode() {
        return generateRandomCode(6);
    }

    public static String generateCodeFromName(String name) {
        if (name == null || name.isEmpty()) {
            return generateRandomCode(6);
        }
        
        String prefix = name.length() >= 3 
            ? name.substring(0, 3).toUpperCase() 
            : String.format("%-3s", name).replace(' ', 'X').toUpperCase();
        
        int randomNum = RANDOM.nextInt(1000);
        return prefix + String.format("%03d", randomNum);
    }
}
