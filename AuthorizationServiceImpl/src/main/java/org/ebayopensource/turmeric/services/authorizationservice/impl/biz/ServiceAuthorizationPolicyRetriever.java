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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorConstants;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesResponse;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.OperationKey;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.PolicyOutputSelector;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.services.authorizationservice.impl.AuthorizationException;
import org.ebayopensource.turmeric.services.authorizationservice.impl.util.EncodingUtils;
import org.ebayopensource.turmeric.services.authorizationservice.impl.util.ResOpKey;
import org.ebayopensource.turmeric.utils.Timer;

import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;
import org.ebayopensource.turmeric.services.policyservice.intf.gen.BasePolicyServiceConsumer;

/**
 * Employ PolicyService to retrieve the requested authorization policies.
 * 
 * @author mpoplacenel
 */
public class ServiceAuthorizationPolicyRetriever implements AuthorizationPolicyRetriever {
	
	private static final Logger LOGGER = Logger.getInstance(ServiceAuthorizationPolicyRetriever.class);
	
	/**
	 * Constructor.
	 */
	public ServiceAuthorizationPolicyRetriever() {
		// nothing in here
	}
	
	@Override
	public Map<String, List<AuthorizationPolicy>> getAuthorizationPolicies(String... keys) 
	throws AuthorizationException {
		return getAuthorizationPolicies(null, keys);
	}
	

	@Override
	public Map<String, List<AuthorizationPolicy>> getAuthorizationPolicies(
			Map<String, AuthorizationPolicy> existingPolicies, String... keys) 
	throws AuthorizationException {
		if (keys == null) return null;
		if (keys.length == 0) { 
			return Collections.emptyMap();
		}
		if (existingPolicies == null) {
			existingPolicies = new HashMap<String, AuthorizationPolicy>();
		}
		FindPoliciesRequest policyRequest = new FindPoliciesRequest();
		policyRequest.getResourceKey().addAll(resOpKeysToResourceList(keys));
		final Set<ResOpKey> resOpKeys = decodeResOpKeys(keys);
		Set<ResOpKey> crossOpResKeys = getCrossOpResources(resOpKeys);
		policyRequest.getOperationKey().addAll(resOpKeysToOperationList(resOpKeys));
		PolicyKey policyKey = new PolicyKey();
		policyKey.setPolicyType("AUTHZ");
		policyRequest.getPolicyKey().add(policyKey);
		final PolicyOutputSelector outputSelector = 
			(keys.length == 1 && crossOpResKeys.isEmpty())  
				? PolicyOutputSelector.SUBJECTS // single exact op
				: PolicyOutputSelector.ALL; // multi-ops or cross-op resource(s)
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Using output selector " + outputSelector + " for " + keys.length + " keys");
		}
		policyRequest.setOutputSelector(outputSelector);
		FindPoliciesResponse policyResponse = null;
		BasePolicyServiceConsumer policyServiceConsumer = new BasePolicyServiceConsumer();
		Timer timer = null;
		if (LOGGER.isDebugEnabled()) {
			timer = new Timer("findPolicies()");
			LOGGER.debug("******** " + timer);
		}
		policyResponse = policyServiceConsumer.findPolicies(policyRequest);
		if (LOGGER.isDebugEnabled()) {
			if (timer != null) timer.end();
			LOGGER.debug("******** " + timer);
		}
		if (policyResponse == null) {
			LOGGER.log(LogLevel.ERROR, "PolicyResponse is NULL!");
			return null;
		}
		if (!AckValue.SUCCESS.equals(policyResponse.getAck())) {
			throw new AuthorizationException(
					ErrorConstants.SVC_SECURITY_APP_AUTHZ_INTERNAL_ERROR, 
					EncodingUtils.encodeErrorMessage(policyResponse.getAck(), policyResponse.getErrorMessage()));
		}
		
		List<Policy> policyList = policyResponse.getPolicySet().getPolicy();

		Map<String, List<AuthorizationPolicy>> map = 
			new HashMap<String, List<AuthorizationPolicy>>(keys.length);
		Set<ResOpKey> opKeys = new LinkedHashSet<ResOpKey>(resOpKeys);
		opKeys.removeAll(crossOpResKeys);
		for (ResOpKey opKey : opKeys) {
			map.put(EncodingUtils.encodeResOpKey(opKey), new ArrayList<AuthorizationPolicy>());
		}		
		if (policyList != null) {
			for (Policy policy : policyList) {
				final String policyName = policy.getPolicyName();
				AuthorizationPolicy authzPolicy = existingPolicies.get(policyName);
				if (authzPolicy == null) { // create a new policy object
					authzPolicy = new AuthorizationPolicy();
					existingPolicies.put(policyName, authzPolicy);
					authzPolicy.setPolicyName(policyName);
					for (SubjectGroup subjectGroup : policy.getTarget().getSubjects().getSubjectGroup()) {
						authzPolicy.getSubjectGroups().add(EncodingUtils.encodeSubjectGroupKey(
								subjectGroup.getSubjectType(), 
								subjectGroup.getSubjectGroupName(), 
								subjectGroup.getSubjectGroupCalculator()));
					}
					for (Subject subject : policy.getTarget().getSubjects().getSubject()) {
						authzPolicy.getSubjects().add(EncodingUtils.encodeSubjectKey(subject.getSubjectType(), 
								subject.getSubjectName()));
					}
				}

				if (keys.length == 1 && crossOpResKeys.isEmpty()) { // single, exact operation case
					ResOpKey resOpKey = EncodingUtils.decodeResOpKey(keys[0]);
					addPolicyToMap(map, authzPolicy, resOpKey);
				} else { // either multi-ops or cross-op resources
					List<Resource> resources = policy.getTarget().getResources().getResource();
					for (Resource resource : resources) {
						List<Operation> operationList = resource.getOperation();
						final String resourceType = resource.getResourceType();
						final String resourceName = resource.getResourceName();
						if (operationList != null) {
							for (Operation operation : operationList) {
								final String operationName = operation.getOperationName();
								ResOpKey resOpKey = new ResOpKey(resourceType, resourceName, operationName);
								if (resOpKeys.contains(resOpKey)) {
									addPolicyToMap(map, authzPolicy, resOpKey);
								} else if (crossOpResKeys.contains(new ResOpKey(resourceType, resourceName, null))) {
									addPolicyToMap(map, authzPolicy, resOpKey);
								}
							}
						} else {
							addPolicyToMap(map, authzPolicy, new ResOpKey(resourceType, resourceName, null));
						}
					}
				}
			}
		}
		
		return map;
	}

	private Set<ResOpKey> getCrossOpResources(Set<ResOpKey> resOpKeys) {
		Set<ResOpKey> retSet = new LinkedHashSet<ResOpKey>();
		for (ResOpKey resOpKey : resOpKeys) {
			if (resOpKey.getOperationName() == null) {
				retSet.add(resOpKey);
			}
		}
		return retSet;
	}

	private void addPolicyToMap(
			Map<String, List<AuthorizationPolicy>> map,
			AuthorizationPolicy authzPolicy, final ResOpKey resOpKeyObj) {
		String resOpKey = EncodingUtils.encodeResOpKey(resOpKeyObj);
		List<AuthorizationPolicy> authzPolicyList = map.get(resOpKey);
		if (authzPolicyList == null) {
			authzPolicyList = new ArrayList<AuthorizationPolicy>();
			map.put(resOpKey, authzPolicyList);
		}
		authzPolicyList.add(authzPolicy);
	}
	
	private Set<ResOpKey> decodeResOpKeys(String[] keys) {
		if (keys == null) return null;
		Set<ResOpKey> resOpKeySet = new LinkedHashSet<ResOpKey>(keys.length);
		for (String key : keys) {
			ResOpKey resOpKey = EncodingUtils.decodeResOpKey(key);
			resOpKeySet.add(resOpKey);
		}
		
		return Collections.<ResOpKey>unmodifiableSet(resOpKeySet);
	}

	private List<OperationKey> resOpKeysToOperationList(Set<ResOpKey> resOpKeySet) {
		if (resOpKeySet == null) return null;
		List<OperationKey> operationList = new ArrayList<OperationKey>();
		for (ResOpKey resOpKey : resOpKeySet) {
			final String operationName = resOpKey.getOperationName();
			if (resOpKey.getOperationName() != null) {
				final OperationKey operationKey = new OperationKey();
				operationKey.setResourceType(resOpKey.getResourceType());
				operationKey.setResourceName(resOpKey.getResourceName());
				operationKey.setOperationName(operationName);
				operationList.add(operationKey);
			}
		}
		return operationList;
	}

	private List<ResourceKey> resOpKeysToResourceList(String[] resOpKeys) {
		if (resOpKeys == null) return null;
		List<ResourceKey> resourceList = new ArrayList<ResourceKey>(resOpKeys.length);
		for (String key : resOpKeys) {
			ResOpKey resOpKey = EncodingUtils.decodeResOpKey(key);
			ResourceKey resourceKey = new ResourceKey();
			resourceKey.setResourceType(resOpKey.getResourceType());
			resourceKey.setResourceName(resOpKey.getResourceName());
			resourceList.add(resourceKey);
		}
		
		return resourceList;
	}

}