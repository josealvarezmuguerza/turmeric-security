/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.test.services.authorizationservice;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.ebayopensource.turmeric.security.v1.services.CreatePolicyRequest;
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
import org.ebayopensource.turmeric.test.services.authorizationhandler.AuthorizationHandlerTests;
import org.ebayopensource.turmeric.test.services.utils.CommonUtils;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public abstract class AbstractAuthorizationTestClass extends CommonUtils {

	public static final String CONFIG_DELIMITER = ":";	
	public static Properties props = new Properties();
	public static Map<String, Long> m_subjectIds = new HashMap<String, Long>();
	public static Map<String, Long> m_subjectGroupIds = new HashMap<String, Long>();
	public static Map<String, Long> m_resourceIds = new HashMap<String, Long>();
	public static List<Resource> m_resourceList = new ArrayList<Resource>();
	public static List<SubjectGroup> m_subjGroupList = new ArrayList<SubjectGroup>();
	public static List<Subject> m_subjList = new ArrayList<Subject>();
	private static final String s_PropFilePath = "org/ebayopensource/turmeric/test/services/authorizationhandler/AuthorizationHandlerTests.properties";
	private String m_globalSubjectDomainsList;
	public String m_includeSubjects;
	public String m_includeSubjectGroups;
	public String m_excludeSubjects;
	public String m_excludeSubjectGroups;
	public String m_policyEffect;
	private String m_expectedResult;
	public String m_policyInfo;
	public String m_resources;
	
	protected static void createPreRequisiteData() throws Exception {
		String subjects = props.getProperty("testcase.prerequisite.subjects");
		String subjectsGroups = props
				.getProperty("testcase.prerequisite.subjectgroups");
		String resources = props.getProperty("testcase.prerequisite.resources");
		createSubjects(subjects);
		createSubjectGroups(subjectsGroups);
		createResources(resources);
	}

	private static void createResources(String resources) throws Exception {
		CreateResourcesRequest createResourcesRequest = new CreateResourcesRequest();
		CreateResourcesResponse createResourcesResponse = new CreateResourcesResponse();
		List<Resource> resourceList = createResourcesRequest.getResources();
		
		StringTokenizer resTokens = new StringTokenizer(resources, ";");
		while (resTokens.hasMoreTokens()) {
			String token = resTokens.nextToken();
			constructCreateResourceRequest(resourceList, token);
		}
		m_resourceList = resourceList;
		createResourcesResponse = PolicyServiceTestHelper.getInstance()
				.createResources(createResourcesRequest);
		if (createResourcesResponse.getResourceIds() != null) {
			List<Long> resourceIds = createResourcesResponse.getResourceIds();
			int i = 0;
			for (Long id : resourceIds)
				m_resourceIds.put(resourceList.get(i++).getResourceName(), id);
	
		}
	}

	private static void constructCreateResourceRequest(List<Resource> resList,
			String token) {
				Resource resource = new Resource();
				StringTokenizer resTokens = new StringTokenizer(token, CONFIG_DELIMITER);
				resource.setResourceType(getToken(resTokens));
				resource.setResourceName(getToken(resTokens));
				List<Operation> operations = resource.getOperation();
				Operation operation = new Operation();
				while (resTokens.hasMoreTokens()) {
					String opsToken = getToken(resTokens);
					if (opsToken != null) {
						operation = new Operation();
						operation.setOperationName(opsToken);
					}
					operations.add(operation);
				}
				resList.add(resource);
			}

	private static void createSubjectGroups(String subjectsGroups) throws Exception {
	
		if (subjectsGroups == null) {
			return;
		}
	
		CreateSubjectGroupsRequest createSubjectGroupsRequest = new CreateSubjectGroupsRequest();
		CreateSubjectGroupsResponse createSubjectGroupsResponse = new CreateSubjectGroupsResponse();
		List<SubjectGroup> subjGroupList = createSubjectGroupsRequest
				.getSubjectGroups();
	
		StringTokenizer subjTokens = new StringTokenizer(subjectsGroups, ";");
		while (subjTokens.hasMoreTokens()) {
			String token = subjTokens.nextToken();
			constructCreateSubjectGroupsRequest(subjGroupList, token);
		}
		m_subjGroupList = subjGroupList;
		createSubjectGroupsResponse = PolicyServiceTestHelper.getInstance()
				.createSubjectGroups(createSubjectGroupsRequest);
	
		if (createSubjectGroupsResponse.getSubjectGroupIds() != null) {
			List<Long> subjectGroupIds = createSubjectGroupsResponse
					.getSubjectGroupIds();
			int i = 0;
			for (Long id : subjectGroupIds) {
				m_subjectGroupIds.put(subjGroupList.get(i++)
						.getSubjectGroupName(), id);
			}
		}
	
	}

	private static void constructCreateSubjectGroupsRequest(List<SubjectGroup> subjGroupList,
			String token) {
				SubjectGroup subjectGroup = new SubjectGroup();
				StringTokenizer subjGroupTokens = new StringTokenizer(token,
						CONFIG_DELIMITER);
				subjectGroup.setSubjectType(getToken(subjGroupTokens));
				subjectGroup.setSubjectGroupName(getToken(subjGroupTokens));
				subjectGroup.setDescription(getToken(subjGroupTokens));
				System.out.println(" > SubjectGroup Name = "
						+ subjectGroup.getSubjectGroupName() + " ; Type = "
						+ subjectGroup.getSubjectGroupName());
				// clean up if subjectgroup already exists
				PolicyServiceTestHelper.getInstance().cleanupSubjectGroup(
						subjectGroup.getSubjectGroupName(),
						subjectGroup.getSubjectType());
				List<Subject> subjList = subjectGroup.getSubject();
				Subject subject = new Subject();
				while (subjGroupTokens.hasMoreTokens()) {
					String subjectName = getToken(subjGroupTokens);
					subject = new Subject();
					subject.setSubjectType(subjectGroup.getSubjectType());
					subject.setSubjectName(subjectName);
					subjList.add(subject);
				}
				subjGroupList.add(subjectGroup);
			
			}

	private static void createSubjects(String subjects) {
		try {
			if (subjects != null) {
				StringTokenizer subjsList = new StringTokenizer(subjects, ";");
				while (subjsList.hasMoreTokens()) {
					String token = subjsList.nextToken();
					StringTokenizer subjTokens = new StringTokenizer(token,
							CONFIG_DELIMITER);
					Subject subject = new Subject();
					subject.setSubjectType(getToken(subjTokens));
					subject.setSubjectName(getToken(subjTokens));
					System.out.println(" > Subject Name = "
							+ subject.getSubjectName() + " ; Type = "
							+ subject.getSubjectType());
					m_subjList.add(subject);
					long subjectId = PolicyServiceTestHelper.getInstance()
							.findSubjects(subject.getSubjectType(),
									subject.getSubjectName());
					if (subjectId > 0)
						m_subjectIds.put(subject.getSubjectName(), subjectId);
					else {
						List<Long> subjIdList = PolicyServiceTestHelper
								.getInstance().createSubjects(
										subject.getSubjectType(),
										subject.getSubjectName());
						if (subjIdList != null && subjIdList.size() > 0)
							m_subjectIds.put(subject.getSubjectName(),
									subjIdList.get(0));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		}
	}

	private static String getToken(StringTokenizer tokenizer) {
		if (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken().trim();
			return "null".equals(token) ? null : token;
		}
		return null;
	}

	protected static void cleanUpResources() {
		for (Resource resource : m_resourceList) {
			PolicyServiceTestHelper.getInstance().cleanUpResource(
					resource.getResourceName(), resource.getResourceType());
		}
	
	}

	protected static void cleanUpSubjects() {
		for (Subject subject : m_subjList) {
			PolicyServiceTestHelper.getInstance().cleanUpSubjects(
					subject.getSubjectType(), subject.getSubjectName());
		}
	}

	protected static void cleanUpSubjectGroups() {
		for (SubjectGroup subjectGroup : m_subjGroupList) {
			PolicyServiceTestHelper.getInstance().cleanupSubjectGroup(
					subjectGroup.getSubjectGroupName(),
					subjectGroup.getSubjectType());
		}
	
	}

	@BeforeClass
	public static void setUpOnce() throws Exception {
		System.out.println(" *** Create Pre-Requisite data ***");
		loadCreatePolicyData();
		createPreRequisiteData();
		System.out
				.println(" *** Create Pre-Requisite data has been completed successfully ***");
	}

	@AfterClass
	public static void tearDownAfter() throws Exception {
		cleanUpResources();
		cleanUpSubjectGroups();
		cleanUpSubjects();
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

	@SuppressWarnings("unchecked")
	public static void loadCreatePolicyData() {
		List list = new ArrayList();
		try {
			InputStream inputStream = AuthorizationHandlerTests.class
					.getResourceAsStream(s_PropFilePath);
			if (inputStream == null) {
				AuthorizationHandlerTests test = new AuthorizationHandlerTests();
				inputStream = test.getInputStream(s_PropFilePath);
			}
			props.load(inputStream);
		} catch (IOException e) {
		}

	}

	public InputStream getInputStream(String a) {
		InputStream is = getClass().getResourceAsStream(a);

		if (is == null) {
			is = ClassLoader.getSystemResourceAsStream(a);
		}
		if (is == null) {
			Thread.currentThread().getContextClassLoader();
			URL resource = ClassLoader
					.getSystemClassLoader().getResource(a);
			try {
				is = new FileInputStream(new File(resource.toExternalForm()));
			} catch (Exception e) {
			}
		}

		return is;
	}	
}
