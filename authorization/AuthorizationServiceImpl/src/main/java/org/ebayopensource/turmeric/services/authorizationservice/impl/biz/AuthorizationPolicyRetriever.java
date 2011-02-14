/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl.biz;

import java.util.List;
import java.util.Map;

import org.ebayopensource.turmeric.services.authorizationservice.impl.AuthorizationException;


/**
 * This is an interface designed to allow its users to retrieve policies. 
 * @author mpoplacenel
 */
public interface AuthorizationPolicyRetriever {
	
	/**
	 * Retrieve policies corresponding to the specified list of encoded ResOps. 
	 * @param resOpKeys the encoded resOps to retrieve the policies for. 
	 * @return the policies linked to the given ResOps. 
	 * @throws AuthorizationException for problems during the retrieval process.
	 */
	Map<String, List<AuthorizationPolicy>> getAuthorizationPolicies(String... resOpKeys)
	throws AuthorizationException;
	
	/**
	 * Retrieve policies corresponding to the specified list of encoded ResOps,  
	 * by first going through the provided policy cache. If a policy was not already there, 
	 * it will be registered in the cache. 
	 * @param existingPoliciesMap the policy cache. 
	 * @param resOpKeys the encoded resOps to retrieve the policies for. 
	 * @return the policies linked to the given ResOps. 
	 * @throws AuthorizationException for problems during the retrieval process.
	 */
	Map<String, List<AuthorizationPolicy>> getAuthorizationPolicies(
			Map<String, AuthorizationPolicy> existingPoliciesMap, String... resOpKeys)
	throws AuthorizationException;
	
}