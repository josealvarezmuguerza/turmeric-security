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
import java.util.List;

import org.ebayopensource.turmeric.policyservice.model.Subject;
import org.ebayopensource.turmeric.policyservice.model.SubjectGroup;
import org.ebayopensource.turmeric.policyservice.model.SubjectType;
import org.ebayopensource.turmeric.utils.jpa.AbstractDAO;
import org.ebayopensource.turmeric.utils.jpa.JPAAroundAdvice;
import org.junit.Before;

public abstract class GroupMembershipTestBase extends AbstractJPATest{
    private TestDAO testDAO;
    private static final String[][] typeData = {
        {"APP", "Application"},
        {"DEV", "Developer"},
        {"IP", "IP address"},
        {"USER", "User"},
    };
    
    private static final String[][] subjectData = {
        {"asmith", "USER", "Ann Smith", "", "jsmith@ebayopensource.org", "0" },
        {"guest", "USER", "Guest User", "", "guest@ebayopensource.org", "0" },
        {"jdoe","USER", "John Doe", "", "jdoe@ebayopensource.org", "0"},       
        {"admin", "USER", "System Admin", "", "admin@ebayopensource.org", "1"},
        {"helpdesk", "USER", "Helpdesk Staff", "", "helpdesk@ebayopensource.org", "1"},
    };
    
    private static final String[][] groupData = {
        {"Everyone", "USER", "All users", "0"},
        {"Admins", "USER", "Administrators", "1"},
    };
    
    @Before
    public void initDAO() {
        ClassLoader classLoader = TestDAO.class.getClassLoader();
        Class[] interfaces = {TestDAO.class};
        TestDAO target = new TestDAOImpl();
        testDAO = (TestDAO) Proxy.newProxyInstance(classLoader, interfaces, new JPAAroundAdvice(factory, target));
        
        initDatabase();
    }
    
    protected void initDatabase() {
        for (String[] type : typeData) {
            testDAO.persistEntity(new SubjectType(type[0], type[1], false));
        }
        
        int idx = -1;
        Subject[] jpaSubjects = new Subject[subjectData.length];
        for (String[] subject : subjectData) {
            jpaSubjects[++idx] = new Subject(subject[0], subject[1], subject[2], subject[3], 0, subject[4]);
            testDAO.persistEntity(jpaSubjects[idx]);
        }
        
        for (String[] group : groupData) {
            SubjectGroup jpaSubjectGroup = new SubjectGroup(group[0], group[1], null, true, true, group[2]);
            List<Subject> subjects = jpaSubjectGroup.getSubjects();
            for (idx = 0; idx < subjectData.length; idx++) {
                if (group[3].equals("0") || group[3].equals(subjectData[idx][5])) {
                    subjects.add(jpaSubjects[idx]);
                }
            }
            testDAO.persistEntity(jpaSubjectGroup);
        }
            
    }

    public static interface TestDAO {
        public void persistEntity(Object entity);
    }
    
    public static class TestDAOImpl extends AbstractDAO implements TestDAO{
        public void persistEntity(Object entity) {
            try {
                super.persistEntity(entity);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }
}
