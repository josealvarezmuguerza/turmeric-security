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

import static org.junit.Assert.*;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.policyservice.provider.AuthenticationProvider;
import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyResponse;
import org.junit.BeforeClass;
import org.junit.Test;


public class AuthenticationProviderImplTest {

	private static AuthenticationProvider authenticationProvider;
	
	@BeforeClass
	public static void initClass() {
		authenticationProvider = new AuthenticationProviderImpl(new AuthenticationFilePolicyProvider());
	}
	
	@Test(expected=org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.class)
	public void testGetAuthenticationPolicy_NullRequest() throws Exception {
		authenticationProvider.getAuthenticationPolicy(null);
	}
	
	@Test(expected=org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.class)
	public void testGetAuthenticationPolicy_NullResourceName() throws Exception {
		authenticationProvider.getAuthenticationPolicy(createRequest(null, "r1-o2", "t1"));
	}
	
	@Test(expected=org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.class)
	public void testGetAuthenticationPolicy_BlankResourceName() throws Exception {
		authenticationProvider.getAuthenticationPolicy(createRequest("", "r1-o2", "t1"));
	}
	
	@Test(expected=org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.class)
	public void testGetAuthenticationPolicy_NullOperationName() throws Exception {
		authenticationProvider.getAuthenticationPolicy(createRequest("r1", null, "t1"));
	}
	
	@Test(expected=org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.class)
	public void testGetAuthenticationPolicy_BlankOperationName() throws Exception {
		authenticationProvider.getAuthenticationPolicy(createRequest("r1", "", "t1"));
	}
	
	@Test(expected=org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.class)
	public void testGetAuthenticationPolicy_NullResourceType() throws Exception {
		authenticationProvider.getAuthenticationPolicy(createRequest("r1", "r1-o2", null));
	}
	
	@Test(expected=org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.class)
	public void testGetAuthenticationPolicy_BlankResourceType() throws Exception {
		authenticationProvider.getAuthenticationPolicy(createRequest("r1", "r1-o2", ""));
	}
	
	// operation found - expected to return associated authentication methods
	@Test
	public void testGetAuthenticationPolicy_OperationFound() throws Exception {
		GetAuthenticationPolicyRequest req = createRequest("r1", "r1-o1", "t1");
		GetAuthenticationPolicyResponse resp = authenticationProvider.getAuthenticationPolicy(req);
		assertRespOnSuccessOnly(req, resp);
		assertNotNull(resp.getPolicy().getAuthenticationScheme());
		assertTrue(resp.getPolicy().getAuthenticationScheme().size() > 1);
	}
	
	// operation not found - expected to return the default authentication method
	@Test
	public void testGetAuthenticationPolicy_OperationNotFound() throws Exception {
		GetAuthenticationPolicyRequest req = createRequest("r1", "r1-o3", "t1");
		GetAuthenticationPolicyResponse resp = authenticationProvider.getAuthenticationPolicy(req);
		assertRespOnSuccessOnly(req, resp);
		assertNotNull(resp.getPolicy().getAuthenticationScheme());
		assertTrue(resp.getPolicy().getAuthenticationScheme().size() == 1);
	}
	
	// resource not found - expected to return empty collection
	@Test
	public void testGetAuthenticationPolicy_ResourceNotFound() throws Exception {
		GetAuthenticationPolicyRequest req = createRequest("r1", "r1-o1", "t2");
		GetAuthenticationPolicyResponse resp = authenticationProvider.getAuthenticationPolicy(req);
		assertRespOnSuccessOnly(req, resp);
		assertNotNull(resp.getPolicy().getAuthenticationScheme());
		assertTrue(resp.getPolicy().getAuthenticationScheme().size() == 0);
	}
	
	private void assertRespOnSuccessOnly(GetAuthenticationPolicyRequest req, GetAuthenticationPolicyResponse resp) {
		assertNotNull(resp);
		assertNotNull(req);
		assertNotNull(resp.getPolicy());
		assertTrue(resp.getPolicy().getResourceName() == req.getResourceName());
		assertTrue(resp.getPolicy().getOperationName() == req.getOperationName());
		assertTrue(resp.getPolicy().getResourceType() == req.getResourceType());
		assertTrue(resp.getAck() == AckValue.SUCCESS);
	}
	
	private GetAuthenticationPolicyRequest createRequest(String resourceName, String operationName, String resourceType) {
		GetAuthenticationPolicyRequest req = new GetAuthenticationPolicyRequest();
		req.setResourceName(resourceName);
		req.setOperationName(operationName);
		req.setResourceType(resourceType);
		return req;
	}
}
