package com.app.keycloak.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.keycloak.entity.UserDto;
import com.app.keycloak.exceptions.ErrorResponse;
import com.app.keycloak.exceptions.KeycloakException;
import com.app.keycloak.service.UsersService;
import com.fasterxml.jackson.core.JsonProcessingException;

@Controller
@RequestMapping("/easybuy/")

public class UsersController {

	private static final Logger logger = LogManager.getLogger(UsersController.class);

	@Autowired
	UsersService usersService;

	@PostMapping("/admin/adduser")
	public ResponseEntity<Object> addUser(@RequestBody UserDto user) {

		try {

			try {
				usersService.addUser(user);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("User added Successfully");

			return new ResponseEntity<>(user, HttpStatus.CREATED);

		} catch (KeycloakException e) {

			logger.error("Getting error when adding the user");

			return new ResponseEntity<>(new ErrorResponse("Error while adding user", e.getMessage()),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	@GetMapping("/admin/getusers")
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

	@GetMapping("/admin/getuser")
	public ResponseEntity<Object> getUserInfo(@RequestParam("username") String username) {

		Object userDto = null;
		try {
			userDto = usersService.getUserInfo(username);
			logger.info("User info fetched from keycloak successfully");
			return new ResponseEntity<>(userDto, HttpStatus.FOUND);
		} catch (KeycloakException e) {
			logger.error("Getting error while fetching the user info");
			return new ResponseEntity<>(new ErrorResponse("Error while fetching the user info", e.getMessage()),
					HttpStatus.NOT_FOUND);
		}

	}

	@PostMapping("/admin/loginvalidation")
	public ResponseEntity<Object> loginValidation(@RequestParam("username") String username,
			@RequestParam("password") String password) {
		try {
			String accessToken = usersService.loginValidation(username,password);
			logger.info("Generated the token for the user");
			return new ResponseEntity<>("Please find the token:  " + accessToken, HttpStatus.OK);
		} catch (KeycloakException e) {
			logger.error("Getting error while fetching the user info");
			return new ResponseEntity<>(new ErrorResponse("Error while fetching the user info", e.getMessage()),
					HttpStatus.UNAUTHORIZED);
		}

	}

	@DeleteMapping("/admin/delete/user")
	public ResponseEntity<Object> deletUser(@RequestParam("username") String username) {

		try {
			usersService.deleteUser(username);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (KeycloakException e) {
			logger.error("Getting error when deleting the user");
			return new ResponseEntity<>(new ErrorResponse("Error when deleting the user", e.getMessage()),
					HttpStatus.NOT_FOUND);
		}

	}

}
