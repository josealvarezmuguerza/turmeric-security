/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.resource;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.GetResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.GetResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class FindResourceTests{

	private static final String CONFIG_DELIMITER = ",";
	private String testCaseName;
	private String inputData;
	private String expectedResult;
	private static String preRequisite;
	private static Map<String,Long> m_resourceIds = new HashMap<String,Long>();
	private static List<Resource> m_resourceList;
	private static final String s_PropFilePath = "FindResourceTests.properties";
	
	@BeforeClass
	public static void setUp() throws Exception {
		if (preRequisite!=null) {
				createPreRequisiteData(preRequisite);
		}
	}
	
	
	@AfterClass
	public static void tearDown() throws Exception {
		cleanUpResources();
	}
	

	public FindResourceTests(String testCaseName,String inputData, String expectedResult) {
		this.testCaseName = testCaseName;
		this.inputData = inputData;
		this.expectedResult = expectedResult;
	}
	
	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data(){
		return loadInputData();
	}
	
    @Test
	public void findResources() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + testCaseName + " *** ");
        GetResourcesRequest getResourcesRequest = new GetResourcesRequest();
		try {
			StringTokenizer subjTokens = new StringTokenizer(inputData,";");
			while(subjTokens.hasMoreTokens()) {
				String token = subjTokens.nextToken();
				constructFindResourcesRequest(getResourcesRequest,token);
			}
				GetResourcesResponse response = new GetResourcesResponse();
				response = PolicyServiceTestHelper.getInstance().getResources(getResourcesRequest);
				String errorMessage = response.getErrorMessage() != null ? response
						.getErrorMessage().getError().get(0).getMessage() : null;

			StringTokenizer restokens = new StringTokenizer(expectedResult,
					CONFIG_DELIMITER);
			String expectedAckValue = getToken(restokens);
			String expectedErrorMessage = getToken(restokens);
			String numOfResources = getToken(restokens);
			int expectedTotalResources = 0;
			if (numOfResources != null) {
				expectedTotalResources = Integer.parseInt(numOfResources);
			}
			String expectedResourceName = getToken(restokens);
			System.out.println("\tExpected Results AckValue = "	+ expectedAckValue + " ; errorMessage = "
					+ expectedErrorMessage + " ; Number of resources = "+ expectedTotalResources + " ;Match Resource ="	+ expectedResourceName);
			if (expectedAckValue != null
					&& expectedAckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				int actualTotalResources = 0;
				if (response.getResources() != null
						&& response.getResources().size() > 0) {
					actualTotalResources = response.getResources().size();
				}
				assertTrue("TestCase ="+testCaseName + "; Mismatch in total resources ",actualTotalResources >= expectedTotalResources);
				if (expectedResourceName != null) {
					for (Resource resource : response.getResources()) {
						assertTrue(expectedResourceName.contains(resource
								.getResourceName()));
					}
				}
			} else {
				assertEquals(errorMessage, AckValue.FAILURE, response.getAck());
				assertNotNull(errorMessage, response.getErrorMessage());
				assertEquals(errorMessage, expectedErrorMessage, errorMessage);
			}

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
		System.out.println("*** Test Scenario : " + testCaseName + " completed successfully ***");
	}

	
	@SuppressWarnings("unchecked")
	public static Collection loadInputData() {
		Properties props = new Properties();
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		InputStream inputStream = FindResourceTests.class.getResourceAsStream(s_PropFilePath);
		try {
			props.load(inputStream);
			// load prerequisite if exists
			int len = props.size();
			preRequisite = props.getProperty("testcase.prerequisite");
			if (preRequisite!=null) len--;
			
			for (int i = 0; i < len / 3; i++) {
				eachRowData = new ArrayList();
				String testName = "testcase" + i + ".name";
				String input = "testcase" + i + ".request";
				String expected = "testcase" + i + ".response";
				eachRowData.add(props.getProperty(testName));
				eachRowData.add(props.getProperty(input));
				eachRowData.add(props.getProperty(expected));
				list.add(eachRowData.toArray());
			}
		} catch (IOException e) {
			
		}
		
		return list;
	}
		
	private static String getToken(StringTokenizer tokenizer) {
		if(tokenizer.hasMoreTokens()) {
		String token = tokenizer.nextToken().trim();
			return "null".equals(token) ? null : token;
		} 
		return null;
	}
	
	private void constructFindResourcesRequest(GetResourcesRequest getResourcesRequest,String subjectToken) throws Exception {
		String resourceId = null;
		String resourceName = null;
		String resourceType = null;
		
		StringTokenizer tokens = new StringTokenizer(subjectToken,CONFIG_DELIMITER);
		resourceId  = getToken(tokens); resourceType = getToken(tokens);	
		resourceName = getToken(tokens);	
			
		ResourceKey resourceKey =new ResourceKey();	
		if (resourceId!=null && resourceId.contains("?")) {
			resourceKey.setResourceId(m_resourceIds.get(resourceName));
			
		} else {
			resourceKey.setResourceName(resourceName);
		}
		
		resourceKey.setResourceType(resourceType);
		getResourcesRequest.getResourceKey().add(resourceKey);
		System.out.println(" > ResourceId = "+ resourceKey.getResourceId()+" ; Resource Name = "+ resourceName + " ; Type = "+ resourceType);
		
	}
	
	private static void createPreRequisiteData(String preRequisite) {
	
		CreateResourcesRequest createResourcesRequest = new CreateResourcesRequest();
		CreateResourcesResponse createResourcesResponse = new CreateResourcesResponse();
		List<Resource> resourceList = createResourcesRequest.getResources();
		
		try {
			StringTokenizer resTokens = new StringTokenizer(preRequisite,";");
			while(resTokens.hasMoreTokens()) {
				String token = resTokens.nextToken();
				constructCreateResourceRequest(resourceList,token);
			}
			m_resourceList = resourceList; 
			createResourcesResponse =PolicyServiceTestHelper.getInstance().createResources(createResourcesRequest);
			
			if (createResourcesResponse.getResourceIds()!=null) {
				List<Long> resourceIds= createResourcesResponse.getResourceIds();
				int i=0;
				for ( Long id :resourceIds) {
					m_resourceIds.put(resourceList.get(i++).getResourceName(),id);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
		
	}

	private static void constructCreateResourceRequest(
			List<Resource> resList, String token) {
			Resource resource =  new Resource();
			StringTokenizer resTokens = new StringTokenizer(token,CONFIG_DELIMITER);
			resource.setResourceType(getToken(resTokens));
			resource.setResourceName(getToken(resTokens));
			resource.setDescription(getToken(resTokens));
			String opsToken = getToken(resTokens);
			if (opsToken!=null) {
				createOperations(resource,opsToken);
			}
			resList.add(resource);
		}
	
	private static void createOperations(Resource resource, String ops) {
		List<Operation> operations = resource.getOperation();
		StringTokenizer opTokens =  new StringTokenizer(ops,"&");
		Operation operation = new Operation();
		System.out.print("\tOperation(s) ..");
		String tempOp = null;
		while (opTokens.hasMoreTokens()) {
			operation = new Operation();
			tempOp = opTokens.nextToken();
			StringTokenizer opDescTokens =  new StringTokenizer(tempOp,":");
			// if description is set
			if (opDescTokens.countTokens()>1) {
				operation.setOperationName(getToken(opDescTokens));
				operation.setDescription(getToken(opDescTokens));
			} else {
				operation.setOperationName(tempOp);	
			}
			operations.add(operation);
			System.out.print(operation.getOperationName()+ " , " + " Desctiption = "+operation.getDescription() +" ");
		}
	}

	private static void cleanUpResources() {
		for(Resource resource:m_resourceList) {
			PolicyServiceTestHelper.getInstance().cleanUpResource(resource.getResourceName(),resource.getResourceType());
		}
	}

}
