/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.errorlibrary.turmericratelimiter;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertFalse;

public class ErrorConstantsTest {


	@Test
	public void testPrivateConstructor() {
		Constructor<?>[] constructors = ErrorConstants.class.getDeclaredConstructors();
		assertEquals(1, constructors.length);
		 assertFalse(constructors[0].isAccessible());
	}
	
	@Test
	public void testErrorNames() {
		assertEquals("Error name must be svc_ratelimiter_call_exceeded_limit" , "svc_ratelimiter_call_exceeded_limit", ErrorConstants.SVC_RATELIMITER_CALL_EXCEEDED_LIMIT);
		assertEquals("Error name must be svc_ratelimiter_invalid_provider_configuration" , "svc_ratelimiter_invalid_provider_configuration", ErrorConstants.SVC_RATELIMITER_INVALID_PROVIDER_CONFIGURATION);
		assertEquals("Error name must be  svc_ratelimiter_service_init_failed", "svc_ratelimiter_service_init_failed" , ErrorConstants.SVC_RATELIMITER_SERVICE_INIT_FAILED);
		assertEquals("Error name must be  svc_ratelimiter_system_error", "svc_ratelimiter_system_error" , ErrorConstants.SVC_RATELIMITER_SYSTEM_ERROR);
	}
	
}
