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

package org.ebayopensource.turmeric.policyservice.getentityhistory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.CreateOperationsResponse;
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.EntityHistory;
import org.ebayopensource.turmeric.security.v1.services.GetEntityHistoryRequest;
import org.ebayopensource.turmeric.security.v1.services.GetEntityHistoryResponse;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.OperationKey;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.test.services.utils.FindPolicyHelper;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GetEntityHistoryTests{

	private static final String CONFIG_DELIMITER = ":";
	private String m_testCaseName;
	private String m_policyKey;
	private String m_subjectKey;
	private String m_subjectGroupKey;
	private String m_resourceKey;
	private String m_operationKey;
	private String m_expectedResult;
	private static Map<String,Long> m_policyIds = new HashMap<String,Long>();
	private static Map<String,Long> m_subjectIds = new HashMap<String,Long>();
	private static Map<String,Long> m_subjectGroupIds = new HashMap<String,Long>();
	private static Map<String,Long> m_resourceIds = new HashMap<String,Long>();
	private static Map<String,Long> m_operationIds = new HashMap<String,Long>();
	private static List<Resource> m_resourceList = new ArrayList<Resource>();
	private static List<SubjectGroup> m_subjGroupList = new ArrayList<SubjectGroup>();
	private static List<Subject> m_subjList = new ArrayList<Subject>();
	private static List<Policy> m_policyList = new ArrayList<Policy>();
	private static Properties props = new Properties();
	private String m_startDate;
	private String m_endDate;
	private static final String m_PropFilePath = "GetEntityHistoryTests.properties";
	private static final String m_PreRequisitePoliciesFilePath = "PreRequisiteHistoryEntities.properties";
	
	@BeforeClass
	public static void setUp() throws Exception {
		System.out.println(" *** Create Pre-Requisite subject, subjectgroup resource & policies  ***");
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

	public GetEntityHistoryTests(String testCaseName, String startDate,String endDate,String subjectKey,String subjectGroupKey,
			String resourceKey,String operationKey, String policyKey, String expectedResult) {
		this.m_testCaseName =testCaseName;
		this.m_startDate = startDate;
		this.m_endDate = endDate;
		this.m_subjectKey = subjectKey;
		this.m_subjectGroupKey = subjectGroupKey;
		this.m_resourceKey = resourceKey;
		this.m_operationKey = operationKey;
		this.m_policyKey = policyKey;
		this.m_expectedResult = expectedResult;
	}
	
	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data(){
		return loadGetEntityHistoryInputData();
	}
	
    @Test
	public void getEntityHistory() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + m_testCaseName + " *** ");
    	GetEntityHistoryRequest getEntityHistoryRequest = new GetEntityHistoryRequest();
		GetEntityHistoryResponse response = new GetEntityHistoryResponse();
		try {
			constructGetEntityHistoryRequest(getEntityHistoryRequest);
			response = PolicyServiceTestHelper.getInstance().getEntityHistory(getEntityHistoryRequest);
			String errorMessage = response.getErrorMessage() != null ? response
					.getErrorMessage().getError().get(0).getMessage() : null;
			StringTokenizer responseTokens = new StringTokenizer(m_expectedResult,"|");
			String expectedAckValue = getToken(responseTokens);
			String expectedErrorMessage = getToken(responseTokens);
			String numOfHistoryEntities = getToken(responseTokens);
			if (expectedAckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				int actualHistoryEntities = 0;
				if (response.getEntityHistories()!=null) {
					actualHistoryEntities = response.getEntityHistories().size();
				}
				int expectedHistoryEntities = 0;
				if (numOfHistoryEntities!=null) {
					expectedHistoryEntities = Integer.parseInt(numOfHistoryEntities);
				}
				assertTrue("TestCase ="+m_testCaseName + "; Mismatch in total number of history entities ",actualHistoryEntities >= expectedHistoryEntities);
				while(responseTokens.hasMoreTokens()) {
					String expectedEntityName = getToken(responseTokens);
					boolean exists = false;
					for (EntityHistory event: response.getEntityHistories()) {
						if (event.getComments().contains(expectedEntityName)) { 
							exists = true;
							break;
						}
					}
				assertTrue(expectedEntityName+" doesn't exist", exists); 
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
	public static Collection loadGetEntityHistoryInputData() {
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		Pattern pattern =Pattern.compile("testcase(\\d*).name");
		int totalTests = 0;
		try {
			InputStream inputStream = GetEntityHistoryTests.class.getResourceAsStream(m_PropFilePath);
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
				String startDate= "testcase" + i + ".request.startDate";
				String endDate = "testcase" + i + ".request.endDate";
				String policyKey = "testcase" + i + ".request.policyKey";
				String subjectKey = "testcase" + i + ".request.subjectKey";
				String subjectGroupKey = "testcase" + i + ".request.subjectGroupKey";
				String resourceKey = "testcase" + i + ".request.resourceKey";
				String operationKey = "testcase" + i + ".request.operationKey";
				String expectedResult = "testcase" + i + ".response";
				eachRowData.add(props.getProperty(testName));
				eachRowData.add(props.getProperty(startDate));
				eachRowData.add(props.getProperty(endDate));
				eachRowData.add(props.getProperty(subjectKey));
				eachRowData.add(props.getProperty(subjectGroupKey));
				eachRowData.add(props.getProperty(resourceKey));
				eachRowData.add(props.getProperty(operationKey));
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
		
	@SuppressWarnings("deprecation")
	protected XMLGregorianCalendar getXMLGregorianCalendar(String date) throws Exception {
	      GregorianCalendar gcal = new GregorianCalendar();
	      if (date.equalsIgnoreCase("Today")){
	    	  Date today = new Date();
	    	  today.setTime(System.currentTimeMillis());
	    	  today.setHours(today.getHours()+ 1);
	    	  gcal.setTime(today);  
	      } else if(date.equalsIgnoreCase("Yesterday")){
	    	  Date yesterday = new Date();
	    	  yesterday.setTime(System.currentTimeMillis());	
	    	  yesterday.setHours(yesterday.getHours()- 24);
	    	  gcal.setTime(yesterday);  
	      }
	      
	      XMLGregorianCalendar xgcal = DatatypeFactory.newInstance()
	            .newXMLGregorianCalendar(gcal);
	      return xgcal;
	   }
	private void constructGetEntityHistoryRequest(GetEntityHistoryRequest getEntityHistoryRequest) throws Exception {
		if (m_startDate!=null) {
			getEntityHistoryRequest.setStartDate(getXMLGregorianCalendar(m_startDate));
		}
		if (m_endDate!=null) {
			getEntityHistoryRequest.setEndDate(getXMLGregorianCalendar(m_endDate));
		}
		
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
			getEntityHistoryRequest.getPolicyKey().add(policyKey);	
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
			getEntityHistoryRequest.getSubjectKey().add(subjectKey);
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
			getEntityHistoryRequest.getSubjectGroupKey().add(subjectGroupKey);
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
			getEntityHistoryRequest.getResourceKey().add(resourceKey);		
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
			getEntityHistoryRequest.getOperationKey().add(operationKey);
			System.out.println("\t operationId = "+opId+ " , operationName= "+opName+" , resourceName = "+resName);
		}
	} 
	
	@SuppressWarnings("unchecked")
	private static void createPreRequisiteData() {
		Properties policiesProps = new Properties();
		CreatePolicyRequest createPolicyRequest = new CreatePolicyRequest();
		CreatePolicyResponse res = new CreatePolicyResponse();
		
		Pattern pattern =Pattern.compile("policy(\\d*).policyInfo");
		int totalPolicies = 0;
		try {
			InputStream inputStream = GetEntityHistoryTests.class.getResourceAsStream(m_PreRequisitePoliciesFilePath);
			policiesProps.load(inputStream);
			Iterator it = policiesProps.keySet().iterator();
			while(it.hasNext()) {
				String key = it.next().toString();
				Matcher matcher = pattern.matcher(key);
				if (matcher.find()) {
					totalPolicies++;
				}
			}
			String subjects = policiesProps.getProperty("prerequisite.subjects");
			String subjectsGroups = policiesProps.getProperty("prerequisite.subjectgroups");
			String resources = policiesProps.getProperty("prerequisite.resources");
			createSubjects(subjects);
			createSubjectGroups(subjectsGroups);
			createResources(resources);

			
			for (int i = 0; i < totalPolicies; i++) {
				String policyInfo = policiesProps.getProperty("policy" + i + ".policyInfo");
				String resourcesInfo = policiesProps.getProperty("policy" + i + ".resources");
				String includeSubjects = policiesProps.getProperty("policy" + i + ".includeSubjects");
				String includeSubjectGroups = policiesProps.getProperty("policy" + i + ".includeSubjectGroups");
				FindPolicyHelper helper = new FindPolicyHelper();
				createPolicyRequest = helper.constructCreatePolicyRequest(policyInfo, resourcesInfo,
						null, includeSubjects,null, includeSubjectGroups,null, null);
				m_policyList.add(createPolicyRequest.getPolicy());
				res = PolicyServiceTestHelper.getInstance().createPolicy(createPolicyRequest);
				long policyid = 0L;
				if (res.getAck().equals(AckValue.SUCCESS)) {
					policyid = res.getPolicyId();
					m_policyIds.put(createPolicyRequest.getPolicy().getPolicyName(), policyid);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
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
					if (subjectId > 0) {
						m_subjectIds.put(subject.getSubjectName(),subjectId);
					} else {
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
				FindPolicyHelper.constructCreateResourceRequest(resourceList,token);
			}
			m_resourceList = resourceList; 
			createResourcesResponse =PolicyServiceTestHelper.getInstance().createResources(createResourcesRequest);
			if (createResourcesResponse.getResourceIds()!=null) {
				List<Long> resourceIds= createResourcesResponse.getResourceIds();
				int i=0;
				for ( Long id :resourceIds){
					m_resourceIds.put(resourceList.get(i).getResourceName(),id);
			
					for (Operation operation : resourceList.get(i).getOperation()){
						Long operationId = PolicyServiceTestHelper.getInstance().createOpertionForResource(resourceList.get(i).getResourceType(), resourceList.get(i).getResourceName(), operation.getOperationName());
						if (operationId!=null) {
							m_operationIds.put(resourceList.get(i).getResourceName()+ operation.getOperationName(),operationId);
						}
					}
					i++;
				}
			
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
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
				FindPolicyHelper.constructCreateSubjectGroupsRequest(subjGroupList,token);
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
