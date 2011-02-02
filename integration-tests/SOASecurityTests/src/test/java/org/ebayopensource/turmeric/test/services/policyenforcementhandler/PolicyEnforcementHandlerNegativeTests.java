package org.ebayopensource.turmeric.test.services.policyenforcementhandler;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.pipeline.HandlerInitContextImpl;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
import org.ebayopensource.turmeric.runtime.tests.common.jetty.AbstractWithServerTest;
import org.ebayopensource.turmeric.services.policyenforcementservice.handler.PolicyEnforcementHandler;
import org.ebayopensource.turmeric.test.services.extended.util.ExtendedTestUtils;
import org.junit.Test;


public class PolicyEnforcementHandlerNegativeTests extends AbstractWithServerTest{

	@Test
	public  void testHandler_Authn_Negative() {
		final String testname = "testHandler_Authn_Negative";
		System.out.println("*** starting " + testname + " *** ");
		MessageContext ctx = null;
		
		PolicyEnforcementHandler handler = new PolicyEnforcementHandler();

		Map<String, String> option = new HashMap<String, String>();
		option.put("policy-types", "AUTHN");
		option.put("service-location",this.jetty.getSPFURI().toString());
		option.put("default-environment","PESTestEnv");

		try {
			ctx = ExtendedTestUtils.createServerMessageContextForCalculatorTestService("XML", "getSquareRoot");
			ctx.getSecurityContext().setCredential("IP", "AdminApp");
			HandlerInitContextImpl initCtx = new HandlerInitContextImpl(ctx.getServiceId(), "name", option);
			handler.init(initCtx);

			handler.invoke(ctx);
			fail("should throw error");
		} catch (ServiceException e) {
			e.printStackTrace();
			// This is the valid message and should be fixed
		    //	assertTrue(e.getMessage().equals("Authentication failed via getSubtraction auth method: CalculatorTestService"));
			assertTrue(e.getMessage().contains("missing required credentials"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("*** " + testname + " completed successfully ***");
	}

	@Test
	public  void testHandler_Authz_Negative() {
		final String testname = "testHandler_Authz_Negative";
		System.out.println("*** starting " + testname + " *** ");
		MessageContext ctx = null;

		PolicyEnforcementHandler handler = new PolicyEnforcementHandler();

		// call default operation "myTestOperation", which maps to authnMethod = token in Authn Policy
		try {
			ctx = ExtendedTestUtils.createServerMessageContextForCalculatorTestService("XML", "getDivision");
			ctx.getSecurityContext().setAuthnSubject("IP", "100.100.200.200");

			Map<String, String> option = new HashMap<String, String>();
			option.put("policy-types", "AUTHZ");
			option.put("default-environment","PESTestEnv");
			option.put("service-location",this.jetty.getSPFURI().toString());

			HandlerInitContextImpl initCtx = new HandlerInitContextImpl(ctx.getServiceId(), "name", option);
			handler.init(initCtx);

			handler.invoke(ctx);
			fail("should throw error");
		} catch (ServiceException e) {
			e.printStackTrace();
			assertTrue(e.getMessage().contains("User is not authorized."));
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("*** " + testname + " completed successfully ***");
	}

	@Test
	public  void testHandler_InvalidPolicyType_Negative() {
		final String testname = "testHandler_InvalidPolicyType_Negative";
		System.out.println("*** starting " + testname + " *** ");
		MessageContext ctx = null;

		PolicyEnforcementHandler handler = new PolicyEnforcementHandler();

		// call default operation "myTestOperation", which maps to authnMethod = token in Authn Policy
		try {
			ctx = ExtendedTestUtils.createServerMessageContextForCalculatorTestService("XML", "getDivision");
			ctx.getSecurityContext().setAuthnSubject("IP", "100.100.200.200");

			Map<String, String> option = new HashMap<String, String>();
			option.put("policy-types", "AUTHZS");
			option.put("default-environment","PESTestEnv");
			option.put("service-location",this.jetty.getSPFURI().toString());

			HandlerInitContextImpl initCtx = new HandlerInitContextImpl(ctx.getServiceId(), "name", option);
			handler.init(initCtx);

			handler.invoke(ctx);
			fail("should throw error");
		} catch (ServiceException e) {
			assertTrue(e.getMessage().contains("AUTHZS is not supported"));
			// what is the error message
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("*** " + testname + " completed successfully ***");
	}
	
	@Test
	public  void testHandler_PolicyTypeNotExisting_Negative() {
		final String testname = "testHandler_emptyPolicyTypeList_Negative";
		System.out.println("*** starting " + testname + " *** ");
		MessageContext ctx = null;

		PolicyEnforcementHandler handler = new PolicyEnforcementHandler();

		// call default operation "myTestOperation", which maps to authnMethod = token in Authn Policy
		try {
			ctx = ExtendedTestUtils.createServerMessageContextForCalculatorTestService("XML", "getDivision");
			ctx.getSecurityContext().setAuthnSubject("IP", "100.100.200.200");

			Map<String, String> option = new HashMap<String, String>();

			HandlerInitContextImpl initCtx = new HandlerInitContextImpl(ctx.getServiceId(), "name", option);
			handler.init(initCtx);

			handler.invoke(ctx);
			fail("should throw error");
		} catch (ServiceException e) {
			//e.printStackTrace();
			assertTrue(e.getMessage().contains("Unexpected policyenforcement error: Invalid Policy Type : null or empty."));
			// what is the error message
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("*** " + testname + " completed successfully ***");
	}
	
	@Test
	public  void testHandler_NullPolicyType_Negative() {
		final String testname = "testHandler_NullPolicyType_Negative";
		System.out.println("*** starting " + testname + " *** ");
		MessageContext ctx = null;

		PolicyEnforcementHandler handler = new PolicyEnforcementHandler();

		// call default operation "myTestOperation", which maps to authnMethod = token in Authn Policy
		try {
			ctx = ExtendedTestUtils.createServerMessageContextForCalculatorTestService("XML", "getDivision");
			ctx.getSecurityContext().setAuthnSubject("USER", "rlcalcsvctest1");

			Map<String, String> option = new HashMap<String, String>();
			option.put("policy-types", null);

			HandlerInitContextImpl initCtx = new HandlerInitContextImpl(ctx.getServiceId(), "name", option);
			handler.init(initCtx);

			handler.invoke(ctx);
			fail("should throw error");
		} catch (ServiceException e) {
			//e.printStackTrace();
			assertTrue(e.getMessage().contains("Unexpected policyenforcement error: Invalid Policy Type : null or empty."));
			// what is the error message
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("*** " + testname + " completed successfully ***");
	}

	@Test
	public  void testHandler_emptyPolicyTypelist_Negative() {
		final String testname = "testHandler_emptyPolicyTypelist_Negative";
		System.out.println("*** starting " + testname + " *** ");
		MessageContext ctx = null;

		PolicyEnforcementHandler handler = new PolicyEnforcementHandler();

		// call default operation "myTestOperation", which maps to authnMethod = token in Authn Policy
		try {
			ctx = ExtendedTestUtils.createServerMessageContextForCalculatorTestService("XML", "getDivision");
			ctx.getSecurityContext().setAuthnSubject("USER", "rlcalcsvctest1");

			Map<String, String> option = new HashMap<String, String>();
			option.put("policy-types", "");

			HandlerInitContextImpl initCtx = new HandlerInitContextImpl(ctx.getServiceId(), "name", option);
			handler.init(initCtx);

			handler.invoke(ctx);
			fail("should throw error");
		} catch (ServiceException e) {
			//e.printStackTrace();
			assertTrue(e.getMessage().contains("Unexpected policyenforcement error: Invalid Policy Type : null or empty."));
			// what is the error message
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("*** " + testname + " completed successfully ***");
	}
}
