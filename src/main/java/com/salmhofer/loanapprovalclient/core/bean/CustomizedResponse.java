package com.salmhofer.loanapprovalclient.core.bean;

public class CustomizedResponse {
	
	private String message;
	private String statusCode;
	private String session;
	
	public CustomizedResponse() {
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getStatusCode() {
		return statusCode;
	}
	
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	
	public String getSession() {
		return session;
	}
	
	public void setSession(String session) {
		this.session = session;
	}
}
