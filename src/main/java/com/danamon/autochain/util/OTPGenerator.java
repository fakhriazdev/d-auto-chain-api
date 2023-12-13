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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Getter
@Setter
public class OTPGenerator {
    //secret key for otp
    private static final String secret = OTP.randomBase32(64);
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

    public static String generateOtp() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        return OTP.create(secret, hexTimes, otpDigits, Type.TOTP);
    }

    public static Boolean verifyOtp(OtpRequest otpRequest) throws NoSuchAlgorithmException, InvalidKeyException{

        return OTP.verify(otpRequest.getSecret(),otpRequest.getCounter(),otpRequest.getCode(),otpDigits,Type.TOTP);
    }

    public static String generateURL(String email){
        return OTP.getURL(secret,otpDigits,Type.TOTP,issuer,email);
    }

    public static OtpResponse getOtp() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        return OtpResponse.builder()
                .secret(secret)
                .code(generateOtp())
                .period(hexTimes)
                .build();
    }
}
