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

/**
 * Stores the detail information for policy editing. It is more convenient to
 * pass a collection of the editing information to a method using an instance of
 * this class than passing them separately.
 * 
 */
public class PolicyEditObject {
	/**
	 * Policy unique ID.
	 */
	Long policyId;
	/**
	 * Editing information for a rule.
	 */
	RuleEditObject ruleEditObject;
	/**
	 * Editing information for a resource.
	 */
	ResourcesEditObject resourcesEditObject;
	/**
	 * Editing information for an subject.
	 */
	SubjectsEditObject subjectsEditObject;

	/**
	 * Set the policy Id.
	 * 
	 * @param input
	 *            Policy Id
	 */
	public void setPolicyId(Long input) {
		policyId = input;
	}

	/**
	 * Get the policy Id.
	 * 
	 * @return policy Id
	 */
	public Long getPolicyId() {
		return policyId;
	}

	/**
	 * Set the rule editing object.
	 * 
	 * @param input
	 *            the rule editing object
	 */
	public void setRuleEditObject(RuleEditObject input) {
		ruleEditObject = input;
	}

	/**
	 * Get the rule editing object.
	 * 
	 * @return the rule editing object
	 */
	public RuleEditObject getRuleEditObject() {
		return ruleEditObject;
	}

	/**
	 * Set the resource editing object.
	 * 
	 * @param input
	 *            the resource editing object.
	 */
	public void setResourcesEditObject(ResourcesEditObject input) {
		resourcesEditObject = input;
	}

	/**
	 * Get the resource editing object.
	 * 
	 * @return the resource editing object
	 */
	public ResourcesEditObject getResourcesEditObject() {
		return resourcesEditObject;
	}

	/**
	 * Set the subject editing object.
	 * 
	 * @param input
	 *            the subject editing object
	 */
	public void setSubjectsEditObject(SubjectsEditObject input) {
		subjectsEditObject = input;
	}

	/**
	 * Get the subject editing object.
	 * 
	 * @return get the subject editing object
	 */
	public SubjectsEditObject getSubjectsEditObject() {
		return subjectsEditObject;
	}
}
