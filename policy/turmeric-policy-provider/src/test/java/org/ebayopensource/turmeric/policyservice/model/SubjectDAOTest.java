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

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Proxy;

import org.ebayopensource.turmeric.utils.jpa.EntityManagerContext;
import org.ebayopensource.turmeric.utils.jpa.JPAAroundAdvice;
import org.junit.Before;
import org.junit.Test;

public class SubjectDAOTest extends AbstractJPATest {
    private SubjectDAO subjectDAO;

    @Before
    public void initDAO() {
        ClassLoader classLoader = SubjectDAO.class.getClassLoader();
        Class[] interfaces = {SubjectDAO.class};
        SubjectDAO target = new SubjectDAOImpl();
        subjectDAO = (SubjectDAO) Proxy.newProxyInstance(classLoader, interfaces, new JPAAroundAdvice(factory, target));
    }

    @Test
    public void testPersistSubject() throws Exception {
        Subject subject = new Subject("subject_name", "subject_type", "subject_description", "", 0, "");
        subjectDAO.persistSubject(subject);

        EntityManagerContext.open(factory);
        try {
            Subject savedSubject = EntityManagerContext.get().find(Subject.class, subject.getId());
            assertNotNull(savedSubject);
        } finally {
            EntityManagerContext.close();
        }
    }

    @Test
    public void testFindSubjectById() throws Exception {
        Subject subject = new Subject("subject_name", "subject_type", "subject_description", "", 0, "");
        subjectDAO.persistSubject(subject);

        subject = subjectDAO.findSubjectById(subject.getId());
        assertNotNull(subject);
    }
}
