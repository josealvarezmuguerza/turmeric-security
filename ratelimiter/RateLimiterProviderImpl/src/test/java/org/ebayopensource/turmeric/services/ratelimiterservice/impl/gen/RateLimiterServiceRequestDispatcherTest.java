/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.ratelimiterservice.impl.gen;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import static org.junit.Assert.fail;

import static org.mockito.Mockito.mock;

import static org.mockito.Mockito.when;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.runtime.common.pipeline.Message;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedResponse;
import org.ebayopensource.turmeric.services.ratelimiterservice.intf.RateLimiterService;
import org.junit.Before;
import org.junit.Test;
/**
 * The Class RateLimiterServiceRequestDispatcherTest.
 * 
 * @author jamuguerza
 */
public class RateLimiterServiceRequestDispatcherTest {
	private RateLimiterService rateLimiterServiceMock ;
	private IsRateLimitedRequest isRateLimitedRequestMock;
	private IsRateLimitedResponse isRateLimitedResponse;
	private Message message;
	
	MessageContext messageContextMock;
	@Before
	public void setUp() {
		rateLimiterServiceMock = mock(RateLimiterService.class);
		isRateLimitedRequestMock = mock(IsRateLimitedRequest.class);
		messageContextMock = mock(MessageContext.class);
		message = mock(Message.class);
		isRateLimitedResponse =  new IsRateLimitedResponse();
		
		isRateLimitedResponse.setAck(AckValue.SUCCESS);
		
		when(
				messageContextMock
						.getOperationName())
				.thenReturn("isRateLimited");
		

		when(
				messageContextMock
						.getRequestMessage())
				.thenReturn(message);

		when(
				rateLimiterServiceMock
						.isRateLimited(isRateLimitedRequestMock))
				.thenReturn(isRateLimitedResponse);
		
	}
	
	@Test
	public void testDispatch() {
		RateLimiterServiceRequestDispatcher dispatcher = new  RateLimiterServiceRequestDispatcher();
				
		boolean dispatch= false;
		try {
			dispatch = dispatcher.dispatch(messageContextMock, rateLimiterServiceMock);
		} catch (Exception e) {
			fail("Exception Thrown" + e.getMessage());
		}

		assertTrue(dispatch);
	}

	@Test
	public void testDispatchWrongOpName() {
		
		when(
				messageContextMock
						.getOperationName())
				.thenReturn("getRateLimited");
		
		RateLimiterServiceRequestDispatcher dispatcher = new  RateLimiterServiceRequestDispatcher();
				
		boolean dispatch= false;
		try {
			dispatch = dispatcher.dispatch(messageContextMock, rateLimiterServiceMock);
		} catch (Exception e) {
			fail("Exception Thrown" + e.getMessage());
		}

		assertFalse(dispatch);

		
	}

}