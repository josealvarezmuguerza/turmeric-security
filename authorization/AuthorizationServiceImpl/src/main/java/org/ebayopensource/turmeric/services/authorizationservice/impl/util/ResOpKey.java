/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl.util;

import org.ebayopensource.turmeric.utils.ObjectUtils;


/**
 * Simple JavaBean encapsulating information about a resource type, resource name
 * and operation name tuple. 
 * 
 * @author mpoplacenel
 */
public class ResOpKey {
	
	private final String resourceType;
	
	private final String resourceName;
	
	private final String operationName;

	/**
	 * Constructor. 
	 * @param resourceType the resource type. 
	 * @param resourceName the resource name.
	 * @param operationName the operatin name. 
	 */
	public ResOpKey(String resourceType, String resourceName, String operationName) {
		this.resourceType = resourceType;
		this.resourceName = resourceName;
		this.operationName = operationName;
	}

	/**
	 * Getter for the resource type. 
	 * @return the resource type.
	 */
	public String getResourceType() {
		return resourceType;
	}

	/**
	 * Getter for the resource name. 
	 * @return the resource name.
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * Getter for the operation name. 
	 * @return the operation name.
	 */
	public String getOperationName() {
		return operationName;
	}

	/**
	 * Overrides {@link Object#equals(Object)} to compare the contents. 
	 * @param o the object to compare against. 
	 * @return <code>true</code> if all properties are equal, <code>false</code> otherwise. 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this == o) return true;
		if (!getClass().equals(o.getClass())) return false;
		ResOpKey that = (ResOpKey) o;
		return ObjectUtils.bothNullOrEqual(getResourceType(), that.getResourceType())
			&& ObjectUtils.bothNullOrEqual(getResourceName(), that.getResourceName()) 
			&& ObjectUtils.bothNullOrEqual(getOperationName(), that.getOperationName());
	}

	/**
	 * Overrides {@link Object#hashCode()} to provide a content-based hash code. 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ObjectUtils.hashCodeOrZero(getResourceType()) * 31 * 31
				+ ObjectUtils.hashCodeOrZero(getResourceName()) * 31 
				+ ObjectUtils.hashCodeOrZero(getOperationName());
	}

	/**
	 * Provides a string representation that includes the contents of the bean,  
	 * to be a bit more informative :D.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String superStr = super.toString();
		return superStr.substring(superStr.lastIndexOf(".") + 1) + "(" 
			+ EncodingUtils.encodeResOpKey(getResourceType(), getResourceName(), getOperationName())
			+ ")";
	}
	
}