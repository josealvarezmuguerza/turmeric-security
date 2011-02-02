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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.ebayopensource.turmeric.policyservice.provider.common.SubjectGroupEditObject;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectTypeInfo;
import org.ebayopensource.turmeric.utils.jpa.EntityManagerContext;
import org.junit.Test;


public class IPSubjectTest extends SubjectTestBase {
    @Test
    public void getSubjectTest() throws Exception {
        SubjectKey userKey = getUserKey("jdoe");
        IPSubject ip = new IPSubject();

        Subject[] hosts = {
            newSubject("router", "IP", "router", "172.16.1.1", "support@ebayopensource.org"),
            newSubject("localip", "IP", "locaip", "172.16.1.100", "support@ebayopensource.org"),
            newSubject("localhost", "IP", "localhost", "127.0.0.1", "support@ebayopensource.org"),
        };
        int len = hosts.length;
        
        int idx = -1;
        SubjectKey[] keys = new SubjectKey[len];
        for (Subject host : hosts) {
            keys[++idx] = ip.createSubject(host, userKey);
            assertEquals(hosts[idx].getSubjectName(), keys[idx].getSubjectName());
        }
        
        for (SubjectKey key : keys) {
            Long subjectId = key.getSubjectId();
            Map<Long, Subject> result = ip.getSubjectById(subjectId);

            assertNotNull(result);
            
            Subject value = result.values().toArray(new Subject[1])[0];
            assertEquals(subjectId, result.keySet().toArray(new Long[1])[0]);
            assertEquals(key.getSubjectName(), value.getSubjectName());
         }

        for (SubjectKey key : keys) {
            String subjectName = key.getSubjectName();
            Map<Long, Subject> result = ip.getSubjectByName(subjectName);
            
            assertNotNull(result);
            
            Subject value = result.values().toArray(new Subject[1])[0];
            assertEquals(key.getSubjectId(), result.keySet().toArray(new Long[1])[0]);
            assertEquals(key.getSubjectName(), value.getSubjectName());
         }
    }
    
    @Test
    public void createSubjectTest() throws Exception {
        SubjectKey userKey = getUserKey("jdoe");
        IPSubject ip = new IPSubject();
        
        Subject localhost = newSubject("localhost", "IP", "localhost", "127.0.0.1/24", "support@ebayopensource.org");
        SubjectKey ipKey = ip.createSubject(localhost, userKey);
        
        EntityManagerContext.open(factory);
        try {
            org.ebayopensource.turmeric.policyservice.model.Subject savedSubject =
                EntityManagerContext.get().find(
                    org.ebayopensource.turmeric.policyservice.model.Subject.class, 
                    ipKey.getSubjectId());
            assertNotNull(savedSubject);
        } finally {
            EntityManagerContext.close();
        }
    }

    @Test
    public void deleteSubjectTest() throws Exception
    {
        IPSubject ip = new IPSubject();
        Map<Long, Subject> result = ip.getSubjectByName("workstation");
        Long subjectId = result.keySet().toArray(new Long[1])[0];
        ip.deleteSubject(subjectId);

        EntityManagerContext.open(factory);
        try {
            org.ebayopensource.turmeric.policyservice.model.Subject savedSubject =
                EntityManagerContext.get().find(
                    org.ebayopensource.turmeric.policyservice.model.Subject.class, 
                    subjectId);
            assertNull(savedSubject);
        } finally {
            EntityManagerContext.close();
        }
    }
    
    @Test
    public void createSubjectGroupTest() throws Exception
    {
        SubjectKey userKey = getUserKey("jdoe");
        IPSubject ip = new IPSubject();
        
        SubjectGroup subjectGroup = new SubjectGroup();
        subjectGroup.setSubjectGroupName("network");
        subjectGroup.setSubjectType("IP");
        subjectGroup.setApplyToAll(Boolean.valueOf(true));
        subjectGroup.setApplyToEach(Boolean.valueOf(true));
 
        SubjectGroupEditObject sgEditObj = new SubjectGroupEditObject();
        List<Long> addList = sgEditObj.getAddSubjectList();
        addList.add(ip.getSubjectByName("workstation").keySet().toArray(new Long[1])[0]);
        addList.add(ip.getSubjectByName("gateway").keySet().toArray(new Long[1])[0]);
    
        SubjectGroupKey groupKey = ip.createSubjectGroup(subjectGroup, sgEditObj, userKey);
        
        EntityManagerContext.open(factory);
        try {
            org.ebayopensource.turmeric.policyservice.model.SubjectGroup savedSubjectGroup =
                EntityManagerContext.get().find(
                    org.ebayopensource.turmeric.policyservice.model.SubjectGroup.class, 
                    groupKey.getSubjectGroupId());
            assertNotNull(savedSubjectGroup);
        } finally {
            EntityManagerContext.close();
        }
    }
    
    @Test
    public void getSubjectGroupTest() throws Exception {
        createSubjectGroupTest();
        
        IPSubject ip = new IPSubject();
        
        String groupName = "network";
        Map<Long, SubjectGroup> resultByName = ip.getSubjectGroupInfoByName(groupName);
        
        assertNotNull(resultByName);
        assertEquals(groupName, resultByName.values().toArray(new SubjectGroup[1])[0].getSubjectGroupName());
        
        Long groupId = resultByName.keySet().toArray(new Long[1])[0];
        Map<Long, SubjectGroup> resultById = ip.getSubjectGroupInfoById(groupId);
        
        assertNotNull(resultById);
        assertEquals(groupName, resultById.values().toArray(new SubjectGroup[1])[0].getSubjectGroupName());
        assertEquals(groupId, resultById.keySet().toArray(new Long[1])[0]);
        
        Map<Long, SubjectGroup> resByType = ip.getSubjectGroupInfoByType();
        
        assertNotNull(resByType);
        assertEquals(1, resByType.size());

        Map<Long, Subject> resultSubject = ip.getSubjectByName("workstation");
        assertNotNull(resultSubject);
        
        Long subjectId = resultSubject.keySet().toArray(new Long[1])[0];
        Map<Long, SubjectGroup> resBySubject = ip.findSubjectGroupInfoBySubject(subjectId);
        
        assertNotNull(resultById);
        assertEquals(groupName, resBySubject.values().toArray(new SubjectGroup[1])[0].getSubjectGroupName());
        assertEquals(groupId, resBySubject.keySet().toArray(new Long[1])[0]);
    }
    
    @Test
    public void updateSubjectGroupTest() throws Exception
    {
        createSubjectGroupTest();
        
        String groupName = "network";
        SubjectKey userKey = getUserKey("jdoe");
        IPSubject ip = new IPSubject();
        
        Map<Long, SubjectGroup> result = ip.getSubjectGroupInfoByName(groupName);
      
        assertNotNull(result);
        SubjectGroup subjectGroup = result.values().toArray(new SubjectGroup[1])[0];
        Long groupId = result.keySet().toArray(new Long[1])[0];
        
        SubjectGroupEditObject sgEditObj = new SubjectGroupEditObject();
        List<Long> addList = sgEditObj.getAddSubjectList();
        addList.add(ip.getSubjectByName("server").keySet().toArray(new Long[1])[0]);
        List<Long> remList = sgEditObj.getRemoveSubjectList();
        remList.add(ip.getSubjectByName("workstation").keySet().toArray(new Long[1])[0]);

        SubjectGroupKey groupKey = ip.updateSubjectGroup(subjectGroup, sgEditObj, userKey);
        EntityManagerContext.open(factory);
        try {
            Query query = EntityManagerContext.get().createQuery("select sg.subjects from " +
                "org.ebayopensource.turmeric.policyservice.model.SubjectGroup as sg " + 
                "where sg.id = " + groupId);
            List<org.ebayopensource.turmeric.policyservice.model.Subject> subjectList =
                query.getResultList();
            assertEquals(2, subjectList.size());
            assertEquals("gateway", subjectList.get(0).getSubjectName());
            assertEquals("server", subjectList.get(1).getSubjectName());
        } finally {
            EntityManagerContext.close();
        }
    }
    
    @Test
    public void getSubjectAssignmentOfSubjectGroupTest() throws Exception
    {
        createSubjectGroupTest();
        
        String groupName = "network";
        IPSubject ip = new IPSubject();

        Map<Long, SubjectGroup> result = ip.getSubjectGroupInfoByName(groupName);

        assertNotNull(result);
        Long groupId = result.keySet().toArray(new Long[1])[0];

        Map<Long, Subject> resultSubject = ip.getSubjectAssignmentOfSubjectGroup(groupId);
        
        assertNotNull(resultSubject);
        Subject[] subjectList = resultSubject.values().toArray(new Subject[0]);
        
        assertEquals(2, subjectList.length);
        assertEquals("workstation", subjectList[0].getSubjectName());
        assertEquals("gateway", subjectList[1].getSubjectName());
    }

    @Test
    public void deleteSubjectGroupTest() throws Exception {
        createSubjectGroupTest();
        
        IPSubject ip = new IPSubject();
        Map<Long, SubjectGroup> result = ip.getSubjectGroupInfoByName("network");
        Long groupId = result.keySet().toArray(new Long[1])[0];
        assertNotNull(groupId);
        
        ip.deleteSubjectGroup(groupId);

        EntityManagerContext.open(factory);
        try {
            org.ebayopensource.turmeric.policyservice.model.SubjectGroup savedSubjectGroup =
                EntityManagerContext.get().find(
                    org.ebayopensource.turmeric.policyservice.model.SubjectGroup.class, 
                    groupId);
            assertNull(savedSubjectGroup);
        } finally {
            EntityManagerContext.close();
        }
    }
        
    @Test
    public void getSubjectTypeInfoTest() throws Exception {
        IPSubject subjType = new IPSubject();
        
        SubjectTypeInfo typeInfo = subjType.getSubjectTypeInfo();
        assertEquals("IP", typeInfo.getName());
        assertEquals(false, typeInfo.isExternal());
    }
    
    private Subject newSubject(String name, String type, String desc, String ipMask, String contact) {
        Subject subject = new Subject();
        subject.setSubjectName(name);
        subject.setSubjectType(type);
        subject.setDescription(desc);
        subject.setIpMask(ipMask);
        subject.setEmailContact(contact);
        return subject;
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
