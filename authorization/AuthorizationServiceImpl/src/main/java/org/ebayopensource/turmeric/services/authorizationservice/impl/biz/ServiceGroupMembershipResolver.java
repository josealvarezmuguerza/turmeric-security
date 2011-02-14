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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorConstants;
import org.ebayopensource.turmeric.security.v1.services.GetGroupMembersRequestType;
import org.ebayopensource.turmeric.security.v1.services.GetGroupMembersResponseType;
import org.ebayopensource.turmeric.security.v1.services.GroupMembersType;
import org.ebayopensource.turmeric.security.v1.services.IsMemberOfRequestType;
import org.ebayopensource.turmeric.security.v1.services.IsMemberOfResponseType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKeyType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.services.authorizationservice.impl.AuthorizationException;
import org.ebayopensource.turmeric.services.authorizationservice.impl.util.EncodingUtils;

import org.ebayopensource.turmeric.services.groupmembershipservice.intf.gen.BaseGroupMembershipServiceConsumer;

/**
 * Employ PolicyService to retrieve the requested authorization policies.
 * 
 * @author mpoplacenel
 */
public class ServiceGroupMembershipResolver implements GroupMembershipResolver {
	
	/**
	 * Constructor. 
	 */
	public ServiceGroupMembershipResolver() {
		// nothing in here
	}

	@Override
	public Map<CloneableSubjectGroupType, Set<CloneableSubjectType>> getMembers(Collection<CloneableSubjectGroupType> subjectGroups) 
	throws AuthorizationException {
		GetGroupMembersRequestType req = new GetGroupMembersRequestType();
		for (SubjectGroupType subjectGroupType : subjectGroups) {
			req.getSubjectgroupKey().add(mapSubjectGroup(subjectGroupType));
		}
		BaseGroupMembershipServiceConsumer groupMembershipServiceConsumer = new BaseGroupMembershipServiceConsumer();		
		GetGroupMembersResponseType resp = groupMembershipServiceConsumer.getGroupMembers(req);
		
		if (!AckValue.SUCCESS.equals(resp.getAck())) {
			throw new AuthorizationException(ErrorConstants.SVC_SECURITY_APP_AUTHZ_INTERNAL_ERROR, 
					EncodingUtils.encodeErrorMessage(resp.getAck(), resp.getErrorMessage()));
		}
		List<GroupMembersType> memberList = resp.getSubjectgroupMembers();
		Map<CloneableSubjectGroupType, Set<CloneableSubjectType>> ret = 
			new HashMap<CloneableSubjectGroupType, Set<CloneableSubjectType>>(memberList.size());
		for (GroupMembersType groupMembersType : memberList) {
			SubjectGroupType subjectGroup = groupMembersType.getSubjectGroup();
			CloneableSubjectGroupType smartSG = new CloneableSubjectGroupType(subjectGroup);
			if (ret.containsKey(smartSG)) {
				throw new IllegalStateException("Multiple subject groups found for name: " + smartSG);
			}
			Set<CloneableSubjectType> subjList = null;
			final List<SubjectType> memberSubjects = groupMembersType.getMemberSubjects();
			if (memberSubjects != null) {
				subjList = new LinkedHashSet<CloneableSubjectType>(memberSubjects.size());
				for (SubjectType memberSubject : memberSubjects) {
					subjList.add(new CloneableSubjectType(memberSubject));
				}
			}
			ret.put(smartSG, subjList);
		}
		
		return ret;
	}

	private SubjectGroupKeyType mapSubjectGroup(SubjectGroupType subjectGroupType) {
		SubjectGroupKeyType key = new SubjectGroupKeyType();
		key.setName(subjectGroupType.getName());

		return key;
	}

	@Override
	public Set<CloneableSubjectGroupType> isMemberOf(CloneableSubjectType subject, Collection<CloneableSubjectGroupType> subjectGroups) 
	throws AuthorizationException {
		IsMemberOfRequestType req = new IsMemberOfRequestType();
		req.setSubject(subject);
		req.getSubjectgroup().addAll(subjectGroups);
		
		BaseGroupMembershipServiceConsumer groupMembershipServiceConsumer = 
			new BaseGroupMembershipServiceConsumer();
		IsMemberOfResponseType resp = groupMembershipServiceConsumer.isMemberOf(req);
		if (!AckValue.SUCCESS.equals(resp.getAck())) {
			throw new AuthorizationException(ErrorConstants.SVC_SECURITY_APP_AUTHZ_INTERNAL_ERROR, 
					EncodingUtils.encodeErrorMessage(resp.getAck(), resp.getErrorMessage()));
		}
		Set<CloneableSubjectGroupType> ret = new LinkedHashSet<CloneableSubjectGroupType>();
		for (SubjectGroupType subjectGroup : resp.getSubjectgroup()) {
			ret.add(new CloneableSubjectGroupType(subjectGroup));
		}
		return ret;
	}

}