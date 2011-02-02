/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.policyservice.provider.config;

import java.util.HashMap;
import java.util.Map;

import org.ebayopensource.turmeric.services.policyservice.provider.config.PolicyServiceProviderConfig.PolicyServiceProvider;
import org.ebayopensource.turmeric.utils.DomParseUtils;
import org.ebayopensource.turmeric.utils.config.exceptions.ConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



public class PolicyServiceProviderConfigMapper {

	/**
	 * Map subject group service configuration
	 * @param filename
	 * @param groupMembershipConfig
	 * @param dst
	 * @throws Exception
	 */
	public static void map(String filename, Element topLevel, PolicyServiceProviderConfig dst) throws ConfigurationException {
	
		if (topLevel == null) {
			return;
		}
		mapPolicyServiceProviderConfig(filename, topLevel, dst);
	}
	
	public static void mapPolicyServiceProviderConfig(String filename, Element policyServiceConfigProvider, PolicyServiceProviderConfig dst) throws ConfigurationException {
		try {
			
			String defProviderKey = DomParseUtils.getElementText(filename, policyServiceConfigProvider, "default");
			dst.setDefaultProvider(defProviderKey);
			
			
			Element providerConfigList = DomParseUtils.getSingleElement(filename, policyServiceConfigProvider, "provider-config-list");
			if (providerConfigList != null) {
				NodeList nodeList = DomParseUtils.getImmediateChildrenByTagName(providerConfigList, "provider-config");
				mapProviders(filename, nodeList, dst);
			}
			
			
		} catch(Exception e) {
			throw new ConfigurationException(
					"Error in mapping ratelimiter service config: " + e.getMessage(),
					e);
		}
	}
	
	private static void mapProviders(String filename, NodeList nodeList, PolicyServiceProviderConfig dst) throws Exception {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element item = (Element)nodeList.item(i);
			String type = DomParseUtils.getElementText(filename, item, "provider-name");
			PolicyServiceProvider provider = mapProvider(filename, item);
			dst.setProvider(type, provider);				
		}
		
	}
	
	private static PolicyServiceProvider mapProvider(String filename, Element policyServiceProvider) throws Exception
	{
		PolicyServiceProvider provider = new PolicyServiceProvider();
		Element policyTypeProviderConfig = DomParseUtils.getSingleElement(filename, policyServiceProvider, "policy-entity-provider-config");
		if (policyTypeProviderConfig != null) {
			NodeList nodeList = DomParseUtils.getImmediateChildrenByTagName(policyTypeProviderConfig, "policy-type-config");
			provider.m_policyType2ProviderClassNameMap.putAll(getType2ProviderMap(filename, nodeList));
		}
		Element subjectTypeProviderConfig = DomParseUtils.getSingleElement(filename, policyServiceProvider, "subject-entity-provider-config");
		if (subjectTypeProviderConfig != null) {
			NodeList nodeList = DomParseUtils.getImmediateChildrenByTagName(subjectTypeProviderConfig, "subject-type-config");
			provider.m_subjectType2ProviderClassNameMap.putAll(getType2ProviderMap(filename, nodeList));
		}
		Element resourceTypeProviderConfig = DomParseUtils.getSingleElement(filename, policyServiceProvider, "resource-entity-provider-config");
		if (resourceTypeProviderConfig != null) {
			NodeList nodeList = DomParseUtils.getImmediateChildrenByTagName(resourceTypeProviderConfig, "resource-type-config");
			provider.m_resourceType2ProviderClassNameMap.putAll(getType2ProviderMap(filename, nodeList));
		}
		
		Element authnProviderConfig = DomParseUtils.getSingleElement(filename, policyServiceProvider, "authn-provider-config");
		if (authnProviderConfig != null) { 
			String authnProviderClassname = DomParseUtils.getElementText(filename, authnProviderConfig, "provider-impl-classname", true);
			provider.m_authnProviderClassName = authnProviderClassname;
		}
		
		return provider;
	}

	private static Map<String, String> getType2ProviderMap(String filename, NodeList nodeList) throws Exception {
		Map<String, String> policyTypeMap = new HashMap<String, String>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element item = (Element)nodeList.item(i);
			String type = DomParseUtils.getElementText(filename, item, "type");
			String className = DomParseUtils.getElementText(filename, item, "provider-impl-classname");
			policyTypeMap.put(type, className);				
		}
		return policyTypeMap;
	}
	
	
}
