/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.subjectgroup;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class CreateSubjectGroupTests{

	private static final String CONFIG_DELIMITER = ",";
	private String testCaseName;
	private String inputData;
	private String expectedResult;
	private static final String s_PropFilePath = "CreateSubjectGroupTests.properties";
	
	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}
	public CreateSubjectGroupTests(String testCaseName,String inputData, String expectedResult) {
		this.testCaseName =testCaseName;
		this.inputData = inputData;
		this.expectedResult = expectedResult;
	}
	
	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data(){
		return loadInputData();
	}
	
    @Test
	public void createSubjectGroups() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + testCaseName + " *** ");
		CreateSubjectGroupsRequest createSubjectGroupsRequest = new CreateSubjectGroupsRequest();
		List<SubjectGroup> subjGroupList = createSubjectGroupsRequest.getSubjectGroups();
		try {
			StringTokenizer subjTokens = new StringTokenizer(inputData, ";");
			while (subjTokens.hasMoreTokens()) {
				String token = subjTokens.nextToken();
				constructCreateSubjectGroupsRequest(subjGroupList, token);
			}
			CreateSubjectGroupsResponse response = new CreateSubjectGroupsResponse();
			response = PolicyServiceTestHelper.getInstance()
					.createSubjectGroups(createSubjectGroupsRequest);
			String errorMessage = response.getErrorMessage() != null ? response
					.getErrorMessage().getError().get(0).getMessage() : null;
			if (getExpectedAckValue().equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				assertNotNull(response.getSubjectGroupIds());
			} else {
				assertEquals(errorMessage, AckValue.FAILURE, response.getAck());
				assertNotNull(errorMessage, response.getErrorMessage());
				assertEquals(errorMessage, getExpectedErrorMessage(),
						errorMessage);
			}
	
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		} finally {
			// clean up the subjects
			cleanUpSubjectGroups(subjGroupList);
		
		}
		System.out.println("*** Test Scenario : " + testCaseName + " completed successfully ***");
	}

	
	@SuppressWarnings("unchecked")
	public static Collection loadInputData() {
		Properties props = new Properties();
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		InputStream inputStream = CreateSubjectGroupTests.class.getResourceAsStream(s_PropFilePath);
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
	private static String getToken(StringTokenizer tokenizer)
	{
		if(tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken().trim();
			return "null".equals(token) ? null : token;
		} 
		return null;
	}
	
	private static void constructCreateSubjectGroupsRequest(
			List<SubjectGroup> subjGroupList, String token) throws Exception {
			SubjectGroup subjectGroup =  new SubjectGroup();
			
			StringTokenizer subjGroupTokens = new StringTokenizer(token,CONFIG_DELIMITER);
			subjectGroup.setSubjectType(getToken(subjGroupTokens));	
			subjectGroup.setSubjectGroupName(getToken(subjGroupTokens));
			subjectGroup.setDescription(getToken(subjGroupTokens));
			System.out.println(" >> SubjectGroup Name = " + subjectGroup.getSubjectGroupName()
					+ " ; Type = " + subjectGroup.getSubjectType()	+ " ; Description = " + subjectGroup.getDescription());
			String subjectsList= getToken(subjGroupTokens);
			List<Subject> subjList = subjectGroup.getSubject();
			
			constructCreateSubjectRequest(subjList,subjectsList);
			createSubjectsIfDoesntExist(subjList);
			subjGroupList.add(subjectGroup);
						
	}
	private static void constructCreateSubjectRequest(List<Subject> subjList, String subjectsTokens) {
		StringTokenizer subjListTokens = new StringTokenizer(subjectsTokens,"&");
		Subject subject =new Subject();
		while(subjListTokens.hasMoreTokens()) {
			String token = subjListTokens.nextToken();
			StringTokenizer subjTokens = new StringTokenizer(token,":");
			subject = new Subject();
			subject.setSubjectType(getToken(subjTokens));
			subject.setSubjectName(getToken(subjTokens));
			subject.setDescription(getToken(subjTokens));
			subjList.add(subject);
			System.out.println(" \t Subject Name = " + subject.getSubjectName()
					+ " ; Type = " + subject.getSubjectType()	+ " ; Description = " + subject.getDescription());
			subjList.add(subject);
		}
		
	}
	private static void createSubjectsIfDoesntExist(List<Subject> subjList) throws Exception {
		Long externalSubjectId = null;
		for(Subject subject:subjList) {
			Long subjectId = PolicyServiceTestHelper.getInstance().findSubjects(subject.getSubjectType(),subject.getSubjectName());
			if (subjectId==-1 ) {
				PolicyServiceTestHelper.getInstance().createSubjects(subject.getSubjectType(),externalSubjectId,subject.getSubjectName());
			} 
		}
	}
	
	private void cleanUpSubjectGroups(List<SubjectGroup> subjGroupList) {
		for(SubjectGroup subjectGroup:subjGroupList) {
			PolicyServiceTestHelper.getInstance().cleanupSubjectGroup(subjectGroup.getSubjectGroupName(),subjectGroup.getSubjectType());
		}
		
	}
}
