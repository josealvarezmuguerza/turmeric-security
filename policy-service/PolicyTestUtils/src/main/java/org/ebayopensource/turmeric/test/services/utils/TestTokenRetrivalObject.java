/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.test.services.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.utils.ReflectionUtils;

public class TestTokenRetrivalObject{

	private static SecurityTokenUtility ref;
	
	private static final String s_providerPropFilePath = 
		"META-INF/test/config/securitytokenclass.properties";
	private static final String s_providerPropKey = "token-class";
	
    public static SecurityTokenUtility getSecurityTokenRetrival() throws IOException, ServiceException{
    	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		InputStream	inStream = classLoader.getResourceAsStream(s_providerPropFilePath);
		String provider = null;
		if (inStream != null) {
			Properties properties = new Properties();
			try {
				properties.load(inStream);
				provider = (String)properties.get(s_providerPropKey);
			} catch (IOException e) {
				throw new IOException("Could not load META-INF/test/config/securitytokenclass.properties file");
			}
			finally {
				try {
					inStream.close();
				} catch (IOException e) {
					throw new IOException("Did not close the file ");
				}
			}
		}
		try {
			ref = ReflectionUtils.createInstance(
					provider, SecurityTokenUtility.class, classLoader);
		} catch (ServiceException e) {
			throw new ServiceException("Missing required test file META-INF/test/config/securitytokenclass.properties file");
		}
		
       return ref;
    }

//    public static void setTokenRetrivalObject(SecurityTokenUtility val){
//    	ref = val;
//    }

}

