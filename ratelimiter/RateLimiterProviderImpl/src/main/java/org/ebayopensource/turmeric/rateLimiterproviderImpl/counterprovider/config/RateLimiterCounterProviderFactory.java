/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.rateLimiterproviderImpl.counterprovider.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.errorlibrary.turmericratelimiter.ErrorConstants;
import org.ebayopensource.turmeric.rateLimiterCounterProvider.RateLimiterCounterProvider;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorDataFactory;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.utils.ReflectionUtils;
import org.ebayopensource.turmeric.utils.config.exceptions.ConfigurationException;

/**
 * A factory for creating RateLimiterCounterProvider objects.
 */
public class RateLimiterCounterProviderFactory {
	
	private static Map<String, RateLimiterCounterProvider>  counterProviderMap = new HashMap<String, RateLimiterCounterProvider>();
	private static Set<String> s_failedProviders = new HashSet<String>();
	private static String s_defaultProviderKey;
	private static volatile CommonErrorData s_errorData;
	private static Logger s_Logger = LogManager.getInstance(RateLimiterCounterProviderFactory.class);
	
	static {
		// static initialization
		RateLimiterCounterProviderConfigManager configMngr = RateLimiterCounterProviderConfigManager.getInstance();
		try {
			s_defaultProviderKey = configMngr.getConfig().getDefaultProvider();			
		} catch (ConfigurationException e) {
			s_errorData = getConfigError(configMngr);
		}
	}
	
	// disable creating instances
	private RateLimiterCounterProviderFactory() {
		
	}
	
	/**
	 * Creates the.
	 *
	 * @return the rate limiter counter provider
	 * @throws ServiceException the service exception
	 */
	public static RateLimiterCounterProvider create() throws  ServiceException {
		return create(s_defaultProviderKey);
	}

	/**
	 * Creates the.
	 *
	 * @param providerKey the provider key
	 * @return the rate limiter provider
	 * @throws ServiceException the service exception
	 */
	public static RateLimiterCounterProvider create(String providerKey) throws ServiceException { 
		
		if (s_errorData != null) {
			throw new ServiceException(s_errorData);
		}
		
		if (providerKey == null){
			providerKey = s_defaultProviderKey;
		}
		
		
		RateLimiterCounterProvider providerImpl = counterProviderMap.get(providerKey);
		RateLimiterCounterProviderConfigManager configMngr = RateLimiterCounterProviderConfigManager.getInstance();
		
		if (providerImpl == null) {
			// check the failed set
			if (s_failedProviders.contains(providerKey)) {
				throw new ServiceException(getConfigError(configMngr));
			}
			synchronized (RateLimiterCounterProviderFactory.class) {
				providerImpl = counterProviderMap.get(providerKey);
				if (providerImpl == null) {
					try {
						String providerImplClassName = configMngr.getConfig().getProviderImplClassName(providerKey);
						if (providerImplClassName != null) {
							providerImpl = getCounterDataModelProviderInstance(providerImplClassName);
							if (providerImpl != null)
								counterProviderMap.put(providerKey, providerImpl);
						}
					} catch (ConfigurationException ce) {
						s_Logger.log(Level.SEVERE, "invalid configuration" , ce);
					}
				}
				if (providerImpl == null) {
					s_failedProviders.add(providerKey);
				}
			}
			
			if (providerImpl == null) {
				throw new ServiceException(getConfigError(configMngr));
			}
		}		
		
		return providerImpl;
	}

	private static RateLimiterCounterProvider getCounterDataModelProviderInstance(String rateLimiterCounterProviderClassName) {
		
		RateLimiterCounterProvider counterProviderImpl = null;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			counterProviderImpl = ReflectionUtils.createInstance(rateLimiterCounterProviderClassName, RateLimiterCounterProvider.class, cl);

		} catch (Exception e) {
			s_Logger.log(Level.SEVERE, 
					"The RateLimiterCounterProvider class name: " 
						+ rateLimiterCounterProviderClassName + " is invalid",
					e);
			
		}
		return counterProviderImpl;
	}

	private static CommonErrorData getConfigError(
			RateLimiterCounterProviderConfigManager configMngr) {
		return ErrorDataFactory.createErrorData(
				ErrorConstants.SVC_RATELIMITER_INVALID_PROVIDER_CONFIGURATION, 
				ErrorConstants.ERRORDOMAIN.toString(),
				new Object[] {"RateLimiterCounter", 
					configMngr.getConfigPath() + 
					configMngr.getConfigFileName()});
	}

}
