/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.exceptions;

/**
 * The class for policy updating exceptions.
 *
 */
public class PolicyUpdateException extends PolicyProviderException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Constructor.
	 * @param category
	 *            category of the exception
	 * @param entityType
	 *            type of the entity related to the exception
	 * @param errorMsg
	 *            detailed error message
	 */
	public PolicyUpdateException(Category category, String entityType, String errorMsg) {
		this(category, entityType, errorMsg, null);
	}

	/**
	 * Constructor.
	 * @param category
	 *            category of the exception
	 * @param entityType
	 *            type of the entity related to the exception
	 * @param cause
	 *            the cause
	 */
	public PolicyUpdateException(Category category, String entityType, Throwable cause) {
		this(category, entityType, cause.getMessage(), cause);
	}

	/**
	 * @param category
	 *            category of the exception
	 * @param entityType
	 *            type of the entity related to the exception
	 * @param errorMessage
	 *            detailed error message
	 * @param cause
	 *            the cause
	 */
	public PolicyUpdateException(Category category, String entityType, String errorMessage, Throwable cause) {
		this(category, entityType, null, errorMessage, cause);
	}
	
	/**
	 * Constructor.
	 * @param category
	 *            category of the exception
	 * @param entityType
	 *            type of the entity related to the exception
	 * @param entityId
	 *            Id of the entity related to the exception
	 * @param errorMessage
	 *            detailed error message
	 * @param cause
	 *            the cause
	 */
	public PolicyUpdateException(Category category, String entityType, Long entityId, String errorMessage, Throwable cause) {
		this(category, entityType, entityId, null, errorMessage, cause);
	}

	/**
	 * Constructor.
	 * @param category
	 *            category of the exception
	 * @param entityType
	 *            type of the entity related to the exception
	 * @param entityId
	 *            Id of the entity related to the exception
	 * @param entityName
	 *            name of the entity related to the exception
	 * @param errorMsg
	 *            detailed error message
	 * @param cause
	 *            the cause
	 */
	public PolicyUpdateException(Category category, String entityType, Long entityId,
			String entityName, String errorMsg, Throwable cause) {
		super(category,entityType,entityId,entityName,errorMsg, cause);
	}

}
