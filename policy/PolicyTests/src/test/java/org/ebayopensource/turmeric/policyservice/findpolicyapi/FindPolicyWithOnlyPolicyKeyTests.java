/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/

package org.ebayopensource.turmeric.policyservice.findpolicyapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesResponse;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.Query;
import org.ebayopensource.turmeric.security.v1.services.QueryCondition;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Resources;
import org.ebayopensource.turmeric.security.v1.services.Target;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.ebayopensource.turmeric.utils.RuleUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FindPolicyWithOnlyPolicyKeyTests{

	private static final String CONFIG_DELIMITER = ":";
	private static Properties props = new Properties();
	private String m_testCaseName;
	private String m_policyKey;
	private String m_expectedResult;
	private static Map<String,Long> m_policyIds = new HashMap<String,Long>();
	private static List<Policy> m_policyList = new ArrayList<Policy>();
	private static final String m_PropFilePath = "FindPolicyWithOnlyPolicyKeyTests.properties";
	
	@BeforeClass
	public static void setUp() throws Exception {
		System.out.println(" *** Create Pre-Requisite data ***");
		cleanUpPolicy();
		createPreRequisiteData();
		System.out.println(" *** Create Pre-Requisite data has been completed successfully ***");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		cleanUpPolicy();
	}

	public FindPolicyWithOnlyPolicyKeyTests(String testCaseName, String policyKey, String expectedResult) {
		this.m_testCaseName =testCaseName;
		this.m_policyKey = policyKey;
		this.m_expectedResult = expectedResult;
	}
	
	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data(){
		return loadFindPolicyInputData();
	}
	
    @Test
	public void findPolicy() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + m_testCaseName + " *** ");
		FindPoliciesRequest findPoliciesRequest = new FindPoliciesRequest();
		FindPoliciesResponse response = new FindPoliciesResponse();
		try {
			findPoliciesRequest = constructFindPoliciesRequest();
			response = PolicyServiceTestHelper.getInstance().findPolicies(findPoliciesRequest);
			String errorMessage = response.getErrorMessage() != null ? response
					.getErrorMessage().getError().get(0).getMessage() : null;
			StringTokenizer policyTokens = new StringTokenizer(m_expectedResult,"|");
			String expectedAckValue = getToken(policyTokens);
			String expectedErrorMessage = getToken(policyTokens);
			String numOfPolicies = getToken(policyTokens);
			if (expectedAckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				int actualPolicies = response.getPolicySet().getPolicy().size();
				int expectedPolices = 0;
				if (numOfPolicies!=null) {
					expectedPolices = Integer.parseInt(numOfPolicies);
				}
				assertTrue("TestCase ="+m_testCaseName + "; Mismatch in total number of policies ",actualPolicies >= expectedPolices);
				while(policyTokens.hasMoreTokens()) {
					String policyName = getToken(policyTokens);
					boolean exists = false;
					for (Policy policy  : response.getPolicySet().getPolicy()) {
							if (policy.getPolicyName().equals(policyName)) { 
								exists = true;
								break;
							}
						}
						assertTrue(policyName+" doesn't exist", exists);	
					}
				
			} else {
				assertEquals(errorMessage, AckValue.FAILURE, response.getAck());
				assertNotNull(errorMessage, response.getErrorMessage());
				assertEquals(errorMessage, expectedErrorMessage,errorMessage);
			}
	
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
		System.out.println("*** Test Scenario : " + m_testCaseName + " completed successfully ***");
	}
		
	@SuppressWarnings("unchecked")
	public static Collection loadFindPolicyInputData() {
		
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		Pattern pattern =Pattern.compile("testcase(\\d*).name");
		int totalTests = 0;
		try {
			InputStream inputStream = FindPolicyWithOnlyPolicyKeyTests.class.getResourceAsStream(m_PropFilePath);
			props.load(inputStream);
			Iterator it = props.keySet().iterator();
			while(it.hasNext()) {
				String key = it.next().toString();
				Matcher matcher = pattern.matcher(key);
				if (matcher.find()) totalTests++;
			}
			for (int i = 0; i < totalTests; i++) {
				eachRowData = new ArrayList();
				String testName = "testcase" + i + ".name";
				String policyKey = "testcase" + i + ".request.policykey";
				String expectedResult = "testcase" + i + ".response";
				eachRowData.add(props.getProperty(testName));
				eachRowData.add(props.getProperty(policyKey));
				eachRowData.add(props.getProperty(expectedResult));
				list.add(eachRowData.toArray());
			}
		} catch (IOException e) {	}
		
		return list;
	}
	
	private static void createPreRequisiteData() {
		String policies = props.getProperty("testcase.prerequisite.policies");
		createPolicies(policies);
	}
	
	private static void createPolicies(String policies) {
		if (policies!=null) {
			StringTokenizer resTokens = new StringTokenizer(policies,";");
			Policy policy = new Policy();
			CreatePolicyRequest createPolicyRequest = new CreatePolicyRequest();
			while(resTokens.hasMoreTokens()) {
				String token = resTokens.nextToken();
				constructCreatePolicyRequest(policy,token);
				policy = RuleUtils.updatePolicyRule(policy);
				m_policyList.add(policy);
				createPolicyRequest.setPolicy(policy);
				CreatePolicyResponse res = PolicyServiceTestHelper.getInstance().createPolicy(createPolicyRequest);
				long policyid = 0L;
				if (res.getAck().equals(AckValue.SUCCESS)) {
					policyid = res.getPolicyId();
					m_policyIds.put(policy.getPolicyName(), policyid);
				}
			}
		}
	}

	private static void constructCreatePolicyRequest(Policy policy, String token) {
		StringTokenizer policyInfo = new StringTokenizer(token,"|");
		Resources resources = new Resources();
		String policDetails = getToken(policyInfo);
		String resourcesDetails = getToken(policyInfo);
		
		StringTokenizer policyTokens = new StringTokenizer(policDetails,CONFIG_DELIMITER);
		policy.setPolicyType(getToken(policyTokens));
		policy.setPolicyName(getToken(policyTokens));
		policy.setDescription(getToken(policyTokens));
		System.out.println("\t  policyType= "+policy.getPolicyType()+" , policyName = "+policy.getPolicyName());
		PolicyServiceTestHelper.getInstance().cleanupPolicy(policy.getPolicyName(), policy.getPolicyType());
		if (resourcesDetails!=null) {
			Resource resource = new Resource();
			StringTokenizer resourceTokens = new StringTokenizer(resourcesDetails,CONFIG_DELIMITER);
			while (resourceTokens.hasMoreTokens()) {
				resource = new Resource();
				resource.setResourceType(getToken(resourceTokens));
				resource.setResourceName(getToken(resourceTokens));
				resources.getResource().add(resource);
				System.out.println("\t  resourceType= "+resource.getResourceType()+" , resourceName = "+resource.getResourceName());
			}
			
		}
		Target target = new Target();
		target.setResources(resources);
		policy.setTarget(target);
	}

	private static String getToken(StringTokenizer tokenizer)
	{
		if(tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken().trim();
			return "null".equals(token) ? null : token;
		} 
		return null;
	}
		
	private FindPoliciesRequest constructFindPoliciesRequest() throws Exception {
		FindPoliciesRequest request = new FindPoliciesRequest();
		if (m_policyKey!=null) {
			PolicyKey policyKey = new PolicyKey();
			StringTokenizer policyTokens = new StringTokenizer(m_policyKey,CONFIG_DELIMITER);
			String policyId  =getToken(policyTokens);
			String policyType =getToken(policyTokens);
			String policyName= getToken(policyTokens);
			if (policyId!=null && policyId.contains("?")) {
				long id = m_policyIds.get(policyName)!=null ?m_policyIds.get(policyName) : -1;
				policyKey.setPolicyId(id);
			}
			policyKey.setPolicyType(policyType);
			policyKey.setPolicyName(policyName);
			request.getPolicyKey().add(policyKey);	
			System.out.println("\t policyId = "+policyKey.getPolicyId()+ " , policyType= "+policyType+" , policyName = "+policyName);
			
			QueryCondition queryCondition = new QueryCondition();
	        Query query = new Query();
	        query.setQueryType("ActivePoliciesOnly");
	        query.setQueryValue("FALSE");
	        queryCondition.getQuery().add(query);
	        request.setQueryCondition(queryCondition);
		}
		return request;
	}
	private static void cleanUpPolicy() {
		for (Policy policy:m_policyList) {
			PolicyServiceTestHelper.getInstance().cleanupPolicy(policy.getPolicyName(),policy.getPolicyType());
		}
		
	}


}
