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

import javax.persistence.EntityManagerFactory;

import org.ebayopensource.turmeric.utils.jpa.PersistenceContext;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractJPATest {
    protected EntityManagerFactory factory;
    
    @Before
    public void initEntityManagerFactory() {
        factory = PersistenceContext.createEntityManagerFactory("policyservice");
    }

    @After
    public void destroyEntityManagerFactory() {
        PersistenceContext.close();
    }
}
