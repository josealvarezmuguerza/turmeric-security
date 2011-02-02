/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.ebayopensource.turmeric.policyservice.exceptions.PolicyFinderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.Category;
import org.ebayopensource.turmeric.policyservice.provider.utils.RuleHelper;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.utils.jpa.AbstractDAO;
import org.ebayopensource.turmeric.utils.jpa.model.AuditInfo;

public class PolicyDAOImpl extends AbstractDAO implements PolicyDAO {
    
    @Override
    public void persistPolicy(Policy policy) {
        persistEntity(policy);
    }
    
    @Override
    public void removePolicy(long id) {
        removeEntity(Policy.class, id);
    }

   @Override
    public Policy findPolicyById(Long id) {
        return findEntity(Policy.class, id);
    }

    @Override
    public Policy findPolicyByName(String name) {
        return getSingleResultOrNull(Policy.class, "policyName", name);
    }

    @Override
    public Operation findOperationById(Long id) {
        return findEntity(Operation.class, id);
    }

    @Override
    public Resource findResourceById(Long id) {
        return findEntity(Resource.class, id);
    }

    @Override
    public Subject findSubjectById(Long id) {
        return findEntity(Subject.class, id);
    }

    @Override
    public SubjectGroup findSubjectGroupById(Long id) {
        return findEntity(SubjectGroup.class, id);
    }

    @Override
    public List<Policy> findPolicyBySubjectId(Long subjectId, String policyType) {
        return findEntityByMemberId(Policy.class, "policyType", policyType, "subjects", subjectId);
    }

    @Override
    public List<Policy> findPolicyBySubjectGroupId(Long subjGrpId, String policyType) {
        return findEntityByMemberId(Policy.class, "policyType", policyType, "subjectGroups", subjGrpId);
    }

    @Override
    public List<Policy> findPolicyByOperationId(Long opId, String policyType) {
        return findEntityByMemberId(Policy.class, "policyType", policyType, "operations", opId);
    }

    @Override
    public List<Policy> findPolicyByResourceId(Long resId, String policyType) {
        return findEntityByMemberId(Policy.class, "policyType", policyType, "resources", resId);
    }

    @Override
    public List<Policy> findAllByName(String name, String policyType) {
        return getWildcardResultList(Policy.class, "policyType", policyType, "policyName", name);
    }

    @Override
    public List<Policy> findAllByType(String policyType) {
        return getResultList(Policy.class, "policyType", policyType);
    }

    public static Policy convert(org.ebayopensource.turmeric.security.v1.services.Policy policy){
        Boolean value;
        if(policy == null){
        	return null;
        }
        
        final List<org.ebayopensource.turmeric.policyservice.model.Rule> ruleList = 
            new ArrayList<org.ebayopensource.turmeric.policyservice.model.Rule>();
        final List<org.ebayopensource.turmeric.security.v1.services.Rule> rules= 
            policy.getRule();
        if (rules != null && !rules.isEmpty()) {
            for (org.ebayopensource.turmeric.security.v1.services.Rule rule: rules) {
            	Rule converted = RuleHelper.convert(rule);
            	if(converted != null){
            		ruleList.add(converted);	
            	}
            }
        }
        
        Policy jpaPolicy = new Policy();
        jpaPolicy.setPolicyName(policy.getPolicyName());
        jpaPolicy.setPolicyType(policy.getPolicyType());
        jpaPolicy.setDescription(policy.getDescription());
        jpaPolicy.setActive((value = policy.isActive())==null ? false:value);
        jpaPolicy.getRules().addAll(ruleList);
        return jpaPolicy;
    }

    public static org.ebayopensource.turmeric.security.v1.services.Policy convert(Policy jpaPolicy)
            throws PolicyFinderException {
        if( jpaPolicy==null) {
        	return null;
        }
        org.ebayopensource.turmeric.security.v1.services.Policy policy = 
            new org.ebayopensource.turmeric.security.v1.services.Policy();

        policy.setPolicyId(jpaPolicy.getId());
        policy.setPolicyName(jpaPolicy.getPolicyName());
        policy.setPolicyType(jpaPolicy.getPolicyType());
        policy.setActive(jpaPolicy.isActive());
        policy.setDescription(jpaPolicy.getDescription());

        if(jpaPolicy.getRules() != null && jpaPolicy.getRules().size() > 0){
            policy.getRule().addAll(RuleHelper.convert(jpaPolicy.getRules()));
         }    

        AuditInfo auditInfo = jpaPolicy.getAuditInfo();
        if (auditInfo != null) {
            policy.setCreatedBy(auditInfo.getCreatedBy());
            policy.setLastModifiedBy(auditInfo.getUpdatedBy());
    
            try {
                GregorianCalendar updatedOn = new GregorianCalendar();
                Date updateDate = auditInfo.getUpdatedOn();
                updatedOn.setTime(updateDate == null ? auditInfo.getCreatedOn() : updateDate);
                policy.setLastModified(DatatypeFactory.newInstance().newXMLGregorianCalendar(
                                updatedOn));
            }
            catch (DatatypeConfigurationException ex) {
                throw new PolicyFinderException(Category.POLICY, jpaPolicy.getPolicyType(),
                                null, jpaPolicy.getPolicyName(), "Failed to convert policy", ex);
            }
        }
        return policy;
    }

    @Override
    public List<AuditHistory> getPolicyHistory(long policyId, Date start, Date end) {
        return getResultList(AuditHistory.class, "category", Category.POLICY.name(), 
                        "entityId", policyId, "auditInfo.createdOn", start, end);
    }

    @Override
    public void audit(PolicyKey policyKey, String operationType, SubjectKey loginSubject) {
        persistEntity(AuditHistory.newRecord(policyKey, operationType, loginSubject));
    }

    @Override
    public List<Policy> findPolicyByExclusionSubjectId(Long subjectId, String policyType) {
        return findEntityByMemberId(Policy.class, "policyType", policyType, "exclusionSubjects", subjectId);
    }

    @Override
    public List<Policy> findPolicyByExclusionSubjectGroupId(Long subjGrpId, String policyType) {
        return findEntityByMemberId(Policy.class, "policyType", policyType, "exclusionSubjectGroups", subjGrpId);
    }
}
