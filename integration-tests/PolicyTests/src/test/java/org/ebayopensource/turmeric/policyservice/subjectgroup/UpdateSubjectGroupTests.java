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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupQuery;
import org.ebayopensource.turmeric.security.v1.services.UpdateMode;
import org.ebayopensource.turmeric.security.v1.services.UpdateSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.UpdateSubjectGroupsResponse;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class UpdateSubjectGroupTests{

	private static final String CONFIG_DELIMITER = ":";
	private String testCaseName;
	private String inputData;
	private String updateMode;
	private String expectedResult;
	private static String preRequisite;
	private static Map<String,Long> m_subjectGroupIds = new HashMap<String,Long>();
	private static List<SubjectGroup> m_subjGroupList;
	private static final String s_PropFilePath = "UpdateSubjectGroupTests.properties";
	
	@BeforeClass
	public static void setUp() throws Exception {
		if (preRequisite!=null) {
			System.out.println(" *** Create Pre-Requisite data  *****"); 
			createPreRequisiteData(preRequisite);
			System.out.println(" *** Create Pre-Requisite data has been completed successfully ***");
			
		}
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		cleanUpSubjectGroups();
	}
	
	public UpdateSubjectGroupTests(String testCaseName,String inputData, String updateMode,String expectedResult) {
		this.testCaseName = testCaseName;
		this.inputData = inputData;
		this.updateMode = updateMode;
		this.expectedResult = expectedResult;
	}
	
	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data(){
		return loadInputData();
	}
	
    @Test
	public void updateSubjectGroups() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + testCaseName + " *** ");
    	UpdateSubjectGroupsRequest updateSubjectGroupsRequest = new UpdateSubjectGroupsRequest();
    	UpdateSubjectGroupsResponse response = new UpdateSubjectGroupsResponse();
		try {
			StringTokenizer subjTokens = new StringTokenizer(inputData,";");
			while(subjTokens.hasMoreTokens()) {
				String token = subjTokens.nextToken();
				constructUpdateSubjectGroupsRequest(updateSubjectGroupsRequest,token);
			}
			
			if (updateMode!=null && updateMode.equalsIgnoreCase("REPLACE")) {
				updateSubjectGroupsRequest.setUpdateMode(UpdateMode.REPLACE);
			} else if (updateMode!=null && updateMode.equalsIgnoreCase("DELETE")) {
				updateSubjectGroupsRequest.setUpdateMode(UpdateMode.DELETE);
			} else {
				updateSubjectGroupsRequest.setUpdateMode(UpdateMode.UPDATE);
			}
			
			response =  PolicyServiceTestHelper.getInstance().updateSubjectGroups(updateSubjectGroupsRequest);
			String errorMessage = response.getErrorMessage() != null ? response
					.getErrorMessage().getError().get(0).getMessage() : null;
					
			System.out.println("\tActual AckValue = "	+ response.getAck()+ "; errorMessage = "+errorMessage);
			StringTokenizer expectedResTokens = new StringTokenizer(expectedResult,"|");
			
			String e_AckValue = getToken(expectedResTokens);
			String e_ErrorMessage = getToken(expectedResTokens);
			if (e_AckValue != null
					&& e_AckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				FindSubjectGroupsRequest findSubjectGroupsRequest = new FindSubjectGroupsRequest();
				SubjectGroupQuery subjectGroupQuery = new  SubjectGroupQuery();
				findSubjectGroupsRequest.setSubjectGroupQuery(subjectGroupQuery);
				FindSubjectGroupsResponse findResponse = new FindSubjectGroupsResponse();
				constructFindSubjectGroupRequest(findSubjectGroupsRequest,updateSubjectGroupsRequest.getSubjectGroups());
				findResponse = PolicyServiceTestHelper.getInstance().findSubjectGroups(findSubjectGroupsRequest);
				errorMessage= findResponse.getErrorMessage() != null ? findResponse
						.getErrorMessage().getError().get(0).getMessage() : null;
				assertEquals(errorMessage, AckValue.SUCCESS, findResponse.getAck());
				
				String e_numOfSubjectGroups = getToken(expectedResTokens);
				String e_matchSubjectGroupInfo = getToken(expectedResTokens);
				
				int e_TotalSubjectGroups = 0;
				int a_TotalSubjectGroups = 0;
				List<SubjectGroup> subjectGroupList= null;
				e_TotalSubjectGroups =Integer.parseInt(e_numOfSubjectGroups);
						
				if (findResponse.getSubjectGroups() != null
						&& findResponse.getSubjectGroups().size()> 0) {
					subjectGroupList = findResponse.getSubjectGroups();
					a_TotalSubjectGroups = subjectGroupList.size();
				}
				assertTrue("TestCase ="+testCaseName + "; Mismatch in total of subjectgroups ",a_TotalSubjectGroups >= e_TotalSubjectGroups);
				int counter = 0;
				StringTokenizer sgTokens = new StringTokenizer(e_matchSubjectGroupInfo,";");
				SubjectGroup subjectGroup = new  SubjectGroup();
				while (sgTokens.hasMoreTokens()) {
					String sgToken = getToken(sgTokens);
					subjectGroup = subjectGroupList.get(counter++);
					verifyUpdateSubjectGroupResponse(subjectGroup,sgToken);
				}
			
			} else {
				assertEquals(errorMessage, AckValue.FAILURE, response.getAck());
				assertNotNull(errorMessage, response.getErrorMessage());
				assertEquals(errorMessage, e_ErrorMessage, errorMessage);
			}

		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
		System.out.println("*** Test Scenario : " + testCaseName + " completed successfully ***");
	}
    
    private void verifyUpdateSubjectGroupResponse(SubjectGroup subjectGroup,
			String sgToken) {
		StringTokenizer tokens = new StringTokenizer(sgToken,":");
		String sgName = getToken(tokens);
		if (sgName!=null) {
			StringTokenizer nameDesc = new StringTokenizer(sgToken,",");
			sgName= getToken(nameDesc);
			String desc = getToken(nameDesc);
			assertEquals(sgName,subjectGroup.getSubjectGroupName());
			if (desc!=null) {
				assertEquals(desc,subjectGroup.getDescription());
			}
		}
		String totalSubjs =  getToken(tokens);
		int expectedTotalOps = 0;
		if (totalSubjs != null) {
			expectedTotalOps = Integer.parseInt(totalSubjs);
		}
		if (expectedTotalOps == 0) {
			
			assertTrue("Exptect no subjects, Subject Group: " + subjectGroup.getSubject() + " size: " + subjectGroup.getSubject().size(),  subjectGroup.getSubject()==null || subjectGroup.getSubject().size()==0);
			
			
		} else {
			assertNotNull(subjectGroup.getSubject());
			assertEquals(expectedTotalOps,subjectGroup.getSubject().size());
			for (int i = 0; i < expectedTotalOps; i++ ) {
				String expectedSubjName  = getToken(tokens);
				boolean exists = false;
				for (Subject subj : subjectGroup.getSubject() ) {
					if (subj.getSubjectName().equals(expectedSubjName)) { 
						exists = true;
						break;
					}
				}
				assertTrue(expectedSubjName+" doesn't exist", exists);	
			}
		}
				
	}
	
	private void constructFindSubjectGroupRequest(
			FindSubjectGroupsRequest findSubjectGroupsRequest,
			List<SubjectGroup> subjectGroups) {
		SubjectGroupQuery subjectGroupQuery = findSubjectGroupsRequest.getSubjectGroupQuery();
		SubjectGroupKey subjectGroupKey =new SubjectGroupKey();
		
		for (SubjectGroup sg:subjectGroups) {
			subjectGroupKey =new SubjectGroupKey();
			subjectGroupKey.setSubjectGroupName(sg.getSubjectGroupName());
			subjectGroupKey.setSubjectType(sg.getSubjectType());
			subjectGroupQuery.getSubjectGroupKey().add(subjectGroupKey);
		}
	}

	@SuppressWarnings("unchecked")
	public static Collection loadInputData() {
		Properties props = new Properties();
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		Pattern pattern =Pattern.compile("testcase(\\d*).name");
		try {
			InputStream inputStream = CreateSubjectGroupTests.class.getResourceAsStream(s_PropFilePath);
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
				String input = "testcase" + i + ".request";
				String updatemode = "testcase" + i + ".updatemode";
				String expected = "testcase" + i + ".response";
				eachRowData.add(props.getProperty(testName));
				eachRowData.add(props.getProperty(input));
				eachRowData.add(props.getProperty(updatemode));
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
	
	private void constructUpdateSubjectGroupsRequest(UpdateSubjectGroupsRequest updateSubjectGroupsRequest,String subjectGroupToken) throws Exception {
		
		String subjectGroupId = null;
		String subjGroupName = null;
		String newSubjGroupName = null;
		String subjGroupType = null;
		String description = null;
		String newDescription = null;
		SubjectGroup subjectGroup = new  SubjectGroup();
		StringTokenizer sgTokens = new StringTokenizer(subjectGroupToken,"|");
		String subjectGroupDetails = getToken(sgTokens);
		String subjectDetails = getToken(sgTokens);
		
		StringTokenizer subjGroupTokens = new StringTokenizer(subjectGroupDetails,CONFIG_DELIMITER);
		subjectGroupId  = getToken(subjGroupTokens); subjGroupType = getToken(subjGroupTokens);	
		subjGroupName = getToken(subjGroupTokens);	
		description =  getToken(subjGroupTokens);
		
		StringTokenizer nameToken = new StringTokenizer(subjGroupName,"@"); // check if new name is provided
		subjGroupName =  getToken(nameToken);
		newSubjGroupName =  getToken(nameToken);
		if (description!=null) {
			nameToken = new StringTokenizer(description,"@"); // check if new description  is provided
			newDescription =  getToken(nameToken);
		}
		
		if (subjectGroupId!=null && subjectGroupId.contains("?")) {
			long id = m_subjectGroupIds.get(subjGroupName);
			subjectGroup = PolicyServiceTestHelper.getInstance().mapIncludedSubjectGroup(id);
		}
		if (newSubjGroupName!=null) {
            m_subjectGroupIds.put(newSubjGroupName, m_subjectGroupIds.get(subjGroupName));
			subjGroupName =  newSubjGroupName;
			subjectGroup.setSubjectGroupName(subjGroupName);
		} else {
            subjectGroup.setSubjectGroupName(subjGroupName);
        }
		if (newDescription!=null) {
			description =  newDescription;
		}
		subjectGroup.setDescription(description);
		subjectGroup.setSubjectType(subjGroupType);
		
		List<Subject> subjList = subjectGroup.getSubject();
		
		extractSubjects(subjList, subjectDetails);
		updateSubjectGroupsRequest.getSubjectGroups().add(subjectGroup);
		System.out.println("  Subject Group Name = "+ subjGroupName + " ; Type = "+ subjGroupType  + " ; Description = "+ description);
		
	}
	
	private static void createPreRequisiteData(String preRequisite) {
	
	
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
			StringTokenizer sgTokens = new StringTokenizer(token,"|");
			String subjectGroupDetails = getToken(sgTokens);
			String subjectDetails = getToken(sgTokens);
			
			StringTokenizer subjGroupTokens = new StringTokenizer(subjectGroupDetails,CONFIG_DELIMITER);
			subjectGroup.setSubjectType(getToken(subjGroupTokens));	
			subjectGroup.setSubjectGroupName(getToken(subjGroupTokens));
			subjectGroup.setDescription(getToken(subjGroupTokens));
			// clean up if subjectgroup already exists
			PolicyServiceTestHelper.getInstance().cleanupSubjectGroup(subjectGroup.getSubjectGroupName(),subjectGroup.getSubjectType());
			System.out.println(" > SubjectGroup Name = " + subjectGroup.getSubjectGroupName()
					+ " ; Type = " + subjectGroup.getSubjectGroupName()	+ " ; Description = " + subjectGroup.getDescription());			
			List<Subject> subjList = subjectGroup.getSubject();
			
			extractSubjects(subjList,subjectDetails);
			createSubjectsIfDoesntExist(subjList);
			subjGroupList.add(subjectGroup);
						
	}
	private static void extractSubjects(List<Subject> subjList, String subjectDetails) {
		if (subjectDetails == null) return;
		StringTokenizer subjListTokens = new StringTokenizer(subjectDetails,CONFIG_DELIMITER);
		
		Subject subject =new Subject();
		while(subjListTokens.hasMoreTokens()) {
			subject = new Subject();
			subject.setSubjectType(getToken(subjListTokens));
			subject.setSubjectName(getToken(subjListTokens));
			subject.setDescription(getToken(subjListTokens));
			subjList.add(subject);
			System.out.println("\t Subject Name = " + subject.getSubjectName()
					+ " ; Type = " + subject.getSubjectType()	+ " ; Description = " + subject.getDescription());
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
