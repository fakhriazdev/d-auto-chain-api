package com.danamon.autochain.util;

import com.amdelamar.jotp.OTP;
import com.amdelamar.jotp.type.Type;
import com.danamon.autochain.dto.auth.OtpRequest;
import com.danamon.autochain.dto.auth.OtpResponse;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class OTPGenerator {
    //secret key for otp
    private static final String secret = OTP.randomBase32(65);
    private static final int otpDigits = 6;
    private static final int period = 120;
    private static final String issuer = "danamon";

    public static OtpResponse generateOtp(String email) throws NoSuchAlgorithmException, InvalidKeyException, IOException, URISyntaxException {

        String timeInHex = OTP.timeInHex(System.currentTimeMillis(), 1);
        String code = OTP.create(secret, timeInHex, otpDigits, Type.TOTP);
        URL url = new URI("http://localhost:5173/verifyOtp?secret=" + secret + "&digit=" + otpDigits + "&period=" + timeInHex + "&email=" + email).toURL();
        return OtpResponse.builder()
                .issuer(issuer)
                .secret(secret)
                .code(code)
                .email(email)
                .url(url)
                .build();
    }

    public static Boolean verifyOtp(OtpRequest otpRequest) throws NoSuchAlgorithmException, InvalidKeyException {
//        String counter = OTP.timeInHex(System.currentTimeMillis(),otpRequest.getPeriod());
        return OTP.verify(otpRequest.getSecret(),otpRequest.getPeriod(),otpRequest.getCode(),otpRequest.getDigits(),Type.TOTP);
    }

    public static URL generateURL(String email) throws URISyntaxException, MalformedURLException {
        return new URI("http://localhost:5173/verifyOtp?secret="+secret+"&digit="+otpDigits+"&period="+period+"&email="+email).toURL();
    }
}
