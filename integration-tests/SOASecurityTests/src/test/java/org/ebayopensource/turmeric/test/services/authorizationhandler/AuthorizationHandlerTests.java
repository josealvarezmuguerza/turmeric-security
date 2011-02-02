/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.test.services.authorizationhandler;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.pipeline.HandlerInitContextImpl;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
import org.ebayopensource.turmeric.runtime.spf.security.ServerSecurityContext;
import org.ebayopensource.turmeric.services.authorizationservice.impl.handler.AuthorizationHandler;
import org.ebayopensource.turmeric.test.services.authorizationservice.AbstractAuthorizationTestClass;
import org.ebayopensource.turmeric.test.services.extended.util.ExtendedTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.ebayopensource.turmeric.test.services.utils.PolicyDataModelHelper;
import org.ebayopensource.turmeric.test.services.utils.SecurityTokenUtility;
import org.ebayopensource.turmeric.test.services.utils.TestDataReader;
import org.ebayopensource.turmeric.test.services.utils.TestTokenRetrivalObject;

public class AuthorizationHandlerTests extends AbstractAuthorizationTestClass {

	private static TestDataReader reader = null;
	private static SecurityTokenUtility util = null;
	private static int maxpolicyId;
	private static Class className = AuthorizationHandlerTests.class;

	@Before
	public void setUp() throws Exception {
		util = TestTokenRetrivalObject.getSecurityTokenRetrival();
		reader = new TestDataReader(className);
		maxpolicyId = totalPolicies(className);

		for (int i = 0; i <= maxpolicyId; i++) {
			PolicyDataModelHelper.createPolicyObject(reader, "policy" + i);
		}
	}

	@After
	public void cleanUp() {
		try {
			for (int i = 0; i <= maxpolicyId; i++) {
				PolicyDataModelHelper.deletePolicyObject(reader, "policy" + i);
			}
			reader.unloadProperties();
			reader = null;
		} catch (Exception e) {
			fail("Authz policy is not deleted");
		}
	}

	@Test
	public void testHandlerPositive_SucessfulAuthorization() throws Exception {
		final String testname = "testHandlerPositive_SucessfulAuthorization";
		System.out.println("*** starting " + testname + " *** ");
		MessageContext ctx = null;
		AuthorizationHandler handler = new AuthorizationHandler();
		ctx = ExtendedTestUtils
				.createServerMessageContextForCalculatorTestService("XML",
						"getSubtraction");
		populateSubjectInContext(ctx, "testcase0");
		Map<String, String> option = new HashMap<String, String>();
		option.put("use-local-invocation", "true");
		HandlerInitContextImpl initCtx = new HandlerInitContextImpl(
				ctx.getServiceId(), "name", option);
		handler.init(initCtx);

		handler.invoke(ctx);
		assertTrue(validateHandlerSuccessAuthz((ServerSecurityContext) ctx
				.getSecurityContext()));
		System.out.println("*** " + testname + " completed successfully ***");
	}

	@Test
	public void testHandlerNegative_FailedAuthorization() throws Exception {
		final String testname = "testHandlerNegative_FailedAuthorization";
		System.out.println("*** starting " + testname + " *** ");
		MessageContext ctx = null;
		AuthorizationHandler handler = new AuthorizationHandler();

		try {
			ctx = ExtendedTestUtils
					.createServerMessageContextForCalculatorTestService("XML",
							"getSubtraction");
			populateSubjectInContext(ctx, "testcase1");
			Map<String, String> option = new HashMap<String, String>();
			option.put("use-local-invocation", "true");
			HandlerInitContextImpl initCtx = new HandlerInitContextImpl(
					ctx.getServiceId(), "name", option);
			handler.init(initCtx);

			handler.invoke(ctx);

			// *** should not get here - expecting exception ***
			System.out.println("*** " + testname + " completed in failure ***");
			assertTrue("Did not throw exception as expected", false);
		} catch (Exception e) {
			System.out.println("EXPECTED EXCEPTION>> " + e.getMessage());
			assertTrue("Authz response validation failed",
					validateHandlerFailedAuthz((ServerSecurityContext) ctx
							.getSecurityContext()));
			System.out.println("*** " + testname
					+ " completed successfully ***");
		}
	}

	private void populateSubjectInContext(MessageContext ctx, String testcaseid)
			throws ServiceException {
		String subjectinfo = reader.getPreEntryValue(testcaseid,
				"request_subjectdetails");
		String domain = null;
		String value = null;
		if (subjectinfo != null) {
			StringTokenizer st = new StringTokenizer(subjectinfo, ":");
			domain = st.nextToken();
			value = st.nextToken();
		} else
			fail("subject list is not provided in the properties file");

		ctx.getSecurityContext().setAuthnSubject(domain, value);
	}

	public boolean validateHandlerSuccessAuthz(
			ServerSecurityContext securityContext) throws Exception {
		if (!securityContext.getAuthzStatus().isDone()) {
			System.out.println("authorization is not done.");
			return false;
		}
		if (!securityContext.getAuthzStatus().isSuccess()) {
			System.out.println("authorization is not successful.");
			return false;
		}
		if (securityContext.getAuthnSubjects() == null
				|| securityContext.getAuthnSubjects().size() == 0) {
			System.out.println("authentication subjects not returned.");
			return false;
		}
		return true;
	}

	public static boolean validateHandlerFailedAuthz(
			ServerSecurityContext securityContext) throws Exception {
		if (!securityContext.getAuthzStatus().isDone()) {
			System.out.println("authorization is not done.");
			return false;
		}
		if (securityContext.getAuthzStatus().isSuccess()) {
			System.out.println("authorization is not a failure.");
			return false;
		}
		if (securityContext.getAuthzStatus().getStatusCode() == null) {
			System.out.println("authorization status code not returned.");
			return false;
		}
		if (securityContext.getAuthzStatus().getStatusReason() == null) {
			System.out.println("authorization status reason not returned.");
			return false;
		}
		return true;
	}

}
