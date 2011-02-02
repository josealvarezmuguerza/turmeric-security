/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.test.services.policyenforcementhandler;

import javax.persistence.EntityManagerFactory;

import org.ebayopensource.turmeric.utils.jpa.PersistenceContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class AbstractJPATest {
    protected static EntityManagerFactory factory;
    
    @BeforeClass
    public static void initEntityManagerFactory() {
        factory = PersistenceContext.createEntityManagerFactory("policyservice");
    }

    @AfterClass
    public static void destroyEntityManagerFactory() {
        PersistenceContext.close();
    }
}
