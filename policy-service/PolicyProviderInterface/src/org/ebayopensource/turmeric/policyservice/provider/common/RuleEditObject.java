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

import org.ebayopensource.turmeric.security.v1.services.Rule;

/**
 * Stores the rule information for policy editing. It is more convenient to
 * pass a collection of the editing information to a method using an instance of
 * this class than passing them separately.
 * 
 */
public class RuleEditObject {
	/**
	 * List of rules to be added to a policy.
	 */
	private List<Rule> addList;
	/**
	 * List of rules to be removed from a policy.
	 */
	private List<Rule> removeList;

	/**
	 * Get all the rules to be added. The return list can be modified from
	 * outside of this class since this class provides no editing methods for
	 * the list.
	 * 
	 * @return A list of rule objects
	 */
	public List<Rule> getAddList() {
		if (addList == null)
			addList = new ArrayList<Rule>();

		return addList;
	}

	/**
	 * Get all the operations to be removed. The return list can be modified
	 * from outside of this class since this class provides no editing methods
	 * for the list.
	 * 
	 * @return A list of rule objects
	 */
	public List<Rule> getRemoveList() {
		if (removeList == null)
			removeList = new ArrayList<Rule>();

		return removeList;
	}
}
