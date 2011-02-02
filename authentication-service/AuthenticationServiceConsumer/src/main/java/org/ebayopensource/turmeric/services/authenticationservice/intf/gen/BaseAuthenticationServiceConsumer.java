/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/

package org.ebayopensource.turmeric.services.authenticationservice.intf.gen;

import java.net.MalformedURLException;
import java.net.URL;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceRuntimeException;
import org.ebayopensource.turmeric.runtime.common.types.Cookie;
import org.ebayopensource.turmeric.runtime.common.types.SOAHeaders;
import org.ebayopensource.turmeric.runtime.sif.service.Service;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;
import org.ebayopensource.turmeric.security.v1.services.AuthenticateRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthenticateResponseType;

import org.ebayopensource.turmeric.services.authenticationservice.intf.AuthenticationService;


/**
 * Note : Generated file, any changes will be lost upon regeneration.
 * 
 */
public class BaseAuthenticationServiceConsumer {

    private URL m_serviceLocation = null;
    private AuthenticationService m_proxy = null;
    private String m_authToken = null;
    private Cookie[] m_cookies;

    public BaseAuthenticationServiceConsumer() {
    }

    protected void setServiceLocation(String serviceLocation)
        throws MalformedURLException
    {
        m_serviceLocation = new URL(serviceLocation);
    }

    private void setUserProvidedSecurityCredentials(Service service) {
        if (m_authToken!= null) {
            service.setSessionTransportHeader(SOAHeaders.AUTH_TOKEN, m_authToken);
        }
        if (m_cookies!= null) {
            for (int i = 0; (i<m_cookies.length); i ++) {
                service.setCookie(m_cookies[i]);
            }
        }
    }

    /**
     * Use this method to set User Credentials (Token) 
     * 
     */
    protected void setAuthToken(String authToken) {
        m_authToken = authToken;
    }

    /**
     * Use this method to set User Credentials (Cookie)
     * 
     */
    protected void setCookies(Cookie[] cookies) {
        m_cookies = cookies;
    }

    protected AuthenticationService getProxy()
        throws ServiceException
    {
        if (m_proxy == null) {
            String svcAdminName = "AuthenticationService";
            Service service = ServiceFactory.create(svcAdminName, "AuthenticationService", m_serviceLocation);
            m_proxy = service.getProxy();
            setUserProvidedSecurityCredentials(service);
        }
        return m_proxy;
    }

    public AuthenticateResponseType authenticate(AuthenticateRequestType param0) {
        AuthenticateResponseType result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.authenticate(param0);
        return result;
    }

}
