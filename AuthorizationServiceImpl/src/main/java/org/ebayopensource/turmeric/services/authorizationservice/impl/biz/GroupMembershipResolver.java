/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl.biz;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.ebayopensource.turmeric.services.authorizationservice.impl.AuthorizationException;


/**
 * This interface enables pluggable group membership expansions.
 * 
 * @author mpoplacenel
 */
public interface GroupMembershipResolver {
	
	/**
	 * Provides the subjectGroup-subject membership map. 
	 * @param subjectGroups the queried subject groups. 
	 * @return the subjectGroup-subject membership map.
	 * @throws AuthorizationException if problems occurred during the membership 
	 * expansion. 
	 */
	Map<CloneableSubjectGroupType, Set<CloneableSubjectType>> getMembers(Collection<CloneableSubjectGroupType> subjectGroups)
	throws AuthorizationException;

	/**
	 * Checks if a subject is a member of any of the given subject groups. 
	 * @param subject the subject to check. 
	 * @param subjectGroups the subject groups to check against. 
	 * @return <code>true</code> if member, <code>false</code> otherwise. 
	 * @throws AuthorizationException if problems occurred during the membership 
	 * expansion. 
	 */
	Set<CloneableSubjectGroupType> isMemberOf(CloneableSubjectType subject, Collection<CloneableSubjectGroupType> subjectGroups) 
	throws AuthorizationException;

}