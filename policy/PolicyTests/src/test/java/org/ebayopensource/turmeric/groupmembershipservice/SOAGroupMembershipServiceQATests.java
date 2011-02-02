/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.groupmembershipservice;


import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.ebayopensource.turmeric.security.v1.services.IsMemberOfRequestType;
import org.ebayopensource.turmeric.security.v1.services.IsMemberOfResponseType;
import org.ebayopensource.turmeric.services.groupmembershipservice.intf.gen.BaseGroupMembershipServiceConsumer;
import org.ebayopensource.turmeric.test.services.utils.TestDataReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class SOAGroupMembershipServiceQATests extends CreateValidateSG {

	private BaseGroupMembershipServiceConsumer m_consumer = null;
	private IsMemberOfRequestType m_req = null;
	private IsMemberOfResponseType m_resp = null;
	private String testcaseNumber;
	private String testcaseDesc;
	private String policyId;
	private String request_id;

	private static final String s_PropFilePath = "SOAGroupMembershipServiceQATests.properties";
	private static Class className = SOAGroupMembershipServiceQATests.class;
	private static TestDataReader reader = null;
	private static int max;

	@BeforeClass
	public static void setUpOnce(){
		try {
			reader = new TestDataReader(className);
			max = maxSG(reader.getProps());
			createSG(reader, max);
		}catch(IOException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void cleanUp() {
		cleanUpSG(reader, max);
		reader.unloadProperties();
    	reader = null;

    }

	@Before
	public void setUp() throws Exception {
		m_consumer = new BaseGroupMembershipServiceConsumer();
		m_req = new IsMemberOfRequestType();
	}

	@After
	public void tearDown() throws Exception {
		m_consumer = null;
		m_req = null;
		m_resp = null;
	}

	public SOAGroupMembershipServiceQATests(String policyId, String request_id, String testcaseNumber, String testcaseDesc) {
		this.policyId = policyId;
		this.request_id = request_id;
		this.testcaseNumber = testcaseNumber;
		this.testcaseDesc = testcaseDesc;
	}

	@SuppressWarnings("unchecked")
	@Parameters
	public static Collection data() throws IOException{
		return loadInputData();
	}

	@SuppressWarnings("unchecked")
	public static Collection loadInputData() throws IOException {
		Properties props = new Properties();
		List  list = new ArrayList();
		List eachRowData = new ArrayList();
		InputStream inputStream = className.getResourceAsStream(s_PropFilePath);
			props.load(inputStream);
			String debug_tests = props.getProperty("test_to_debug");
			String total_tests = props.getProperty("total_testcases");
			if(!debug_tests.equals("") && !debug_tests.equals("null")){
				String[] debug_num = debug_tests.split(",");
				for(String s:debug_num){
					int num = Integer.parseInt(s);
					String policyid = props.getProperty("testcase"+s+".policyid");
					String testcasenum = props.getProperty("testcase"+s+".number");
					String testcaseDesc = props.getProperty("testcase"+s+".description");
					String requestid = "testcase"+testcasenum;

					if(policyid!=null){
						eachRowData = new ArrayList();
						eachRowData.add(policyid);
						eachRowData.add(requestid.trim());
						eachRowData.add(testcasenum);
						eachRowData.add(testcaseDesc);
						list.add(eachRowData.toArray());
					}
				}
			}else{
				//int len = Integer.parseInt(total_tests);
				int len = totalTestCount(props);
				for (int i = 0; i <= len; i++) {
					String policyid = props.getProperty("testcase"+i+".policyid");
					String testcasenum = props.getProperty("testcase"+i+".number");
					String testcaseDesc = props.getProperty("testcase"+i+".description");
					String requestid = "testcase"+testcasenum;

					if(policyid!=null){
						eachRowData = new ArrayList();
						eachRowData.add(policyid);
						eachRowData.add(requestid.trim());
						eachRowData.add(testcasenum);
						eachRowData.add(testcaseDesc);
						list.add(eachRowData.toArray());
					}
				}
			}

		return list;
	}

	@Test
	public void testIsMemberOf() throws Exception {
		System.out.println("\n -- Testcase Number = "+testcaseNumber+"\n -- Testcase Description= "+testcaseDesc );
		try{
			String request_id = this.request_id;
			populateSubjectSG(reader, m_req,request_id);
			m_resp = m_consumer.isMemberOf(m_req);
			System.out.println("is member: " + m_resp.isIsMember());
			validateOutput(reader, m_resp,request_id);
		}catch(Exception e){
			e.printStackTrace();
			fail("Testcase should not fail");
		}
	}
}
