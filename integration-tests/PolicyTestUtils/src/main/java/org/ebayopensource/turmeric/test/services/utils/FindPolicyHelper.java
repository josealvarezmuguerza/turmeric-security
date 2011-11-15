/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.test.services.utils;

import java.util.List;
import java.util.StringTokenizer;

import org.ebayopensource.turmeric.security.v1.services.Condition;
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.EffectType;
import org.ebayopensource.turmeric.security.v1.services.Expression;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PrimitiveValue;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Resources;
import org.ebayopensource.turmeric.security.v1.services.Rule;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.Subjects;
import org.ebayopensource.turmeric.security.v1.services.Target;

public class FindPolicyHelper {
	private static final String CONFIG_DELIMITER = ":";
	private static final String CONFIG_RULE_DELIMITER = "$";
	
	
	public  CreatePolicyRequest constructCreatePolicyRequest(String policyInfo,
			String resourcesInfo, String globalsubjectdomainsList,
			String includeSubjects, String excludeSubjects,
			String includeSubjectGroups, String excludeSubjectGroups,
			String policyEffect,String ruleCondition) throws Exception {
		
		CreatePolicyRequest request = new CreatePolicyRequest();
 		Policy policy = new Policy();
 		Target target = new Target();
        Subjects subjects = new Subjects();
        boolean haveSubjects = false;
 		if (policyInfo!=null) {
			StringTokenizer policyTokens = new StringTokenizer(policyInfo,CONFIG_DELIMITER);
			String policyType =getToken(policyTokens);
			String policyName= getToken(policyTokens);
			String policyDesc =getToken(policyTokens);
			policy.setPolicyName(policyName);
	 		policy.setPolicyType(policyType);
	 		policy.setDescription(policyDesc);
	 		PolicyServiceTestHelper.getInstance().cleanupPolicy(policyName, policyType);
			System.out.println("policyType = "+ policyType+ ", policyname = "+ policyName+" ,policyDesc = "+ policyDesc);
		}
		if (resourcesInfo!=null) {
			StringTokenizer resList = new StringTokenizer(resourcesInfo,";");
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
		if (includeSubjects!=null) {
			StringTokenizer subjectsTokens = new StringTokenizer(includeSubjects,";");
			Subject subject = new Subject();
			List<Subject> subjList = subjects.getSubject();
			while (subjectsTokens.hasMoreTokens()) {
				String nextToken = subjectsTokens.nextToken();
				StringTokenizer subjTokens = new StringTokenizer(nextToken,CONFIG_DELIMITER);
				String subjType =getToken(subjTokens);
				String subjName= getToken(subjTokens);
				long subjectId = PolicyServiceTestHelper.getInstance().findSubjects(subjType, subjName);
				subject = PolicyServiceTestHelper.getInstance().mapIncludedSubject(subjectId);
				subject.setSubjectName(subjName);
				subject.setSubjectType(subjType);
				subjList.add(subject);
			}
			haveSubjects = true;
		}
		if (excludeSubjects!=null) {
			StringTokenizer subjectsTokens = new StringTokenizer(excludeSubjects,";");
			Subject subject = new Subject();
			List<Subject> subjList = subjects.getSubject();
			while (subjectsTokens.hasMoreTokens()) {
				String nextToken = subjectsTokens.nextToken();
				StringTokenizer subjTokens = new StringTokenizer(nextToken,CONFIG_DELIMITER);
				String subjType =getToken(subjTokens);
				String subjName= getToken(subjTokens);
				long subjectId = PolicyServiceTestHelper.getInstance().findSubjects(subjType, subjName);
				subject = PolicyServiceTestHelper.getInstance().mapExcludedSubject(subjectId);
				subject.setSubjectName(subjName);
				subject.setSubjectType(subjType);
				subjList.add(subject);
			}
            haveSubjects = true;
		}
		
		if (includeSubjectGroups!=null) {
			StringTokenizer subjGrpList = new StringTokenizer(includeSubjectGroups,";");
			List<SubjectGroup> subjectGroupList = subjects.getSubjectGroup();
			SubjectGroup subjectGroup = new SubjectGroup();
			while (subjGrpList.hasMoreTokens()) {
				String nextToken = subjGrpList.nextToken();
				StringTokenizer subjGrpTokens = new StringTokenizer(nextToken,CONFIG_DELIMITER);
				String subjType =getToken(subjGrpTokens);
				String subjGrpName= getToken(subjGrpTokens);
				long subjectGroupId = PolicyServiceTestHelper.getInstance().findSubjectGroup(subjType, subjGrpName);
				subjectGroup = PolicyServiceTestHelper.getInstance().mapIncludedSubjectGroup(subjectGroupId);
				subjectGroup.setSubjectGroupName(subjGrpName);
				subjectGroup.setSubjectType(subjType);
				subjectGroupList.add(subjectGroup);
			}
            haveSubjects = true;
		}
		if (excludeSubjectGroups!=null) {
			StringTokenizer subjGrpList = new StringTokenizer(excludeSubjectGroups,";");
			SubjectGroup subjectGroup = new SubjectGroup();
			List<SubjectGroup> subjectGroupList = subjects.getSubjectGroup();
			while (subjGrpList.hasMoreTokens()) {
				String nextToken = subjGrpList.nextToken();
				StringTokenizer subjGrpTokens = new StringTokenizer(nextToken,CONFIG_DELIMITER);
				String subjType =getToken(subjGrpTokens);
				String subjGrpName= getToken(subjGrpTokens);
				long subjectGroupId = PolicyServiceTestHelper.getInstance().findSubjectGroup(subjType, subjGrpName);
				//long subjectGroupId = m_subjectGroupIds.get(subjGrpName)!=null ?m_subjectGroupIds.get(subjGrpName) : -1;
				subjectGroup = PolicyServiceTestHelper.getInstance().mapExcludedSubjectGroup(subjectGroupId);
				subjectGroup.setSubjectGroupName(subjGrpName);
				subjectGroup.setSubjectType(subjType);
				subjectGroupList.add(subjectGroup);
			}
            haveSubjects = true;
		}
		if (haveSubjects) {
		    target.setSubjects(subjects);
		}
		
		if (policyEffect!=null) {
			getPolicyEffect(policy,policyEffect,ruleCondition);
		}
		policy.setTarget(target);
		request.setPolicy(policy);
 		return request;
		
	}
	
	public CreatePolicyRequest constructCreatePolicyRequest(String policyInfo,
			String resourcesInfo, String globalsubjectdomainsList,
			String includeSubjects, String excludeSubjects,
			String includeSubjectGroups, String excludeSubjectGroups,
			String policyEffect) throws Exception {
		
		return constructCreatePolicyRequest(policyInfo, resourcesInfo,
				globalsubjectdomainsList, includeSubjects,
				excludeSubjects, includeSubjectGroups,
				excludeSubjectGroups, policyEffect,null);
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
			if (effectType!=null) {
				effectType = getToken(policyeffectTokens);
				rule.setEffect(getEffectType(effectType));	
			}
			
			String effectDuration = getToken(policyeffectTokens);
			String rolloverperiod = getToken(policyeffectTokens);
			if (ruleCondition!=null) {
				StringTokenizer ruleTokens = new StringTokenizer(ruleCondition,CONFIG_RULE_DELIMITER);
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
				expression.setPrimitiveValue(primitiveValue);
				condition.setExpression(expression);
				rule.setCondition(condition);
				rule.setPriority(0);
				rule.setDescription("Desc ");
		 }
		policy.getRule().add(rule);
	}
	
	private EffectType getEffectType(String effectType) {
		
		if (effectType.equalsIgnoreCase("BLOCK")) {
			return EffectType.BLOCK;
		} else if (effectType.equalsIgnoreCase("CHALLENGE")) {
			return EffectType.CHALLENGE;
		} else if (effectType.equalsIgnoreCase("FLAG")) {
			return EffectType.FLAG;
		} else  {
			return EffectType.ALLOW;
		}
				
	}
	
	private static String getToken(StringTokenizer tokenizer) {
		if(tokenizer.hasMoreTokens()){
			String token = tokenizer.nextToken().trim();
			return "null".equals(token) ? null : token;
		} 
		return null;
	}
	
	
	public  static void constructCreateResourceRequest(
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

	public static void constructCreatePolicyRequest(Policy policy, String token) {
		StringTokenizer policyInfo = new StringTokenizer(token,"|");
		Resources resources = new Resources();
		String policDetails = getToken(policyInfo);
		String resourcesDetails = getToken(policyInfo);
		
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
		Target target = new Target();
		target.setResources(resources);
		policy.setTarget(target);
	}

	public static void constructCreateSubjectGroupsRequest(
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
	
		
}
