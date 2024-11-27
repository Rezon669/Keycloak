package com.app.keycloak.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.app.keycloak.entity.UserDto;
import com.app.keycloak.exceptions.KeycloakException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
@Component
public class UsersService {

	@Value("${baseUrl}")
	private String baseUrl;

	@Value("${clientId}")
	private String clientId;

	@Value("${clientSecret}")
	private String clientSecret;

	@Value("${granttype}")
	private String grantType;

	@Value("${realm}")
	private String realm;

	private String accessToken;
	private static final Logger logger = LogManager.getLogger(UsersService.class);
	private List<UserDto> users = new ArrayList<>();

	// Method to call an API
	private static ResponseEntity<String> callApi(String endpoint, HttpEntity<?> httpEntity, HttpMethod httpMethod) {
		RestTemplate restTemplate = new RestTemplate();
		logger.info("Calling API at endpoint: {}", endpoint);
		return restTemplate.exchange(endpoint, httpMethod, httpEntity, String.class);
	}

	// Method to get admin token
	public String getAdminToken() {
		logger.info("Requesting admin token from Keycloak");
		String tokenUrl = baseUrl + "/realms/" + realm + "/protocol/openid-connect/token";

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", grantType);
		map.add("client_id", clientId);
		map.add("client_secret", clientSecret);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		try {
			ResponseEntity<String> response = callApi(tokenUrl, request, HttpMethod.POST);

			if (response.getStatusCode().is2xxSuccessful()) {
				ObjectMapper mapper = new ObjectMapper();
				String accessToken = mapper.readTree(response.getBody()).get("access_token").asText();
				logger.info("Admin token successfully retrieved.");
				return accessToken;
			} else {
				logger.error("Failed to retrieve token. HTTP Status: {}", response.getStatusCode());
				throw new KeycloakException("Failed to retrieve token. HTTP Status: " + response.getStatusCode());
			}
		} catch (Exception e) {
			logger.error("Error while generating the token: {}", e.getMessage(), e);
			throw new KeycloakException("Error getting admin token: " + e.getMessage(), e);
		}
	}

	// Method to check if token is expired or null
	private boolean isTokenExpired() {
		return accessToken == null || accessToken.isEmpty();
	}

	// Method to refresh token if expired
	private void refreshTokenIfNeeded() {
		if (isTokenExpired()) {
			logger.warn("Access token is expired or null, refreshing token.");
			accessToken = getAdminToken();
		}
	}

	public List<UserDto> getUsers() {
		logger.info("Fetching users from Keycloak.");
		refreshTokenIfNeeded(); // Ensure token is valid

		String url = baseUrl + "/admin/realms/" + realm + "/users";

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setBearerAuth(accessToken);

		HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
		try {
			ResponseEntity<String> response = callApi(url, httpEntity, HttpMethod.GET);

			if (response != null && response.getStatusCode().is2xxSuccessful()) {
				users.clear();

				ObjectMapper mapper = new ObjectMapper();
				ArrayNode jsonArray = (ArrayNode) mapper.readTree(response.getBody());

				for (int i = 0; i < jsonArray.size(); i++) {
					ObjectNode userObj = (ObjectNode) jsonArray.get(i);
					String username = userObj.get("username").asText();
					String firstname = userObj.get("firstName").asText();
					String lastname = userObj.get("lastName").asText();
					String email = userObj.get("email").asText();
					ObjectNode attributes = (ObjectNode) userObj.get("attributes");
					String city = attributes.get("city").get(0).asText();
					String phno = attributes.get("phno").get(0).asText();

					users.add(new UserDto(username, firstname, lastname, email, phno, city));
				}

				logger.info("Successfully fetched {} users from Keycloak.", users.size());
			} else {
				logger.error("Error while fetching users. HTTP Status: {}", response.getStatusCode());
				throw new KeycloakException(
						"Getting error when fetching the User details. HTTP Status: " + response.getStatusCode());
			}
		} catch (Exception e) {
			logger.error("Error while fetching users info: {}", e.getMessage(), e);
			throw new KeycloakException("Error fetching users from Keycloak: " + e.getMessage(), e);
		}
		return users;
	}

	public String addUser(UserDto user) throws JsonProcessingException {
		logger.info("Adding new user to Keycloak: {}", user.getUsername());
		refreshTokenIfNeeded(); // Ensure token is valid

		String url = baseUrl + "/admin/realms/" + realm + "/users";
		ObjectMapper mapper = new ObjectMapper();

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
		httpHeaders.setBearerAuth(accessToken);

		ObjectNode jsonBody = mapper.createObjectNode();
		jsonBody.put("username", user.getUsername());
		jsonBody.put("firstName", user.getFirstName());
		jsonBody.put("lastName", user.getLastName());
		jsonBody.put("email", user.getEmailid());
		jsonBody.put("emailVerified", true);
		jsonBody.put("enabled", true);

		ArrayNode credentials = mapper.createArrayNode();
		ObjectNode creds = mapper.createObjectNode();
		creds.put("type", "password");
		creds.put("value", user.getPassword());
		creds.put("temporary", false);
		credentials.add(creds);
		jsonBody.set("credentials", credentials);

		ObjectNode attributes = mapper.createObjectNode();
		ArrayNode cityArray = mapper.createArrayNode();
		cityArray.add(user.getCity());
		attributes.set("city", cityArray);

		ArrayNode phnoArray = mapper.createArrayNode();
		phnoArray.add(user.getMobilenumber());
		attributes.set("phno", phnoArray);

		jsonBody.set("attributes", attributes);

		HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(jsonBody), httpHeaders);

		try {
			ResponseEntity<String> response = callApi(url, entity, HttpMethod.POST);

			if (response != null && response.getStatusCode().is2xxSuccessful()) {
				logger.info("User {} created successfully.", user.getUsername());
				return "User created successfully";
			} else {
				logger.error("Failed to create user {}. HTTP Status: {}", user.getUsername(), response.getStatusCode());
				throw new KeycloakException("Failed to create user. HTTP Status: " + response.getStatusCode());
			}
		} catch (Exception e) {
			logger.error("Error creating user {}: {}", user.getUsername(), e.getMessage(), e);
			throw new KeycloakException("Error creating user: " + e.getMessage(), e);
		}
	}

	public List<UserDto> getUserInfo(String uname) {
		logger.info("Fetching user info for username: {}", uname);
		refreshTokenIfNeeded(); // Ensure token is valid

		String userUrl = baseUrl + "/admin/realms/" + realm + "/users?username=" + uname;

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setBearerAuth(accessToken);

		HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

		try {
			ResponseEntity<String> response = callApi(userUrl, httpEntity, HttpMethod.GET);

			if (response != null && response.getStatusCode().is2xxSuccessful()) {
				users.clear();
				ObjectMapper mapper = new ObjectMapper();
				ArrayNode jsonArray = (ArrayNode) mapper.readTree(response.getBody());
				if (jsonArray.size() != 0) {
					for (int i = 0; i < jsonArray.size(); i++) {
						ObjectNode userObj = (ObjectNode) jsonArray.get(i);
						String username = userObj.has("username") ? userObj.get("username").asText() : null;
						String firstname = userObj.has("firstName") ? userObj.get("firstName").asText() : null;
						String lastname = userObj.has("lastName") ? userObj.get("lastName").asText() : null;
						String email = userObj.has("email") ? userObj.get("email").asText() : null;

						// Handle attributes field
						ObjectNode attributes = userObj.has("attributes") ? (ObjectNode) userObj.get("attributes")
								: null;
						String city = null;
						String phno = null;

						if (attributes != null) {
							city = attributes.has("city") && attributes.get("city").isArray()
									? attributes.get("city").get(0).asText()
									: null;
							phno = attributes.has("phno") && attributes.get("phno").isArray()
									? attributes.get("phno").get(0).asText()
									: null;
						}
						users.add(new UserDto(username, firstname, lastname, email, phno, city));
					}

					logger.info("Successfully fetched user info for username: {}", uname);
				}
			} else {
				logger.error("Error fetching user info for username {}. HTTP Status: {}", uname,
						response.getStatusCode());
				throw new KeycloakException("Error fetching user info. HTTP Status: " + response.getStatusCode());
			}
		} catch (Exception e) {
			logger.error("Error fetching user info for username {}: {}", uname, e.getMessage(), e);
			throw new KeycloakException("Error fetching user info: " + e.getMessage(), e);
		}
		return users;
	}

	public Object deleteUser(String uname) {
		logger.info("Fetching user info for username: {}", uname);
		String userId = null;
		refreshTokenIfNeeded(); // Ensure token is valid

		// Construct the URL to fetch user info based on username
		String userUrl = baseUrl + "/admin/realms/" + realm + "/users?username=" + uname;

		// Set headers with Bearer token
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setBearerAuth(accessToken);

		HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);

		try {
			// Make the GET request to fetch user info
			ResponseEntity<String> response = callApi(userUrl, httpEntity, HttpMethod.GET);

			if (response != null && response.getStatusCode().is2xxSuccessful()) {
				// Parse the response body to extract the user ID
				ObjectMapper mapper = new ObjectMapper();
				ArrayNode jsonArray = (ArrayNode) mapper.readTree(response.getBody());

				boolean userFound = false; // Flag to check if user is found

				for (int i = 0; i < jsonArray.size(); i++) {
					ObjectNode userObj = (ObjectNode) jsonArray.get(i);
					String username = userObj.get("username").asText();

					// Check if the username matches
					if (uname.equals(username)) {
						userId = userObj.get("id").asText();
						logger.info("Found user with username: {} and ID: {}", uname, userId);

						// Construct the delete URL with the user ID
						String deleteUrl = baseUrl + "/admin/realms/" + realm + "/users/" + userId;

						// Set headers for deletion
						HttpHeaders deleteHeaders = new HttpHeaders();
						deleteHeaders.setBearerAuth(accessToken);
						HttpEntity<String> deleteEntity = new HttpEntity<>(deleteHeaders);

						try {
							// Make the DELETE request to delete the user
							ResponseEntity<String> apiResponse = callApi(deleteUrl, deleteEntity, HttpMethod.DELETE);

							if (apiResponse.getStatusCode().is2xxSuccessful()) {
								logger.info("User with username {} deleted successfully", uname);
							} else {
								logger.error("Error when deleting user {}. HTTP Status: {}", uname,
										apiResponse.getStatusCode());
							}
						} catch (Exception e) {
							logger.error("Error when deleting the user {}: {}", uname, e.getMessage(), e);
							throw new KeycloakException("Error when deleting the user: " + e.getMessage(), e);
						}

						userFound = true; // Set flag to true
						break; // Exit loop after finding and deleting the user
					}
				}

				if (!userFound) {
					logger.warn("No user found with username: {}", uname);
					return "User not found"; // You can return a message indicating the user was not found
				}
			} else {
				logger.error("Error fetching user info for username {}. HTTP Status: {}", uname,
						response.getStatusCode());
				throw new KeycloakException("Error fetching user info. HTTP Status: " + response.getStatusCode());
			}
		} catch (Exception e) {
			logger.error("Error fetching user info for username {}: {}", uname, e.getMessage(), e);
			throw new KeycloakException("Error fetching user info: " + e.getMessage(), e);
		}

		return null; // Return null or any appropriate response as needed
	}

	public String loginValidation(String username, String password) {
		logger.info("Requesting user token from Keycloak");
		String tokenUrl = baseUrl + "/realms/" + realm + "/protocol/openid-connect/token";

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", "password");
		map.add("client_id", clientId);
		map.add("client_secret", clientSecret);
		map.add("username", username);
		map.add("password", password);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		try {
			ResponseEntity<String> response = callApi(tokenUrl, request, HttpMethod.POST);

			if (response.getStatusCode().is2xxSuccessful()) {
				ObjectMapper mapper = new ObjectMapper();
				String accessToken = mapper.readTree(response.getBody()).get("access_token").asText();
				logger.info("User token successfully retrieved.");
				return accessToken;
			} else {
				logger.error("Failed to retrieve token. HTTP Status: {}", response.getStatusCode());
				throw new KeycloakException("Failed to retrieve token. HTTP Status: " + response.getStatusCode());
			}
		} catch (Exception e) {
			logger.error("Error while generating the token: {}", e.getMessage(), e);
			throw new KeycloakException("Error getting when generating token: " + e.getMessage(), e);
		}
	}

}