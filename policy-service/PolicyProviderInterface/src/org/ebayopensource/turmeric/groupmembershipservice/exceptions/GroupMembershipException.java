/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.groupmembershipservice.exceptions;

/**
 * This is the base class group membership searching exception.
 * 
 * @author rpallikonda
 */
public class GroupMembershipException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param errorMsg
	 *            Detailed error message
	 */
	public GroupMembershipException(String errorMsg) {
		super(errorMsg);
	}

	/**
	 * Constructor.
	 * 
	 * @param cause
	 *            The cause
	 */
	public GroupMembershipException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 * @param errorMessage
	 *            Detailed error message
	 * @param cause
	 *            The case
	 */
	public GroupMembershipException(String errorMessage, Throwable cause) {
		super(errorMessage, cause);
	}
}
