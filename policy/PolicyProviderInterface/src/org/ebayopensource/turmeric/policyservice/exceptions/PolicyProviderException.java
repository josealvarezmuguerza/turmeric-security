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
 * Base class for policy provider exceptions.
 *
 */
public class PolicyProviderException extends Exception {
	
	/**
	 * Enum types for the exception category.
	 */
	public static enum Category {
		/**
		 * Policy related exception.
		 */
		POLICY,
		/**
		 * Resource related exception.
		 */
		RESOURCE,
		/**
		 * Operation related exception.
		 */
		OPERATION,
		/**
		 * Subject related exception.
		 */
		SUBJECT,
		/**
		 * Subject group related exception.
		 */
		SUBJECTGROUP,
		/**
		 * Rule related exception.
		 */
		RULE,
		/**
		 * Query related exception
		 */
		QUERY
	};
	
	/**
	 * 
	 */
	private final Category m_category;
	
	/**
	 * 
	 */
	private final Long m_entityId;
	
	/**
	 * 
	 */
	private final String m_entityName;
	
	/**
	 * 
	 */
	private final String m_entityType;	
	
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
	public PolicyProviderException(Category category, String entityType, String errorMsg) {
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
	public PolicyProviderException(Category category, String entityType, Throwable cause) {
		this(category, entityType, cause.getMessage(), cause);
	}

	/**
	 * Constructor.
	 * @param category
	 *            category of the exception
	 * @param entityType
	 *            type of the entity related to the exception
	 * @param errorMessage
	 *            detailed error message
	 * @param cause
	 *            the cause
	 */
	public PolicyProviderException(Category category, String entityType, String errorMessage, Throwable cause) {
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
	public PolicyProviderException(Category category, String entityType, Long entityId, String errorMessage, Throwable cause) {
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
	public PolicyProviderException(Category category, String entityType, Long entityId,
			String entityName, String errorMsg, Throwable cause) {
		super(errorMsg, cause);
		m_category = category;
		m_entityId = entityId;
		m_entityName = entityName;
		m_entityType = entityType;
	}

	/**
	 * Get the category of the exception.
	 * @return category
	 */
	public Category getCategory() {
		return m_category;
	}

	/**
	 * Get the Id of the entity related to the exception.
	 * @return entity Id
	 */
	public Long getEntityId() {
		return m_entityId;
	}

	/**
	 * Get the name of the entity related to the exception.
	 * @return entity name
	 */
	public String getEntityName() {
		return m_entityName;
	}
	
	/**
	 * Get the type of the entity related to the exception.
	 * @return entity type
	 */
	public String getEntityType() {
		return m_entityType;
	}

}
