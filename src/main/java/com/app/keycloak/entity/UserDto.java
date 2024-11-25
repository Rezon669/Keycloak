package com.app.keycloak.entity;

public class UserDto {

	private String username;
	private String firstName;
	private String lastName;
	private String emailid;
	private String mobilenumber;
	private String password;
	private String city;
	
	
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmailid() {
		return emailid;
	}
	public void setEmailid(String emailid) {
		this.emailid = emailid;
	}
	public String getMobilenumber() {
		return mobilenumber;
	}
	public void setMobilenumber(String mobilenumber) {
		this.mobilenumber = mobilenumber;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}

	public UserDto( String username, String firstName, String lastName, String emailid, String mobilenumber,
			String city) {
		super();
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.emailid = emailid;
		this.mobilenumber = mobilenumber;
		this.city = city;
	}

}
