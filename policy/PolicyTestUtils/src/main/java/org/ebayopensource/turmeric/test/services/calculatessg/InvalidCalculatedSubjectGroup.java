/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.test.services.calculatessg;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


import org.ebayopensource.turmeric.groupmembershipservice.exceptions.GroupMembershipException;
import org.ebayopensource.turmeric.groupmembershipservice.provider.BaseCalculatedSubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;

public class InvalidCalculatedSubjectGroup {

	public boolean contains(SubjectType subject) throws GroupMembershipException {
		Properties	props =  new Properties();	
		InputStream is = this.getClass().getResourceAsStream("TestSubjects.properties");
		try {
			props.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String list = props.getProperty("subjects_list");
		String[] subs = list.split(",");
		for(String value: subs){
			String[] s = value.split(":");
			if(subject.getValue().equals(s[0]) && subject.getDomain().equals(s[1]))
				return true;
		}
		return false;
	}

}
