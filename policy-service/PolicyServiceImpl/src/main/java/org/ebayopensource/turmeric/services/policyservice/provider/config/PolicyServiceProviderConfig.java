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

import org.ebayopensource.turmeric.runtime.binding.utils.CollectionUtils;
import org.ebayopensource.turmeric.utils.config.BaseConfigHolder;


/**
 * This class represents the ratelimiter service provider configuration
 * 
 * @author rpallikonda
 */
public class PolicyServiceProviderConfig extends BaseConfigHolder {
	
	String m_default;
	Map<String, PolicyServiceProvider> m_configs = new HashMap<String, PolicyServiceProvider>();
	
	public static class PolicyServiceProvider {
		public Map<String, String> m_policyType2ProviderClassNameMap = new HashMap<String,String>();
		public Map<String, String> m_subjectType2ProviderClassNameMap = new HashMap<String,String>();
		public Map<String, String> m_resourceType2ProviderClassNameMap = new HashMap<String,String>();
		public String m_authnProviderClassName;
	}

	private static final char NL = '\n';

	/**
	 * @return m_default The default provider name
	 */
	public String getDefaultProvider() {
		return m_default;
	}
	
	
	/**
	 * @param className
	 *            the m_default to set
	 */
	public void setDefaultProvider(String providerKey) {
		checkReadOnly();
		m_default = providerKey;
	}
	
	public void setProvider(String name, PolicyServiceProvider provider) {
		checkReadOnly();
		m_configs.put(name, provider);
	}
	
	/**
	 * @return the m_policyType2ProviderClassNameMap or copy as needed
	 */
	public Map<String, String> getPolicyTypeProviders(String providerName) {
		if (providerName == null) // implies default
			providerName = m_default;
		PolicyServiceProvider policyServiceProvider = m_configs.get(providerName);
		if (policyServiceProvider == null) return CollectionUtils.EMPTY_STRING_MAP;
		Map<String, String> policyTypeProviderMap = policyServiceProvider.m_policyType2ProviderClassNameMap;
		
		if (isReadOnly()) {
			return copyProviderMap(policyTypeProviderMap);
		}
		return policyTypeProviderMap;
	}
	
	
	/**
	 * @return the m_subjectType2ProviderClassNameMap or copy as needed
	 */
	public Map<String, String> getSubjectTypeProviders(String providerName) {
		if (providerName == null) // implies default
			providerName = m_default;
		PolicyServiceProvider policyServiceProvider = m_configs.get(providerName);
		if (policyServiceProvider == null) return CollectionUtils.EMPTY_STRING_MAP;
		Map<String, String> subjectTypeProviderMap = policyServiceProvider.m_subjectType2ProviderClassNameMap;
		if (isReadOnly()) {
			return copyProviderMap(subjectTypeProviderMap);
		}
		return subjectTypeProviderMap;
	}
	
	
	/**
	 * @return the m_resourceType2ProviderClassNameMap or copy as needed
	 */
	public Map<String, String> getResourceTypeProviders(String providerName) {
		if (providerName == null) // implies default
			providerName = m_default;
		PolicyServiceProvider policyServiceProvider = m_configs.get(providerName);
		if (policyServiceProvider == null) return CollectionUtils.EMPTY_STRING_MAP;
		Map<String, String> resourceTypeProviderMap = policyServiceProvider.m_resourceType2ProviderClassNameMap;
		if (isReadOnly()) {
			return copyProviderMap(resourceTypeProviderMap);
		}
		return resourceTypeProviderMap;
	}
	
	public String getAuthenticationProvider(String providerName) {
		if (providerName == null) // implies default
			providerName = m_default;
		PolicyServiceProvider policyServiceProvider = m_configs.get(providerName);
		if (policyServiceProvider == null) return null;
		return policyServiceProvider.m_authnProviderClassName;
	}
	

	private Map<String, String> copyProviderMap(
			Map<String, String> inProvidersMap) {
		if (inProvidersMap == null || inProvidersMap.isEmpty())
			return CollectionUtils.EMPTY_STRING_MAP;
		Map<String, String> outProvidersMap = new HashMap<String, String>(inProvidersMap);
		return outProvidersMap;
		
	}
	

	public PolicyServiceProviderConfig copy() {
		PolicyServiceProviderConfig result = new PolicyServiceProviderConfig();
		result.m_default = m_default;
		for (Map.Entry<String, PolicyServiceProvider> providerEntry : m_configs.entrySet()) {
			PolicyServiceProvider provider = new PolicyServiceProvider();
			provider.m_policyType2ProviderClassNameMap = 
				new HashMap<String, String>(providerEntry.getValue().m_policyType2ProviderClassNameMap);
			provider.m_resourceType2ProviderClassNameMap = 
				new HashMap<String, String>(providerEntry.getValue().m_resourceType2ProviderClassNameMap);
			provider.m_subjectType2ProviderClassNameMap = 
				new HashMap<String, String>(providerEntry.getValue().m_subjectType2ProviderClassNameMap);
			result.m_configs.put(providerEntry.getKey(), provider);
		}
		return result;
	}

	public void dump(StringBuffer sb) {
		sb.append("========== Policy Service Provider Config =========="+"\n");
		// TODO: implement the dumping
		
	}
}
