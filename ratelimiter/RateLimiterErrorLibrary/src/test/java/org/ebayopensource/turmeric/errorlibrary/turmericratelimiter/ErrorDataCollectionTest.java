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


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ErrorDataCollectionTest {

	@Test
	public void testAvailableErrorValues() {
		assertNotNull("no defined value for svc_ratelimiter_call_exceeded_limit" , ErrorDataCollection.svc_ratelimiter_call_exceeded_limit);
		assertNotNull("no defined value for svc_ratelimiter_invalid_provider_configuration" , ErrorDataCollection.svc_ratelimiter_invalid_provider_configuration);
		assertNotNull("no defined value for svc_ratelimiter_service_init_failed" , ErrorDataCollection.svc_ratelimiter_service_init_failed);
		assertNotNull("no defined value for svc_ratelimiter_system_error" , ErrorDataCollection.svc_ratelimiter_system_error);
		
	}
	
	@Test
	public void testErrorNames() {
		assertEquals("Error name must be svc_ratelimiter_call_exceeded_limit" , "svc_ratelimiter_call_exceeded_limit", ErrorDataCollection.svc_ratelimiter_call_exceeded_limit.getErrorName());
		assertEquals("Error name must be svc_ratelimiter_invalid_provider_configuration" , "svc_ratelimiter_invalid_provider_configuration", ErrorDataCollection.svc_ratelimiter_invalid_provider_configuration.getErrorName());
		assertEquals("Error name must be  svc_ratelimiter_service_init_failed", "svc_ratelimiter_service_init_failed" , ErrorDataCollection.svc_ratelimiter_service_init_failed.getErrorName());
		assertEquals("Error name must be  svc_ratelimiter_system_error", "svc_ratelimiter_system_error" , ErrorDataCollection.svc_ratelimiter_system_error.getErrorName());
	}
	
	@Test
	public void testErrorId() {
		assertEquals("Error id must be  40001", 40001L , ErrorDataCollection.svc_ratelimiter_system_error.getErrorId());
		assertEquals("Error idmust be 40002" , 40002L, ErrorDataCollection.svc_ratelimiter_call_exceeded_limit.getErrorId());
		assertEquals("Error id must be  40003", 40003L , ErrorDataCollection.svc_ratelimiter_service_init_failed.getErrorId());
		assertEquals("Error id must be 40004" , 40004L, ErrorDataCollection.svc_ratelimiter_invalid_provider_configuration.getErrorId());

	}
	
}
