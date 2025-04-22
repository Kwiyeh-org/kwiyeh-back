package com.kwiyeh.back.utils;

import java.util.Random;

public class MyAppFunctions {
     public static String GenerateForgetPasswordCode() {
        Random random = new Random();
        
        // Generate random number between 1 and 9999 (inclusive)
        int randomNumber = random.nextInt(9999) + 1;
        
        // Format with leading zeros to ensure 4 digits
        String formattedNumber = String.format("%04d", randomNumber);
        
        return formattedNumber;
    }
}
