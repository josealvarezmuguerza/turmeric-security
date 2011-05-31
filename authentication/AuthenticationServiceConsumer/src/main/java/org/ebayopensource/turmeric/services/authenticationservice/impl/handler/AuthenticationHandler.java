/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
//B''H
package org.ebayopensource.turmeric.services.authenticationservice.impl.handler;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.ebayopensource.turmeric.runtime.common.security.SecurityContext;
import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
import org.ebayopensource.turmeric.runtime.spf.impl.pipeline.ServerLoggingHandlerUtils;
import org.ebayopensource.turmeric.runtime.spf.security.ServerSecurityContext;
import org.ebayopensource.turmeric.security.v1.services.AuthenticateRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthenticateResponseType;
import org.ebayopensource.turmeric.security.v1.services.AuthenticationExtendedResponseType;
import org.ebayopensource.turmeric.security.v1.services.CredentialType;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePairType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.securitycommon.spf.impl.SkipLocalValidationUtil;
import org.ebayopensource.turmeric.services.authenticationservice.intf.gen.BaseAuthenticationServiceConsumer;

/**
 * This handler calls the authentication service to perform authentication.
 * 
 * @author gyue
 */
public class AuthenticationHandler extends BaseHandler {
	/**
	 * logger.
	 */
	private static Logger s_logger = Logger
			.getLogger("AuthenticationHandler.class");

	/**
	 * Required credential.
	 */
	private static final String CREDENTIAL_IPADDRESS = "ipaddress";

	/**
	 * Logging Handler.
	 */
	private static final LoggingHandlerUtils loggingHandlerUtils = new ServerLoggingHandlerUtils();

	/**
	 * if "use-ip-credential" is not null and is not set to true, this will be
	 * set to false.
	 */
	private boolean m_extractIP = true;

	@Override
	public void init(InitContext ctx) throws ServiceException {
		super.init(ctx);
		// Server Side Only
		HandlerPreconditions.checkServerSide(ctx, this.getClass());
		Map<String, String> options = ctx.getOptions();

		// Only if configured on the server side and user-ip-credential=true
		String value = options.get("use-ip-credential");
		m_extractIP = (value == null || "true".equalsIgnoreCase(value));

	}

	@Override
	public void invoke(MessageContext ctx) throws ServiceException {

		long startTime = System.nanoTime();

		if (m_extractIP) {
			String clientIp = (String) ctx
					.getProperty(SOAConstants.CTX_PROP_TRANSPORT_CLIENT_SOURCE_IP);
			if (clientIp != null) {
				ctx.getSecurityContext().setCredential(CREDENTIAL_IPADDRESS,
						clientIp);
			}
		}

		try {
			boolean skipAuthn = SkipLocalValidationUtil.checkIfSkipOnLocal(ctx);
			boolean isTransportLocal = ctx.getRequestMessage()
					.getTransportProtocol()
					.equalsIgnoreCase(SOAConstants.TRANSPORT_LOCAL);

			if (isTransportLocal && skipAuthn) {

				// To skip Authn when invoked on Local Transport..
				s_logger.log(Level.WARNING, "AUTHN: "
						+ getLoggingUtil().getServiceDotOperation(ctx) + " "
						+ getLocalEventText());
				return;
			}
			if (ctx.getOperationName().equalsIgnoreCase(
					SOAConstants.OP_GET_CACHE_POLICY)
					|| ctx.getOperationName().equalsIgnoreCase(
							SOAConstants.OP_GET_VERSION)) {

				// To skip Authn when invoked on system operation
				s_logger.log(Level.WARNING, "AUTHN: "
						+ getLoggingUtil().getServiceDotOperation(ctx) + " "
						+ getSysOpText(ctx.getOperationName()));
				return;
			}

			// make a call thru SOA (local/remote biding)
			invokeSOAAuthentiationService(ctx);

			logRemoteEventText(ctx);
		} catch (ServiceException e) {
			throw e;
		} finally {
			// update metrics
			long duration = System.nanoTime() - startTime;
			((BaseMessageContextImpl) ctx).updateSvcAndOpMetric(
					SystemMetricDefs.OP_TIME_AUTHENTICATION, duration);
		}
	}

	/**
	 * @return @link LoggingHandlerUtils
	 */
	private static LoggingHandlerUtils getLoggingUtil() {
		return loggingHandlerUtils;
	}

	/**
	 * @return hardcoded local event text.
	 */
	private String getLocalEventText() {
		// Default String.. invoked only from one ctxt.
		return "disabled=true,transport=LOCAL";
	}

	/**
	 * @param operationName
	 *            the sysOpName to be included in the return string
	 * @return hardcoded sysop text
	 */
	private String getSysOpText(String operationName) {
		// Default String.. invoked only from one ctxt.
		return "disabled=true,sysOpName=" + operationName;
	}

	/**
	 * logs all the subjects and groups in the security context wrapped by the
	 * passed in message context.
	 * 
	 * @param ctx @link MessageContext
	 * @throws ServiceException
	 *             any exceptions wrapped in this
	 */
	private static void logRemoteEventText(MessageContext ctx)
			throws ServiceException {

		final SecurityContext secCtx = ctx.getSecurityContext();
		final StringBuilder strBuf = new StringBuilder();
		int size, i;

		final Map<String, String> subjects = secCtx.getAuthnSubjects();
		if (!subjects.isEmpty()) {
			i = 0;
			size = subjects.size();
			strBuf.append("Subjects=[");
			for (Map.Entry<String, String> subject : subjects.entrySet()) {

				strBuf.append(subject.getKey() + "=" + subject.getValue());
				if (i < size - 1)
					strBuf.append("&");
				i++;
			}
			strBuf.append("]");
		}

		final List<String> subjectGroups = secCtx.getResolvedSubjectGroups();
		final String subjectGroupDomain = null; // SubjectGroups do not have
												// domain..so filling with null
		if (!subjectGroups.isEmpty()) {
			i = 0;
			size = subjectGroups.size();
			if (strBuf.length() > 0) {
				strBuf.append(",");
			}
			strBuf.append("SubjectGroups=[");
			for (String subjectGroup : subjectGroups) {
				strBuf.append(subjectGroupDomain + "=" + subjectGroup);
				if (i < size - 1)
					strBuf.append("&");
				i++;
			}
			strBuf.append("]");
		}

		// Log the AuthN event with the identity information
		s_logger.log(Level.WARNING,
				"AUTHN: " + getLoggingUtil().getServiceDotOperation(ctx) + " "
						+ strBuf.toString());
	}

	/**
	 * get the resource name.
	 * 
	 * @param ctx @link MessageContext
	 * @return resource name
	 */
	private String getResourceName(MessageContext ctx) {
		String resourceName = ctx.getAdminName();
		if (resourceName == null) { // For Backward compatibility
			resourceName = ctx.getServiceQName().getLocalPart();
		}
		return resourceName;
	}

	/**
	 * Invoke authentication service via SOA Service call. If response is
	 * failure, will throw a @link SecurityException. Other exceptions also
	 * wrapped in SecurityException.
	 * 
	 * @param ctx @link MessageContext
	 * @throws ServiceException
	 *             if response is failure or other exceptions.
	 */
	private void invokeSOAAuthentiationService(MessageContext ctx)
			throws ServiceException {
		BaseAuthenticationServiceConsumer consumer = new BaseAuthenticationServiceConsumer();
		AuthenticateRequestType request = new AuthenticateRequestType();
		request.setResourceName(getResourceName(ctx));
		request.setOperationName(ctx.getOperationName());
		request.setResourceType("Service");

		ServerSecurityContext secCtx = (ServerSecurityContext) ctx
				.getSecurityContext();
		// get credential from context
		Map<String, String> credentials = ctx.getSecurityContext()
				.getCredentials();
		Iterator<String> i = credentials.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			String value = credentials.get(key);
			CredentialType cred = new CredentialType();
			cred.setName(key);
			cred.setValue(value);
			request.getCredential().add(cred);
		}
		AuthenticateResponseType soaAuthnResponse = null;
		try {
			soaAuthnResponse = consumer.authenticate(request);
			// propagate subject list to context
			propagateSecurityInfoFromSOA(secCtx, soaAuthnResponse);

			if (soaAuthnResponse.getAck() == AckValue.FAILURE) {
				List<CommonErrorData> errorDataList = soaAuthnResponse
						.getErrorMessage().getError();
				String errorText = "Generic authentication error";
				String errorId = "0";
				if (errorDataList.size() > 0) {
					ErrorData errorData = errorDataList.get(0);
					errorText = errorData.getMessage();
					errorId = String.valueOf(errorData.getErrorId());
				}
				secCtx.setAuthnFailure(errorText, errorId, null, null);

				throw new SecurityException(ErrorDataFactory.createErrorData(
						ErrorConstants.SVC_SECURITY_AUTHN_FAILED,
						ErrorConstants.ERRORDOMAIN.toString(),
						new Object[] { errorText }));

			} else {
				secCtx.setAuthnSuccess(null, null, null);
			}
		} catch (SecurityException e) {
			s_logger.log(Level.SEVERE, "exception ", e);
			throw e;
		} catch (Exception e) {
			s_logger.log(Level.SEVERE, "exception ", e);

			throw new SecurityException(ErrorDataFactory.createErrorData(
					ErrorConstants.SVC_SECURITY_UNEXPECTED_AUTHN_ERROR,
					ErrorConstants.ERRORDOMAIN.toString(),
					new Object[] { e.getMessage() }));
		}
	}

	/**
	 * Copy the subject list from authn response to the security context.
	 * 
	 * @param secCtx @link ServerSecurityContext
	 * @param soaAuthnResponse @link AuthenticateResponseType
	 * @throws ServiceException propogate any exceptions
	 */
	private void propagateSecurityInfoFromSOA(ServerSecurityContext secCtx,
			AuthenticateResponseType soaAuthnResponse) throws ServiceException {

		// set the authn method
		secCtx.setAuthnMethodName(soaAuthnResponse.getAuthenticationMethod());

		// set authentication subjects
		if (soaAuthnResponse.getSubject() != null) {
			List<SubjectType> subjects = soaAuthnResponse.getSubject();
			Iterator<SubjectType> i = subjects.iterator();
			while (i.hasNext()) {
				SubjectType sub = i.next();
				secCtx.setAuthnSubject(sub.getDomain(), sub.getValue());
			}
		}

		// set resolved subject groups
		if (soaAuthnResponse.getSubjectgroup() != null) {
			List<SubjectGroupType> subjectGroups = soaAuthnResponse
					.getSubjectgroup();
			Iterator<SubjectGroupType> sg = subjectGroups.iterator();
			while (sg.hasNext()) {
				SubjectGroupType subgrp = sg.next();
				secCtx.setResolvedSubjectGroup(subgrp.getName());
			}
		}

		AuthenticationExtendedResponseType extendedInfo = soaAuthnResponse
				.getExtendedInfo();
		if (extendedInfo != null) {
			if (extendedInfo.getKvpair().size() > 0) {
				for (KeyValuePairType kvpair : extendedInfo.getKvpair()) {
					secCtx.setAuthnCustomData(kvpair.getKey(),
							kvpair.getValue());
				}
			}
		}
	}
}
