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


import org.ebayopensource.turmeric.utils.DomParseUtils;
import org.ebayopensource.turmeric.utils.config.exceptions.ConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



/**
 * The Class RateLimiterCounterProviderConfigMapper.
 */
public class RateLimiterCounterProviderConfigMapper {

	/**
	 * Map RL counter configuration.
	 *
	 * @param filename the filename
	 * @param topLevel the top level
	 * @param dst the dst
	 * @throws ConfigurationException the configuration exception
	 */
	public static void map(String filename, Element topLevel, RateLimiterCounterProviderConfig dst) throws ConfigurationException {
	
		if (topLevel == null) {
			return;
		}
		mapRateLimiterCounterProviderConfig(filename, topLevel, dst);
	}
	
	/**
	 * Map rate limiter counter provider config.
	 *
	 * @param filename the filename
	 * @param rateLimiterCounterConfigProvider the rate limiter counters config provider
	 * @param dst the dst
	 * @throws ConfigurationException the configuration exception
	 */
	public static void mapRateLimiterCounterProviderConfig(String filename, Element rateLimiterCounterConfigProvider, RateLimiterCounterProviderConfig dst) throws ConfigurationException {
		try {
			String defProviderKey = DomParseUtils.getElementText(filename, rateLimiterCounterConfigProvider, "default");
			dst.setDefaultProvider(defProviderKey);
			
			Element providerConfigList = DomParseUtils.getSingleElement(filename, rateLimiterCounterConfigProvider, "provider-config-list");
			if (providerConfigList != null) {
				NodeList nodeList = DomParseUtils.getImmediateChildrenByTagName(providerConfigList, "provider-config");
				dst.setProviderMap(getProviderMap(filename, nodeList));
			}
		} catch(Exception e) {
			throw new ConfigurationException(
					"Error in mapping rateLimiter counter config: " + e.getMessage(),
					e);
		}
	}
	
	private static Map<String, String> getProviderMap(String filename, NodeList nodeList) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element item = (Element)nodeList.item(i);
			String type = DomParseUtils.getElementText(filename, item, "provider-name");
			String className = DomParseUtils.getElementText(filename, item, "provider-impl-classname");
			map.put(type, className);				
		}
		return map;
	}
	
	
}
