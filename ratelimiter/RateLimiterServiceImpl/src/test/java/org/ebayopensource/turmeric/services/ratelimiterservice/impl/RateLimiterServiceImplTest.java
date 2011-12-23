package org.ebayopensource.turmeric.services.ratelimiterservice.impl;
/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;

import org.junit.Test;


/**
 * Unit test for simple RateLimiterServiceImpl.
 * @author jamuguerza
 */

public class RateLimiterServiceImplTest {

	
	@Test
	public void testGetPreferredProvider() {
		try {
			Method declaredMethod = RateLimiterServiceImpl.class.getDeclaredMethod("getPreferredProvider",null);
			declaredMethod.setAccessible(true);
			String preferredProvider = (String)  declaredMethod.invoke(new RateLimiterServiceImpl(), null);
			
			assertNotNull(preferredProvider);
			assertEquals("rateLimiterProviderImpl", preferredProvider.trim());
			assertEquals("rateLimiterProviderImpl", preferredProvider);

			
		} catch (Exception e) {
			fail("cannot achive method" + e.getMessage());
		}
		
	}
}
