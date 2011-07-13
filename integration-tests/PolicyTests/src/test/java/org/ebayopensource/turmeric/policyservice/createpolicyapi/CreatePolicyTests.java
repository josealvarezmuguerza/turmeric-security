/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.createpolicyapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.EffectType;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Resources;
import org.ebayopensource.turmeric.security.v1.services.Rule;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.Subjects;
import org.ebayopensource.turmeric.security.v1.services.Target;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CreatePolicyTests{

	private static final String CONFIG_DELIMITER = ":";
	private String m_testCaseName;
	private String m_policyInfo;
	private String m_resources;
	private String m_globalSubjectDomainsList;
	private String m_includeSubjects;
	private String m_includeSubjectGroups;
	private String m_excludeSubjects;
	private String m_excludeSubjectGroups;
	private String m_policyEffect;
	private String m_expectedResult;
	private static Properties props = new Properties();
	private static Map<String,Long> m_subjectIds = new HashMap<String,Long>();
	private static Map<String,Long> m_subjectGroupIds = new HashMap<String,Long>();
	private static Map<String,Long> m_resourceIds = new HashMap<String,Long>();
	private static List<Resource> m_resourceList = new ArrayList<Resource>();
	private static List<SubjectGroup> m_subjGroupList = new ArrayList<SubjectGroup>();
	private static List<Subject> m_subjList = new ArrayList<Subject>();
	private static final String s_PropFilePath = "org/ebayopensource/turmeric/policyservice/createpolicyapi/CreatePolicyTests.properties";
	@BeforeClass
	public static void setUp() throws Exception {
		System.out.println(" *** Create Pre-Requisite data ***");
		createPreRequisiteData();
		System.out.println(" *** Create Pre-Requisite data has been completed successfully ***");
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		cleanUpResources();
		cleanUpSubjectGroups();
		cleanUpSubjects();
	}
	public CreatePolicyTests(String testCaseName, String policyInfo,
			String resources, String globalSubjectDomainsList,
			String includeSubjects, String excludeSubjects,
			String includeSubjectGroups, String excludeSubjectGroups,
			String policyEffect, String expectedResult) {
		this.m_testCaseName = testCaseName;
		this.m_policyInfo = policyInfo;
		this.m_resources = resources;
		this.m_globalSubjectDomainsList = globalSubjectDomainsList;
		this.m_includeSubjects = includeSubjects;
		this.m_excludeSubjects = excludeSubjects;
		this.m_includeSubjectGroups = includeSubjectGroups;
		this.m_excludeSubjectGroups = excludeSubjectGroups;
		this.m_policyEffect = policyEffect;
		this.m_expectedResult = expectedResult;
	}

	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data(){
		return loadCreatePolicyData();
	}
	public InputStream getInputStream(String a) {
		InputStream is = getClass().getResourceAsStream( a );
	
		if(is ==null){
			is =ClassLoader.getSystemResourceAsStream(a);
		}
		if(is ==null){
			URL resource = Thread.currentThread().getContextClassLoader().getSystemClassLoader().getResource(a);    
		    try {
				is = new FileInputStream(new File(resource.toExternalForm()));
			} catch (Exception e) {
			}
		}
		
		return is;
	}
    @Test
	public void createPolicy() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + m_testCaseName + " *** ");
		CreatePolicyRequest createPolicyRequest = new CreatePolicyRequest();
		CreatePolicyResponse response = new CreatePolicyResponse();
	

		try {
			createPolicyRequest = constructCreatePolicyRequest();
			response = PolicyServiceTestHelper.getInstance().createPolicy(createPolicyRequest);
			String errorMessage = response.getErrorMessage() != null ? response
					.getErrorMessage().getError().get(0).getMessage() : null;
			
			StringTokenizer resTokens = new StringTokenizer(m_expectedResult,"|");
			String expectedAckValue = getToken(resTokens);
			String expectedErrorMessage = getToken(resTokens);
					
			if (expectedAckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				//assertNotNull();
			} else {
				assertEquals(errorMessage, AckValue.FAILURE, response.getAck());
				assertNotNull(errorMessage, response.getErrorMessage());
				assertEquals(errorMessage, expectedErrorMessage,	errorMessage);
			}
	
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		} finally {
		   cleanUpPolicy();
		}
		System.out.println("*** Test Scenario : " + m_testCaseName + " completed successfully ***");
	}

	private static void createPreRequisiteData() {
		String subjects = props.getProperty("testcase.prerequisite.subjects");
		String subjectsGroups = props.getProperty("testcase.prerequisite.subjectgroups");
		String resources = props.getProperty("testcase.prerequisite.resources");
		createSubjects(subjects);
		createSubjectGroups(subjectsGroups);
		createResources(resources);
	}
		
	private static void createResources(String resources) {
		CreateResourcesRequest createResourcesRequest = new CreateResourcesRequest();
		CreateResourcesResponse createResourcesResponse = new CreateResourcesResponse();
		List<Resource> resourceList = createResourcesRequest.getResources();
		try {
			StringTokenizer resTokens = new StringTokenizer(resources,";");
			while(resTokens.hasMoreTokens()) {
				String token = resTokens.nextToken();
				constructCreateResourceRequest(resourceList,token);
			}
			m_resourceList = resourceList; 
			createResourcesResponse =PolicyServiceTestHelper.getInstance().createResources(createResourcesRequest);
			if (createResourcesResponse.getResourceIds()!=null) {
				List<Long> resourceIds= createResourcesResponse.getResourceIds();
				int i=0;
				for ( Long id :resourceIds) m_resourceIds.put(resourceList.get(i++).getResourceName(),id);
				
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
			List<Operation> operations = resource.getOperation();
			Operation operation = new Operation();
			while (resTokens.hasMoreTokens()) {
				String opsToken = getToken(resTokens);
				if (opsToken!=null) {
					operation = new Operation();
					operation.setOperationName(opsToken);
				}
				operations.add(operation);
			}
			resList.add(resource);
		}

	private static void createSubjectGroups(String subjectsGroups) {
		
		CreateSubjectGroupsRequest createSubjectGroupsRequest = new CreateSubjectGroupsRequest();
		CreateSubjectGroupsResponse createSubjectGroupsResponse = new CreateSubjectGroupsResponse();
		List<SubjectGroup> subjGroupList = createSubjectGroupsRequest.getSubjectGroups();
		
		try {
			StringTokenizer subjTokens = new StringTokenizer(subjectsGroups,";");
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
			List<SubjectGroup> subjGroupList, String token) {
			SubjectGroup subjectGroup =  new SubjectGroup();
			StringTokenizer subjGroupTokens = new StringTokenizer(token,CONFIG_DELIMITER);
			subjectGroup.setSubjectType(getToken(subjGroupTokens));	
			subjectGroup.setSubjectGroupName(getToken(subjGroupTokens));
			subjectGroup.setDescription(getToken(subjGroupTokens));
			System.out.println(" > SubjectGroup Name = " + subjectGroup.getSubjectGroupName()
					+ " ; Type = " + subjectGroup.getSubjectGroupName()	);
			// clean up if subjectgroup already exists
			PolicyServiceTestHelper.getInstance().cleanupSubjectGroup(subjectGroup.getSubjectGroupName(),subjectGroup.getSubjectType());
			List<Subject> subjList = subjectGroup.getSubject();
			Subject subject =new Subject();
			while (subjGroupTokens.hasMoreTokens()) {
				String subjectName= getToken(subjGroupTokens);
				subject = new Subject();
				subject.setSubjectType(subjectGroup.getSubjectType());
				subject.setSubjectName(subjectName);
				subjList.add(subject);
			}
			subjGroupList.add(subjectGroup);
						
	}
	private static void createSubjects(String subjects) {
		try {
			if (subjects!=null) {
				StringTokenizer subjsList = new StringTokenizer(subjects,";");
				while(subjsList.hasMoreTokens()) {
					String token = subjsList.nextToken();
					StringTokenizer subjTokens = new StringTokenizer(token,CONFIG_DELIMITER);
					Subject subject = new Subject();
					subject.setSubjectType(getToken(subjTokens));
					subject.setSubjectName(getToken(subjTokens));
					System.out.println(" > Subject Name = " + subject.getSubjectName()+ " ; Type = " + subject.getSubjectType());
					m_subjList.add(subject);
					long subjectId = PolicyServiceTestHelper.getInstance().findSubjects(subject.getSubjectType(), subject.getSubjectName());
					if (subjectId > 0)  m_subjectIds.put(subject.getSubjectName(),subjectId);
					 else {
						List<Long> subjIdList = PolicyServiceTestHelper.getInstance().createSubjects(subject.getSubjectType(), subject.getSubjectName());
						if (subjIdList!=null && subjIdList.size() >0)  	m_subjectIds.put(subject.getSubjectName(),subjIdList.get(0));
					 }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Collection loadCreatePolicyData() {
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		Pattern pattern =Pattern.compile("testcase(\\d*).name");
		int totalTests = 0;
		try {
			InputStream inputStream = CreatePolicyTests.class.getResourceAsStream(s_PropFilePath);
			if(inputStream==null){
		CreatePolicyTests c = new CreatePolicyTests(null, null, null, null, null, null, null, null, null, null);
             inputStream = c.getInputStream(s_PropFilePath);
			}

System.out.println("***********************************************************************inputStream" +inputStream);
			props.load(inputStream);
			Iterator it = props.keySet().iterator();
			while(it.hasNext()) {
				String key = it.next().toString();
				Matcher matcher = pattern.matcher(key);
				if (matcher.find()) 	totalTests++;
			}
			for (int i = 0; i < totalTests; i++) {
				eachRowData = new ArrayList();
				String testName = "testcase" + i + ".name";
				String policyinfo = "testcase" + i + ".request.policyInfo";
				String resources = "testcase" + i + ".request.resources";
				String globalsubjectdomainsList = "testcase" + i + ".request.globalSubjectDomainsList";
				String includeSubjects = "testcase" + i + ".request.includeSubjects";
				String excludeSubjects = "testcase" + i + ".request.excludeSubjects";
				String includeSubjectGroups = "testcase" + i + ".request.includeSubjectGroups";
				String excludeSubjectGroups = "testcase" + i + ".request.excludeSubjectGroups";
				String policyEffect = "testcase" + i + ".request.policyEffect";
				String expectedResult = "testcase" + i + ".response";
				eachRowData.add(props.getProperty(testName));
				eachRowData.add(props.getProperty(policyinfo));
				eachRowData.add(props.getProperty(resources));
				eachRowData.add(props.getProperty(globalsubjectdomainsList));
				eachRowData.add(props.getProperty(includeSubjects));
				eachRowData.add(props.getProperty(excludeSubjects));
				eachRowData.add(props.getProperty(includeSubjectGroups));
				eachRowData.add(props.getProperty(excludeSubjectGroups));
				eachRowData.add(props.getProperty(policyEffect));
				eachRowData.add(props.getProperty(expectedResult));
				
				list.add(eachRowData.toArray());
			}
		} catch (IOException e) {}
		
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
	
	
	private CreatePolicyRequest constructCreatePolicyRequest() throws Exception {
		CreatePolicyRequest request = new CreatePolicyRequest();
 		Policy policy = new Policy();
 		Target target = new Target();
 		if (m_policyInfo!=null) {
			StringTokenizer policyTokens = new StringTokenizer(m_policyInfo,CONFIG_DELIMITER);
			String policyType =getToken(policyTokens);
			String policyName= getToken(policyTokens);
			String policyDesc =getToken(policyTokens);
			policy.setPolicyName(policyName);
	 		policy.setPolicyType(policyType);
	 		policy.setDescription(policyDesc);
	 		PolicyServiceTestHelper.getInstance().cleanupPolicy(policyName, policyType);
			System.out.println("policyType = "+ policyType+ ", policyname = "+ policyName+" ,policyDesc = "+ policyDesc);
		}
		if (m_resources!=null) {
			StringTokenizer resList = new StringTokenizer(m_resources,";");
			Resources resources = new Resources();
			List<Resource> resourceList = resources.getResource();
			Resource resource = new Resource();
			while (resList.hasMoreTokens()) {
				resource = new Resource();
				String nextToken = resList.nextToken();
				StringTokenizer resourceTokens = new StringTokenizer(nextToken,CONFIG_DELIMITER);
				String resType =getToken(resourceTokens);
				String resName= getToken(resourceTokens);
				resource.setResourceName(resName);
				resource.setResourceType(resType);
				System.out.println("resourceType = "+ resType+ ", resourceName = "+ resName);
				while (resourceTokens.hasMoreElements()) {
					Operation operation = new Operation();
					operation.setOperationName(getToken(resourceTokens));
					resource.getOperation().add(operation);
					System.out.println("\t Operation name = "+operation.getOperationName());
				}
				resourceList.add(resource);
			}
			target.setResources(resources);
		}
		if (m_includeSubjects!=null) {
			StringTokenizer subjectsTokens = new StringTokenizer(m_includeSubjects,";");
			Subject subject = new Subject();
			Subjects subjects = new Subjects();
			List<Subject> subjList = subjects.getSubject();
			while (subjectsTokens.hasMoreTokens()) {
				String nextToken = subjectsTokens.nextToken();
				StringTokenizer subjTokens = new StringTokenizer(nextToken,CONFIG_DELIMITER);
				String subjType =getToken(subjTokens);
				String subjName= getToken(subjTokens);
				long subjectId = m_subjectIds.get(subjName)!=null ?m_subjectIds.get(subjName) : -1;
				subject = PolicyServiceTestHelper.getInstance().mapIncludedSubject(subjectId);
				subject.setSubjectName(subjName);
				subject.setSubjectType(subjType);
				subjList.add(subject);
				
			}
			target.setSubjects(subjects);
		}
		if (m_excludeSubjects!=null) {
			StringTokenizer subjectsTokens = new StringTokenizer(m_excludeSubjects,";");
			Subject subject = new Subject();
			Subjects subjects = new Subjects();
			List<Subject> subjList = subjects.getSubject();
			while (subjectsTokens.hasMoreTokens()) {
				String nextToken = subjectsTokens.nextToken();
				StringTokenizer subjTokens = new StringTokenizer(nextToken,CONFIG_DELIMITER);
				String subjType =getToken(subjTokens);
				String subjName= getToken(subjTokens);
				long subjectId = m_subjectIds.get(subjName)!=null ?m_subjectIds.get(subjName) : -1;
				subject = PolicyServiceTestHelper.getInstance().mapExcludedSubject(subjectId);
				subject.setSubjectName(subjName);
				subject.setSubjectType(subjType);
				subjList.add(subject);
			}
			target.setSubjects(subjects);
		}
		
		if (m_includeSubjectGroups!=null) {
			StringTokenizer subjGrpList = new StringTokenizer(m_includeSubjectGroups,";");
			Subjects subjects = new Subjects();
			List<SubjectGroup> subjectGroupList = subjects.getSubjectGroup();
			SubjectGroup subjectGroup = new SubjectGroup();
			while (subjGrpList.hasMoreTokens()) {
				String nextToken = subjGrpList.nextToken();
				StringTokenizer subjGrpTokens = new StringTokenizer(nextToken,CONFIG_DELIMITER);
				String subjType =getToken(subjGrpTokens);
				String subjGrpName= getToken(subjGrpTokens);
				long subjectGroupId = m_subjectGroupIds.get(subjGrpName)!=null ?m_subjectGroupIds.get(subjGrpName) : -1;
				subjectGroup = PolicyServiceTestHelper.getInstance().mapIncludedSubjectGroup(subjectGroupId);
				subjectGroup.setSubjectGroupName(subjGrpName);
				subjectGroup.setSubjectType(subjType);
				subjectGroupList.add(subjectGroup);
			}
			target.setSubjects(subjects);
		}
		if (m_excludeSubjectGroups!=null) {
			StringTokenizer subjGrpList = new StringTokenizer(m_excludeSubjectGroups,";");
			SubjectGroup subjectGroup = new SubjectGroup();
			Subjects subjects = new Subjects();
			List<SubjectGroup> subjectGroupList = subjects.getSubjectGroup();
			while (subjGrpList.hasMoreTokens()) {
				String nextToken = subjGrpList.nextToken();
				StringTokenizer subjGrpTokens = new StringTokenizer(nextToken,CONFIG_DELIMITER);
				String subjType =getToken(subjGrpTokens);
				String subjGrpName= getToken(subjGrpTokens);
				long subjectGroupId = m_subjectGroupIds.get(subjGrpName)!=null ?m_subjectGroupIds.get(subjGrpName) : -1;
				subjectGroup = PolicyServiceTestHelper.getInstance().mapExcludedSubjectGroup(subjectGroupId);
				subjectGroup.setSubjectGroupName(subjGrpName);
				subjectGroup.setSubjectType(subjType);
				subjectGroupList.add(subjectGroup);
			}
			target.setSubjects(subjects);
		}
		
		if (m_policyEffect!=null) {
			Rule rule = new Rule();
			if (m_policyEffect.equalsIgnoreCase("BLOCK")) {
				rule.setEffect(EffectType.BLOCK);
			} else if (m_policyEffect.equalsIgnoreCase("CHALLENGE")) {
				rule.setEffect(EffectType.CHALLENGE);
			} else if (m_policyEffect.equalsIgnoreCase("FLAG")) {
				rule.setEffect(EffectType.FLAG);
			} else  {
				rule.setEffect(EffectType.ALLOW);
			}
			policy.getRule().add(rule);
		}
		request.setPolicy(policy);
 		return request;
 		
	}

	private void cleanUpPolicy() {
		if (m_policyInfo!=null) {
			StringTokenizer policyTokens = new StringTokenizer(m_policyInfo,CONFIG_DELIMITER);
			String policyType =getToken(policyTokens);
			String policyName= getToken(policyTokens);
			PolicyServiceTestHelper.getInstance().cleanupPolicy(policyName, policyType);
		}
	}
	
	private static void cleanUpResources() {
		for(Resource resource:m_resourceList) {
			PolicyServiceTestHelper.getInstance().cleanUpResource(resource.getResourceName(), resource.getResourceType());
		}
		
	}
	private static void cleanUpSubjects() {
		for(Subject subject:m_subjList) {
			PolicyServiceTestHelper.getInstance().cleanUpSubjects(subject.getSubjectType(),subject.getSubjectName());
		}
	}
	private static void cleanUpSubjectGroups() {
		for(SubjectGroup subjectGroup:m_subjGroupList) {
			PolicyServiceTestHelper.getInstance().cleanupSubjectGroup(subjectGroup.getSubjectGroupName(),subjectGroup.getSubjectType());
		}
		
	}

}
