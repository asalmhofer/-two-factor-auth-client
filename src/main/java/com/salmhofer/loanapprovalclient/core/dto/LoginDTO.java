package com.salmhofer.loanapprovalclient.core.dto;

import java.io.Serializable;

public class LoginDTO implements Serializable{
	
	private static final long serialVersionUID = 5647101244475096484L;
	
	private String username;
	private String password;
	private Integer pin;
	private String session;
	private String businessKey;
	
	public LoginDTO() {

	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public Integer getPin() {
		return pin;
	}
	
	public void setPin(Integer pin) {
		this.pin = pin;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override
	public String toString() {
		return getUsername() + "; " + getPassword() + "; " + getPin();
	}
	
	public String getSession() {
		return session;
	}
	
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public String getBusinessKey() {
		return businessKey;
	}
	
	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}
	
}
