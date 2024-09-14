package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.demo.modal.User;


public interface UserRepository extends JpaRepository<User , String> {

	@Query("SELECT u from User u where u.username=?1")	
	User findByUsername(String username);
	
	@Query("SELECT u from User u where TRIM(LOWER(u.mobile)) = TRIM(LOWER(?1))")
	User findByMobile(String mobile);

//	@Query("SELECT u from User u where u.mobile=?1")	
//	User findByMobile(String mobile);


}
