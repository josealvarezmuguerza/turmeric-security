/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.test.services.authorizationservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ebayopensource.test.v1.services.GetAuthzCacheKeysResponse;
import org.ebayopensource.test.v1.services.ResourceInfoType;
import org.ebayopensource.test.v1.services.SGInfoType;
import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.ErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorDataCollection;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceInvocationRuntimeException;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeResponseType;
import org.ebayopensource.turmeric.security.v1.services.UpdateMode;
import org.ebayopensource.turmeric.services.authorizationservice.impl.util.EncodingUtils;
import org.ebayopensource.turmeric.test.services.utils.PolicyServiceTestHelper;
import org.ebayopensource.turmeric.test.services.utils.TestDataReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AuthorizationServiceCacheTestValidation extends CreateValidateAuthz{


	private static final String SERVICE_RES_TYPE = "SERVICE";
	private AuthorizationServiceTestUtils m_util = null;
    private HashMap<String, Long> m_resMap = null;
	private static final String s_PropFilePath = "AuthorizationServiceCacheTestValidation.properties";
	private static Class className = AuthorizationServiceCacheTestValidation.class;
	private static TestDataReader reader = null;
	private static int max;
    
	@BeforeClass 
	public static void setUpOnce(){
		try {
			reader = new TestDataReader(className);
			max = maxPolicies(reader.getProps());
			createPolicies(reader, max);
		}catch(IOException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@AfterClass 
	public static void cleanUp() {
		cleanUpPolicy(reader, max);
		reader.unloadProperties();
    	reader = null;
		
    }

	@Before
	public void setUp() throws Exception {
		m_util = AuthorizationServiceTestUtils.getInstance(false);
		m_resMap = new HashMap<String, Long>();
	}

	@After
	public void tearDown() throws Exception {
		m_util = null;
		m_resMap = null;
	}

	private void createExpectedResourceSGMap(String validationNum, String request_id){
		String rMap = reader.getPreEntryValue(request_id,"response_resourceMap_validation"+validationNum);

		if(rMap!=null){
			String[] resources = rMap.split(",");
			String[] value = null;
			for(String resDetail : resources){
				value = resDetail.split(":");
				m_resMap.put(encodeSvcOp(value[0],value[1]), Long.parseLong(value[2]));
				System.out.println("resources for "+validationNum+" is resource name="+value[0]+ " opName="+value[1]+" count= "+ value[2]);
			}
		}

	}

	private void serviceToInvoke(String validationNum, String request_id) throws MalformedURLException, ServiceException{
		String resourceList = reader.getPreEntryValue(request_id,"request_createrequest"+validationNum);

		if(resourceList!=null){
			String[] serviceDetails = resourceList.split(",");
			String[] value = null;
			for(String svcInvoke : serviceDetails){
				value = svcInvoke.split(":");
				m_util.invokeAuthzSvc(value[0],value[1],value[2],value[3]);
				System.out.println("Service to Invoke for "+validationNum+" servicename="+value[0]+"aopName="+value[1]+" domain name="+value[2]+ " subjectname= "+value[3]);
			}
		}

	}

	@Test
	public void testValidation_test() throws Exception{
		System.out.println("\n--- testValidation_test ---");
		try{
			String request_id = "testcase1";
			// poke cache to get a clean slate
			final boolean poked = m_util.pokeCache();
			if (m_util.isRemote()) {
				assertTrue("Cache poking returned FALSE!", poked);
			}

			String token = reader.getPreEntryValue(request_id,"request_admintest_token").trim();
            String token_header = reader.getPreEntryValue(request_id, "request_admintest_headervalue").trim();
			String calc_sub = reader.getPreEntryValue(request_id,"request_CalculatorTestService__getSubtraction_subjectlist");
			String admin_sub = reader.getPreEntryValue(request_id,"request_AdminTestItemService_addItem_subjectlist");
			String[] calc_values = null;
			String[] admin_values = null;

			if(calc_sub!=null)
				calc_values = calc_sub.split(":");
			else
				fail("invalid parameters specified in properties");
			if(admin_sub!=null)
				admin_values = admin_sub.split(":");
			else
				fail("invalid parameters specified in properties");

			// invoke AdminTestItemValidation:getAuthzCacheKeys Service
			GetAuthzCacheKeysResponse resKeys = m_util.invokeAdminService_getAuthzCacheKeys(token,token_header);
			displayResourceDetails(resKeys, "invokeAdminService - test2");
			m_resMap.put(encodeSvcOp(AuthorizationServiceTestUtils.SERVICE_NAME,
						AuthorizationServiceTestUtils.OPERATION_NAME),
						0L);
			validateAuthzCache(resKeys);

			// invoke AdminTestItemValidation:getAuthzCacheKeys Service and CalculatorTestService::getSubtraction
			m_util.invokeAuthzSvc("CalculatorTestService", "getSubtraction",  calc_values[0], calc_values[1]);
			resKeys = m_util.invokeAdminService_getAuthzCacheKeys(token,token_header);
			displayResourceDetails(resKeys, "invoke calculator:addition and Admin Service - test2");
			m_resMap.put(encodeStdSvcOp(), 1L);
			m_resMap.put(encodeSvcOp("CalculatorTestService", "getSubtraction"), 0L);
			validateAuthzCache(resKeys);

			// invoke AdminTestItemValidation:addItem Service and CalculatorTestService::getSubtraction
			m_util.invokeAuthzSvc("CalculatorTestService", "getSubtraction", calc_values[0], calc_values[1]);
			m_util.invokeAuthzSvc(AuthorizationServiceTestUtils.SERVICE_NAME, "addItem",  admin_values[0], admin_values[1]);
			resKeys = m_util.invokeAdminService_getAuthzCacheKeys(token,token_header);
			displayResourceDetails(resKeys,"invoke calculator:subtraction and Admin Service - test2");
			m_resMap.put(encodeStdSvcOp(), 2L);
			m_resMap.put(encodeSvcOp("CalculatorTestService", "getSubtraction"), 1L);
			validateAuthzCache(resKeys);
		}catch(Exception e){
			e.printStackTrace();
			fail("Testcase should not fail. "+e.getMessage());
		}
	}

	@Test
	public void testValidation_poke() throws Exception{
		System.out.println("\n--- testValidation_poke ---");

		try{

			String request_id = "testcase2";
			// poke cache to get a clean slate
			final boolean poked = m_util.pokeCache();
			if (m_util.isRemote()) {
				assertTrue("Cache poking returned FALSE!", poked);
			}
			String token = reader.getPreEntryValue(request_id,"request_admintest_token").trim();
			String token_header = reader.getPreEntryValue(request_id, "request_admintest_headervalue").trim();
			String calc_sub = reader.getPreEntryValue(request_id,"request_CalculatorTestService__getSubtraction_subjectlist");
			String admin_sub = reader.getPreEntryValue(request_id,"request_AdminTestItemService_addItem_subjectlist");
			String[] calc_values = null;
			String[] admin_values = null;

			if(calc_sub!=null)
				calc_values = calc_sub.split(":");
			else
				fail("invalid parameters specified in properties");
			if(admin_sub!=null)
				admin_values = admin_sub.split(":");
			else
				fail("invalid parameters specified in properties");

			// invoke AdminTestItemValidation:addItem Service and invoke CalculatorTestService:getSubtraction twice
			m_util.invokeAuthzSvc("CalculatorTestService", "getSubtraction", calc_values[0], calc_values[1]); // build
			m_util.invokeAuthzSvc("CalculatorTestService", "getSubtraction",  calc_values[0], calc_values[1]); // hit
			m_util.invokeAuthzSvc(AuthorizationServiceTestUtils.SERVICE_NAME, "addItem", admin_values[0], admin_values[1]); //build
			// invoke AdminTestItemValidation:getAuthzCacheKeys Service
		
			GetAuthzCacheKeysResponse resKeys = m_util.invokeAdminService_getAuthzCacheKeys(token,token_header); // miss/build
			displayResourceDetails(resKeys,
					"invoke calculator:subtraction and AdminTestItemValidation::addItem and "
					+ "AdminTestItemValidation::getAuthzCacheKeys - testValidation_poke");
			m_resMap.put(encodeStdSvcOp(), 0L); // getAuthzCacheKeys was a miss - lazy per-operation loading, remember?
			m_resMap.put(encodeSvcOp("CalculatorTestService", "getSubtraction"), 1L); // hit, we invoked it twice
			// For addItem, the user adminwltest1 was authorized by
			// policy AdminTestItemValidation_Authz directly, before SG expansion.
			// So we only count the expansion for the getAuthzCacheKeys call.
			validateAuthzCache(resKeys);

			assertTrue("Cache poking returned FALSE!", m_util.pokeCache());

			// invoke AdminTestItemValidation:getAuthzCacheKeys Service
			resKeys = m_util.invokeAdminService_getAuthzCacheKeys(token,token_header);
			displayResourceDetails(resKeys, "invoke Admin Service after poke - testValidation_poke");
			m_resMap.put(encodeStdSvcOp(), 0L); // cache was rebuilt for this key for the get()
			m_resMap.put(encodeSvcOp("CalculatorTestService", "getSubtraction"), null); // no more auto-rebuilding on poke
			validateAuthzCache(resKeys);

			validatePokerFlag(false);
		}catch(Exception e){
			e.printStackTrace();
			fail("Testcase should not fail");
		}
		
	}

	@Test
	public void testValidation_sg() throws Exception{
		System.out.println("\n--- testValidation_sg ---");

		try{
			String request_id = "testcase3";
			// poke cache to get a clean slate
			final boolean poked = m_util.pokeCache();
			if (m_util.isRemote()) {
				assertTrue("Cache poking returned FALSE!", poked);
			}

			// invoke AdminTestItemValidation:getAuthzCacheKeys Service
			String token = reader.getPreEntryValue(request_id,"request_admintest_token").trim();
			String token_header = reader.getPreEntryValue(request_id, "request_admintest_headervalue").trim();
			String service2_sub = reader.getPreEntryValue(request_id,"request_Service2__getHeaders_subjectlist");
			String admin_sub = reader.getPreEntryValue(request_id,"request_AdminTestItemService_addItem_subjectlist");
			String itemManage_sub = reader.getPreEntryValue(request_id,"request_SecurityItemManage__securityPlaceOffer_subjectlist");
			String[] service2_values = null;
			String[] admin_values = null;
			String[] itemManage_values = null;

			if(service2_sub!=null)
				service2_values = service2_sub.split(":");
			else
				fail("invalid parameters specified in properties");
			if(admin_sub!=null)
				admin_values = admin_sub.split(":");
			else
				fail("invalid parameters specified in properties");
			if(itemManage_sub!=null)
				itemManage_values = itemManage_sub.split(":");
			else
				fail("invalid parameters specified in properties");

			// invoke AdminTestItemValidation:addItem Service
			m_util.invokeAuthzSvc(AuthorizationServiceTestUtils.SERVICE_NAME, "addItem", admin_values[0], admin_values[1]);
			/*
			 *  Policies for Service2.getHeaders:
			 *  - Role2(DEV:Security_DevIDGroup:__, DEV:DevIDGroup:__)
			 *  - CalcSG1(USER:CalcSG1:__)
			 */
			m_util.invokeAuthzSvc("Service2", "getHeaders", service2_values[0], service2_values[1]);
			/*
			 * Policies(SubjectGroups...) for SecurityItemManager.securityPlaceOffer:
			 * - Security_BuyerRole(DEV:Security_DevIDGroup:__)
			 * - Security_BuyerPlaceOfferRole(USER:Security_UserBuyerGroup)
			 */
			m_util.invokeAuthzSvc("SecurityItemManage", "securityPlaceOffer", itemManage_values[0], itemManage_values[1]);
			m_util.invokeAuthzSvc("SecurityItemManage", "securityPlaceOffer", itemManage_values[0], itemManage_values[1]);
			GetAuthzCacheKeysResponse resKeys = m_util.invokeAdminService_getAuthzCacheKeys(token,token_header);
			displayResourceDetails(resKeys,"invoke Admin Service sg - test2");
			m_resMap.put(encodeStdSvcOp(), 0L); // the actual getAuthzCacheKeys invocation builds it,
			m_resMap.put(encodeSvcOp(AuthorizationServiceTestUtils.SERVICE_NAME, "addItem"), 0L);
			m_resMap.put(encodeSvcOp("SecurityItemManage", "securityPlaceOffer"), 1L); // called twice
			m_resMap.put(encodeSvcOp("Service2", "getHeaders"), 0L); // called once

			validateAuthzCache(resKeys);
		}catch(Exception e){
			e.printStackTrace();
			fail("Testcase should not fail");
		}
	}

	@Test
	public void testValidation_sgPoke() throws Exception{
		System.out.println("\n--- testValidation_sgPoke ---");

		try{
			String request_id = "testcase4";
			// poke cache to get a clean slate
			final boolean poked = m_util.pokeCache();
			if (m_util.isRemote()) {
				assertTrue("Cache poking returned FALSE!", poked);
			}

			// invoke AdminTestItemValidation:getAuthzCacheKeys Service
			String token = reader.getPreEntryValue(request_id,"request_admintest_token").trim();
			String token_header = reader.getPreEntryValue(request_id, "request_admintest_headervalue").trim();
			String admin_sub = reader.getPreEntryValue(request_id,"request_AdminTestItemService_addItem_subjectlist");
			String itemManage_sub = reader.getPreEntryValue(request_id,"request_SecurityItemManage__securityPlaceOffer_subjectlist");
			String[] admin_values = null;
			String[] itemManage_values = null;

			if(admin_sub!=null)
				admin_values = admin_sub.split(":");
			else
				fail("invalid parameters specified in properties");
			if(itemManage_sub!=null)
				itemManage_values = itemManage_sub.split(":");
			else
				fail("invalid parameters specified in properties");


			GetAuthzCacheKeysResponse resKeys = null;

			// invoke AdminTestItemValidation:addItem Service
			// SG- 	Security_DevIDGroup consists "DocDevName" user.
			m_util.invokeAuthzSvc(AuthorizationServiceTestUtils.SERVICE_NAME, "addItem", admin_values[0], admin_values[1]);
			m_util.invokeAuthzSvc("SecurityItemManage", "securityPlaceOffer", itemManage_values[0], itemManage_values[1]);
			m_util.invokeAuthzSvc("SecurityItemManage", "securityPlaceOffer", itemManage_values[0], itemManage_values[1]);
			resKeys = m_util.invokeAdminService_getAuthzCacheKeys(token,token_header);
			displayResourceDetails(resKeys,"invoke Admin Service - test2");
			m_resMap.put(encodeStdSvcOp(), 0L); // built during the direct invocation
			m_resMap.put(encodeSvcOp("SecurityItemManage", "securityPlaceOffer"), 1L);
			validateAuthzCache(resKeys);

			assertTrue("Cache poking returned FALSE!", m_util.pokeCache());

			m_util.invokeAuthzSvc(AuthorizationServiceTestUtils.SERVICE_NAME, "addItem", admin_values[0], admin_values[1]);
			resKeys = m_util.invokeAdminService_getAuthzCacheKeys(token,token_header);
			displayResourceDetails(resKeys,"invoke Admin Service sg Poke - test2");
			m_resMap.put(encodeStdSvcOp(), 0L); // rebuilt due to the direct invocation
			m_resMap.put(encodeSvcOp(AuthorizationServiceTestUtils.SERVICE_NAME, "addItem"), 0L); // rebuilt
			m_resMap.put(encodeSvcOp("SecurityItemManage", "securityPlaceOffer"), null); // not rebuilt
			validateAuthzCache(resKeys);

			validatePokerFlag(false);
		}catch(Exception e){
			e.printStackTrace();
			fail("Testcase should not fail");
		}
	}

	@Test
	public void testValidation_userNotAuthz() throws Exception{
		System.out.println("\n--- testValidation_userNotAuthz ---");
		try{
			String request_id = "testcase5";

			// poke cache to get a clean slate
			final boolean poked = m_util.pokeCache();
			if (m_util.isRemote()) {
				assertTrue("Cache poking returned FALSE!", poked);
			}

			String itemManage_sub = reader.getPreEntryValue(request_id,"request_SecurityItemManage_securityPlaceOffer_subjectlist");
			String[] itemManage_values = null;

			if(itemManage_sub!=null)
				itemManage_values = itemManage_sub.split(":");
			else
				fail("invalid parameters specified in properties");
			// invoke AdminTestItemValidation:addItem Service
			// 	SG- 	Security_DevIDGroup consists "DocDevName" user.
			authorizeServiceRemote_AuthFail("SecurityItemManage", "securityPlaceOffer",itemManage_values[0], itemManage_values[1]);
		}catch(Exception e){
			e.printStackTrace();
			fail("Testcase should not fail");
		}
	}

	@Test
	public void testValidation_userNotAuthz_validateCache() throws Exception{
		System.out.println("\n--- testValidation_userNotAuthz_validateCache ---");

		try{
			String request_id = "testcase6";
			// poke cache to get a clean slate
			final boolean poked = m_util.pokeCache();
			if (m_util.isRemote()) {
				assertTrue("Cache poking returned FALSE!", poked);
			}

			String token = reader.getPreEntryValue(request_id,"request_admintest_token").trim();
			String token_header = reader.getPreEntryValue(request_id, "request_admintest_headervalue").trim();
			String itemManage_sub = reader.getPreEntryValue(request_id,"request_SecurityItemManage__securityPlaceOffer_subjectlist");
			String[] itemManage_values = null;

			if(itemManage_sub!=null)
				itemManage_values = itemManage_sub.split(":");
			else
				fail("invalid parameters specified in properties");

			m_util.invokeAuthzSvc("SecurityItemManage", "securityPlaceOffer",itemManage_values[0], itemManage_values[1]);
			GetAuthzCacheKeysResponse resKeys = m_util.invokeAdminService_getAuthzCacheKeys(token,token_header);
			displayResourceDetails(resKeys,"testValidation_userNotAuthz_validateCache");
			m_resMap.put(encodeSvcOp("SecurityItemManage", "securityPlaceOffer"), 0L);
			validateAuthzCache(resKeys);
			authorizeServiceRemote_AuthFail("SecurityItemManage", "securityPlaceOffer", itemManage_values[0], "DocDevName_invalid");
			resKeys = m_util.invokeAdminService_getAuthzCacheKeys(token,token_header);
			m_resMap.put(encodeSvcOp("SecurityItemManage", "securityPlaceOffer"), 1L);
			validateAuthzCache(resKeys);
		}catch(Exception e){
			e.printStackTrace();
			fail("Testcase should not fail");
		}

	}

	@Test
	public void testValidation_userNotAuthz_validateCacheHits() throws Exception {
		System.out.println("\n--- testValidation_userNotAuthz_validatecachehits ---");

		try{
			String request_id = "testcase7";
			// poke cache to get a clean slate
			final boolean poked = m_util.pokeCache();
			if (m_util.isRemote()) {
				assertTrue("Cache poking returned FALSE!", poked);
			}

			// Get an invalid token
			String token = reader.getPreEntryValue(request_id,"request_admintest_token").trim();
			String token_header = reader.getPreEntryValue(request_id, "request_admintest_headervalue").trim();

			try {
				m_util.invokeAdminService_getAuthzCacheKeys(token,token_header);
				fail("Should not have succeeded");
			} catch (Exception e) {
				e.printStackTrace();
				assertTrue("Exception " + e + " is not a " + ServiceInvocationRuntimeException.class.getName(),
						e instanceof ServiceInvocationRuntimeException);
				final ErrorMessage errorMessage = ((ServiceInvocationRuntimeException) e).getErrorMessage();
				ErrorData errorData = errorMessage.getError().get(0);
				assertEquals("Unexpected error ID for error: \n"
						+ AuthorizationServiceTestErrorFormatter.errorMessageToString(errorMessage),
				 	 ErrorDataCollection.svc_security_authz_failed.getErrorId(), errorData.getErrorId());
				System.out.println("YAY! Failed as expected");
			}
		}catch(Exception e){
			e.printStackTrace();
			fail("Testcase should not fail");
		}
	}

	@Test
	public void testUpdateInvokedPolicy_noImpact() {
		System.out.println("\n--- updateInvokedPolicy_noImpact ---");
		GetAuthzCacheKeysResponse resKeys = null;

		try{
			// poke cache to get a clean slate
			final boolean poked = m_util.pokeCache();
			if (m_util.isRemote()) {
				assertTrue("Cache poking returned FALSE!", poked);
			}
			
			String[] test1ext_customError_values = null;
			String[] test1ext_myTestOperation_values = null;
			String[] sg_values = null;
			
			String request_id = "testcase8";
			String token = reader.getPreEntryValue(request_id,"request_admintest_token").trim();
			String token_header = reader.getPreEntryValue(request_id, "request_admintest_headervalue").trim();
			String sg_create = reader.getPreEntryValue(request_id, "request_SGInclusion").trim();  
			String sg_update = reader.getPreEntryValue(request_id, "request_removeSubFromSG").trim();
			String test1ext_customError_sub = reader.getPreEntryValue(request_id, "request_test1ext_customError2_subjectlist").trim();
			String test1ext_myTestOperation_sub = reader.getPreEntryValue(request_id, "request_test1ext_myTestOperation_subjectlist").trim();
			
			if(test1ext_customError_sub!=null)
				test1ext_customError_values = test1ext_customError_sub.split(":");
			else
				fail("invalid parameters specified in properties");
			if(test1ext_myTestOperation_sub!=null)
				test1ext_myTestOperation_values = test1ext_myTestOperation_sub.split(":");
			else
				fail("invalid parameters specified in properties");
			if(sg_update != null)
					sg_values = sg_update.split(":");
			else
				fail("invalid parameters specified in properties");

			List<String> resourceMap = new ArrayList<String>();
			resourceMap.add("SERVICE:test1ext:customError2");
			resourceMap.add("SERVICE:test1ext:myTestOperation");
			List<String> subjectGInclusion = new ArrayList<String>();
			subjectGInclusion.add(sg_create);

			PolicyServiceTestHelper helper = PolicyServiceTestHelper.getInstance(false);

			try{
				// Create an Authz policy resource:test1ext operation:- customError2 and myTestOperation , assign SG: Security_UserIDGroup_valid
				helper.createPolicy("AUTHZ", "SuryaTestAuthzPolicy", "Validate authz cache feature", resourceMap, null, null, subjectGInclusion, null, null, 0, 0, null, null, true);
				// Make a call to test1ext:customError2 with "soasec_user1_test" user
				m_util.invokeAuthzSvc("test1ext", "customError2", test1ext_customError_values[0], test1ext_customError_values[1]);
				resKeys = m_util.invokeAdminService_getAuthzCacheKeys(token,token_header);
				m_resMap.put(encodeSvcOp("test1ext", "customError2"), 0L);
				m_resMap.put(encodeSvcOp("test1ext", "myTestOperation"), null);
				validateAuthzCache(resKeys);
				// update SG: Security_UserIDGroup_valid, remove "soasec_user1_test"
				helper.updateSubjectGroup("SURYA_UserIDGroup_CacheTEST", sg_values[0],sg_values[1], UpdateMode.DELETE);
				// Make a call to test1ext:myTestOperation with "soasec_user1_test" user
				List<String> sgNames = m_util.invokeAuthzSvc("test1ext", "myTestOperation", test1ext_myTestOperation_values[0], test1ext_myTestOperation_values[1]);
				resKeys = m_util.invokeAdminService_getAuthzCacheKeys(token,token_header);
				m_resMap.put(encodeSvcOp("test1ext", "customError2"), 0L);
				m_resMap.put(encodeSvcOp("test1ext", "myTestOperation"), 0L);
				validateAuthzCache(resKeys);
			}catch(Exception e){
				e.printStackTrace();
				fail("should not fail");
			}finally{
				helper.cleanupPolicy("SuryaTestAuthzPolicy", "AUTHZ");
			}
		}catch(Exception e){
			e.printStackTrace();
			fail("Testcase should not fail");
		}
	}

	private void validatePokerFlag(boolean expValue){
		boolean value = m_util.getPoker();
		assertEquals("poker flag is not reset to false", expValue, value);
	}

	private void authorizeServiceRemote_AuthFail(String serviceName, String opName,
			String subjectDomain, String subjectName)
	throws ServiceException, MalformedURLException{
		AuthorizeResponseType result = m_util.authorize(serviceName, opName, subjectDomain, subjectName);

		assertEquals("ACK value not as expected", AckValue.FAILURE, result.getAck());
		assertTrue(result.getErrorMessage() != null);
		System.out.println("failed with error message: \n"
				+ AuthorizationServiceTestErrorFormatter.errorMessageToString(result.getErrorMessage()));
	}

	private void displayResourceDetails(GetAuthzCacheKeysResponse resKeys, String message) {
		System.out.println(message);
		List<ResourceInfoType> res = resKeys.getResourceInfo();
		Iterator<ResourceInfoType> itr = res.iterator();
		ResourceInfoType resource = null;
		while(itr.hasNext()){
			resource = (ResourceInfoType) itr.next();
			System.out.println("resource = "
					+ EncodingUtils.encodeResOpKey(resource.getResOp().getResourceType(),
							resource.getResOp().getResourceName(), resource.getResOp().getOperationName())
					+ ", hits = " + resource.getHits());
		}
		System.out.println("SG info:-");
		List<SGInfoType> sgtype = resKeys.getSgInfo();
		Iterator<SGInfoType> sgitr = sgtype.iterator();
		SGInfoType sg = null;
		while(sgitr.hasNext()){
			sg = (SGInfoType) sgitr.next();
			System.out.println("SG name=" + sg.getSgName()+" hits= "+sg.getHits());
		}
	}

	private void validateSGStats(List<SGInfoType> resInfoList, Map<String, Long> expStats) {
		Map<String, Long> outStats = new HashMap<String, Long>(resInfoList.size());
		for (SGInfoType sgInfo : resInfoList) {
			outStats.put(sgInfo.getSgName(), sgInfo.getHits());
		}
		validateStats(expStats, outStats);
	}

	private String encodeStdSvcOp() {
		return encodeSvcOp(AuthorizationServiceTestUtils.SERVICE_NAME,
				AuthorizationServiceTestUtils.OPERATION_NAME);
	}

	private String encodeSvcOp(String svcNm, String opNm) {
		return EncodingUtils.encodeResOpKey(SERVICE_RES_TYPE, svcNm, opNm);
	}

	private String encodeSG(String sgNm) {
		return encodeSG(sgNm, "EBAYUSER");
	}

	private String encodeSG(String sgNm, String domain) {
		return EncodingUtils.encodeSubjectGroupKey(domain, sgNm, null);
	}

	private void validateResOpStats(List<ResourceInfoType> resInfoList, Map<String, Long> expStats) {
		Map<String, Long> outStats = new HashMap<String, Long>(resInfoList.size());
		for (ResourceInfoType resInfo : resInfoList) {
			outStats.put(EncodingUtils.encodeResOpKey(resInfo.getResOp().getResourceType(),
					resInfo.getResOp().getResourceName(),
					resInfo.getResOp().getOperationName()), resInfo.getHits());
		}
		validateStats(expStats, outStats);
	}

	private void validateStats(Map<String, Long> expStats,
			Map<String, Long> outStats) {
		for (Map.Entry<String, Long> expEntry : expStats.entrySet()) {
			String expKey = expEntry.getKey();
			if (expEntry.getValue() == null && outStats.containsKey(expKey)) {
				fail("Out stats " + outStats + " should not contain key " + expKey);
			} else if (expEntry.getValue() != null && !outStats.containsKey(expKey)) {
				fail("Out stats " + outStats + " don't even contain key " + expKey);
			}
			assertEquals("Out stats " + outStats + " have wrong value for key " + expKey,
					expEntry.getValue(),
					outStats.get(expKey));
		}
	}

	private void validateAuthzCache(GetAuthzCacheKeysResponse resKeys) {
		validateResOpStats(resKeys.getResourceInfo(), m_resMap);
		m_resMap.clear();
//		validateSGStats(resKeys.getSgInfo(), m_sgMap);
//		m_sgMap.clear();
	}

}