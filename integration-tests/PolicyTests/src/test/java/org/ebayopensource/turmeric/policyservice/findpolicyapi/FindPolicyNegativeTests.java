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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesResponse;
import org.ebayopensource.turmeric.security.v1.services.OperationKey;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.Query;
import org.ebayopensource.turmeric.security.v1.services.QueryCondition;
import org.ebayopensource.turmeric.security.v1.services.Resolution;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FindPolicyNegativeTests{
	private static Properties props = new Properties();
	private static final String CONFIG_DELIMITER = ":";
	private String m_testCaseName;
	private String m_policyKey;
	private String m_subjectKey;
	private String m_subjectGroupKey;
	private String m_resourceKey;
	private String m_operationKey;
	private String m_queryCondition;
	private String m_expectedResult;

	private static final String s_PropFilePath = "FindPolicyNegativeTests.properties";
	
	@BeforeClass
	public static void setUp() throws Exception {

	}

	@AfterClass
	public static void tearDown() throws Exception {

	}

	public FindPolicyNegativeTests(String testCaseName, String policyKey, String subjectKey, String subjectGroupKey,
			String resourceKey, String operationKey, String queryCondition,String expectedResult) {
		this.m_testCaseName =testCaseName;
		this.m_policyKey = policyKey;
		this.m_subjectKey = subjectKey;
		this.m_subjectGroupKey = subjectGroupKey;
		this.m_resourceKey = resourceKey;
		this.m_operationKey = operationKey;
		this.m_queryCondition = queryCondition;
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
			StringTokenizer resTokens = new StringTokenizer(m_expectedResult,"|");
			String expectedAckValue = getToken(resTokens);
			String expectedErrorMessage = getToken(resTokens);
			if (expectedAckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
			} else {
				assertEquals(errorMessage, AckValue.FAILURE, response.getAck());
				assertNotNull(errorMessage, response.getErrorMessage());
				assertEquals(errorMessage, expectedErrorMessage,errorMessage);
			}
	
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		} finally {
			   //cleanUpPolicy();
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
			InputStream inputStream = FindPolicyNegativeTests.class.getResourceAsStream(s_PropFilePath);
			props.load(inputStream);
			Iterator it = props.keySet().iterator();
			while(it.hasNext()) {
				String key = it.next().toString();
				Matcher matcher = pattern.matcher(key);
				if (matcher.find())	totalTests++;
			}
			for (int i = 0; i < totalTests; i++) {
				eachRowData = new ArrayList();
				String testName = "testcase" + i + ".name";
				String policyKey = "testcase" + i + ".request.policykey";
				String subjectKey = "testcase" + i + ".request.subjectkey";
				String subjectGroupKey = "testcase" + i + ".request.subjectgroupkey";
				String resourceKey = "testcase" + i + ".request.resourcekey";
				String operationKey = "testcase" + i + ".request.operationkey";
				String queryCondition = "testcase" + i + ".request.querycondition";
				String expectedResult = "testcase" + i + ".response";
				eachRowData.add(props.getProperty(testName));
				eachRowData.add(props.getProperty(policyKey));
				eachRowData.add(props.getProperty(subjectKey));
				eachRowData.add(props.getProperty(subjectGroupKey));
				eachRowData.add(props.getProperty(resourceKey));
				eachRowData.add(props.getProperty(operationKey));
				eachRowData.add(props.getProperty(queryCondition));
				eachRowData.add(props.getProperty(expectedResult));
				list.add(eachRowData.toArray());
			}
		} catch (IOException e) {
			
		}
		
		return list;
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
			
			policyKey.setPolicyType(policyType);
			policyKey.setPolicyName(policyName);
			request.getPolicyKey().add(policyKey);	
			System.out.println("\t policyId = "+policyId+ " , policyType= "+policyType+" , policyName = "+policyName);
		}
		if (m_subjectKey!=null) {
			SubjectKey subjectKey = new SubjectKey();
			StringTokenizer subjTokens = new StringTokenizer(m_subjectKey,CONFIG_DELIMITER);
			String subjectId  =getToken(subjTokens);
			String subjectType =getToken(subjTokens);
			String subjectName= getToken(subjTokens);
			subjectKey.setSubjectType(subjectType);
			subjectKey.setSubjectName(subjectName);
			request.getSubjectKey().add(subjectKey);
			System.out.println("\t subjectId = "+subjectId+ " , subjectType= "+subjectType+" , subjectName = "+subjectName);
		}
		if (m_subjectGroupKey!=null) {
			SubjectGroupKey subjectGroupKey = new SubjectGroupKey();
			StringTokenizer subjTokens = new StringTokenizer(m_subjectGroupKey,CONFIG_DELIMITER);
			String subjectGroupId  =getToken(subjTokens);
			String subjectType =getToken(subjTokens);
			String subjectGroupName= getToken(subjTokens);
			subjectGroupKey.setSubjectType(subjectType);
			subjectGroupKey.setSubjectGroupName(subjectGroupName);
			request.getSubjectGroupKey().add(subjectGroupKey);
			System.out.println("\t subjectGroupId = "+subjectGroupId+ " , subjectType= "+subjectType+" , subjectGroupName = "+subjectGroupName);
		}
		if (m_resourceKey!=null) {
			ResourceKey resourceKey = new ResourceKey();
			StringTokenizer resourceTokens = new StringTokenizer(m_resourceKey,CONFIG_DELIMITER);
			String resourceId  =getToken(resourceTokens);
			String resourceType =getToken(resourceTokens);
			String resourceName= getToken(resourceTokens);
			resourceKey.setResourceType(resourceType);
			resourceKey.setResourceName(resourceName);
			request.getResourceKey().add(resourceKey);		
			System.out.println("\t resourceId = "+resourceId+ " , resourceType= "+resourceType+" , resourceName = "+resourceName);
		}
		if (m_operationKey!=null) {
			OperationKey operationKey = new OperationKey();
			StringTokenizer opTokens = new StringTokenizer(m_operationKey,CONFIG_DELIMITER);
			String opId  =getToken(opTokens);
			String opName= getToken(opTokens);
			String resType =getToken(opTokens);
			String resName =getToken(opTokens);
			operationKey.setOperationName(opName);
			operationKey.setResourceType(resType);
			operationKey.setResourceName(resName);
			request.getOperationKey().add(operationKey);
			System.out.println("\t operationId = "+opId+ " , operationName= "+opName+" , resourceName = "+resName);
		}
		if (m_queryCondition!=null) {
			QueryCondition queryCondition = new QueryCondition();
			StringTokenizer opTokens = new StringTokenizer(m_queryCondition,CONFIG_DELIMITER);
			String queryType = getToken(opTokens);
			String queryValue = getToken(opTokens);
			Query query = new Query();
			query.setQueryType(queryType); // Effect/SubjectSearchScope/MaskedIds/ActivePoliciesOnly
			query.setQueryValue(queryValue); // BLOCK|FLAG|CHALLENGE|ALLOW/TARGET|EXCLUDED|BOTH/TRUE|FALSE/TRUE|FALSE
			queryCondition.setResolution(Resolution.AND);
			queryCondition.getQuery().add(query);
			request.setQueryCondition(queryCondition);
			System.out.println("\t QueryConditon  querytype = "+ queryType +" queryValue = " +queryValue);
		}
		
		return request;
	}
	
}
