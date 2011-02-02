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
 * Stores the subject information for policy editing. It is more convenient to
 * pass a collection of the editing information to a method using an instance of
 * this class than passing them separately.
 * 
 */
public class SubjectsEditObject {
	/**
	 * List of IDs of the subject types to be assigned to a policy.
	 */
	List<Long> addSubjectTypeList;
	/**
	 * List of IDs of the subject types to be unassigned from a policy.
	 */
	List<Long> removeSubjectTypeList;
	/**
	 * List of IDs of the inclusive subjects to be assigned to a policy.
	 */
	List<Long> addSubjectList;
	/**
	 * List of IDs of the inclusive subjects to be unassigned to a policy.
	 */
	List<Long> removeSubjectList;
	/**
	 * List of IDs of the exclusive subjects to be assigned to a policy.
	 */
	List<Long> addExclusionSubjectList;
	/**
	 * List of IDs of the exclusive subjects to be unassigned to a policy.
	 */
	List<Long> removeExclusionSubjectList;
	/**
	 * List of IDs of the subject groups to be assigned to a policy.
	 */
	List<Long> addSubjectGroupList;
	/**
	 * List of IDs of the subject groups to be unassigned to a policy.
	 */
	List<Long> removeSubjectGroupList;
	/**
	 * List of IDs of the exclusive subject groups to be assigned to a policy.
	 */
	List<Long> addExclusionSubjectGroupList;
	/**
	 * List of IDs of the exclusive subject groups to be unassigned to a policy.
	 */
	List<Long> removeExclusionSubjectGroupList;

	/**
	 * Get all the subject types to be added. The return list can be modified
	 * from outside of this class since this class provides no editing methods
	 * for the list.
	 * 
	 * @return A list of subject type IDs
	 */
	public List<Long> getAddSubjectTypeList() {
		if (addSubjectTypeList == null)
			addSubjectTypeList = new ArrayList<Long>();

		return addSubjectTypeList;
	}

	/**
	 * Get all the subject types to be removed. The return list can be modified
	 * from outside of this class since this class provides no editing methods
	 * for the list.
	 * 
	 * @return A list of subject type IDs
	 */
	public List<Long> getRemoveSubjectTypeList() {
		if (removeSubjectTypeList == null)
			removeSubjectTypeList = new ArrayList<Long>();

		return removeSubjectTypeList;
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
	 * Get all the subjects to be removed. The return list can be modified from
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

	/**
	 * Get all the exclusion subjects to be added. The return list can be
	 * modified from outside of this class since this class provides no editing
	 * methods for the list.
	 * 
	 * @return A list of subject IDs
	 */
	public List<Long> getAddExclusionSubjectList() {
		if (addExclusionSubjectList == null)
			addExclusionSubjectList = new ArrayList<Long>();

		return addExclusionSubjectList;
	}

	/**
	 * Get all the exclusion subjects to be removed. The return list can be
	 * modified from outside of this class since this class provides no editing
	 * methods for the list.
	 * 
	 * @return A list of subject IDs
	 */
	public List<Long> getRemoveExclusionSubjectList() {
		if (removeExclusionSubjectList == null)
			removeExclusionSubjectList = new ArrayList<Long>();

		return removeExclusionSubjectList;
	}

	/**
	 * Get all the subject groups to be added. The return list can be modified
	 * from outside of this class since this class provides no editing methods
	 * for the list.
	 * 
	 * @return A list of subject group IDs
	 */
	public List<Long> getAddSubjectGroupList() {
		if (addSubjectGroupList == null)
			addSubjectGroupList = new ArrayList<Long>();

		return addSubjectGroupList;
	}

	/**
	 * Get all the subject groups to be removed. The return list can be modified
	 * from outside of this class since this class provides no editing methods
	 * for the list.
	 * 
	 * @return A list of subject group IDs
	 */
	public List<Long> getRemoveSubjectGroupList() {
		if (removeSubjectGroupList == null)
			removeSubjectGroupList = new ArrayList<Long>();

		return removeSubjectGroupList;
	}

	/**
	 * Get all the exclusion subject groups to be added. The return list can be
	 * modified from outside of this class since this class provides no editing
	 * methods for the list.
	 * 
	 * @return A list of subject group IDs
	 */
	public List<Long> getAddExclusionSubjectGroupList() {
		if (addExclusionSubjectGroupList == null)
			addExclusionSubjectGroupList = new ArrayList<Long>();

		return addExclusionSubjectGroupList;
	}

	/**
	 * Get all the exclusion subject groups to be removed. The return list can
	 * be modified from outside of this class since this class provides no
	 * editing methods for the list.
	 * 
	 * @return A list of subject group IDs
	 */
	public List<Long> getRemoveExclusionSubjectGroupList() {
		if (removeExclusionSubjectGroupList == null)
			removeExclusionSubjectGroupList = new ArrayList<Long>();

		return removeExclusionSubjectGroupList;
	}

}
