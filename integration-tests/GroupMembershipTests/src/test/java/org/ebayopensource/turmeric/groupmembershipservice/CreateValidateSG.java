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

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ebayopensource.turmeric.security.v1.services.IsMemberOfRequestType;
import org.ebayopensource.turmeric.security.v1.services.IsMemberOfResponseType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.test.services.utils.CommonUtils;
import org.ebayopensource.turmeric.test.services.utils.TestDataReader;

import static org.junit.Assert.assertEquals;

public class CreateValidateSG  extends CommonUtils{

	public void populateSubjectSG(TestDataReader reader, IsMemberOfRequestType m_req, String request_id){
		String subList = reader.getPreEntryValue(request_id,"request_subjectlist");
		String sgList = reader.getPreEntryValue(request_id,"request_sglist");

		SubjectType subject = null;

		if(subList.equals("null")){
			 m_req.setSubject(subject);
		} else if(subList!=null && !subList.equals("")){
			String[] subKeys = subList.split(",");
			for(String value : subKeys){
				String[] subValue = value.split(":");
				if(subValue.length==1)
					subject = createSubjectType("",subValue[0]);
				else
					subject = createSubjectType(subValue[1],subValue[0]);
				m_req.setSubject(subject);
			}
		}

		SubjectGroupType subjectGroup = null;
		if(sgList==null){

		}else if(sgList.equals("null")){
			m_req.getSubjectgroup().add(subjectGroup);
		} else if(sgList!=null && !sgList.equals("")){
			String[] sgKeys = sgList.split(",");
			for(String check : sgKeys){
				String[] sgValue = check.split(":");
				if(sgValue.length==2 && sgValue[0]!=null && !sgValue[0].equals("") && sgValue[1]!=null && !sgValue[1].equals(""))
					subjectGroup = createSubjectGroupType(sgValue[0],"",sgValue[1]);
				else if(sgValue[2].equals("null"))
					subjectGroup = createSubjectGroupType(sgValue[0],null,sgValue[1]);
				else
					subjectGroup = createSubjectGroupType(sgValue[0],sgValue[2],sgValue[1]);
				m_req.getSubjectgroup().add(subjectGroup);
			}
		}
	}

	public SubjectType createSubjectType(String domain, String value){
		SubjectType subject = new SubjectType();
		if(domain.equals("null"))
			subject.setDomain(null);
		else
			subject.setDomain(domain);
		if(value.equals("null"))
			subject.setValue(null);
		else
		    subject.setValue(value);
		return subject;
	}

	public SubjectGroupType createSubjectGroupType(String name, String calculated, String domain){
		SubjectGroupType subjectGroup = new SubjectGroupType();
		subjectGroup.setName(name);
		subjectGroup.setCalculator(calculated);
		subjectGroup.setDomain(domain);
		return subjectGroup;
	}

	public void validateOutput(TestDataReader reader,IsMemberOfResponseType m_resp,String request_id){
		String isMember = reader.getPreEntryValue(request_id,"response_isMember");
		String sgName = reader.getPreEntryValue(request_id, "response_sgname");
		String size = reader.getPreEntryValue(request_id, "response_sgsize");

		if(isMember!=null){
			assertEquals("isMember is not set with correct value",Boolean.parseBoolean(isMember),m_resp.isIsMember());
		}
		if(sgName!=null){
			assertEquals("sgName is not correct",sgName,m_resp.getSubjectgroup().get(0).getName());
		}
		if(size!=null){
			assertEquals("size is not correct",Integer.parseInt(size),m_resp.getSubjectgroup().size());
		}
	}

	 public static int totalTestCount(Properties props){
			Pattern pattern = Pattern.compile("testcase(\\d+)"+"."+"number");
			Matcher matcher = null;
			Set<?> s = props.keySet();
			Iterator<?> ite = s.iterator();

			int max = 0;
			int value;
			while(ite.hasNext()){
				matcher = pattern.matcher(ite.next().toString());
				if(matcher.find()){
					value = Integer.parseInt(matcher.group(1));
					if(value>max)
						max = value;
				}
			}
	      return max;
		}
}
