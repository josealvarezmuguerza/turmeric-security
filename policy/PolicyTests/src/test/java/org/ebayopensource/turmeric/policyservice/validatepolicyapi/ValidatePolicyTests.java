/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/

package org.ebayopensource.turmeric.policyservice.validatepolicyapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.ebayopensource.turmeric.security.v1.services.Condition;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.EffectType;
import org.ebayopensource.turmeric.security.v1.services.Expression;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PrimitiveValue;
import org.ebayopensource.turmeric.security.v1.services.Query;
import org.ebayopensource.turmeric.security.v1.services.QueryCondition;
import org.ebayopensource.turmeric.security.v1.services.Resolution;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Resources;
import org.ebayopensource.turmeric.security.v1.services.Rule;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.Subjects;
import org.ebayopensource.turmeric.security.v1.services.SupportedPrimitive;
import org.ebayopensource.turmeric.security.v1.services.Target;
import org.ebayopensource.turmeric.security.v1.services.ValidatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.ValidatePolicyResponse;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ValidatePolicyTests{

	private static final String CONFIG_DELIMITER = ":";
	private String m_testCaseName;
	private String m_policyInfo;
	private String m_resources;
	private String m_includeSubjects;
	private String m_policyEffect;
	private String m_ruleCondition;
	private String m_queryCondition;
	private String m_expectedResult;
	private static Properties props = new Properties();
	private static Map<String,Long> m_subjectIds = new HashMap<String,Long>();
	private static Map<String,Long> m_resourceIds = new HashMap<String,Long>();
	private static List<Resource> m_resourceList = new ArrayList<Resource>();
	private static List<Subject> m_subjList = new ArrayList<Subject>();
	private static final String s_PropFilePath = "ValidatePolicyTests.properties";
	
	@BeforeClass
	public static void setUp() throws Exception {
		System.out.println(" *** Create Pre-Requisite data ***");
		createPreRequisiteData();
		System.out.println(" *** Create Pre-Requisite data has been completed successfully ***");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		cleanUpResources();
		cleanUpSubjects();
	}

	public ValidatePolicyTests(String testCaseName, String policyInfo,
			String resources, String includeSubjects,String policyEffect,
			String ruleCondition,String queryCondition, String expectedResult) {
		this.m_testCaseName = testCaseName;
		this.m_policyInfo = policyInfo;
		this.m_resources = resources;
		this.m_includeSubjects = includeSubjects;
		this.m_policyEffect = policyEffect;
		this.m_ruleCondition = ruleCondition;
		this.m_queryCondition = queryCondition;
		this.m_expectedResult = expectedResult;
	}

	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data(){
		return loadValidatePolicyData();
	}
	
    @Test
	public void testvalidatePolicy() throws Exception {
    	System.out.println("\n*** Test Scenario :  " + m_testCaseName + " *** ");
		ValidatePolicyRequest validatePolicyRequest = new ValidatePolicyRequest();
		ValidatePolicyResponse response = new ValidatePolicyResponse();
		try {
			constructValidatePolicyRequest(validatePolicyRequest);
			response = PolicyServiceTestHelper.getInstance().validatePolicy(validatePolicyRequest);
			String errorMessage = response.getErrorMessage() != null ? response
					.getErrorMessage().getError().get(0).getMessage() : null;
			StringTokenizer resTokens = new StringTokenizer(m_expectedResult,"|");
			String expectedAckValue = getToken(resTokens);
			String expectedErrorMessage = getToken(resTokens);
			String validationStatus = getToken(resTokens);
			
			if (expectedAckValue.equalsIgnoreCase("success")) {
				assertEquals(errorMessage, AckValue.SUCCESS, response.getAck());
				assertNull(errorMessage, response.getErrorMessage());
				assertTrue(Boolean.parseBoolean(validationStatus));
			} else {
				assertEquals(errorMessage, AckValue.FAILURE, response.getAck());
				assertNotNull(errorMessage, response.getErrorMessage());
				assertEquals(errorMessage, expectedErrorMessage,errorMessage);
				assertFalse(Boolean.parseBoolean(validationStatus));
			}
	
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e.getMessage(), false);
		} 
		System.out.println("*** Test Scenario : " + m_testCaseName + " completed successfully ***");
	}

		
	@SuppressWarnings("unchecked")
	public static Collection loadValidatePolicyData() {
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		Pattern pattern =Pattern.compile("testcase(\\d*).name");
		int totalTests = 0;
		try {
			InputStream inputStream = ValidatePolicyTests.class.getResourceAsStream(s_PropFilePath);
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
				String policyinfo = "testcase" + i + ".request.policyInfo";
				String resources = "testcase" + i + ".request.resources";
				String includeSubjects = "testcase" + i + ".request.includeSubjects";
				String policyEffect = "testcase" + i + ".request.policyEffect";
				String ruleCondition = "testcase" + i + ".request.rulecondition";
				String queryCondition = "testcase" + i + ".request.querycondition";
				String expectedResult = "testcase" + i + ".response";
				eachRowData.add(props.getProperty(testName));
				eachRowData.add(props.getProperty(policyinfo));
				eachRowData.add(props.getProperty(resources));
				eachRowData.add(props.getProperty(includeSubjects));
				eachRowData.add(props.getProperty(policyEffect));
				eachRowData.add(props.getProperty(ruleCondition));
				eachRowData.add(props.getProperty(queryCondition));
				eachRowData.add(props.getProperty(expectedResult));
				list.add(eachRowData.toArray());
			}
		} catch (IOException e) {	}
		
		return list;
	}
	
	private static void createPreRequisiteData() {
		String subjects = props.getProperty("testcase.prerequisite.subjects");
		String resources = props.getProperty("testcase.prerequisite.resources");
		createSubjects(subjects);
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
				for ( Long id :resourceIds) {
					m_resourceIds.put(resourceList.get(i++).getResourceName(),id);
				}
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
	
	private static String getToken(StringTokenizer tokenizer)
	{
		if(tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken().trim();
			return "null".equals(token) ? null : token;
		} 
		return null;
	}
		
	private void  constructValidatePolicyRequest(ValidatePolicyRequest validatePolicyRequest) throws Exception {
	
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
			System.out.println("policyType = "+ policyType+ ", policyname = "+ policyName+" ,policyDesc = "+ policyDesc);
		}
		if (m_resources!=null) {
			StringTokenizer resList = new StringTokenizer(m_resources,";");
			Resource resource = new Resource();
			Resources resources = new Resources();
			
			while (resList.hasMoreTokens()) {
				resource = new Resource();
				String nextToken = resList.nextToken();
				StringTokenizer resourceTokens = new StringTokenizer(nextToken,CONFIG_DELIMITER);
				String resType =getToken(resourceTokens);
				String resName= getToken(resourceTokens);
				resource.setResourceName(resName);
				resource.setResourceType(resType);
				resources.getResource().add(resource);
				System.out.println("resourceType = "+ resType+ ", resourceName = "+ resName);
				while (resourceTokens.hasMoreElements()) {
					Operation operation = new Operation();
					operation.setOperationName(getToken(resourceTokens));
					resource.getOperation().add(operation);
					System.out.print("\n\t Operation name = "+operation.getOperationName());
				}
				
			}
			target.setResources(resources);
		}
		if (m_includeSubjects!=null) {
			StringTokenizer subjList = new StringTokenizer(m_includeSubjects,";");
			Subject subject = new Subject();
			Subjects subjects = new Subjects();
			while (subjList.hasMoreTokens()) {
				String nextToken = subjList.nextToken();
				StringTokenizer subjTokens = new StringTokenizer(nextToken,CONFIG_DELIMITER);
				String subjType =getToken(subjTokens);
				String subjName= getToken(subjTokens);
				long subjectId = m_subjectIds.get(subjName)!=null ?m_subjectIds.get(subjName) : -1;
				subject = PolicyServiceTestHelper.getInstance().mapIncludedSubject(subjectId);
				subject.setSubjectName(subjName);
				subject.setSubjectType(subjType);
				subjects.getSubject().add(subject);
			}
			target.setSubjects(subjects);
		}
					
		System.out.println("resources "+ m_resources);
		getPolicyEffect(policy,m_policyEffect,m_ruleCondition);
 		if (m_queryCondition!=null) {
 			StringTokenizer queryList = new StringTokenizer(m_queryCondition,":");
 			String queryType = getToken(queryList);
 			String queryValue = getToken(queryList);
 			Query query = new Query();
 			query.setQueryType(queryType); // Effect/SubjectSearchScope/MaskedIds/ActivePoliciesOnly
 			if(queryValue != null) {
 				query.setQueryValue(queryValue); // BLOCK|FLAG|CHALLENGE|ALLOW/TARGET|EXCLUDED|BOTH/TRUE|FALSE/TRUE|FALSE
 			}
 			QueryCondition queryCondition = new QueryCondition();
 			queryCondition.setResolution(Resolution.AND);
 			queryCondition.getQuery().add(query);
 			validatePolicyRequest.setScope(queryCondition);
 			
 		}
 		validatePolicyRequest.setPolicy(policy);
 		
	}

	private void getPolicyEffect(Policy policy, String policyEffect,String ruleCondition) {
		long rollover = 0L;
		Rule rule = new Rule();
		String rCondition = new String();
		Condition condition = new Condition();
		Expression expression = new Expression();
		PrimitiveValue primitiveValue = new PrimitiveValue();
		
		if (policyEffect!=null){
			StringTokenizer policyeffectTokens = new StringTokenizer(policyEffect,CONFIG_DELIMITER);
			String effectType = getToken(policyeffectTokens);
			String effectDuration = getToken(policyeffectTokens);
			String rolloverperiod = getToken(policyeffectTokens);
			if (policyEffect!=null){
			StringTokenizer ruleTokens = new StringTokenizer(ruleCondition,CONFIG_DELIMITER);
			String ruleName = getToken(ruleTokens);
			rCondition = getToken(ruleTokens);
			rule.setRuleName(ruleName);
			}
			if (rolloverperiod!=null) {
					rollover = Long.parseLong(rolloverperiod);
			}
			rule.setRolloverPeriod(rollover);
				rollover = 0L;
			if (effectDuration!=null) {
				rollover = Long.parseLong(effectDuration);
			}
				rule.setEffectDuration(rollover);
				primitiveValue.setValue(rCondition);
				primitiveValue.setType(SupportedPrimitive.STRING);
				expression.setPrimitiveValue(primitiveValue);
				condition.setExpression(expression);
				rule.setCondition(condition);
				rule.setPriority(0);
				rule.setDescription("Desc ");
			
			rule.setEffect(getEffectType(effectType));
		 }
		policy.getRule().add(rule);
	}
	
	private EffectType getEffectType(String effectType) {
		if (effectType.equalsIgnoreCase("flag")) {
			return EffectType.FLAG;
		} else if (effectType.equalsIgnoreCase("allow")) {
			return EffectType.ALLOW;
		}
		return null;
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
	
}
