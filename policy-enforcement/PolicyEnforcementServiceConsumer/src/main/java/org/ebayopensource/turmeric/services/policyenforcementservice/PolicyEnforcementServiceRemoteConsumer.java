/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *    
 * http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.services.policyenforcementservice;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.utils.ReflectionUtils;
import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
import org.ebayopensource.turmeric.runtime.common.types.SOAHeaders;
import org.ebayopensource.turmeric.runtime.sif.service.EnvironmentMapper;
import org.ebayopensource.turmeric.runtime.sif.service.Service;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceInvokerOptions;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;

import org.ebayopensource.turmeric.services.policyenforcementservice.gen.BasePolicyEnforcementServiceConsumer;
import org.ebayopensource.turmeric.services.policyenforcementservice.intf.AsyncPolicyEnforcementService;

/**
 * This Remote consumer is used by PolicyEnforcement Handler
 * for making the remote calls to Policy Enforcement.
 * 
 * @author prjande
 */
public class PolicyEnforcementServiceRemoteConsumer extends BasePolicyEnforcementServiceConsumer {

	private String m_messageProtocol = null;

	private URL m_serviceLocation = null;
	private Service m_service = null;
	private AsyncPolicyEnforcementService m_proxy = null;
	private String m_defEnvName = null;
	private EnvironmentMapper m_envMapper = null;
	private static Logger s_logger = LogManager.getInstance(PolicyEnforcementServiceRemoteConsumer.class);
	private final static String DEFAULT_ENV = "production";
	
	
	public PolicyEnforcementServiceRemoteConsumer() {
		super();
	}
    
	public PolicyEnforcementServiceRemoteConsumer(String mEnvName, String mEnvMapper) {
		super();
		m_defEnvName = mEnvName;
		
		if (mEnvMapper != null && !mEnvMapper.isEmpty())
		{
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			try {
				m_envMapper = ReflectionUtils.createInstance(mEnvMapper, EnvironmentMapper.class, cl);
			} catch (ServiceException e)
			{
				logCalMsg("Environemnt mapper " + mEnvMapper + " Class not loaded !");
			}
		}
		
	}
    
    
    
    /**
     * Set the Message Protocol.    
     * 
     * @param Message Protocol
     */
    public void setMessageProtocol(String messageProtocol) {
    	m_messageProtocol = messageProtocol;
    }
        
    
    /**
     * By default the URL will be set as specified in configuration file 
     */
    @Override
    public void setServiceLocation(String serviceLocation) throws MalformedURLException {
         m_serviceLocation = new URL(serviceLocation);
    }
    
    @Override
    protected AsyncPolicyEnforcementService getProxy() throws ServiceException {    	
    	m_service = getService();
        m_proxy = m_service.getProxy();
        return m_proxy;
    }
    
    /**
     * Method returns an instance of Service which has been initilized for this Consumer
     * 
     */
    public Service getService()
        throws ServiceException
    {
        String svcAdminName = "PolicyEnforcementService";
        
        String env = m_envMapper == null ? null : m_envMapper.getDeploymentEnvironment();
        if(env == null){
        	env = m_defEnvName;
        }
        
        if(env == null || env.isEmpty()){
        	env = DEFAULT_ENV;
        }
        
		if (m_service == null) {
			m_service = ServiceFactory.create(svcAdminName, env,
					"PolicyEnforcementService", m_serviceLocation);
		}
        
        ServiceInvokerOptions options = m_service.getInvokerOptions();
        options.setTransportName("HTTP11");
        
        if(m_messageProtocol != null) {
        	if (m_messageProtocol.equalsIgnoreCase("SOAP11")) {
                options.setMessageProtocolName(SOAConstants.MSG_PROTOCOL_SOAP_11);
            } else {
                options.setMessageProtocolName(SOAConstants.MSG_PROTOCOL_SOAP_12);
            }
        }        
                        
        return m_service;
    }
    
    
    private static void logCalMsg(String msg) {
		if (s_logger.isLoggable(Level.INFO)) {
			s_logger.log(Level.SEVERE, "PolicyEnforcementDebugInfo : " + msg);
		}
	}
    	
}
