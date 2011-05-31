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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.runtime.binding.objectnode.ObjectNode;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorDataFactory;
import org.ebayopensource.turmeric.runtime.common.exceptions.SecurityException;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.handler.HandlerPreconditions;
import org.ebayopensource.turmeric.runtime.common.impl.handlers.BaseHandler;
import org.ebayopensource.turmeric.runtime.common.impl.internal.monitoring.SystemMetricDefs;
import org.ebayopensource.turmeric.runtime.common.impl.internal.pipeline.BaseMessageContextImpl;
import org.ebayopensource.turmeric.runtime.common.impl.pipeline.LoggingHandlerUtils;
import org.ebayopensource.turmeric.runtime.common.pipeline.InboundMessage;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
import org.ebayopensource.turmeric.runtime.spf.impl.pipeline.ServerLoggingHandlerUtils;
import org.ebayopensource.turmeric.runtime.spf.security.ServerSecurityContext;
import org.ebayopensource.turmeric.security.errorlibrary.ErrorConstants;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeResponseType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.securitycommon.spf.impl.SkipLocalValidationUtil;
import org.ebayopensource.turmeric.services.authorizationservice.intf.gen.BaseAuthorizationServiceConsumer;


/**
 * This handler calls the authorization service to perform authorization.
 *
 */
public class ObjectAuthorizationHandler extends BaseHandler {

	private static Logger s_logger = Logger.getLogger("ObjectAuthorizationHandler.class");
	
	private String m_resourceType = "OBJECT";
	private String[] m_objectXpath;
	private boolean m_useServiceName = false;

	
	@Override
	public void init(InitContext ctx) throws ServiceException {
		super.init(ctx);
		HandlerPreconditions.checkServerSide(ctx, this.getClass()); // Server Side Only

		Map<String, String> options = ctx.getOptions();
		
		String str = options.get("useServiceName");
		if (str != null)
			m_useServiceName = str.equals("true");
		
		str = options.get("objectXpath");
		if (s_logger.isLoggable(Level.INFO))
    	{
			s_logger.log(Level.SEVERE, "ObjectAuthorizationHandler: " +  str); 
					
    	}

		m_objectXpath = str.split(";");
		
		m_resourceType = options.get("resourceType");
		if (m_resourceType == null || m_resourceType.isEmpty())
			m_resourceType = "OBJECT";		

	}


	@Override
	public void invoke(MessageContext ctx) throws ServiceException {
		long startTime = System.nanoTime();

		try {
			if (m_objectXpath == null || m_objectXpath.length == 0)
				return;

			boolean skipOnLocal = SkipLocalValidationUtil.checkIfSkipOnLocal(ctx) ;
			boolean isTransportLocal = ctx.getRequestMessage().getTransportProtocol().equalsIgnoreCase(SOAConstants.TRANSPORT_LOCAL);

			if (isTransportLocal && skipOnLocal) {

				// To skip  when invoked on Local Transport..
				s_logger.log(Level.WARNING, "ObjectAuthorizationHandler: " +  getLoggingUtil().getServiceDotOperation(ctx) + " " + 
						getEventText());
				return;
			}


			if (ctx.getOperationName().equalsIgnoreCase(SOAConstants.OP_GET_CACHE_POLICY)) {
				// To skip Authn when invoked on system operation
				s_logger.log(Level.WARNING, "ObjectAuthorizationHandler: " +  getLoggingUtil().getServiceDotOperation(ctx) + " " +  
						getSysOpText());
				return;
			}

			String resourceName = getResourceName(ctx);
			String operationName = ctx.getOperationName();
			int size = m_objectXpath.length;
			for (int i = 0; i < size; i++)
			{
				String objectXpath = m_objectXpath[i].trim();
				String[] token = objectXpath.split(":");
				if (resourceName.equals(token[0]) == false)
					continue;

				if (operationName.equals(token[1]))
				{
					String tagName = token[2];
					boolean isContinue = invokeAuthorizationService(ctx,resourceName, operationName, tagName);
					if (!isContinue)
						return;
				}
			}
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
	private boolean invokeAuthorizationService(MessageContext ctx,
			String resourceName,
			String operationName,
			String tagName)
			throws ServiceException {
		try {
			BaseAuthorizationServiceConsumer consumer = new BaseAuthorizationServiceConsumer();

			// create authorization request
			AuthorizeRequestType request = new AuthorizeRequestType();
			AuthorizeResponseType soaAuthzResponse = null;
			ServerSecurityContext secCtx = (ServerSecurityContext) ctx.getSecurityContext();

			setSubjects(ctx, secCtx, request);

			request.setResourceName((new StringBuffer()
				.append("SERVICE.")
				.append(resourceName).append(".")
				.append(operationName)).toString());
			
			request.setResourceType(m_resourceType); 

			ObjectNode node = ((InboundMessage)ctx.getRequestMessage()).getMessageBody();
			List<String> ret = getNodeValue(node, tagName);
			if (ret == null)
				return true;

			boolean authFail = false;
			int size = ret.size();
			for (int i =0 ; i < size; i++)
			{
				request.setOperationName(ret.get(i));
				soaAuthzResponse = consumer.authorize(request);
				if (soaAuthzResponse.getAck() == AckValue.FAILURE)
				{
					authFail = true;
					break;
				}
				authFail = false;
			}

			if (authFail) //check for super user
			{
				request.setOperationName("All");
				soaAuthzResponse = consumer.authorize(request);
				if (soaAuthzResponse.getAck() == AckValue.SUCCESS)
					authFail = false;
			}

			if (!authFail)
			{
				propagateSecurityInfoFromSOA(secCtx, soaAuthzResponse);
				return false;
			}

			
			throw new SecurityException(
					ErrorDataFactory.createErrorData(ErrorConstants.SVC_SECURITY_AUTHZ_FAILED,
							ErrorConstants.ERRORDOMAIN.toString(),
								new Object[] { request.getOperationName(),
										request.getResourceName() }));
		} catch (SecurityException e) {
			s_logger.log(Level.SEVERE, "exception", e);
			throw e;
		} catch (Exception e) {
			s_logger.log(Level.SEVERE, "exception", e);
			throw new SecurityException(
					ErrorDataFactory.createErrorData(ErrorConstants.SVC_SECURITY_UNEXPECTED_AUTHZ_ERROR,
							ErrorConstants.ERRORDOMAIN.toString(),
						new Object[] { e.getMessage() }));
		}
	}

	private void setSubjects(MessageContext ctx, ServerSecurityContext secCtx, AuthorizeRequestType request) throws Exception
	{
		// get authenticated subjects from context
		for (String subjectDomain : secCtx.getAuthnSubjects().keySet()) {
			String subjectName = secCtx.getAuthnSubjects().get(subjectDomain);
			SubjectType subject = new SubjectType();
			subject.setDomain(subjectDomain);
			subject.setValue(subjectName);
			request.getSubject().add(subject);
		}

		// retrieve IP address from context

		String sourceClientIp = (String) ctx
				.getProperty(SOAConstants.CTX_PROP_TRANSPORT_CLIENT_SOURCE_IP);
		SubjectType subject = new SubjectType();
		subject.setDomain("IP");
		subject.setValue(sourceClientIp);
		request.getSubject().add(subject);

		for (String subjectGroupName : secCtx.getResolvedSubjectGroups()) {
			SubjectGroupType subjectGroup = new SubjectGroupType();
			subjectGroup.setName(subjectGroupName);
			subjectGroup.setCalculator(null);
			subjectGroup.setDomain(null);
			request.getResolvedSubjectGroup().add(subjectGroup);
		}
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
		
		String resourceName = null;
		if (m_useServiceName)
		{
			resourceName= ctx.getServiceId().getServiceName();
		} else {
			resourceName = ctx.getAdminName();
		}
		
		return resourceName;
	}

	private List<String> getNodeValue(ObjectNode currNode, String xpath) throws Exception
	{

		if (s_logger.isLoggable(Level.INFO))
    	{
			String str = printNode(currNode, "");
			s_logger.log(Level.SEVERE, "ObjectAuthorizationHandler: " +  str); 
    	}

		List<ObjectNode> list = currNode.getChildNodes();

		List<String> retList = getNodeList(list, xpath);

		return retList;
	}

	private String printNode(ObjectNode currNode, String prefix)
	{
		StringBuilder sb = new StringBuilder(30);
		try{

		sb.append(currNode.getNodeName().getLocalPart());
		if (currNode.getNodeValue() != null)
			sb.append("=").append(currNode.getNodeValue().toString());

		sb.append("\n");

		List<ObjectNode> list = currNode.getChildNodes();
		int size = list.size();
		for (int i = 0; i < size; i++)
		{
			sb.append("\n---Node---");
			sb.append(printNode(list.get(i), "  " + prefix));
		}

		List<ObjectNode> list1 = currNode.getAttributes();
		size = list1.size();
		for (int i = 0; i < size; i++)
		{
			sb.append("\n---Attribute---");
			sb.append(printNode(list1.get(i), "  " + prefix));
		}
		} catch (Exception e)
		{

		}

		return sb.toString();

	}

	private List<String> getNodeList(List<ObjectNode> list, String xpath) throws Exception
	{

		List<String> ret = null;

		int size = list.size();
		for (int i = 0; i < size; i++)
		{
			ObjectNode currNode = list.get(i);

			String _tagName;

			boolean attFlag = false;

			//System.out.println("xpath=>" + xpath);
			String[] token = xpath.split("/");

			if (token.length == 1)
			{
				token= xpath.split("@");
				if (token.length > 1)
					attFlag = true;
			}

			if (token.length == 0)
				continue;


			String tagName = token[0];
			_tagName = currNode.getNodeName().getLocalPart();

			//System.out.println(" xpath2=>" + xpath);
			//System.out.println(" tagName=>" + tagName);
			//System.out.println("_tagName=>" + _tagName);


			if (!_tagName.equals(tagName))
				continue;


			int index = -1;
			if (attFlag)
				index = xpath.indexOf('@');
			else
				index = xpath.indexOf('/');
			if (index < 0)
			{
				if (currNode.getNodeValue() != null)
				{
					if (ret == null)
						ret = new ArrayList<String>();
					ret.add(currNode.getNodeValue().toString());
				}
			} else {
				String restOfXpath = xpath.substring(index + 1);
				List<ObjectNode> childNodeIter = null;
				if (attFlag){
					childNodeIter = currNode.getAttributes();
				} else {
					childNodeIter = currNode.getChildNodes();
				}

				List ret1 = getNodeList(childNodeIter, restOfXpath);
				if (ret1 != null)
				{
					if (ret == null)
					{
						ret = new ArrayList<String>();
					}
					ret.addAll(ret1);
				}
			}
		}

		return ret;
	}
}
