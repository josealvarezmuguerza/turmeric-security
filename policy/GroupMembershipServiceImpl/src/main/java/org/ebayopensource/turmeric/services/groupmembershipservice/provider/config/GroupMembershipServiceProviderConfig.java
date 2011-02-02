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
import java.util.Map;

import org.ebayopensource.turmeric.runtime.binding.utils.CollectionUtils;
import org.ebayopensource.turmeric.utils.config.BaseConfigHolder;

/**
 * This class represents the groupmembership service provider configuration
 * 
 * @author rpallikonda
 */
public class GroupMembershipServiceProviderConfig extends BaseConfigHolder {
	String m_default;
	Map<String, String> m_providerMap;

	private static final char NL = '\n';

	/**
	 * @return m_default The default provider name
	 */
	public String getDefaultProvider() {
		return m_default;
	}
	
	/**
	 * @return m_default The default provider name
	 */
	public String getProviderImplClassName(String providerKey) {
		return m_providerMap.get(providerKey);
	}
	
	
	/**
	 * @param className
	 *            the m_default to set
	 */
	public void setDefaultProvider(String providerKey) {
		checkReadOnly();
		m_default = providerKey;
	}
	
	/**
	 * @return the m_providerMap or copy as needed
	 */
	public Map<String, String> getProviderMap() {
		if (isReadOnly()) {
			return copyProviderMap(m_providerMap);
		}
		return m_providerMap;
	}
	
	
	/**
	 * @return set the m_providerMap 
	 */
	public void setProviderMap(Map<String, String> providerMap) {
		checkReadOnly();
		m_providerMap = providerMap;
	}

	public GroupMembershipServiceProviderConfig copy() {
		GroupMembershipServiceProviderConfig result = new GroupMembershipServiceProviderConfig();
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
	
	public void dump(StringBuffer sb) {
		sb.append("========== GroupMembership Service Provider Config =========="+"\n");

		sb.append("Default provider="
				+ m_default + NL);
	}
}
