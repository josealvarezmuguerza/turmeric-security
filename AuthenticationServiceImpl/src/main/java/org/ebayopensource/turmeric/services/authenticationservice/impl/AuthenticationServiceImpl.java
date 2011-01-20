/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authenticationservice.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.authentication.provider.AuthenticationException;
import org.ebayopensource.turmeric.authentication.provider.AuthenticationRequest;
import org.ebayopensource.turmeric.authentication.provider.AuthenticationResponse;
import org.ebayopensource.turmeric.authentication.provider.Authenticator;
import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorConstants;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorUtils;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.security.v1.services.AuthenticateRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthenticateResponseType;
import org.ebayopensource.turmeric.security.v1.services.AuthenticationExtendedResponseType;
import org.ebayopensource.turmeric.security.v1.services.CredentialType;
import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyResponse;
import org.ebayopensource.turmeric.services.authenticationservice.authenticator.AuthenticatorStore;

import org.ebayopensource.turmeric.services.authenticationservice.intf.AuthenticationService;
import org.ebayopensource.turmeric.services.policyservice.intf.gen.BasePolicyServiceConsumer;

public class AuthenticationServiceImpl implements AuthenticationService {

	private static  Logger s_logger = LogManager.getInstance(AuthenticationServiceImpl.class);
	private static AuthenticatorStore s_AuthnStore = AuthenticatorStore.getInstance();
	
    public AuthenticateResponseType authenticate(AuthenticateRequestType request) {

    	AuthenticateResponseType response = new AuthenticateResponseType();
    	
    	// validate  request
    	if(request == null || request.getOperationName() == null || 
    			request.getResourceName() == null || request.getResourceType() == null || 
    			request.getCredential().isEmpty()) {
    		setErrorInResponse(response, ErrorUtils.createErrorData(ErrorConstants.SVC_SECURITY_AUTHN_INVALID_REQUEST, 
					ErrorConstants.ERRORDOMAIN.toString(), 
					new Object[] {"resource/resourcetype/operationname and credentials should be present" }));
    		return response;
    	}  	
    	
    	boolean hasRequiredCredentials = false;
    	try {
			// step 1: get authentication policy for the res/op
			GetAuthenticationPolicyResponse authnPolicyResponse = getAuthnPolicy(request);
				
			// step 2: get the authn methods associated to the res/op
			List<String> authnMethods = authnPolicyResponse.getPolicy().getAuthenticationScheme();
			
			// create and map request
			AuthenticationRequest authenticateRequest = new AuthenticationRequest();
			mapRequest(authenticateRequest, request);
			
			
			for (String authnMethod : authnMethods) {
				// step 3: lookup the registered authenticator for this authn method
				Authenticator authenticator = s_AuthnStore.getAuthenticator(authnMethod);
				if (authenticator == null) {
					throw new ServiceException(ErrorUtils.createErrorData(
							ErrorConstants.SVC_SECURITY_AUTHN_NO_AUTHENTICATOR_CONFIGURED,
							ErrorConstants.ERRORDOMAIN.toString(), new Object[] { authnMethod}));
				}
				
				// step 4: check required credentials
			    List<String> requiredCredentials = authenticator.getRequiredCredentials();
			    
			    if (hasCredentials(requiredCredentials, request)) {
			    	hasRequiredCredentials = true;
			    	// step 5: invoke authenticate() on the authenticator
			    	response.setAuthenticationMethod(authenticator.getAuthenticationMethod()); // set the auth method here so when the below
			    	//throws an exception, we have this info
			    	AuthenticationResponse authenticateResponse = authenticator.authenticate(authenticateRequest);
			    	// post checking. Make sure proper errorId is sent back in the case of error
			        if (authenticateResponse != null) {
			            // set authn method if it has not beens et
			            mapResponse(response, authenticateResponse);
			            break;
			        } else {
			        	// Internal error()
			        	s_logger.log(Level.SEVERE, "unexpected null response from authenticator: " + authnMethod);
			        	
			        	setErrorInResponse(response, 
			        			ErrorUtils.createErrorData(ErrorConstants.SVC_SECURITY_UNEXPECTED_AUTHN_ERROR, 
			        					ErrorConstants.ERRORDOMAIN.toString(), 
			        					new Object[] {"unexpected null response from authenticator" }));
			        }
			    }
			    
			}
		} catch (ServiceException e) {
			if (e.getErrorMessage() != null && e.getErrorMessage().getError().size() > 0) {
				setErrorInResponse(response, e.getErrorMessage().getError().get(0));
			} else {
				setErrorInResponse(response, 
	        			ErrorUtils.createErrorData(ErrorConstants.SVC_SECURITY_AUTHN_INTERNAL_ERROR, 
	        					ErrorConstants.ERRORDOMAIN.toString()));
			}
		} catch (AuthenticationException e) {
			setErrorInResponse(response, 
        			ErrorUtils.createErrorData(ErrorConstants.SVC_SECURITY_UNEXPECTED_AUTHN_ERROR, 
        					ErrorConstants.ERRORDOMAIN.toString(),
        					new Object[] {"Authenticator exception: " + e.getMessage()}));
		}

    	if (!hasRequiredCredentials)
    		setErrorInResponse(response, ErrorUtils.createErrorData(ErrorConstants.SVC_SECURITY_AUTHN_MISSING_REQ_CREDENTIALS, 
        					ErrorConstants.ERRORDOMAIN.toString()));
    	return response;
    }
    
    private GetAuthenticationPolicyResponse getAuthnPolicy(AuthenticateRequestType authnRequest) throws ServiceException  {
    	BasePolicyServiceConsumer consumer = new BasePolicyServiceConsumer();
		GetAuthenticationPolicyRequest request = new GetAuthenticationPolicyRequest();
		request.setResourceName(authnRequest.getResourceName());
		request.setOperationName(authnRequest.getOperationName());
		request.setResourceType(authnRequest.getResourceType());
		GetAuthenticationPolicyResponse response = null;
		try {
			response = consumer.getAuthenticationPolicy(request);
		} catch (Exception e) { 
			s_logger.log(Level.SEVERE, "PolicyService exception",  e);
			throw new ServiceException (ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_AUTHN_INTERNAL_ERROR, ErrorConstants.ERRORDOMAIN.toString()),
					e);
		}
		if (response == null) {
			s_logger.log(Level.SEVERE, "PolicyService response is null");
			throw new ServiceException (ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_AUTHN_INTERNAL_ERROR, ErrorConstants.ERRORDOMAIN.toString()));
		}
		
		// validate the response
    	if (response.getAck() != AckValue.SUCCESS) {
    		s_logger.log(Level.SEVERE, "PolicyService getAuthenticationPolicy call returned failure");
    		throw new ServiceException (ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_AUTHN_INTERNAL_ERROR, ErrorConstants.ERRORDOMAIN.toString()));
    	}
    	
    	if (response.getPolicy() == null ) {
    		s_logger.log(Level.SEVERE, "PolicyService authnPolicy  is null and does not have required info");
    		throw new ServiceException (ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_AUTHN_POLICY_NOT_FOUND, ErrorConstants.ERRORDOMAIN.toString(), 
					new Object[]{request.getResourceName(), request.getOperationName()}));
    	}
    	
    	if (response.getPolicy().getAuthenticationScheme().isEmpty()) {
    		s_logger.log(Level.SEVERE, "PolicyService response is empty and does not have required info");
    		throw new ServiceException (ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_AUTHN_POLICY_MISSING_AUTHN_METHOD, ErrorConstants.ERRORDOMAIN.toString(),
					new Object[]{request.getResourceName(), request.getOperationName()}));
    	}
    		
		return response;
    }
    
    // check if the crential map contains all the requiredCredentials specified on the requiredCredentials list
    private boolean hasCredentials(List<String> requiredCredentials, AuthenticateRequestType authnRequest) {
        // if there's no required credentail, let it go thru
        if (requiredCredentials == null ||  requiredCredentials.isEmpty())
            return true;

        // if no credential was specified and the required credential is not empty, return error
        if (authnRequest.getCredential().isEmpty())
            return false;

        // for all requiredcredentials, we should match all authnRequest.getCredential()
        // if theres any requiredcredentials that doesnt match, this fails
        
        for (String requiredCred : requiredCredentials) {
        	boolean matchFound = false;
			for (CredentialType requestCred : authnRequest.getCredential()) {
				if (requiredCred.equalsIgnoreCase(requestCred.getName())){
					matchFound = true;
					break;
				}
			}
			if(!matchFound){
				return false;
			}
		}
        // all were mathed, else would not come here
        return true;
    }

    // request mapping from AuthenticateRequestType to AuthenticationRequest
    private void mapRequest(AuthenticationRequest req, AuthenticateRequestType reqType) {
    	req.setResourceName(reqType.getResourceName());
    	req.setOperationName(reqType.getOperationName());
    	req.setResourceType(reqType.getResourceType());
    	req.setCredentials(reqType.getCredential());
    }
    
    // response mapping from AuthenticationResponse to AuthenticateResponseType
    private void mapResponse(AuthenticateResponseType respType, AuthenticationResponse resp) {
    	respType.setAuthenticationMethod(resp.getAuthenticationMethod());
    	if (resp.getAuthnSubjects() != null)
    		respType.getSubject().addAll(resp.getAuthnSubjects());
    	if (resp.getSubjectGroups() != null)
    		respType.getSubjectgroup().addAll(resp.getSubjectGroups());
    	if (resp.getExtendedInfo() != null) {
    		AuthenticationExtendedResponseType extendedResponseType = new AuthenticationExtendedResponseType();
    		extendedResponseType.getKvpair().addAll(resp.getExtendedInfo());
    		respType.setExtendedInfo(extendedResponseType);
    	}
        respType.setAck(AckValue.SUCCESS);
    }
       
    private void setErrorInResponse(AuthenticateResponseType respType, CommonErrorData errorData){
    	respType.setAck(AckValue.FAILURE);
    	ErrorMessage errMsg = new ErrorMessage();    	
    	respType.setErrorMessage(errMsg);
    	errMsg.getError().add(errorData);
    }
    
}
