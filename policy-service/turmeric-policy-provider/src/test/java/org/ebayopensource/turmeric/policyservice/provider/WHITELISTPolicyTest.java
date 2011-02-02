/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ebayopensource.turmeric.policyservice.provider.common.PolicyEditObject;
import org.ebayopensource.turmeric.policyservice.provider.common.ResourcesEditObject;
import org.ebayopensource.turmeric.policyservice.provider.common.SubjectsEditObject;
import org.ebayopensource.turmeric.security.v1.services.EntityHistory;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.utils.jpa.EntityManagerContext;
import org.junit.Test;

public class WHITELISTPolicyTest extends PolicyTestBase {
    @Test
    public void createPolicyTest() throws Exception {
        SubjectKey userKey = getUserKey("jdoe");
        USERSubject us = new USERSubject();
        GENERICResource gr = new GENERICResource();
        WHITELISTPolicy wlp = new WHITELISTPolicy();
        
        Policy policy = new Policy();
        policy.setPolicyName("adminwl");
        policy.setPolicyType("WHITELIST");
        policy.setDescription("admin whitelist policy");
        
        PolicyEditObject polEdObj = new PolicyEditObject();
        
        SubjectsEditObject subEdObj = new SubjectsEditObject();
        polEdObj.setSubjectsEditObject(subEdObj);
        
        List<Long> addSubjList = subEdObj.getAddSubjectList();
        addSubjList.add(us.getSubjectByName("admin").keySet().toArray(new Long[1])[0]);
        List<Long> addSubjGrpList = subEdObj.getAddSubjectGroupList();
        addSubjGrpList.add(us.getSubjectGroupInfoByName("managers").keySet().toArray(new Long[1])[0]);

        List<Long> addExSubjList = subEdObj.getAddExclusionSubjectList();
        addExSubjList.add(us.getSubjectByName("guest").keySet().toArray(new Long[1])[0]);
        List<Long> addExSubjGrpList = subEdObj.getAddExclusionSubjectGroupList();
        addExSubjGrpList.add(us.getSubjectGroupInfoByName("guests").keySet().toArray(new Long[1])[0]);

        ResourcesEditObject resEdObj = new ResourcesEditObject();
        polEdObj.setResourcesEditObject(resEdObj);
        
        List<Long> addResList = resEdObj.getAddResourceList();
        addResList.add(gr.getResourceInfoByName("adminsvc").getResourceId());
        List<Long> addOpList = resEdObj.getAddOperationList();
        addOpList.add(gr.getOperationByName("adminsvc", "LOGIN").getOperationId());
        
        PolicyKey policyKey = wlp.createPolicy(policy, polEdObj, userKey);
        Long policyId = policyKey.getPolicyId();
        
        EntityManagerContext.open(factory);
        try {
            org.ebayopensource.turmeric.policyservice.model.Policy savedPolicy =
                EntityManagerContext.get().find(
                    org.ebayopensource.turmeric.policyservice.model.Policy.class, 
                    policyKey.getPolicyId());
            assertNotNull(savedPolicy);
            
            Map<Long, Subject> subjects = wlp.getSubjectAssignmentOfPolicy(policyId, null);
            assertEquals(1, subjects.size());

            Map<Long, SubjectGroup> subjectGroups = wlp.getSubjectGroupAssignmentOfPolicy(policyId, null);
            assertEquals(1, subjectGroups.size());

            Map<Long, Subject> exclusionSubjects = wlp.getExclusionSubjectAssignmentOfPolicy(policyId, null);
            assertEquals(1, exclusionSubjects.size());

            Map<Long, SubjectGroup> exclusionSubjectGroups = wlp.getExclusionSubjectGroupAssignmentOfPolicy(policyId, null);
            assertEquals(1, exclusionSubjectGroups.size());

            Map<Long, Resource> resources = wlp.getResourceAssignmentOfPolicy(policyId, null);
            assertEquals(1, resources.size());

            Map<Long, Operation> operations = wlp.getOperationAssignmentOfPolicy(policyId, null);
            assertEquals(1, operations.size());  
        } finally {
            EntityManagerContext.close();
        }
    }
    
    @Test
    public void updatePolicyTest() throws Exception {
        createPolicyTest();
  
        SubjectKey userKey = getUserKey("jdoe");
        USERSubject us = new USERSubject();
        GENERICResource gr = new GENERICResource();

        WHITELISTPolicy wlp = new WHITELISTPolicy();
        Policy policy = wlp.getPolicyInfo("adminwl");
        long policyId = policy.getPolicyId();
        
        PolicyEditObject polEdObj = new PolicyEditObject();
        polEdObj.setPolicyId(policyId);
        
        SubjectsEditObject subEdObj = new SubjectsEditObject();
        polEdObj.setSubjectsEditObject(subEdObj);

        List<Long> addSubjList = subEdObj.getAddSubjectList();
        addSubjList.add(us.getSubjectByName("manager").keySet().toArray(new Long[1])[0]);
        List<Long> remSubjList = subEdObj.getRemoveSubjectList();
        remSubjList.add(us.getSubjectByName("admin").keySet().toArray(new Long[1])[0]);
        
        List<Long> addSubjGrpList = subEdObj.getAddSubjectGroupList();
        addSubjGrpList.add(us.getSubjectGroupInfoByName("helpdesk").keySet().toArray(new Long[1])[0]);
        List<Long> remSubjGrpList = subEdObj.getRemoveSubjectGroupList();
        remSubjGrpList.add(us.getSubjectGroupInfoByName("managers").keySet().toArray(new Long[1])[0]);

        ResourcesEditObject resEdObj = new ResourcesEditObject();
        polEdObj.setResourcesEditObject(resEdObj);
        
        List<Long> addResList = resEdObj.getAddResourceList();
        addResList.add(gr.getResourceInfoByName("crmsvc").getResourceId());
        List<Long> remResList = resEdObj.getRemoveResourceList();
        remResList.add(gr.getResourceInfoByName("adminsvc").getResourceId());

        List<Long> addOpList = resEdObj.getAddOperationList();
        addOpList.add(gr.getOperationByName("crmsvc", "START").getOperationId());
        List<Long> remOpList = resEdObj.getRemoveOperationList();
        remOpList.add(gr.getOperationByName("adminsvc", "LOGIN").getOperationId());

        policy.setDescription("updated description");
        wlp.updatePolicy(policy, polEdObj, userKey);
        
        EntityManagerContext.open(factory);
        try {
            Policy policyInfo = wlp.getPolicyInfo(policyId);
            assertEquals("updated description", policyInfo.getDescription());
            
            Query query = EntityManagerContext.get().createQuery("select pl.subjects from " +
                "org.ebayopensource.turmeric.policyservice.model.Policy as pl " + 
                "where pl.id = " + policyId);
            List<org.ebayopensource.turmeric.policyservice.model.Subject> subjectList =
                query.getResultList();
            assertEquals(1, subjectList.size());
            assertEquals("manager", subjectList.get(0).getSubjectName());
            
            query = EntityManagerContext.get().createQuery("select pl.subjectGroups from " +
                "org.ebayopensource.turmeric.policyservice.model.Policy as pl " + 
                "where pl.id = " + policyId);
            List<org.ebayopensource.turmeric.policyservice.model.SubjectGroup> subjectGroupList =
                query.getResultList();
            assertEquals(1, subjectGroupList.size());
            assertEquals("helpdesk", subjectGroupList.get(0).getSubjectGroupName());
        
            query = EntityManagerContext.get().createQuery("select pl.resources from " +
                "org.ebayopensource.turmeric.policyservice.model.Policy as pl " + 
                "where pl.id = " + policyId);
            List<org.ebayopensource.turmeric.policyservice.model.Resource> resourceList =
                query.getResultList();
            assertEquals(1, resourceList.size());
            assertEquals("crmsvc", resourceList.get(0).getResourceName());
            
            query = EntityManagerContext.get().createQuery("select pl.operations from " +
                "org.ebayopensource.turmeric.policyservice.model.Policy as pl " + 
                "where pl.id = " + policyId);
            List<org.ebayopensource.turmeric.policyservice.model.Operation> operationList =
                query.getResultList();
            assertEquals(1, operationList.size());
            assertEquals("START", operationList.get(0).getOperationName());
        } finally {
            EntityManagerContext.close();
        }
    }
    
    @Test
    public void deletePolicyTest() throws Exception {
        WHITELISTPolicy wlp = new WHITELISTPolicy();
        Policy result = wlp.getPolicyInfo("globalwl");
        long policyId = result.getPolicyId();
        wlp.deletePolicy(policyId);
        
        EntityManagerContext.open(factory);
        try {
            org.ebayopensource.turmeric.policyservice.model.Policy savedPolicy =
                EntityManagerContext.get().find(
                    org.ebayopensource.turmeric.policyservice.model.Policy.class, 
                    policyId);
            assertNull(savedPolicy);
        } finally {
            EntityManagerContext.close();
        }
    }
    
    @Test
    public void findPolicyTest() throws Exception {
        createPolicyTest();

        USERSubject us = new USERSubject();
        GENERICResource gr = new GENERICResource();
        WHITELISTPolicy wlp = new WHITELISTPolicy();
        Policy policy = wlp.getPolicyInfo("adminwl");
        Long policyId = policy.getPolicyId();
        
        Map<Long, Policy> policies = null;
        Set<Long> subjects = new HashSet();
        subjects.add(us.getSubjectByName("admin").keySet().toArray(new Long[1])[0]);
        policies = wlp.findPolicyInfoBySubject(subjects, null);
        assertEquals(policyId, policies.keySet().toArray(new Long[1])[0]);
        
        Set<Long> subjectGroups = new HashSet();
        subjectGroups.add(us.getSubjectGroupInfoByName("managers").keySet().toArray(new Long[1])[0]);
        policies = wlp.findPolicyInfoBySubjectGroup(subjectGroups, null);
        assertEquals(policyId, policies.keySet().toArray(new Long[1])[0]);
        
        Set<Long> resources = new HashSet();
        resources.add(gr.getResourceInfoByName("adminsvc").getResourceId());
        policies = wlp.findPolicyInfoByResource(resources, null);
        assertEquals(policyId, policies.keySet().toArray(new Long[1])[0]);
 
        Set<Long> operations = new HashSet();
        operations.add(gr.getOperationByName("adminsvc", "LOGIN").getOperationId());
        policies = wlp.findPolicyInfoByOperation(operations, null);
        assertEquals(policyId, policies.keySet().toArray(new Long[1])[0]);
        
        PolicyKey policyKey = new PolicyKey();
        policyKey.setPolicyType(wlp.getPolicyType());
        policyKey.setPolicyName("%wl%");
        policies = wlp.findPolicyInfo(policyKey, null, null);
        assertEquals(2, policies.size());
    }

    private Policy newPolicy(String name, String type, String desc) {
        Policy policy = new Policy();
        policy.setPolicyName(name);
        policy.setPolicyType(type);
        policy.setDescription(desc);
        return policy;
    }

    private SubjectKey getUserKey(String name) throws Exception
    {
        USERSubject userProvider = new USERSubject();
        Map<Long, Subject> usrMap = userProvider.getSubjectByName(name);

        SubjectKey userKey = new SubjectKey();
        Subject subject = usrMap.values().toArray(new Subject[1])[0];
        userKey.setSubjectId((Long)usrMap.keySet().toArray(new Long[1])[0]);
        userKey.setSubjectName(subject.getSubjectName());
        userKey.setSubjectType(subject.getSubjectType());
        
        return userKey;        
    }
}
