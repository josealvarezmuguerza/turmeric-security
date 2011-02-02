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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.Resources;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectTypeInfo;
import org.ebayopensource.turmeric.security.v1.services.Subjects;
import org.ebayopensource.turmeric.security.v1.services.Target;
import org.ebayopensource.turmeric.security.v1.services.UpdateMode;


class SelfProvisioningPolicy extends BasePolicyServiceImpl {

	static final String AUTHZ_POLICY_TYPE = "AUTHZ";
	//after a subjecGroup/policy be created, should call this one
	void createProvisioningPolicy( Object object, SubjectKey loginSubject) throws ServiceException, PolicyProviderException
	{
		if (loginSubject == null)
			return;
	
		String policyName = null;
		String objectName = null;
		Long objectId = null;

		boolean isSubjectGroup = false;
		if (object instanceof SubjectGroupKey  ) {
			SubjectGroupKey key = (SubjectGroupKey)object;
			objectId = key.getSubjectGroupId();
			objectName = key.getSubjectGroupName();
			policyName = "Admin" + "_SubjectGroup_" + objectName;
			isSubjectGroup = true;
		} else if (object instanceof PolicyKey) {
			PolicyKey key = (PolicyKey) object;
			objectId = key.getPolicyId();
			objectName = key.getPolicyName();
			policyName = "Admin" + "_Policy_" + objectName;
		} else {
			throwInvalidInputException("not valid input");
		}

		
		Subject creator = new Subject();
		creator.setSubjectName(loginSubject.getSubjectName());
		creator.setSubjectType(loginSubject.getSubjectType());
		
		Target target = new Target();
		Policy policy = new Policy();
		policy.setPolicyName(policyName);
		policy.setPolicyType(AUTHZ_POLICY_TYPE);
		policy.setDescription("Managing SubjectGroup " + objectName);
		policy.setTarget(target);

		Subjects subjects = new Subjects();
		target.setSubjects(subjects);
		
		SubjectGroup subjectGroup = new SubjectGroup();
		subjectGroup.setSubjectGroupName(policyName);
		subjectGroup.setDescription("Managing " + subjectGroup.getSubjectGroupName());
		subjectGroup.setSubjectType(creator.getSubjectType());
		subjectGroup.getSubject().add(creator);
		subjects.getSubjectGroup().add(subjectGroup);
	
	    SubjectGroupKey sgKeys = createSubjectGroup(subjectGroup, loginSubject);

		Long manageGroupId = sgKeys.getSubjectGroupId();
		if (manageGroupId == null)
		{
			throwInvalidInputException("Couldn't create managed subject group");
		}

		Resources resources = new Resources();
		target.setResources(resources);

		List<Resource> resourceList = getResourceFromObject(
				objectName,
				objectId,
				policyName,
				manageGroupId,
				isSubjectGroup,
				true,
				loginSubject);
		target.getResources().getResource().addAll(resourceList);

		PolicyKey policyKey = createPolicy(policy,loginSubject);
		policy.setPolicyId(policyKey.getPolicyId());

		if (policyKey != null)
		{
			enablePolicy(policyKey, loginSubject);
		}
	}

	void updateProvisioningPolicy(String oldName, Object object, SubjectKey loginSubject) throws ServiceException, PolicyProviderException
	{
		String policyNameToken = null;
		String newName = null;

		boolean isSubjectGroup = false;
		if (object instanceof SubjectGroupKey  ) {
			SubjectGroupKey key = (SubjectGroupKey)object;
			newName = key.getSubjectGroupName();
			policyNameToken = "Admin_SubjectGroup_";
			isSubjectGroup = true;
		} else if (object instanceof PolicyKey) {
			PolicyKey key = (PolicyKey) object;
			newName = key.getPolicyName();
			policyNameToken = "Admin_Policy_";
		} else {
			throwInvalidInputException("not valid input");
		}
		
		//change admin group name
		updateSubjectGroup(
				policyNameToken + oldName,
				policyNameToken + newName, loginSubject);
		
		PolicyKey policyKey = new PolicyKey();
		policyKey.setPolicyName(policyNameToken + oldName);
		policyKey.setPolicyType(AUTHZ_POLICY_TYPE);
		Policy currPolicy = getPolicyInfo(policyKey); 

		if (currPolicy == null)
			return;
		
		Policy inputPolicy = new Policy();
		inputPolicy.setPolicyId(currPolicy.getPolicyId());
		inputPolicy.setPolicyName(policyNameToken + newName);
		inputPolicy.setPolicyType(AUTHZ_POLICY_TYPE);
		
		Target target = new Target();
		inputPolicy.setTarget(target);
		
		Resources resources = new Resources();
		target.setResources(resources);
		
		//create necessary operation
		List<Resource> resourceListAdd = getResourceFromObject(
				newName,
				null,
				policyNameToken + newName,
				null,
				isSubjectGroup,
				true,
				loginSubject);
		
		resources.getResource().addAll(resourceListAdd);
		updatePolicy(inputPolicy, currPolicy, UpdateMode.UPDATE, loginSubject);
		
		//delete related operation
		List<Resource> resourceListRemove = getResourceFromObject(
				oldName,
				null,
				policyNameToken + oldName,
				null,
				isSubjectGroup,
				false,
				loginSubject);
		
		resources.getResource().clear();
		resources.getResource().addAll(resourceListRemove);
		updatePolicy(inputPolicy, currPolicy, UpdateMode.DELETE, loginSubject);
		
		for (Resource resource: resourceListRemove)
		{
			updateResource(resource, UpdateMode.DELETE, loginSubject);
		}
	
	}

	//after a subjecGroup/policy be deleted, should call this one
	void deleteProvisioningPolicy(
			Object object,
			SubjectKey loginSubject) throws ServiceException, PolicyProviderException
	{
		String policyName = null;
		String objectName = null;
		Long objectId = null;

		boolean isSubjectGroup = false;
		if (object instanceof SubjectGroupKey  ) {
			SubjectGroupKey key = (SubjectGroupKey)object;
			objectId = key.getSubjectGroupId();
			objectName = key.getSubjectGroupName();
			policyName = "Admin" + "_SubjectGroup_" + objectName;
			isSubjectGroup = true;
		} else if (object instanceof PolicyKey) {
			PolicyKey key = (PolicyKey) object;
			objectId = key.getPolicyId();
			objectName = key.getPolicyName();
			policyName = "Admin" + "_Policy_" + objectName;
		} else {
			throwInvalidInputException("not valid input");
		}

		PolicyKey policyKey = new PolicyKey();
		policyKey.setPolicyName(policyName);
		policyKey.setPolicyType(AUTHZ_POLICY_TYPE);

		if (getPolicyInfo(policyKey) != null)
			deletePolicy(policyKey);;
			
		//delete admin subject group
		SubjectGroupKey subjectGroupKey = 	getSubjectGroupByName(policyName);
		 
		if (subjectGroupKey != null)
			subjectGroupKey = deleteSubjectGroup(subjectGroupKey);;
		
		if (subjectGroupKey != null)
		{
			Long manageSubjectGroupId = subjectGroupKey.getSubjectGroupId();
			//delete related operation
			List<Resource> resourceListRemove = getResourceFromObject(
					objectName,
					objectId,
					policyName,
					manageSubjectGroupId,
					isSubjectGroup,
					false,
					loginSubject);
			
			for (Resource resource: resourceListRemove)
			{
				updateResource(resource, UpdateMode.DELETE, loginSubject);
			}
		}
	}

	private  void appendResourceMap(
			Map<String, List<String>> resourceMap,
			String resourceName,
			String objectName,
			Long objectId)
	{
		List<String> oplist = null;
		if (resourceMap.containsKey(resourceName))
		{
			oplist = resourceMap.get(resourceName);
		} else {
			oplist = new ArrayList<String>();
		}

		if (objectName != null)
			oplist.add(objectName);
		if (objectId != null)
			oplist.add(objectId.toString());

		resourceMap.put(resourceName, oplist);

	}

	private SubjectGroupKey getSubjectGroupByName(String name) throws ServiceException, PolicyProviderException
	{
		List<SubjectTypeInfo> subjectTypes  = getSubjectTypes();
		for (SubjectTypeInfo subjectType : subjectTypes)
		{
			SubjectGroupKey key  = new SubjectGroupKey();
			key.setSubjectGroupName(name);
			String type = subjectType.getName();
			key.setSubjectType(type);
			Map<Long, SubjectGroup> map = findSubjectGroup(key);
			if (map == null || map.isEmpty() )
				continue;
			
			Entry<Long, SubjectGroup> entry = map.entrySet().iterator().next();
			SubjectGroupKey subjectGroupKey = new SubjectGroupKey();
			SubjectGroup sg = entry.getValue();
			subjectGroupKey.setSubjectType(sg.getSubjectType());
			subjectGroupKey.setSubjectGroupName(sg.getSubjectGroupName());
			subjectGroupKey.setSubjectGroupId(entry.getKey());
			
			
			return subjectGroupKey;
		}
		
		return null;
	}

	private  List<Resource> constructResourceList(Map<String, List<String>> resourceMap)
	{
		List<Resource> resourceList = new ArrayList<Resource>();

		Iterator<String> resourceIter = resourceMap.keySet().iterator();
		while (resourceIter.hasNext())
		{
			String resourceName = resourceIter.next();
			Resource resource = new Resource();
			resource.setResourceType("OBJECT"); 
			resource.setResourceName(resourceName);

			List<Operation> operations = resource.getOperation();
			Iterator<String> opIter = resourceMap.get(resourceName).iterator();
			while (opIter.hasNext())
			{
				Operation operation = new Operation();
				operation.setOperationName(opIter.next());
				operations.add(operation);
			}
			resourceList.add(resource);
		}

		return resourceList;
	}

	private List<Resource> getResourceFromObject(
			String objectName,
			Long objectId,
			String manageGroupName,
			Long manageGroupId,
			boolean isSubjectGroup,
			boolean isCreateResource,
			SubjectKey loginSubject) throws PolicyProviderException, ServiceException
	{
		Map<String, List<String>> resourceMap = new HashMap<String,List<String>>();

		if (isSubjectGroup)
		{
			appendResourceMap(
					resourceMap,
					"SERVICE.PolicyService.deleteSubjectGroups",
					objectName,
					objectId);

			appendResourceMap(
					resourceMap,
					"SERVICE.PolicyService.updateSubjectGroups",
					objectName,
					objectId);

		} else {
			appendResourceMap(
					resourceMap,
					"SERVICE.PolicyService.deletePolicy",
					objectName,
					objectId);

			appendResourceMap(
					resourceMap,
					"SERVICE.PolicyService.updatePolicy",
					objectName,
					objectId);
			
			appendResourceMap(
					resourceMap,
					"SERVICE.PolicyService.enablePolicy",
					objectName,
					objectId);
			
			appendResourceMap(
					resourceMap,
					"SERVICE.PolicyService.disablePolicy",
					objectName,
					objectId);
		}

		appendResourceMap(
				resourceMap,
				"SERVICE.PolicyService.updateSubjectGroups",
				manageGroupName,
				manageGroupId);
		


		List<Resource> resourceList = constructResourceList(resourceMap);

		Iterator<Resource> resourceIter;
		if (isCreateResource)
		{
			//create necessary operation
			resourceIter = resourceList.iterator();
			while (resourceIter.hasNext())
			{
				Resource resource = resourceIter.next();
				ResourceKey key = new ResourceKey();
				key.setResourceName(resource.getResourceName());
				key.setResourceId(resource.getResourceId());
				key.setResourceType(resource.getResourceType());
				Resource currResource = getResourceInfo(key);
				
				if (currResource == null)
				{
					createResource(resource, loginSubject);
				} else {
					updateResource(resource, UpdateMode.UPDATE, loginSubject);
				}
			}
		}
		return resourceList;
	}

	
	private SubjectGroupKey updateSubjectGroup(String oldName,String newName, SubjectKey loginSubject) throws ServiceException, PolicyProviderException
	{
		SubjectGroupKey subjectGroupKey = getSubjectGroupByName(oldName);
		
		if (subjectGroupKey == null)
			return null;

		SubjectGroup subjectGroup = new SubjectGroup();
		subjectGroup.setSubjectType(subjectGroupKey.getSubjectType());
		subjectGroup.setSubjectGroupName(oldName);
		Utils.setSubjectGroupId(subjectGroup, subjectGroupKey.getSubjectGroupId() );
		
		SubjectGroup newSubjectGroup = new SubjectGroup();
		newSubjectGroup.setSubjectType(subjectGroup.getSubjectType());
		newSubjectGroup.setSubjectGroupName(newName);
		Utils.setSubjectGroupId(newSubjectGroup, subjectGroupKey.getSubjectGroupId() );
		
		
		
		return updateSubjectGroup(newSubjectGroup, subjectGroup, 
				UpdateMode.UPDATE, loginSubject);
		
	}
	
}
