package com.danamon.autochain.util;

import com.amdelamar.jotp.OTP;
import com.amdelamar.jotp.type.Type;
import com.danamon.autochain.dto.auth.OtpRequest;
import com.danamon.autochain.dto.auth.OtpResponse;
import com.danamon.autochain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Getter
@Setter
public class OTPGenerator {
    //secret key for otp
    private static final String secret = OTP.randomBase32(65);
    private static final int otpDigits = 6;
    private static final int period = 120;
    private static final String hexTimes;
    private static final String issuer = "danamon";

    static {
        try {
            hexTimes = OTP.timeInHex(System.currentTimeMillis(),period);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateOtp() throws NoSuchAlgorithmException, InvalidKeyException {
        return OTP.create(secret, hexTimes, otpDigits, Type.TOTP);
    }

    public static Boolean verifyOtp(OtpRequest otpRequest) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        String counter = OTP.timeInHex(System.currentTimeMillis(),otpRequest.getPeriod());
        return OTP.verify(otpRequest.getSecret(),counter,otpRequest.getCode(),otpRequest.getDigits(),Type.TOTP);
    }

    public static URL generateURL(String email) throws URISyntaxException, MalformedURLException {
        return new URI("http://localhost:5173/verifyOtp?secret="+secret+"&digit="+otpDigits+"&period="+period+"&email="+email).toURL();
    }

    public static OtpResponse getOtp() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        return OtpResponse.builder()
                .secret(secret)
                .code(generateOtp())
                .period(period)
                .build();
    }
}
