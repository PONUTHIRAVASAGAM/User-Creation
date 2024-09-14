package com.example.demo.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.modal.FailedLoginAttempt;

public interface FailedLoginAttemptRepository extends JpaRepository<FailedLoginAttempt, Long> {
    List<FailedLoginAttempt> findByUsername(String username);
}
