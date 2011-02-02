/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/

package org.ebayopensource.turmeric.services.groupmembershipservice.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.BaseResponse;
import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.errorlibrary.turmericpolicy.ErrorConstants;
import org.ebayopensource.turmeric.groupmembershipservice.exceptions.GroupMembershipException;
import org.ebayopensource.turmeric.groupmembershipservice.intf.GroupMembershipService;
import org.ebayopensource.turmeric.groupmembershipservice.provider.BaseCalculatedSubjectGroup;
import org.ebayopensource.turmeric.groupmembershipservice.provider.GroupMembershipProvider;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorUtils;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceRuntimeException;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.security.v1.services.GetGroupMembersRequestType;
import org.ebayopensource.turmeric.security.v1.services.GetGroupMembersResponseType;
import org.ebayopensource.turmeric.security.v1.services.GroupMembersType;
import org.ebayopensource.turmeric.security.v1.services.IsMemberOfRequestType;
import org.ebayopensource.turmeric.security.v1.services.IsMemberOfResponseType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKeyType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.services.groupmembershipservice.provider.config.GroupMembershipServiceProviderFactory;
import org.ebayopensource.turmeric.utils.ContextUtils;
import org.ebayopensource.turmeric.utils.ReflectionUtils;


public class GroupMembershipServiceImpl
    implements GroupMembershipService
{
	private static  Logger s_logger = LogManager.getInstance(GroupMembershipServiceImpl.class);
	private static volatile GroupMembershipProvider s_provider;
	
	private static final String s_providerPropFilePath = 
		"META-INF/soa/services/config/GroupMembershipService/service_provider.properties";
	private static final String s_providerPropKey = "preferred-provider";


	private static List<CommonErrorData> s_errorData = null;

	private static void initialize() {
		if (s_errorData != null) {
			throw new ServiceRuntimeException(s_errorData);
		}
		try {
			if (s_provider == null) {
				synchronized (GroupMembershipServiceImpl.class) {
					if (s_provider == null)	{						
						s_provider = GroupMembershipServiceProviderFactory.create(getPreferredProvider());
					}
				}
			}
		} catch (ServiceException se) {
			s_errorData = se.getErrorMessage().getError();
			throw new ServiceRuntimeException(s_errorData);
		}
	}
	
	private static String getPreferredProvider() {
		ClassLoader classLoader = ContextUtils.getClassLoader();
		InputStream	inStream = classLoader.getResourceAsStream(s_providerPropFilePath);
		String provider = null;
		if (inStream != null) {
			Properties properties = new Properties();
			try {
				properties.load(inStream);
				provider = (String)properties.get(s_providerPropKey);
			} catch (IOException e) {
				
			}
			finally {
				try {
					inStream.close();
				} catch (IOException e) {
					
				}
			}
		}
		return provider;
	}
	
	public IsMemberOfResponseType isMemberOf(IsMemberOfRequestType request)
	{
		initialize();
					
     	if (request == null) {
     		return buildIsMemberOfResponse(ErrorConstants.SVC_GROUPMEMBERSHIPSERVICE_INVALID_INPUT_ERROR, "null input specified as request");
     	}

		IsMemberOfResponseType resp = new IsMemberOfResponseType();
		SubjectType subject = request.getSubject();
		List<SubjectGroupType> subjectGroupList = request.getSubjectgroup();

		List<SubjectGroupType> validSubjectGroupList = new ArrayList<SubjectGroupType>();
		if ( subjectGroupList == null ||
				subjectGroupList.isEmpty() ||
				subject == null ||
				subject.getDomain() == null ||
				subject.getDomain().length() == 0 ||
				subject.getValue() == null || 
				subject.getValue().length() == 0) 
		{
			return buildIsMemberOfResponse(ErrorConstants.SVC_GROUPMEMBERSHIPSERVICE_INVALID_INPUT_ERROR, "request doesn't " +
					"have required information for the subject/subjectgroups in request");
		}

		Boolean needFurtherAction = false;
		for (SubjectGroupType subjectGroup : subjectGroupList) {
			if (subjectGroup == null)
				continue;
			
			if (subject.getDomain().equals(subjectGroup.getDomain()) == false)
				continue;
			
			
	    	if (!isCalculatorDefined(subjectGroup))
	    	{
	    		needFurtherAction = true;
	    	} else
				try {
					if (isMemberOfCalculateGroup(subjectGroup, subject))
					{
						validSubjectGroupList.add(subjectGroup);
					}
				} catch (ServiceException e) {
					throw new ServiceRuntimeException(e.getErrorMessage().getError());
				} catch (GroupMembershipException e) {
					return buildIsMemberOfResponse(ErrorConstants.SVC_GROUPMEMBERSHIPSERVICE_INTERNAL_ERROR, e.getMessage());
				} 
		}
		
		if (needFurtherAction)
		{	
			List<SubjectGroupType> l = null;
			try {
				l = s_provider.getSubjectGroupsBySubject(subject);
			} catch (GroupMembershipException e) {
				return buildIsMemberOfResponse(ErrorConstants.SVC_GROUPMEMBERSHIPSERVICE_INTERNAL_ERROR, e.getMessage());
			}
			if (!(l == null || l.isEmpty()))
			{
				for (SubjectGroupType subjectGroup : subjectGroupList) {
					for (SubjectGroupType g : l) {
						if (g.getName() == null)
							continue;
						
						if (subjectGroup != null && !isCalculatorDefined(subjectGroup) &&
							subjectGroup.getName().equals(g.getName()) &&
							subjectGroup.getDomain().equals(subject.getDomain())) {
							validSubjectGroupList.add(subjectGroup);
						}
					}
				}
			}
		}

		if (validSubjectGroupList.isEmpty()) {
			resp.setIsMember(false);
		} else {
			resp.setIsMember(true);
		}
		
		resp .setAck(AckValue.SUCCESS);
		resp.getSubjectgroup().addAll(validSubjectGroupList);
		return resp;

	}

	private void buildResponse(BaseResponse resp, String errorId, String errorMsg) {
		
		resp.setAck(AckValue.FAILURE);
		if (errorId != null && errorId.isEmpty() == false) {
			ErrorMessage errorMessage = new ErrorMessage();
			resp.setErrorMessage(errorMessage);
			resp.getErrorMessage().getError().add(ErrorUtils.createErrorData(
					errorId, ErrorConstants.ERRORDOMAIN.toString()));
		}
		
		if (errorMsg != null && errorMsg.isEmpty() == false)
			s_logger.log(Level.INFO, errorMsg);
		
	}
	
	private GetGroupMembersResponseType buildGetGMResponse(
			String errorId, String errorMsg) {
		GetGroupMembersResponseType resp = new GetGroupMembersResponseType();
		
		buildResponse(resp, errorId, errorMsg);
		return resp;
	}
	
	private IsMemberOfResponseType buildIsMemberOfResponse(
			String errorId, String errorMsg) {
		IsMemberOfResponseType resp = new IsMemberOfResponseType();
		
		buildResponse(resp, errorId, errorMsg);
		resp.setIsMember(false);
		return resp;
	}
	
	public GetGroupMembersResponseType getGroupMembers(GetGroupMembersRequestType request) {
		initialize();

		GetGroupMembersResponseType response = new GetGroupMembersResponseType();

		if (request == null) {
			return buildGetGMResponse(ErrorConstants.SVC_GROUPMEMBERSHIPSERVICE_INVALID_INPUT_ERROR, "null input specified as request");
     	} 
		
		List<SubjectGroupKeyType> subjectGroupKeys = request.getSubjectgroupKey();
		
		if (subjectGroupKeys == null || subjectGroupKeys.size() <= 0) {
			response.setAck(AckValue.FAILURE);
			response.setErrorMessage(new ErrorMessage());
			response.getErrorMessage().getError().add(
					ErrorUtils.createErrorData(
							ErrorConstants.SVC_GROUPMEMBERSHIPSERVICE_INVALID_INPUT_ERROR, 
							ErrorConstants.ERRORDOMAIN.toString()));
			return response;
		}
		//try not to get duplicate stuff for subjectgroupkeys if repeated
		Set<SubjectGroupKeyType> processedSubGrpKeys = new HashSet<SubjectGroupKeyType>();
		try {
			for (SubjectGroupKeyType subjectGroupKey : subjectGroupKeys) {
				if(isInProcessedKeys(processedSubGrpKeys, subjectGroupKey))
					continue;
				GroupMembersType subjectGroup = s_provider.getSubjectGroupByKey(subjectGroupKey);
				if(subjectGroup == null || subjectGroup.getSubjectGroup() == null)
					continue;
				
				if (!isCalculatorDefined(subjectGroup.getSubjectGroup()))
					response.getSubjectgroupMembers().add(subjectGroup);
				processedSubGrpKeys.add(subjectGroupKey);
			}
			response.setAck(AckValue.SUCCESS);
		} catch (GroupMembershipException e) {
			s_logger.log(Level.SEVERE, "getSubjectGroupByKey failed", e);
			response.setAck(AckValue.FAILURE);
			response.setErrorMessage(new ErrorMessage());
			response.getErrorMessage().getError().add(
					ErrorUtils.createErrorData(
							ErrorConstants.SVC_GROUPMEMBERSHIPSERVICE_INTERNAL_ERROR, 
							ErrorConstants.ERRORDOMAIN.toString()));
		}
		
		
		return response;
	}
	


	private boolean isInProcessedKeys(
			Set<SubjectGroupKeyType> processedSubGrpKeys,SubjectGroupKeyType subjectGroupKey) {
		
		if(subjectGroupKey==null) return false;
		
		for(SubjectGroupKeyType processedKey:processedSubGrpKeys){
			if(processedKey.getId()==subjectGroupKey.getId() && processedKey.getName().equals(subjectGroupKey.getName()))
				return true;
		}
		return false;
	}

	private Boolean isMemberOfCalculateGroup(SubjectGroupType subjectGroup,SubjectType subject) throws ServiceException, GroupMembershipException
    {
    	String CalcStr = subjectGroup.getCalculator();
    	if (CalcStr == null || CalcStr.trim().length() == 0)
    		return false;
    		  		

    	SubjectGroupType calcSG = s_provider.getCalculatedSubjectGroup(subjectGroup);
    	if (calcSG == null) {
    		s_logger.log(Level.SEVERE, "Calculated subject group found:", subjectGroup.getName());
    		return false;
    	}	
    	
    	try {
			return isMemberOf(subject, calcSG);
		} catch (Exception e) {
			throw new ServiceException(
					ErrorUtils.createErrorData(ErrorConstants.SVC_GROUPMEMBERSHIPSERVICE_INTERNAL_ERROR, 
							ErrorConstants.ERRORDOMAIN.toString()), e.getCause());
		}
    }

	private Boolean isMemberOf(SubjectType subject, SubjectGroupType calcSG)
			throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		BaseCalculatedSubjectGroup obj;
		try {
			obj = ReflectionUtils
					.createInstance(calcSG.getCalculator().trim(),
							BaseCalculatedSubjectGroup.class, cl,
							new Class[] {}, null);
		} catch (Exception e) {
			s_logger.log(Level.SEVERE, "Impl class: " + calcSG.getCalculator() + " not found for SubjectGroup:" +
					calcSG.getName());
			return false;
		}
			
		return obj.contains(subject);
			
	}
	

	private boolean isCalculatorDefined( SubjectGroupType subjectGroup ) {
		return subjectGroup.getCalculator() != null && subjectGroup.getCalculator().trim().length() > 0;
	}
   
}
