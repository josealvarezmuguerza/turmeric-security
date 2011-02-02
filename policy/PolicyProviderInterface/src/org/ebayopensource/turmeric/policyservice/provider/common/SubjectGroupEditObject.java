/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.provider.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the subject information for subject group editing. It is more
 * convenient to pass a collection of the editing information to a method using
 * an instance of this class than passing them separately.
 * 
 */
public class SubjectGroupEditObject {
	/**
	 * Unique subject group ID.
	 */
	Long subjectGroupId;
	/**
	 * List of IDs of the subjects to be added to the system or assigned to a
	 * subject group or a policy.
	 */
	List<Long> addSubjectList;
	/**
	 * List of IDs of the subjects to be removed from the system or unassigned
	 * from a subject group or a policy.
	 */
	List<Long> removeSubjectList;

	/**
	 * Set the subject group ID for the subjects. All subjects contained in this
	 * class is assumed to have the same subject group ID.
	 * 
	 * @param input
	 *            subject group ID
	 */
	public void setSujectGroupId(Long input) {
		subjectGroupId = input;
	}

	/**
	 * Get the subject group ID for the subjects. All subjects contained in this
	 * class is assumed to have the same subject group ID.
	 * 
	 * @return subject group ID
	 */
	public Long getSubjectGroupId() {
		return subjectGroupId;
	}

	/**
	 * Get all the subjects to be added. The return list can be modified from
	 * outside of this class since this class provides no editing methods for
	 * the list.
	 * 
	 * @return A list of subject IDs
	 */
	public List<Long> getAddSubjectList() {
		if (addSubjectList == null)
			addSubjectList = new ArrayList<Long>();

		return addSubjectList;
	}

	/**
	 * Get all the subjects to be added. The return list can be modified from
	 * outside of this class since this class provides no editing methods for
	 * the list.
	 * 
	 * @return A list of subject IDs
	 */
	public List<Long> getRemoveSubjectList() {
		if (removeSubjectList == null)
			removeSubjectList = new ArrayList<Long>();

		return removeSubjectList;
	}

}
