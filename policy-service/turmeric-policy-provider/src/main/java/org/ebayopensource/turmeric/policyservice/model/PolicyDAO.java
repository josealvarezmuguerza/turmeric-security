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

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;

public interface PolicyDAO {

    public void persistPolicy(Policy jpaPolicy);

    public Policy findPolicyById(Long policyId);

    public Policy findPolicyByName(String policyName);

    public Operation findOperationById(Long operationId);

    public Resource findResourceById(Long resourceId);

    public Subject findSubjectById(Long subjectId);

    public SubjectGroup findSubjectGroupById(Long subjectGroupId);

    public List<Policy> findPolicyBySubjectId(Long subjectId, String policyType);

    public List<Policy> findPolicyBySubjectGroupId(Long subjGrpId, String policyType);

    public List<Policy> findPolicyByOperationId(Long opId, String policyType);

    public List<Policy> findPolicyByResourceId(Long resId, String policyType);

    void removePolicy(long id);

    public List<Policy> findAllByName(String name, String policyType);

    public List<Policy> findAllByType(String policyType);

    public void audit(PolicyKey policyKey, String operationType, SubjectKey loginSubject);

    public List<AuditHistory> getPolicyHistory(long policyId, Date start, Date end);

    public List<Policy> findPolicyByExclusionSubjectId(Long subjectId, String policyType);

    public List<Policy> findPolicyByExclusionSubjectGroupId(Long subjGrpId, String policyType);
}
