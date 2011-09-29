/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.rateLimiterCounterCassandraProviderImpl.model;

import org.ebayopensource.turmeric.rateLimiterCounterProvider.Policy.model.RateLimiterPolicyModel;


/**
 *    column_metadata =
 *  [
 *  {column_name: 'timestamp', validation_class: UTF8Type},
 *  {column_name: 'active', validation_class:UTF8Type},
 *  {column_name: 'ip', validation_class: UTF8Type},
 *  {column_name: 'effect', validation_class: UTF8Type},
 *  {column_name: 'count', validation_class: LongType},
 *  {column_name: 'effectDuration', validation_class: LongType},
 *  {column_name: 'rolloverPeriod', validation_class: LongType}
 *  ];
 *  
 * The Class RateLimiterPolicyModel.
 * @author jamuguerza
 */
public class RateLimiterGenericsPolicyModel<K> extends RateLimiterPolicyModel {
	
	public RateLimiterGenericsPolicyModel(K keyType){
		
	}
}
