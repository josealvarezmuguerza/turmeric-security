/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.ratelimiterservice.impl.handler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.services.ratelimiterservice.intf.gen.BaseRateLimiterServiceConsumer;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorData;
import org.ebayopensource.turmeric.errorlibrary.turmericratelimiter.ErrorConstants;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorUtils;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.handler.HandlerPreconditions;
import org.ebayopensource.turmeric.runtime.common.impl.handlers.BaseHandler;
import org.ebayopensource.turmeric.runtime.common.impl.internal.monitoring.SystemMetricDefs;
import org.ebayopensource.turmeric.runtime.common.impl.internal.pipeline.BaseMessageContextImpl;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
import org.ebayopensource.turmeric.runtime.spf.security.ServerSecurityContext;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedResponse;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;

import com.ebay.kernel.calwrapper.trafficlimiter.TrafficLimiterStatusEnum;

/**
 * This handler calls the RateLimiter service to perform RateLimiter.
 * 
 */
public class RateLimiterHandler extends BaseHandler {

	private static Logger s_logger = Logger.getLogger("RateLimiterHandler.class");

	private static String delimiter = "[delimiter]";
	private static final String SUBJECT_TYPE_ID_SUFFIX = "_ID" ;
	
	@Override
	public void init(InitContext ctx) throws ServiceException {
		super.init(ctx);
		HandlerPreconditions.checkServerSide(ctx, this.getClass()); // Server Side Only	
	}

	@Override
	public void invoke(MessageContext ctx) throws ServiceException {
		long startTime = System.nanoTime();
		try {
			// make a call thru SOA (local/remote biding)
			invokeSOARateLimiterService(ctx);
			
		} catch (ServiceException e) {
			throw e;
		} finally {
			// update metrics
			long duration = System.nanoTime() - startTime;
			((BaseMessageContextImpl) ctx).updateSvcAndOpMetric(
					SystemMetricDefs.OP_TIME_TRAFFICLIMITER, duration);
		}
	}
	/**
	 * Invoke RateLimiter service via SOA Service call.
	 * 
	 * @param ctx
	 * @throws ServiceException
	 */
	private void invokeSOARateLimiterService(MessageContext ctx)
			throws ServiceException {
		ServerSecurityContext secCtx = (ServerSecurityContext) ctx
		.getSecurityContext();
		
		if (secCtx.getWhitelistStatus().isSuccess())
			return;
			
		BaseRateLimiterServiceConsumer consumer = new BaseRateLimiterServiceConsumer();

		// create RateLimiter request
		IsRateLimitedRequest request = new IsRateLimitedRequest();
		request.setResourceName(getResourceName(ctx));
		request.setOperationName(ctx.getOperationName());
		request.setResourceType("SERVICE");
		
		
		//retrieve IP from context
		SubjectType subject;
		
		String sourceClientIp = (String) ctx
        .getProperty(SOAConstants.CTX_PROP_TRANSPORT_CLIENT_SOURCE_IP);
		if (!isEmpty(sourceClientIp)) {
			subject = new SubjectType();
			subject.setDomain("IP");
			subject.setValue(sourceClientIp);
			request.getSubject().add(subject);
		}	
		
		//get authenticated subjects from contex
		Map<String, String> authSubjectMap = secCtx.getAuthnSubjects();
		
		for (String subjectDomain : authSubjectMap.keySet()) {
			if (isEmpty(subjectDomain)) continue; // do not process null keys
			String subjectValue = authSubjectMap.get(subjectDomain);
			
			if (subjectDomain.endsWith(SUBJECT_TYPE_ID_SUFFIX)) {
				// the current key is a subject id (domain) for e.g, APP_ID
				String subjectNameDomain = subjectDomain.substring(0, subjectDomain.length() - SUBJECT_TYPE_ID_SUFFIX.length());
				String subjectNameValue = authSubjectMap.get(subjectNameDomain);
				if (!isEmpty(subjectNameValue))
				{
					subjectDomain = subjectNameDomain;
					subjectValue = subjectValue + delimiter + subjectNameValue;
				}	
			} else {
				// the current key is a subject name (domain) for e.g, APP
				// check for matching id domain exists, if yes, then skip as it is handled in the 'if'
				if (authSubjectMap.containsKey(subjectDomain + SUBJECT_TYPE_ID_SUFFIX))
					continue;
			}
			
			if (!isEmpty(subjectValue))
			{
				subject = new SubjectType();
				subject.setDomain(subjectDomain);
				subject.setValue(subjectValue);
				request.getSubject().add(subject);
			}
		}
			
		for (String subjectGroupName : secCtx.getResolvedSubjectGroups()) {
			SubjectGroupType subjectGroup = new SubjectGroupType();
			subjectGroup.setName(subjectGroupName);
			request.getResolvedSubjectGroup().add(subjectGroup);
		}

		IsRateLimitedResponse isRateLimitedResponse = null;
		try {
			isRateLimitedResponse = consumer.isRateLimited(request);
			propagateSecurityInfoFromSOA(secCtx, isRateLimitedResponse);
		} catch (Exception e) {
			s_logger.log(Level.SEVERE, "exception",  e);
			throw new ServiceException(
					ErrorUtils.createErrorData(ErrorConstants.SVC_RATELIMITER_SYSTEM_ERROR,
					ErrorConstants.ERRORDOMAIN.toString(), new Object[] { e.getMessage() }));
		}

		if (isRateLimitedResponse.getAck() == AckValue.FAILURE) {
			List<CommonErrorData> errorDataList = isRateLimitedResponse
						.getErrorMessage().getError();
			String errorText = "Generic RateLimiter error";
			if (errorDataList.size() > 0) {
				ErrorData errorData = errorDataList.get(0);
				errorText = errorData.getMessage();
			}
				

			throw new ServiceException(
					ErrorUtils.createErrorData(ErrorConstants.SVC_RATELIMITER_SYSTEM_ERROR,
							ErrorConstants.ERRORDOMAIN.toString(), new Object[] { request.getOperationName(),
								request.getResourceName(),
								errorText}));
		} else {
			String status = isRateLimitedResponse.getStatus().value();
					
			if (status.equals(TrafficLimiterStatusEnum.SERVE_BLOCK.getName())) {
				throw new ServiceException(ErrorUtils.createErrorData(ErrorConstants.SVC_RATELIMITER_CALL_EXCEEDED_LIMIT,
						ErrorConstants.ERRORDOMAIN.toString(), new Object[] { request.getOperationName(),
					request.getResourceName() }));
			}
		}
		
	}
	
	private boolean isEmpty(String str) {
		if(str==null || str.trim().isEmpty())
			return true;
		return false;
	}

	// add the resolved subject group list from israteLimited response to the security
	// context
	private void propagateSecurityInfoFromSOA(ServerSecurityContext secCtx,
			IsRateLimitedResponse isRateLimitedResponse) throws ServiceException {

		// add resolved subject groups to context
		if (isRateLimitedResponse.getResolvedSubjectGroup() != null) {
			List<SubjectGroupType> resolvedSubjectGroups = isRateLimitedResponse
					.getResolvedSubjectGroup();
			Iterator<SubjectGroupType> iter = resolvedSubjectGroups.iterator();
			while (iter.hasNext()) {
				SubjectGroupType resolvedSubjectGroup = iter.next();
				secCtx.setResolvedSubjectGroup(resolvedSubjectGroup.getName());
			}
		}
	}
	
	private String getResourceName(MessageContext ctx) {
		String resourceName = ctx.getAdminName();
		if (resourceName == null ) { // For Backward compatibility
			resourceName = ctx.getServiceQName().getLocalPart();
		}		
		return resourceName;
	}	
	
}
