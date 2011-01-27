package org.ebayopensource.turmeric.test.services.policyenforcementhandler;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.pipeline.HandlerInitContextImpl;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
import org.ebayopensource.turmeric.runtime.spf.security.ServerSecurityContext;
import org.ebayopensource.turmeric.runtime.tests.common.jetty.AbstractWithServerTest;
import org.ebayopensource.turmeric.services.policyenforcementservice.handler.PolicyEnforcementHandler;
import org.ebayopensource.turmeric.test.services.extended.util.ExtendedTestUtils;
import org.ebayopensource.turmeric.test.services.utils.CommonUtils;
import org.ebayopensource.turmeric.test.services.utils.PolicyDataModelHelper;
import org.ebayopensource.turmeric.test.services.utils.SecurityTokenUtility;
import org.ebayopensource.turmeric.test.services.utils.TestDataReader;
import org.ebayopensource.turmeric.test.services.utils.TestTokenRetrivalObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class PolicyEnforcementHandlerTests extends AbstractWithServerTest {
	private static TestDataReader reader = null;
	private static SecurityTokenUtility util = null;
	private static int maxpolicyId;
	private static Class className = PolicyEnforcementHandlerTests.class;

	@BeforeClass
	public static void setUpOnce() {
		try {
			util = TestTokenRetrivalObject.getSecurityTokenRetrival();
			reader = new TestDataReader(className);
			maxpolicyId = totalPolicies(className);
			for (int i = 0; i <= maxpolicyId; i++) {
				PolicyDataModelHelper.createPolicyObject(reader, "policy" + i);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Testcase should not fail");
		}
	}

	@AfterClass
	public static void cleanUp() {
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
	@Ignore
	public void testHandlerPositive_SucessfulInvocation_Authn_ExtendedInfo()
			throws Exception {
		final String testname = "testHandlerPositive_SucessfulInvocation_Authn_ExtendedInfo";
		System.out.println("*** starting " + testname + " *** ");
		MessageContext ctx = null;

		PolicyEnforcementHandler handler = new PolicyEnforcementHandler();

		// maps to authnMethod = TestOSExtendedInfo in Authn Policy
		ctx = ExtendedTestUtils
				.createServerMessageContextForCalculatorTestService("XML",
						"getSubtraction");

		ctx.getSecurityContext().setCredential("TYPE", "1");

		Map<String, String> option = new HashMap<String, String>();
		option.put("policy-types", "AUTHN");
		option.put("default-environment", "PESTestEnv");
		option.put("service-location", this.jetty.getSPFURI().toString());

		HandlerInitContextImpl initCtx = new HandlerInitContextImpl(
				ctx.getServiceId(), "name", option);
		try {
			handler.init(initCtx);

			handler.invoke(ctx);

			validateExtendedInfoData((ServerSecurityContext) ctx
					.getSecurityContext());
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(e
					.getMessage()
					.contains(
							"Service call has exceeded the number of times the operation is allowed to be called"));
		}

		System.out.println("*** " + testname + " completed successfully ***");
	}

	@Test
	public void testHandlerPositive_SucessfulInvocation_Authz()
			throws Exception {
		final String testname = "testHandlerPositive_SucessfulInvocation_Authz";
		System.out.println("*** starting " + testname + " *** ");
		MessageContext ctx = null;

		PolicyEnforcementHandler handler = new PolicyEnforcementHandler();

		ctx = ExtendedTestUtils
				.createServerMessageContextForCalculatorTestService("XML",
						"getSubtraction");

		populateSubjectInContext(ctx);

		Map<String, String> option = new HashMap<String, String>();
		option.put("policy-types", "AUTHZ");
		option.put("service-location", this.jetty.getSPFURI().toString());
		option.put("default-environment", "PESTestEnv");

		HandlerInitContextImpl initCtx = new HandlerInitContextImpl(
				ctx.getServiceId(), "name", option);

		handler.init(initCtx);

		handler.invoke(ctx);
		validateHandlerSuccessAuthz((ServerSecurityContext) ctx
				.getSecurityContext());

		System.out.println("*** " + testname + " completed successfully ***");
	}

	@Test
	public void testHandlerPositive_SucessfulInvocation_Authn()
			throws Exception {
		final String testname = "testHandlerPositive_SucessfulInvocation_Authn";
		System.out.println("*** starting " + testname + " *** ");
		MessageContext ctx = null;
		ctx = ExtendedTestUtils
				.createServerMessageContextForCalculatorTestService("XML",
						"getSubtraction");

		populateAuthnInfoInContext(ctx);

		PolicyEnforcementHandler handler = new PolicyEnforcementHandler();

		Map<String, String> option = new HashMap<String, String>();
		option.put("policy-types", "AUTHN");
		option.put("default-environment", "PESTestEnv");
		option.put("service-location", this.jetty.getSPFURI().toString());

		HandlerInitContextImpl initCtx = new HandlerInitContextImpl(
				ctx.getServiceId(), "name", option);
		handler.init(initCtx);

		handler.invoke(ctx);
		validateAuthn((ServerSecurityContext) ctx.getSecurityContext());
		System.out.println("*** " + testname + " completed successfully ***");
	}

	@Test
	public void testHandlerPositive_SucessfulInvocation_AuthnAuthz()
			throws Exception {
		final String testname = "testHandlerPositive_SucessfulInvocation_AuthnAuthz";
		System.out.println("*** starting " + testname + " *** ");
		MessageContext ctx = null;
		ctx = ExtendedTestUtils
				.createServerMessageContextForCalculatorTestService("XML",
						"getSubtraction");

		populateAuthnInfoInContext(ctx);

		PolicyEnforcementHandler handler = new PolicyEnforcementHandler();

		Map<String, String> option = new HashMap<String, String>();
		option.put("policy-types", "AUTHN,AUTHZ");
		option.put("service-location", this.jetty.getSPFURI().toString());
		option.put("default-environment", "PESTestEnv");

		HandlerInitContextImpl initCtx = new HandlerInitContextImpl(
				ctx.getServiceId(), "name", option);
		handler.init(initCtx);

		handler.invoke(ctx);
		validateAuthn((ServerSecurityContext) ctx.getSecurityContext());
		validateHandlerSuccessAuthz((ServerSecurityContext) ctx
				.getSecurityContext());

		System.out.println("*** " + testname + " completed successfully ***");
	}

	public void validate(ServerSecurityContext securityContext)
			throws Exception {
		if (securityContext.getAuthnSubjects() == null
				|| securityContext.getAuthnSubjects().size() == 0) {
			System.out.println("authentication subjects not returned.");
			fail("authentication subjects not returned");
		}
		if (!(securityContext.getAuthnCustomData("key1") == null)) {
			System.out.println("authentication method not specified.");
			fail("Extended info is not populated");
		}

	}

	public void validateExtendedInfoData(ServerSecurityContext securityContext)
			throws Exception {
		assertTrue("Valid custom data is populated", securityContext
				.getAuthnCustomData("key1").equals("value1"));
	}

	public void validateAuthn(ServerSecurityContext securityContext)
			throws Exception {
		if (securityContext.getAuthnSubjects() == null
				|| securityContext.getAuthnSubjects().size() == 0) {
			System.out.println("authentication subjects not returned.");
			fail("Did not return the authenticated subjects");
		}

	}

	public void validateHandlerSuccessAuthz(
			ServerSecurityContext securityContext) throws Exception {
		if (!securityContext.getAuthzStatus().isDone()) {
			System.out.println("authorization is not done.");
			fail("authorization is not done.");
		}
		if (!securityContext.getAuthzStatus().isSuccess()) {
			System.out.println("authorization is not successful.");
			fail("authorization is not successful.");
		}
	}

	private void populateAuthnInfoInContext(MessageContext ctx)
			throws Exception {
		String subjectinfo = reader.getEntryValue("request_subjectdetails");
		String domain = null;
		String value = null;
		if (subjectinfo != null) {
			StringTokenizer st = new StringTokenizer(subjectinfo, ":");
			domain = st.nextToken();
			value = st.nextToken();
		} else
			fail("subject list is not provided in the properties file");

		Map<String, String> map = new HashMap<String, String>();
		map.put(domain, value);
		String authmode = reader.getEntryValue("request_authenticationmode");
		HashMap<String, String> mp = (HashMap) util.getSecurityToken(authmode,
				map);
		ctx.getSecurityContext().setCredential(authmode, mp.get(authmode));
	}

	private void populateSubjectInContext(MessageContext ctx)
			throws ServiceException {
		String subjectinfo = reader.getEntryValue("request_subjectdetails");
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

	public static int totalPolicies(Class className) throws IOException {
		Properties props = new Properties();
		InputStream input = className.getResourceAsStream(className
				.getSimpleName() + ".properties");
		props.load(input);

		Pattern pattern = Pattern.compile("policyName" + "_" + "policy(\\d)");
		Matcher matcher = null;
		Set s = props.keySet();
		Iterator ite = s.iterator();

		int max = 0;
		int value;
		while (ite.hasNext()) {
			matcher = pattern.matcher(ite.next().toString());
			if (matcher.find()) {
				value = Integer.parseInt(matcher.group(1));
				if (value > max)
					max = value;
			}
		}
		System.out.println("max value=" + max);
		return max;
	}

}
