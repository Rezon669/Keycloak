package com.app.keycloak.service;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
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
    
    String tokenUrl ;
    

    
    List<UserDto> users = new ArrayList<>();

    // Method to call an API
    private static ResponseEntity<String> callApi(String endpoint, HttpEntity<?> httpEntity, HttpMethod httpMethod) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.exchange(endpoint, httpMethod, httpEntity, String.class);
    }

    // Method to get admin token
    public String getAdminToken() {
         tokenUrl = baseUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", grantType);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
        

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<String> response = callApi(tokenUrl, request, HttpMethod.POST);

            if(response.getStatusCode().is2xxSuccessful()) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                String accessToken = jsonResponse.getString("access_token");
                return accessToken;
            } else {
                throw new KeycloakException("Failed to retrieve token. HTTP Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new KeycloakException("Error getting admin token: " + e.getMessage(), e);
        }
    }
    

	public String loginValidation(String emailid, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	public void updatePassword(String password, String confirmpassword, String emailid) {
		// TODO Auto-generated method stub
		
	}

	public List<UserDto> getUsers() {
		// TODO Auto-generated method stub
		if(tokenUrl == null) {
			tokenUrl = getAdminToken();
		}
		
		String url = baseUrl + "/admin/realms/" + realm +"/users";
		
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setBearerAuth(tokenUrl);
		//httpHeaders.setContentType("application/json");
		
		HttpEntity<String> httpEntity = new HttpEntity<String>(httpHeaders);
		try {
		ResponseEntity<String> response = callApi(url, httpEntity, HttpMethod.GET);
		
		if(response!= null && response.getStatusCode().is2xxSuccessful()) {
			  users.clear();
			
			JSONArray jsonArray = new JSONArray(response.getBody());
			
			for(int i=0; i<=jsonArray.length()-1; i++) {
				JSONObject userObj = jsonArray.getJSONObject(i);
				String username = userObj.getString("username");
				String firstname = userObj.getString("firstName");
				String lastname = userObj.getString("lastName");
				String email = userObj.getString("email");
				JSONObject attributes = userObj.getJSONObject("attributes");
				String city = attributes.getJSONArray("city").getString(0);
				String phno = attributes.getJSONArray("phno").getString(0);	
				
				users.add(new UserDto(username,firstname,lastname,email,phno,city));
					
			}
			
		}else{
			throw new KeycloakException("Getting error when fetching the User details" + response.getStatusCode());
		}
		}catch(Exception e) {
			throw new KeycloakException("Error fetching users from Keycloak: " + e.getMessage(), e);
		}
		return users;	
		
	}

    public String addUser(UserDto user) throws JsonProcessingException {

        // Check if tokenUrl is null, and if so, obtain a new token
        if (tokenUrl == null) {
            tokenUrl = getAdminToken();
        }

        String url = baseUrl + "/admin/realms/" + realm + "/users";
        ObjectMapper mapper = new ObjectMapper();

        // Set HTTP headers with content type and Bearer auth token
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(tokenUrl);

        // Create the JSON body for the user
        ObjectNode jsonBody = mapper.createObjectNode();
        jsonBody.put("username", user.getUsername());
        jsonBody.put("firstName", user.getFirstName());
        jsonBody.put("lastName", user.getLastName());
        jsonBody.put("email", user.getEmailid());
        jsonBody.put("emailVerified", false); // Assuming email verification is false
        jsonBody.put("enabled", true); // User enabled by default

        // Add "attributes" (city, phno)
        ObjectNode attributes = mapper.createObjectNode();
        ArrayNode cityArray = mapper.createArrayNode();
        cityArray.add(user.getCity());
        attributes.set("city", cityArray);

        ArrayNode phnoArray = mapper.createArrayNode();
        phnoArray.add(user.getMobilenumber());  // Assuming `phno` is a field in `UserDto`
        attributes.set("phno", phnoArray);

        // Add attributes to the main JSON body
        jsonBody.set("attributes", attributes);
        
       

        // Wrap the JSON body and headers in an HttpEntity
        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(jsonBody), httpHeaders);

        try {
            // Make the POST request
            ResponseEntity<String> response = callApi(url, entity, HttpMethod.POST);

            if (response.getStatusCode().is2xxSuccessful()) {
                return "User created successfully";
            } else {
                // Handle unsuccessful response
                throw new KeycloakException("Failed to create user. HTTP Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            // Handle exceptions and provide a meaningful error message
            throw new KeycloakException("Error creating user: " + e.getMessage(), e);
        }
    }
}
