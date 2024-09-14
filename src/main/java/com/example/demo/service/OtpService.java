package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private Map<String, String> otpStore = new HashMap<>();

    public String generateOtp(String mobile) {
        String otp = String.format("%06d", new Random().nextInt(999999));

        otpStore.put(mobile, otp);

        return otp;
    }
    
    public void sendOtp(String mobile, String otp) {
        System.out.println("Sending OTP " + otp + " to mobile: " + mobile);
    }

    public boolean verifyOtp(String mobile, String otp) {
    	
        String storedOtp = otpStore.get(mobile);

        if (storedOtp != null && storedOtp.equals(otp)) {
            
            otpStore.remove(mobile); 
            return true;
        }
        return false;
    }
}
