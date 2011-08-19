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


import org.ebayopensource.turmeric.utils.config.BaseConfigManager;
import org.ebayopensource.turmeric.utils.config.exceptions.ConfigurationException;
import org.w3c.dom.Element;


/**
 * The Class RateLimiterCounterProviderConfigManager.
 * @author jamuguerza
 */
public class RateLimiterCounterProviderConfigManager extends BaseConfigManager { 
	
	private static final String CONFIG_FILENAME = "RateLimiterCounterProviderConfig.xml";
	private static final String SCHEMA_FILENAME = "RateLimiterCounterProviderConfig.xsd";
	private static final String ROOT_ELEMENT = "rate-limiter-counter-provider-config";
    private static RateLimiterCounterProviderConfigManager s_instance = null; 

	private RateLimiterCounterProviderConfig m_config;
	
	

	/**
	 * Gets the single instance of RateLimiterCounterProviderConfigManager.
	 *
	 * @return single instance of RateLimiterCounterProviderConfigManager
	 */
	public static RateLimiterCounterProviderConfigManager getInstance() {
    	if (s_instance == null) {
    		s_instance = new RateLimiterCounterProviderConfigManager();
    	}
    	return s_instance;
    }
	
	
	/**
	 * Gets the config.
	 *
	 * @return the config
	 * @throws ConfigurationException the configuration exception
	 */
	public synchronized RateLimiterCounterProviderConfig getConfig() throws ConfigurationException {
		loadConfig();
		return m_config;
	}
	
	/**
	 * Gets the config for update.
	 *
	 * @return the config for update
	 * @throws ConfigurationException the configuration exception
	 */
	public synchronized RateLimiterCounterProviderConfig getConfigForUpdate() throws ConfigurationException {
		loadConfig();
		if (m_config == null) {
			return null;
		}
		return m_config.copy();
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void map(Element rootData) throws ConfigurationException {
		// if no rootData, reset config to null (clean up previous instance)
		if (rootData == null) {
			m_config = null;
		} else {
			m_config = new RateLimiterCounterProviderConfig();
			RateLimiterCounterProviderConfigMapper.map(getConfigFileName(), rootData, m_config);				
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getConfigFileName() {
		return CONFIG_FILENAME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSchemaFileName() {
		return SCHEMA_FILENAME;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getRootElementName() {
		return ROOT_ELEMENT;
	}
	

}
