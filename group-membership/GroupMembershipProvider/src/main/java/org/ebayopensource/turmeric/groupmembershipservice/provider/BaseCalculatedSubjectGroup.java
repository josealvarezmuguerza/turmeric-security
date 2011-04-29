/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.groupmembershipservice.provider;

import org.ebayopensource.turmeric.groupmembershipservice.exceptions.GroupMembershipException;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;

/**
 * Interface for Calculated SubjectGroup class.
 * 
 * @author dmuthiayen
 * 
 */
public interface BaseCalculatedSubjectGroup {

	/**
	 * Checks whether the subject is a member of a calculated subject group.
	 * 
	 * @param subject
	 *            The subject to check.
	 * @return True if the subject belongs to a specific calculated subject
	 *         group and false otherwise.
	 * 
	 * @throws GroupMembershipException
	 *             Throws this exception when error happens
	 */
	public boolean contains(SubjectType subject)
			throws GroupMembershipException;
}
