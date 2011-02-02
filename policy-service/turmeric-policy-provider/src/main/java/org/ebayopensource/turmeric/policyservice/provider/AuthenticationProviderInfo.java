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

import java.util.List;

/**
 * AuthenticationProviderInfo
 * 
 * Container for authentication methods with its
 * corresponding resource and operation
 * 
 * Code derived from EBay reference codes
 *
 */
class AuthenticationProviderInfo {
	private String resourceName;
	private String resourceType;
	private String operationName;
	private List<String> authenticationMethods;

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public String getResourceType() {
		return resourceType;
	}
	
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public String getOperationName() {
		return operationName;
	}
	
	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public List<String> getAuthenticationMethods() {
		return authenticationMethods;
	}
	
	public void setAuthenticationMethods(List<String> authenticationMethods) {
		this.authenticationMethods = authenticationMethods;
	}
	
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("resource name {").append(resourceName).append("}");
		buffer.append(" resource type {").append(resourceType).append("}");
		buffer.append(" operation name {").append(operationName).append("}");
		if (authenticationMethods != null && !authenticationMethods.isEmpty()) {
			buffer.append(" authentication methods\n");
			for (String authMethod : authenticationMethods) {
				buffer.append("  method {").append(authMethod).append("}");
			}
		}
		return buffer.toString();
	}
}
