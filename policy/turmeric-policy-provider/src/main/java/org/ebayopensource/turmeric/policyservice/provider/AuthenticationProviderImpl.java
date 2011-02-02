/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.provider;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.Category;
import org.ebayopensource.turmeric.policyservice.provider.AuthenticationProvider;
import org.ebayopensource.turmeric.security.v1.services.AuthenticationPolicy;
import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyResponse;


/**
 * @author mgorovoy
 *
 */
public class AuthenticationProviderImpl implements AuthenticationProvider {

	private AuthenticationPolicyProvider policyProvider;
	
	public AuthenticationProviderImpl() {
		this.policyProvider = new AuthenticationFilePolicyProvider();
	}
	
	public AuthenticationProviderImpl(AuthenticationPolicyProvider policyProvider) {
		this.policyProvider = policyProvider;
	}
	
    /**
     * @see org.ebayopensource.turmeric.policyservice.provider.AuthenticationProvider#getAuthenticationPolicy(org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyRequest)
     */
    @Override
    public GetAuthenticationPolicyResponse getAuthenticationPolicy(
                    GetAuthenticationPolicyRequest request) throws PolicyProviderException {
    	if (request == null ||
    		request.getResourceName() == null ||
    		request.getResourceType() == null ||
    		request.getOperationName() == null ||
    		request.getResourceName().equals("") ||
    		request.getResourceType().equals("") ||
    		request.getOperationName().equals("")) {
			throw new org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException(Category.POLICY, 
					"AUTHN", "invalid request");
		}
		
		try {
			policyProvider.initialize();
		} catch (org.ebayopensource.turmeric.utils.config.exceptions.PolicyProviderException e) {
			throw new PolicyProviderException(Category.POLICY, 
					"AUTHN", "initialization failed", e);
		}

		GetAuthenticationPolicyResponse response = new GetAuthenticationPolicyResponse();
		
		try {
			AuthenticationProviderInfo authInfo = policyProvider.getAuthnPolicyByResource(
					request.getResourceName(), 
					request.getOperationName(), 
					request.getResourceType());
			
			if (authInfo != null) {
				mapAuthnPolicy(response, authInfo);
			}
			response.setAck(AckValue.SUCCESS);
		} catch (org.ebayopensource.turmeric.utils.config.exceptions.PolicyProviderException e) {
			throw new org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException(Category.POLICY, 
					"AUTHN", "unexpected error", e);
		}
		
		return response;
    }

	private void mapAuthnPolicy(GetAuthenticationPolicyResponse response, AuthenticationProviderInfo authInfo) {
		AuthenticationPolicy newPolicy = new AuthenticationPolicy();
		
		newPolicy.getAuthenticationScheme().addAll(authInfo.getAuthenticationMethods());
		newPolicy.setOperationName(authInfo.getOperationName());
		newPolicy.setResourceName(authInfo.getResourceName());
		newPolicy.setResourceType(authInfo.getResourceType());
		response.setPolicy(newPolicy);
	}
}
