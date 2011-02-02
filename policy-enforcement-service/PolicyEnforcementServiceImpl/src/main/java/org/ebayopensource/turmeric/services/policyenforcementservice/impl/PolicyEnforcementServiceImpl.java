/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *    
 * http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
//B''H
package org.ebayopensource.turmeric.services.policyenforcementservice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorData;
import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorConstants;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorUtils;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.security.v1.services.AuthenticateRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthenticateResponseType;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeResponseType;
import org.ebayopensource.turmeric.security.v1.services.CredentialType;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedResponse;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePair;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePairType;
import org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.security.v1.services.VerifyAccessRequest;
import org.ebayopensource.turmeric.security.v1.services.VerifyAccessResponse;
import org.ebayopensource.turmeric.services.ratelimiterservice.intf.gen.BaseRateLimiterServiceConsumer;

import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;
import org.ebayopensource.turmeric.services.policyenforcementservice.intf.PolicyEnforcementService;
import org.ebayopensource.turmeric.services.authenticationservice.intf.gen.BaseAuthenticationServiceConsumer;
import org.ebayopensource.turmeric.services.authorizationservice.intf.gen.BaseAuthorizationServiceConsumer;

/**
 * This Impl class manages calls to different Policy Service
 * 
 * @author prjande
 */
public class PolicyEnforcementServiceImpl implements PolicyEnforcementService {
	private static final String DEFAULT_OBJECT_RESOURCE_TYPE = "OBJECT";
	private static final String ALL_OBJECT = "All";
	private static final String SUBJECT_TYPE_ID_SUFFIX = "_ID" ;
	public static final String POLICY_TYPE_AUTHN = "AUTHN";
	public static final String POLICY_TYPE_AUTHZ = "AUTHZ";
	public static final String POLICY_TYPE_RL = "RL";
	public static final String POLICY_TYPE_WHITELIST = "WHITELIST";
	public static final String POLICY_TYPE_BLACKLIST = "BLACKLIST";
	
	private static final List<String> s_policyTypeSet = new ArrayList<String>(5){{
		add(POLICY_TYPE_AUTHN);
		add(POLICY_TYPE_RL);
		add(POLICY_TYPE_AUTHZ);
		add(POLICY_TYPE_BLACKLIST);
		add(POLICY_TYPE_WHITELIST);
	}};

	private static String delimiter = "[delimiter]";

	private static Logger s_logger = Logger
			.getInstance(PolicyEnforcementServiceImpl.class);

	public VerifyAccessResponse verifyAccess(VerifyAccessRequest request) {
		VerifyAccessResponse response = new VerifyAccessResponse();

		try {
			validateRequest(request);

			List<String> policyList = request.getPolicyType();

			// step 1. Call AuthN Cons7umer
			AuthenticateResponseType soaAuthnResponse = null;
			if (policyList.contains(POLICY_TYPE_AUTHN)) {
				AuthenticateRequestType authnRequest = new AuthenticateRequestType();
				mapAuthnRequest(authnRequest, request);
				soaAuthnResponse = invokeSOAAuthentiationService(authnRequest);
			}

			// step 2. Call RL Consumer
			IsRateLimitedResponse isRateLimitedResponse = null;
			if (policyList.contains(POLICY_TYPE_RL)) {
				IsRateLimitedRequest isRateLimitedRequest = new IsRateLimitedRequest();
				mapIsRateLimitedRequest(isRateLimitedRequest, request,
						soaAuthnResponse);
				isRateLimitedResponse = invokeSOARateLimiterService(isRateLimitedRequest);
			}

			boolean isRlAllowed = isRateLimitedResponse == null ? true
					: isRateLimitedResponse.getStatus() == null ? true
							: isRateLimitedResponse.getStatus() == RateLimiterStatus.SERVE_GIF ? false
									: isRateLimitedResponse.getStatus() == RateLimiterStatus.BLOCK ? false
											: true;

			// step 3. Call AuthZ Consumer
			AuthorizeResponseType soaAuthzResponse = null;
			if (policyList.contains(POLICY_TYPE_AUTHZ) && isRlAllowed) {
				// Check if the Object level authentication is required
				if (isObjectLevelAuthzRequired(request)) {
					soaAuthzResponse = performObjectObjectAuthz(request,
							soaAuthnResponse, isRateLimitedResponse);
				} else {
					AuthorizeRequestType authzRequest = new AuthorizeRequestType();
					mapAuthzRequest(authzRequest, request, soaAuthnResponse,
							isRateLimitedResponse);
					soaAuthzResponse = invokeSOAAuthorizationService(authzRequest);
				}
			}

			// step 4. Map All Responses
			mapResponse(response, soaAuthnResponse, isRateLimitedResponse,
					soaAuthzResponse, policyList);

		} catch (ServiceException e) {
			// All system errors come in the form of exception.
			// Here, map the exception to error response,
			// as we never throw exception from service impl
			mapErrorFromException(response, e);
			return response;
		}

		return response;
	}

	private AuthorizeResponseType performObjectObjectAuthz(
			VerifyAccessRequest request, AuthenticateResponseType soaAuthnResponse,
			IsRateLimitedResponse isRateLimitedResponse) throws ServiceException {
		// create authorization request
		AuthorizeRequestType authzRequest = new AuthorizeRequestType();
		
		// Resource name is SERVICE.<ServiceName>.<Operation>
		authzRequest.setResourceName((new StringBuffer().append("SERVICE.")
				.append(request.getOperationKey().getResourceName())
				.append(".").append(request.getOperationKey()
				.getOperationName())).toString());
		
		// Resource Type is provided by user e.g. "CLOUD", default is "OBJECT"
		String resourceType = request.getResourceType();
		resourceType = resourceType == null ? DEFAULT_OBJECT_RESOURCE_TYPE : resourceType;
		authzRequest.setResourceType(resourceType);
		
		// fill Subjects
		fillSubjects(authzRequest, request, soaAuthnResponse,
				isRateLimitedResponse);
		
		// Authz consumer
		BaseAuthorizationServiceConsumer consumer = new BaseAuthorizationServiceConsumer();
		
		AuthorizeResponseType authzResponse = null;
		AuthorizeResponseType lastAuthzErrResponse = null;
		String failedAccessObject = null;
		try {
			// for every access control object perform Authz
			for (String accessControlObject : request.getAccessControlObject()) {
				authzRequest.setOperationName(accessControlObject);
				authzResponse = consumer.authorize(authzRequest);
				if (authzResponse.getAck() == AckValue.FAILURE) {
					failedAccessObject = accessControlObject;
					lastAuthzErrResponse = authzResponse;
					break;
				}
			}

			// No authz was done or if authz failed check if authz is configured
			// for "All"
			if (authzResponse == null
					|| authzResponse.getAck() == AckValue.FAILURE) {
				authzRequest.setOperationName(ALL_OBJECT);
				authzResponse = consumer.authorize(authzRequest);
			}

			if (authzResponse.getAck() == AckValue.FAILURE) {
				// If an object was provided and report failure for the object else for ALL
				authzRequest
						.setOperationName(failedAccessObject == null ? ALL_OBJECT
								: failedAccessObject);
				// If object was provided then report failure message for the object
				throwServiceException(authzRequest,
						lastAuthzErrResponse == null ? authzResponse
								: lastAuthzErrResponse);

			}
		}catch (ServiceException e) {
			if (isLogEnabled(LogLevel.ERROR)) {
				logError("%s failed due to exception : %s ", "Authorization", e
						.getLocalizedMessage());
			}
			throw e;
		} catch (Exception e) {
			if (isLogEnabled(LogLevel.ERROR)) {
				logError("%s failed due to exception : %s ", "Authorization", e
						.getLocalizedMessage());
			}
			
			throw new ServiceException(ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_UNEXPECTED_AUTHZ_ERROR, 
					ErrorConstants.ERRORDOMAIN.toString(), 
					new Object[] {e.getMessage()}));
		}

		return authzResponse;
	}

	private void throwServiceException(
			AuthorizeRequestType authzRequest,
			AuthorizeResponseType authzResponse)
			throws ServiceException {
		// Error Mapping
		List<CommonErrorData> errorDataList = authzResponse.getErrorMessage().getError();
		String errorText = "Generic authorization error";
		// String errorId = "0";
		if (errorDataList.size() > 0) {
			ErrorData errorData = errorDataList.get(0);
			errorText = errorData.getMessage();
			// errorId = String.valueOf(errorData.getErrorId());
		}
		// secCtx.setAuthzFailure(errorText, errorId, null, null);

		throw new ServiceException(ErrorUtils.createErrorData(
				ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_AUTHZ_FAILED, 
				ErrorConstants.ERRORDOMAIN.toString(), 
				new Object[] {authzRequest.getResourceName(), authzRequest.getOperationName(), errorText}));
	}

	private boolean isObjectLevelAuthzRequired(VerifyAccessRequest request) {
		return request.getAccessControlObject() != null
				&& request.getAccessControlObject().size() > 0;
	}

	private void validateRequest(VerifyAccessRequest request)
			throws ServiceException {
		if (request == null) {
			throw new ServiceException(ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_INVALID_NULL_INPUT, 
					ErrorConstants.ERRORDOMAIN.toString()));
		}

		if (request.getOperationKey() == null
				|| request.getOperationKey().getOperationName() == null
				|| request.getOperationKey().getResourceName() == null
				|| request.getOperationKey().getResourceType() == null) {
			throw new ServiceException(ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_MISSING_RESOURCE_INPUT, 
					ErrorConstants.ERRORDOMAIN.toString()));
		}

		if (request.getPolicyType().size() <= 0) {
			throw new ServiceException(ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_MISSING_POLICY_TYPE_INPUT, 
					ErrorConstants.ERRORDOMAIN.toString()));
		}
		if (request.getPolicyType().contains(POLICY_TYPE_BLACKLIST)
				|| request.getPolicyType().contains(POLICY_TYPE_WHITELIST)) {
			throw new ServiceException(ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_UNSUPPORTED_POLICY_TYPE_INPUT, 
					ErrorConstants.ERRORDOMAIN.toString()));
		}

		for (String policyType : request.getPolicyType()) {
			if (!s_policyTypeSet.contains(policyType)) {
				throw new ServiceException(ErrorUtils.createErrorData(
						ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_INVALID_POLICY_TYPE_INPUT, 
						ErrorConstants.ERRORDOMAIN.toString(), new Object[]{policyType}));
			}
		}

	}

	// request mapping of AuthenticateRequestType from
	// PolicyEnforcementServiceRequest
	private void mapAuthnRequest(AuthenticateRequestType authnRequest,
			VerifyAccessRequest request) {

		authnRequest.setResourceName(request.getOperationKey()
				.getResourceName());
		authnRequest.setOperationName(request.getOperationKey()
				.getOperationName());
		authnRequest.setResourceType(request.getOperationKey()
				.getResourceType());

		// set credentials
		List<CredentialType> credentials = new ArrayList<CredentialType>();
		mapCredentials(credentials, request.getCredential());
		authnRequest.getCredential().addAll(credentials);
	}

	// map credentials
	private void mapCredentials(List<CredentialType> credentials,
			List<KeyValuePair> credentialList) {
		CredentialType credentialType;
		for (KeyValuePair credential : credentialList) {
			credentialType = new CredentialType();
			credentialType.setName(credential.getKey());
			credentialType.setValue(credential.getValue());
			credentials.add(credentialType);
		}
	}

	/**
	 * Invoke authentication service via SOA Service call
	 * 
	 * @param ctx
	 * @throws ServiceException
	 */
	private AuthenticateResponseType invokeSOAAuthentiationService(
			AuthenticateRequestType authnRequest)
			throws ServiceException {
		BaseAuthenticationServiceConsumer consumer = new BaseAuthenticationServiceConsumer();

		AuthenticateResponseType soaAuthnResponse = null;
		try {
			soaAuthnResponse = consumer.authenticate(authnRequest);

			if (soaAuthnResponse.getAck() == AckValue.FAILURE) {
				List<CommonErrorData> errorDataList = soaAuthnResponse.getErrorMessage().getError();
				String errorText = "Generic authentication error";
				// String errorId = "0";
				if (errorDataList.size() > 0) {
					ErrorData errorData = errorDataList.get(0);
					errorText = errorData.getMessage();
					// errorId = String.valueOf(errorData.getErrorId());
				}

				throw new ServiceException(ErrorUtils.createErrorData(
						ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_AUTHN_FAILED, 
						ErrorConstants.ERRORDOMAIN.toString(), 
						new Object[] {soaAuthnResponse.getAuthenticationMethod(), errorText}));
			}

		} catch (ServiceException e) {
			if (isLogEnabled(LogLevel.ERROR)) {
				logError("%s failed due to exception : %s ", "Authentication",
						e.getLocalizedMessage());
			}
			throw e;
		} catch (Exception e) {
			if (isLogEnabled(LogLevel.ERROR)) {
				logError("%s failed due to exception : %s ", "Authentication",
						e.getLocalizedMessage());
			}
			throw new ServiceException(ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_AUTHN_FAILED, 
					ErrorConstants.ERRORDOMAIN.toString(), 
					new Object[] {e.getMessage()}));
		}
		return soaAuthnResponse;
	}

	// request mapping of IsRateLimitedRequest
	private void mapIsRateLimitedRequest(
			IsRateLimitedRequest isRateLimitedRequest,
			VerifyAccessRequest request,
			AuthenticateResponseType soaAuthnResponse)
			throws ServiceException {

		isRateLimitedRequest.setResourceName(request.getOperationKey()
				.getResourceName());
		isRateLimitedRequest.setOperationName(request.getOperationKey()
				.getOperationName());
		isRateLimitedRequest.setResourceType(request.getOperationKey()
				.getResourceType());

		// add ClientIp/Proxy/Pool Subjects
		isRateLimitedRequest.getSubject().addAll(request.getSubject());

		List<SubjectType> rlSubjectList;

		// get authenticated subjects for RL request
		if (soaAuthnResponse != null) {
			rlSubjectList = mapPolicySubjectToRLSubject(soaAuthnResponse
					.getSubject());
			isRateLimitedRequest.getSubject().addAll(rlSubjectList);

			// get subject groups
			isRateLimitedRequest.getResolvedSubjectGroup().addAll(
					soaAuthnResponse.getSubjectgroup());
		} else {
			rlSubjectList = mapPolicySubjectToRLSubject(request.getSubject());
			isRateLimitedRequest.getSubject().addAll(rlSubjectList);
		}

		// :TODO set the Challenge info
		/*
		 * if(request.getChallengeAnswer() != null) {
		 * isRateLimitedRequest.setChallengeAnswer(request.getChallengeAnswer()); }
		 */

		SubjectGroupType sg = new SubjectGroupType();
		sg.setName("AllIP");
		sg.setDomain("IP");
		sg.setCalculator("AllIP");
		isRateLimitedRequest.getResolvedSubjectGroup().add(sg);
	}

	private List<SubjectType> mapPolicySubjectToRLSubject(
			List<SubjectType> subjectList)
			throws ServiceException {

		List<SubjectType> rlSubjectList = new ArrayList<SubjectType>();

		Map<String, String> authSubjectMap = new HashMap<String, String>();

		if (subjectList != null) {
			Iterator<SubjectType> i = subjectList.iterator();
			while (i.hasNext()) {
				SubjectType sub = i.next();
				if (sub.getDomain() == null || sub.getValue() == null) {
					throw new ServiceException(ErrorUtils.createErrorData(
							ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_INVALID_SUBJECT_DOMAIN_INPUT, 
							ErrorConstants.ERRORDOMAIN.toString()));
				}
				authSubjectMap.put(sub.getDomain(), sub.getValue());
			}
		}
		SubjectType subject;
		
		/*
		 * Iterate through the subject domains, and find the matching subject name/id domain pairs
		 * If found, then only one subject is output, with subject name domain and the value combined from above pairs
		 * as here : subject_id_value[DELIM]subject_name_value
		 * 
		 */
		for (String subjectDomain : authSubjectMap.keySet()) {
			if (subjectDomain == null) continue; // do not process null keys
			String subjectValue = authSubjectMap.get(subjectDomain);
			if (subjectDomain.endsWith(SUBJECT_TYPE_ID_SUFFIX)) {
				// the current key is a subject id (domain) for e.g, APP_ID
				String subjectNameDomain = subjectDomain.substring(0, subjectDomain.length() - SUBJECT_TYPE_ID_SUFFIX.length());
				String subjectNameValue = authSubjectMap.get(subjectNameDomain);	
				subjectDomain = subjectNameDomain;
				subjectValue = subjectValue + delimiter + subjectNameValue;
				
			} else {
				// the current key is a subject name (domain) for e.g, APP
				// check for matching id domain exists, if yes, then skip as it is handled in the 'if'
				if (authSubjectMap.containsKey(subjectDomain + SUBJECT_TYPE_ID_SUFFIX))
					continue;
			}
			
			subject = new SubjectType();
			subject.setDomain(subjectDomain);
			subject.setValue(subjectValue);
			rlSubjectList.add(subject);
		}
		return rlSubjectList;
	}

	/**
	 * Invoke RateLimiter service via SOA Service call
	 * 
	 * @param ctx
	 * @throws ServiceException
	 */
	private IsRateLimitedResponse invokeSOARateLimiterService(
			IsRateLimitedRequest isRateLimitedRequest)
			throws ServiceException {

		// The Policy Enforcement service currently do not support the WL and BL
		/*
		 * if (secCtx.getWhitelistStatus().isSuccess()) return;
		 */

		BaseRateLimiterServiceConsumer consumer = new BaseRateLimiterServiceConsumer();

		IsRateLimitedResponse isRateLimitedResponse = null;
		try {
			isRateLimitedResponse = consumer
					.isRateLimited(isRateLimitedRequest);
		} catch (Exception e) {
			if (isLogEnabled(LogLevel.ERROR)) {
				logError("RateLimiting failed due to exception : %s ", e
						.getLocalizedMessage());
			}
			throw new ServiceException(ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_RL_FAILED, 
					ErrorConstants.ERRORDOMAIN.toString(), new Object[]{e.getMessage()}));
		}

		if (isRateLimitedResponse.getAck() == AckValue.FAILURE) {
			List<CommonErrorData> errorDataList = isRateLimitedResponse.getErrorMessage().getError();
			String errorText = "Generic RateLimiter error";
			if (errorDataList.size() > 0) {
				ErrorData errorData = errorDataList.get(0);
				errorText = errorData.getMessage();
			}

			throw new ServiceException(ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_UNEXPECTED_RL_ERROR, 
					ErrorConstants.ERRORDOMAIN.toString(), 
					new Object[]{isRateLimitedRequest.getResourceName(), isRateLimitedRequest.getOperationName(), errorText}));
		}
		return isRateLimitedResponse;
	}

	// request mapping of AuthorizeRequestType
	private void mapAuthzRequest(AuthorizeRequestType authzRequest,
			VerifyAccessRequest request,
			AuthenticateResponseType soaAuthnResponse,
			IsRateLimitedResponse isRateLimitedResponse) {

		authzRequest.setResourceName(request.getOperationKey()
				.getResourceName());
		authzRequest.setOperationName(request.getOperationKey()
				.getOperationName());
		authzRequest.setResourceType(request.getOperationKey()
				.getResourceType());

		fillSubjects(authzRequest, request, soaAuthnResponse,
				isRateLimitedResponse);
	}

	private void fillSubjects(AuthorizeRequestType authzRequest,
			VerifyAccessRequest request,
			AuthenticateResponseType soaAuthnResponse,
			IsRateLimitedResponse isRateLimitedResponse) {
		// add ClientIp/Proxy/Pool Subjects
		authzRequest.getSubject().addAll(request.getSubject());

		// get authenticated subjects & resolved subject groups from context
		if (soaAuthnResponse != null) {
			authzRequest.getSubject().addAll(soaAuthnResponse.getSubject());
			authzRequest.getResolvedSubjectGroup().addAll(
					soaAuthnResponse.getSubjectgroup());
		}

		// get resolved subject groups from rate limiter response
		if (isRateLimitedResponse != null) {
			authzRequest.getResolvedSubjectGroup().addAll(
					isRateLimitedResponse.getResolvedSubjectGroup());
		}
	}

	/**
	 * Invoke authorization service via SOA Service call
	 * 
	 * @param ctx
	 * @throws ServiceException
	 */
	private AuthorizeResponseType invokeSOAAuthorizationService(
			AuthorizeRequestType authzRequest)
			throws ServiceException {
		BaseAuthorizationServiceConsumer consumer = new BaseAuthorizationServiceConsumer();

		AuthorizeResponseType soaAuthzResponse = null;
		try {
			soaAuthzResponse = consumer.authorize(authzRequest);

			if (soaAuthzResponse.getAck() == AckValue.FAILURE) {
				throwServiceException(authzRequest, soaAuthzResponse);

			}
		} catch (ServiceException e) {
			if (isLogEnabled(LogLevel.ERROR)) {
				logError("%s failed due to exception : %s ", "Authorization", e
						.getLocalizedMessage());
			}
			throw e;
		} catch (Exception e) {
			if (isLogEnabled(LogLevel.ERROR)) {
				logError("%s failed due to exception : %s ", "Authorization", e
						.getLocalizedMessage());
			}
			throw new ServiceException(ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_UNEXPECTED_AUTHZ_ERROR, 
					ErrorConstants.ERRORDOMAIN.toString(), 
					new Object[] {e.getMessage()}));
		}
		return soaAuthzResponse;
	}

	// response mapping to Policy Enforcement Service Response
	private void mapResponse(VerifyAccessResponse response,
			AuthenticateResponseType soaAuthnResponse,
			IsRateLimitedResponse isRateLimitedResponse,
			AuthorizeResponseType soaAuthzResponse, List<String> policyList) {

		AckValue ack = null;
		if (policyList.contains(POLICY_TYPE_AUTHN)) {
			response.setAck(AckValue.fromValue(soaAuthnResponse.getAck()
					.value()));
			response.getAuthenticatedSubject().addAll(soaAuthnResponse.getSubject());
			response.getResolvedSubjectGroup().addAll(soaAuthnResponse.getSubjectgroup());
			if (soaAuthnResponse.getExtendedInfo() != null) {
				List<KeyValuePair> kvPairList = new ArrayList<KeyValuePair>();
				for (KeyValuePairType kvPairType : soaAuthnResponse
						.getExtendedInfo().getKvpair()) {
					KeyValuePair kvp = new KeyValuePair();
					kvp.setKey(kvPairType.getKey());
					kvp.setValue(kvPairType.getValue());
					kvPairList.add(kvp);
				}
				response.getExtendedInfo().addAll(kvPairList);
			}
			if (soaAuthnResponse.getErrorMessage() != null)
				response.setErrorMessage(soaAuthnResponse.getErrorMessage());
		}
		if (policyList.contains(POLICY_TYPE_RL) && isRateLimitedResponse != null) {
			ack = (isRateLimitedResponse.getAck() == null) ? response.getAck()
					: isRateLimitedResponse.getAck() == AckValue.FAILURE ? AckValue.FAILURE
							: response.getAck() == null ? AckValue.SUCCESS : response.getAck();

			response.setAck(ack);
			response.setRateLimiterStatus(isRateLimitedResponse.getStatus());
			if (soaAuthnResponse != null
					&& soaAuthnResponse.getErrorMessage() != null)
				response.setErrorMessage(isRateLimitedResponse.getErrorMessage());
			// response.setChallenge(isRateLimitedResponse.getChallenge());
		}
		if (policyList.contains(POLICY_TYPE_AUTHZ) && soaAuthzResponse != null) {
			ack = (response.getAck() == null) ? soaAuthzResponse.getAck()
					: response.getAck() == AckValue.SUCCESS ? soaAuthzResponse.getAck()
							: AckValue.FAILURE;

			response.setAck(ack);
			response.getResolvedSubjectGroup().addAll(soaAuthzResponse.getResolvedSubjectGroup());
			if (soaAuthnResponse != null
					&& soaAuthnResponse.getErrorMessage() != null)
				response.setErrorMessage(soaAuthzResponse.getErrorMessage());
		}
	}


	// error mapping from SecurityFacadeException to
	// PolicyEnforcementServiceResponse
	private void mapErrorFromException(VerifyAccessResponse respType,
			ServiceException ex) {
		respType.setAck(AckValue.FAILURE);
		respType.setErrorMessage(ex.getErrorMessage());		
	}

	static Logger getLogger() {
		return s_logger;
	}

	public static void debug(String format, Object... args) {
		log(LogLevel.DEBUG, format, args);
	}

	public static void logError(String format, Object... args) {
		log(LogLevel.ERROR, format, args);
	}

	public static void log(LogLevel level, String format, Object... args) {
		String message = String.format(format, args);
		s_logger.log(level, message);
	}

	public static boolean isDebugEnabled() {
		return isLogEnabled(LogLevel.DEBUG);
	}

	public static boolean isLogEnabled(LogLevel level) {
		return s_logger.isLogEnabled(level);
	}

}
