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
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.DisablePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.DisablePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.EnablePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.EnablePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Resources;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.Subjects;
import org.ebayopensource.turmeric.security.v1.services.Target;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.ebayopensource.turmeric.utils.RuleUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EnableDisablePolicyTests{

	private static final String CONFIG_DELIMITER = ":";
	private static Properties props = new Properties();
	private String m_testCaseName;
	private String m_policyKey;
	private String m_enablePolicy;
	private String m_expectedResult;
	private static Map<String,Long> m_subjectIds = new HashMap<String,Long>();
	private static Map<String,Long> m_subjectGroupIds = new HashMap<String,Long>();;
	private static List<SubjectGroup> m_subjGroupList = new ArrayList<SubjectGroup>();
	private static List<Subject> m_subjList = new ArrayList<Subject>();
	private static List<Resource> m_resourceList = new ArrayList<Resource>();
	private static Map<String,Long> m_policyIds = new HashMap<String,Long>();
	private static List<Policy> m_policyList = new ArrayList<Policy>();
	private static final String s_PropFilePath = "EnableDisablePolicyTests.properties";
	
	@BeforeClass
	public static void setUp() throws Exception {
		System.out.println(" *** Create Pre-Requisite data ***");
		createPreRequisiteData();
		System.out.println(" *** Create Pre-Requisite data has been completed successfully ***");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		cleanUpPolicy();
		cleanUpResources();
		cleanUpSubjectGroups();
		cleanUpSubjects();
	}

	public EnableDisablePolicyTests(String testCaseName, String policyKey,String enablePolicy, String expectedResult) {
		this.m_testCaseName =testCaseName;
		this.m_policyKey = policyKey;
		this.m_enablePolicy = enablePolicy;
		this.m_expectedResult = expectedResult;
	}
	
	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data(){
		return loadEnableDisablePolicyInputData();
	}
	
    @Test
	public void enableDisablePolicy() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + m_testCaseName + " *** ");
    	try {
	    	if (m_enablePolicy.equalsIgnoreCase("TRUE")){
	    		EnablePolicyRequest enablePolicyRequest = new EnablePolicyRequest();
	        	EnablePolicyResponse response = new EnablePolicyResponse();
	        	enablePolicyRequest = constructEnablePolicyRequest();
				response = PolicyServiceTestHelper.getInstance().enablePolicy(enablePolicyRequest);
				String errorMessage = response.getErrorMessage() != null ? response
						.getErrorMessage().getError().get(0).getMessage() : null;
				StringTokenizer policyTokens = new StringTokenizer(m_expectedResult,"|");
				String expectedAckValue = getToken(policyTokens);
				String expectedErrorMessage = getToken(policyTokens);
				
				if (expectedAckValue.equalsIgnoreCase("success")) {
					assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
					assertNull(errorMessage, response.getErrorMessage());
				} else {
	    			assertEquals(errorMessage, AckValue.FAILURE, response.getAck());
					assertNotNull(errorMessage, response.getErrorMessage());
					assertEquals(errorMessage, expectedErrorMessage,errorMessage);
				}
	    	} else {
    		
	    		DisablePolicyRequest disablePolicyRequest = new DisablePolicyRequest();
	    		DisablePolicyResponse response = new DisablePolicyResponse();
	    		disablePolicyRequest = constructDisablePolicyRequest();
	    		response = PolicyServiceTestHelper.getInstance().disablePolicy(disablePolicyRequest);
				String errorMessage = response.getErrorMessage() != null ? response
						.getErrorMessage().getError().get(0).getMessage() : null;
				StringTokenizer policyTokens = new StringTokenizer(m_expectedResult,"|");
				String expectedAckValue = getToken(policyTokens);
				String expectedErrorMessage = getToken(policyTokens);
				
				if (expectedAckValue.equalsIgnoreCase("success")) {
					assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
					assertNull(errorMessage, response.getErrorMessage());
				} else {
	    			assertEquals(errorMessage, AckValue.FAILURE, response.getAck());
					assertNotNull(errorMessage, response.getErrorMessage());
					assertEquals(errorMessage, expectedErrorMessage,errorMessage);
				}
		
	    	}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
		System.out.println("*** Test Scenario : " + m_testCaseName + " completed successfully ***");
	}
		
	@SuppressWarnings("unchecked")
	public static Collection loadEnableDisablePolicyInputData() {
		
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		Pattern pattern =Pattern.compile("testcase(\\d*).name");
		int totalTests = 0;
		try {
			InputStream inputStream = EnableDisablePolicyTests.class.getResourceAsStream(s_PropFilePath);
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
				String policyKey = "testcase" + i + ".request.policykey";
				String enablePolicy = "testcase" + i + ".request.enablepolicy";
				String expectedResult = "testcase" + i + ".response";
				eachRowData.add(props.getProperty(testName));
				eachRowData.add(props.getProperty(policyKey));
				eachRowData.add(props.getProperty(enablePolicy));
				eachRowData.add(props.getProperty(expectedResult));
				list.add(eachRowData.toArray());
			}
		} catch (IOException e) {	}
		
		return list;
	}
	
	private static void createPreRequisiteData() {
		String subjects = props.getProperty("testcase.prerequisite.subjects");
		String subjectsGroups = props.getProperty("testcase.prerequisite.subjectgroups");
		String resources = props.getProperty("testcase.prerequisite.resources");
		String policies = props.getProperty("testcase.prerequisite.policies");
		createSubjects(subjects);
		createSubjectGroups(subjectsGroups);
		
		createResources(resources);
		createPolicies(policies);
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
	
	private static void createResources(String resources) {
		CreateResourcesRequest createResourcesRequest = new CreateResourcesRequest();
		List<Resource> resourceList = createResourcesRequest.getResources();
		try {
			StringTokenizer resTokens = new StringTokenizer(resources,";");
			while(resTokens.hasMoreTokens()) {
				String token = resTokens.nextToken();
				constructCreateResourceRequest(resourceList,token);
			}
			m_resourceList = resourceList; 
			PolicyServiceTestHelper.getInstance().createResources(createResourcesRequest);
						
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
	private static void createPolicies(String policies) {
		if (policies!=null) {
			StringTokenizer resTokens = new StringTokenizer(policies,";");
			Policy policy = new Policy();
			CreatePolicyRequest createPolicyRequest = new CreatePolicyRequest();
			while(resTokens.hasMoreTokens()) {
				String token = resTokens.nextToken();
				constructCreatePolicyRequest(policy,token);
				policy =RuleUtils.updatePolicyRule(policy);
				m_policyList.add(policy);
				createPolicyRequest.setPolicy(policy);
				CreatePolicyResponse res = PolicyServiceTestHelper.getInstance().createPolicy(createPolicyRequest);
				long policyid = 0L;
				if (res.getAck().equals(AckValue.SUCCESS)) {
					policyid = res.getPolicyId();
					m_policyIds.put(policy.getPolicyName(), policyid);
				}
			}
		}
	}

	private static void constructCreatePolicyRequest(Policy policy, String token) {
		StringTokenizer policyInfo = new StringTokenizer(token,"|");
		Resources resources = new Resources();
		Subjects  subjects = new Subjects();
		
		String policDetails = getToken(policyInfo);
		String resourcesDetails = getToken(policyInfo);
		String subjectGroupDetails = getToken(policyInfo);
		StringTokenizer policyTokens = new StringTokenizer(policDetails,CONFIG_DELIMITER);
		policy.setPolicyType(getToken(policyTokens));
		policy.setPolicyName(getToken(policyTokens));
		policy.setDescription(getToken(policyTokens));
		System.out.println("\t  policyType= "+policy.getPolicyType()+" , policyName = "+policy.getPolicyName());
		PolicyServiceTestHelper.getInstance().cleanupPolicy(policy.getPolicyName(), policy.getPolicyType());
		if (resourcesDetails!=null) {
			Resource resource = new Resource();
			StringTokenizer resourceTokens = new StringTokenizer(resourcesDetails,CONFIG_DELIMITER);
			while (resourceTokens.hasMoreTokens()) {
				resource = new Resource();
				resource.setResourceType(getToken(resourceTokens));
				resource.setResourceName(getToken(resourceTokens));
				resources.getResource().add(resource);
				System.out.println("\t  resourceType= "+resource.getResourceType()+" , resourceName = "+resource.getResourceName());
			}
			
		}
		if (subjectGroupDetails!=null) {
			SubjectGroup subjectGroup = new SubjectGroup();
			StringTokenizer sgTokens = new StringTokenizer(subjectGroupDetails,CONFIG_DELIMITER);
			while (sgTokens.hasMoreTokens()) {
				subjectGroup = new SubjectGroup();
				subjectGroup.setSubjectType(getToken(sgTokens));
				subjectGroup.setSubjectGroupName(getToken(sgTokens));				
				subjects.getSubjectGroup().add(subjectGroup);
				System.out.println("\t  subjectType= "+subjectGroup.getSubjectType()+" , subjectGroupName = "+subjectGroup.getSubjectGroupName());
			}
		}
		Target target = new Target();
		target.setResources(resources);
		target.setSubjects(subjects);
		policy.setTarget(target);
	}

	private static String getToken(StringTokenizer tokenizer)
	{
		if(tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken().trim();
			return "null".equals(token) ? null : token;
		} 
		return null;
	}
		
	private EnablePolicyRequest constructEnablePolicyRequest() throws Exception {
		EnablePolicyRequest request = new EnablePolicyRequest();
		if (m_policyKey!=null) {
			PolicyKey policyKey = new PolicyKey();
			StringTokenizer policyTokens = new StringTokenizer(m_policyKey,CONFIG_DELIMITER);
			String policyId  =getToken(policyTokens);
			String policyType =getToken(policyTokens);
			String policyName= getToken(policyTokens);
			if (policyId!=null && policyId.contains("?")) {
				long id = m_policyIds.get(policyName)!=null ?m_policyIds.get(policyName) : -1;
				policyKey.setPolicyId(id);
			}
			policyKey.setPolicyType(policyType);
			policyKey.setPolicyName(policyName);
			request.setPolicyKey(policyKey);	
			System.out.println("\t policyId = "+policyKey.getPolicyId()+ " , policyType= "+policyType+" , policyName = "+policyName);
		}
		return request;
	}
	private DisablePolicyRequest constructDisablePolicyRequest() throws Exception {
		DisablePolicyRequest request = new DisablePolicyRequest();
		if (m_policyKey!=null) {
			PolicyKey policyKey = new PolicyKey();
			StringTokenizer policyTokens = new StringTokenizer(m_policyKey,CONFIG_DELIMITER);
			String policyId  =getToken(policyTokens);
			String policyType =getToken(policyTokens);
			String policyName= getToken(policyTokens);
			if (policyId!=null && policyId.contains("?")) {
				long id = m_policyIds.get(policyName)!=null ?m_policyIds.get(policyName) : -1;
				policyKey.setPolicyId(id);
			}
			policyKey.setPolicyType(policyType);
			policyKey.setPolicyName(policyName);
			request.setPolicyKey(policyKey);	
			System.out.println("\t policyId = "+policyKey.getPolicyId()+ " , policyType= "+policyType+" , policyName = "+policyName);
		}
		return request;
	}
	
	private static void cleanUpPolicy() {
		for (Policy policy:m_policyList) {
			PolicyServiceTestHelper.getInstance().cleanupPolicy(policy.getPolicyName(),policy.getPolicyType());
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
