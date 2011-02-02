/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.subject;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectsResponse;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class CreateSubjectNegativeTests{

	private static final String CONFIG_DELIMITER = ",";
	private String testCaseName;
	private String inputData;
	private String expectedResult;
	private static final String s_PropFilePath = "CreateSubjectNegativeTests.properties";
	
	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}
	public CreateSubjectNegativeTests(String testCaseName,String inputData, String expectedResult) {
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
	public void testCreateSubjects() throws Exception {
		
		System.out.println("\n*** Test Scenario :  " + testCaseName + " *** ");
		
		CreateSubjectsRequest createSubjectsRequest = new CreateSubjectsRequest();
		List<Subject> subjList = createSubjectsRequest.getSubjects();
		StringTokenizer subjTokens = new StringTokenizer(inputData,";");
		
		while(subjTokens.hasMoreTokens()) {
			String token = subjTokens.nextToken();
			constructCreateSubjectRequest(subjList,token);
		}
		
		try {
				CreateSubjectsResponse response = new CreateSubjectsResponse();
				response = PolicyServiceTestHelper.getInstance().
				createSubjects(createSubjectsRequest);
				String errorMessage = response.getErrorMessage() != null ? response
							.getErrorMessage().getError().get(0).getMessage(): null;
				if (getExpectedAckValue().equalsIgnoreCase("success")) {
					assertEquals(errorMessage, AckValue.SUCCESS,response.getAck());
					assertNull(errorMessage, response.getErrorMessage());	
				} else {
					assertEquals(errorMessage, AckValue.FAILURE,response.getAck());
					assertNotNull(errorMessage, response.getErrorMessage());
					assertEquals(errorMessage, getExpectedErrorMessage(),errorMessage);
				}		
		
	
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		} finally {
			// clean up the subjects
			cleanUpSubjects(subjList);
		
		}
		System.out.println("*** Test Scenario : " + testCaseName + " completed successfully ***");
	}

	
	@SuppressWarnings("unchecked")
	public static Collection loadInputData() {
		Properties props = new Properties();
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		InputStream inputStream = CreateSubjectNegativeTests.class.getResourceAsStream(s_PropFilePath);
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
	
	private String getExpectedAckValue() {
		StringTokenizer tokens = new StringTokenizer(expectedResult,CONFIG_DELIMITER);
		return tokens.nextToken();
	}

	private String getExpectedErrorMessage() {
		String errorMessage = null;
		StringTokenizer tokens = new StringTokenizer(expectedResult,CONFIG_DELIMITER);
		List<String> fields = new ArrayList<String>();
		while(tokens.hasMoreTokens()){
			fields.add(tokens.nextToken());
		}
		if (fields.size() <=1 ) {
			return null;
		}
		errorMessage = fields.get(1).equals("null") ?  null : fields.get(1).trim();
		
		return errorMessage;
	}
	private String getToken(StringTokenizer tokenizer)
	{
		if(tokenizer.hasMoreTokens())
		{
			String token = tokenizer.nextToken().trim();
			return "null".equals(token) ? null : token;
		} 
		return null;
	}
	private void constructCreateSubjectRequest(List<Subject> subjList, String subjectsTokens) {
		String subjName = null;
		String subjType = null;
		String subjDesc = null;
		StringTokenizer subjTokens = new StringTokenizer(subjectsTokens,CONFIG_DELIMITER);
		subjType = getToken(subjTokens);	subjName = getToken(subjTokens); subjDesc = getToken(subjTokens);
		Subject subject =new Subject();	
		subject.setSubjectType(subjType);
		subject.setSubjectName(subjName);
		subject.setDescription(subjDesc);
		System.out.println(" > Subject Name = "+ subjName + " ; Type = "+ subjType + " ; Description = "+ subjDesc);
		PolicyServiceTestHelper.getInstance().cleanUpSubjects(subjType, subjName);
		subjList.add(subject);
		
	}
	
	private void cleanUpSubjects(List<Subject> subjList) {
		for(Subject subject:subjList) {
			PolicyServiceTestHelper.getInstance().cleanUpSubjects(subject.getSubjectName(), subject.getSubjectType());
		}
		
	}
}
