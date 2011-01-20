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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.GetGroupMembersRequestType;
import org.ebayopensource.turmeric.security.v1.services.GetGroupMembersResponseType;
import org.ebayopensource.turmeric.security.v1.services.GroupMembersType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKeyType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.services.authorizationservice.impl.biz.CloneableSubjectGroupType;
import org.ebayopensource.turmeric.services.authorizationservice.impl.util.EncodingUtils;
import org.ebayopensource.turmeric.utils.cache.AbstractCache;
import org.ebayopensource.turmeric.utils.cache.AbstractPokerCache;

import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;
import org.ebayopensource.turmeric.services.groupmembershipservice.intf.gen.BaseGroupMembershipServiceConsumer;

/**
 * Cache of expanded subject groups (i.e., all "owned" subjects are populated inside).
 * 
 * @author mpoplacenel
 */
public class SubjectGroupMembershipCache extends AbstractPokerCache<String, Set<String>> {

	/**
	 * Name of this cache.
	 * @see AbstractCache#AbstractCache(String, org.ebayopensource.turmeric.utils.cache.AbstractCache.CacheBuilder) 
	 */
	public static final String SUBJECT_GROUP_CACHE_NAME = "GroupMembershipSubjectCache";
	
	private static final Logger LOGGER = Logger.getInstance(SubjectGroupMembershipCache.class);
	
	/**
	 * Constructor.
	 * @param refreshInterval the refresh interval in mSecs. 
	 * @param startTimeOfDay the start time of the day in seconds since midnight. 
	 */
	public SubjectGroupMembershipCache(long refreshInterval, int startTimeOfDay) {
		super(SUBJECT_GROUP_CACHE_NAME, 
			new SubjectGroupCacheBuilder(), refreshInterval, startTimeOfDay);
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

	private static class SubjectGroupCacheBuilder implements CacheBuilder<String, Set<String>> {
		
		public SubjectGroupCacheBuilder() {
			// nothing in here
		}

		@Override
		public Map<String, Set<String>> build(String... keys)
		throws CacheBuildingException {
			if (keys == null) {
				throw new NullPointerException("Null subject groups array given");
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.log(LogLevel.INFO, "Building group membership for keys: " + Arrays.asList(keys));
			}
			Map<String, Set<String>> calcSubjGrpMap = new LinkedHashMap<String, Set<String>>();
			List<SubjectGroupKeyType> subjectGroupKeys = new ArrayList<SubjectGroupKeyType>(keys.length);
			for (String key : keys) {
				CloneableSubjectGroupType subjectGroup = EncodingUtils.decodeSubjectGroupKey(key);
				if (subjectGroup.isCalculated()) { 
					// no key generation for calculated subject groups
					calcSubjGrpMap.put(key, Collections.<String>emptySet());
					continue;
				}
				SubjectGroupKeyType subjGrpKey = new SubjectGroupKeyType();
				subjGrpKey.setName(subjectGroup.getName());
				subjectGroupKeys.add(subjGrpKey);
			}
			Map<String, Set<String>> tmpResultMap = new LinkedHashMap<String, Set<String>>();
			if (subjectGroupKeys.size() > 0) {
				GetGroupMembersRequestType req = new GetGroupMembersRequestType();
				req.getSubjectgroupKey().addAll(subjectGroupKeys);
				BaseGroupMembershipServiceConsumer groupMembershipServiceConsumer = 
					new BaseGroupMembershipServiceConsumer();
				GetGroupMembersResponseType resp = groupMembershipServiceConsumer.getGroupMembers(req);
				if (!AckValue.SUCCESS.equals(resp.getAck())) {
					throw new WSCacheBuildingException(resp.getAck(), resp.getErrorMessage());
				}
				if (resp != null && resp.getSubjectgroupMembers() != null) {
					for (GroupMembersType groupMembersType : resp.getSubjectgroupMembers()) {
						SubjectGroupType subjGrp = groupMembersType.getSubjectGroup();
						List<SubjectType> subjects = groupMembersType.getMemberSubjects();
						List<String> subjectKeys = new ArrayList<String>(subjects.size());
						for (SubjectType subject : subjects) {
							subjectKeys.add(EncodingUtils.encodeSubjectKey(subject.getDomain(), 
									subject.getValue()));
						}
						tmpResultMap.put(EncodingUtils.encodeSubjectGroupKey(subjGrp.getDomain(), 
								subjGrp.getName(), 
								subjGrp.getCalculator()), 
								Collections.<String>unmodifiableSet(
										new LinkedHashSet<String>(subjectKeys)));
					}
				}
			}
			// now add the calculated ones, with empty subjects
			tmpResultMap.putAll(calcSubjGrpMap);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.log(LogLevel.INFO, "Built group membership for keys: " + Arrays.asList(keys));
			}
			
			return Collections.<String, Set<String>>unmodifiableMap(tmpResultMap);
		}

	}

}