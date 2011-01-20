/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl;


/**
 * This exception is thrown when an authorization error occurred. It could be 
 * wrapping an internal error or indicating a failed authorization. 
 * 
 * @author dmuthiayen
 */
public class AuthorizationException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String errorId;

	/**
	 * Simple message-based constructor. 
	 * @param errorId the error id. 
	 * @param errorMsg the error message. 
	 */
	public AuthorizationException(String errorId, String errorMsg) {
		super(errorMsg);
		
		this.errorId = errorId;
	}

	/**
	 * Simple cause-based constructor. 
	 * @param errorId the error id. 
	 * @param cause the original throwable being wrapped/represented. 
	 */
	public AuthorizationException(String errorId, Throwable cause) {
		super(cause);
		
		this.errorId = errorId;
	}

	/**
	 * Full constructor. 
	 * @param errorId the error id. 
	 * @param errorMessage the error message.
	 * @param cause the original throwable being wrapped/represented. 
	 */
	public AuthorizationException(String errorId, String errorMessage,
			Throwable cause) {
		super(errorMessage, cause);
		
		this.errorId = errorId;
	}

	/**
	 * Getter for the error ID.  
	 * @return errorId the error id. 
	 */
	public String getErrorId() {
		return errorId;
	}

}