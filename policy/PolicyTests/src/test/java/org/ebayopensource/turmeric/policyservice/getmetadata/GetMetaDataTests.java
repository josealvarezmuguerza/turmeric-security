/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.getmetadata;

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
import org.ebayopensource.turmeric.security.v1.services.GetMetaDataRequest;
import org.ebayopensource.turmeric.security.v1.services.GetMetaDataResponse;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePair;
import org.ebayopensource.turmeric.security.v1.services.Query;
import org.ebayopensource.turmeric.security.v1.services.QueryCondition;
import org.ebayopensource.turmeric.security.v1.services.Resolution;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GetMetaDataTests{

	private static final String CONFIG_DELIMITER = ":";
	private static Properties props = new Properties();
	private String m_testCaseName;
	private String m_queryCondition;
	private String m_expectedResult;
	private static final String m_PropFilePath = "GetMetaDataTests.properties";
	
	@BeforeClass
	public static void setUp() throws Exception {
	}

	@AfterClass
	public static void tearDown() throws Exception {
	}

	public GetMetaDataTests(String testCaseName, String queryCondition, String expectedResult) {
		this.m_testCaseName =testCaseName;
		this.m_queryCondition = queryCondition;
		this.m_expectedResult = expectedResult;
	}
	
	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data(){
		return loadGetMetaDataInputData();
	}
	
    @Test
	public void getMetaData() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + m_testCaseName + " *** ");
		GetMetaDataRequest getMetaDataRequest = new GetMetaDataRequest();
		GetMetaDataResponse response = new GetMetaDataResponse();
		try {
			getMetaDataRequest = constructGetMetaDataRequest();
			response = PolicyServiceTestHelper.getInstance().getMetaData(getMetaDataRequest);
			String errorMessage = response.getErrorMessage() != null ? response
					.getErrorMessage().getError().get(0).getMessage() : null;
			StringTokenizer policyTokens = new StringTokenizer(m_expectedResult,"|");
			String expectedAckValue = getToken(policyTokens);
			String expectedErrorMessage = getToken(policyTokens);
			String expectedTypes = getToken(policyTokens);
			if (expectedAckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				if (expectedTypes!=null) {
					StringTokenizer typesTokens = new StringTokenizer(expectedTypes,":");
					
					List<KeyValuePair> keyList = response.getMetadataValue();
					boolean isExists = false;
					while (typesTokens.hasMoreTokens()) {
						String typeToken = getToken(typesTokens);
						for (KeyValuePair pair : keyList) {
							if (pair.getKey().equalsIgnoreCase(typeToken)) {
								isExists = true;
								break;
							}
						}
						assertTrue(typeToken+ "not found in metadata",isExists);
					}
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
	public static Collection loadGetMetaDataInputData() {
		
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		Pattern pattern =Pattern.compile("testcase(\\d*).name");
		int totalTests = 0;
		try {
			InputStream inputStream = GetMetaDataTests.class.getResourceAsStream(m_PropFilePath);
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
				String policyKey = "testcase" + i + ".querycondition";
				String expectedResult = "testcase" + i + ".response";
				eachRowData.add(props.getProperty(testName));
				eachRowData.add(props.getProperty(policyKey));
				eachRowData.add(props.getProperty(expectedResult));
				list.add(eachRowData.toArray());
			}
		} catch (IOException e) {	}
		
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
		
	private GetMetaDataRequest constructGetMetaDataRequest() throws Exception {
		GetMetaDataRequest request = new GetMetaDataRequest();
		QueryCondition queryCondition = new QueryCondition();
		Query query = new Query();
		
		if (m_queryCondition!=null) {
			StringTokenizer queryTokens = new StringTokenizer(m_queryCondition,CONFIG_DELIMITER);
			String queryType =getToken(queryTokens);
			String queryValue= getToken(queryTokens);
			query.setQueryType(queryType); // Effect/SubjectSearchScope/MaskedIds/ActivePoliciesOnly
			query.setQueryValue(queryValue); // BLOCK|FLAG|CHALLENGE|ALLOW/TARGET|EXCLUDED|BOTH/TRUE|FALSE/TRUE|FALSE
			queryCondition.setResolution(Resolution.AND);
			queryCondition.getQuery().add(query);
			
			System.out.println("\t queryType = "+queryType+ " , QueryValue= "+queryValue);
		     request.setQueryCondition(queryCondition);
		}
		return request;
	}

}
