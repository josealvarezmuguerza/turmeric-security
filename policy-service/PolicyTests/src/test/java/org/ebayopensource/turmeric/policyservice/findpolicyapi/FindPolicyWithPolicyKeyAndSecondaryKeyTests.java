/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.findpolicyapi;

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
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesResponse;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.OperationKey;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.Query;
import org.ebayopensource.turmeric.security.v1.services.QueryCondition;
import org.ebayopensource.turmeric.security.v1.services.Resolution;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.test.services.utils.FindPolicyHelper;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.ebayopensource.turmeric.utils.RuleUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class FindPolicyWithPolicyKeyAndSecondaryKeyTests{

	private static final String CONFIG_DELIMITER = ":";
	private String m_testCaseName;
	private String m_policyKey;
	private String m_subjectKey;
	private String m_subjectGroupKey;
	private String m_resourceKey;
	private String m_operationKey;
	private String m_queryCondition;
	private String m_expectedResult;
	private static Properties props = new Properties();
	private static Map<String,Long> m_policyIds = new HashMap<String,Long>();
	private static Map<String,Long> m_subjectIds = new HashMap<String,Long>();
	private static Map<String,Long> m_subjectGroupIds = new HashMap<String,Long>();
	private static Map<String,Long> m_resourceIds = new HashMap<String,Long>();
	private static Map<String,Long> m_operationIds = new HashMap<String,Long>();
	private static List<Resource> m_resourceList = new ArrayList<Resource>();
	private static List<SubjectGroup> m_subjGroupList = new ArrayList<SubjectGroup>();
	private static List<Subject> m_subjList = new ArrayList<Subject>();
	private static List<Policy> m_policyList = new ArrayList<Policy>();
	private static final String m_PropFilePath = "FindPolicyWithPolicyKeyAndSecondaryKeyTests.properties";
	private static final String m_PreRequisitePoliciesFilePath = "PreRequisitePolicies.properties";
	
	@Before
	public  void setUp() throws Exception {
		System.out.println(" *** Create Pre-Requisite subject, subjectgroup resource & policies  ***");
		createPreRequisiteData();
		System.out.println(" *** Create Pre-Requisite data has been completed successfully ***");
	}

	@After
	public  void tearDown() throws Exception {
		cleanUpPolicies();
		cleanUpResources();
		cleanUpSubjectGroups();
		cleanUpSubjects();
	}
	
	
	public FindPolicyWithPolicyKeyAndSecondaryKeyTests(String testCaseName, String policyKey, String subjectKey,
			String subjectGroupKey,	String resourceKey, String operationKey, String expectedResult) {
		this.m_testCaseName =testCaseName;
		this.m_policyKey = policyKey;
		this.m_subjectKey = subjectKey;
		this.m_subjectGroupKey = subjectGroupKey;
		this.m_resourceKey = resourceKey;
		this.m_operationKey = operationKey;
		this.m_expectedResult = expectedResult;
	}
	
	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data(){
		return loadFindPolicyInputData();
	}
	
    @Test
	public void findPolicy() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + m_testCaseName + " *** ");
		FindPoliciesRequest findPoliciesRequest = new FindPoliciesRequest();
		FindPoliciesResponse response = new FindPoliciesResponse();
		try {
			findPoliciesRequest = constructFindPoliciesRequest();
			response = PolicyServiceTestHelper.getInstance().findPolicies(findPoliciesRequest);
			String errorMessage = response.getErrorMessage() != null ? response
					.getErrorMessage().getError().get(0).getMessage() : null;
			StringTokenizer policyTokens = new StringTokenizer(m_expectedResult,"|");
			String expectedAckValue = getToken(policyTokens);
			String expectedErrorMessage = getToken(policyTokens);
			String numOfPolicies = getToken(policyTokens);
			if (expectedAckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				int actualPolicies = response.getPolicySet().getPolicy().size();
				int expectedPolices = 0;
				if (numOfPolicies!=null) {
					expectedPolices = Integer.parseInt(numOfPolicies);
				}
				assertTrue("TestCase ="+m_testCaseName + "; Mismatch in total number of policies ",actualPolicies >= expectedPolices);
				if (expectedPolices>0) {
					String policyList = getToken(policyTokens);
					StringTokenizer policyNameTokens = new StringTokenizer(policyList,",");
					while(policyNameTokens.hasMoreTokens()) {
						String policyName = getToken(policyNameTokens);
						boolean exists = false;
						for (Policy policy  : response.getPolicySet().getPolicy()) {
							if (policy.getPolicyName().equals(policyName)) { 
								exists = true;
								break;
							}
						}
					assertTrue(policyName+" doesn't exist", exists);	
				}
			 }
			} else {
				assertEquals(errorMessage, AckValue.FAILURE, response.getAck());
				assertNotNull(errorMessage, response.getErrorMessage());
				assertEquals(errorMessage, expectedErrorMessage,errorMessage);
			}
	
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
		System.out.println("*** Test Scenario : " + m_testCaseName + " completed successfully ***");
	}
		
	@SuppressWarnings("unchecked")
	public static Collection loadFindPolicyInputData() {
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		Pattern pattern =Pattern.compile("testcase(\\d*).name");
		int totalTests = 0;
		try {
			InputStream inputStream = FindPolicyWithPolicyKeyAndSecondaryKeyTests.class.getResourceAsStream(m_PropFilePath);
			props.load(inputStream);
			Iterator it = props.keySet().iterator();
			while(it.hasNext()) {
				String key = it.next().toString();
				Matcher matcher = pattern.matcher(key);
				if (matcher.find()) {
					totalTests++;
				}
			}
			for (int i = 0; i < totalTests; i++) {
				eachRowData = new ArrayList();
				String testName = "testcase" + i + ".name";
				String policyKey = "testcase" + i + ".request.policykey";
				String subjectKey = "testcase" + i + ".request.subjectkey";
				String subjectGroupKey = "testcase" + i + ".request.subjectgroupkey";
				String resourceKey = "testcase" + i + ".request.resourcekey";
				String operationKey = "testcase" + i + ".request.operationkey";
				String expectedResult = "testcase" + i + ".response";
				eachRowData.add(props.getProperty(testName));
				eachRowData.add(props.getProperty(policyKey));
				eachRowData.add(props.getProperty(subjectKey));
				eachRowData.add(props.getProperty(subjectGroupKey));
				eachRowData.add(props.getProperty(resourceKey));
				eachRowData.add(props.getProperty(operationKey));
				eachRowData.add(props.getProperty(expectedResult));
				list.add(eachRowData.toArray());
			}
		} catch (IOException e) {}
		
		return list;
	}
	
	private static void createPreRequisiteData() {
		Properties policiesProps = new Properties();
		Pattern pattern =Pattern.compile("policy(\\d*).policyInfo");
		int totalPolicies = 0;
		try {
			InputStream inputStream = FindPolicyWithPolicyKeyAndSecondaryKeyTests.class.getResourceAsStream(m_PreRequisitePoliciesFilePath);
			policiesProps.load(inputStream);
			Iterator it = policiesProps.keySet().iterator();
			while(it.hasNext()) {
				String key = it.next().toString();
				Matcher matcher = pattern.matcher(key);
				if (matcher.find()) {
					totalPolicies++;
				}
			}
		} catch (IOException e) {}
		
		try {
			
			String subjects = policiesProps.getProperty("prerequisite.subjects");
			String subjectsGroups = policiesProps.getProperty("prerequisite.subjectgroups");
			String resources = policiesProps.getProperty("prerequisite.resources");
			
			createSubjects(subjects);
			createSubjectGroups(subjectsGroups);
			createResources(resources);
			CreatePolicyRequest createPolicyRequest = new CreatePolicyRequest();
			CreatePolicyResponse res = new CreatePolicyResponse();
			for (int i = 0; i < totalPolicies; i++) {
				String policyInfo = policiesProps.getProperty("policy" + i + ".policyInfo");
				String resourcesInfo = policiesProps.getProperty("policy" + i + ".resources");
				String globalsubjectdomainsList = policiesProps.getProperty("policy" + i+ ".globalSubjectDomainsList");
				String includeSubjects = policiesProps.getProperty("policy" + i + ".includeSubjects");
				String excludeSubjects = policiesProps.getProperty("policy" + i + ".excludeSubjects");
				String includeSubjectGroups = policiesProps.getProperty("policy" + i + ".includeSubjectGroups");
				String excludeSubjectGroups = policiesProps.getProperty("policy" + i + ".excludeSubjectGroups");
				String policyEffect =policiesProps.getProperty("policy" + i + ".queryCondition");
				FindPolicyHelper helper = new FindPolicyHelper();
				createPolicyRequest = helper.constructCreatePolicyRequest(policyInfo, resourcesInfo,
						globalsubjectdomainsList, includeSubjects,
						excludeSubjects, includeSubjectGroups,
						excludeSubjectGroups, policyEffect);
				createPolicyRequest.setPolicy(RuleUtils.updatePolicyRule(createPolicyRequest.getPolicy()));
				m_policyList.add(createPolicyRequest.getPolicy());
				res = PolicyServiceTestHelper.getInstance().createPolicy(createPolicyRequest);
				long policyid = 0L;
				if (res.getAck().equals(AckValue.SUCCESS)) {
					policyid = res.getPolicyId();
					m_policyIds.put(createPolicyRequest.getPolicy().getPolicyName(), policyid);
				}
			}
		} catch (Exception e) {

		}
		
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


	private static String getToken(StringTokenizer tokenizer)
	{
		if(tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken().trim();
			return "null".equals(token) ? null : token;
		} 
		return null;
	}
		
	private FindPoliciesRequest constructFindPoliciesRequest() throws Exception {
		FindPoliciesRequest request = new FindPoliciesRequest();
		QueryCondition queryCondition = new QueryCondition();
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
			request.getPolicyKey().add(policyKey);	
			System.out.println("\t policyId = "+policyId+ " , policyType= "+policyType+" , policyName = "+policyName);
		}
		if (m_subjectKey!=null) {
			SubjectKey subjectKey = new SubjectKey();
			StringTokenizer subjTokens = new StringTokenizer(m_subjectKey,CONFIG_DELIMITER);
			String subjectId  =getToken(subjTokens);
			String subjectType =getToken(subjTokens);
			String subjectName= getToken(subjTokens);
			if (subjectId!=null && subjectId.contains("?")) {
				long id = m_subjectIds.get(subjectName)!=null ?m_subjectIds.get(subjectName) : -1;
				subjectKey.setSubjectId(id);
			}
			subjectKey.setSubjectType(subjectType);
			subjectKey.setSubjectName(subjectName);
			request.getSubjectKey().add(subjectKey);
			System.out.println("\t subjectId = "+subjectId+ " , subjectType= "+subjectType+" , subjectName = "+subjectName);
		}
		if (m_subjectGroupKey!=null) {
			SubjectGroupKey subjectGroupKey = new SubjectGroupKey();
			StringTokenizer subjTokens = new StringTokenizer(m_subjectGroupKey,CONFIG_DELIMITER);
			String subjectGroupId  =getToken(subjTokens);
			String subjectType =getToken(subjTokens);
			String subjectGroupName= getToken(subjTokens);
			if (subjectGroupId!=null && subjectGroupId.contains("?")) {
				long id = m_subjectGroupIds.get(subjectGroupName)!=null ?m_subjectGroupIds.get(subjectGroupName) : -1;
				subjectGroupKey.setSubjectGroupId(id);
			}
			subjectGroupKey.setSubjectType(subjectType);
			subjectGroupKey.setSubjectGroupName(subjectGroupName);
			request.getSubjectGroupKey().add(subjectGroupKey);
			System.out.println("\t subjectGroupId = "+subjectGroupId+ " , subjectType= "+subjectType+" , subjectGroupName = "+subjectGroupName);
		}
		if (m_resourceKey!=null) {
			ResourceKey resourceKey = new ResourceKey();
			StringTokenizer resourceTokens = new StringTokenizer(m_resourceKey,CONFIG_DELIMITER);
			String resourceId  =getToken(resourceTokens);
			String resourceType =getToken(resourceTokens);
			String resourceName= getToken(resourceTokens);
			if (resourceId!=null && resourceId.contains("?")) {
				long id = m_resourceIds.get(resourceName)!=null ?m_resourceIds.get(resourceName) : -1;
				resourceKey.setResourceId(id);
			}
			resourceKey.setResourceType(resourceType);
			resourceKey.setResourceName(resourceName);
			request.getResourceKey().add(resourceKey);		
			System.out.println("\t resourceId = "+resourceId+ " , resourceType= "+resourceType+" , resourceName = "+resourceName);
		}
		if (m_operationKey!=null) {
			OperationKey operationKey = new OperationKey();
			StringTokenizer opTokens = new StringTokenizer(m_operationKey,CONFIG_DELIMITER);
			String opId  =getToken(opTokens);
			String opName= getToken(opTokens);
			String resType =getToken(opTokens);
			String resName =getToken(opTokens);
			if (opId!=null && opId.contains("?")) {
				long id = m_operationIds.get(resName+opName)!=null ?m_operationIds.get(resName+opName) : -1;
				operationKey.setOperationId(id);
			}
			operationKey.setOperationName(opName);
			operationKey.setResourceType(resType);
			operationKey.setResourceName(resName);
			request.getOperationKey().add(operationKey);
			System.out.println("\t operationId = "+opId+ " , operationName= "+opName+" , resourceName = "+resName);
		}
	
		if (m_queryCondition!=null) {
			
			StringTokenizer opTokens = new StringTokenizer(m_queryCondition,CONFIG_DELIMITER);
			String queryType = getToken(opTokens);
			String queryValue = getToken(opTokens);
			Query query = new Query();
			query.setQueryType(queryType); // Effect/SubjectSearchScope/MaskedIds/ActivePoliciesOnly
			query.setQueryValue(queryValue); // BLOCK|FLAG|CHALLENGE|ALLOW/TARGET|EXCLUDED|BOTH/TRUE|FALSE/TRUE|FALSE
			queryCondition.setResolution(Resolution.AND);
			queryCondition.getQuery().add(query);
			request.setQueryCondition(queryCondition);
			if (!(queryType.equalsIgnoreCase("ActivePoliciesOnly"))) {
				query.setQueryType("ActivePoliciesOnly");
		        query.setQueryValue("FALSE");
		        queryCondition.getQuery().add(query);
		        request.setQueryCondition(queryCondition);		
			}
			System.out.println("\t QueryConditon  querytype = "+ queryType +" queryValue = " +queryValue);
		} else {
			Query query = new Query();
			query.setQueryType("ActivePoliciesOnly");
	        query.setQueryValue("FALSE");
	        queryCondition.getQuery().add(query);
	        request.setQueryCondition(queryCondition);		
		}
		
		return request;
	}
	
	private static void cleanUpPolicies() {
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
