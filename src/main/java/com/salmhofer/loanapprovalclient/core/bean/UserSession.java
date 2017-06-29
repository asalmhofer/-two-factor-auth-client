package com.salmhofer.loanapprovalclient.core.bean;

public class UserSession {
	
	private String username;
	private String password;
	private Integer pin;
	private String session;
	private String businessKey;
	
	public UserSession() {

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
	
	public void setSession(String session) {
		this.session = session;
	}
	
	public String getBusinessKey() {
		return businessKey;
	}
	
	public void setBusinessKey(String businessKey) {
		this.businessKey = businessKey;
	}
}
