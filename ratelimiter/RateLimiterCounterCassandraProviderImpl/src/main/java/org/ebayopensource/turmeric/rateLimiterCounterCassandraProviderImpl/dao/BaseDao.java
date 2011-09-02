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

import org.ebayopensource.turmeric.rateLimiterCounterProvider.Policy.model.RateLimiterPolicyModel;

/*
 *  @author jamuguerza
 */
public interface BaseDao {
		  public void delete(String key);
		  public Set<String> getKeys();
		  public boolean  containsKey(String key);
		  public void save(String key, RateLimiterPolicyModel  activeEffect);
		  public RateLimiterPolicyModel find(String key);		  
}
