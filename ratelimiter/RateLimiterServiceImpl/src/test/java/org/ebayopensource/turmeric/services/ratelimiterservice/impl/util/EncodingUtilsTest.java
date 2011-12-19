/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.services.ratelimiterservice.impl.util;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;

import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.common.v1.types.ErrorSeverity;
import org.ebayopensource.turmeric.services.ratelimiterservice.impl.util.EncodingUtils;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;


/**
 * Unit test for simple EncodingUtils.
 * @author jamuguerza
 */

public class EncodingUtilsTest {

	@Test
	public void testEncodeErrorMessageContentNull() {
		
		String encodeErrorMessage = EncodingUtils.encodeErrorMessage(AckValue.SUCCESS, null);
		assertNotNull("encodeErrorMessage returns null", encodeErrorMessage);
	
		assertTrue("encodeErrorMessage should start with 'ack='", encodeErrorMessage.startsWith("ack=") );
		assertFalse("encodeErrorMessage should contains Severity'", encodeErrorMessage.contains("severity") );
		assertFalse("encodeErrorMessage should contains errorId'", encodeErrorMessage.contains("errorId") );
		assertFalse("encodeErrorMessage should contains exceptionId'", encodeErrorMessage.contains("exceptionId") );
		assertFalse("encodeErrorMessage should contains domain'", encodeErrorMessage.contains("domain") );
		assertFalse("encodeErrorMessage should contains subdomain'", encodeErrorMessage.contains("subdomain") );
		assertFalse("encodeErrorMessage should contains message'", encodeErrorMessage.contains("message") );
	}
	
	@Test
	public void testEncodeErrorMessageContent() {
		ErrorMessage em =  new ErrorMessage();
		CommonErrorData error =  new CommonErrorData();
		error.setErrorId(500);
		error.setSeverity(ErrorSeverity.ERROR);
		error.setExceptionId("exception_id");
		error.setDomain("turmeric");
		error.setSubdomain("rl");
		error.setMessage("testError");
		
		
		em.getError().add(error);
		
		String encodeErrorMessage = EncodingUtils.encodeErrorMessage(AckValue.SUCCESS, em);
		assertNotNull("encodeErrorMessage returns null", encodeErrorMessage);
	
		assertTrue("encodeErrorMessage should start with 'ack='", encodeErrorMessage.startsWith("ack=") );
		assertTrue("encodeErrorMessage should contains Severity'", encodeErrorMessage.contains("severity") );
		assertTrue("encodeErrorMessage should contains errorId'", encodeErrorMessage.contains("errorId") );
		assertTrue("encodeErrorMessage should contains exceptionId'", encodeErrorMessage.contains("exceptionId") );
		assertTrue("encodeErrorMessage should contains domain'", encodeErrorMessage.contains("domain") );
		assertTrue("encodeErrorMessage should contains subdomain'", encodeErrorMessage.contains("subdomain") );
		assertTrue("encodeErrorMessage should contains message'", encodeErrorMessage.contains("message") );
	
		assertTrue("severity value does not match",  encodeErrorMessage.contains(ErrorSeverity.ERROR.value()));
		assertTrue("errorId value does not match",  encodeErrorMessage.contains("500"));
		assertTrue("exceptionId value does not match", encodeErrorMessage.contains("exception_id"));
		assertTrue("domain value does not match", encodeErrorMessage.contains("turmeric"));
		assertTrue("subdomain value does not match", encodeErrorMessage.contains("rl"));
		assertTrue("message value does not match", encodeErrorMessage.contains("testError"));
	
	}
	
}
