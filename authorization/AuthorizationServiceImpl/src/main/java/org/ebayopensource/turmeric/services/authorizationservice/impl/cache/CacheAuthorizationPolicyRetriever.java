/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl.cache;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.ebayopensource.turmeric.services.authorizationservice.impl.AuthorizationException;
import org.ebayopensource.turmeric.services.authorizationservice.impl.biz.AuthorizationPolicy;
import org.ebayopensource.turmeric.services.authorizationservice.impl.biz.AuthorizationPolicyRetriever;
import org.ebayopensource.turmeric.utils.cache.AbstractCache.CacheBuildingException;


/**
 * Cache-based authorization policy provider. 
 * 
 * @author mpoplacenel
 */
public class CacheAuthorizationPolicyRetriever implements AuthorizationPolicyRetriever {
	
	private AuthorizationPolicyCache m_policyCache;
	
	/**
	 * Constructor. 
	 * @param authorizationPolicyRetriever the authorization policy retriever. 
	 * @param refreshInterval the refresh interval in mSecs. 
	 * @param startTimeOfDay the start time of the day in seconds since midnight. 
	 */
	public CacheAuthorizationPolicyRetriever(AuthorizationPolicyRetriever authorizationPolicyRetriever, 
			long refreshInterval, int startTimeOfDay) {
		super();
		
		m_policyCache = new AuthorizationPolicyCache(authorizationPolicyRetriever, 
				refreshInterval, startTimeOfDay);
	}

	@Override
	public Map<String, List<AuthorizationPolicy>> getAuthorizationPolicies(
			String... resOpKeys)
	throws AuthorizationException {
		return getAuthorizationPolicies(m_policyCache.getAuthorizationPolicyCache(), resOpKeys);
	}
	
	@Override
	public Map<String, List<AuthorizationPolicy>> getAuthorizationPolicies(
			Map<String, AuthorizationPolicy> existingPolicies, String... resOpKeys)
	throws AuthorizationException {
		if (resOpKeys == null) return null;
		Map<String, List<AuthorizationPolicy>> ret = 
			new LinkedHashMap<String, List<AuthorizationPolicy>>(resOpKeys.length);
		for (String resOpKey : resOpKeys) { 
			try {
				List<AuthorizationPolicy> cachePolicies = m_policyCache.get(resOpKey);
				ret.put(resOpKey, cachePolicies);
			} catch (CacheBuildingException e) {
				throw CacheUtils.toAuthorizationException(e);
			}
		}
		return ret;
	}

	/**
	 * Exposes the internal cache object. 
	 * @return a reference to the internal policy cache. 
	 */
	public AuthorizationPolicyCache getCache() {
		return m_policyCache;
	}

}