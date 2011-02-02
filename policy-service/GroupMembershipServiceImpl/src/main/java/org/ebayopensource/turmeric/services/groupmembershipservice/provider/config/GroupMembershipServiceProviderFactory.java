/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.groupmembershipservice.provider.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.errorlibrary.turmericpolicy.ErrorConstants;
import org.ebayopensource.turmeric.groupmembershipservice.provider.GroupMembershipProvider;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorUtils;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.utils.ReflectionUtils;
import org.ebayopensource.turmeric.utils.config.exceptions.ConfigurationException;


public class GroupMembershipServiceProviderFactory {
	
	private static Map<String, GroupMembershipProvider>  s_serviceDMProviderMap = new HashMap<String, GroupMembershipProvider>();
	private static Set<String> s_failedProviders = new HashSet<String>();
	private static String s_defaultProviderKey;
	private static volatile CommonErrorData s_errorData;
	private static Logger s_Logger = LogManager.getInstance(GroupMembershipServiceProviderFactory.class);
	
	static {
		// static initialization
		GroupMembershipServiceProviderConfigManager configMngr = GroupMembershipServiceProviderConfigManager.getInstance();
		try {
			s_defaultProviderKey = configMngr.getConfig().getDefaultProvider();			
		} catch (ConfigurationException e) {
			s_errorData = getConfigError(configMngr);
		}
	}
	
	// disable creating instances
	private GroupMembershipServiceProviderFactory() {
		
	}
	
	public static GroupMembershipProvider create() throws ServiceException {
		return create(s_defaultProviderKey);
	}

	public static GroupMembershipProvider create(String providerKey) throws ServiceException { 
		
		if (s_errorData != null) 
			throw new ServiceException(s_errorData);
		
		if (providerKey == null)
			providerKey = s_defaultProviderKey;
		
		GroupMembershipProvider providerImpl = s_serviceDMProviderMap.get(providerKey);
		GroupMembershipServiceProviderConfigManager configMngr = GroupMembershipServiceProviderConfigManager.getInstance();
		
		if (providerImpl == null) {
			// check the failed set
			if (s_failedProviders.contains(providerKey)) {
				new ServiceException(getConfigError(configMngr));
			}
			synchronized (GroupMembershipServiceProviderFactory.class) {
				providerImpl = s_serviceDMProviderMap.get(providerKey);
				if (providerImpl == null) {
					try {
						String providerImplClassName = configMngr.getConfig().getProviderImplClassName(providerKey);
						if (providerImplClassName != null) {
							providerImpl = getServiceDataModelProviderInstance(providerImplClassName);
							if (providerImpl != null)
								s_serviceDMProviderMap.put(providerKey, providerImpl);
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

	private static GroupMembershipProvider getServiceDataModelProviderInstance(String groupMembershipServiceProviderClassName) {
		
		GroupMembershipProvider serviceDMProviderImpl = null;
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			serviceDMProviderImpl = ReflectionUtils.createInstance(groupMembershipServiceProviderClassName, GroupMembershipProvider.class, cl);

		} catch (Exception e) {
			s_Logger.log(Level.SEVERE, 
					"The GroupMembershipServiceDMProvider class name: " 
						+ groupMembershipServiceProviderClassName + " is invalid",
					e);
			
		}
		return serviceDMProviderImpl;
	}

	private static CommonErrorData getConfigError(
			GroupMembershipServiceProviderConfigManager configMngr) {
		return ErrorUtils.createErrorData(
				ErrorConstants.SVC_GROUPMEMBERSHIPSERVICE_INVALID_PROVIDER_CONFIGURATION, 
				ErrorConstants.ERRORDOMAIN.toString(),
				new Object[] {new String("GroupMembershipService"), 
					configMngr.getConfigPath() + 
					configMngr.getConfigFileName()});
	}

}
