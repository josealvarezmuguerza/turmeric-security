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
import java.util.Iterator;
import java.util.List;

import org.ebayopensource.turmeric.security.v1.services.CredentialType;

/**
 * This class represents an authentication request.
 * 
 * @author gyue
 * 
 */
public class AuthenticationRequest {
	/**
	 * Name of the resource for the request.
	 */
	String m_resourceName;

	/**
	 * Name of the operation to be carried out on the resource.
	 */
	String m_operationName;

	/**
	 * Type of the resource, like service.
	 */
	String m_resourceType;

	/**
	 * List of credentials on the request.
	 */
	List<CredentialType> m_credentials;

	/**
	 * Default constructor.
	 */
	public AuthenticationRequest() {
	}

	/**
	 * Overloaded constructor with resource, operation and resource type.
	 *
	 * @param resource
	 *            the resource on which the operation is to be carried out
	 * @param operation
	 *            the operation to be carried out
	 * @param resourceType
	 *            the type of the resource, e.g. SERVICE
	 */
	public AuthenticationRequest(String resource, String operation,
			String resourceType) {
		m_resourceName = resource;
		m_operationName = operation;
		m_resourceType = resourceType;
	}

	/**
	 * Getter method.
	 * 
	 * @return resource name
	 */
	public String getResourceName() {
		return m_resourceName;
	}

	/**
	 * Setter method.
	 * 
	 * @param name
	 *            the resource name to set
	 */
	public void setResourceName(String name) {
		m_resourceName = name;
	}

	/**
	 * @return operation name
	 */
	public String getOperationName() {
		return m_operationName;
	}

	/**
	 * @param name
	 *            the operation name to set
	 */
	public void setOperationName(String name) {
		m_operationName = name;
	}

	/**
	 * @return resource type
	 */
	public String getResourceType() {
		return m_resourceType;
	}

	/**
	 * @param type
	 *            the resource type to set
	 */
	public void setResourceType(String type) {
		m_resourceType = type;
	}

	/**
	 * Add credential to the request.
	 * 
	 * @param cred
	 *            the @link CredentialType credential to be added to the request
	 */
	public void addCredential(CredentialType cred) {
		if (m_credentials == null)
			m_credentials = new ArrayList<CredentialType>();

		if (cred == null)
			return;

		m_credentials.add(cred);
	}

	/**
	 * Get credential with the specified name. If the credential does not exist
	 * in the credentials list for this request, method returns null..
	 *
	 * @param name
	 *            the name of the credential to be returned
	 * @return @link CredentialType if one exists with the specified name, null
	 *         otherwise.
	 */
	public CredentialType getCredential(String name) {
		if (m_credentials != null) {
			Iterator<CredentialType> i = m_credentials.iterator();
			while (i.hasNext()) {
				CredentialType cred = i.next();
				if (cred.getName().equalsIgnoreCase(name))
					return cred;
			}
		}
		return null;
	}

	/**
	 * Set the credential list with the passed in list. This will replace any
	 * existing credentials, if any.
	 * 
	 * @param credentials
	 *            the list of credentials to be set for this request.
	 */
	public void setCredentials(List<CredentialType> credentials) {
		if (m_credentials == null) {
			m_credentials = new ArrayList<CredentialType>();
		}
		m_credentials.clear();
		m_credentials.addAll(credentials);
	}

	/**
	 * Get the list of credentials for this request.
	 * 
	 * @return the list of @link CredentialType set on this request.
	 */
	public List<CredentialType> getCredentials() {
		if (m_credentials == null)
			m_credentials = new ArrayList<CredentialType>();
		return m_credentials;
	}

}
