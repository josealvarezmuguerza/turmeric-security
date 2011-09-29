/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.rateLimiterCounterCassandraProviderImpl.dao;

import java.util.Set;

import org.ebayopensource.turmeric.rateLimiterCounterCassandraProviderImpl.model.RateLimiterGenericsPolicyModel;
import org.ebayopensource.turmeric.rateLimiterCounterProvider.Policy.model.RateLimiterPolicyModel;

/*
 *  @author jamuguerza
 */
public interface BaseDao<K> {
		  public void delete(K key);
		  public Set<K> getKeys();
		  public boolean  containsKey(K key);
		  public void save(K key, RateLimiterGenericsPolicyModel<?>  activeEffect);
		  public RateLimiterGenericsPolicyModel<?> find(K key);		  
}
