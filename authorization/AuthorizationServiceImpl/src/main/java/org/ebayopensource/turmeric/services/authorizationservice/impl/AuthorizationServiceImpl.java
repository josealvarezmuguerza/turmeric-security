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

import org.ebayopensource.turmeric.security.v1.services.AuthorizeRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeResponseType;

import org.ebayopensource.turmeric.services.authorizationservice.impl.biz.InternalAuthorizationServiceImpl;
import org.ebayopensource.turmeric.services.authorizationservice.intf.AuthorizationService;

/**
 * Authorization Service Implementation class. The business logic is delegated to
 * {@link InternalAuthorizationServiceImpl}, as it relies on a cache which must be
 * held in a static context. This class is not a singleton, but can have multiple 
 * instances as controlled by the Turmeric SOA framework. 
 * 
 * @author mpoplacenel
 */
public class AuthorizationServiceImpl implements AuthorizationService {
	
	/**
	 * Performs an authorization request by delegating to {@link InternalAuthorizationServiceImpl#authorize(AuthorizeRequestType)}.
	 * The logic is not inlined as the underlying cache is held in the static context and
	 * it needs a singleton; this class cannot be a singleton, hence the {@link InternalAuthorizationServiceImpl}.
	 *   
	 * @param param0 the request object.
	 * @return the response object. 
	 * 
	 * @see org.ebayopensource.turmeric.services.authorizationservice.intf.AuthorizationService#authorize(org.ebayopensource.turmeric.security.v1.services.AuthorizeRequestType)
	 */
	public AuthorizeResponseType authorize(AuthorizeRequestType param0) {
		return InternalAuthorizationServiceImpl.getInstance().authorize(param0);
	}

}