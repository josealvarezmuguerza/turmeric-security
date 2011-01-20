/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.authentication.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.ebayopensource.turmeric.security.v1.services.KeyValuePairType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;

/**
 * This class represents an authentication response.
 * <p>
 * 
 * @author gyue
 * 
 */
public class AuthenticationResponse {
	/**
	 * List of subjects that have been authenticated.
	 */
	List<SubjectType> m_subjects;

	/**
	 * List of subject groups.
	 */
	List<SubjectGroupType> m_subjectGroups;

	/**
	 * Authentication method.
	 */
	String m_authnMethod;

	/**
	 * Extended information about the authentication.
	 */
	List<KeyValuePairType> m_extendedInfo;

	/**
	 * Returns the authenticated @link SubjecType for the domain.
	 * 
	 * @param domain
	 *            the name of the domain
	 * @return the subject for the passed in domain name if it exists, null
	 *         otherwise.
	 */
	public SubjectType getAuthnSubject(String domain) {
		if (m_subjects != null) {
			Iterator<SubjectType> i = m_subjects.iterator();
			while (i.hasNext()) {
				SubjectType subj = i.next();
				if (subj.getDomain().equalsIgnoreCase(domain))
					return subj;
			}
		}
		return null;
	}

	/**
	 * Get a cloned, unmodifiable list of subjects that have been authenticated.
	 * 
	 * @return unmodifiable list of @link SubjectType held by this reponse.
	 */
	public final List<SubjectType> getAuthnSubjects() {
		if (m_subjects == null) {
			return new ArrayList<SubjectType>();
		}
		return Collections.unmodifiableList(new ArrayList<SubjectType>(
				m_subjects));
	}

	/**
	 * Adds a subject to the authentication response. Used by the @link
	 * Authenticator during the authentication process.
	 * 
	 * @param subj
	 *            subject to be added to the response. Will throw a
	 *            NullPointerException if passed in value is null.
	 */
	public final void addAuthnSubject(SubjectType subj) {
		if (subj == null) {
			throw new NullPointerException();
		}

		if (m_subjects == null) {
			m_subjects = new ArrayList<SubjectType>();
		}
		m_subjects.add(subj);
	}

	/**
	 * Set a list of subjects to the response. Used by the @link Authenticator
	 * during the authentication process.
	 * 
	 * @param subjects
	 *            a list of @link SubjectTypes to be added to the response. This
	 *            will overwrite any existing subjects already set on the
	 *            response. Will throw a NullPointerException if passed in value
	 *            is null.
	 */
	public void setAuthnSubjects(List<SubjectType> subjects) {
		if (subjects == null) {
			throw new NullPointerException();
		}

		if (m_subjects == null) {
			m_subjects = new ArrayList<SubjectType>();
		}
		m_subjects.clear();
		m_subjects.addAll(subjects);
	}

	/**
	 * Get the list of @link SubjectGroupType set on the response.
	 * 
	 * @return list of subject groups
	 */
	public List<SubjectGroupType> getSubjectGroups() {
		if (m_subjectGroups == null) {
			return new ArrayList<SubjectGroupType>();
		}
		return m_subjectGroups;
	}

	/**
	 * Adds a subject group to the authentication response. Used by the @link
	 * Authenticator during the authentication process.
	 * 
	 * @param subjGroup
	 *            subject group to be added to the response. Will throw a
	 *            NullPointerException if passed in value is null.
	 */
	public final void addSubjectGroup(SubjectGroupType subjGroup) {
		if (subjGroup == null) {
			throw new NullPointerException();
		}

		if (m_subjectGroups == null) {
			m_subjectGroups = new ArrayList<SubjectGroupType>();
		}
		m_subjectGroups.add(subjGroup);
	}

	/**
	 * Set a list of subject groups to the response. Used by the @link
	 * Authenticator during the authentication process.
	 * 
	 * @param subjectGroupList
	 *            a list of @link SubjectGroupType to be added to the response.
	 *            This will overwrite any existing subject groups already set on
	 *            the response. Will throw a NullPointerException if passed in
	 *            value is null.
	 */
	public void setSubjectGroups(List<SubjectGroupType> subjectGroupList) {
		if (subjectGroupList == null) {
			throw new NullPointerException();
		}

		if (m_subjectGroups == null) {
			m_subjectGroups = new ArrayList<SubjectGroupType>();
		}
		m_subjectGroups.clear();
		m_subjectGroups.addAll(subjectGroupList);
	}

	/**
	 * Get a cloned, unmodifiable list of @KeyValuePairType extended information
	 * on the authenticated response.
	 * 
	 * @return unmodifiable list of key-vlaue pairs that describe the extended
	 *         information on the response.
	 */
	public final List<KeyValuePairType> getExtendedInfo() {
		if (m_extendedInfo == null) {
			return new ArrayList<KeyValuePairType>();
		}
		return Collections.unmodifiableList(new ArrayList<KeyValuePairType>(
				m_extendedInfo));
	}

	/**
	 * Add a @link KeyValuePairType extended information object to the response.
	 * 
	 * @param extnInfo
	 *            extended information to be stored in the response.
	 */
	public final void addExtendedInfo(KeyValuePairType extnInfo) {
		if (extnInfo == null) {
			throw new NullPointerException();
		}

		if (m_extendedInfo == null) {
			m_extendedInfo = new ArrayList<KeyValuePairType>();
		}
		m_extendedInfo.add(extnInfo);
	}

	/**
	 * Set a list of @link KeyValuePairType object as the extended information
	 * to the response.
	 * 
	 * @param kvpairs
	 *            a list of @link KeyValuePairType to be added to the response.
	 *            This will overwrite any existing extended information already
	 *            set on the response. Will throw a NullPointerException if
	 *            passed in value is null.
	 */
	public void setExtendedInfo(List<KeyValuePairType> kvpairs) {
		if (kvpairs == null) {
			throw new NullPointerException();
		}

		if (m_extendedInfo == null) {
			m_extendedInfo = new ArrayList<KeyValuePairType>();
		}
		m_extendedInfo.clear();
		m_extendedInfo.addAll(kvpairs);
	}

	/**
	 * Get authentication method.
	 * 
	 * @return authentication method used by the @link Authenticator
	 */
	public String getAuthenticationMethod() {
		return m_authnMethod;
	}

	/**
	 * Set authentication method.
	 * 
	 * @param method
	 *            the authentication method used to authenticate the request.
	 */
	public void setAuthenticationMethod(String method) {
		m_authnMethod = method;
	}

}
