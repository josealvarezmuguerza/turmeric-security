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
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CreateResourceTests {

	private static final String CONFIG_DELIMITER = ",";
	private String testCaseName;
	private String inputData;
	private String expectedResult;
	private static final String s_PropFilePath = "CreateResourceTests.properties";

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}

	public CreateResourceTests(String testCaseName, String inpurData,
			String expectedResult) {
		this.testCaseName = testCaseName;
		this.inputData = inpurData;
		this.expectedResult = expectedResult;
	}

	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data() {
		return loadInputData();
	}

	@SuppressWarnings("unchecked")
	public static Collection loadInputData() {
		Properties props = new Properties();
		List list = new ArrayList();
		List eachRowData = new ArrayList();
		InputStream inputStream = CreateResourceTests.class
				.getResourceAsStream(s_PropFilePath);
		try {
			props.load(inputStream);
			int len = props.size() / 3;
			for (int i = 0; i < len; i++) {
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

	@Test
	public void testCreateResources() throws Exception {
		System.out.println("\n*** Test Scenario :  " + testCaseName + " *** ");
		CreateResourcesRequest createResourcesRequest = new CreateResourcesRequest();
		CreateResourcesResponse createResourcesResponse = new CreateResourcesResponse();
		List<Resource> resourceList = createResourcesRequest.getResources();
		try {
			StringTokenizer resourcesTokens = new StringTokenizer(inputData,
					";");
			while (resourcesTokens.hasMoreTokens()) {
				constructCreateRequest(resourceList, resourcesTokens
						.nextToken());
			}
			createResourcesResponse = PolicyServiceTestHelper.getInstance()
					.createResources(createResourcesRequest);
			String errorMessage = createResourcesResponse.getErrorMessage() != null ? createResourcesResponse
					.getErrorMessage().getError().get(0).getMessage()
					: null;
			if (getExpectedAckValue().equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS,
						createResourcesResponse.getAck());
				assertNull(errorMessage, createResourcesResponse
						.getErrorMessage());
			} else {
				assertEquals(errorMessage, AckValue.FAILURE,
						createResourcesResponse.getAck());
				assertNotNull(errorMessage, createResourcesResponse
						.getErrorMessage());
				assertEquals(errorMessage, getExpectedErrorMessage(),
						errorMessage);
			}

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		} finally {
			// clean up the resource
			cleanUpResources(resourceList);

		}
		System.out.println("*** Test Scenario : " + testCaseName
				+ " completed successfully ***");
	}

	private String getExpectedAckValue() {
		StringTokenizer tokens = new StringTokenizer(expectedResult,
				CONFIG_DELIMITER);
		return tokens.nextToken();
	}

	private String getExpectedErrorMessage() {
		String errorMessage = null;
		StringTokenizer tokens = new StringTokenizer(expectedResult,
				CONFIG_DELIMITER);
		List<String> fields = new ArrayList<String>();
		while (tokens.hasMoreTokens()) {
			fields.add(tokens.nextToken());
		}
		if (fields.size() <= 1) {
			return null;
		}

		errorMessage = fields.get(1).equals("null") ? null : fields.get(1);

		return errorMessage;
	}

	private String getToken(StringTokenizer tokenizer) {
		if (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			return "null".equals(token) ? null : token;
		}
		return null;
	}

	private void constructCreateRequest(List<Resource> resourceList,
			String resourceData) {
		String resourceName = null;
		String resourceType = null;
		String resourceDesc = null;
		StringTokenizer resourceTokens = new StringTokenizer(resourceData,
				CONFIG_DELIMITER);
		resourceType = getToken(resourceTokens);
		resourceName = getToken(resourceTokens);
		resourceDesc = getToken(resourceTokens);

		Resource resource = new Resource();
		resource.setResourceType(resourceType);
		resource.setResourceName(resourceName);
		resource.setDescription(resourceDesc);

		System.out.println("\n > Resource Name = " + resourceName
				+ " ; Type = " + resourceType + " ; Description = "
				+ resourceDesc);

		PolicyServiceTestHelper.getInstance().cleanUpResource(
				resource.getResourceName(), resource.getResourceType());
		if (resourceTokens.countTokens() > 0) {
			List<Operation> operations = resource.getOperation();
			String ops = resourceTokens.nextToken();
			StringTokenizer opTokens = new StringTokenizer(ops, "&");
			Operation operation = new Operation();
			String tempOp = null;
			while (opTokens.hasMoreTokens()) {
				operation = new Operation();
				tempOp = opTokens.nextToken();
				StringTokenizer opDescTokens = new StringTokenizer(tempOp, ":");
				// if description is set
				if (opDescTokens.countTokens() > 1) {
					operation.setOperationName(getToken(opDescTokens));
					operation.setDescription(getToken(opDescTokens));
				} else {
					operation.setOperationName(tempOp);
				}
				operations.add(operation);
				System.out.print("\n\t Operation name = "
						+ operation.getOperationName() + " , "
						+ " Desctiption = " + operation.getDescription() + " ");
			}
		}
		resourceList.add(resource);

	}

	private void cleanUpResources(List<Resource> resourceList) {
		for (Resource resource : resourceList) {
			PolicyServiceTestHelper.getInstance().cleanUpResource(
					resource.getResourceName(), resource.getResourceType());
		}

	}
}
