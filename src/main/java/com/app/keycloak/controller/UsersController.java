package com.app.keycloak.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.keycloak.entity.UserDto;
import com.app.keycloak.exceptions.ErrorResponse;
import com.app.keycloak.exceptions.KeycloakException;
import com.app.keycloak.service.UsersService;
import com.fasterxml.jackson.core.JsonProcessingException;

@Controller
@RequestMapping("/easybuy/user")

public class UsersController {

	private static final Logger logger = LogManager.getLogger(UsersController.class);

	@Autowired
	UsersService usersService;

	@PostMapping("/adduser")
	public ResponseEntity<Object> addUser(@RequestBody UserDto user) {

		try {

			try {
				usersService.addUser(user);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("User added Successfully");

			return new ResponseEntity<>(user, HttpStatus.ACCEPTED);

		} catch (KeycloakException e) {

			logger.error("Getting error when adding the user");

			return new ResponseEntity<>(new ErrorResponse("Error while adding user", e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	@GetMapping("/getusers")
	public ResponseEntity<Object> getUser() {
		List<UserDto> userDto;

		try {

			userDto = usersService.getUsers();
			logger.info("Users info fetched from keycloak server");

			return new ResponseEntity<>(userDto, HttpStatus.OK);

		} catch (KeycloakException e) {

			logger.error("Getting error when fetching the user details");

			return new ResponseEntity<>(new ErrorResponse("Error fetching users", e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

}
