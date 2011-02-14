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

/**
 * Authentication Exception.
 * 
 * @author gyue
 */
public class AuthenticationException extends Exception {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor with message.
	 * 
	 * @param errorMsg   error message for this exception. 
	 */
	public AuthenticationException(String errorMsg) {
		super(errorMsg);
	}

	/**
	 * Constructor with @link Throwable cause. This new exception will wrap the cause.
	 * 
	 * @param cause	 the @link Throwable that needs to be wrapped by this exception. 
	 */
	public AuthenticationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor with @link Throwable cause and custom error message. 
	 * This new exception will wrap the cause and contain the specified error message.
	 * 
	 * @param cause	 the @link Throwable that needs to be wrapped by this exception.
	 * @param errorMessage   error message for this exception.
	 */
	public AuthenticationException(String errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
	
	
}
