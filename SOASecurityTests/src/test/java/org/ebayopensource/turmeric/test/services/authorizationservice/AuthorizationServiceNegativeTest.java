package org.ebayopensource.turmeric.test.services.authorizationservice;

import static org.ebayopensource.turmeric.services.authorizationservice.impl.biz.AuthorizationServiceImplUtils.NULL_ERR_PARAM_VALUE;
import static org.ebayopensource.turmeric.services.authorizationservice.impl.biz.AuthorizationServiceImplUtils.POLICY_ERR_PARAM_NAME;
import static org.ebayopensource.turmeric.test.services.authorizationservice.AuthorizationServiceTestErrorFormatter.errorMessageToString;
import static org.ebayopensource.turmeric.test.services.authorizationservice.AuthorizationServiceTestErrorFormatter.errorParametersToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.common.v1.types.ErrorParameter;
import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorDataCollection;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeResponseType;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesResponse;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.services.authorizationservice.intf.gen.BaseAuthorizationServiceConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.omg.CORBA.PolicyHelper;

import org.ebayopensource.turmeric.test.services.utils.FindPolicyHelper;
import org.ebayopensource.turmeric.test.services.utils.PolicyDataModelHelper;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.ebayopensource.turmeric.test.services.utils.TestDataReader;

/**
 * This class contains the logic for the main suite of negative
 * AuthorizationService tests. It is further extended to define the type of
 * invocation - remote or local.
 * 
 * @author sukoneru
 * @author mpoplacenel (pulled the logic into the abstract class, enhanced error
 *         reporting, some brush-up)
 */
@RunWith(Parameterized.class)
public class AuthorizationServiceNegativeTest extends CreateValidateAuthz {

	private BaseAuthorizationServiceConsumer m_consumer = null;
	private String policyId;
	private String request_id;
	private String testcaseNumber;
	private String testcaseDesc;
	private static final String s_PropFilePath = "AuthorizationServiceNegativeTest.properties";
	private static Class className = AuthorizationServiceNegativeTest.class;
	private static TestDataReader reader = null;
	private static int max;

	@BeforeClass 
	public static void setUpOnce(){
		
	}

	@AfterClass
	public static void cleanUp() {
		

	}

	@Before
	public void setUp() throws Exception {
		try {
			reader = new TestDataReader(className);
			max = maxPolicies(reader.getProps());
			createPolicies(reader, max);
			m_consumer = isRemote() ? new BaseAuthorizationServiceConsumer(
					"AuthzRemote") : new BaseAuthorizationServiceConsumer();
		} catch (ServiceException e) {
			throw new RuntimeException("Could not initialize consumer", e);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() throws Exception {
		cleanUpPolicy(reader, max);
		reader.unloadProperties();
		reader = null;
		m_consumer = null;
	}

	public AuthorizationServiceNegativeTest(String policyId, String request_id,
			String testcaseNumber, String testcaseDesc) {
		System.err.println("AuthorizationServiceNegativeTest: testcaseNumber ="+testcaseNumber);
		this.policyId = policyId;
		this.request_id = request_id;
		this.testcaseNumber = testcaseNumber;
		this.testcaseDesc = testcaseDesc;
	}

	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data() throws IOException {
		return loadInputData();
	}

	@SuppressWarnings("unchecked")
	public static Collection loadInputData() throws IOException {
		Properties props = new Properties();
		List list = new ArrayList();
		List eachRowData = new ArrayList();
		InputStream inputStream = className.getResourceAsStream(s_PropFilePath);
		props.load(inputStream);
		String debug_tests = props.getProperty("test_to_debug");
		String total_tests = props.getProperty("total_testcases");
		if (!debug_tests.equals("") && !debug_tests.equals("null")) {
			String[] debug_num = debug_tests.split(",");
			for (String s : debug_num) {
				int num = Integer.parseInt(s);
				String policyid = props.getProperty("testcase" + s
						+ ".policyid");
				String testcasenum = props.getProperty(
						"testcase" + s + ".number").trim();
				String testcaseDesc = props.getProperty("testcase" + s
						+ ".description");

				if (policyid != null) {
					eachRowData = new ArrayList();
					eachRowData.add(policyid);
					eachRowData.add("testcase" + testcasenum);
					eachRowData.add(testcasenum);
					eachRowData.add(testcaseDesc);
					list.add(eachRowData.toArray());
				}
			}
		} else {
			// int len = Integer.parseInt(total_tests);
			int len = totalTestCount(props);
			for (int i = 0; i <= len; i++) {
				String policyid = props.getProperty(
						"testcase" + i + ".policyid").trim();
				String testcasenum = props.getProperty(
						"testcase" + i + ".number").trim();
				String testcaseDesc = props.getProperty("testcase" + i
						+ ".description");

				if (policyid != null) {
					eachRowData = new ArrayList();
					eachRowData.add(policyid);
					eachRowData.add("testcase" + testcasenum);
					eachRowData.add(testcasenum);
					eachRowData.add(testcaseDesc);
					list.add(eachRowData.toArray());
				}
			}
		}

		return list;
	}

	@Test
	public void testAuthorizeNegative() throws Exception {
		System.out.println("\n -- Testcase Number = " + testcaseNumber
				+ "\n -- Testcase Description= " + testcaseDesc);
		try {
			String request_id = this.request_id;
			AuthorizeResponseType result = null;
			AuthorizeRequestType req = new AuthorizeRequestType();

			populateAuthzRequest(reader, req, request_id);
			populateSubjectDetails(reader, req, request_id);

			result = m_consumer.authorize(req);
			
			String errorMessage = result.getErrorMessage().getError().get(0).getMessage();
			Long errorId = result.getErrorMessage().getError().get(0).getErrorId();

			System.out.println("Error = " + errorMessage + ", Error Id = " + errorId);
			String error = reader.getPreEntryValue(request_id,
					"response_errormessge");
			String policy = reader.getPreEntryValue(request_id,
					"response_policy");
			validateNegativeOutput(result, error, req, policy);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Testcase should not fail: "+e.getMessage());
		}

	}

	public void validateNegativeOutput(AuthorizeResponseType response,
			String error, AuthorizeRequestType req, String policies) {
		long errorid = 0;
		if (error.equalsIgnoreCase("svc_security_app_authz_unauthorized_user"))
			errorid = ErrorDataCollection.svc_security_app_authz_unauthorized_user
					.getErrorId();
		else if (error
				.equalsIgnoreCase("svc_security_app_authz_internal_error"))
			errorid = ErrorDataCollection.svc_security_app_authz_internal_error
					.getErrorId();
		else if (error
				.equalsIgnoreCase("svc_security_sys_authz_missing_authz_request"))
			errorid = ErrorDataCollection.svc_security_sys_authz_missing_authz_request
					.getErrorId();

		assertFailure(response, errorid, req, policies);
	}

	/**
	 * Is the AuthZ Service consumer remote?
	 * 
	 * @return <code>true</code> (to use a remote consumer). Override to return
	 *         <code>false</code> if you want to run the tests in local mode.
	 */
	protected boolean isRemote() {
		return false;
	}

	/**
	 * Assert the result indicates a failure as indicated.
	 * 
	 * @param result
	 *            the result to assert on.
	 * @param expErrorId
	 *            the expected error ID.
	 * @param policyCSV
	 *            the policy comma-separated values expected to be found as the
	 *            <code>policy</code> error parameter.
	 * @param authzReq
	 *            the original authorization request, to validate the result
	 *            against.
	 */
	private void assertFailure(AuthorizeResponseType result, long expErrorId,
			AuthorizeRequestType authzReq, String policyCSV) {
		assertEquals("Unexpected ACK value", AckValue.FAILURE, result.getAck());
		final ErrorMessage errorMessage = result.getErrorMessage();
		assertNotNull("Null error message", errorMessage);
		final List<CommonErrorData> errorDataList = errorMessage.getError();
		String errMsgStr = errorMessageToString(errorMessage) + "\n";
		assertEquals("Invalid error code in error: " + errMsgStr, expErrorId,
				errorDataList.get(0).getErrorId());
		assertNotNull("ErrorData list should never be null in error: "
				+ errMsgStr, errorDataList);
		assertEquals("Invalid ErrorData list size in error: " + errMsgStr, 1,
				errorDataList.size());
		ErrorData errData = errorDataList.get(0);
		final List<ErrorParameter> errParamList = errData.getParameter();
		assertNotNull("ErrorData param list should never be null in error: "
				+ errMsgStr, errParamList);
		String resOp = authzReq.getResourceType() + "."
				+ authzReq.getResourceName() + "."
				+ authzReq.getOperationName();
		List<SubjectType> subjects = authzReq.getSubject();
		final int baseParamNum = 2;
		assertEquals("Invalid number of error parameters in error: "
				+ errMsgStr, baseParamNum + subjects.size()
				+ (policyCSV != null ? 1 : 0) // policy=<policyCSV> param
				+ (policyCSV != null ? 0 : 1), // stackTrace param
				errParamList.size());
		System.out.println("Failed with error message "
				+ errParamList.get(0).getValue());
		int i = 1;
		assertOnErrorParameter(errMsgStr, errParamList, i++, null, resOp);
		for (SubjectType subject : subjects) {
			final String domain = subject.getDomain() == null ? NULL_ERR_PARAM_VALUE
					: subject.getDomain();
			final String subjectName = subject.getValue() == null ? NULL_ERR_PARAM_VALUE
					: subject.getValue();
			assertOnErrorParameter(errMsgStr, errParamList, i++, null, domain
					+ "." + subjectName);
		}
		if (policyCSV != null) { // better error reporting - policies are also
									// included
			assertOnErrorParameter(errMsgStr, errParamList, i++, null,
					POLICY_ERR_PARAM_NAME + "=" + policyCSV);
		}
	}

	/**
	 * Assert the error parameter at given index has the specified name and
	 * value.
	 * 
	 * @param errMsgStr
	 *            TODO
	 * @param errParamList
	 *            error parameters list
	 * @param idx
	 *            parameter index
	 * @param expName
	 *            the expected parameter name
	 * @param expValue
	 *            the expected parameter value
	 */
	private void assertOnErrorParameter(String errMsgStr,
			List<ErrorParameter> errParamList, int idx, String expName,
			String expValue) {
		expName = expName == null ? "Param" + (idx + 1) : expName;
		ErrorParameter errParam = errParamList.get(idx);
		assertEquals("Unexpected name for error parameter #" + idx
				+ " in list:\n " + errorParametersToString(errParamList)
				+ ", \nerror: " + errMsgStr + "\n", expName, errParam.getName());
		assertEquals(
				"Unexpected value for error parameter #"
						+ idx
						+ " in list:\n "
						+ AuthorizationServiceTestErrorFormatter.errorParametersToString(errParamList)
						+ ", \nerror: " + errMsgStr + "\n", expValue,
				errParam.getValue());
	}

}