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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupQuery;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class FindSubjectGroupTests{

	private static final String CONFIG_DELIMITER = ",";
	private String testCaseName;
	private String inputData;
	private String expectedResult;
	private static String preRequisite;
	private static Map<String,Long> m_subjectGroupIds = new HashMap<String,Long>();
	private static List<SubjectGroup> m_subjGroupList;
	private static final String s_PropFilePath = "FindSubjectGroupTests.properties";
	
	@BeforeClass
	public static void setUp() throws Exception {
		if (preRequisite!=null) {
				createPreRequisiteData(preRequisite);
		}
	}
	
	
	@AfterClass
	public static void tearDown() throws Exception {
		cleanUpSubjectGroups();
	}
	

	public FindSubjectGroupTests(String testCaseName,String inputData, String expectedResult) {
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
	public void testFindSubjectGroups() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + testCaseName + " *** ");
    	FindSubjectGroupsRequest findSubjectGroupsRequest = new FindSubjectGroupsRequest();
		try {
			StringTokenizer subjTokens = new StringTokenizer(inputData,";");
			SubjectGroupQuery subjectGroupQuery = new  SubjectGroupQuery();
			while(subjTokens.hasMoreTokens()) {
				String token = subjTokens.nextToken();
				constructFindSubjectGroupRequest(subjectGroupQuery,token);
			}
			findSubjectGroupsRequest.setSubjectGroupQuery(subjectGroupQuery);
			FindSubjectGroupsResponse response = new FindSubjectGroupsResponse();
			response = PolicyServiceTestHelper.getInstance().findSubjectGroups(findSubjectGroupsRequest);
			String errorMessage = response.getErrorMessage() != null ? response
						.getErrorMessage().getError().get(0).getMessage() : null;
						
			StringTokenizer restokens = new StringTokenizer(expectedResult,CONFIG_DELIMITER);
			String expectedAckValue = getToken(restokens);
			String expectedErrorMessage = getToken(restokens);
			String numOfSubjGrps = getToken(restokens);
			int expectedTotalSubjGrps = 0;
			if (numOfSubjGrps!=null) {
				expectedTotalSubjGrps = Integer.parseInt(numOfSubjGrps);
			}
			String expectedSubjectName = getToken(restokens);
			System.out.println("\tExpected Results AckValue = "+expectedAckValue+" ; errorMessage = "
					+expectedErrorMessage+ " ; Number of SubjectGroups = "+expectedTotalSubjGrps+" ;Match SubjectGroup ="+expectedSubjectName);
						
			if (expectedAckValue != null
					&& expectedAckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				int actualTotalSubjGrps = 0;
				if (response.getSubjectGroups() != null
						&& response.getSubjectGroups().size() > 0) {
					actualTotalSubjGrps = response.getSubjectGroups().size();
				}
				assertTrue("TestCase ="+testCaseName + "; Mismatch in total Subject Groups  ",actualTotalSubjGrps >= expectedTotalSubjGrps);
				if (expectedSubjectName != null) {
					for (SubjectGroup subjGrp : response.getSubjectGroups()) {
						assertTrue(expectedSubjectName.contains(subjGrp
								.getSubjectGroupName()));
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
		InputStream inputStream = CreateSubjectGroupTests.class.getResourceAsStream(s_PropFilePath);
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
	
	private void constructFindSubjectGroupRequest(SubjectGroupQuery sbjectGroupQuery,String subjectGroupToken) throws Exception {
		String subjectGroupId = null;
		String subjGroupName = null;
		String subjGroupType = null;
		
		StringTokenizer subjGroupTokens = new StringTokenizer(subjectGroupToken,CONFIG_DELIMITER);
		subjectGroupId  = getToken(subjGroupTokens); subjGroupType = getToken(subjGroupTokens);	
		subjGroupName = getToken(subjGroupTokens);	
		
		SubjectGroupKey subjectGroupKey =new SubjectGroupKey();	
		if (subjectGroupId!=null && subjectGroupId.contains("?")) {
			subjectGroupKey.setSubjectGroupId(m_subjectGroupIds.get(subjGroupName));
		} else {
			subjectGroupKey.setSubjectGroupName(subjGroupName);
		}
		subjectGroupKey.setSubjectType(subjGroupType);
		sbjectGroupQuery.getSubjectGroupKey().add(subjectGroupKey);
		
		System.out.println(" > Subject Group Id = "+ subjectGroupKey.getSubjectGroupId()+" ; Subject Name = "+ subjGroupName + " ; Type = "+ subjGroupType);
		
	}
	
	private static void createPreRequisiteData(String preRequisite) throws Exception {
	
		CreateSubjectGroupsRequest createSubjectGroupsRequest = new CreateSubjectGroupsRequest();
		CreateSubjectGroupsResponse createSubjectGroupsResponse = new CreateSubjectGroupsResponse();
		List<SubjectGroup> subjGroupList = createSubjectGroupsRequest.getSubjectGroups();
		
		try {
			StringTokenizer subjTokens = new StringTokenizer(preRequisite,";");
			while(subjTokens.hasMoreTokens()) {
				String token = subjTokens.nextToken();
				constructCreateSubjectGroupsRequest(subjGroupList,token);
			}
			m_subjGroupList = subjGroupList; 
			createSubjectGroupsResponse =PolicyServiceTestHelper.getInstance().createSubjectGroups(createSubjectGroupsRequest);
			
			if (createSubjectGroupsResponse.getSubjectGroupIds()!=null) {
				List<Long> subjectGroupIds= createSubjectGroupsResponse.getSubjectGroupIds();
				int i=0;
				for ( Long id :subjectGroupIds) {
					m_subjectGroupIds.put(subjGroupList.get(i++).getSubjectGroupName(),id);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
		
	}

	private static void constructCreateSubjectGroupsRequest(
			List<SubjectGroup> subjGroupList, String token) throws Exception {
			SubjectGroup subjectGroup =  new SubjectGroup();
			
			StringTokenizer subjGroupTokens = new StringTokenizer(token,CONFIG_DELIMITER);
			subjectGroup.setSubjectType(getToken(subjGroupTokens));	
			subjectGroup.setSubjectGroupName(getToken(subjGroupTokens));
			subjectGroup.setDescription(getToken(subjGroupTokens));
			
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
			System.out.println(" > Subject Name = " + subject.getSubjectName()
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
	
	private static void cleanUpSubjectGroups() {
		for(SubjectGroup subjectGroup:m_subjGroupList) {
			PolicyServiceTestHelper.getInstance().cleanupSubjectGroup(subjectGroup.getSubjectGroupName(),subjectGroup.getSubjectType());
		}
		
	}

}
