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
 * Stores the operation information for resource editing. It is more convenient
 * to pass a collection of the editing information to a method using an instance
 * of this class than passing them separately.
 * 
 */
public class OperationEditObject {
	/**
	 * The unique ID of a resource.
	 */
	Long resourceId;
	/**
	 * The operations to be added into the system or assigned to a resource or policy.
	 */
	List<String> addOperationList;
	/**
	 * The operations to be removed from the system or unassigned from a resource or policy.
	 */
	List<String> removeOperationList;

	/**
	 * Set the resource Id of the containing operations.
	 * 
	 * @param input
	 *            resource Id
	 */
	public void setResourceId(Long input) {
		resourceId = input;
	}

	/**
	 * Get the resource Id of the containing operations.
	 * 
	 * @return resource Id
	 */
	public Long getResourceId() {
		return resourceId;
	}

	/**
	 * Get the operations to be added.
	 * 
	 * @return list of operation names
	 */
	public List<String> getAddList() {
		if (addOperationList == null)
			addOperationList = new ArrayList<String>();

		return addOperationList;
	}

	/**
	 * Get the operations to be removed.
	 * 
	 * @return list of operation names
	 */
	public List<String> getRemoveList() {
		if (removeOperationList == null)
			removeOperationList = new ArrayList<String>();

		return removeOperationList;
	}
}
