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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.services.authorizationservice.impl.util.EncodingUtils;


/**
 * This class represents an authorization policy.
 * 
 * @author mpoplacenel
 */
public class AuthorizationPolicy {
	
	private List<String> m_subjects;

	private List<String> m_subjectGroups;
	
	private String policyName;
	
	/**
	 * 
	 * @return subjects the authorized subjects for the given operation
	 */
	public List<String> getSubjects() {
		if (m_subjects == null)
			m_subjects = new ArrayList<String>();
		return m_subjects;
	}

	/**
	 * @param subjects
	 *            the authorized subjects to set for the given operation
	 */
	public void setSubjects(List<String> subjects) {
		if (m_subjects == null) {
			m_subjects = new ArrayList<String>();
		}
		m_subjects.clear();
		m_subjects.addAll(subjects);
	}

	/**
	 * @return subjectGroups the authorized subject groups for the given
	 *         operation
	 */
	public List<String> getSubjectGroups() {
		if (m_subjectGroups == null)
			m_subjectGroups = new ArrayList<String>();
		return m_subjectGroups;
	}

	/**
	 * @param subjectGroups
	 *            the authorized subject groups to set for the given operation
	 */
	public void setSubjectGroups(List<String> subjectGroups) {
		if (m_subjectGroups == null) {
			m_subjectGroups = new ArrayList<String>();
		}
		m_subjectGroups.clear();
		m_subjectGroups.addAll(subjectGroups);
	}

	/**
	 * Gets the authorized subject with the specified domain.
	 * 
	 * @param domain the domain to get the subject for. 
	 * @return the requested subject. 
	 */
	public String getSubject(String domain) {
		if (m_subjects != null) {
			Iterator<String> i = m_subjects.iterator();
			while (i.hasNext()) {
				String subject = i.next();
				SubjectType subj = EncodingUtils.decodeSubjectKey(subject);
				if (subj.getDomain().equalsIgnoreCase(domain))
					return subject;
			}
		}
		return null;
	}

	/**
	 * Add a subject to the list of authorized subjects.
	 * 
	 * @param subject the subject to add. 
	 */
	public void addSubject(String subject) {
		if (m_subjects == null)
			m_subjects = new ArrayList<String>();

		if (subject == null)
			return;

		m_subjects.add(subject);
	}

	/**
	 * Gets the first authorized subject group with the specified name.
	 * 
	 * @param name the name.
	 * @return the subject group. 
	 */
	public String getSubjectGroup(String name) {
		if (m_subjectGroups != null) {
			Iterator<String> i = m_subjectGroups.iterator();
			while (i.hasNext()) {
				String subjectGroup = i.next();
				if (subjectGroup.equalsIgnoreCase(name))
					return subjectGroup;
			}
		}
		return null;
	}

	/**
	 * Add a subject group to the list of authorized subject groups.
	 * 
	 * @param subjectGroup the subject group to add. 
	 */
	public void addSubjectGroup(String subjectGroup) {
		if (m_subjectGroups == null)
			m_subjectGroups = new ArrayList<String>();

		if (subjectGroup == null)
			return;

		m_subjectGroups.add(subjectGroup);
	}

	/**
	 * Getter for the policy name property. 
	 * @return the policy name. 
	 */
	public String getPolicyName() {
		return policyName;
	}

	/**
	 * Setter for the policy name property. 
	 * @param policyName the policy name.
	 */
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	/**
	 * Provides a more meaningful string representation of the object.
	 * @return a more meaningful string representation of the object, as I said :D.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String superStr = super.toString();
		return superStr.substring(superStr.lastIndexOf('.') + 1) + "("
			+ "polNm=" + getPolicyName()
			+ ", "
			+ "subjs=" + getSubjects()
			+ ", "
			+ "subjGrps=" + getSubjectGroups()
			+ ")";
	}

}