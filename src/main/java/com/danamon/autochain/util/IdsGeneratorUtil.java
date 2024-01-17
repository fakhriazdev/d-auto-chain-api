package com.danamon.autochain.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.Month;

public class IdsGeneratorUtil {
    public static String generate(String param1, String param2) throws NoSuchAlgorithmException, IOException, URISyntaxException, InvalidKeyException {
        String rawYear = String.valueOf(LocalDateTime.now().getYear()) ;
        String year = String.valueOf(rawYear.charAt(2) + rawYear.charAt(3));
        String month = getMonth(LocalDateTime.now().getMonth());
        String code = OTPGenerator.generateOtp("ids").getCode();


        return param1 + "/" +
                param2 + "/" +
                year + month + "/" +
                code;
    }

    private static String getMonth(Month month){
        return switch (month){
            case JANUARY -> "01";
            case FEBRUARY -> "02";
            case MARCH -> "03";
            case APRIL -> "04";
            case MAY -> "05";
            case JUNE -> "06";
            case JULY -> "07";
            case AUGUST -> "08";
            case SEPTEMBER -> "09";
            case OCTOBER -> "10";
            case NOVEMBER -> "11";
            case DECEMBER -> "12";
        };
    }

}
