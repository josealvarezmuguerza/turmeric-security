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
import java.util.Map;

import org.ebayopensource.turmeric.runtime.binding.utils.CollectionUtils;
import org.ebayopensource.turmeric.utils.config.BaseConfigHolder;

/**
 * This class represents the ratelimiter counter provider configuration.
 *
 * @author jamuguerza
 */
public class RateLimiterCounterProviderConfig extends BaseConfigHolder {
	
	/** The m_default. */
	String m_default;
	
	/** The m_provider map. */
	Map<String, String> m_providerMap;

	private static final char NL = '\n';

	/**
	 * Gets the default provider.
	 *
	 * @return m_default The default provider name
	 */
	public String getDefaultProvider() {
		return m_default;
	}
	
	/**
	 * Gets the provider impl class name.
	 *
	 * @param providerKey the provider key
	 * @return m_default The default provider name
	 */
	public String getProviderImplClassName(String providerKey) {
		return m_providerMap.get(providerKey);
	}
	
	
	/**
	 * Sets the default provider.
	 *
	 * @param providerKey the new default provider
	 */
	public void setDefaultProvider(String providerKey) {
		checkReadOnly();
		m_default = providerKey;
	}
	
	/**
	 * Gets the provider map.
	 *
	 * @return the m_providerMap or copy as needed
	 */
	public Map<String, String> getProviderMap() {
		if (isReadOnly()) {
			return copyProviderMap(m_providerMap);
		}
		return m_providerMap;
	}
	
	
	/**
	 * Sets the provider map.
	 *
	 * @param providerMap the provider map
	 */
	public void setProviderMap(Map<String, String> providerMap) {
		checkReadOnly();
		m_providerMap = providerMap;
	}

	/**
	 * Copy.
	 *
	 * @return the rate limiter counter provider config
	 */
	public RateLimiterCounterProviderConfig copy() {
		RateLimiterCounterProviderConfig result = new RateLimiterCounterProviderConfig();
		result.m_default = m_default;
		result.m_providerMap = new HashMap<String, String>(m_providerMap); 
		return result;
	}

	private Map<String, String> copyProviderMap(
			Map<String, String> inProvidersMap) {
		if (inProvidersMap == null || inProvidersMap.isEmpty())
			return CollectionUtils.EMPTY_STRING_MAP;
		Map<String, String> outProvidersMap = new HashMap<String, String>(inProvidersMap);
		return outProvidersMap;
		
	}
	
	/**
	 * Dump.
	 *
	 * @param sb the sb
	 */
	public void dump(StringBuffer sb) {
		sb.append("========== RateLimiter Counter Provider Config =========="+"\n");

		sb.append("Default provider="
				+ m_default + NL);
	}
}
