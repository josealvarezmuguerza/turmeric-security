/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl.biz;

import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorConstants;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorDataFactory;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeRequestType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;

/**
 * Utilities specific to the Authorization Service implementation. 
 * 
 * @author mpoplacenel
 */
public class AuthorizationServiceImplUtils {


	/**
	 * The string to be used for a <code>null</code> value. 
	 */
	public static final String NULL_ERR_PARAM_VALUE = "[null]";

	private static final String UNAUTHORIZED_ERR_MSG = "Authorization failed.";

	/**
	 * The error parameter key to represent the list of policies. 
	 */
	public static final String POLICY_ERR_PARAM_NAME = "policy";

	private AuthorizationServiceImplUtils() {
		// no instance - static only
	}

	/**
	 * Creates an AUTHORIZED message.
	 * @param authzReq the request object to extract the information from. 
	 * @param authzPolicyList the list of authorization policies. 
	 * @return the newly-created error message. 
	 */
	public static ErrorMessage createUnauthorizedErrorMessage(
			AuthorizeRequestType authzReq, List<AuthorizationPolicy> authzPolicyList) {		
		Object[] errArgArr = AuthorizationServiceImplUtils.createErrorArguments(UNAUTHORIZED_ERR_MSG, authzReq, authzPolicyList);
    	CommonErrorData errorData = 
    		ErrorDataFactory.createErrorData(ErrorConstants.SVC_SECURITY_APP_AUTHZ_UNAUTHORIZED_USER, 
    				ErrorConstants.ERRORDOMAIN.toString(), errArgArr);
			
		ErrorMessage errMsg = new ErrorMessage();
		errMsg.getError().add(errorData);
		
		return errMsg;
	}

	/**
	 * Creates a list of error parameters. 
	 * @param message the error message to use as the first parameter. 
	 * @param authzReq the original authorization request.
	 * @param authzPolicyList the list of policies - <code>null</code> will skip the parameter altogether.
	 */
	public static Object[] createErrorArguments(
			String message,
			AuthorizeRequestType authzReq, List<AuthorizationPolicy> authzPolicyList) {

		List<String> errParamList = new ArrayList<String>();
		errParamList.add(message);
		errParamList.add(authzReq.getResourceType() + "." + authzReq.getResourceName() 
				+ "." + authzReq.getOperationName());
		
		for (SubjectType subject : authzReq.getSubject()) {
			String domain = subject.getDomain() == null ? NULL_ERR_PARAM_VALUE : subject.getDomain();
			final String subjectName = subject.getValue() == null ? NULL_ERR_PARAM_VALUE : subject.getValue();
			errParamList.add(domain + "." + subjectName);
		}
		
		if (authzPolicyList != null) {
			errParamList.add(POLICY_ERR_PARAM_NAME + "=" + createPolicyCSV(authzPolicyList));
		}
		
		return errParamList.toArray();
	}

	private static String createPolicyCSV(List<AuthorizationPolicy> authzPolicies) {
		StringBuilder policyCSV = new StringBuilder();
		int i = 0;
		if (authzPolicies != null) {
			for (AuthorizationPolicy authorizationPolicy : authzPolicies) {
				if (i++ > 0) {
					policyCSV.append(',');
				}
				final String policyName = authorizationPolicy.getPolicyName() == null 
						? NULL_ERR_PARAM_VALUE 
						: authorizationPolicy.getPolicyName();
				policyCSV.append(policyName);
			}
		}
		
		return policyCSV.toString();
	}

}