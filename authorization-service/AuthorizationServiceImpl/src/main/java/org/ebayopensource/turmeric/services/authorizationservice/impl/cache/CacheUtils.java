/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl.cache;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorConstants;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.services.authorizationservice.impl.AuthorizationException;
import org.ebayopensource.turmeric.utils.cache.AbstractCache.CacheBuildingException;


/**
 * Cross-functional utilities for the cache package. 
 * @author mpoplacenel
 */
public class CacheUtils {

	private static final Logger LOGGER = LogManager.getInstance(CacheUtils.class);
	
	private CacheUtils() {
		// no instance
	}

	/**
	 * Converts/wraps the given exception to an {@link AuthorizationException}. 
	 * @param e the exception to convert/wrap. 
	 * @return the converted {@link AuthorizationException}.
	 */
	public static AuthorizationException toAuthorizationException(CacheBuildingException e) {
		if (e.getCause() instanceof AuthorizationException) {
			// we're losing some part of the stack trace; log it...
			LOGGER.log(Level.SEVERE, "Error in the cache builder", e);
			return (AuthorizationException) e.getCause();
		}
		return new AuthorizationException(ErrorConstants.SVC_SECURITY_APP_AUTHZ_INTERNAL_ERROR, e);
	}
	
}
