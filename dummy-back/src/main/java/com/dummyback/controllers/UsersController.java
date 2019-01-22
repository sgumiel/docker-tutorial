package com.dummyback.controllers;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dummyback.models.User;
import com.dummyback.repositories.UsersRepository;

@RestController
public class UsersController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	@Autowired
	private UsersRepository usersRepository;
	
	
	@GetMapping("/users")
	public List<User> getUsers(){
		
		LOGGER.debug("Request for all users");
		
		return (List)this.usersRepository.findAll();
	}
	
	@PostMapping("users")
	public User saveUser(final User user) {
		
		LOGGER.debug("Request for a new user");
		final User u = this.usersRepository.save(user);
		
		return u;
	}

}
