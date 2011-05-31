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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.errorlibrary.turmericratelimiter.ErrorConstants;
import org.ebayopensource.turmeric.ratelimiter.provider.RateLimiterProvider;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorDataFactory;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceRuntimeException;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedResponse;
import org.ebayopensource.turmeric.services.ratelimiterservice.provider.config.RateLimiterServiceProviderFactory;
import org.ebayopensource.turmeric.utils.ContextUtils;

import org.ebayopensource.turmeric.services.ratelimiterservice.intf.RateLimiterService;

/**
 * The Class RateLimiterServiceImpl.
 */
public class RateLimiterServiceImpl
    implements RateLimiterService
{
	private static volatile RateLimiterProvider s_provider;
	
	private static final String s_providerPropFilePath = 
		"META-INF/soa/services/config/RateLimiterService/service_provider.properties";
	private static final String s_providerPropKey = "preferred-provider";


	private static List<CommonErrorData> s_errorData = null;

	private static void initialize() {
		if (s_errorData != null) {
			throw new ServiceRuntimeException(s_errorData);
		}
		try {
			if (s_provider == null) {
				synchronized (RateLimiterServiceImpl.class) {
					if (s_provider == null)	{						
						s_provider = RateLimiterServiceProviderFactory.create(getPreferredProvider());
					}
				}
			}
		} catch (ServiceException se) {
			s_errorData = se.getErrorMessage().getError();
			throw new ServiceRuntimeException(s_errorData);
		}
	}
	
	private static String getPreferredProvider() {
		ClassLoader classLoader = ContextUtils.getClassLoader();
		InputStream	inStream = classLoader.getResourceAsStream(s_providerPropFilePath);
		String provider = null;
		if (inStream != null) {
			Properties properties = new Properties();
			try {
				properties.load(inStream);
				provider = (String)properties.get(s_providerPropKey);
			} catch (IOException e) {
				// ignore
			}
			finally {
				try {
					inStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return provider;
	}

	/**
	 * {@inheritDoc}
	 */
    @Override
	public IsRateLimitedResponse isRateLimited(IsRateLimitedRequest rateLimiterRequest) {
    
    	initialize();
    
    	if (s_provider == null ) {
    	    throw new ServiceRuntimeException(
    	    		ErrorDataFactory.createErrorData(
    	    				ErrorConstants.SVC_RATELIMITER_SERVICE_INIT_FAILED,
    	    				ErrorConstants.ERRORDOMAIN.toString(),
    	    				new Object[] {"invalid configuraton"}));
    	}
    	//  dispatch it to the provider
    	return s_provider.isRateLimited(rateLimiterRequest);   	
    }


}
