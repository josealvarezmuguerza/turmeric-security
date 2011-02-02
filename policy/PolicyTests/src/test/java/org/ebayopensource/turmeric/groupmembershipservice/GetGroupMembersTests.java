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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ebayopensource.turmeric.security.v1.services.GetGroupMembersRequestType;
import org.ebayopensource.turmeric.security.v1.services.GetGroupMembersResponseType;
import org.ebayopensource.turmeric.security.v1.services.GroupMembersType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKeyType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.services.groupmembershipservice.intf.gen.BaseGroupMembershipServiceConsumer;
import org.ebayopensource.turmeric.test.services.utils.SubjectGroupModelHelper;
import org.ebayopensource.turmeric.test.services.utils.TestDataReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;



public class GetGroupMembersTests  extends CreateValidateSG{

	private BaseGroupMembershipServiceConsumer m_consumer = null;
	private GetGroupMembersRequestType m_request = null;
	private GetGroupMembersResponseType m_response = null;

	private static final String s_PropFilePath = "GetGroupMembersTests.properties";
	private static Class className = GetGroupMembersTests.class;
	private static TestDataReader reader = null;

	@BeforeClass
	public static void setUpOnce() throws IOException{
		reader = new TestDataReader(className);
	}

	@AfterClass
	public static void cleanUp() {
    	reader = null;

    }

	@Before
	public void setUp() throws Exception {
		m_consumer = new BaseGroupMembershipServiceConsumer();
		m_request = new GetGroupMembersRequestType();
	}

	@After
	public void tearDown() throws Exception {
		m_consumer = null;
		m_request = null;
		m_response = null;
	}

	private void populateSGKey(GetGroupMembersRequestType m_request, String request_id){
		String sgs = reader.getEntryValue(request_id,"request_sglist");
		String[] sgList = sgs.split(",");
	    for(String s: sgList){
	    	String[] value = s.split(":");
	    	SubjectGroupKeyType subjectGroupKey = null;
	    	if(value[0].equals("null"))
	    		subjectGroupKey = createSubjectGroupKeyType(null,Long.parseLong(value[1]));
	    	else if(value[0]!= null)
	    		subjectGroupKey = createSubjectGroupKeyType(value[0],Long.parseLong(value[1]));
	    	m_request.getSubjectgroupKey().add(subjectGroupKey);
	    }
	}

	private SubjectGroupKeyType createSubjectGroupKeyType(String name, Long id){
		SubjectGroupKeyType subjectGroupKey = new SubjectGroupKeyType();
		if(name != null)
			subjectGroupKey.setName(name);
		if(id != null)
			subjectGroupKey.setId(id);
		return subjectGroupKey;
	}

	private void validateResponse(GetGroupMembersResponseType m_response, String request_id){
		List<GroupMembersType> members = null;

		String	totalSg = reader.getEntryValue(request_id,"output_totalSG");
		if(totalSg!=null){
			members = m_response.getSubjectgroupMembers();
			assertTrue(members.size()==Integer.parseInt(totalSg));
		}

		String value = reader.getEntryValue(request_id,"output_sgDetails");
		if(value!=null){
			String[] v = value.split(",");

			for(String sValue : v ){
				String[] sgInfo = sValue.split(":");
				members = m_response.getSubjectgroupMembers();

				for (GroupMembersType groupMember : members) {
					if(groupMember.getSubjectGroup() != null && groupMember.getMemberSubjects() != null
							&& groupMember.getMemberSubjects().size() > 0 && groupMember.getSubjectGroup().getName().equals(sgInfo[0])){

						assertTrue(groupMember.getMemberSubjects().size()==Integer.parseInt(sgInfo[1]));

					    String[] list = sgInfo[2].split("&");
					    int check = 0;
					    for(SubjectType s : groupMember.getMemberSubjects()){
					        check = 0;
					    	for(String name : list){
					    		if(s.getValue().equals(name)){
					    			check = 1;
					    			break;
					    		}

					    	}
					    	if(check==1)
					    		assertTrue(true);
					    	else
					    		assertTrue(false);
			            }
				}

			}
		}
	  }
	}

	@Test
	public void testGetGroupMembers() throws Exception {
		System.out.println("\n--- testGetGroupMembers() ---");
		String sg = "policy1";
		try{
			SubjectGroupModelHelper.createSGDataModel(className,sg);
			String request_id = "test1";
			populateSGKey(m_request,request_id);

			m_response = m_consumer.getGroupMembers(m_request);
			List<GroupMembersType> subjectGroupMembers = m_response.getSubjectgroupMembers();
			if (subjectGroupMembers == null || subjectGroupMembers.isEmpty()) {
			    System.out.println(">>FAILED");
			    assertTrue(false);
			}
;			System.out.println("size="+subjectGroupMembers.get(0).getMemberSubjects().size());
			if (m_response.getSubjectgroupMembers().get(0).getMemberSubjects().size() == Integer.parseInt(reader.getEntryValue(request_id,"output_memberlist"))) {
				System.out.println(">>PASSED");
				assertTrue(true);
			} else if (m_response.getErrorMessage() != null){
				System.out.println(">>FAILED");
				assertTrue(false);
			}else{
				assertTrue(false);
			}
		}catch(Exception ex){
			ex.printStackTrace();
			fail("Testcase should not fail");
		}finally{
			try{
				if(sg!=null){
						SubjectGroupModelHelper.deleteSGDataModel(className, sg);
				}
			}catch(Exception e){
				fail("SubjectGroup is not deleted");
			}
		}
	}

	@Test
	public void testGetGroupMembersWithCalcAndNonCalcSG() throws Exception {
		System.out.println("----testGetGroupMembersWithNonCalcSG()-----");
		String[] sg = {"policy1","policy2"};
		try{
			SubjectGroupModelHelper.createSGDataModel(className,sg);
			String request_id = "test2";
			populateSGKey(m_request,request_id);
			m_response = m_consumer.getGroupMembers(m_request);
			validateResponse(m_response,request_id);
		}catch(Exception e){
			e.printStackTrace();
			fail("Testcase should not fail");
		}finally{
			try{
				if(sg!=null){
					for(String policy : sg)
						SubjectGroupModelHelper.deleteSGDataModel(className, policy);
				}
			}catch(Exception e){
				fail("SubjectGroup is not deleted");
			}
		}
	}


	@Test
	public void testGetGroupMembersWithNonCalcSGID() throws Exception {

		System.out.println("\n--- testGetGroupMembersWithNonCalcSG() ---");
		String[] sg = {"policy1","policy3"};
		String request_id = "test3";
		try{
			HashMap<String, Long> hm = SubjectGroupModelHelper.createSGDataModel(className,sg);
			SubjectGroupKeyType subjectGroupKey = null;
			Set set = (Set) hm.entrySet();
			Iterator i = (Iterator) set.iterator();
			while(i.hasNext()){
				   Map.Entry m = (Map.Entry) i.next();
				   subjectGroupKey = createSubjectGroupKeyType(null, Long.parseLong(m.getValue().toString()));
				   m_request.getSubjectgroupKey().add(subjectGroupKey);
			}
			m_response = m_consumer.getGroupMembers(m_request);
			validateResponse(m_response,request_id);
		}catch(Exception e){
			e.printStackTrace();
			fail("Testcase should not fail");
		}finally{
			try{
				if(sg!=null){
					for(String policy : sg)
						SubjectGroupModelHelper.deleteSGDataModel(className, policy);
				}
			}catch(Exception e){
				fail("SubjectGroup is not deleted");
			}
		}
	}

	@Test
	public void testGetGroupMembersWithCalcSG() throws Exception {
		System.out.println("\n--- testGetGroupMembersWithCalcSG() ---");
		String[] sg = {"policy2","policy4"};
		try{
			HashMap<String, Long> hm = SubjectGroupModelHelper.createSGDataModel(className,sg);
			SubjectGroupKeyType subjectGroupKey = null;
			Set set = (Set) hm.entrySet();
			Iterator i = (Iterator) set.iterator();
			while(i.hasNext()){
				   Map.Entry m = (Map.Entry) i.next();
				   subjectGroupKey = createSubjectGroupKeyType(null, Long.parseLong(m.getValue().toString()));
				   m_request.getSubjectgroupKey().add(subjectGroupKey);
			}
			m_response = m_consumer.getGroupMembers(m_request);
			if (!(m_response.getSubjectgroupMembers().size() == Integer.parseInt(reader.getEntryValue("test4","output_totalSG")))) {
				System.out.println(">>FAILED");
				assertTrue(false);
			} else
				assertTrue(true);
		}catch(Exception e){
			e.printStackTrace();
			fail("Testcase should not fail");
		}finally{
			try{
				if(sg!=null){
					for(String policy : sg)
						SubjectGroupModelHelper.deleteSGDataModel(className, policy);
				}
			}catch(Exception e){
				fail("SubjectGroup is not deleted");
			}
		}
	}

	@Test
	public void testGetGroupMembersInvalidSG() throws Exception {
		System.out.println("\n--- testGetGroupMembersInvalidSG() ---");
		SubjectGroupKeyType subjectGroupKey = createSubjectGroupKeyType("InvalisSgForTest", 0L);
		m_request.getSubjectgroupKey().add(subjectGroupKey);
		m_response = m_consumer.getGroupMembers(m_request);
		if (!(m_response.getSubjectgroupMembers().size() == Integer.parseInt(reader.getEntryValue("test5","output_totalSG")))) {
			System.out.println(">>FAILED");
			assertTrue(false);
		}
		else
			assertTrue(true);
	}



}

