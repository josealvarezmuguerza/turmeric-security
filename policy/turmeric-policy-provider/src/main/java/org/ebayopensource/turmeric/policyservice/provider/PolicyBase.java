/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.provider;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.ebayopensource.turmeric.policyservice.exceptions.PolicyCreationException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyDeleteException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyFinderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyUpdateException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.Category;
import org.ebayopensource.turmeric.policyservice.provider.PolicyTypeProvider;
import org.ebayopensource.turmeric.policyservice.provider.common.PolicyBuilderObject;
import org.ebayopensource.turmeric.policyservice.provider.common.PolicyEditObject;
import org.ebayopensource.turmeric.policyservice.provider.common.ResourcesEditObject;
import org.ebayopensource.turmeric.policyservice.provider.common.SubjectsEditObject;
import org.ebayopensource.turmeric.policyservice.provider.utils.QueryConditionHelper;
import org.ebayopensource.turmeric.security.v1.services.EntityHistory;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePair;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.Query;
import org.ebayopensource.turmeric.security.v1.services.QueryCondition;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Rule;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectTypeInfo;


public abstract class PolicyBase implements PolicyTypeProvider {
	protected abstract String getPolicyType();
	
	protected String getQueryValue(QueryCondition queryCondition,
            String queryType) {
        if (queryCondition != null) {
            for (Query query : queryCondition.getQuery()) {
                if (query.getQueryType().equalsIgnoreCase(queryType))
                    return query.getQueryValue();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.ebayopensource.turmeric.policyservice.provider.PolicyTypeProvider#createPolicy(org.ebayopensource.turmeric.services.Policy, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
     */
    public PolicyKey createPolicy(
            Policy inputPolicy,
            PolicyEditObject policyEditObject,
            SubjectKey createdBy) throws PolicyUpdateException,
            PolicyCreationException {

        PolicyKey policyKey = createPolicyInfo(inputPolicy, createdBy);
        policyEditObject.setPolicyId(policyKey.getPolicyId());
        inputPolicy.setActive(Boolean.FALSE);
        try {
            return updatePolicy(inputPolicy, policyEditObject, createdBy);
        } catch (PolicyDeleteException e) {
            //Should not happen.
            throw new PolicyUpdateException(Category.POLICY, 
                    getPolicyType(), "Failed to create policy", e);
        }
    }

	/* (non-Javadoc)
     * @see org.ebayopensource.turmeric.policyservice.provider.PolicyTypeProvider#updatePolicy(java.lang.Long, org.ebayopensource.turmeric.services.Policy, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
     */
    public PolicyKey updatePolicy(
            Policy inputPolicy,
            PolicyEditObject policyEditObject,
            SubjectKey modifiedBy) throws PolicyUpdateException,
            PolicyCreationException, PolicyDeleteException {
        Boolean isModified = false;

        if (policyEditObject != null) {
	        Long policyId = policyEditObject.getPolicyId();
	        if (policyEditObject.getRuleEditObject() != null)
	            isModified = updateRuleOfPolicy(policyId, policyEditObject
	                    .getRuleEditObject().getRemoveList(), policyEditObject
	                    .getRuleEditObject().getAddList())
	                    || isModified;
	
	        if (policyEditObject.getResourcesEditObject() != null)
	            isModified = updateResourcesOfPolicy(policyId, policyEditObject
	                    .getResourcesEditObject())
	                    || isModified;
	
	        if (policyEditObject.getSubjectsEditObject() != null)
	            isModified = updateSubjectsOfPolicy(policyId, policyEditObject
	                    .getSubjectsEditObject())
	                    || isModified;
	
	        inputPolicy.setPolicyId(policyId);
        }

        return updatePolicyInfo(inputPolicy, modifiedBy);

    }

	protected boolean updateResourcesOfPolicy(
            Long policyId,
            ResourcesEditObject resourcesEditObject)
            throws PolicyCreationException, PolicyUpdateException,
            PolicyDeleteException {

        boolean isModified = false;

        if (resourcesEditObject == null)
            return isModified;

        List<Long> removeResourceList = resourcesEditObject
                .getRemoveResourceList();
        if (removeResourceList != null && !removeResourceList.isEmpty()) {
            removeResourceAssignmentOfPolicy(policyId, resourcesEditObject
                    .getRemoveResourceList());
            isModified = true;
        }

        List<Long> addResourceList = resourcesEditObject.getAddResourceList();
        if (addResourceList != null && !addResourceList.isEmpty()) {
            addResourceAssignmentOfPolicy(policyId, addResourceList);
            isModified = true;
        }

        List<Long> removeOperationList = resourcesEditObject
                .getRemoveOperationList();
        if (removeOperationList != null && !removeOperationList.isEmpty()) {
            removeOperationAssignmentOfPolicy(policyId, removeOperationList);
            isModified = true;
        }

        List<Long> addOperationList = resourcesEditObject.getAddOperationList();
        if (addOperationList != null && !addOperationList.isEmpty()) {
            addOperationAssignmentOfPolicy(policyId, addOperationList);
            isModified = true;
        }

        return isModified;
    }

	protected boolean updateSubjectsOfPolicy(
            Long policyId,
            SubjectsEditObject policyEditObject)
            throws PolicyCreationException, PolicyUpdateException,
            PolicyDeleteException {
        boolean isModified = false;

        List<Long> removeSubjectTypeList = policyEditObject
                .getRemoveSubjectTypeList();
        if (!removeSubjectTypeList.isEmpty()) {
            removeSubjectTypeAssignmentOfPolicy(policyId, removeSubjectTypeList);
            isModified = true;
        }

        List<Long> addSubjectTypeList = policyEditObject
                .getAddSubjectTypeList();
        if (!addSubjectTypeList.isEmpty()) {
            addSubjectTypeAssignmentOfPolicy(policyId, addSubjectTypeList);
            isModified = true;
        }

        List<Long> removeSubjectList = policyEditObject.getRemoveSubjectList();
        if (!removeSubjectList.isEmpty()) {
            removeSubjectAssignmentOfPolicy(policyId, removeSubjectList);
            isModified = true;
        }

        List<Long> addSubjectList = policyEditObject.getAddSubjectList();
        if (addSubjectList != null && !addSubjectList.isEmpty()) {
            addSubjectAssignmentOfPolicy(policyId, addSubjectList);
            isModified = true;
        }

        List<Long> removeExclusionSubjectList = policyEditObject
                .getRemoveExclusionSubjectList();
        if (!removeExclusionSubjectList.isEmpty()) {
            removeExclusionSubjectAssignmentOfPolicy(policyId,
                    removeExclusionSubjectList);
            isModified = true;
        }

        List<Long> addExclusionSubjectList = policyEditObject
                .getAddExclusionSubjectList();
        if (!addExclusionSubjectList.isEmpty()) {
            addExclusionSubjectAssignmentOfPolicy(policyId,
                    addExclusionSubjectList);
            isModified = true;
        }

        List<Long> removeSubjectGroupList = policyEditObject
                .getRemoveSubjectGroupList();
        if (!removeSubjectGroupList.isEmpty()) {
            removeSubjectGroupAssignmentOfPolicy(policyId,
                    removeSubjectGroupList);
            isModified = true;
        }

        List<Long> addSubjectGroupList = policyEditObject
                .getAddSubjectGroupList();
        if (!addSubjectGroupList.isEmpty()) {
            addSubjectGroupAssignmentOfPolicy(policyId, addSubjectGroupList);
            isModified = true;
        }

        List<Long> removeExclusionSubjectGroupList = policyEditObject
                .getRemoveExclusionSubjectGroupList();
        if (!removeExclusionSubjectGroupList.isEmpty()) {
            removeExclusionSubjectGroupAssignmentOfPolicy(policyId,
                    removeExclusionSubjectGroupList);
            isModified = true;
        }

        List<Long> addExclusionSubjectGroupList = policyEditObject
                .getAddExclusionSubjectGroupList();
        if (!addExclusionSubjectGroupList.isEmpty()) {
            addExclusionSubjectGroupAssignmentOfPolicy(policyId,
                    addExclusionSubjectGroupList);
            isModified = true;
        }

        return isModified;
    }

    public PolicyBuilderObject applyQueryCondition(
            PolicyBuilderObject builderObject,
            QueryCondition queryCondition) {

        QueryConditionHelper queryConditionHelper = new QueryConditionHelper(
                queryCondition);
        if (queryConditionHelper.isIdMasked()) {

            Map<Long, Resource> resources = builderObject.getResources();
            Map<Long, Resource> maskedResources = new HashMap<Long, Resource>();
            for (Long id : resources.keySet()) {
                Long maskedId = maskHighOrderBits(id);
                Resource resource = resources.get(id);
                resource.setResourceId(maskedId);
                List<Operation> operations = resource.getOperation();
                for (Operation operation : operations) {
                    operation.setOperationId(maskHighOrderBits(operation
                            .getOperationId()));
                }
                maskedResources.put(maskedId, resource);
            }
            builderObject.setResources(maskedResources);

            Map<Long, Rule> rules = builderObject.getRules();
            Map<Long, Rule> maskedRules = new HashMap<Long, Rule>();
            for (Long id : rules.keySet()) {
                Long maskedId = maskHighOrderBits(id);
                Rule rule = rules.get(id);
                rule.setRuleId(maskedId);
                maskedRules.put(maskedId, rule);
            }
            builderObject.setRules(maskedRules);

            builderObject.setInclusionSubjectGrps(maskIds(builderObject
                    .getInclusionSubjectGrps()));
            builderObject.setInclusionSubjects(maskIds(builderObject
                    .getInclusionSubjects()));
            builderObject.setExclusionSubjectGrps(maskIds(builderObject
                    .getExclusionSubjectGrps()));
            builderObject.setExclusionSubjects(maskIds(builderObject
                    .getExclusionSubjects()));
        }
        return builderObject;
    }

    protected <T> Map<Long, T> maskIds(Map<Long, T> objs) {
        Map<Long, T> maskedObjs = new HashMap<Long, T>();
        for (Long id : objs.keySet()) {
            Long maskedId = maskHighOrderBits(id);
            maskedObjs.put(maskedId, objs.get(id));
        }
        return maskedObjs;
    }

    protected Long maskHighOrderBits(Long l) {
        return Long.valueOf(l.intValue() & 0x7fffffff);
    }

    public abstract List<EntityHistory> getAuditHistory(PolicyKey policyKey,
                    XMLGregorianCalendar startDate, XMLGregorianCalendar endDate)
                    throws PolicyFinderException;

    public abstract void audit(PolicyKey policyKey, String operationType, SubjectKey loginSubject)
                    throws PolicyFinderException;

    protected abstract PolicyKey createPolicyInfo(Policy inputPolicy, SubjectKey createdBy)
                    throws PolicyCreationException;

    protected abstract PolicyKey updatePolicyInfo(Policy inputPolicy, SubjectKey modifiedBy)
                    throws PolicyUpdateException;

    protected abstract boolean updateRuleOfPolicy(Long policyId, List<Rule> removeList,
                    List<Rule> addList) throws PolicyUpdateException;

    protected abstract void addOperationAssignmentOfPolicy(Long policyId,
                    List<Long> addOperationList) throws PolicyUpdateException;

    protected abstract void removeOperationAssignmentOfPolicy(Long policyId,
                    List<Long> removeOperationList) throws PolicyUpdateException;

    protected abstract void addResourceAssignmentOfPolicy(Long policyId, List<Long> addResourceList)
                    throws PolicyUpdateException;

    protected abstract void removeResourceAssignmentOfPolicy(Long policyId,
                    List<Long> removeResourceList) throws PolicyUpdateException;

    protected abstract void addExclusionSubjectGroupAssignmentOfPolicy(Long policyId,
                    List<Long> addExclusionSubjectGroupList) throws PolicyUpdateException;

    protected abstract void removeExclusionSubjectGroupAssignmentOfPolicy(Long policyId,
                    List<Long> removeExclusionSubjectGroupList) throws PolicyUpdateException;

    protected abstract void addSubjectGroupAssignmentOfPolicy(Long policyId,
                    List<Long> addSubjectGroupList) throws PolicyUpdateException;

    protected abstract void removeSubjectGroupAssignmentOfPolicy(Long policyId,
                    List<Long> removeSubjectGroupList) throws PolicyUpdateException;

    protected abstract void addExclusionSubjectAssignmentOfPolicy(Long policyId,
                    List<Long> addExclusionSubjectList) throws PolicyUpdateException;

    protected abstract void removeExclusionSubjectAssignmentOfPolicy(Long policyId,
                    List<Long> removeExclusionSubjectList) throws PolicyUpdateException;

    protected abstract void addSubjectAssignmentOfPolicy(Long policyId, List<Long> addSubjectList)
                    throws PolicyUpdateException;

    protected abstract void removeSubjectAssignmentOfPolicy(Long policyId,
                    List<Long> removeSubjectList) throws PolicyUpdateException;

    protected abstract void addSubjectTypeAssignmentOfPolicy(Long policyId,
                    List<Long> addSubjectTypeList) throws PolicyUpdateException;

    protected abstract void removeSubjectTypeAssignmentOfPolicy(Long policyId,
                    List<Long> removeSubjectTypeList) throws PolicyUpdateException;
}
