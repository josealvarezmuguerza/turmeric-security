/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.test.services.extended.util;

import org.ebayopensource.turmeric.runtime.sif.pipeline.ClientMessageContext;
import org.ebayopensource.turmeric.runtime.spf.pipeline.ServerMessageContext;

/**
 * @author sukoneru
 */
public class ExtendedTestUtils {

	public static final String CALCULATOR_TEST_SERVICE_NAME = "CalculatorTestService";
	
	public static ServerMessageContext createServerMessageContextForCalculatorTestService(String bindingName, String operationName) throws Exception {
		return TestUtils.createServerMessageContext(bindingName, CALCULATOR_TEST_SERVICE_NAME, operationName, null, null);
	}
	
	public static ServerMessageContext createServerMessageContextForCalculatorTestServiceObjectAuthz(String bindingName, String operationName, String payload) throws Exception {
		return TestUtils.createServerMessageContextWithPayload(bindingName, CALCULATOR_TEST_SERVICE_NAME, operationName, null, null,payload);
	}

	// This routes to the specified clientname(clientname/clientconfig.xml). The one above routes always through "default/clientconfig.xml"
	public static ClientMessageContext createClientMessageContextForCalculatorTestService(String bindingName, String clientname) throws Exception {
		return TestUtils.createClientMessageContext(bindingName, CALCULATOR_TEST_SERVICE_NAME, null, null, null, null, null, false, 0, clientname);
	}
	
}

