/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.rateLimiterproviderImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesResponse;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedResponse;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.PolicyOutputSelector;
import org.ebayopensource.turmeric.security.v1.services.Query;
import org.ebayopensource.turmeric.security.v1.services.QueryCondition;
import org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.services.policyservice.intf.gen.BasePolicyServiceConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

/**
 * The Class RateLimiterProviderImplTest.
 */
public class RateLimiterProviderImplTest extends
		RateLimiterProviderAbstractTest {

	/** The provider. */
	RateLimiterProviderImpl provider = null;
	
	/** The rate limit request. */
	IsRateLimitedRequest rateLimitRequest = null;
	// mock
	/** The base policy service consumer mock. */
	BasePolicyServiceConsumer basePolicyServiceConsumerMock;

	/**
	 * The Class IsPolicyType.
	 */
	class IsPolicyType extends ArgumentMatcher<FindPoliciesRequest> {
		
		/** The type. */
		String type;

		/**
		 * Instantiates a new checks if is policy type.
		 *
		 * @param type the type
		 */
		public IsPolicyType(String type) {
			this.type = type;
			// set as defualt
			if (type == null || type.trim().length() == 0) {
				type = "BLACKLIST";
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean matches(Object list) {
			if (((FindPoliciesRequest) list) != null) {
				List<PolicyKey> policyKeys = ((FindPoliciesRequest) list)
						.getPolicyKey();
				for (PolicyKey key : policyKeys) {
					if (key != null) {
						if (type.equalsIgnoreCase(key.getPolicyType())) {
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	/**
	 * Sets the up.
	 */
	@Before
	public void setUp() {
		basePolicyServiceConsumerMock = mock(BasePolicyServiceConsumer.class);
		rateLimitRequest = new IsRateLimitedRequest();
		provider = new RateLimiterProviderImpl();
		// set mock
		provider.setConsumer(basePolicyServiceConsumerMock);
		// mock for BL
		when(
				basePolicyServiceConsumerMock
						.findPolicies(argThat(new IsPolicyType(BLACKLIST))))
				.thenReturn(super.generateBLFindPoliciesResponse());
		when(
				basePolicyServiceConsumerMock
						.findPolicies(argThat(new IsPolicyType(WHITELIST))))
				.thenReturn(super.generateWLFindPoliciesResponse());
		when(
				basePolicyServiceConsumerMock
						.findPolicies(argThat(new IsPolicyType(RL))))
				.thenReturn(super.generateRLFindPoliciesResponse());
	}

	/**
	 * Tear down.
	 */
	@After
	public void tearDown() {

		try {
			System.out.println("sleep");
			Thread.sleep(20000);
			System.out.println("wake");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		provider = null;
		rateLimitRequest = null;
	}

	/**
	 * Test init provider.
	 */
	@Test
	public void testInitProvider() {
		assertNotNull("Provider is null", provider);
	}

	/**
	 * Test is rate limited response not null.
	 */
	@Test
	public void testIsRateLimitedResponseNotNull() {
		IsRateLimitedResponse rateLimitResponse = provider
				.isRateLimited(rateLimitRequest);
		assertNotNull(rateLimitResponse);
	}

	/**
	 * Test is status ok.
	 */
	@Test
	public void testIsStatusOK() {
		rateLimitRequest.setOperationName("performSearch");
		rateLimitRequest.setResourceName("ServiceName");
		rateLimitRequest.setResourceType("SERVICE");
		SubjectType subjectType = new SubjectType();
		subjectType.setDomain("IP");
		subjectType.setValue("10.2.124.4");
		rateLimitRequest.getSubject().add(subjectType);
		IsRateLimitedResponse rateLimitResponse = provider
				.isRateLimited(rateLimitRequest);
		assertEquals("Status was no OK", RateLimiterStatus.SERVE_OK,
				rateLimitResponse.getStatus());
	}

	/**
	 * Test is status blocked.
	 */
	@Test
	public void testIsStatusBlocked() {
		IsRateLimitedResponse rateLimitResponse = provider
				.isRateLimited(rateLimitRequest);
		assertEquals("Status was not BLOCKED", RateLimiterStatus.BLOCK,
				rateLimitResponse.getStatus());
	}

	/**
	 * Test unsupported status.
	 */
	@Test
	public void testUnsupportedStatus() {
		rateLimitRequest.setOperationName("performSearch");
		rateLimitRequest.setResourceName("ServiceName");
		rateLimitRequest.setResourceType("URL");
		IsRateLimitedResponse rateLimitResponse = provider
				.isRateLimited(rateLimitRequest);
		assertEquals("Status was not UNSUPPORTED",
				RateLimiterStatus.UNSUPPORTED, rateLimitResponse.getStatus());

	}

	/**
	 * Test timestamp set.
	 */
	@Test
	public void testTimestampSet() {

		IsRateLimitedResponse rateLimitResponse = provider
				.isRateLimited(rateLimitRequest);
		assertNotNull("Missing Time Stamp", rateLimitResponse.getTimestamp());
	}

	/**
	 * Test mockfor rl.
	 */
	@Test
	public void testMockforRL() {
		PolicyKey policyKey2 = new PolicyKey();
		policyKey2.setPolicyType(RL);
		FindPoliciesRequest policyRequest2 = new FindPoliciesRequest();
		policyRequest2.getPolicyKey().add(policyKey2);
		policyRequest2.setOutputSelector(PolicyOutputSelector.ALL);
		QueryCondition queryCondition2 = new QueryCondition();
		Query query2 = new Query();
		query2.setQueryType("ActivePoliciesOnly");
		query2.setQueryValue("TRUE");
		queryCondition2.getQuery().add(query2);
		policyRequest2.setQueryCondition(queryCondition2);
		FindPoliciesResponse policyResponse2 = new FindPoliciesResponse();
		policyResponse2.setAck(AckValue.FAILURE);
		when(
				basePolicyServiceConsumerMock
						.findPolicies(argThat(new IsPolicyType(RL))))
				.thenReturn(policyResponse2);
		assertEquals(policyResponse2, basePolicyServiceConsumerMock
				.findPolicies(policyRequest2));
	}

	/**
	 * Test mockfor wl.
	 */
	@Test
	public void testMockforWL() {
		PolicyKey policyKey2 = new PolicyKey();
		policyKey2.setPolicyType(WHITELIST);
		FindPoliciesRequest policyRequest2 = new FindPoliciesRequest();
		policyRequest2.getPolicyKey().add(policyKey2);
		policyRequest2.setOutputSelector(PolicyOutputSelector.ALL);
		QueryCondition queryCondition2 = new QueryCondition();
		Query query2 = new Query();
		query2.setQueryType("ActivePoliciesOnly");
		query2.setQueryValue("TRUE");
		queryCondition2.getQuery().add(query2);
		policyRequest2.setQueryCondition(queryCondition2);
		FindPoliciesResponse policyResponse2 = new FindPoliciesResponse();
		policyResponse2.setAck(AckValue.FAILURE);
		when(
				basePolicyServiceConsumerMock
						.findPolicies(argThat(new IsPolicyType(WHITELIST))))
				.thenReturn(policyResponse2);
		assertEquals(policyResponse2, basePolicyServiceConsumerMock
				.findPolicies(policyRequest2));
	}

	/**
	 * Test mockfor bl.
	 */
	@Test
	public void testMockforBL() {
		// createMock
		FindPoliciesResponse policyResponse = new FindPoliciesResponse();
		policyResponse.setAck(AckValue.SUCCESS);

		PolicyKey policyKey = new PolicyKey();
		policyKey.setPolicyType("BLACKLIST");
		FindPoliciesRequest policyRequest = new FindPoliciesRequest();
		policyRequest.getPolicyKey().add(policyKey);
		policyRequest.setOutputSelector(PolicyOutputSelector.ALL);
		QueryCondition queryCondition = new QueryCondition();
		Query query = new Query();
		query.setQueryType("ActivePoliciesOnly");
		query.setQueryValue("TRUE");
		queryCondition.getQuery().add(query);
		policyRequest.setQueryCondition(queryCondition);

		when(
				basePolicyServiceConsumerMock
						.findPolicies(argThat(new IsPolicyType(BLACKLIST))))
				.thenReturn(policyResponse);

		// test it
		assertEquals(policyResponse, basePolicyServiceConsumerMock
				.findPolicies(policyRequest));

	}

	/**
	 * Test in b locked.
	 */
	@Test
	public void testInBLocked() {
		rateLimitRequest = super.generateIsRateLimitedRequest(rateLimitRequest);
		SubjectType subjectType = new SubjectType();
		subjectType.setDomain("IP");
		subjectType.setValue("10.2.124.100");
		rateLimitRequest.getSubject().add(subjectType);

		provider.setConsumer(basePolicyServiceConsumerMock);

		IsRateLimitedResponse rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should be Block", RateLimiterStatus.BLOCK,
				rateLimitResponse.getStatus());

	}

	/**
	 * Test in not in b land not in wl.
	 */
	@Test
	public void testInNotInBLandNotInWL() {
		rateLimitRequest = super.generateIsRateLimitedRequest(rateLimitRequest);
		SubjectType subjectType = new SubjectType();
		subjectType.setDomain("IP");
		subjectType.setValue("10.2.124.300");
		rateLimitRequest.getSubject().add(subjectType);

		IsRateLimitedResponse rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should be Block", RateLimiterStatus.BLOCK,
				rateLimitResponse.getStatus());
	}

	/**
	 * Test in block group.
	 */
	@Test
	public void testInBlockGroup() {
		rateLimitRequest = super.generateIsRateLimitedRequest(rateLimitRequest);
		SubjectGroupType groupType = new SubjectGroupType();
		groupType.setDomain("EBAY");
		groupType.setName("group" + BLACKLIST);
		rateLimitRequest.getResolvedSubjectGroup().add(groupType);
		IsRateLimitedResponse rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should be Block", RateLimiterStatus.BLOCK,
				rateLimitResponse.getStatus());
	}

	/**
	 * Test in wl sub.
	 */
	@Test
	public void testInWLSub() {
		rateLimitRequest = super.generateIsRateLimitedRequest(rateLimitRequest);
		SubjectType subjectType = new SubjectType();
		subjectType.setDomain("IP");
		subjectType.setValue("10.2.124.1");
		rateLimitRequest.getSubject().add(subjectType);

		IsRateLimitedResponse rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should be OK", RateLimiterStatus.SERVE_OK,
				rateLimitResponse.getStatus());
	}

	/**
	 * Test in wl sub group.
	 */
	@Test
	public void testInWLSubGroup() {
		rateLimitRequest = super.generateIsRateLimitedRequest(rateLimitRequest);
		SubjectGroupType groupType = new SubjectGroupType();
		groupType.setDomain("EBAY");
		groupType.setName("group" + WHITELIST);
		rateLimitRequest.getResolvedSubjectGroup().add(groupType);

		IsRateLimitedResponse rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should be Ok", RateLimiterStatus.SERVE_OK,
				rateLimitResponse.getStatus());
	}

	// @super.initRule() for the rules
	// 10.2.124.3:hits >2 allowed connection for the Ip is 10.2.124.3 effect
	// duration is 3000l rolloverPeriod 20000l effect flag
	// 10.2.124.5:hits > 1 effect duration is 5000l rolloverPeriod 20000l effect
	// block
	// HITS >8 allowed connection to all is 8 effect duration is 8000l
	// rolloverPeriod 20000l effect block
	/**
	 * Test in rl.
	 */
	@Test
	public void testInRL() {
		rateLimitRequest = super.generateIsRateLimitedRequest(rateLimitRequest);
		IsRateLimitedRequest rateLimitRequest2 = super
				.generateIsRateLimitedRequest(new IsRateLimitedRequest());
		SubjectType subjectType = new SubjectType();
		subjectType.setDomain("IP");
		subjectType.setValue("10.2.124.3");
		SubjectType subjectType2 = new SubjectType();
		subjectType2.setDomain("IP");
		subjectType2.setValue("10.2.124.5");
		rateLimitRequest.getSubject().add(subjectType);
		rateLimitRequest2.getSubject().add(subjectType2);
		IsRateLimitedResponse rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should be OK 10.2.124.3 counter is only 1 ",
				RateLimiterStatus.SERVE_OK, rateLimitResponse.getStatus());
		rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should be ok 10.2.124.3 counter is only 2",
				RateLimiterStatus.SERVE_OK, rateLimitResponse.getStatus());
		rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should be FLAG 10.2.124.3 counter is 3 > 2",
				RateLimiterStatus.FLAG, rateLimitResponse.getStatus());

		// since diff ip should
		rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest2));
		assertEquals("this should be OK since diff ip 10.2.124.5 counter is 1",
				RateLimiterStatus.SERVE_OK, rateLimitResponse.getStatus());
		// for the effect duration to reset it
		try {
			Thread.currentThread();
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// since effect is reset
		rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should Reset 10.2.124.3 counter is 1 ",
				RateLimiterStatus.SERVE_OK, rateLimitResponse.getStatus());
		rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should Reset 10.2.124.3 counter is 2 ",
				RateLimiterStatus.SERVE_OK, rateLimitResponse.getStatus());
		rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest2));
		assertEquals("this should SERVE_GIF, 10.2.124.5 counter is 2 >1 ",
				RateLimiterStatus.SERVE_GIF, rateLimitResponse.getStatus());

	}

	// HITS >8
	/**
	 * Test hits.
	 */
	@Test
	public void testHITS() {
		rateLimitRequest = super.generateIsRateLimitedRequest(rateLimitRequest);
		SubjectType subjectType = new SubjectType();
		subjectType.setDomain("IP");
		subjectType.setValue("10.2.124.4");
		rateLimitRequest.getSubject().add(subjectType);
		for (int i = 0; i < 8; i++) {
			IsRateLimitedResponse rateLimitResponse = provider
					.isRateLimited(super
							.generateIsRateLimitedRequest(rateLimitRequest));
			assertEquals("counter is ".concat(i + 1 + ""),
					RateLimiterStatus.SERVE_OK, rateLimitResponse.getStatus());
		}
		IsRateLimitedResponse rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should Reset", RateLimiterStatus.BLOCK,
				rateLimitResponse.getStatus());

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should Reset", RateLimiterStatus.SERVE_OK,
				rateLimitResponse.getStatus());
		rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals(RateLimiterStatus.SERVE_OK, rateLimitResponse.getStatus());
	}

	// "ServiceName.checkout:count > 15 effect is BLOCK effect duration 8000l
	// roll over period 20000l

	/**
	 * Test servicecount.
	 */
	@Test
	public void testServicecount() {
		// ServiceName.checkout:count > 15
		rateLimitRequest = super.generateIsRateLimitedRequest(rateLimitRequest);
		SubjectType subjectType = new SubjectType();
		subjectType.setDomain("IP");
		subjectType.setValue("10.2.124.4");
		rateLimitRequest.getSubject().add(subjectType);
		for (int i = 0; i < 15; i++) {

			if (i == 8) {
				IsRateLimitedResponse rateLimitResponse = provider
						.isRateLimited(super
								.generateIsRateLimitedRequest(rateLimitRequest));
				assertEquals("this should be OK i" + i,
						RateLimiterStatus.BLOCK, rateLimitResponse.getStatus());
				i++;
				// HITS >8 since block effect duration reset
				try {
					Thread.sleep(8000);
					System.out.println("resetting ....... effect duration ");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			IsRateLimitedResponse rateLimitResponse = provider
					.isRateLimited(super
							.generateIsRateLimitedRequest(rateLimitRequest));
			assertEquals("this should be OK i" + i, RateLimiterStatus.SERVE_OK,
					rateLimitResponse.getStatus());
		}
		IsRateLimitedResponse rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should Reset", RateLimiterStatus.BLOCK,
				rateLimitResponse.getStatus());
		// rollover period test
		try {
			Thread.sleep(20000);
			System.out.println("resetting ....... rollover period ");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should Reset", RateLimiterStatus.SERVE_OK,
				rateLimitResponse.getStatus());
		rateLimitResponse = provider.isRateLimited(super
				.generateIsRateLimitedRequest(rateLimitRequest));
		assertEquals("this should Reset", RateLimiterStatus.SERVE_OK,
				rateLimitResponse.getStatus());
	}

}
