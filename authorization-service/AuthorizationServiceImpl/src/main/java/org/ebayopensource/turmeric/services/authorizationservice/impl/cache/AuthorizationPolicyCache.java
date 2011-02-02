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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ebayopensource.turmeric.services.authorizationservice.impl.AuthorizationException;
import org.ebayopensource.turmeric.services.authorizationservice.impl.biz.AuthorizationPolicy;
import org.ebayopensource.turmeric.services.authorizationservice.impl.biz.AuthorizationPolicyRetriever;
import org.ebayopensource.turmeric.utils.Timer;
import org.ebayopensource.turmeric.utils.cache.AbstractPokerCache;

import com.ebay.kernel.logger.Logger;

/**
 * Policy cache for the Authorization Service. 
 * 
 * @author mpoplacenel
 */
public class AuthorizationPolicyCache 
extends AbstractPokerCache<String, List<AuthorizationPolicy>> {
	
	/**
	 * Cache name ("AuthZ_Cache").
	 */
	public static final String AUTHZ_POLICY_CACHE_NAME = "AuthZPolicyCache";
	
	private static final Logger LOGGER = Logger.getInstance(AuthorizationPolicyCache.class);
	
	/**
	 * Constructor.
	 *  
	 * @param policyProvider the policy provider to use to retrieve the policies. 
	 * @param refreshInterval refresh interval in milliseconds.
	 * @param startTimeOfDay start time in seconds since midnight. 
	 */
	public AuthorizationPolicyCache(AuthorizationPolicyRetriever policyProvider, long refreshInterval, int startTimeOfDay) {
		super(AUTHZ_POLICY_CACHE_NAME, 
			new AuthorizationPolicyCacheBuilder(policyProvider), refreshInterval, startTimeOfDay);
	}
	
	/**
	 * Exposes the internal authorization policy cache. 
	 * @return the policyName-to-policy cache map. 
	 */
	public Map<String, AuthorizationPolicy> getAuthorizationPolicyCache() {
		return ((AuthorizationPolicyCacheBuilder) this.cacheBuilder).getAuthorizationPolicyCache();
	}
	
    /**
     * Clears all the internal caches. 
     * @return the cleared set of keys. 
     * @see org.ebayopensource.turmeric.utils.cache.AbstractCache#clearAll()
     */
    @Override
	public Set<String> clearAll() {
    	((AuthorizationPolicyCacheBuilder) this.cacheBuilder).clear();
		return super.clearAll();
	}

	/**
	 * Override to disable rebuilding of the keys on poking or scheduled refreshing.
	 * @return <code>true</code>
	 * @see org.ebayopensource.turmeric.utils.cache.AbstractPokerCache#onlyClearOnRebuilding()
	 */
	@Override
	protected boolean onlyClearOnRebuilding() {
		return true;
	}

	/**
	 * Cache builder implementation for the Authorization Service's Policy cache. 
	 * 
	 * @author mpoplacenel
	 */
	private static class AuthorizationPolicyCacheBuilder 
	implements CacheBuilder<String, List<AuthorizationPolicy>> {
		
		private final AuthorizationPolicyRetriever policyProvider;
		
		private final Map<String, AuthorizationPolicy> authorizationPolicyCache = 		
			new HashMap<String, AuthorizationPolicy>();
		
		/**
		 * Constructor.
		 * @param authorizer the authorizer to use for the policy scheme. 
		 */
		public AuthorizationPolicyCacheBuilder(AuthorizationPolicyRetriever policyProvider) {
			this.policyProvider = policyProvider;
		}

		/**
		 * Clears the authorization policy cache. 
		 */
		private void clear() {
			this.authorizationPolicyCache.clear();
		}
		
		/**
		 * Fetch the policies for the given resource and on a per-operation basis, 
		 * identify the authorized subjects.
		 * @param the keys to build the cache entries for. 
		 * @return the map containing the newly-built entries, indexed by their keys.
		 *  
		 * @see org.ebayopensource.turmeric.utils.cache.AbstractCache.CacheBuilder#build(Object...)
		 */
		@Override
		public Map<String, List<AuthorizationPolicy>> build(String... keys) 
		throws CacheBuildingException {
			if (keys == null) {
				throw new NullPointerException("Null keys passed in");
			}
			Timer timer = null;
			if (LOGGER.isDebugEnabled()) {
				timer = new Timer("getAuthorizationPolicies()");
				LOGGER.debug("******** " + timer);
			}
			// step 1: get authorization policies for the resType:resName
			Map<String, List<AuthorizationPolicy>> tmpResOpMap;
			try {
				tmpResOpMap = this.policyProvider.getAuthorizationPolicies(
						this.authorizationPolicyCache, keys);
				if (LOGGER.isDebugEnabled()) {
					if (timer != null) timer.end();
					LOGGER.debug("******** " + timer);
				}
			} catch (AuthorizationException e) {
				throw new CacheBuildingException(e);
			}
			if (tmpResOpMap == null) {
				return null;
			}
			
			Map<String, List<AuthorizationPolicy>> resOpMap = 
				new LinkedHashMap<String, List<AuthorizationPolicy>>(tmpResOpMap.size());
			for (Map.Entry<String, List<AuthorizationPolicy>> resE : tmpResOpMap.entrySet()) {
				resOpMap.put(resE.getKey(), Collections.unmodifiableList(resE.getValue()));
			}
			
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Built: " + resOpMap);
			}
			return Collections.unmodifiableMap(resOpMap);
		}

		/**
		 * Getter for the policy cache. 
		 * @return the policy cache. 
		 */
		public Map<String, AuthorizationPolicy> getAuthorizationPolicyCache() {
			return authorizationPolicyCache;
		}
		
	}
		
}
