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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectsResponse;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectsRequest;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectsResponse;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectQuery;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class FindSubjectTests{

	private static final String CONFIG_DELIMITER = ",";
	private String testCaseName;
	private String inputData;
	private String expectedResult;
	private static String preRequisite;
	private static Map<String,Long> m_subjectIds = new HashMap<String,Long>();
	private static List<Subject> m_subjList;
	private static final String s_PropFilePath = "FindSubjectTests.properties";
	
	@BeforeClass
	public static void setUp() throws Exception {
		if (preRequisite!=null) {
				createPreRequisiteData(preRequisite);
		}
	}
		
	@AfterClass
	public static void tearDown() throws Exception {
		cleanUpSubjects();
	}
	
	public FindSubjectTests(String testCaseName,String inputData, String expectedResult) {
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
	public void findSubjects() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + testCaseName + " *** ");
    	FindSubjectsRequest findSubjectsRequest = new FindSubjectsRequest();
    	SubjectQuery subjectQuery = new SubjectQuery();
		try {
			StringTokenizer subjTokens = new StringTokenizer(inputData,";");
			while(subjTokens.hasMoreTokens()) {
				String token = subjTokens.nextToken();
				constructFindSubjectsRequest(subjectQuery,token);
			}
			findSubjectsRequest.setSubjectQuery(subjectQuery);
			FindSubjectsResponse response = new FindSubjectsResponse();
			response = PolicyServiceTestHelper.getInstance().findSubjects(
					findSubjectsRequest);
			String errorMessage = response.getErrorMessage() != null ? response
					.getErrorMessage().getError().get(0).getMessage() : null;
			StringTokenizer restokens = new StringTokenizer(expectedResult,
					CONFIG_DELIMITER);
			String expectedAckValue = getToken(restokens);
			String expectedErrorMessage = getToken(restokens);
			String numOfSubjects = getToken(restokens);
			int expectedTotalSubjs = 0;
			if (numOfSubjects != null) {
				expectedTotalSubjs = Integer.parseInt(numOfSubjects);
			}
			String expectedSubjectName = getToken(restokens);
			System.out.println("\tExpected Results AckValue = "+ expectedAckValue + " ; errorMessage = "+ expectedErrorMessage + 
						" ; Number of Subjects = "	+ expectedTotalSubjs + " ;Match Subject ="+ expectedSubjectName);
			if (expectedAckValue != null
					&& expectedAckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				int actualTotalSubjs = 0;
				if (response.getSubjects() != null
						&& response.getSubjects().size() > 0) {
					actualTotalSubjs = response.getSubjects().size();
				}
				assertTrue("TestCase ="+testCaseName + "; Mismatch in total subjects ",actualTotalSubjs >= expectedTotalSubjs);
				if (expectedSubjectName != null) {
					for (Subject subj : response.getSubjects()) {
						assertTrue(expectedSubjectName.contains(subj
								.getSubjectName()));
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
		InputStream inputStream = FindSubjectTests.class.getResourceAsStream(s_PropFilePath);
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
	
	private void constructFindSubjectsRequest(SubjectQuery subjectQuery,String subjectToken) throws Exception {
		String subjectId = null;
		String subjName = null;
		String subjType = null;
		
		StringTokenizer tokens = new StringTokenizer(subjectToken,CONFIG_DELIMITER);
		subjectId  = getToken(tokens); subjType = getToken(tokens);	
		subjName = getToken(tokens);	
		
		SubjectKey subjectKey =new SubjectKey();	
		if (subjectId!=null && subjectId.contains("?")) {
			subjectKey.setSubjectId(m_subjectIds.get(subjName));
		} else {
			subjectKey.setSubjectName(subjName);
		}
		subjectKey.setSubjectType(subjType);
		subjectQuery.getSubjectKey().add(subjectKey);
		System.out.println(" > SubjectId = "+ subjectKey.getSubjectId()+" ; Subject Name = "+ subjName + " ; Type = "+ subjType);
		
	}
	
	private static void createPreRequisiteData(String preRequisite) {
	
		CreateSubjectsRequest createSubjectsRequest = new CreateSubjectsRequest();
		CreateSubjectsResponse createSubjectsResponse = new CreateSubjectsResponse();
		List<Subject> subjList = createSubjectsRequest.getSubjects();
		
		try {
			StringTokenizer subjTokens = new StringTokenizer(preRequisite,";");
			while(subjTokens.hasMoreTokens()) {
				String token = subjTokens.nextToken();
				constructCreateSubjectsRequest(subjList,token);
			}
			m_subjList = subjList; 
			createSubjectsResponse =PolicyServiceTestHelper.getInstance().createSubjects(createSubjectsRequest);
			
			if (createSubjectsResponse.getSubjectIds()!=null) {
				List<Long> subjectIds= createSubjectsResponse.getSubjectIds();
				int i=0;
				for ( Long id :subjectIds) {
					m_subjectIds.put(subjList.get(i++).getSubjectName(),id);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
	}
	private static void constructCreateSubjectsRequest(
			List<Subject> subjList, String token) {
			Subject subject =  new Subject();
			StringTokenizer subjTokens = new StringTokenizer(token,CONFIG_DELIMITER);
			subject.setSubjectType(getToken(subjTokens));
			subject.setSubjectName(getToken(subjTokens));
			subject.setDescription(getToken(subjTokens));
			// cleanup subjects if exist
			PolicyServiceTestHelper.getInstance().cleanUpSubjects(subject.getSubjectType(),subject.getSubjectName());
			subjList.add(subject);
			System.out.println(" > Subject Name = " + subject.getSubjectName()
						+ " ; Type = " + subject.getSubjectType()	+ " ; Description = " + subject.getDescription());
			
	}
	
	private static void cleanUpSubjects() {
		for(Subject subject:m_subjList) {
			PolicyServiceTestHelper.getInstance().cleanUpSubjects(subject.getSubjectType(),subject.getSubjectName());
		}
	}
}
