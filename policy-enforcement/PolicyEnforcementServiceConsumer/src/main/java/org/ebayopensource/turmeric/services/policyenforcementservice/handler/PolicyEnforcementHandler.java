/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *    
 * http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
//B''H
package org.ebayopensource.turmeric.services.policyenforcementservice.handler;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorData;
import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorConstants;
import org.ebayopensource.turmeric.runtime.binding.objectnode.ObjectNode;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorUtils;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.handler.HandlerPreconditions;
import org.ebayopensource.turmeric.runtime.common.impl.handlers.BaseHandler;
import org.ebayopensource.turmeric.runtime.common.impl.internal.monitoring.SystemMetricDefs;
import org.ebayopensource.turmeric.runtime.common.impl.internal.pipeline.BaseMessageContextImpl;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.runtime.common.pipeline.InboundMessage;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
import org.ebayopensource.turmeric.runtime.common.types.SOAHeaders;
import org.ebayopensource.turmeric.runtime.spf.security.ServerSecurityContext;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePair;
import org.ebayopensource.turmeric.security.v1.services.OperationKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.security.v1.services.VerifyAccessRequest;
import org.ebayopensource.turmeric.security.v1.services.VerifyAccessResponse;
import org.ebayopensource.turmeric.services.policyenforcementservice.PolicyEnforcementServiceRemoteConsumer;

import com.ebay.kernel.calwrapper.trafficlimiter.TrafficLimiterStatusEnum;

/**
 * This handler calls the PolicyEnforcement service.
 *
 * @author prjande
 */
public class PolicyEnforcementHandler extends BaseHandler {
	
    private static final String DELIMITER = "-";
    private static final String CREDENTIAL_PREFIX = "credential";

    private static final String DEFAULT_IP = "127.0.0.1";
    
    /**
     *  String constant for HTTP Servlet Request name.
     */
    public static final String HTTP_SERVLET_REQUEST = "HttpServletRequest";

    private List<String> m_policyTypeList;
    private Map<String, String> m_credentialNameHeaderMap;
    private static Logger s_logger = LogManager.getInstance(PolicyEnforcementHandler.class);
    private volatile String m_policyEnforcementServiceLocation = null;

    private String m_resourceType = null;
    private String[] m_objectXpath = null;
    private String m_defaultEnvironment = null;
    private String m_envMapper = null;

    @Override
    public void init(InitContext ctx) throws ServiceException {
        super.init(ctx);

        HandlerPreconditions.checkServerSide(ctx, this.getClass()); // Server Side Only

        // get the credentials
        m_credentialNameHeaderMap = new ConcurrentHashMap<String, String>();

        Map<String, String> options = ctx.getOptions();
        Iterator<String> i = options.keySet().iterator();
        while (i.hasNext()) {
            String key = i.next();
            if (key.toLowerCase().startsWith(CREDENTIAL_PREFIX)) {
                String name = key.substring(key.indexOf(DELIMITER) + 1);
                m_credentialNameHeaderMap.put(name.toLowerCase(), options
                        .get(key));
            }
        }

        m_envMapper = options.get("environment-mapper-class-name");//class name of the mapper
        m_policyEnforcementServiceLocation = options.get("service-location");
        
        m_defaultEnvironment = options.get("default-environment");

        String policyTypes = options.get("policy-types");
        if (isEmpty(policyTypes)) {
        	throw new ServiceException(ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_UNEXPECTED_POLICYENFORCEMENT_ERROR, 
					ErrorConstants.ERRORDOMAIN.toString(), 
					new Object[] { "Invalid Policy Type : null or empty." }));
        }

        String[] policyTypeArray = policyTypes.split(",");
        m_policyTypeList = new ArrayList<String>();

        for (String policy : policyTypeArray) {
            policy = policy.trim();
            if (policy == null) {
                logCalMsgForDebug("The policy type can not be null.");
                throw new ServiceException(ErrorUtils.createErrorData(
    					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_UNEXPECTED_POLICYENFORCEMENT_ERROR, 
    					ErrorConstants.ERRORDOMAIN.toString(), 
    					new Object[] { "Invalid Policy Type : " + policy }));
            }
            try {
                m_policyTypeList
                        .add(policy.toUpperCase());
            } catch (IllegalArgumentException e) {
                logCalMsgForDebug(e.getMessage());
                throw new ServiceException(ErrorUtils.createErrorData(
    					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_UNEXPECTED_POLICYENFORCEMENT_ERROR, 
    					ErrorConstants.ERRORDOMAIN.toString(), 
    					new Object[] { "Invalid Policy Type : " + policy }));
                
            }
        }

        String str = options.get("objectXpath");

        m_objectXpath = str != null && str.length() > 0 ? str.trim().split(";") : null;

        // Configured for Object Level auth
        m_resourceType = options.get("resourceType");
        m_resourceType = (isEmpty(m_resourceType)) && m_objectXpath != null ? "OBJECT" : m_resourceType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void invoke(MessageContext ctx) throws ServiceException {

        long startTime = System.nanoTime();

        if (ctx.getSecurityContext().getCredentials() == null
                || ctx.getSecurityContext().getCredentials().isEmpty()) {
            // extract the header from message context and puts it to security
            // context
            Iterator<String> i = m_credentialNameHeaderMap.keySet().iterator();
            while (i.hasNext()) {
                String credName = i.next();
                String headerName = m_credentialNameHeaderMap.get(credName);
                String headerValue = ctx.getRequestMessage()
                        .getTransportHeader(headerName);
                if (headerValue != null) {
                    // header found. Insert to credential map
                    ctx.getSecurityContext().setCredential(credName,
                            headerValue);
                }
            }
        }

        try {
            // make a call thru SOA (remote biding)
            invokeSOAPolicyEnforcementService(ctx);

            // logRemoteEventText(ctx);
        } catch (ServiceException e) {
            throw e;
        } finally {
            // update metrics
            long duration = System.nanoTime() - startTime;
            ((BaseMessageContextImpl) ctx).updateSvcAndOpMetric(
                    SystemMetricDefs.OP_TIME_POLICYENFORCEMENT, duration);
        }
    }

    /**
     * Invoke authentication service via SOA Service call.
     *
     * @param ctx
     * @throws ServiceException
     */
    private void invokeSOAPolicyEnforcementService(MessageContext ctx)
            throws ServiceException {
		PolicyEnforcementServiceRemoteConsumer consumer = new PolicyEnforcementServiceRemoteConsumer(
				m_defaultEnvironment, m_envMapper);

        try {
            if (!(isEmpty(m_policyEnforcementServiceLocation))){
                consumer.setServiceLocation(m_policyEnforcementServiceLocation);
            }
        } catch (MalformedURLException me) {
            logCalMsgForDebug(me.getMessage());
            throw new ServiceException(ErrorUtils.createErrorData(
					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_INVALID_URL_PATH, 
					ErrorConstants.ERRORDOMAIN.toString(), 
					new Object[] { me.getMessage() }));
        }
        

        VerifyAccessRequest request = new VerifyAccessRequest();

        // 1. get the Policy Type
        request.getPolicyType().addAll(m_policyTypeList);

        // 2. get the resource and operation info from the context
        OperationKey opKey = new OperationKey();
        String resourceName = ctx.getAdminName();
        if (resourceName == null) { // For Backward compatibility
            resourceName = ctx.getServiceQName().getLocalPart();
        }
        opKey.setResourceName(resourceName);
        opKey.setOperationName(ctx.getOperationName());
        opKey.setResourceType("SERVICE");
        request.setOperationKey(opKey);

        // setup access objects (required for object level Authz
        setupAccessObjects(ctx, request, resourceName, ctx.getOperationName());

        ServerSecurityContext secCtx = (ServerSecurityContext) ctx
                .getSecurityContext();

        // 3. get credential from context
        Map<String, String> credentials = ctx.getSecurityContext()
                .getCredentials();
        Iterator<String> i = credentials.keySet().iterator();
        while (i.hasNext()) {
            String key = i.next();
            String value = credentials.get(key);
            KeyValuePair cred = new KeyValuePair();
            cred.setKey(key);
            cred.setValue(value);
            request.getCredential().add(cred);
        }

        // 4. get authenticated subjects from context
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

        // 5. retrieve subjects IP from context for Authz request
        SubjectType subject;
        String clientIp = ctx.getClientAddress().getIpAddress();
        if(!isEmpty(clientIp)){
	        subject = new SubjectType();
	        subject.setDomain("IP");
	        subject.setValue(clientIp);
	        request.getSubject().add(subject);
        }

        String sourceClientIp = (String) ctx
                .getProperty(SOAConstants.CTX_PROP_TRANSPORT_CLIENT_SOURCE_IP);
        if (sourceClientIp == null) {
            // This will be in case of a local call.
            sourceClientIp = DEFAULT_IP;
        }

        // set up Source Client Ip
        subject = new SubjectType();
        subject.setDomain("IP");
        subject.setValue(sourceClientIp);
        request.getSubject().add(subject);

        VerifyAccessResponse verifyAccessResponse = null;
        try {
            // call the PolicyEnforcement Service
            verifyAccessResponse = consumer.verifyAccess(request);

            // propagate subject, subjectGroups and extended info list to
            // context
            propagateSecurityInfoFromSOA(secCtx, verifyAccessResponse);

            if (verifyAccessResponse.getAck() == AckValue.FAILURE) {
                List<CommonErrorData> errorDataList = verifyAccessResponse
                        .getErrorMessage().getError();
                String errorText = "Generic authentication error";
                String errorId = "0";
                String subdomain="";
                if (errorDataList.size() > 0) {
                    ErrorData errorData = errorDataList.get(0);
                    errorText = errorData.getMessage();
                    errorId = String.valueOf(errorData.getErrorId());
                    subdomain = errorData.getSubdomain();
                }
                
                setPolicyValidationStatus(secCtx, errorText, errorId);

              	 throw new ServiceException(ErrorUtils.createErrorData(
                			ErrorSubDomainEnum.valueOf(subdomain.toUpperCase()).getErrorDomain(), 
             				ErrorConstants.ERRORDOMAIN.toString(), 
             				new Object[] { request.getOperationKey().getResourceName(),
                              request.getOperationKey().getOperationName(),
                              errorText }));
              	 
            } else {
            	
            	setPolicyValidationStatus(secCtx, null, null);
            
            	if (verifyAccessResponse.getRateLimiterStatus() != null) {
                String status = verifyAccessResponse.getRateLimiterStatus()
                        .value();

	                if (status.equals(TrafficLimiterStatusEnum.SERVE_BLOCK
	                        .getName())) {
	                	throw new ServiceException(ErrorUtils.createErrorData(
	         					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_HANDLER_RL_LIMIT_EXCEED, 
	         					ErrorConstants.ERRORDOMAIN.toString(), 
	         					new Object[] { request.getOperationKey().getResourceName(),
	                                request.getOperationKey().getOperationName()}));
	                }
            	}
            }

        } catch (ServiceException e) {
            logCalMsgForDebug(e.getMessage());
            throw e;
        } catch (Exception e) {
            logCalMsgForDebug(e.getMessage());
            throw new ServiceException(ErrorUtils.createErrorData(
 					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_HANDLER_UNEXPECTED_ERROR, 
 					ErrorConstants.ERRORDOMAIN.toString(), 
 					new Object[] { e.getMessage() }));
        }
    }

	private boolean isEmpty(String str) {
		if(str == null || str.trim().isEmpty())
			return true;
		return false;
	}

	private void setPolicyValidationStatus(ServerSecurityContext secCtx,
			String errorText, String errorId) throws ServiceException {
		for (String policyType : m_policyTypeList) {
			if (policyType.equalsIgnoreCase("AUTHN")) {
				if (errorText != null && errorId != null) {
					secCtx.setAuthnFailure(errorText, errorId, null, null);
				} else {
					secCtx.setAuthnSuccess(null, null, null);
				}
			}
			if (policyType.equalsIgnoreCase("AUTHZ")) {
				if (errorText != null && errorId != null) {
					secCtx.setAuthzFailure(errorText, errorId, null, null);
				} else {
					secCtx.setAuthzSuccess(null, null, null);
				}
			}

		}
	}
    
    private void setupAccessObjects(MessageContext ctx,
            VerifyAccessRequest request, String resourceName,
            String operationName) throws ServiceException {
        // setup access objects
        if (m_objectXpath != null) {
            // setup resource type
            request.setResourceType(m_resourceType);

            // setup access objects
            for (String anObjectXPath : m_objectXpath) {

                String objectXpath = anObjectXPath.trim();
                String[] token = objectXpath.split(":");

                if (token.length < 3) {
                    continue;
                }

                if (resourceName.equals(token[0]) == false)
                    continue;

                if (operationName.equals(token[1])) {
                    String tagName = token[2];
                    ObjectNode node = ((InboundMessage) ctx.getRequestMessage())
                            .getMessageBody();
                    try {
                        List<String> accessObjects = getNodeValue(node, tagName);
                        request.getAccessControlObject().addAll(accessObjects);
                    } catch (Exception e) {
                        s_logger.log(Level.SEVERE, "error: " , e);
                        throw new ServiceException(ErrorUtils.createErrorData(
             					ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_HANDLER_UNEXPECTED_ERROR, 
             					ErrorConstants.ERRORDOMAIN.toString(), 
             					new Object[] { e.getMessage() }));
                    }
                }
            }
        }
    }

    private List<String> getNodeValue(ObjectNode currNode, String xpath)
            throws Exception {

        if (s_logger.isLoggable(Level.INFO)) {
            String str = printNode(currNode, "");
            s_logger.log(Level.SEVERE, "ObjectAuthorizationHandler: 1 " + str);
        }

        List<ObjectNode> list = currNode.getChildNodes();

        List<String> retList = getNodeList(list, xpath);

        return retList;
    }

    private String printNode(ObjectNode currNode, String prefix) {
        StringBuilder sb = new StringBuilder(30);
        try {

            sb.append(currNode.getNodeName().getLocalPart());
            if (currNode.getNodeValue() != null)
                sb.append("=").append(currNode.getNodeValue().toString());

            sb.append("\n");

            List<ObjectNode> list = currNode.getChildNodes();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                sb.append("\n---Node---");
                sb.append(printNode(list.get(i), "  " + prefix));
            }

            List<ObjectNode> list1 = currNode.getAttributes();
            size = list1.size();
            for (int i = 0; i < size; i++) {
                sb.append("\n---Attribute---");
                sb.append(printNode(list1.get(i), "  " + prefix));
            }
        } catch (Exception e) {

        }

        return sb.toString();

    }

    private List<String> getNodeList(List<ObjectNode> list, String xpath)
            throws Exception {
        List<String> ret = new ArrayList<String>();

        int size = list.size();
        for (int i = 0; i < size; i++) {
            ObjectNode currNode = list.get(i);

            String _tagName;

            boolean attFlag = false;

            // System.out.println("xpath=>" + xpath);
            String[] token = xpath.split("/");

            if (token.length == 1) {
                token = xpath.split("@");
                if (token.length > 1)
                    attFlag = true;
            }

            if (token.length == 0)
                continue;

            String tagName = token[0];
            _tagName = currNode.getNodeName().getLocalPart();

            if (!_tagName.equals(tagName))
                continue;

            int index = -1;
            if (attFlag)
                index = xpath.indexOf('@');
            else
                index = xpath.indexOf('/');
            if (index < 0) {
                if (currNode.getNodeValue() != null) {
                    ret.add(currNode.getNodeValue().toString());
                }
            } else {
                String restOfXpath = xpath.substring(index + 1);
                List<ObjectNode> childNodeIter = null;
                if (attFlag) {
                    childNodeIter = currNode.getAttributes();
                } else {
                    childNodeIter = currNode.getChildNodes();
                }

                List<? extends String> ret1 = getNodeList(childNodeIter,
                        restOfXpath);
                if (ret1 != null) {
                    ret.addAll(ret1);
                }
            }
        }

        return ret;
    }

    // add the subjects, resolved subject groups and extended info from
    // PolicyEnforcement
    // response to the security context
    private void propagateSecurityInfoFromSOA(ServerSecurityContext secCtx,
            VerifyAccessResponse response) throws ServiceException {

        // add authenticated subjects to context
        if (response.getAuthenticatedSubject() != null) {
            List<SubjectType> subjects = response.getAuthenticatedSubject();
            Iterator<SubjectType> i = subjects.iterator();
            while (i.hasNext()) {
                SubjectType sub = i.next();
                secCtx.setAuthnSubject(sub.getDomain(), sub.getValue());
            }
        }

        // add resolved subject groups to context
        if (response.getResolvedSubjectGroup() != null) {
            List<SubjectGroupType> resolvedSubjectGroups = response
                    .getResolvedSubjectGroup();
            Iterator<SubjectGroupType> iter = resolvedSubjectGroups.iterator();
            while (iter.hasNext()) {
                SubjectGroupType resolvedSubjectGroup = iter.next();
                secCtx.setResolvedSubjectGroup(resolvedSubjectGroup.getName());
            }
        }

        // add extended info to context
        if (response.getExtendedInfo() != null) {
            List<KeyValuePair> extendedInfo = response.getExtendedInfo();
            if (extendedInfo != null) {
                if (extendedInfo.size() > 0) {
                    for (KeyValuePair kvpair : extendedInfo) {
                        secCtx.setAuthnCustomData(kvpair.getKey(), kvpair
                                .getValue());
                    }
                }
            }
        }

    }

    private static void logCalMsgForDebug(String msg) {
        if (s_logger.isLoggable(Level.INFO)) {
            s_logger.log(Level.SEVERE, "PolicyEnforcementDebugInfo 0 " + msg);
        }
    }

    
    private enum ErrorSubDomainEnum {
    	POLICYENFORCEMENT,
    	AUTHZ,
    	AUTHN,
    	CLIENTTOKENRETRIEVAL,
    	RL;
    	
    	public String getErrorDomain(){
    		if(this.equals(POLICYENFORCEMENT)){
    			return ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_HANDLER_PES_FAILED;
    		}
    		
    		if(this.equals(AUTHZ)){
    			return ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_HANDLER_AUTHZ_FAILED;
    		}
    		
    		if(this.equals(AUTHN)){
    			return ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_HANDLER_AUTHN_FAILED;
    		}
    		
    		if(this.equals(CLIENTTOKENRETRIEVAL)){
    			return ErrorConstants.SVC_SECURITY_CLIENTTOKENRETRIEVAL_HANDLER_ERROR;
    		}

    		if(this.equals(RL)){
    			return ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_HANDLER_RL_FAILED;
    		}
    		
    		//default
    		return ErrorConstants.SVC_SECURITY_POLICYENFORCEMENT_HANDLER_PES_FAILED;
    	}
    }
}
