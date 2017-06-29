package com.salmhofer.loanapprovalclient.core.controller;

import java.util.HashMap;
import java.util.logging.Logger;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.salmhofer.loanapprovalclient.core.bean.CustomizedResponse;
import com.salmhofer.loanapprovalclient.core.bean.UserSession;
import com.salmhofer.loanapprovalclient.core.dto.LoginDTO;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

@Controller
public class MainController {
	
	private final static Logger logger = Logger.getLogger("MainController");
	
	private HashMap<String, UserSession> authStore = new HashMap<String, UserSession>();
	
    @RequestMapping(value="/",method = RequestMethod.GET)
    public String homepage(){
        return "index";
    }
    
    /**
     * Listner for user/password authentication (1st step)
     * @param loginDTO
     */
    @RequestMapping(value = "/login-username", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
    public CustomizedResponse loginWithUsername(@RequestBody LoginDTO loginDTO){
        System.out.println("Received request (backend controller): " + loginDTO);
        
        UserSession session = new UserSession();
        session.setPassword(loginDTO.getPassword());
        session.setUsername(loginDTO.getUsername());
        session.setPin(loginDTO.getPin());
        session.setBusinessKey(loginDTO.getBusinessKey());
        
        CustomizedResponse r = triggerAuthWorkflowFirstStep(session);
    	return r;
    }
    
    /**
     * Listner for PIN authentication (2nd step)
     * @param loginDTO
     */
    @RequestMapping(value = "/login-pin", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
    public CustomizedResponse loginWithPin(@RequestBody LoginDTO loginDTO){
        System.out.println("Received request (backend controller): " + loginDTO);
        
        UserSession session = new UserSession();
        session.setPassword(loginDTO.getPassword());
        session.setUsername(loginDTO.getUsername());
        session.setPin(loginDTO.getPin());
        session.setSession(loginDTO.getSession());
        session.setBusinessKey(loginDTO.getBusinessKey());
        
        CustomizedResponse r = new CustomizedResponse(); 
        boolean isProcessRunning = sendMessageToWorkflow(session, "pin-received-message");
        
        if(isProcessRunning){
			r.setMessage("Successfully authenticated with your PIN!");
			r.setStatusCode("SUCCESS");
		} else {
			r.setMessage("The PIN was invalid!");
			r.setStatusCode("INVALID");
		}
        
    	return r;
    }
    
    /**
     * Listener for logout the user
     * @param loginDTO
     */
    @RequestMapping(value = "/logout", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	@ResponseBody
    public CustomizedResponse logout(@RequestBody LoginDTO loginDTO){
        System.out.println("Received logout request (backend controller): " + loginDTO);
        
        UserSession session = new UserSession();
        session.setPassword(loginDTO.getPassword());
        session.setUsername(loginDTO.getUsername());
        session.setPin(loginDTO.getPin());
        session.setBusinessKey(loginDTO.getBusinessKey());
        session.setSession(loginDTO.getSession());
        
        CustomizedResponse r = new CustomizedResponse(); 
        boolean isProcessRunning = sendMessageToWorkflow(session, "logout-received-message");
        
        if(!isProcessRunning){
			r.setMessage("Logout Successful!");
			r.setStatusCode("SUCCESS");
		} else {
			r.setMessage("Problems occurred while logging you out!");
			r.setStatusCode("INVALID");
		}
        
    	return r;
    }
    
    /**
     * Trigger the BPMN workflow after the user provides user/password 
     * @param userSession
     */
    private CustomizedResponse triggerAuthWorkflowFirstStep(UserSession userSession) {
    	
    	//Prepare the JSON payload which is needed by the BPMN process
    	JSONObject var = prepareUserSessionVariables(userSession);
		
		//Start the process via triggering a REST POST request
		Client client = Client.create();

		WebResource webResource = client
		   .resource("http://localhost:8080/engine-rest/process-definition/key/twofactorauthprocess/start");

		ClientResponse response = webResource.type("application/json")
		   .post(ClientResponse.class, var.toString());

		if (response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
		}
		
		//Continue here if the start-request was successful
		logger.info("Process 'twofactorauthprocess' successfully triggered with status code " + response.getStatus());
		
		CustomizedResponse r = new CustomizedResponse();
		String respEntity = response.getEntity(String.class).toString();
		
		/*
		 * Check if the process has already been ended synchronously.
		 * This would mean that the process has ended and the authentication was not successful.
		 */
		try {
			
			JSONObject o = new JSONObject(respEntity);
			logger.info(o.toString());
			
			if(!o.getBoolean("ended")){
				r.setMessage("Successfully authenticated with username and password - The PIN is valid for ONE minute!");
				r.setStatusCode("SUCCESS");
			} else {
				r.setMessage("Userername and/or password are invalid!");
				r.setStatusCode("INVALID");
			}
			
			/*
			 * Setting the returned process id for further referencing the BPMN process instance.
			 * This will be returned to the client and set as variable in their session.
			 */
			r.setSession(o.getString("id"));
			
			logger.info("SESSION = " + r.getSession());
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return r; 
    }
    
    /**
     * Send message of kind 'pin-received-message' to the SPECIFIC process id started by the user.
     * This is assured by the sessionID == processID
     * @param userSession
     */
    private boolean sendMessageToWorkflow(UserSession userSession, String messageName) {
    	
    	//Prepare the JSON payload which is needed by the BPMN process
    	JSONObject var = prepareMessageVariables(userSession, messageName);
		
		//Start the process via triggering a REST POST request
		Client client = Client.create();

		WebResource webResource = client
		   .resource("http://localhost:8080/engine-rest/message");

		ClientResponse response = webResource.type("application/json")
		   .post(ClientResponse.class, var.toString());

		logger.info("Log return code: " + response.getStatus());
		
		/*
		 * Second request - check the process status
		 * This additional request is necessary, because the message-sending REST function
		 * does not provide return value variables!
		 */
		CustomizedResponse r = new CustomizedResponse();
		boolean isProcessRunning = checkProcessStatus(userSession.getSession());
		
		return isProcessRunning;
    }
    
    /**
     * Check if a process is running for a specific BPMN process.
     * @param processID
     */
    private boolean checkProcessStatus(String processID) {
    	
		//Start the process via triggering a REST POST request
		Client client = Client.create();

		WebResource webResource = client
		   .resource("http://localhost:8080/engine-rest/process-instance/" + processID);

		ClientResponse response = webResource.get(ClientResponse.class);
		logger.info("Status of 'process info': " + response.getStatus());
		
		String respEntity = response.getEntity(String.class).toString();
		logger.info("status response: " + respEntity);
		
		if (response.getStatus() != 200) {
			return false;
		}
		
		return true;
    }
    
    /**
     * Prepare a JSON object necessary for the start-process-instance REST call
     * @param userSession
     */
	private JSONObject prepareUserSessionVariables(UserSession userSession) {
		//Create JSON object with form data to fill the Camunda variables
    	JSONObject data = new JSONObject();
		JSONObject var = new JSONObject();
		
		JSONObject usernameData = new JSONObject();
		JSONObject passwordData = new JSONObject();
		JSONObject pinData = new JSONObject();
		
		try {
			usernameData.put("value", userSession.getUsername());
			usernameData.put("type", "String");
			
			passwordData.put("value", userSession.getPassword());
			passwordData.put("type", "String");
			
			pinData.put("value", userSession.getPin());
			pinData.put("type", "Integer");
						
			data.put("username", usernameData);
			data.put("password", passwordData);
			data.put("userPin", pinData);
			
			var.put("variables", data);
			var.put("businessKey", userSession.getBusinessKey());
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		logger.info("JSON user session variables: " + var);
		return var;
	}
	
	/**
     * Prepare a JSON object necessary for the send-message REST call
     * @param userSession
     */
	private JSONObject prepareMessageVariables(UserSession userSession, String messageName) {
		//Create JSON object with form data to fill the Camunda variables
		JSONObject var = new JSONObject();
		
		JSONObject pinData = new JSONObject();
		JSONObject pin = new JSONObject();
		
		try {
			pin.put("value", userSession.getPin());
			pin.put("type", "Integer");
			
			pinData.put("userPin", pin);
			
			var.put("messageName", messageName);
			var.put("businessKey", userSession.getBusinessKey());
			var.put("processVariables", pinData);
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		logger.info("JSON message request variables: " + var);
		return var;
	}
    
    
}
