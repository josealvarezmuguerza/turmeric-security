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

import java.lang.reflect.Proxy;

import org.ebayopensource.turmeric.policyservice.model.AbstractJPATest;
import org.ebayopensource.turmeric.policyservice.model.Subject;
import org.ebayopensource.turmeric.policyservice.model.SubjectType;
import org.ebayopensource.turmeric.utils.jpa.AbstractDAO;
import org.ebayopensource.turmeric.utils.jpa.JPAAroundAdvice;
import org.junit.Before;

public abstract class SubjectTestBase extends AbstractJPATest{
    private TestDAO testDAO;
    private static final String[][] typeData = {
        {"APP", "Application"},
        {"DEV", "Developer"},
        {"IP", "IP address"},
        {"USER", "User"},
    };
    
    private static final String[][] subjectData = {
        {"jdoe","USER", "John Doe", "", "jdoe@ebayopensource.org"},       
        {"workstation", "IP", "workstation", "172.16.1.100", "support@ebayopensource.org" },
        {"server", "IP", "server", "172.16.1.101", "support@ebayopensource.org" },
        {"gateway", "IP", "gateway", "172.16.1.1", "support@ebayopensource.org" },
        {"subnet", "IP", "subnet", "172.16.1.100/25", "support@ebayopensource.org" },
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
        
        for (String[] subject : subjectData) {
            testDAO.persistEntity(new Subject(subject[0], subject[1], subject[2], subject[3], 0, subject[4]));
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
