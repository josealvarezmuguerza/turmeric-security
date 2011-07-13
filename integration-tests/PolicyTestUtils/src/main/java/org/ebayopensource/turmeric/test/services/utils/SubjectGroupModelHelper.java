/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.test.services.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.ebayopensource.turmeric.security.v1.services.Policy;

public class SubjectGroupModelHelper {
	
	public static HashMap<String, Long> createSGDataModel(Class name, String... sgIds) throws Exception{
		TestDataReader reader = new TestDataReader(name);
		HashMap<String, Long> hm = new HashMap<String, Long>();
		for(String sgId : sgIds){
		   HashMap<String,Long> h = createSGObject(reader,sgId);
		   Set set = (Set) h.entrySet();
		   Iterator i = (Iterator) set.iterator();
		   while(i.hasNext()){
			   Map.Entry m = (Map.Entry) i.next();
			   hm.put(m.getKey().toString(), Long.parseLong(m.getValue().toString()));
		   }
		}
		return hm;
	}
	
	public static void deleteSGDataModel(Class name, String... sgIds) throws Exception{
		TestDataReader reader = new TestDataReader(name);
		for(String sgId : sgIds){
			deletePolicyObject(reader,sgId);
		}
	}
	
	public static void deletePolicyObject(TestDataReader reader, String sgId) throws Exception{
		PolicyServiceTestHelper helper = PolicyServiceTestHelper.getInstance(false);
		String sg = reader.getEntryValue(sgId, "subjectGroupDetails");
		if(sg!= null || !sg.equals("")){
			String[] subGroupKeys = sg.split(",");
			for(int i=0; i < subGroupKeys.length; i++){
				String[] sgDetails = subGroupKeys[i].split(":");
			    helper.cleanupSubjectGroup(sgDetails[0], sgDetails[1]);
			}
		}
	}
	
	public static HashMap<String, Long> createSGObject(TestDataReader reader, String sgId) throws Exception{
        PolicyServiceTestHelper helper = PolicyServiceTestHelper.getInstance(false);
		
		
		List<String> subjectGroupInclusion = new ArrayList<String>();
		String subGroupInclusion = reader.getEntryValue(sgId, "subjectGroupDetails");
		if(subGroupInclusion!=null && !subGroupInclusion.equals("")){
		   String[] subGroupInclusionKeys = subGroupInclusion.split(",");
		   for(int i=0; i < subGroupInclusionKeys.length; i++){
				subjectGroupInclusion.add(subGroupInclusionKeys[i]);
		   }
		}
		
		HashMap<String, Long> sgReturn = new HashMap<String,Long>();
		sgReturn = (HashMap<String, Long>) helper.createSubjectGroup(subjectGroupInclusion);
	    
	    return sgReturn;

	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//		String policy = "p1";
//		try {
//			TestTokenRetrivalObject.setTokenRetrivalObject((SecurityTokenUtility)new TurmericTokenProviderHelper());	
//		    SubjectGroupModelHelper.createSGDataModel(SOAGroupMembershipServiceQATest.class,policy);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}finally{
//			SubjectGroupModelHelper.deleteSGDataModel(SOAGroupMembershipServiceQATest.class, policy);
//		}
	}
}
