/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.ratelimiterservice.provider.config;

import java.util.HashMap;
import java.util.Map;

import org.ebayopensource.turmeric.utils.DomParseUtils;
import org.ebayopensource.turmeric.utils.config.exceptions.ConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



public class RateLimiterServiceProviderConfigMapper {

	/**
	 * Map subject group service configuration
	 * @param filename
	 * @param rateLimiterConfig
	 * @param dst
	 * @throws Exception
	 */
	public static void map(String filename, Element topLevel, RateLimiterServiceProviderConfig dst) throws ConfigurationException {
	
		if (topLevel == null) {
			return;
		}
		mapRateLimiterServiceProviderConfig(filename, topLevel, dst);
	}
	
	public static void mapRateLimiterServiceProviderConfig(String filename, Element rateLimiterServiceConfigProvider, RateLimiterServiceProviderConfig dst) throws ConfigurationException {
		try {
			String defProviderKey = DomParseUtils.getElementText(filename, rateLimiterServiceConfigProvider, "default");
			dst.setDefaultProvider(defProviderKey);
			
			Element providerConfigList = DomParseUtils.getSingleElement(filename, rateLimiterServiceConfigProvider, "provider-config-list");
			if (providerConfigList != null) {
				NodeList nodeList = DomParseUtils.getImmediateChildrenByTagName(providerConfigList, "provider-config");
				dst.setProviderMap(getProviderMap(filename, nodeList));
			}
		} catch(Exception e) {
			throw new ConfigurationException(
					"Error in mapping rateLimiter service config: " + e.getMessage(),
					e);
		}
	}
	
	private static Map<String, String> getProviderMap(String filename, NodeList nodeList) throws Exception {
		Map<String, String> policyTypeMap = new HashMap<String, String>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element item = (Element)nodeList.item(i);
			String type = DomParseUtils.getElementText(filename, item, "provider-name");
			String className = DomParseUtils.getElementText(filename, item, "provider-impl-classname");
			policyTypeMap.put(type, className);				
		}
		return policyTypeMap;
	}
	
	
}
