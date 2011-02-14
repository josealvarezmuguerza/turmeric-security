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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorConstants;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeResponseType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.services.authorizationservice.impl.AuthorizationException;
import org.ebayopensource.turmeric.services.authorizationservice.impl.cache.AuthorizationServiceCacheToggleBean;
import org.ebayopensource.turmeric.services.authorizationservice.impl.cache.CacheAuthorizationPolicyRetriever;
import org.ebayopensource.turmeric.services.authorizationservice.impl.util.EncodingUtils;
import org.ebayopensource.turmeric.services.authorizationservice.impl.util.cache.PokerCacheConfigBean;
import org.ebayopensource.turmeric.utils.cache.AbstractPokerCache;

import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;

import java.util.Collections;

/**
 * This class contains the business logic of the Authorization Service Impl. 
 * 
 * @author dmuthiayen (orig SecurityFramework.AuthorizationServiceImpl this is derived from), mpoplacenel
 */
/* package */ class Authorizer {
	
	/**
	 * Cache name. 
	 */
	public static final String AUTHZ_CACHE_NAME = "AuthorizationCache";

	private static final Logger LOGGER = Logger.getInstance(Authorizer.class);
	
	/**
	 * The group membership resolver. 
	 */
	protected final GroupMembershipResolver m_groupMembershipResolver;
	
	/**
	 * The cache-powered authorization policy retriever. 
	 */
	protected final CacheAuthorizationPolicyRetriever m_cacheAuthorizationPolicyRetriever;
	
	/**
	 * The direct (non-cached) authorization policy retriever.
	 */
	protected final ServiceAuthorizationPolicyRetriever m_authorizationPolicyRetriever;
	
	/**
	 * Atomic boolean keeping track of the cache setting (ON or OFF). 
	 */
	protected final AtomicBoolean m_cacheOn;
	
	/**
	 * The Configuration Management bean for poking the cache. 
	 */
	protected final PokerCacheConfigBean m_bean;
	
	/**
	 * The Configuration Management bean for toggling the cache ON or OFF. 
	 */
	protected final AuthorizationServiceCacheToggleBean m_cacheToggleBean;

	/**
	 * Constructor.
	 * @param cacheOn flag specifying if the cache is ON or OFF.
	 * @param refreshInterval the refresh interval in mSecs. 
	 * @param startTimeOfDay the start time of the day in seconds since midnight. 
	 */
	public Authorizer(boolean cacheOn, long refreshInterval, int startTimeOfDay) {
		m_groupMembershipResolver = new ServiceGroupMembershipResolver();
		m_cacheOn = new AtomicBoolean(cacheOn);
		m_authorizationPolicyRetriever =  new ServiceAuthorizationPolicyRetriever();
		m_cacheAuthorizationPolicyRetriever = 
			new CacheAuthorizationPolicyRetriever(m_authorizationPolicyRetriever, refreshInterval, startTimeOfDay);
		
		m_cacheToggleBean = new AuthorizationServiceCacheToggleBean();
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Registering Authorization Cache bean");
		}
		// instantiate the config bean; it'll register to the bag from within the constructor
		m_bean = new PokerCacheConfigBean(
				AUTHZ_CACHE_NAME, 
				m_cacheAuthorizationPolicyRetriever.getCache());
	}
	
	/**
	 * "Prime" the cache. 
	 * @param serviceName the service name to initialize for. 
	 * @throws AuthorizationException if problems occurred. 
	 */
	public void initialize(String serviceName) throws AuthorizationException {
		// initialize the cache
		if (!m_cacheOn.get()) return;
		String resKey = EncodingUtils.encodeResOpKey("SERVICE", serviceName, null);
		m_cacheAuthorizationPolicyRetriever.getAuthorizationPolicies(resKey);
	}
	
	/**
	 * Authorize a request. 
	 * @param authzRequest the request to authorize. 
	 * @return the response (indicating success or failure + error message).
	 * @throws AuthorizationException if problems were encountered. 
	 */
	public AuthorizeResponseType authorize(AuthorizeRequestType authzRequest) throws AuthorizationException {
        final String resType = authzRequest.getResourceType();
        final String resName = authzRequest.getResourceName();

		// get the policy from the cache
		final String resOpKey = EncodingUtils.encodeResOpKey(
				resType, resName, authzRequest.getOperationName());
		Map<String, List<AuthorizationPolicy>> authzPolicyUniMap = 
			(m_cacheOn.get() ? m_cacheAuthorizationPolicyRetriever : m_authorizationPolicyRetriever)
				.getAuthorizationPolicies(resOpKey);
        List<AuthorizationPolicy> authzPolicyList = null;
        if (authzPolicyUniMap != null && authzPolicyUniMap.size() > 0) {
        	assert authzPolicyUniMap.containsKey(resOpKey) && authzPolicyUniMap.size() == 1 
        		: "Ain't containing " + resOpKey + ", but it's " + authzPolicyUniMap;
        	authzPolicyList = authzPolicyUniMap.get(resOpKey);
        }
		// if no policy for this resource, we're in it deeply
        if (authzPolicyList == null) {
			throw new AuthorizationException(
					ErrorConstants.SVC_SECURITY_SYS_AUTHZ_POLICY_NOT_FOUND,
						"No policy for resource/operation " + resType
						+ "." + resName + "." + authzRequest.getOperationName());
		}
		for (AuthorizationPolicy authzPolicy : authzPolicyList) {
			List<String> authorizedSubjectKeys = authzPolicy.getSubjects();
			for (String policySubjectKey : authorizedSubjectKeys) {
				CloneableSubjectType policySubject = EncodingUtils.decodeSubjectKey(policySubjectKey);
				for (SubjectType subject : authzRequest.getSubject()) { 
					CloneableSubjectType smartSubject = new CloneableSubjectType(subject);
					if (policySubject.match(smartSubject)) { 
						AuthorizeResponseType authzResp = new AuthorizeResponseType();
						authzResp.setAck(AckValue.SUCCESS);
						if (LOGGER.isInfoEnabled()) {
							LOGGER.log(LogLevel.INFO, "Policy " + authzPolicy.getPolicyName()
									+ " directly authorized subject " + subject);
						}
						return authzResp;
					}
				}
			}
			// check if any resolved subject group is authorized for this ResOp
			final List<String> subjectGroupKeys = authzPolicy.getSubjectGroups();
			if (subjectGroupKeys != null && authzRequest.getResolvedSubjectGroup() != null) {
				// encode them to do a set retainsAll() against the policy subjects
				Set<String> resolvedSubjectGroupKeys = 
					new HashSet<String>(authzRequest.getResolvedSubjectGroup().size());
				for (SubjectGroupType rawResolvedSubjectGroup : authzRequest.getResolvedSubjectGroup()) {
						CloneableSubjectGroupType resolvedSubjectGroup = new CloneableSubjectGroupType(rawResolvedSubjectGroup);
						String resolvedSubjectGroupKey = EncodingUtils.encodeSubjectGroupKey(
								resolvedSubjectGroup.getDomain(), resolvedSubjectGroup.getName(), 
								resolvedSubjectGroup.getCalculator());
						resolvedSubjectGroupKeys.add(resolvedSubjectGroupKey);
				}
				resolvedSubjectGroupKeys.retainAll(subjectGroupKeys);
				if (resolvedSubjectGroupKeys.size() > 0) {
						AuthorizeResponseType authzResp = new AuthorizeResponseType();
						authzResp.setAck(AckValue.SUCCESS);
						if (LOGGER.isInfoEnabled()) {
							LOGGER.log(LogLevel.INFO, "Policy " + authzPolicy.getPolicyName()
									+ " contained resolved subject group(s) " + resolvedSubjectGroupKeys);
						}
						return authzResp;
				}
			}
		
			// now check if any of the request's subjects is member of any of the subject groups 
			// in cache, and collect all such groups into the resolved groups
			Set<SubjectGroupType> resolvedSubjectGroups = subjectGroupKeys.size() > 0 
					? getAuthzSubjectGroups(authzRequest.getSubject(), subjectGroupKeys)
					: Collections.<SubjectGroupType>emptySet();
			if (resolvedSubjectGroups.size() > 0) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.log(LogLevel.INFO, "Policy " + authzPolicy.getPolicyName()
							+ " resolved subject groups " + resolvedSubjectGroups);
				}
				AuthorizeResponseType authzResp = new AuthorizeResponseType();
				authzResp = new AuthorizeResponseType();
				authzResp.setAck(AckValue.SUCCESS);
				authzResp.getResolvedSubjectGroup().addAll(resolvedSubjectGroups);
				return authzResp;
			}
		}
		AuthorizeResponseType authzResp = new AuthorizeResponseType();
		authzResp.setAck(AckValue.FAILURE);
		authzResp.setErrorMessage(
				AuthorizationServiceImplUtils.createUnauthorizedErrorMessage(authzRequest, authzPolicyList));
		
		return authzResp;
	}

	/**
	 * Make a call to group membership service to lookup if any subject in the
	 * request is a member of any of the authorized subject groups for this
	 * policy. 
	 * 
	 * @param reqSubjects the subjects specified in the request
	 * @param policySubjectGroupKeys the subject groups specified in the 
	 * authorization policy, encoded as String keys.
	 *  
	 * @return the set of authorized subject groups (if any).
	 * 
	 * @throws AuthorizationException if problems occurred. 
	 */
	protected Set<SubjectGroupType> getAuthzSubjectGroups(
			List<SubjectType> reqSubjects, List<String> policySubjectGroupKeys)
	throws AuthorizationException {
		final Set<CloneableSubjectType> smartReqSubjects = new LinkedHashSet<CloneableSubjectType>();
		for (SubjectType reqSubj : reqSubjects) {
			smartReqSubjects.add(new CloneableSubjectType(reqSubj));
		}
		
		Set<SubjectGroupType> retSGs = new LinkedHashSet<SubjectGroupType>();
		Set<CloneableSubjectGroupType> calcSmartSGs = new LinkedHashSet<CloneableSubjectGroupType>();
		Set<CloneableSubjectGroupType> nonCalcSmartSGs = new LinkedHashSet<CloneableSubjectGroupType>();
		// identify non-calc'ed subject groups who have members intersecting the request subjects
		for (String policySubjectGroupKey : policySubjectGroupKeys) {
			CloneableSubjectGroupType policySubjectGroup = EncodingUtils.decodeSubjectGroupKey(policySubjectGroupKey);
			if (policySubjectGroup.isCalculated()) {
				calcSmartSGs.add(policySubjectGroup);
			} else {
				nonCalcSmartSGs.add(policySubjectGroup);
			}
		}
		if (nonCalcSmartSGs.size() > 0) { 
			Map<CloneableSubjectGroupType, Set<CloneableSubjectType>> membersMap = getGroupMembershipResolver().getMembers(nonCalcSmartSGs);
			for (CloneableSubjectGroupType policySubjectGroup : membersMap.keySet()) {
				Set<CloneableSubjectType> subjects = membersMap.get(policySubjectGroup);
				if (subjects != null) {
					subjects = new LinkedHashSet<CloneableSubjectType>(subjects);
					subjects.retainAll(smartReqSubjects);
					if (subjects.size() > 0) { // smth was in the req subj keys
						retSGs.add(policySubjectGroup.toSubjectGroupType());
					}
				}
			}
		}
		
		// for each request subject, try to see if any of the calculated groups considers it a member
		if (calcSmartSGs.size() > 0) {
			for (SubjectType reqSubj : reqSubjects) {
				CloneableSubjectType smartReqSubj = new CloneableSubjectType(reqSubj);
				Set<CloneableSubjectGroupType> memberOfList = 
					getGroupMembershipResolver().isMemberOf(smartReqSubj, calcSmartSGs);
				retSGs.addAll(memberOfList);
			}
		}
		
		return retSGs;

	}
	
	protected GroupMembershipResolver getGroupMembershipResolver() {
		return m_groupMembershipResolver;
	}

	/**
	 * Provides the list of names for the cache policies. 
	 * @return the list of names for the cache policies.
	 */
	public Set<String> getCachedPolicyResources() {
		return m_cacheAuthorizationPolicyRetriever.getCache().getKeys();
	}
	
	/**
	 * Provides the hit count for the policy identified by the given key. 
	 * @param resOpKey the key identifying the policy. 
	 * @return the hit count. 
	 */
	public long getPolicyStat(String resOpKey) {
		return m_cacheAuthorizationPolicyRetriever.getCache().getStat(resOpKey);
	}

	/**
	 * Clears all caches. 
	 */
	public void clearAll() {
		m_cacheAuthorizationPolicyRetriever.getCache().clearAll();
	}

	/**
	 * Poke the internal caches.
	 * 
	 * @return <code>true</code> if both pokes returned <code>true</code>,
	 * <code>false</code> otherwise. 
	 * 
	 * @see AbstractPokerCache#poke()
	 */
	public boolean poke() {
		return m_cacheAuthorizationPolicyRetriever.getCache().poke();
	}
	
	/**
	 * Sets the cache ON if it was OFF, only logs otherwise. 
	 * @param b the new value for the cache. 
	 * @return <code>!b</code> if it modified it, <code>b</code> otherwise. 
	 */
	public boolean setCacheOn(boolean b) {
		if (m_cacheOn.compareAndSet(!b, b)) { // toggled it
			if (LOGGER.isLogEnabled(LogLevel.INFO)) {
				LOGGER.log(LogLevel.INFO, "Turning the AuthZ Cache "
						+ (b ? "ON" : "OFF"));
			}
			// at this point, no new request/thread will access the cache
			// anymore
			if (!b) { // turning OFF - clear it
				clearAll();
			}
			return !b;
		} else {
			if (LOGGER.isLogEnabled(LogLevel.FINE)) {
				LOGGER.log(LogLevel.FINE, "No-op for same-value cache flag: "
						+ m_cacheOn + " = " + b);
			}
			return b;
		}
	}

	/**
	 * Provides the current state of the cache flag. 
	 * @return <code>true</code> if the cache is ON, <code>false</code> if OFF.
	 */
	public boolean isCacheOn() {
		return m_cacheOn.get();
	}
	
}