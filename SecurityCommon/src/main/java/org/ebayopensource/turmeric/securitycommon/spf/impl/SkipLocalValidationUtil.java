/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *    
 * http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.securitycommon.spf.impl;

import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;

/**
 * This class provides a utility method to check
 * if the authn / authz needs to be skipped for
 * a local call.
 * 
 * @author prjande
 */

public class SkipLocalValidationUtil {

	/** The Constant SKIP_RESOURCE. */
	private static final String SKIP_RESOURCE = "PolicyService";
	
	/** The Constant SKIP_OPERATIONS. */
	private static final String[] SKIP_OPERATIONS = {"getAuthenticationPolicy", "findPolicies"};
	
	/**
	 * Instantiates a new skip local validation util.
	 */
	private SkipLocalValidationUtil() {
		
	}
	
	/**
	 * Check if need to skip based on predefined conditions.
	 * @param ctx the message context including the necessary information.
	 * @return true if to skip and flase otherwise
	 */
	public static boolean checkIfSkipOnLocal(MessageContext ctx) {
		String resource = getResourceName(ctx);
		String operation = ctx.getOperationName();
		
		if(resource == null || operation == null) {
			return false;
		}
		
		if(SKIP_RESOURCE.equalsIgnoreCase(resource) ) {			
			for(String opName : SKIP_OPERATIONS) {
				if(opName.equalsIgnoreCase(operation)) {
					return true;
				}
			}			
		}
		return false;
	}
	
	/**
	 * Gets the resource name.
	 *
	 * @param ctx the ctx
	 * @return the resource name
	 */
	private static String getResourceName(MessageContext ctx) {
		String resourceName = ctx.getAdminName();
		if (resourceName == null ) { // For Backward compatibility
			resourceName = ctx.getServiceQName().getLocalPart();
		}		
		return resourceName;
	}
	
}
