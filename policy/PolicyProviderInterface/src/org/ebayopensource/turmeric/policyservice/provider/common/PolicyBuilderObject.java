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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Rule;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;

/**
 * Collects all the information about a policy. It is more convenient to pass a
 * collection of the policy information to a method using an instance of this
 * class than passing them separately.
 * 
 */
public class PolicyBuilderObject {
	
	/**
	 * Map to store the inclusive subjects. Key is the subject unique ID.
	 */
	private Map<Long, Subject> m_inclusionSubjects = new HashMap<Long, Subject>();
	/**
	 * Map to store the exclusive subjects. Key is the subject unique ID.
	 */
	private Map<Long, Subject> m_exclusionSubjects = new HashMap<Long, Subject>();
	/**
	 * List of global subject names.
	 */
	private List<String> m_globalSubjects = new ArrayList<String>();
	/**
	 * Map to store the inclusive subject groups. Key is the subject group unique ID.
	 */
	private Map<Long, SubjectGroup> m_inclusionSubjectGrps = new HashMap<Long, SubjectGroup>();
	/**
	 * Map to store the exclusive subject groups. Key is the subject group unique ID.
	 */
	private Map<Long, SubjectGroup> m_exclusionSubjectGrps = new HashMap<Long, SubjectGroup>();
	/**
	 * Map to store the resources. Key is the resource unique ID.
	 */
	private Map<Long, Resource> m_resources = new HashMap<Long, Resource>();
	/**
	 * Map to store the rules. Key is the rule unique ID.
	 */
	private Map<Long, Rule> m_rules = new HashMap<Long, Rule>();

	/**
	 * Get the inclusion subjects.
	 * 
	 * @return map of subjects, subject IDs as the keys
	 */
	public Map<Long, Subject> getInclusionSubjects() {
		return m_inclusionSubjects;
	}

	/**
	 * Set the inclusion subjects.
	 * 
	 * @param inclusionSubjects
	 *            map of subjects, subject IDs as the keys
	 */
	public void setInclusionSubjects(Map<Long, Subject> inclusionSubjects) {
		if (inclusionSubjects == null)
			inclusionSubjects = new HashMap<Long, Subject>();
		m_inclusionSubjects = new HashMap<Long, Subject>(inclusionSubjects);
	}

	/**
	 * Get the exclusion subjects.
	 * 
	 * @return map of subjects, subject IDs as the keys
	 */
	public Map<Long, Subject> getExclusionSubjects() {
		return m_exclusionSubjects;
	}

	/**
	 * Set the exclusion subjects.
	 * 
	 * @param exclusionSubjects
	 *            map of subjects, subject IDs as the keys
	 */
	public void setExclusionSubjects(Map<Long, Subject> exclusionSubjects) {
		if (exclusionSubjects == null)
			exclusionSubjects = new HashMap<Long, Subject>();
		m_exclusionSubjects = new HashMap<Long, Subject>(exclusionSubjects);
	}

	/**
	 * Get the global subjects.
	 * 
	 * @return list of subject names
	 */
	public List<String> getGlobalSubjects() {
		return m_globalSubjects;
	}

	/**
	 * Set the global subjects.
	 * 
	 * @param globalSubjects
	 *            list of subject names.
	 */
	public void setGlobalSubjects(List<String> globalSubjects) {
		if (globalSubjects == null)
			globalSubjects = new ArrayList<String>();
		m_globalSubjects = new ArrayList<String>(globalSubjects);
	}

	/**
	 * Get the inclusion subject groups.
	 * 
	 * @return map of subjects, subject IDs as the keys
	 */
	public Map<Long, SubjectGroup> getInclusionSubjectGrps() {
		return m_inclusionSubjectGrps;
	}

	/**
	 * Set the inclusion subject groups.
	 * 
	 * @param inclusionSubjectGrps
	 *            map of subject groups, subject group IDs as the keys
	 */
	public void setInclusionSubjectGrps(
			Map<Long, SubjectGroup> inclusionSubjectGrps) {
		if (inclusionSubjectGrps == null)
			inclusionSubjectGrps = new HashMap<Long, SubjectGroup>();
		m_inclusionSubjectGrps = new HashMap<Long, SubjectGroup>(
				inclusionSubjectGrps);
	}

	/**
	 * Get the exclusion subject groups.
	 * 
	 * @return map of subject groups, subject group IDs as the keys
	 */
	public Map<Long, SubjectGroup> getExclusionSubjectGrps() {
		return m_exclusionSubjectGrps;
	}

	/**
	 * Set the exclusion subject groups.
	 * 
	 * @param exclusionSubjectGrps
	 *            map of subject groups, subject group IDs as the keys
	 */
	public void setExclusionSubjectGrps(
			Map<Long, SubjectGroup> exclusionSubjectGrps) {
		if (exclusionSubjectGrps == null)
			exclusionSubjectGrps = new HashMap<Long, SubjectGroup>();
		m_exclusionSubjectGrps = new HashMap<Long, SubjectGroup>(
				exclusionSubjectGrps);
	}

	/**
	 * Get all resources.
	 * 
	 * @return map of resources, resource IDs as the keys
	 */
	public Map<Long, Resource> getResources() {
		return m_resources;
	}

	/**
	 * Set all resources.
	 * 
	 * @param resources
	 *            map of resources, resource IDs as the keys
	 */
	public void setResources(Map<Long, Resource> resources) {
		if (resources == null)
			resources = new HashMap<Long, Resource>();
		m_resources = new HashMap<Long, Resource>(resources);
	}

	/**
	 * Get rules.
	 * 
	 * @return map of rules, rule IDs as the keys.
	 */
	public Map<Long, Rule> getRules() {
		return m_rules;
	}

	/**
	 * Set rules.
	 * 
	 * @param rules
	 *            map of rules, rule IDs as the keys.
	 */
	public void setRules(Map<Long, Rule> rules) {
		if (rules == null)
			rules = new HashMap<Long, Rule>();
		m_rules = new HashMap<Long, Rule>(rules);
	}

}
