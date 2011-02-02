/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.ratelimiterservice.impl;

/**
 * @author gbaal
 * 
 */
public class RateLimiterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1110137073012526421L;
	private String errorId;

	public String getErrorId() {
		return errorId;
	}

	public RateLimiterException(String errorId, String errorMsg) {
		super(errorMsg);
		this.errorId = errorId;
	}
	public RateLimiterException(String errorId, Throwable cause){
		super(cause);
		this.errorId = errorId;
	}
	public RateLimiterException(String errorId, String errorMsg, Throwable cause){
		super(errorMsg, cause);
		this.errorId = errorId;
	}
}
