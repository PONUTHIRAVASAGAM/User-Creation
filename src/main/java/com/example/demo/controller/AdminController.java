package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.RegisterRequestDto;
import com.example.demo.modal.User;
import com.example.demo.repo.UserRepository;

@Configuration
@RestController
@RequestMapping("/admin")
public class AdminController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
    private PasswordEncoder passwordEncoder;
	
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody final RegisterRequestDto dto) {
	    System.out.println("=========== Registered Method calling ===========");
	    System.out.println("====== Username =======" + dto.getUsername());

	    if (userRepository.findByUsername(dto.getUsername()) != null) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body("Username Already Exists");
	    }

	    User user = new User();
	    user.setUsername(dto.getUsername());
	    user.setPassword(passwordEncoder.encode(dto.getPassword()));
	    user.setPassCode(dto.getPassword());
	    user.setRole(dto.getRole());

	    if ("ADMIN".equalsIgnoreCase(dto.getRole())) {
	        user.setStatus("ACTIVE"); 
	    } else {
	        user.setStatus("PENDING"); 
	    }

	    userRepository.save(user);

	    if ("ADMIN".equalsIgnoreCase(dto.getRole())) {
	        return ResponseEntity.status(HttpStatus.CREATED).body("Admin User Created Successfully.");
	    } else {
	        return ResponseEntity.status(HttpStatus.CREATED).body("User Created Successfully. Please wait for further instructions.");
	    }
	}


}
