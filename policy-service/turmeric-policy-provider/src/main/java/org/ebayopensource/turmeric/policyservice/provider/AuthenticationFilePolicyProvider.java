/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.utils.DomParseUtils;
import org.ebayopensource.turmeric.utils.config.exceptions.PolicyProviderException;
import org.ebayopensource.turmeric.utils.config.impl.BaseFilePolicyProvider;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * AuthenticationFilePolicyProvider
 * 
 * A policy provider that parses policy given the XML configuration and
 * its corresponding schema XSD
 * 
 * Code derived from EBay reference codes
 *
 */
public class AuthenticationFilePolicyProvider extends BaseFilePolicyProvider implements AuthenticationPolicyProvider {
	
	private static Logger s_logger = LogManager.getInstance(AuthenticationFilePolicyProvider.class);
	
	private static final String RELATIVE_CONFIG_PATH = "META-INF/security/config";
	private static final String RELATIVE_SCHEMA_PATH = "META-INF/security/schema";

	private static final String AUTHN_POLICY_FILENAME = "AuthenticationPolicy.xml";
	private static final String AUTHN_POLICY_SCHEMA = "AuthenticationPolicy.xsd";
	private static final String POLICY_ROOT_ELEMENT = "authentication-policy";
	private static final String ALL_OP = "*";

	private String policyFile;
	private String policySchema;
	private String policyRootElement;
	private Map<Resource, OperationHolder> m_operationHolderMap;
	
	public AuthenticationFilePolicyProvider() {
		this(AUTHN_POLICY_FILENAME, AUTHN_POLICY_SCHEMA, POLICY_ROOT_ELEMENT);
	}
	
	AuthenticationFilePolicyProvider(String policyFile, String policySchema, String policyRootElement) {
		super();
		this.policyFile = policyFile;
		this.policySchema = policySchema;
		this.policyRootElement = policyRootElement;
	}

	@Override
	public String getPolicyFileName() {
		return RELATIVE_CONFIG_PATH + "/" + policyFile;
	}

	@Override
	public String getPolicySchemaName() {
		return RELATIVE_SCHEMA_PATH + "/" + policySchema;
	}

	@Override
	public String getPolicyRootElement() {
		return policyRootElement;
	}
	
	@Override
	protected void mapPolicyData(Element policyData) throws PolicyProviderException {
		m_operationHolderMap = new HashMap<Resource, OperationHolder>();
		m_operationHolderMap.clear();
 
		if (policyData == null) {
			s_logger.log(Level.SEVERE, "AuthenticationFilePolicyProvider mapPolicyData(): Authn policy data is empty!!!");
			return;
		}
		try {
			// iterate thru the resource list
			NodeList resourceList = DomParseUtils.getImmediateChildrenByTagName(policyData, "resource");
			for (int i = 0; i < resourceList.getLength(); i++) {
				Element resource = (Element) resourceList.item(i);
				
				String resourceName = DomParseUtils.getRequiredAttribute(getPolicyFileName(), resource, "name");
				String resourceType = DomParseUtils.getRequiredAttribute(getPolicyFileName(), resource, "type");
				String defaultAuthenticationMethod = DomParseUtils.getRequiredAttribute(getPolicyFileName(), resource, "default-authentication-method");
				if (defaultAuthenticationMethod != null) {
					// TODO - verify why need to convert to lower case
					defaultAuthenticationMethod = defaultAuthenticationMethod.toLowerCase();
				}
					
				// TODO - verify why need to convert to upper case
				Resource resourceObj = new Resource(resourceType.toUpperCase(), resourceName);
				OperationHolder operationHolder = new OperationHolder(defaultAuthenticationMethod);
				m_operationHolderMap.put(resourceObj, operationHolder);
	
				NodeList opList = DomParseUtils.getImmediateChildrenByTagName(resource, "operation");
				// iterate thru the operation list
				for (int j = 0; j < opList.getLength(); j++) {
					Element operation = (Element) opList.item(j);
					String operationName = DomParseUtils.getRequiredAttribute(getPolicyFileName(), operation, "name");
					NodeList authnMethodList = DomParseUtils.getImmediateChildrenByTagName(operation, "authentication-method");
					List<String> authenticationMethods = new ArrayList<String>();

					for (int k = 0; k < authnMethodList.getLength(); k++) {
						Element authnMethodElem = (Element) authnMethodList.item(k);
						String authnMethod = authnMethodElem.getFirstChild().getNodeValue();
						if (authnMethod != null) {
							authnMethod = authnMethod.toLowerCase();
							authenticationMethods.add(authnMethod);
						}
					}
					// add operation (key) and authentication methods (value) to operation holder
					operationHolder.addAuthenticationMethods(operationName, authenticationMethods);
				}
			}
		} catch(Exception e) {
			s_logger.log(Level.SEVERE, "exception", e);
			throw new PolicyProviderException( 
					"Error in mapping authn file policy: " + e.getMessage(),
					e);
		}
	}

	/**
	 * Return the authentication policy associated to this resource/operation
	 * @param resource
	 * @param operation
	 * @return
	 */
	public AuthenticationProviderInfo getAuthnPolicyByResource(String resourceName, String operationName, String resourceType) throws PolicyProviderException {
		
		List<String> emptyAuthMethods = Collections.emptyList();
		AuthenticationProviderInfo resourceAuthInfo = new AuthenticationProviderInfo();
		resourceAuthInfo.setResourceName(resourceName);
		resourceAuthInfo.setResourceType(resourceType);
		resourceAuthInfo.setOperationName(operationName);
		resourceAuthInfo.setAuthenticationMethods(emptyAuthMethods);

		if (resourceName == null ||
			operationName == null ||
			resourceType == null ||
			resourceName.equals("") ||
			operationName.equals("") ||
			resourceType.equals("")) {
			return resourceAuthInfo;
		}
		
		// TODO - verify why need to convert to upper case
		Resource resource = new Resource(resourceType.toUpperCase(), resourceName);
		OperationHolder opHolder = m_operationHolderMap.get(resource);
		if (opHolder == null) {
			return resourceAuthInfo;
		}
		
		if (opHolder.getDefaultAuthenticationMethod() == null) {
			String errorMsg = "Policy data error: Authentication method not defined for resource " + resource.getName() +
			", type " + resource.getType() + ", and operation " + operationName;
			throw new PolicyProviderException(
					errorMsg);
		}

		List<String> defaultAuthenticationMethods = new ArrayList<String>();
		defaultAuthenticationMethods.add(opHolder.getDefaultAuthenticationMethod());
		
		List<String> authenticationMethods = opHolder.getAuthenticationMethods(operationName);
		if (authenticationMethods == null || authenticationMethods.isEmpty()) {
			// check for "*" if no matching authentication methods returned for this operation
			// otherwise use the default authentication method
			authenticationMethods = opHolder.getAuthenticationMethods(ALL_OP);
			if (authenticationMethods != null) {
				resourceAuthInfo.setAuthenticationMethods(authenticationMethods);
			} else {
				resourceAuthInfo.setAuthenticationMethods(defaultAuthenticationMethods);
			}
		} else {
			// use authentication methods returned for this operation
			resourceAuthInfo.setAuthenticationMethods(authenticationMethods);
		}
		return resourceAuthInfo;
	}
	
	/**
	 * The logic of this class was copied from Resource class in reference/PolicyServiceProviderImpl.
	 *
	 */
	private static class Resource {
		private String m_type;
		private String m_name;

		public Resource(String type, String name) {
			m_type = type;
			m_name = name;
		}
		
		public String getType() {
			return m_type;
		}
		
		public String getName() {
			return m_name;
		}
		
		@Override
		public int hashCode() {
			final int PRIME = 31;
			int result = 1;
			result = PRIME * result + ((m_name == null) ? 0 : m_name.hashCode());
			result = PRIME * result + ((m_type == null) ? 0 : m_type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}	
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Resource other = (Resource) obj;
			if (m_name == null) {
				if (other.m_name != null) {
					return false;
				}
			} else if (!m_name.equals(other.m_name)) {
				return false;
			}
			if (m_type == null) {
				if (other.m_type != null) {
					return false;
				}
			} else if (!m_type.equals(other.m_type)) {
				return false;
			}
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("type {").append(m_type).append("}");
			buffer.append(" name {").append(m_name).append("}");
			return buffer.toString();
		}
	}
	
	/**
	 * The logic of this class was copied from AuthnPolicyOperationHolder class in reference/PolicyServiceProviderImpl.
	 *
	 */
	private static class OperationHolder {
		// map of operation name (key) and authenticated methods (value)
		private Map<String, List<String>> m_authenticationMethods;
		private String m_defaultAuthenticationMethod;

		public OperationHolder(String defaultAuthenticationMethod) {
			m_authenticationMethods = new HashMap<String, List<String>>();
			this.m_defaultAuthenticationMethod = defaultAuthenticationMethod;
		}
		
		public List<String> getAuthenticationMethods(String operationName) {
			return m_authenticationMethods.get(operationName);
		}
		
		public String getDefaultAuthenticationMethod() {
			return m_defaultAuthenticationMethod;
		}
		
		public void addAuthenticationMethods(String operationName, List<String> authenticationMethods) {
			m_authenticationMethods.put(operationName, authenticationMethods);
		}
		
		@Override
		public String toString() {
			StringBuilder buffer = new StringBuilder();
			buffer.append("default authentication method {").append(m_defaultAuthenticationMethod).append("}");
			if (m_authenticationMethods != null && m_authenticationMethods.size() > 0) {
				Iterator<String> keyIter = m_authenticationMethods.keySet().iterator();
				while (keyIter.hasNext()) {
					String key = keyIter.next();
					buffer.append(" operation {").append(key).append("}");
					List<String> authMethods = m_authenticationMethods.get(key);
					if (authMethods != null && authMethods.size() > 0) {
						for (String authMethod : authMethods) {
							buffer.append(" authentication method {").append(authMethod).append("}");
						}
					}
					
				}
			}
			return buffer.toString();
		}
	}
}
