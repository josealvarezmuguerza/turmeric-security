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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AuthenticationFilePolicyProviderTest {

	private AuthenticationPolicyProvider policyProvider;
	
	@Test
	public void testGetAuthnPolicyByResource_DefaultConfigs() throws Exception {
		policyProvider = new AuthenticationFilePolicyProvider();
		policyProvider.initialize();
		testGetAuthnPolicyByResourceCommonScenarios();
	}
	
	@Test
	public void testGetAuthnPolicyByResource_CustomConfigs() throws Exception {
		policyProvider = new AuthenticationFilePolicyProvider("AnotherAuthenticationPolicy.xml", "AuthenticationPolicy.xsd", "authentication-policy");
		policyProvider.initialize();
		testGetAuthnPolicyByResourceCommonScenarios();
	}
	
	private void testGetAuthnPolicyByResourceCommonScenarios() throws Exception {
		AuthenticationProviderInfo authInfo = null;
		
		// invalid param
		authInfo = policyProvider.getAuthnPolicyByResource("", "r1-o1", "t1");
//		System.out.println(authInfo.getAuthenticationMethods());
		assertTrue(authInfo.getAuthenticationMethods().size() == 0);
		
		// invalid param
		authInfo = policyProvider.getAuthnPolicyByResource("r1", "", "t1");
//		System.out.println(authInfo.getAuthenticationMethods());
		assertTrue(authInfo.getAuthenticationMethods().size() == 0);
		
		// invalid param
		authInfo = policyProvider.getAuthnPolicyByResource("r1", "r1-o1", "");
//		System.out.println(authInfo.getAuthenticationMethods());
		assertTrue(authInfo.getAuthenticationMethods().size() == 0);
		
		// resource (name + type) does not exist does not exist
		authInfo = policyProvider.getAuthnPolicyByResource("r1", "r1-o1", "t2");
//		System.out.println(authInfo.getAuthenticationMethods());
		assertTrue(authInfo.getAuthenticationMethods().size() == 0);
		
		// operation exists
		authInfo = policyProvider.getAuthnPolicyByResource("r1", "r1-o1", "t1");
//		System.out.println(authInfo.getAuthenticationMethods());
		assertTrue(authInfo.getAuthenticationMethods().size() > 0);
		
		// operation exists
		authInfo = policyProvider.getAuthnPolicyByResource("r1", "r1-o2", "t1");
//		System.out.println(authInfo.getAuthenticationMethods());
		assertTrue(authInfo.getAuthenticationMethods().size() > 0);
		
		// operation does not exist but use default method instead
		authInfo = policyProvider.getAuthnPolicyByResource("r1", "r1-o3", "t1");
//		System.out.println(authInfo.getAuthenticationMethods());
		assertTrue(authInfo.getAuthenticationMethods().size() > 0);
	}
}
