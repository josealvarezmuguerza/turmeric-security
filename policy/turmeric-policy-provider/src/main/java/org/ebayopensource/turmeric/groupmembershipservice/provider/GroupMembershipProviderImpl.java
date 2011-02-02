/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.groupmembershipservice.provider;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.ebayopensource.turmeric.groupmembershipservice.exceptions.GroupMembershipException;
import org.ebayopensource.turmeric.policyservice.model.SubjectDAO;
import org.ebayopensource.turmeric.policyservice.model.SubjectDAOImpl;
import org.ebayopensource.turmeric.security.v1.services.GroupMembersType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKeyType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.utils.config.exceptions.PolicyProviderException;
import org.ebayopensource.turmeric.utils.jpa.JPAAroundAdvice;
import org.ebayopensource.turmeric.utils.jpa.PersistenceContext;


/**
 * @author mgorovoy
 *
 */
public class GroupMembershipProviderImpl implements GroupMembershipProvider {
    private final EntityManagerFactory factory;
    private final GroupMembershipProvider impl;
     
    public GroupMembershipProviderImpl() {
        factory = PersistenceContext.createEntityManagerFactory("policyservice");
        
        ClassLoader classLoader = GroupMembershipProvider.class.getClassLoader();
        Class[] interfaces = {GroupMembershipProvider.class};
        GroupMembershipProvider target = new GroupMembershipProviderBase();
        impl = (GroupMembershipProvider) Proxy.newProxyInstance(classLoader, interfaces, new JPAAroundAdvice(factory, target));
    }
   
    private class GroupMembershipProviderBase implements GroupMembershipProvider
    {
        private final SubjectDAO subjectDAO;
    
        private final CalculatedGroupMembershipProviderImpl calcProvider = 
            CalculatedGroupMembershipProviderImpl.getInstance();
        
        public GroupMembershipProviderBase()
        {
            this.subjectDAO = new SubjectDAOImpl();
        }
        
        @Override
        public List<SubjectGroupType> getSubjectGroupsBySubject(SubjectType subject)
                        throws GroupMembershipException {
            List<SubjectGroupType> result = new ArrayList<SubjectGroupType>();
            List<org.ebayopensource.turmeric.policyservice.model.SubjectGroup> jpaSubjectGroups =
                subjectDAO.findSubjectGroupBySubjectName(subject.getValue(), subject.getDomain());
            for (org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup : jpaSubjectGroups) {
                SubjectGroupType group = new SubjectGroupType();
                group.setName(jpaSubjectGroup.getSubjectGroupName());
                group.setDomain(jpaSubjectGroup.getSubjectType());
                result.add(group);
            }
            
            return result;
        }
    
        @Override
        public GroupMembersType getSubjectGroupByKey(SubjectGroupKeyType subjectGroupKey)
                        throws GroupMembershipException {
            org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup = null;
            
            Long groupId = subjectGroupKey.getId();
            if (groupId != null && groupId > 0) {
                jpaSubjectGroup = subjectDAO.findSubjectGroupById(groupId);
            } else {
                String groupName = subjectGroupKey.getName();
                if (groupName != null && !groupName.isEmpty()) {
                    jpaSubjectGroup = subjectDAO.findSubjectGroupByName(groupName);
                }
            }
            

            GroupMembersType members = new GroupMembersType();
            if (jpaSubjectGroup != null) {
                SubjectGroupType group = new SubjectGroupType();
                group.setName(jpaSubjectGroup.getSubjectGroupName());
                group.setDomain(jpaSubjectGroup.getSubjectType());
                group.setCalculator(jpaSubjectGroup.getSubjectGroupCalculator());
                members.setSubjectGroup(group);
                
                List<SubjectType> subjects = members.getMemberSubjects();
                List<org.ebayopensource.turmeric.policyservice.model.Subject> jpaSubjectList = 
                    jpaSubjectGroup.getSubjects();
                
                if (jpaSubjectList != null && !jpaSubjectList.isEmpty()) {
                    for (org.ebayopensource.turmeric.policyservice.model.Subject jpaSubject : jpaSubjectList) {
                        SubjectType subject = new SubjectType();
                        subject.setValue(jpaSubject.getSubjectName());
                        subject.setDomain(jpaSubject.getSubjectType());
                        
                        subjects.add(subject);
                    }
                }
            }
    
            return members;
        }
    
        @Override
        public SubjectGroupType getCalculatedSubjectGroup(SubjectGroupType subjectGroup)
                        throws GroupMembershipException {
            try {
                calcProvider.initialize();
            } catch (PolicyProviderException e) {
                throw new GroupMembershipException(e.getMessage(), e.getCause());
            }
            return calcProvider.getCalculatedSG(subjectGroup);
        }
    }

    @Override
    public List<SubjectGroupType> getSubjectGroupsBySubject(SubjectType subject)
                    throws GroupMembershipException {
        return impl.getSubjectGroupsBySubject(subject);
    }

    @Override
    public GroupMembersType getSubjectGroupByKey(SubjectGroupKeyType subjectGroupKey)
                    throws GroupMembershipException {
        return impl.getSubjectGroupByKey(subjectGroupKey);
    }

    @Override
    public SubjectGroupType getCalculatedSubjectGroup(SubjectGroupType subjectGroup)
                    throws GroupMembershipException {
        return impl.getCalculatedSubjectGroup(subjectGroup);
    }
}
