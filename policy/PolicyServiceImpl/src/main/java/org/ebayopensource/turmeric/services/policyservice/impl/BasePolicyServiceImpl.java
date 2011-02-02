/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.policyservice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.ebayopensource.turmeric.errorlibrary.turmericpolicy.ErrorConstants;

import org.ebayopensource.turmeric.policyservice.exceptions.PolicyFinderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException;
import org.ebayopensource.turmeric.policyservice.provider.PolicyTypeProvider;
import org.ebayopensource.turmeric.policyservice.provider.ResourceTypeProvider;
import org.ebayopensource.turmeric.policyservice.provider.SubjectTypeProvider;
import org.ebayopensource.turmeric.policyservice.provider.common.OperationEditObject;
import org.ebayopensource.turmeric.policyservice.provider.common.PolicyEditObject;
import org.ebayopensource.turmeric.policyservice.provider.common.ResourcesEditObject;
import org.ebayopensource.turmeric.policyservice.provider.common.RuleEditObject;
import org.ebayopensource.turmeric.policyservice.provider.common.SubjectGroupEditObject;
import org.ebayopensource.turmeric.policyservice.provider.common.SubjectsEditObject;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorUtils;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContextAccessor;
import org.ebayopensource.turmeric.runtime.common.security.SecurityContext;

import org.ebayopensource.turmeric.security.v1.services.EntityHistory;
import org.ebayopensource.turmeric.security.v1.services.GroupCalculatorInfo;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.OperationKey;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.Rule;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectTypeInfo;
import org.ebayopensource.turmeric.security.v1.services.Target;
import org.ebayopensource.turmeric.security.v1.services.UpdateMode;
import org.ebayopensource.turmeric.services.policyservice.provider.config.PolicyServiceProviderFactory;


class BasePolicyServiceImpl 
{
	 final int maxResourceNameLength = 128;
	 
	 
	 protected List<SubjectTypeInfo> getSubjectTypes() throws ServiceException, org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException
	 {	
		List<SubjectTypeInfo> retList = new ArrayList<SubjectTypeInfo>();
		Set<String> subjectTypes= PolicyServiceProviderFactory.getSubjectTypes();	
		for (String type: subjectTypes)
		{
			SubjectTypeProvider subjectTypeprovider = PolicyServiceProviderFactory.
					getSubjectTypeProvider(type);
			SubjectTypeInfo object = subjectTypeprovider.getSubjectTypeInfo();
			retList.add(object);
		}
		return retList;
	}
	 
	 protected List<String> getPolicyTypes() throws ServiceException
	 {	
		List<String> ret = new ArrayList<String>();
		 for (String type: PolicyServiceProviderFactory.getPolicyTypes())
			 ret.add(type);
	
		 return ret;	
	 }
	 
    protected Map<Long, Resource> getResource(ResourceKey key) throws ServiceException, PolicyProviderException
    {
    	Map<Long, Resource> result = new HashMap<Long, Resource>();
    	ResourceTypeProvider provider = PolicyServiceProviderFactory.getResourceTypeProvider(key.getResourceType()); 
    	Long resourceId = key.getResourceId();
    	String resourceName = key.getResourceName();
    	Resource resource = null;
    	if (resourceId != null)
    	{
    		resource = provider.getResourceInfoById(resourceId);
    		if (resource != null)
    			result.put(resource.getResourceId(), resource);
    	} else if (resourceName != null) {
    		resource = provider.getResourceInfoByName(resourceName);
    		if (resource != null)
    			result.put(resource.getResourceId(), resource);
    	} else {
    		Map<Long, Resource> map = provider.getResourceInfoByType();
    		if (map != null && !map.isEmpty())
    			result.putAll(map);
    	}
    		
    	Iterator<Entry<Long, Resource>> iter = result.entrySet().iterator();
    	while (iter.hasNext())
    	{
    		Entry<Long, Resource> entry = iter.next();
    		Resource res = entry.getValue();
    		provider = PolicyServiceProviderFactory.getResourceTypeProvider(res.getResourceType()); 
        	List<Operation> opList = provider.getOperationByResourceId(res.getResourceId());
        	if (opList != null)
        		res.getOperation().addAll(opList);
    	}
    		
    	return result;
    }
	
	protected Policy getPolicyInfo(PolicyKey policyKey)
		throws ServiceException, PolicyProviderException 
	{
		String policyName = policyKey.getPolicyName();
		Long policyId=  policyKey.getPolicyId();
		if ( policyName == null && policyId == null)
			throwInvalidInputException("Please input value for policy name or Id.");
		
		Policy work = null;
		String policyType = policyKey.getPolicyType();
		PolicyTypeProvider provider = PolicyServiceProviderFactory.getPolicyTypeProvider(policyType == null ? null : policyType);
		if (policyId != null)
			work = provider.getPolicyInfo(policyId);
		else
			work = provider.getPolicyInfo(policyName);
		
		if (work != null)
		{
			policyKey.setPolicyId(work.getPolicyId());
			policyKey.setPolicyName(work.getPolicyName());
		}
		
		return work;
	}
	
	private void constructSubjectRequest(
			Target target,
			List<SubjectTypeInfo> inputSubjectTypeList,
			List<SubjectKey> inputSubjectList,
			List<SubjectKey> inputExclusionSubjectList) throws ServiceException
	{
		if (target == null || target.getSubjects() == null)
			return;
		
		List<Subject> subjectList = target.getSubjects().getSubject();
		
		if (subjectList == null || subjectList.isEmpty())
			return;
		
		for (Subject subject: subjectList)
		{
			SubjectKey subjectKey = new SubjectKey();
			subjectKey.setSubjectId(Utils.getSubjectId(subject));
			subjectKey.setSubjectName(subject.getSubjectName());
			subjectKey.setSubjectType(subject.getSubjectType());
						
			if (Utils.isSubjectType(subject))
			{
				try {
					SubjectTypeProvider subjectTypeProvider = PolicyServiceProviderFactory.
						getSubjectTypeProvider(subject.getSubjectType());
				
					SubjectTypeInfo object = subjectTypeProvider.getSubjectTypeInfo();
					if (object != null)
						inputSubjectTypeList.add(object);
				} catch (PolicyProviderException e)
				{
					throwInvalidInputException("invalid input for subject type");
				}
			} else if (Utils.isExclusion(subject))
				inputExclusionSubjectList.add(subjectKey);
			else
				inputSubjectList.add(subjectKey);		
		}
	}
	
	private void constructSubjectGroupRequest(
			Target target,
			List<SubjectGroupKey> inputSubjectGroupList,
			List<SubjectGroupKey> inputExclusionSubjectGroupList)
	{
		if (target == null || target.getSubjects() == null)
			return;
		
		List<SubjectGroup> subjectGroupList = target.getSubjects().getSubjectGroup();
		
		if (subjectGroupList == null || subjectGroupList.isEmpty())
			return;
		
		for (SubjectGroup subjectGroup:subjectGroupList)
		{
			SubjectGroupKey subjectGroupKey = new SubjectGroupKey();
			subjectGroupKey.setSubjectGroupId(Utils.getSubjectGroupId(subjectGroup));
			subjectGroupKey.setSubjectGroupName(subjectGroup.getSubjectGroupName());
			subjectGroupKey.setSubjectType(subjectGroup.getSubjectType());
			if (Utils.isExclusion(subjectGroup))
				inputExclusionSubjectGroupList.add(subjectGroupKey);
			else
				inputSubjectGroupList.add(subjectGroupKey);
		}
	}
	
	private void constructResourceOperationRequest(
			Target target,
			List<ResourceKey> inputResourceList,
			List<OperationKey> inputOperationList)
	{
		if (target == null || target.getResources() == null)
			return;
		
		List<Resource> inputResource = target.getResources().getResource();
		
		if (inputResource == null || inputResource.isEmpty())
			return;
		
		for (Resource resource: inputResource)
		{
			if (resource.getOperation() == null) {
				ResourceKey resourceKey = new ResourceKey();
				resourceKey.setResourceId(resource.getResourceId());
				resourceKey.setResourceName(resource.getResourceName());
				resourceKey.setResourceType(resource.getResourceType());
				inputResourceList.add(resourceKey);
			} else 	{
				for (Operation op: resource.getOperation())
				{
					OperationKey operationKey = new OperationKey();
					operationKey.setOperationId(op.getOperationId());
					operationKey.setOperationName(op.getOperationName());
					operationKey.setResourceType(resource.getResourceType());
					operationKey.setResourceName(resource.getResourceName());
					inputOperationList.add(operationKey);
				}
			}
		}
	}
	protected SubjectGroupKey createSubjectGroup(SubjectGroup inputSubjectGroup,SubjectKey loginSubject)
		throws ServiceException, PolicyProviderException 
	{
		SubjectTypeProvider provider = PolicyServiceProviderFactory.
			getSubjectTypeProvider(inputSubjectGroup.getSubjectType());
		
		SubjectGroupEditObject subjectGroupEditObject = validateSubjectGroupRequest(
				provider,
				inputSubjectGroup, 
				null,
				UpdateMode.REPLACE);
		
		SubjectGroupKey subjectGroupKey = provider.createSubjectGroup( 
				inputSubjectGroup, 
				subjectGroupEditObject,
				loginSubject
				);
		
		return subjectGroupKey;
	}
	
	protected SubjectGroupKey updateSubjectGroup(
			SubjectGroup inputSubjectGroup, 
			SubjectGroup currSubjectGroup,
			UpdateMode updatemode,
			SubjectKey loginSubject)
		throws ServiceException, PolicyProviderException 
	{
		SubjectTypeProvider provider = PolicyServiceProviderFactory.
			getSubjectTypeProvider(inputSubjectGroup.getSubjectType());
		
		SubjectGroupEditObject subjectGroupEditObject = validateSubjectGroupRequest(
				provider,
				inputSubjectGroup, 
				currSubjectGroup,
				updatemode);
		
		SubjectGroupKey ret = provider.updateSubjectGroup(
				inputSubjectGroup, 
				subjectGroupEditObject,
				loginSubject);
	
		return ret;
	}
	protected PolicyKey createPolicy(Policy inputPolicy, SubjectKey loginSubject)
		throws ServiceException, PolicyProviderException 
	{
		String policyType = inputPolicy.getPolicyType();
		PolicyTypeProvider provider = PolicyServiceProviderFactory.getPolicyTypeProvider(policyType == null ? null : policyType);
		PolicyEditObject  policyEditObject = validatePolicyRequest(
				provider,
				inputPolicy, 
				null,
				UpdateMode.REPLACE
				);
		inputPolicy.setActive(Boolean.FALSE);
		PolicyKey policyKey = provider.createPolicy(
				inputPolicy, 
				policyEditObject,
				loginSubject
				);
		return policyKey;
	}
	
	protected void deletePolicy(PolicyKey policyKey) 
		throws ServiceException, PolicyProviderException 
	{
		Policy currPolicy = getPolicyInfo(policyKey);
		if (currPolicy == null)
			throwInvalidInputException("The given policy doesn't exist");
		
		String policyType = policyKey.getPolicyType();
		PolicyTypeProvider provider = PolicyServiceProviderFactory.getPolicyTypeProvider(policyType == null ? null : policyType);
		provider.deletePolicy(policyKey.getPolicyId());
	}
	
	protected PolicyKey updatePolicy(Policy inputPolicy, Policy currPolicy, UpdateMode updateMode, SubjectKey loginSubject) 
		throws ServiceException, PolicyProviderException 
	{
		if (inputPolicy.isActive() != null && 
			!inputPolicy.isActive().equals(currPolicy.isActive()))
			throwInvalidInputException("updatePolicy cann't change Policy status");
		
		String policyType = inputPolicy.getPolicyType();
			
		PolicyTypeProvider provider = PolicyServiceProviderFactory
				.getPolicyTypeProvider(policyType == null ? null : policyType);
	
		PolicyEditObject policyEditObject = validatePolicyRequest(
				provider,
				inputPolicy,
				currPolicy,
				updateMode
				);
		
		PolicyKey updatedPolicyKey = provider.updatePolicy(
				inputPolicy, 
				policyEditObject,
				loginSubject
				);
		return updatedPolicyKey;
	}
	
	private PolicyEditObject validatePolicyRequest(
			PolicyTypeProvider provider,
			Policy inputPolicy, 
			Policy currPolicy,
			UpdateMode updateMode) 
		throws ServiceException, PolicyProviderException 
	{
		RuleEditObject ruleEditObject = validateRuleOfPolicy(
				provider,
				currPolicy,  
				inputPolicy.getRule(),
				updateMode
				);
		//FIXME
		ResourcesEditObject resourcesEditObject = validateResourcesOfPolicy(
				provider,
				currPolicy,  
				inputPolicy,
				updateMode);
	
		SubjectsEditObject subjectsEditObject = validateSubjectsOfPolicy(
				provider,
				currPolicy,  
				inputPolicy,
				updateMode);

		PolicyEditObject policyEditObject = new PolicyEditObject();
		policyEditObject.setRuleEditObject(ruleEditObject);
		policyEditObject.setResourcesEditObject(resourcesEditObject);
		policyEditObject.setSubjectsEditObject(subjectsEditObject);
		if (currPolicy != null)
			policyEditObject.setPolicyId(currPolicy.getPolicyId());

		return policyEditObject;
	}
	
	protected Policy validatePolicyInfo(
			Policy policy)
		throws ServiceException, PolicyProviderException 
	{	
		final int maxPolicyNameLength = 109;
		
		PolicyKey policyKey = new PolicyKey();
		policyKey.setPolicyId(policy.getPolicyId());
		policyKey.setPolicyName(policy.getPolicyName());
		policyKey.setPolicyType(policy.getPolicyType());
		
		Policy currPolicy = getPolicyInfo(policyKey);
	
		if (currPolicy != null  && policy.getPolicyId() != null)
		{
			String oldName = policy.getPolicyName();
			if (oldName != null)
			{
				if (!currPolicy.getPolicyName().equals(oldName))
					validateName(currPolicy.getPolicyName(), maxPolicyNameLength);
			}
		}
			
		return currPolicy;
	}
	
	
    protected SubjectKey createSubject(Subject subject, boolean externalOnly, SubjectKey loginSubject) throws ServiceException, PolicyProviderException
    {
	    String type = subject.getSubjectType();
	    String name = subject.getSubjectName();
	    SubjectTypeProvider provider = PolicyServiceProviderFactory.
	    			getSubjectTypeProvider(type);
	    			
	    SubjectKey key = new SubjectKey();
	    key.setSubjectName(name);
	    key.setSubjectType(type);
	    	
	    if (getSubjectInfo(key) != null)
		    throwInvalidInputException("subject already existed");
	    if (provider.isExternalSubjectType())
	    {
	    	Set<Subject> externalSet = null;
	    	Long externalSubjectId = subject.getExternalSubjectId();
	    	if (externalSubjectId != null && externalSubjectId > 0)
	    	{
	    		if (provider.getExternalSubjectById(externalSubjectId) == null)
				    throwInvalidInputException("the external subject does not exist");
	    	} else {
	    		externalSet = provider.getExternalSubjectByName(name);
	    		
	    		if (externalSet == null || externalSet.isEmpty()) 
		    		throwInvalidInputException("cannot find the external subject.");
	    					
	    		if (externalSet.size() > 1)
		    		throwInvalidInputException("multiple external subject exist.");
	    		
	    		Subject work = externalSet.iterator().next();
	    		subject.setExternalSubjectId(work.getExternalSubjectId());
	    	}
	    	
	    } else if (externalOnly)
	    {
	    	throwInvalidInputException("this is not an external subject type");
	    }
	    	
    	return  provider.createSubject(subject, loginSubject);
	
    }

    protected Long deleteSubject(SubjectKey key) throws ServiceException, PolicyProviderException
    {
    	if (getSubjectInfo(key) == null)
	    	throwInvalidInputException("this subject does not exist");
    				
    	Long subjectId = key.getSubjectId();
    			
    	SubjectTypeProvider provider = PolicyServiceProviderFactory.
    				getSubjectTypeProvider(key.getSubjectType());
    			
    	Map<Long, SubjectGroup> map = provider.findSubjectGroupInfoBySubject(subjectId);
    	if (map != null && !map.isEmpty())
	    	throwInvalidInputException("subject is being referenced by a subject group");
					
    	for (String policyType: getPolicyTypes())
    	{
    		PolicyTypeProvider policyProvider = PolicyServiceProviderFactory.
				getPolicyTypeProvider(policyType); 
    		
    		Set<Long> subjectSet = new HashSet<Long>();
    		subjectSet.add(subjectId);
    		Map<Long, Policy> policyMap= policyProvider.findPolicyInfoBySubject(subjectSet, null);
    		if (policyMap != null && !policyMap.isEmpty())
    			throwInvalidInputException("subject is being referenced by a policy");
    		
    		policyMap= policyProvider.findPolicyInfoByExclusionSubject(subjectSet, null);
    		if (policyMap != null && !policyMap.isEmpty())
	    		throwInvalidInputException("subject is being referenced by a policy");
    	}
    			
    	provider.deleteSubject(subjectId);	
		return subjectId;
    }
    
	private void validateSubjectGroupCalculateor(SubjectGroup subjectGroup) throws ServiceException, PolicyProviderException
	{
		SubjectTypeProvider provider = PolicyServiceProviderFactory.
			getSubjectTypeProvider(subjectGroup.getSubjectType());
		
		String calc = subjectGroup.getSubjectGroupCalculator();
		GroupCalculatorInfo calcInfo = provider.getGroupCalculator(calc);
		if (calcInfo == null)
			throwInvalidInputException("calculator is not defined for: " + calc);		
	}
	
	protected List<GroupCalculatorInfo> getSubjectGroupCalculateor() throws ServiceException, PolicyProviderException
	{
		List<GroupCalculatorInfo> ret = new ArrayList<GroupCalculatorInfo>();
		Set<String> subjectTypes= PolicyServiceProviderFactory.getSubjectTypes();
		
		for (String subjectType: subjectTypes)
		{
			SubjectTypeProvider provider = PolicyServiceProviderFactory.
				getSubjectTypeProvider(subjectType);
		
			List<GroupCalculatorInfo> calcList = provider.getGroupCalculators();
			if (calcList == null || calcList.isEmpty() )
				continue;
			ret.addAll(calcList);
		}
		
		return ret;
	}

	protected SubjectGroupKey deleteSubjectGroup(SubjectGroupKey key) 
		throws ServiceException, PolicyProviderException
	{
		if (getSubjectGroupInfo(key) == null)
			return null;
		
		Long subjectGroupId = key.getSubjectGroupId();
		key.setSubjectGroupId(subjectGroupId);
			
		SubjectTypeProvider provider = PolicyServiceProviderFactory.
				getSubjectTypeProvider(key.getSubjectType());
	
		for (String policyType: getPolicyTypes())
    	{
    		PolicyTypeProvider policyProvider = PolicyServiceProviderFactory.
				getPolicyTypeProvider(policyType); 
    		
    		Set<Long> subjectGroupSet = new HashSet<Long>();
    		subjectGroupSet.add(subjectGroupId);
    		Map<Long, Policy> policyMap= policyProvider.findPolicyInfoBySubjectGroup(subjectGroupSet, null);
    		if (policyMap != null && !policyMap.isEmpty())
				throwInvalidInputException("subject group is being referenced by a policy");
    		
    		policyMap= policyProvider.findPolicyInfoByExclusionSubjectGroup(subjectGroupSet, null);
    		if (policyMap != null && !policyMap.isEmpty())
	    		throwInvalidInputException("subject group is being referenced by a policy");

    	}
		
		provider.deleteSubjectGroup(subjectGroupId);	
		key.setSubjectGroupId(subjectGroupId);
		
		return key;
	}
	protected SubjectGroup getSubjectGroupInfo(SubjectGroupKey subjectGroupKey) 
		throws ServiceException, PolicyProviderException
	{	
		if (subjectGroupKey == null )
			throwInvalidInputException("please input subject group");
	
		Long subjectGroupId = subjectGroupKey.getSubjectGroupId();
		String subjectGroupName = subjectGroupKey.getSubjectGroupName() ;
		if (subjectGroupName == null && subjectGroupId == null)
			throwInvalidInputException("plase input value for subject group name or subject group Id");
		
		Map<Long, SubjectGroup> ret = findSubjectGroup(subjectGroupKey);
		if (ret != null)
		{
			if (ret.isEmpty())
				return null;
			
			if (ret.size() > 1)
				throwInvalidInputException("more than one subject group found");
		}
		
		Entry<Long, SubjectGroup> entry = ret.entrySet().iterator().next();
		SubjectGroup subjectGroup = entry.getValue();
		subjectGroupKey.setSubjectGroupId(entry.getKey());
		subjectGroupKey.setSubjectGroupName(subjectGroup.getSubjectGroupName());
		
		return subjectGroup;
	}

	protected void throwInvalidInputException(String errString) throws ServiceException {
		throw new ServiceException(
				ErrorUtils.createErrorData(
						ErrorConstants.SVC_POLICYSERVICE_INVALID_INPUT_ERROR, 
						ErrorConstants.ERRORDOMAIN.toString(), 
						new Object[]{errString}));
	}

	private SubjectGroupEditObject validateSubjectGroupRequest(
			SubjectTypeProvider provider,
			SubjectGroup subjectGroup, 
			SubjectGroup currSubjectGroup,
			UpdateMode updateMode) 
		throws ServiceException, PolicyProviderException
	{	
		
		Long subjectGroupId = null;
	
		List<Long> currSubjectList = new ArrayList<Long>();		
		if (currSubjectGroup != null)	
		{
			subjectGroupId = Utils.getSubjectGroupId(currSubjectGroup);
			
			Map<Long,Subject> subjectMap = provider.getSubjectAssignmentOfSubjectGroup(subjectGroupId);
			if (subjectMap != null && !subjectMap.isEmpty())
				currSubjectList.addAll(subjectMap.keySet());	
		}
		
		SubjectGroupEditObject subjectGroupEditObject = new SubjectGroupEditObject();
		subjectGroupEditObject.setSujectGroupId(subjectGroupId);
		
		List<SubjectKey> inputSubjectList = getSubjectKeyInfo(subjectGroup);
		if (subjectGroup.getSubjectGroupCalculator() == null ||
				subjectGroup.getSubjectGroupCalculator().isEmpty())
		{
			
			validateAssignment(
					currSubjectList,  
					inputSubjectList,  
					subjectGroupEditObject.getAddSubjectList(),
					subjectGroupEditObject.getRemoveSubjectList(),
					updateMode);
		} else {
			validateSubjectGroupCalculateor(subjectGroup);
		}
		
		return subjectGroupEditObject;
	}
	
	private List<SubjectKey> getSubjectKeyInfo(SubjectGroup subjectGroup) throws ServiceException {
		List<SubjectKey> subjectKeys = new ArrayList<SubjectKey>();
		if (subjectGroup == null || subjectGroup.getSubject().isEmpty())
			return subjectKeys;
		
		for (Subject subject : subjectGroup.getSubject()) {
			SubjectKey subjectKey = new SubjectKey();
			subjectKey.setSubjectId(Utils.getSubjectId(subject));
			subjectKey.setSubjectName(subject.getSubjectName());
			// subject type should match subject group type
			if (!subject.getSubjectType().
					equalsIgnoreCase(subjectGroup.getSubjectType())) {
				throwInvalidInputException("subject type" + subject.getSubjectType() +
						" should be the same as the subject group that it is assigned to");
			}
			subjectKey.setSubjectType(subject.getSubjectType());
			subjectKeys.add(subjectKey);
		}
		return subjectKeys;
	}

	private void validateResourceName(String name) throws ServiceException{
		if (name == null || name.trim().isEmpty())
			return;		
		
		 //  name length can not exceed maxResourceNameLength.
        if (name != null && name.trim().length() > maxResourceNameLength) {
        	throwInvalidInputException("name length can not exceed "+maxResourceNameLength+" characters");
        }
        
      //subject name must be alpha_nemeric
        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && c != '.') {
            	throwInvalidInputException("permissible characters in name are: a-z, A-Z, 0-9, _ or -.");
            }
        }
	}
	
	private void validateName(String name, int length) throws ServiceException
	{
		if (name == null || name.trim().isEmpty())
			return;
		
		if ( name.startsWith("Admin_Policy_") ||
				name.startsWith("Admin_SubjectGroup_") ||
				name.equals("All"))
		{
			throwInvalidInputException("name is reserved");
		}
		
		 //  name length can not exceed 109.
        if (name != null && name.trim().length() > length) {
        	throwInvalidInputException("name length can not exceed "+length+" characters");
        }
        
      //subject name must be alpha_nemeric
        for (char c : name.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '_' && c != '-' && c != '.') {
            	throwInvalidInputException("permissible characters in name are: a-z, A-Z, 0-9, _ or -.");
            }
        }
	}
	
	protected SubjectGroup validateSubjectGroupInfo(
			SubjectGroup subjectGroup)
		throws ServiceException, PolicyProviderException 
	{
		final int maxSubjectGroupNameLength = 128;
		validateName(subjectGroup.getSubjectGroupName(), maxSubjectGroupNameLength);
		
			
		if (subjectGroup.getSubjectGroupCalculator() != null && 
				subjectGroup.getSubjectGroupCalculator().trim().length() > 256)
			throwInvalidInputException("calculator name can not exceed 256");
		
		if (subjectGroup.getDescription() != null && 
				subjectGroup.getDescription().trim().length() > 256)
			throwInvalidInputException("Description can not exceed 256");		
		
		SubjectGroupKey subjectGroupKey = new SubjectGroupKey();
		subjectGroupKey.setSubjectGroupId(Utils.getSubjectGroupId(subjectGroup));
		subjectGroupKey.setSubjectGroupName(subjectGroup.getSubjectGroupName());
		subjectGroupKey.setSubjectType(subjectGroup.getSubjectType());
		
		SubjectGroup currSubjectGroup = getSubjectGroupInfo(subjectGroupKey);
		if (currSubjectGroup != null  && subjectGroupKey.getSubjectGroupId() != null)
		{
			String oldName = subjectGroup.getSubjectGroupName();
			if (oldName != null)
			{
				if (!currSubjectGroup.getSubjectGroupName().equals(oldName))
					validateName(currSubjectGroup.getSubjectGroupName(), maxSubjectGroupNameLength);
			}
			Utils.setSubjectGroupId(currSubjectGroup,subjectGroupKey.getSubjectGroupId() );
		}
		
		return  currSubjectGroup;
	}
	
	private List<Rule> validateRemoveRuleOfPolicy(
			List<Long> currRuleIdList,
			List<String> currRuleNameList,
			List<Rule> inputRuleList,
			List<Rule> removeRuleList)
		throws ServiceException, PolicyProviderException 
	{
		List<Rule> unremoveableRuleList = new ArrayList<Rule>();
		
		if (currRuleIdList.isEmpty() || currRuleNameList.isEmpty())
		{
			unremoveableRuleList.addAll(inputRuleList);
			return unremoveableRuleList;
		}
		
		for (Rule rule :inputRuleList)
		{
			if (rule.getRuleId() != null)
			{
				int pos = currRuleIdList.indexOf(rule.getRuleId());
				if (pos >= 0)
				{
					rule.setRuleName(currRuleNameList.get(pos));
					removeRuleList.add(rule);
				} else {
					unremoveableRuleList.add(rule);
				}
			} else if (rule.getRuleName() != null)
			{
				int pos = currRuleNameList.indexOf(rule.getRuleName());
				if (pos >= 0)
				{
					rule.setRuleId(currRuleIdList.get(pos));
					removeRuleList.add(rule);
				} else {
					unremoveableRuleList.add(rule);
				}
			} else {
				throw new ServiceException("invalid input of Rule: either rule name or id is required");
			}
		}
		return unremoveableRuleList;
	}
	
	private void validateAddRuleOfPolicy(
			PolicyTypeProvider provider,
			List<String> currRuleNameList,
			List<Rule> inputRuleList,
			List<Rule> addRuleList)
		throws ServiceException, PolicyProviderException 
	{
		for (Rule rule :inputRuleList)
		{
			if (rule.getRuleName()== null)
				throwInvalidInputException("please input rule name");			
			
			if (currRuleNameList.contains(rule.getRuleName()))
				continue;
			
			if (provider.isRuleNameUsed(rule.getRuleName()))
				throwInvalidInputException("rule with the same name already exists");
			
			if (!provider.isRuleValid(rule))
				throwInvalidInputException("rule is not valid");
			
			addRuleList.add(rule);
		}
	}
	
	private SubjectsEditObject validateSubjectsOfPolicy(
			PolicyTypeProvider provider,
			Policy currPolicy,  
			Policy inputPolicy,
			UpdateMode updateMode)
		throws ServiceException, PolicyProviderException 
	{
		List<Long> currSubjectTypeList = new ArrayList<Long>();
		List<Long> currSubjectList = new ArrayList<Long>();
		List<Long> currExclusionSubjectList = new ArrayList<Long>();
		List<Long> currSubjectGroupList = new ArrayList<Long>();
		List<Long> currExclusionSubjectGroupList = new ArrayList<Long>();
		
		List<SubjectTypeInfo> inputSubjectTypeList = new ArrayList<SubjectTypeInfo>();
		List<SubjectKey> inputSubjectList = new ArrayList<SubjectKey>();
		List<SubjectGroupKey> inputSubjectGroupList = new ArrayList<SubjectGroupKey>();
		List<SubjectKey> inputExclusionSubjectList = new ArrayList<SubjectKey>();
		List<SubjectGroupKey> inputExclusionSubjectGroupList = new ArrayList<SubjectGroupKey>();
		
		if ( currPolicy != null)
		{
			Long policyId = currPolicy.getPolicyId();
			Map<Long, SubjectTypeInfo> subjectTypeMap= provider.getSubjectTypeAssignmentOfPolicy(policyId, null);
			if (subjectTypeMap != null && !subjectTypeMap.isEmpty())
				currSubjectTypeList.addAll(subjectTypeMap.keySet());
			
			Map<Long, Subject> subjectMap = provider.getSubjectAssignmentOfPolicy(policyId, null);
			if (subjectMap != null && !subjectMap.isEmpty())
				currSubjectList.addAll(subjectMap.keySet());
				
			Map<Long, Subject> subjectExclusionMap = provider.getExclusionSubjectAssignmentOfPolicy(policyId, null);
			if (subjectExclusionMap != null && !subjectExclusionMap.isEmpty())
				currExclusionSubjectList.addAll(subjectExclusionMap.keySet());
			
			Map<Long, SubjectGroup> subjectGroupMap = provider.getSubjectGroupAssignmentOfPolicy(policyId, null);
			if (subjectGroupMap != null && !subjectGroupMap.isEmpty())
				currSubjectGroupList.addAll(subjectGroupMap.keySet());
			
			Map<Long, SubjectGroup> subjectGroupExclusionMap = provider.getExclusionSubjectGroupAssignmentOfPolicy(policyId, null);
			if (subjectGroupExclusionMap != null && !subjectGroupExclusionMap.isEmpty())
				currExclusionSubjectGroupList.addAll(subjectGroupExclusionMap.keySet());
		}

		constructSubjectRequest(
				inputPolicy.getTarget(),
				inputSubjectTypeList,
				inputSubjectList,
				inputExclusionSubjectList);
		
		constructSubjectGroupRequest(
				inputPolicy.getTarget(),
				inputSubjectGroupList,
				inputExclusionSubjectGroupList);
		
		SubjectsEditObject subjectsEditObject = new SubjectsEditObject();
		
		validateAssignment(
				currSubjectTypeList,
				inputSubjectTypeList,
				subjectsEditObject.getAddSubjectTypeList(),
				subjectsEditObject.getRemoveSubjectTypeList(),
				updateMode);
		
		validateAssignment(
				currSubjectList,  
				inputSubjectList,  
				subjectsEditObject.getAddSubjectList(),
				subjectsEditObject.getRemoveSubjectList(),
				updateMode);
		
		validateAssignment(
				currExclusionSubjectList,  
				inputExclusionSubjectList,  
				subjectsEditObject.getAddExclusionSubjectList(),
				subjectsEditObject.getRemoveExclusionSubjectList(),
				updateMode);
		
		validateAssignment(
				currSubjectGroupList,  
				inputSubjectGroupList,  
				subjectsEditObject.getAddSubjectGroupList(),
				subjectsEditObject.getRemoveSubjectGroupList(),
				updateMode);
		
		validateAssignment(
				currExclusionSubjectGroupList,  
				inputExclusionSubjectGroupList,  
				subjectsEditObject.getAddExclusionSubjectGroupList(),
				subjectsEditObject.getRemoveExclusionSubjectGroupList(),
				updateMode);
		
		if (inputPolicy.isActive() != null && inputPolicy.isActive()) 
		{
			boolean  throwError = true;
			
			if (!subjectsEditObject.getAddSubjectTypeList().isEmpty() ||
					!subjectsEditObject.getAddSubjectList().isEmpty() ||
					!subjectsEditObject.getAddSubjectGroupList().isEmpty() )
				throwError = false;
		
			if (throwError)
			{
				if (currSubjectTypeList.size() > subjectsEditObject.getRemoveSubjectTypeList().size() || 
					currSubjectList.size() > subjectsEditObject.getRemoveSubjectList().size() ||
					currSubjectGroupList.size() > subjectsEditObject.getRemoveSubjectGroupList().size() )
					throwError = false;
			}
			
			if (throwError)
				throwInvalidInputException("need at least one subject/subjectGroup assigned");
		}
		
		return subjectsEditObject;
	}
	
	private ResourcesEditObject validateResourcesOfPolicy(
			PolicyTypeProvider provider,
			Policy currPolicy,  
			Policy inputPolicy,
			UpdateMode updateMode)
		throws ServiceException, PolicyProviderException 
	{
		List<ResourceKey> inputResourceList  = new ArrayList<ResourceKey>();
		List<OperationKey> inputOperationList  = new ArrayList<OperationKey>();
		List<Long> currResourceList = new ArrayList<Long>();
		List<Long> currOperationList = new ArrayList<Long>();;
	
		if ( currPolicy != null)
		{
			Long policyId = currPolicy.getPolicyId();
			Map<Long, Resource> resourceMap = provider.getResourceAssignmentOfPolicy(policyId, null);
			if (resourceMap != null && !resourceMap.isEmpty())
				currResourceList.addAll(resourceMap.keySet());
			
			Map<Long, Operation> operationMap = provider.getOperationAssignmentOfPolicy(policyId, null);
			if (operationMap != null && !operationMap.isEmpty())
				currOperationList.addAll(operationMap.keySet());
		}
		
		constructResourceOperationRequest(
				inputPolicy.getTarget(),
				inputResourceList,
				inputOperationList);
		
		if (inputResourceList.size() > 0 && !provider.allowResourceLevel())
			throwInvalidInputException("this policy type only allow operation level of resource");
				
		ResourcesEditObject resourcesEditObject = new ResourcesEditObject();
		validateAssignment(
				currResourceList,  
				inputResourceList,  
				resourcesEditObject.getAddResourceList(),
				resourcesEditObject.getRemoveResourceList(),
				updateMode);
		
		validateAssignment(
				currOperationList,  
				inputOperationList,  
				resourcesEditObject.getAddOperationList(),
				resourcesEditObject.getRemoveOperationList(),
				updateMode);
		
			
		if (inputPolicy.isActive() != null && inputPolicy.isActive() && !provider.allowGlobalLevel()) 
		{
			boolean  throwError = true;
			
			if (!resourcesEditObject.getAddOperationList().isEmpty() ||
					!resourcesEditObject.getAddResourceList().isEmpty())
				throwError = false;
			
			if (throwError)
			{	
				if (currOperationList.size() > resourcesEditObject.getRemoveOperationList().size() || 
						currResourceList.size() > resourcesEditObject.getRemoveResourceList().size()  )
					throwError = false;
			}
			
			if (throwError)
				throwInvalidInputException("need at least one subject/subjectGroup assigned");
		}
		return resourcesEditObject;
	}
	
	private RuleEditObject validateRuleOfPolicy(
			PolicyTypeProvider provider,
			Policy currPolicy,    
			List<Rule> inputList,
			UpdateMode updateMode)
		throws ServiceException, PolicyProviderException 
	{
		final int maxRuleNameLength = 128;
		if (!provider.isRuleRequired())
			return null;
		
		if (inputList.isEmpty())
		{
			if (updateMode.equals(UpdateMode.REPLACE))
					throw new ServiceException("need at least one rule");
			return null;
		}
		
		List<Long> currRuleIdList = new ArrayList<Long>();
		List<String> currRuleNameList = new ArrayList<String>();
		List<Rule> currRuleList = new ArrayList<Rule>();
		
		if (currPolicy != null)
		{
			validateName(currPolicy.getPolicyName(), maxRuleNameLength);
			
			Map<Long, Rule>  currRuleMap = provider.getRuleAssignmentOfPolicy(currPolicy.getPolicyId(), null);
			if (currRuleMap != null && !currRuleMap.isEmpty())
			{
				currRuleList.addAll(currRuleMap.values());	
				for (Rule rule: currRuleList)
				{
					currRuleIdList.add(rule.getRuleId());
					currRuleNameList.add(rule.getRuleName());
				}
			}
		}
		
		RuleEditObject ruleEditObject = new RuleEditObject();
		switch (updateMode){
		case DELETE:
			List<Rule> list = validateRemoveRuleOfPolicy(
						currRuleIdList,
						currRuleNameList,
						inputList,
						ruleEditObject.getRemoveList());
				
			if (list.size() == inputList.size())
				throw new ServiceException("No Rule can be deleted");
				
			if (ruleEditObject.getRemoveList().size() == currRuleIdList.size())
				throw new ServiceException("need at least one rule");
			
			break;
		
		case UPDATE:
			validateAddRuleOfPolicy(
					provider,
					currRuleNameList,
					inputList,
					ruleEditObject.getAddList());
			
			break;
		case REPLACE:
			List<Rule> unremoveableRuleList = validateRemoveRuleOfPolicy(
					currRuleIdList,
					currRuleNameList,
					inputList,
					ruleEditObject.getRemoveList());
			
			validateAddRuleOfPolicy(
					provider,
					currRuleNameList,
					unremoveableRuleList,
					ruleEditObject.getAddList());
			
			ruleEditObject.getRemoveList().clear();
			ruleEditObject.getRemoveList().addAll(currRuleList);
			ruleEditObject.getAddList().clear();
			ruleEditObject.getAddList().addAll(inputList);
			if (inputList.size() < 1)
				throw new ServiceException("need at least one rule");
			break;
		default:
			break;
		}

		return ruleEditObject;
	}
	
	protected ResourceKey createResource(Resource resource, SubjectKey loginSubject)
		throws ServiceException, PolicyProviderException 
	{
		ResourceTypeProvider provider = PolicyServiceProviderFactory.getResourceTypeProvider(resource.getResourceType()); 
		
		OperationEditObject operationEditObject = validateResourceRequest(
				provider,
				resource, 
				null);
		
		return provider.createResource(resource, operationEditObject,loginSubject);
	}
	
	protected ResourceKey updateResource(Resource resource, UpdateMode updateMode, SubjectKey loginSubject)
		throws ServiceException, PolicyProviderException 
	{
		ResourceTypeProvider provider = PolicyServiceProviderFactory.getResourceTypeProvider(resource.getResourceType()); 
	
		OperationEditObject operationEditObject = validateResourceRequest(
				provider,
				resource, 
				updateMode);
		
		return provider.updateResource(
				resource, 
				operationEditObject,
				loginSubject);
	}
	

	
	private OperationEditObject validateResourceRequest(
			ResourceTypeProvider provider,
			Resource resource, 
			UpdateMode updateMode)
		throws ServiceException, PolicyProviderException 
	{
		//validation
		List<Operation> currOperationList = null;
		
		boolean isCreate = updateMode == null? true:false;
		Long resourceId = null;	
		ResourceKey resourceKey = new ResourceKey();
		resourceKey.setResourceName(resource.getResourceName());
		resourceKey.setResourceId(resource.getResourceId());
		resourceKey.setResourceType(resource.getResourceType());
		Resource currResource = getResourceInfo(resourceKey);
			
		if (isCreate)
		{
			if (currResource != null )
				throwInvalidInputException("resource already exists");
			updateMode = UpdateMode.REPLACE;
		} else {	
			if (currResource == null)
				throwInvalidInputException("resource does not exist");
			resourceId = currResource.getResourceId();
			resource.setResourceId(resourceId);
			currOperationList = provider.getOperationByResourceId(resourceId);
		}
			
		validateResourceInfo(isCreate, resource, currResource);
		
		return validateOperationOfResource(
					currOperationList, 
					resource, 
					updateMode);

	}
    
	protected Operation getOperation(OperationKey key) throws ServiceException, PolicyProviderException 
	{
		ResourceTypeProvider provider = PolicyServiceProviderFactory.getResourceTypeProvider(key.getResourceType()); 
		String resourceName = key.getResourceName(); //required field
		Long operationId = key.getOperationId();
		String operationName = key.getOperationName();
		
		Operation operation = null;
		if (operationId != null)
		{
			operation = provider.getOperationById(resourceName,operationId);
		} else if (operationName != null) {
			operation = provider.getOperationByName(resourceName, operationName);
		}
		
		if(operation != null)
		{			
			key.setOperationId(operation.getOperationId());
			key.setOperationName(operation.getOperationName());
		}
		
		return operation;
	}
    
    protected ResourceKey deleteResource(ResourceKey resourceKey)
		throws ServiceException, PolicyProviderException 
	{
		//validation
		Resource resource = getResourceInfo(resourceKey);
		if (resource == null)
			throwInvalidInputException("resource does not exist");
			
		//validate if the resource has been referenced by Policy
		Set<Long> resourceIdSet = new HashSet<Long>();
		resourceIdSet.add(resourceKey.getResourceId());
		
		Set<Long> operationIdSet = new HashSet<Long>();
		ResourceTypeProvider provider = PolicyServiceProviderFactory.getResourceTypeProvider(resourceKey.getResourceType()); 
		List<Operation> operationList = provider.getOperationByResourceId(resourceKey.getResourceId());
		if (operationList != null)
			for (Operation op: operationList)
				operationIdSet.add(op.getOperationId());
	
		for (String policyType: getPolicyTypes())
	    {
			PolicyTypeProvider policyProvider = PolicyServiceProviderFactory.getPolicyTypeProvider(policyType); 
			Map<Long, Policy> policyMap1= policyProvider.findPolicyInfoByResource(resourceIdSet, null);
		    if (policyMap1 != null && !policyMap1.isEmpty())
				throwInvalidInputException("resource is being referenced by a policy");
		    
		    if(operationIdSet.size() > 0)
		    {
		    	Map<Long, Policy> policyMap2= policyProvider.findPolicyInfoByOperation(operationIdSet, null);
		    	if (policyMap2 != null && !policyMap2.isEmpty())
		    		throwInvalidInputException("operation is being referenced by a policy");
		    }
		}
		
		provider.deleteResource(resourceKey.getResourceId());
		return resourceKey;
	}
    

   protected Subject getSubjectInfo(SubjectKey key) 
		throws ServiceException, PolicyProviderException
	{
		if (key.getSubjectName() == null && key.getSubjectId() == null)
			throwInvalidInputException("please input value for subject name or subject Id");
		
		Map<Long, Subject> ret = findSubject(key);
		if (ret != null)
		{
			if (ret.isEmpty())
				return null;
			
			if (ret.size() > 1)
				throwInvalidInputException("more than one subject found");
		}
		
		Entry<Long, Subject> entry = ret.entrySet().iterator().next();
		Subject subject = entry.getValue();
		key.setSubjectId(entry.getKey());
		key.setSubjectName(entry.getValue().getSubjectName());
		
		return subject;
	}
	
	protected Set<Subject> findExternalSubject(SubjectKey subjectKey) throws ServiceException, PolicyProviderException
	{	
    	SubjectTypeProvider provider = PolicyServiceProviderFactory.
    			getSubjectTypeProvider(subjectKey.getSubjectType());
    			
    	String name = subjectKey.getSubjectName();
    	Set<Subject> map = provider.getExternalSubjectByName(name);
    	return map;
    }
	protected Map<Long, Subject> findSubject(SubjectKey subjectKey) throws ServiceException, PolicyProviderException
	{
		Map<Long, Subject> finalMap = new HashMap<Long, Subject>();
		String name = subjectKey.getSubjectName();
		Long id = subjectKey.getSubjectId();
			
		SubjectTypeProvider provider = PolicyServiceProviderFactory.
			getSubjectTypeProvider(subjectKey.getSubjectType());
		if (id != null)
		{
			Map<Long, Subject> map = provider.getSubjectById(id);
			if (map != null && !map.isEmpty())
				finalMap.putAll(map);
		} else if (name != null)
		{
			Map<Long, Subject> map = provider.getSubjectByName(name);
			if (map != null && !map.isEmpty())
				finalMap.putAll(map);
		} else {
			Map<Long, Subject> map = provider.getSubjectByType();
			if (map!= null && !map.isEmpty())
			finalMap.putAll(map);
		}
			
		return finalMap;
	}
	
	protected Map<Long, SubjectGroup> findSubjectGroup(SubjectGroupKey key) throws ServiceException,  PolicyProviderException
	{
		Map<Long, SubjectGroup> finalMap = new HashMap<Long, SubjectGroup>();
		String name = key.getSubjectGroupName();
		Long id = key.getSubjectGroupId();	
		String type = key.getSubjectType();
		
		if (type == null)
			throwInvalidInputException("subject type is required");
		
		
		SubjectTypeProvider provider = PolicyServiceProviderFactory.getSubjectTypeProvider(type);
		if (id != null)
		{
			Map<Long, SubjectGroup> map = provider.getSubjectGroupInfoById(id);
			if (map != null)
				finalMap.putAll(map);
		} else if (name != null)
		{
			Map<Long, SubjectGroup> map = provider.getSubjectGroupInfoByName(name);
			if (!map.isEmpty())
				finalMap.putAll(map);
		} else {
			Map<Long, SubjectGroup> map = provider.getSubjectGroupInfoByType();
			if (!map.isEmpty())
			finalMap.putAll(map);
		}
					
		return finalMap;
	}
	
	protected Map<Long, Subject> getSubjects(SubjectGroupKey key) throws ServiceException,  PolicyProviderException
	{
		SubjectTypeProvider provider = PolicyServiceProviderFactory.getSubjectTypeProvider(key.getSubjectType());
		Map<Long, Subject> subjectMap = provider.getSubjectAssignmentOfSubjectGroup(key.getSubjectGroupId());
		if (subjectMap == null || subjectMap.isEmpty())
			return subjectMap;
		
		Iterator<Entry<Long,Subject>> sIter = subjectMap.entrySet().iterator();
		while (sIter.hasNext())
		{
			Entry<Long,Subject> entryS = sIter.next();
			Long subjectId = entryS.getKey();
			Subject subject = entryS.getValue();
			Utils.setSubjectId(subject, subjectId);
		}
		
		return subjectMap;
		
	}	
	
	protected void audit(Object object, String operationType, SubjectKey loginSubject) throws ServiceException,PolicyFinderException 
	{
		if (object instanceof PolicyKey)
		{
			PolicyKey policyKey = (PolicyKey)object;
			String policyType = policyKey.getPolicyType();
			PolicyTypeProvider provider = PolicyServiceProviderFactory.
				getPolicyTypeProvider(policyType == null ? null : policyType);
			provider.audit(policyKey, operationType, loginSubject);
		} else if (object instanceof SubjectGroupKey){
			SubjectGroupKey subjectGroupKey = (SubjectGroupKey)object;
			SubjectTypeProvider provider = PolicyServiceProviderFactory.
				getSubjectTypeProvider(subjectGroupKey.getSubjectType());
			provider.audit(subjectGroupKey, operationType, loginSubject);	
		} else if (object instanceof SubjectKey){
			SubjectKey subjectKey = (SubjectKey)object;
			SubjectTypeProvider provider = PolicyServiceProviderFactory.
				getSubjectTypeProvider(subjectKey.getSubjectType());
			provider.audit(subjectKey, operationType, loginSubject);	
		} else if (object instanceof ResourceKey){
			ResourceKey resourceKey = (ResourceKey)object;
			ResourceTypeProvider provider = PolicyServiceProviderFactory.
				getResourceTypeProvider(resourceKey.getResourceType());
			provider.audit(resourceKey, operationType, loginSubject);	
		}  else if (object instanceof OperationKey){
			OperationKey operationKey = (OperationKey)object;
			ResourceTypeProvider provider = PolicyServiceProviderFactory.
				getResourceTypeProvider(operationKey.getResourceType());
			provider.audit(operationKey, operationType, loginSubject);	
		} 
		
	}
	
	private void validateAssignment(
			List<Long> currList,  
			List<?> inputList, 
			List<Long> addList,
			List<Long> removeList,
			UpdateMode updateMode) throws ServiceException,  PolicyProviderException
	{	
		addList.clear();
		removeList.clear();
		
		
		Set<Long> existList = new HashSet<Long>();
		Set<Long> nonExistList = new HashSet<Long>();
		
		for (Object object: inputList)
		{
			Long objectId = getObjectId(object);
			if (!currList.contains(objectId))
				nonExistList.add(objectId);
			else
				existList.add(objectId);
		}
			
		switch (updateMode){
			case UPDATE:
				addList.addAll(nonExistList);
				break;
			case DELETE:
				removeList.addAll(existList);
				break;
			case REPLACE:
				addList.addAll(nonExistList);
				for (Long curr: currList)
				{
					if (!existList.contains(curr))
						removeList.add(curr);
				}
				break;
			default:
				break;
		}
			
		return;
	}
	
	
	private Long getObjectId(Object object) throws ServiceException,  PolicyProviderException
	{
		if (object instanceof SubjectKey)
		{
			SubjectKey subjectKey = (SubjectKey)object;
			if (getSubjectInfo(subjectKey)== null)
				throwInvalidInputException("the subject does not exist");
			
			return subjectKey.getSubjectId();
		} else if (object instanceof SubjectGroupKey)
		{
			SubjectGroupKey subjectGroupKey = (SubjectGroupKey)object;
			if (getSubjectGroupInfo(subjectGroupKey) == null)
				throwInvalidInputException("the subject group does not exist");
			
			return subjectGroupKey.getSubjectGroupId();
		} else if (object instanceof ResourceKey)
		{
			ResourceKey resourceKey = (ResourceKey)object;
			if (getResourceInfo(resourceKey) == null)
				throwInvalidInputException("the resource does not exist");
			
			return resourceKey.getResourceId();
		} else if (object instanceof OperationKey)
		{
			OperationKey operationKey = (OperationKey)object;
			if ( getOperation(operationKey) == null)
				throwInvalidInputException("the operation does not exist");
			
			return operationKey.getOperationId();
		} else if (object instanceof SubjectTypeInfo)
		{
			SubjectTypeInfo subjectTypeInfo = (SubjectTypeInfo)object;
			return subjectTypeInfo.getId();
		}
		return null;
	}

	protected Resource getResourceInfo(ResourceKey resourceKey) 
		throws ServiceException,  PolicyProviderException
	{
		if (resourceKey == null )
			throwInvalidInputException("please input resource");
		
		String type =resourceKey.getResourceType();
		
		if (resourceKey.getResourceName() == null && resourceKey.getResourceId() == null)
			throwInvalidInputException("please input value for resource name or resource Id");
		
		Resource currResourceInfo;
		ResourceTypeProvider provider = PolicyServiceProviderFactory.getResourceTypeProvider(type); 
		
		if (resourceKey.getResourceId() != null)
			currResourceInfo = provider.getResourceInfoById(resourceKey.getResourceId());
		else 
			currResourceInfo = provider.getResourceInfoByName(resourceKey.getResourceName());
		
		if (currResourceInfo != null)
		{
			resourceKey.setResourceId(currResourceInfo.getResourceId());
			resourceKey.setResourceName(currResourceInfo.getResourceName());
		}
		return currResourceInfo;
	}
	
	
	
	private OperationEditObject validateOperationOfResource(
			List<Operation> currList,  
			Resource inputResource,  
			UpdateMode updateMode) throws ServiceException,  PolicyProviderException
	{	
		
		if (inputResource == null)
			return null;
		
		List<Operation> inputList = inputResource.getOperation();
		
		
		Set<String> existList = new HashSet<String>();
		Set<String> nonExistList = new HashSet<String>();
		
		OperationEditObject editObject = new OperationEditObject();
		List<String> addList = editObject.getAddList();
		List<String> removeList = editObject.getRemoveList();
		
		List<String> currNameList = new ArrayList<String>();
		List<Long> currIdList = new ArrayList<Long>();
	
		if (currList != null)
			for (Operation op:currList)
			{
				currNameList.add(op.getOperationName());
				currIdList.add(op.getOperationId());
			}
		
		for (Operation object: inputList)
		{
			String operationName = object.getOperationName();
			if (operationName == null)
			{
				Long operationId = object.getOperationId();
				if (operationId == null)
					throwInvalidInputException("operation is not valid");
	
				int index = currIdList.indexOf(operationId);
				if (index >= 0)
					existList.add(currNameList.get(index));
			} else {
				if (currNameList.contains(operationName))
					existList.add(operationName);
				else
				{
					nonExistList.add(operationName);
					validateResourceName(operationName);

					String desc = object.getDescription();
					if(desc != null)
					{ 			
						if(desc.trim().length() > 256)
							throwInvalidInputException("description length can not exceed 256");
					}						
				}
			}
		}
			
	
		switch (updateMode){
		case UPDATE:
			addList.addAll(nonExistList);
			break;
		case DELETE:
			removeList.addAll(existList);
			break;
		case REPLACE:
			addList.addAll(nonExistList);
			
			for (String curr: currNameList)
			{
				if (!existList.contains(curr))
					removeList.add(curr);
			}
			
			break;
		default:
			break;
		}		
		
		//verify removeList's reference
		Set<Long> operationSet = new HashSet<Long>();
		for (String operationName: removeList)
		{
			int index = currNameList.indexOf(operationName);
    		operationSet.add(currIdList.get(index));
		}
		
		if (!operationSet.isEmpty())
			for (String policyType: getPolicyTypes())
	    	{
				PolicyTypeProvider policyProvider = PolicyServiceProviderFactory.getPolicyTypeProvider(policyType); 
				Map<Long, Policy> policyMap= policyProvider.findPolicyInfoByOperation(operationSet, null);
		    		if (policyMap != null && !policyMap.isEmpty())
						throwInvalidInputException("operation is being referenced by Policy");
			}
		
		editObject.setResourceId(inputResource.getResourceId());
		return editObject;
	}
	
	private void validateResourceInfo(
			boolean isCreate,
			Resource inputResource, 
			Resource currResource)
		throws ServiceException,  PolicyProviderException 
	{ 
		String resourceName = inputResource.getResourceName();
		if(isCreate)
		{
			validateResourceName(resourceName);
		}
		else if(resourceName != null && !resourceName.equals(currResource.getResourceName()))
		{
			validateResourceName(resourceName);
		}

		String desc = inputResource.getDescription();
		if(desc != null)
		{ 			
			if(desc.trim().length() > 256)
				throwInvalidInputException("description length can not exceed 256");
		}						
	}
	
	protected SubjectKey getLoginSubject() throws  PolicyProviderException, ServiceException
	{
		MessageContext messageContext = MessageContextAccessor.getContext();
		if (messageContext == null
				|| messageContext.getSecurityContext() == null) 
			return null;
		
		Map<String, String> authnSubjects = null;
		try {
			SecurityContext sc = messageContext.getSecurityContext();
			authnSubjects = sc.getAuthnSubjects();
			if (authnSubjects == null || authnSubjects.isEmpty())
				return null;
		} catch (ServiceException e){
			return null;
		}

		
		for (String subjectType : authnSubjects.keySet()) 
		{	
			String subjectName = authnSubjects.get(subjectType);
			Long subjectId = null;
			SubjectKey subjectKey = new SubjectKey();
			subjectKey.setSubjectName(subjectName);
			subjectKey.setSubjectType(subjectType);
			Map<Long, Subject> subjectMap = findSubject(subjectKey);
		
			if (subjectMap != null && !subjectMap.isEmpty())
			{
				subjectId = subjectMap.keySet().iterator().next();
				subjectKey.setSubjectId(subjectId);
				return subjectKey;
			}
		}
		
		return null;
	}
	   
	protected List<EntityHistory> getEntityHistory(Object object, XMLGregorianCalendar startDate, XMLGregorianCalendar endDate) throws ServiceException, PolicyProviderException
    {

    	if (object instanceof PolicyKey)
    	{
    		PolicyKey policyKey = (PolicyKey)object;
    		String policyType = policyKey.getPolicyType();
    		PolicyTypeProvider provider =PolicyServiceProviderFactory.
				getPolicyTypeProvider(policyType == null ? null : policyType);
    	
    		return provider.getAuditHistory(policyKey, startDate, endDate);
    		
    	}  if (object instanceof ResourceKey) {
    		ResourceKey resourceKey = (ResourceKey)object;
    		ResourceTypeProvider provider =PolicyServiceProviderFactory.
				getResourceTypeProvider(resourceKey.getResourceType());
    	
    		return provider.getAuditHistory(resourceKey, startDate, endDate);
    	}  if (object instanceof OperationKey) {
    		OperationKey operationKey = (OperationKey)object;
    		ResourceTypeProvider provider =PolicyServiceProviderFactory.
				getResourceTypeProvider(operationKey.getResourceType());
    	
    		return provider.getAuditHistory(operationKey, startDate, endDate);
    	}  if (object instanceof SubjectKey) {
    		SubjectKey subjectKey = (SubjectKey)object;
    		SubjectTypeProvider provider =PolicyServiceProviderFactory.
				getSubjectTypeProvider(subjectKey.getSubjectType());
    	
    		return provider.getAuditHistory(subjectKey, startDate, endDate);
    	} if (object instanceof SubjectGroupKey){
    		SubjectGroupKey subjectGroupKey = (SubjectGroupKey)object;
    		SubjectTypeProvider provider =PolicyServiceProviderFactory.
				getSubjectTypeProvider(subjectGroupKey.getSubjectType());
    	
    		return  provider.getAuditHistory(subjectGroupKey, startDate, endDate);
    	} 
    	return null;
    }

	protected PolicyKey enablePolicy(PolicyKey policyKey, SubjectKey loginSubject)
		throws ServiceException, PolicyProviderException 
	{
    	Policy currPolicy = getPolicyInfo(policyKey);
    	if (currPolicy == null )
			throwInvalidInputException("the Policy does not exist");
    		
    	PolicyTypeProvider provider = PolicyServiceProviderFactory
				.getPolicyTypeProvider(currPolicy.getPolicyType());
    		
    	Long policyId = currPolicy.getPolicyId();
    		
    	Map<Long, SubjectTypeInfo> subjectTypeMap= provider.getSubjectTypeAssignmentOfPolicy(policyId, null);
    	if (subjectTypeMap == null || subjectTypeMap.isEmpty())
    	{
    		Map<Long, Subject> subjectMap = provider.getSubjectAssignmentOfPolicy(policyId, null);
    		if (subjectMap == null || subjectMap.isEmpty())
    		{
    			Map<Long, SubjectGroup> subjectGroupMap = provider.getSubjectGroupAssignmentOfPolicy(policyId, null);
    			if (subjectGroupMap == null || subjectGroupMap.isEmpty())
    				throwInvalidInputException("a valid Policy need at least one subject/subjectGroup assigned");
    		}
    	}
    		
    	currPolicy.setActive(Boolean.TRUE);
    	return provider.updatePolicy(
    				currPolicy, 
    				null,
    				loginSubject
    				);
    		
	}
}
