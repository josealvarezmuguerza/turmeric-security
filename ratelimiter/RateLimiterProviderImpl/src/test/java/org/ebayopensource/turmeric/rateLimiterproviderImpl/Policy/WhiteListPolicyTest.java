/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesResponse;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedResponse;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicySet;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Resources;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.Subjects;
import org.ebayopensource.turmeric.security.v1.services.Target;
import org.ebayopensource.turmeric.services.policyservice.intf.gen.BasePolicyServiceConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class WhiteListPolicy.
 */
public class WhiteListPolicyTest {
	BasePolicyServiceConsumer policyConsumer;
	FindPoliciesResponse policyResponse;
	FindPoliciesRequest policyRequest;
	WhiteListPolicy wlPolicy;
	IsRateLimitedRequest isRateLimitedRequest;
	IsRateLimitedResponse isRateLimitedResponse;

	@Before
	public void prepare() throws ServiceException {
		
		policyConsumer = mock(BasePolicyServiceConsumer.class);
		policyRequest = mock(FindPoliciesRequest.class);
		policyResponse = new FindPoliciesResponse();

		policyResponse.setPolicySet(retrieveMockedPolicies());
		when(policyConsumer.findPolicies(policyRequest)).thenReturn(policyResponse);

		
		isRateLimitedRequest = new IsRateLimitedRequest();
		isRateLimitedResponse =  new IsRateLimitedResponse();
		
		wlPolicy = new WhiteListPolicy(isRateLimitedRequest);
	}
	
	@After
	public void tearDown() {
		policyConsumer = null;
		policyRequest = null;
		policyResponse = null;
		isRateLimitedRequest = null;
		wlPolicy = null;
	}

	private PolicySet retrieveMockedPolicies() {
		PolicySet policies= new PolicySet();
		Policy policy_1 = new Policy();
		policy_1.setActive(true);
		policy_1.setCreatedBy("jose_test");
		policy_1.setDescription("WL test policy");
		policy_1.setLastModified(null);
		policy_1.setLastModifiedBy(null);
		policy_1.setPolicyId(1L);
		policy_1.setPolicyName("WL_Policy_1");
		policy_1.setPolicyType("WL");
		
		Target target = new Target();
		
		//Subjects
		Subject subject_1 = new Subject();
		subject_1.setSubjectType("USER");
		subject_1.setSubjectName("user_" + 1);
		
		Subjects subjects = new Subjects();
		subjects.getSubject().add(subject_1);
		target.setSubjects(subjects);

		//Resources
		Resource resource_1= new Resource();
		resource_1.setResourceId(2L);
		resource_1.setResourceName("resource_1");
		resource_1.setResourceType("SERVICE");
		
		Resources resources = new Resources();
		resources.getResource().add(resource_1);
		target.setResources(resources);
		
		policy_1.setTarget(target);
		policies.getPolicy().add(policy_1);
		
		return policies;
	}
	
	 @Test
	public void testGetPolicyType() {
		assertEquals("PolicyType must be WHITELIST","WHITELIST", wlPolicy.getPolicyType());
	}
	 
	 @Test
	 public void testIsExcluded() {
		 assertFalse(" WL policy must not be excluded", wlPolicy.isExcluded());
	 }
	 
	 @Test
	 public void testIsIncluded() {
		 assertFalse(" WL policy must not be included", wlPolicy.isIncluded());
	 }
	 

	 
}