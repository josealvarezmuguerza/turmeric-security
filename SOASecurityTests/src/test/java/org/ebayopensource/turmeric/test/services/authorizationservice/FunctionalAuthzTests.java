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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.ebayopensource.test.v1.services.GetItemRequest;
import org.ebayopensource.test.v1.services.GetItemResponse;
import org.ebayopensource.turmeric.runtime.sif.service.Service;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceInvokerOptions;
import org.ebayopensource.turmeric.test.services.utils.TestDataReader;
import org.ebayopensource.turmeric.test.v1.services.admintestitemvalidation.intf.AsyncAdminTestItemValidation;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FunctionalAuthzTests extends CreateValidateAuthz{

	private static Class className = FunctionalAuthzTests.class;
	private static TestDataReader reader = null;
	private static int max;
	
	@BeforeClass 
	public static void setUpOnce(){
		try {
			reader = new TestDataReader(className);
			max = maxPolicies(reader.getProps());
			createPolicies(reader, max);
		 }catch(Exception e){
				e.printStackTrace();
				fail("Testcase should not fail");
			}
	}
	
	
	@AfterClass 
	public static void cleanUp() {
		try{
			cleanUpPolicy(reader, max);
			reader.unloadProperties();
			reader = null;
		}catch(Exception e){
			fail("Authz policy is not deleted");
		}
		
    }
	
	@Test
	public void testSecurity_AdminTestItemValidationService_Negative() {
		  System.out.println("start test testSecurity_AdminTestItemValidationService_Negative()");
		  try{
			  Service service = ServiceFactory.create("AdminTestItemValidation", "production", "AdminTestItemValidationConsumer", null);

			  ServiceInvokerOptions sio = service.getInvokerOptions();
			  sio.setTransportName("LOCAL");
			  AsyncAdminTestItemValidation m_proxy = service.getProxy();

			  service.getRequestContext().setTransportHeader(
					  reader.getPreEntryValue("testcase1","authn_header"), reader.getPreEntryValue("testcase1","authn_value"));


			  GetItemRequest req = new GetItemRequest();
			  req.setItemId("11111");
			  GetItemResponse response = m_proxy.getItem(req);
			  fail("Should throw an exception as user is not authorized");
		  }catch(Exception e){
			  e.printStackTrace();
			  assertTrue("valid and should fail as user is unauthorized",e.getMessage().contains("Caller is not authorized to access operation") );
		  }

	}
	
	@Test
	public void testSecurity_AdminTestItemValidationService_SG(){
		  System.out.println("start test testSecurity_AdminTestItemValidationService_SG()");
		  try{
			  Service service = ServiceFactory.create("AdminTestItemValidation", "production", "AdminTestItemValidationConsumer", null);

			  ServiceInvokerOptions sio = service.getInvokerOptions();
			  sio.setTransportName("LOCAL");
			  AsyncAdminTestItemValidation m_proxy = service.getProxy();

			  service.getRequestContext().setTransportHeader(
					  reader.getPreEntryValue("testcase2","authn_header"), reader.getPreEntryValue("testcase2","authn_value"));

			  GetItemRequest req = new GetItemRequest();
			  req.setItemId("11111");
			  GetItemResponse response = m_proxy.getItem(req);
			  assertTrue("Value as expected", response.getItemName().contains("IPOD"));
		  }catch(Exception e){
			  e.printStackTrace();
			  fail("Should not throw exception."+e.getMessage());
		  }

	}
	
}
