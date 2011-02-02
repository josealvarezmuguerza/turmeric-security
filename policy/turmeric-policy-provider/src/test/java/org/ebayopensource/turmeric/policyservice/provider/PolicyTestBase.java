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
import java.util.HashSet;
import java.util.Set;

import org.ebayopensource.turmeric.policyservice.model.AbstractJPATest;
import org.ebayopensource.turmeric.policyservice.model.Operation;
import org.ebayopensource.turmeric.policyservice.model.Policy;
import org.ebayopensource.turmeric.policyservice.model.Resource;
import org.ebayopensource.turmeric.policyservice.model.Subject;
import org.ebayopensource.turmeric.policyservice.model.SubjectGroup;
import org.ebayopensource.turmeric.utils.jpa.AbstractDAO;
import org.ebayopensource.turmeric.utils.jpa.JPAAroundAdvice;
import org.junit.Before;

public abstract class PolicyTestBase extends AbstractJPATest{
    private TestDAO testDAO;
    
    private static final String[][] subjectData = {
        {"jdoe", "USER", "John Doe", "", "jdoe@ebayopensource.org"},  
        {"admin", "USER", "System Admin", "", "admin@ebayopensource.org"},  
        {"manager", "USER", "System Manager", "", "manager@ebayopensource.org"},  
        {"guest", "USER", "Guest", "", "guest@ebayopensource.org"},  
    };
    
    private static final String[][] subjectGroupData = {
        {"managers", "USER", "", "Managers Group"},  
        {"helpdesk", "USER", "", "Helpdesk Group"},  
        {"guests", "USER", "", "Guests Group"},  
    };
    
    private static final String[][] resourceData = {
        { "GENERIC", "adminsvc", "admin service" },
        { "GENERIC", "crmsvc", "crm service" },
    };

    private static final String[][] operationData = {
        { "LOGIN", "login operation" },
        { "START", "start service" },
        { "STOP", "stop service" },
    };

    private static final String[][] policyData = {
        {"globalwl", "WHITELIST", "Global Whitelist Polic0y"},
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
        for (String[] subjectField : subjectData) {
            testDAO.persistEntity(new Subject(subjectField[0], subjectField[1], subjectField[2], subjectField[3], 0, subjectField[4]));
        }

        for (String[] subjectGroupField : subjectGroupData) {
            testDAO.persistEntity(new SubjectGroup(subjectGroupField[0], subjectGroupField[1], subjectGroupField[2], true, true, subjectGroupField[3]));
        }
        
        for (String[] operationField : operationData) {
            testDAO.persistEntity(new Operation(operationField[0], operationField[1]));
        }

        for (String[] resourceField : resourceData) {
            Resource res = new Resource(resourceField[0], resourceField[1], resourceField[2]);
            Set<Operation> ops = new HashSet<Operation>();
            for (String[] opField : operationData) {
                ops.add(new Operation(opField[0], opField[1]));
            }
            res.addOperations(ops);
            testDAO.persistEntity(res);
        }

        for (String[] policyField : policyData) {
            testDAO.persistEntity(new Policy(policyField[0], policyField[1], policyField[2]));
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
