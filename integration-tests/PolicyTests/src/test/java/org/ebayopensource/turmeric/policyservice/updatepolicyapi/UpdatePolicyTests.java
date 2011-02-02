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

package org.ebayopensource.turmeric.policyservice.updatepolicyapi;

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
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Resources;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.Subjects;
import org.ebayopensource.turmeric.security.v1.services.Target;
import org.ebayopensource.turmeric.security.v1.services.UpdateMode;
import org.ebayopensource.turmeric.security.v1.services.UpdatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.UpdatePolicyResponse;
import org.ebayopensource.turmeric.test.services.utils.FindPolicyHelper;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class UpdatePolicyTests{
	private String m_testCaseName;
	private String m_policyInfo;
	private String m_subjects;
	private String m_subjectGroups;
	private String m_resources;
	private String m_updateMode;
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
	private static final String CONFIG_DELIMITER = ":";
	private static final String m_PropFilePath = "UpdatePolicyTests.properties";
	private static final String m_PreRequisitePoliciesFilePath = "PreRequisitePolicies.properties";
	
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

	public UpdatePolicyTests(String testCaseName, String policyInfo, String subjects,
			String subjectGroups, String resources, String updateMode, String expectedResult) {
		this.m_testCaseName =testCaseName;
		this.m_policyInfo = policyInfo;
		this.m_subjects = subjects;
		this.m_subjectGroups = subjectGroups;
		this.m_resources = resources;
		this.m_updateMode = updateMode;
		this.m_expectedResult = expectedResult;
	}
	
	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data(){
		return loadUpdatePolicyInputData();
	}
	
    @Test
	public void updatePolicy() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + m_testCaseName + " *** ");
		UpdatePolicyRequest updatePolicyRequest = new UpdatePolicyRequest();
		UpdatePolicyResponse response = new UpdatePolicyResponse();
		try {
			constructUpdatePolicyRequest(updatePolicyRequest);
			response = PolicyServiceTestHelper.getInstance().updatePolicy(updatePolicyRequest);
			String errorMessage = response.getErrorMessage() != null ? response
					.getErrorMessage().getError().get(0).getMessage() : null;
			StringTokenizer policyTokens = new StringTokenizer(m_expectedResult,"|");
			String expectedAckValue = getToken(policyTokens);
			String expectedErrorMessage = getToken(policyTokens);
			if (expectedAckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				// To DO
				/*int actualPolicies = response.get().getPolicy().size();
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
				} */
			} else {
				assertEquals(errorMessage, AckValue.FAILURE, response.getAck());
				assertNotNull(errorMessage, response.getErrorMessage());
				assertEquals(errorMessage, expectedErrorMessage,
						errorMessage);
			}
	
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
		System.out.println("*** Test Scenario : " + m_testCaseName + " completed successfully ***");
	}
		
	@SuppressWarnings("unchecked")
	public static Collection loadUpdatePolicyInputData() {
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		Pattern pattern =Pattern.compile("testcase(\\d*).name");
		int totalTests = 0;
		try {
			InputStream inputStream = UpdatePolicyTests.class.getResourceAsStream(m_PropFilePath);
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
				String policyKey = "testcase" + i + ".request.policyInfo";
				String subjectKey = "testcase" + i + ".request.subjects";
				String subjectGroupKey = "testcase" + i + ".request.subjectgroups";
				String resourceKey = "testcase" + i + ".request.resources";
				String updateMode = "testcase" + i +".updatemode";
				String expectedResult = "testcase" + i + ".response";
				eachRowData.add(props.getProperty(testName));
				eachRowData.add(props.getProperty(policyKey));
				eachRowData.add(props.getProperty(subjectKey));
				eachRowData.add(props.getProperty(subjectGroupKey));
				eachRowData.add(props.getProperty(resourceKey));
                eachRowData.add(props.getProperty(updateMode));
				eachRowData.add(props.getProperty(expectedResult));
				list.add(eachRowData.toArray());
			}
		} catch (IOException e) {}
		
		return list;
	}
	
	@SuppressWarnings("unchecked")
	private static void createPreRequisiteData() {
		Properties policiesProps = new Properties();
		Pattern pattern =Pattern.compile("policy(\\d*).policyInfo");
		int totalPolicies = 0;
		try {
			InputStream inputStream = UpdatePolicyTests.class.getResourceAsStream(m_PreRequisitePoliciesFilePath);
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
		
	private void constructUpdatePolicyRequest(UpdatePolicyRequest updatePolicyRequest) throws Exception {
		Policy policy = new Policy();
		Target target = new Target();
		Subjects subjects = new Subjects();
		Resources resources = new Resources();
		if (m_policyInfo!=null) {
			StringTokenizer policyTokens = new StringTokenizer(m_policyInfo,CONFIG_DELIMITER);
			String policyType = getToken(policyTokens);
			String policyName = getToken(policyTokens);
			String policyDesc = getToken(policyTokens);
			Long id = null;
			int idx = (policyName==null) ? -1 : policyName.indexOf('@');
			if (idx >= 0) {
			    String oldName = policyName.substring(0, idx);
                policyName = policyName.substring(++idx);
                id = m_policyIds.get(oldName);
                m_policyIds.put(policyName, id);
			} else {
                id = m_policyIds.get(policyName);
			}
			idx = (policyDesc==null) ? -1 : policyDesc.indexOf('@');
			if (idx >= 0) {
			    policyDesc = policyDesc.substring(++idx);
			}
			policy.setPolicyId(id);
			policy.setPolicyType(policyType);
            policy.setPolicyName(policyName);
            policy.setDescription(policyDesc);
			System.out.println("\t  policyType= "+policyType+" , policyName = "+policyName);
		}
		if (m_subjects!=null) {
			Subject subject = new Subject();
			StringTokenizer subjetsTokens = new StringTokenizer(m_subjects,";");
			while (subjetsTokens.hasMoreTokens()) {
				String subjectInfo  =getToken(subjetsTokens);
				StringTokenizer subjectTokens = new StringTokenizer(subjectInfo,CONFIG_DELIMITER);
				String subjectType =getToken(subjectTokens);
				String subjectName= getToken(subjectTokens);
				subject.setSubjectType(subjectType);
				subject.setSubjectName(subjectName);
				System.out.println("\t  subjectType= "+subjectType+" , subjectName = "+subjectName);
				subjects.getSubject().add(subject);
			}
		}
		if (m_subjectGroups!=null) {
			SubjectGroup subjectGroup = new SubjectGroup();
			StringTokenizer subjGroupTokens = new StringTokenizer(m_subjectGroups,";");
			while(subjGroupTokens.hasMoreTokens()) {
				String subjectGroupInfo  =getToken(subjGroupTokens);	
				StringTokenizer subjGroupInfo = new StringTokenizer(subjectGroupInfo,CONFIG_DELIMITER);
				String subjectType =getToken(subjGroupInfo);
				String subjectGroupName= getToken(subjGroupInfo);
				subjectGroup.setSubjectType(subjectType);
				subjectGroup.setSubjectGroupName(subjectGroupName);
				System.out.println("\t  subjectType= "+subjectType+" , subjectGroupName = "+subjectGroupName);
				subjects.getSubjectGroup().add(subjectGroup);
			}
			
		}
		if (m_resources!=null) {
		
			StringTokenizer resourcesTokens = new StringTokenizer(m_resources,";");
			while(resourcesTokens.hasMoreTokens()) {
				Resource resource = new Resource();
				String resourceInfo =getToken(resourcesTokens);
				StringTokenizer resourceTokens = new StringTokenizer(resourceInfo,CONFIG_DELIMITER);
				String resourceType =getToken(resourceTokens);
				String resourceName= getToken(resourceTokens);
				resource.setResourceType(resourceType);
				resource.setResourceName(resourceName);
				System.out.println("\t   resourceType= "+resourceType+" , resourceName = "+resourceName);
				while (resourceTokens.hasMoreTokens()) {
					Operation op = new Operation();
					String opName =getToken(resourceTokens);
					op.setOperationName(opName);
					op.setResourceId(m_resourceIds.get(resourceName));
					resource.getOperation().add(op);
				}
					resources.getResource().add(resource);
			}
		}
		
		target.setSubjects(subjects);
		target.setResources(resources);
		policy.setTarget(target);
		updatePolicyRequest.setPolicy(policy);
		updatePolicyRequest.setUpdateMode(UpdateMode.fromValue(m_updateMode));
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
