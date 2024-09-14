package com.example.demo.controller;


import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.config.JwtTokenUtil;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.RegisterRequestDto;
import com.example.demo.modal.FailedLoginAttempt;
import com.example.demo.modal.User;
import com.example.demo.repo.FailedLoginAttemptRepository;
import com.example.demo.repo.UserRepository;
import com.example.demo.service.OtpService;


@Configuration
@RestController
@RequestMapping("/account")
public class UserController {

	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private FailedLoginAttemptRepository failedLoginAttemptRepository;
	
	@Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;
	
	

	/*==============================================
	BELOW API USED TO INSERT USERS MANUALLY BY ADMIN
	================================================*/
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody final RegisterRequestDto dto) {
		System.out.println("===========Registered Method calling==========");
		System.out.println("======Mobile======="+dto.getMobile());

	    if (userRepository.findByUsername(dto.getUsername()) != null) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body("Username Already Exists");
	    } else if (userRepository.findByMobile(dto.getMobile().replace("+", ""))!= null) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body("Contact number Already Exists");
	    }

	    String otp = otpService.generateOtp(dto.getMobile());
	    otpService.sendOtp(dto.getMobile(), otp);

	    User user = new User();
	    user.setUsername(dto.getUsername());
//	    user.setMobile(dto.getMobile());
	    user.setMobile(dto.getMobile().replace("+", "")); 
	    user.setPassword(passwordEncoder.encode(dto.getPassword()));
	    user.setPassCode(dto.getPassword());
	    user.setStatus("PENDING"); // Set status to pending
	    user.setRole(dto.getRole()); // Assign the role
	    userRepository.save(user);

	    return ResponseEntity.status(HttpStatus.CREATED).body("User Created Successfully. Please verify OTP sent to your mobile.");
	}

	@PostMapping("/verifyOtp")
	public ResponseEntity<?> verifyOtp(@RequestParam String mobile, @RequestParam String otp) {
		
		System.out.println("======Mobile======="+mobile);
		System.out.println("======otp======="+otp);
	    
	    if (otpService.verifyOtp(mobile, otp)) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
	    }

	    User user = userRepository.findByMobile(mobile);
	    System.out.println("======user======="+user);
	    if (user == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
	    }

	    user.setStatus("ACTIVE"); 
	    userRepository.save(user); 

	    return ResponseEntity.status(HttpStatus.OK).body("User verified successfully and activated.");
	}	
	
	
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
	    User user = userRepository.findByUsername(dto.getUsername());

	    if (user == null) {
	        recordFailedLoginAttempt(dto.getUsername());
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
	    }


	    if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
	        recordFailedLoginAttempt(dto.getUsername());
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
	    }

	    if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
	    	
	        String token = JwtTokenUtil.generateToken(user.getUsername());

	        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
	            return ResponseEntity.status(HttpStatus.OK).body("Welcome Admin! You have full access. Token: " + token);
	        } else if ("USER".equalsIgnoreCase(user.getRole())) {
	            return ResponseEntity.status(HttpStatus.OK).body("Welcome User! You have limited access. Token: " + token);
	        } else {
	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
	        }
	    } else {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Please verify your OTP to activate your account.");
	    }
	}

	private void recordFailedLoginAttempt(String username) {
		
	    FailedLoginAttempt attempt = new FailedLoginAttempt();
	    attempt.setUsername(username);
	    attempt.setTimestamp(LocalDateTime.now());
	    failedLoginAttemptRepository.save(attempt);
	    
	}

	
//	@PostMapping("/login")
//	public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
//	    User user = userRepository.findByUsername(dto.getUsername());
//
//	    // Check if user exists and password is correct
//	    if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
//	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
//	    }
//
//	    // Check user status
//	    if ("ACTIVE".equalsIgnoreCase(user.getStatus())) {
//	        // Generate token if user is active
//	        String token = JwtTokenUtil.generateToken(user.getUsername());
//
//	        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
//	            return ResponseEntity.status(HttpStatus.OK).body("Welcome Admin! You have full access. Token: " + token);
//	        } else if ("USER".equalsIgnoreCase(user.getRole())) {
//	            return ResponseEntity.status(HttpStatus.OK).body("Welcome User! You have limited access. Token: " + token);
//	        } else {
//	            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied");
//	        }
//	    } else {
//	        // Prompt user to verify OTP if not active
//	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Please verify your OTP to activate your account.");
//	    }
//	}




}