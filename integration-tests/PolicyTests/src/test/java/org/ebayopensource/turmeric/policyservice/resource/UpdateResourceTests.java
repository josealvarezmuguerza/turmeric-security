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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.GetResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.GetResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.UpdateMode;
import org.ebayopensource.turmeric.security.v1.services.UpdateResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.UpdateResourcesResponse;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class UpdateResourceTests{

	private static final String CONFIG_DELIMITER = ":";
	private String testCaseName;
	private String inputData;
	private String updateMode;
	private String expectedResult;
	private static String preRequisite;
	private static Map<String,Long> m_resourceIds = new HashMap<String,Long>();
	private static List<Resource> m_resourceList;
	private static final String s_PropFilePath = "UpdateResourceTests.properties";
	
	@Before
	public void setUp() throws Exception {
		if (preRequisite!=null) {
			System.out.println(" *** Create Pre-Requisite data has been completed successfully ***");
			createPreRequisiteData(preRequisite);
			System.out.println(" *** Create Pre-Requisite data has been completed successfully ***");
		}
	}
	
	
	@After
	public void tearDown() throws Exception {
		cleanUpResources();
	}
	

	public UpdateResourceTests(String testCaseName,String inputData,String updateMode, String expectedResult) {
		this.testCaseName = testCaseName;
		this.updateMode = updateMode;
		this.inputData = inputData;
		this.expectedResult = expectedResult;
	}
	
	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data(){
		return loadInputData();
	}
	
    @Test
	public void updateResources() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + testCaseName + " *** ");
        UpdateResourcesRequest updateResourcesRequest = new UpdateResourcesRequest();
		try {
			StringTokenizer subjTokens = new StringTokenizer(inputData,";");
			while(subjTokens.hasMoreTokens()) {
				String token = subjTokens.nextToken();
				constructUpdateResourcesRequest(updateResourcesRequest,token);
			}
			
			if (updateMode!=null && updateMode.equalsIgnoreCase("REPLACE")) {
				updateResourcesRequest.setUpdateMode(UpdateMode.REPLACE);
			} else if (updateMode!=null && updateMode.equalsIgnoreCase("DELETE")) {
				updateResourcesRequest.setUpdateMode(UpdateMode.DELETE);
			} else {
				updateResourcesRequest.setUpdateMode(UpdateMode.UPDATE);
			}
			UpdateResourcesResponse response = new UpdateResourcesResponse();
			response = PolicyServiceTestHelper.getInstance().updateResources(updateResourcesRequest);
			String errorMessage = response.getErrorMessage() != null ? response
					.getErrorMessage().getError().get(0).getMessage() : null;
			System.out.println("\tActual AckValue = "	+ response.getAck()+ "; errorMessage = "+errorMessage);
				
			StringTokenizer resTokens = new StringTokenizer(expectedResult,"#");
			
			String expectedAckValue = getToken(resTokens);
			String expectedErrorMessage = getToken(resTokens);
			
			if (expectedAckValue != null
					&& expectedAckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				
				// get resources to match the response 
				GetResourcesRequest getResourcesRequest = new GetResourcesRequest();
				GetResourcesResponse getResponse = new GetResourcesResponse();
				constructGetResourceRequest(getResourcesRequest,updateResourcesRequest.getResources());
				getResponse = PolicyServiceTestHelper.getInstance().getResources(getResourcesRequest);
				
				assertEquals(errorMessage, AckValue.SUCCESS, getResponse.getAck());
				String numOfResources = getToken(resTokens);
				String matchResources = getToken(resTokens);
				
				int expectedTotalResources = 0;
				int actualTotalResources = 0;
				int counter = 0;
				List<Resource> resourceList= null;
				if (numOfResources != null) {
					expectedTotalResources = Integer.parseInt(numOfResources);
				}
				
				if (getResponse.getResources() != null
						&& getResponse.getResources().size() > 0) {
					resourceList = getResponse.getResources();
					actualTotalResources = resourceList.size();
					
				}
				assertTrue("TestCase ="+testCaseName + "; Mismatch in total resources ",actualTotalResources >= expectedTotalResources);
				
				StringTokenizer resources = new StringTokenizer(matchResources,";");
				while (resources.hasMoreTokens()) {
					String resourceToken = getToken(resources);
					Resource resource = resourceList.get(counter++);
					verifyGetResourceResponse(resource,resourceToken);
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

	
	private void verifyGetResourceResponse(Resource resource,
			String resourceToken) {
		StringTokenizer tokens = new StringTokenizer(resourceToken,":");
		String rName = getToken(tokens);
		if (rName!=null) {
			assertEquals(rName,resource.getResourceName());
		}
		String totalOps =  getToken(tokens);
		System.out.println("totalOps"+totalOps );
		System.out.println("resourceToken"+resourceToken );
		System.out.println("resource.getResourceName()"+resource.getResourceName() );
		int expectedTotalOps = 0;
		if (totalOps != null) {
			expectedTotalOps = Integer.parseInt(totalOps);
		}
		if (expectedTotalOps == 0) {
			assertTrue(resource.getOperation()==null || resource.getOperation().isEmpty());
		} else {
		    assertTrue(resource.getOperation()!=null && !resource.getOperation().isEmpty());
			assertEquals(expectedTotalOps,resource.getOperation().size());
			for (int i = 0; i < expectedTotalOps; i++ ) {
				String expectedOperationName  = getToken(tokens);
				boolean exists = false;
				for (Operation op : resource.getOperation()) {
					if (op.getOperationName().equals(expectedOperationName)) { 
						exists = true;
						break;
					}
				}
				assertTrue(expectedOperationName+" doesn't exist", exists);	
			}
		}
				
	}


	private void constructGetResourceRequest(
			GetResourcesRequest getResourcesRequest, List<Resource> resources) {
		ResourceKey key = new ResourceKey();
		for (Resource res:resources) {
			key = new ResourceKey();
			key.setResourceId(res.getResourceId());
			key.setResourceName(res.getResourceName());
			key.setResourceType(res.getResourceType());
			getResourcesRequest.getResourceKey().add(key);
		}
		
	}


	@SuppressWarnings("unchecked")
	public static Collection loadInputData() {
		Properties props = new Properties();
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		Pattern pattern =Pattern.compile("testcase(\\d*).name");
		
		try {
			InputStream inputStream = UpdateResourceTests.class.getResourceAsStream(s_PropFilePath);
			props.load(inputStream);
			Iterator it = props.keySet().iterator();
			int totalTests = 0;
			while(it.hasNext()) {
				String key = it.next().toString();
				Matcher matcher = pattern.matcher(key);
				if (matcher.find()) {
					totalTests++;
				}
			}
			// load prerequisite if exists
			preRequisite = props.getProperty("testcase.prerequisite");
					
			for (int i = 0; i < totalTests; i++) {
				eachRowData = new ArrayList();
				String testName = "testcase" + i + ".name";
				String input = "testcase" + i + ".updaterequest";
				String updateMode ="testcase" + i + ".updatemode"; 
				String expected = "testcase" + i + ".response";
				eachRowData.add(props.getProperty(testName));
				eachRowData.add(props.getProperty(input));
				eachRowData.add(props.getProperty(updateMode));
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
	
	private void constructUpdateResourcesRequest(UpdateResourcesRequest updateResourcesRequest,String resToken) throws Exception {
		Resource resource =new Resource();
		String resourceId = null;
		String resourceName = null;
		String newResourceName = null;
		String resourceType = null;
		StringTokenizer tokens = new StringTokenizer(resToken,CONFIG_DELIMITER);
		resourceId  = getToken(tokens); resourceType = getToken(tokens);	
		resourceName = getToken(tokens);	
		StringTokenizer nameToken = new StringTokenizer(resourceName,"@"); // need for update resource
		resourceName =  getToken(nameToken);
		newResourceName =  getToken(nameToken);
		if (resourceId!=null && resourceId.contains("?")) {         // need for update resource by id & type
			resource.setResourceId(m_resourceIds.get(resourceName));
		}
		if (newResourceName!=null) {
			resourceName = newResourceName;
			m_resourceIds.put(newResourceName, resource.getResourceId());
		}
        resource.setResourceName(resourceName);
		resource.setResourceType(resourceType);
		System.out.println(" > ResourceId = "+ resource.getResourceId()+" ; Resource Name = "+ resourceName + " ; Type = "+ resourceType);
		if (tokens.hasMoreTokens()) {
			constructOperations(resource,tokens);	
		}
		updateResourcesRequest.getResources().add(resource);
		
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
			StringTokenizer tokens = new StringTokenizer(token,CONFIG_DELIMITER);
			resource.setResourceType(getToken(tokens));
			resource.setResourceName(getToken(tokens));	
			resource.setDescription(getToken(tokens));	
			System.out.println(" > Resource Type = "+ resource.getResourceType()
					+" ; Resource Name = "+ resource.getResourceName() + " ; Desc = "+ resource.getDescription());
			
			PolicyServiceTestHelper.getInstance().cleanUpResource(resource.getResourceName(),resource.getResourceType());
			if (tokens.hasMoreTokens()) {
				constructOperations(resource,tokens);	
			}
			resList.add(resource);
		}
	private static void constructOperations(Resource resource, StringTokenizer tokens) {
			Operation operation = new Operation();
		List<Operation> operationsList = resource.getOperation();
		StringTokenizer nameToken = null;
		while (tokens.hasMoreTokens()) {
			operation = new Operation();
			String opTokens = getToken(tokens);
			nameToken = new StringTokenizer(opTokens,"^");
			operation.setOperationName(getToken(nameToken));
			operation.setDescription(getToken(nameToken));
			operationsList.add(operation);
			System.out.println( "\t operation name = "+operation.getOperationName() +"; Description = "+operation.getDescription());
		}
	}
	private static void cleanUpResources() {
		for(Resource resource:m_resourceList) {
			PolicyServiceTestHelper.getInstance().cleanUpResource(resource.getResourceName(),resource.getResourceType());
		}
	}
}
