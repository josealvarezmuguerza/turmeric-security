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

import java.util.List;

import org.ebayopensource.turmeric.groupmembershipservice.exceptions.GroupMembershipException;
import org.ebayopensource.turmeric.security.v1.services.GroupMembersType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKeyType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;

/**
 * GroupMembershipProvider is the interface for all custom group membership
 * providers. This interface need to be implemented in order to provide the
 * detail logic for each different providers.
 * 
 */
public interface GroupMembershipProvider {
	/**
	 * Retrieves the subject group by searching for the containing subject.
	 * 
	 * @param subject
	 *            The subject used for the search.
	 * @return A list of subject groups
	 * @throws GroupMembershipException
	 *             Thrown if there is any error.
	 */
	public List<SubjectGroupType> getSubjectGroupsBySubject(SubjectType subject)
			throws GroupMembershipException;

	/**
	 * Retrieves the subject group by its key.
	 * 
	 * @param subjectGroupKey
	 *            The key used for the search.
	 * @return The search result, with the subject group and all containing
	 *         subjects
	 * @throws GroupMembershipException
	 *             Thrown if there is any error.
	 */
	public GroupMembersType getSubjectGroupByKey(
			SubjectGroupKeyType subjectGroupKey)
			throws GroupMembershipException;

	/**
	 * Retrieves the calculated subject group by given the calculator name
	 * inside the parameter.
	 * 
	 * @param subjectGroup
	 *            Need to set the calculator inside the given subject group
	 * @return The calculated subject group
	 * @throws GroupMembershipException
	 *             Thrown if there is any error.
	 */
	public SubjectGroupType getCalculatedSubjectGroup(
			SubjectGroupType subjectGroup) throws GroupMembershipException;
}
