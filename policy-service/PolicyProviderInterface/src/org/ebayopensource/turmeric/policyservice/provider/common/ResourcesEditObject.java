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
 * Stores the resource information for policy editing. It is more convenient to
 * pass a collection of the editing information to a method using an instance of
 * this class than passing them separately.
 * 
 */
public class ResourcesEditObject {
	/**
	 * List of unique IDs of the resource to be added into the system or
	 * assigned to a policy.
	 */
	List<Long> addResourceList;
	/**
	 * List of unique IDs of the resource to be removed from the system or
	 * unassigned from a policy.
	 */
	List<Long> removeResourceList;
	/**
	 * List of unique IDs of the operations to be added into the system or
	 * assigned to a resource or policy.
	 */
	List<Long> addOperationList;
	/**
	 * List of unique IDs of the operations to be removed from the system or
	 * unassigned from a resource or policy.
	 */
	List<Long> removeOperationList;

	/**
	 * Get all the operations to be added. The return list can be modified from
	 * outside of this class since this class provides no editing methods for
	 * the list.
	 * 
	 * @return A list of operation IDs
	 */
	public List<Long> getAddOperationList() {
		if (addOperationList == null)
			addOperationList = new ArrayList<Long>();

		return addOperationList;
	}

	/**
	 * Get all the operations to be removed. The return list can be modified
	 * from outside of this class since this class provides no editing methods
	 * for the list.
	 * 
	 * @return A list of operation IDs
	 */
	public List<Long> getRemoveOperationList() {
		if (removeOperationList == null)
			removeOperationList = new ArrayList<Long>();

		return removeOperationList;
	}

	/**
	 * Get all the resources to be added. The return list can be modified from
	 * outside of this class since this class provides no editing methods for
	 * the list.
	 * 
	 * @return A list of resource IDs
	 */
	public List<Long> getAddResourceList() {
		if (addResourceList == null)
			addResourceList = new ArrayList<Long>();

		return addResourceList;
	}

	/**
	 * Get all the resources to be removed. The return list can be modified from
	 * outside of this class since this class provides no editing methods for
	 * the list.
	 * 
	 * @return A list of resource IDs
	 */
	public List<Long> getRemoveResourceList() {
		if (removeResourceList == null)
			removeResourceList = new ArrayList<Long>();

		return removeResourceList;
	}
}
