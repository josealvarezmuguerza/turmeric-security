/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl.handler;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorData;
import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorConstants;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorDataFactory;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.exceptions.SecurityException;
import org.ebayopensource.turmeric.runtime.common.handler.HandlerPreconditions;
import org.ebayopensource.turmeric.runtime.common.impl.handlers.BaseHandler;
import org.ebayopensource.turmeric.runtime.common.impl.internal.monitoring.SystemMetricDefs;
import org.ebayopensource.turmeric.runtime.common.impl.internal.pipeline.BaseMessageContextImpl;
import org.ebayopensource.turmeric.runtime.common.impl.pipeline.LoggingHandlerUtils;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
import org.ebayopensource.turmeric.runtime.spf.impl.pipeline.ServerLoggingHandlerUtils;
import org.ebayopensource.turmeric.runtime.spf.security.ServerSecurityContext;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeResponseType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.securitycommon.spf.impl.SkipLocalValidationUtil;
import org.ebayopensource.turmeric.services.authorizationservice.intf.gen.BaseAuthorizationServiceConsumer;


/**
 * This handler calls the authorization service to perform authorization.
 * 
 * @author dmuthiayen
 */
public class AuthorizationHandler extends BaseHandler {
	
	/**
	 * Logger.
	 */
	private static Logger s_logger = Logger.getLogger("AuthorizationHandler.class");

	@Override
	public void init(InitContext ctx) throws ServiceException {
		super.init(ctx);
		// Server Side Only
		HandlerPreconditions.checkServerSide(ctx, this.getClass());
	}

	@Override
	public void invoke(MessageContext ctx) throws ServiceException {
		long startTime = System.nanoTime();

		try {
			boolean skipAuthz = SkipLocalValidationUtil.checkIfSkipOnLocal(ctx);
			
			boolean isTransportLocal = ctx.getRequestMessage().getTransportProtocol().equalsIgnoreCase(SOAConstants.TRANSPORT_LOCAL);
			
			if (isTransportLocal && skipAuthz) {
				
				// To skip Authz when invoked on Local Transport..
				s_logger.log(Level.WARNING, "AUTHZ: " +  getLoggingUtil().getServiceDotOperation(ctx) + " " + 
						getEventText());
				return;
			}
			if (ctx.getOperationName().equalsIgnoreCase(SOAConstants.OP_GET_CACHE_POLICY)
					||ctx.getOperationName().equalsIgnoreCase(SOAConstants.OP_GET_VERSION)) {
				// To skip Authn when invoked on system operation
				s_logger.log(Level.WARNING, "AUTHZ: " +  getLoggingUtil().getServiceDotOperation(ctx) + " " +  
						getSysOpText());
				return;
			}
			
			// make a call thru SOA (local/remote biding)
			invokeSOAAuthorizationService(ctx);
		} catch (ServiceException e) {
			throw e;
		} finally {
			// update metrics
			long duration = System.nanoTime() - startTime;
			((BaseMessageContextImpl) ctx).updateSvcAndOpMetric(
					SystemMetricDefs.OP_TIME_AUTHORIZATION, duration);
		}
	}

	private LoggingHandlerUtils getLoggingUtil() {
		LoggingHandlerUtils loggingHandlerUtils = new ServerLoggingHandlerUtils();
		return loggingHandlerUtils;
	}
	
	private String getEventText() {
		// Default String.. invoked only from one ctxt.
		return "disabled=true,transport=LOCAL";
	}
	
	private String getSysOpText() {
		// Default String.. invoked only from one ctxt.
		return "disabled=true,sysOpName=" + SOAConstants.OP_GET_CACHE_POLICY;
	}
	

	/**
	 * Invoke authorization service via SOA Service call
	 * 
	 * @param ctx
	 * @throws ServiceException
	 */
	private void invokeSOAAuthorizationService(MessageContext ctx)
			throws ServiceException {
		BaseAuthorizationServiceConsumer consumer = new BaseAuthorizationServiceConsumer();

		// create authorization request
		AuthorizeRequestType request = new AuthorizeRequestType();
		request.setResourceName(getResourceName(ctx));
		request.setOperationName(ctx.getOperationName());
		request.setResourceType("SERVICE");

		ServerSecurityContext secCtx = (ServerSecurityContext) ctx
				.getSecurityContext();
		// get authenticated subjects from context
		for (String subjectDomain : secCtx.getAuthnSubjects().keySet()) {
			if(isEmpty(subjectDomain))
				continue;
			String subjectName = secCtx.getAuthnSubjects().get(subjectDomain);
			if(isEmpty(subjectName))
				continue;
			SubjectType subject = new SubjectType();
			subject.setDomain(subjectDomain);
			subject.setValue(subjectName);
			request.getSubject().add(subject);
		}

		// retrieve IP address from context
		SubjectType subject;
		String sourceClientIp = (String) ctx
        .getProperty(SOAConstants.CTX_PROP_TRANSPORT_CLIENT_SOURCE_IP);
		if(!isEmpty(sourceClientIp)){
			subject = new SubjectType();
			subject.setDomain("IP");
			subject.setValue(sourceClientIp);
			request.getSubject().add(subject);
		}
		
		// get resolved subject groups from context
		for (String subjectGroupName : secCtx.getResolvedSubjectGroups()) {
			SubjectGroupType subjectGroup = new SubjectGroupType();
			subjectGroup.setName(subjectGroupName);
			subjectGroup.setCalculator(null);
			subjectGroup.setDomain(null);
			request.getResolvedSubjectGroup().add(subjectGroup);
		}

		AuthorizeResponseType soaAuthzResponse = null;
		try {
			soaAuthzResponse = consumer.authorize(request);
			// propagate subject group list to context
			propagateSecurityInfoFromSOA(secCtx, soaAuthzResponse);

			if (soaAuthzResponse.getAck() == AckValue.FAILURE) {
				List<CommonErrorData> errorDataList = soaAuthzResponse
						.getErrorMessage().getError();
				String errorText = "Generic authorization error";
				String errorId = "0";
				if (errorDataList.size() > 0) {
					ErrorData errorData = errorDataList.get(0);
					errorText = errorData.getMessage();
					errorId = String.valueOf(errorData.getErrorId());
				}
				secCtx.setAuthzFailure(errorText, errorId, null, null);

				throw new SecurityException(
						ErrorDataFactory.createErrorData(ErrorConstants.SVC_SECURITY_AUTHZ_FAILED,
								ErrorConstants.ERRORDOMAIN.toString(),
						new Object[] { request.getOperationName(),
								request.getResourceName() }));
			} else {
				secCtx.setAuthzSuccess(null, null, null);
			}
		} catch (SecurityException e) {
			s_logger.log(Level.SEVERE, "exception ", e);
			throw e;
		} catch (Exception e) {
			s_logger.log(Level.SEVERE, "exception ", e);
			throw new SecurityException(
					ErrorDataFactory.createErrorData(ErrorConstants.SVC_SECURITY_UNEXPECTED_AUTHZ_ERROR,
							ErrorConstants.ERRORDOMAIN.toString(),
					new Object[] { e.getMessage() }));
		}
	}


	private boolean isEmpty(String str) {
		if(str==null|| str.trim().isEmpty())
			return true;
		return false;
	}

	// add the resolved subject group list from authz response to the security
	// context
	private void propagateSecurityInfoFromSOA(ServerSecurityContext secCtx,
			AuthorizeResponseType soaAuthzResponse) throws ServiceException {

		// add resolved subject groups to context
		if (soaAuthzResponse.getResolvedSubjectGroup() != null) {
			List<SubjectGroupType> resolvedSubjectGroups = soaAuthzResponse
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
