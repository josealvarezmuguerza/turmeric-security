/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.ebayopensource.turmeric.policyservice.getauthnpolicy;

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
import org.ebayopensource.turmeric.policyservice.findpolicyapi.FindPolicyWithPolicyTypeAndSecondaryKeyTests;
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.test.services.utils.FindPolicyHelper;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.ebayopensource.turmeric.utils.RuleUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GetAuthenticationPolicyTests{

	private static final String CONFIG_DELIMITER = ":";
	private static Properties props = new Properties();
	private String m_testCaseName;
	private String m_getAuthzPolicyKey;
	private String m_expectedResult;
	private static final String m_PropFilePath = "GetAuthenticationPolicyTests.properties";
	
	private static Map<String,Long> m_policyIds = new HashMap<String,Long>();
	private static Map<String,Long> m_subjectIds = new HashMap<String,Long>();
	private static Map<String,Long> m_subjectGroupIds = new HashMap<String,Long>();
	private static Map<String,Long> m_resourceIds = new HashMap<String,Long>();
	private static List<Resource> m_resourceList = new ArrayList<Resource>();
	private static List<SubjectGroup> m_subjGroupList = new ArrayList<SubjectGroup>();
	private static List<Subject> m_subjList = new ArrayList<Subject>();
	private static List<Policy> m_policyList = new ArrayList<Policy>();
	private static final String m_PreRequisitePoliciesFilePath = "PreRequisitePolicies.properties";

	@BeforeClass
	public static void setUp() throws Exception {
		System.out.println(" *** Create Pre-Requisite subject, subjectgroup resource & policies  ***");
		cleanUpPolicies();
		cleanUpResources();
		cleanUpSubjectGroups();
		cleanUpSubjects();
		createPreRequisiteData();
		System.out.println(" *** Create Pre-Requisite data has been completed successfully ***");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		cleanUpPolicies();
		cleanUpResources();
		cleanUpSubjectGroups();
		cleanUpSubjects();
	}

	public GetAuthenticationPolicyTests(String testCaseName, String policyKey, String expectedResult) {
		this.m_testCaseName =testCaseName;
		this.m_getAuthzPolicyKey = policyKey;
		this.m_expectedResult = expectedResult;
	}
	
	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data(){
		return loadFindPolicyInputData();
	}
	
    @Test
	public void getAuthnPolicy() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + m_testCaseName + " *** ");
		GetAuthenticationPolicyRequest  getAuthenticationPolicyRequest = new GetAuthenticationPolicyRequest();
		GetAuthenticationPolicyResponse response = new GetAuthenticationPolicyResponse();
		try {
			getAuthenticationPolicyRequest = constructGetAuthenticationPolicyRequest();
			response = PolicyServiceTestHelper.getInstance().getAuthenticationPolicy(getAuthenticationPolicyRequest);
			String errorMessage = response.getErrorMessage() != null ? response
					.getErrorMessage().getError().get(0).getMessage() : null;
			StringTokenizer responseTokens = new StringTokenizer(m_expectedResult,"|");
			String expectedAckValue = getToken(responseTokens);
			String expectedErrorMessage = getToken(responseTokens);
			String numOfAuthnPolcies = getToken(responseTokens);
			if (expectedAckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				
				int actualPolicies = 0;
				if (response.getPolicy()!=null && response.getPolicy().getAuthenticationScheme()!=null) {
					actualPolicies = response.getPolicy().getAuthenticationScheme().size();
				}
				int expectedPolices = 0;
				if (numOfAuthnPolcies!=null) {
					expectedPolices = Integer.parseInt(numOfAuthnPolcies);
				}
				assertTrue("TestCase ="+m_testCaseName + "; Mismatch in total number of policies ",actualPolicies >= expectedPolices);
				while(responseTokens.hasMoreTokens()) {
					String expectedAuthnPolicyName = getToken(responseTokens);
					boolean exists = false;
					for (String actualAuthnPolicyName  : response.getPolicy().getAuthenticationScheme()) {
							if (actualAuthnPolicyName.equals(expectedAuthnPolicyName)) { 
								exists = true;
								break;
							}
						}
						assertTrue(expectedAuthnPolicyName+" doesn't exist", exists); 
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

	private static void createPreRequisiteData() {
		Properties policiesProps = new Properties();
		Pattern pattern =Pattern.compile("policy(\\d*).policyInfo");
		int totalPolicies = 0;
		try {
			InputStream inputStream = FindPolicyWithPolicyTypeAndSecondaryKeyTests.class.getResourceAsStream(m_PreRequisitePoliciesFilePath);
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
				Policy policy = createPolicyRequest.getPolicy();
				createPolicyRequest.setPolicy(RuleUtils.updatePolicyRule(policy));
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

	
	@SuppressWarnings("unchecked")
	public static Collection loadFindPolicyInputData() {
		
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		Pattern pattern =Pattern.compile("testcase(\\d*).name");
		int totalTests = 0;
		try {
			InputStream inputStream = GetAuthenticationPolicyTests.class.getResourceAsStream(m_PropFilePath);
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
				String policyKey = "testcase" + i + ".getAuthzPolicyKey";
				String expectedResult = "testcase" + i + ".response";
				eachRowData.add(props.getProperty(testName));
				eachRowData.add(props.getProperty(policyKey));
				eachRowData.add(props.getProperty(expectedResult));
				list.add(eachRowData.toArray());
			}
		} catch (IOException e) {	}
		
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
		
	private GetAuthenticationPolicyRequest constructGetAuthenticationPolicyRequest() throws Exception {
		GetAuthenticationPolicyRequest request = new GetAuthenticationPolicyRequest();
		if (m_getAuthzPolicyKey!=null){
			StringTokenizer requestTokens = new StringTokenizer(
					m_getAuthzPolicyKey, CONFIG_DELIMITER);
			String resourceType = getToken(requestTokens);
			String resourceName = getToken(requestTokens);
			String operationName = getToken(requestTokens);
			request.setResourceType(resourceType);
			request.setResourceName(resourceName);
			request.setOperationName(operationName);

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
