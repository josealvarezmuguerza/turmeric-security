/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl.biz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ebayopensource.turmeric.runtime.spf.impl.internal.config.Initializer;
import org.ebayopensource.turmeric.services.authorizationservice.impl.AuthorizationException;


/**
 * This class represents the Initializer for the Authorization Service.
 * It keeps track of the initializer calls in an internal thread-safe list of service names, 
 * which one can access via {@link #getServiceNames()}. 
 * 
 * @author mpoplacenel
 */
public class AuthorizationServiceInitializer implements Initializer {
	
	private List<String> m_serviceNames = new ArrayList<String>();
	
	/**
	 * Initializes the Authorization Service for the given service name.
	 * @param serviceName the service name to perform the initialization for.
	 * @throws InitializerException wrapping any problem that occurred while 
	 * executing the initialization logic. 
	 * @see org.ebayopensource.turmeric.soaframework.spf.impl.internal.config.Initializer#initialize(java.lang.String)
	 */
	@Override
	public void initialize(String serviceName) throws InitializerException {
		try {
			InternalAuthorizationServiceImpl.getInstance().initialize(serviceName);
			synchronized (m_serviceNames) {
				m_serviceNames.add(serviceName);
			}
		} catch (AuthorizationException e) {
			throw new InitializerException("Error initializing AuthorizationService for service " + serviceName, e);
		}
	}
	
	/**
	 * Provide the list of service names the initialization was performed for. 
	 * @return the list of service names the initialization was performed for.
	 * @see org.ebayopensource.turmeric.runtime.spf.impl.internal.config.Initializer#getServiceNames()
	 */
	public List<String> getServiceNames() {
		synchronized (m_serviceNames) {
			return Collections.unmodifiableList(m_serviceNames);
		}
		
	}

}