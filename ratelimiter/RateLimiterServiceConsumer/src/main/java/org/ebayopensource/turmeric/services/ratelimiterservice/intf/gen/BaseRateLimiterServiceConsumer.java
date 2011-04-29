/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/

package org.ebayopensource.turmeric.services.ratelimiterservice.intf.gen;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceRuntimeException;
import org.ebayopensource.turmeric.runtime.common.types.Cookie;
import org.ebayopensource.turmeric.runtime.common.types.SOAHeaders;
import org.ebayopensource.turmeric.runtime.sif.service.Service;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedResponse;

import org.ebayopensource.turmeric.services.ratelimiterservice.intf.AsyncRateLimiterService;


/**
 * Note : Generated file, any changes will be lost upon regeneration.
 * 
 */
public class BaseRateLimiterServiceConsumer {

    private URL m_serviceLocation = null;
    private AsyncRateLimiterService m_proxy = null;
    private String m_authToken = null;
    private Cookie[] m_cookies;

    /**
     * Instantiates a new base rate limiter service consumer.
     */
    public BaseRateLimiterServiceConsumer() {
    }

    /**
     * Sets the service location.
     *
     * @param serviceLocation the new service location
     * @throws MalformedURLException the malformed url exception
     */
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
     * Use this method to set User Credentials (Token).
     *
     * @param authToken the new auth token
     */
    protected void setAuthToken(String authToken) {
        m_authToken = authToken;
    }

    /**
     * Use this method to set User Credentials (Cookie).
     *
     * @param cookies the new cookies
     */
    protected void setCookies(Cookie[] cookies) {
        m_cookies = cookies;
    }

    /**
     * Gets the proxy.
     *
     * @return the proxy
     * @throws ServiceException the service exception
     */
    protected AsyncRateLimiterService getProxy()
        throws ServiceException
    {
        String svcAdminName = "RateLimiterService";
        Service service = ServiceFactory.create(svcAdminName, "RateLimiterService", m_serviceLocation);
        m_proxy = service.getProxy();
        setUserProvidedSecurityCredentials(service);
        return m_proxy;
    }

    /**
     * Checks if is rate limited async.
     *
     * @param param0 the param0
     * @param param1 the param1
     * @return the future
     */
    public Future<?> isRateLimitedAsync(IsRateLimitedRequest param0, AsyncHandler<IsRateLimitedResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.isRateLimitedAsync(param0, param1);
        return result;
    }

    /**
     * Checks if is rate limited async.
     *
     * @param param0 the param0
     * @return the response
     */
    public Response<IsRateLimitedResponse> isRateLimitedAsync(IsRateLimitedRequest param0) {
        Response<IsRateLimitedResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.isRateLimitedAsync(param0);
        return result;
    }

    /**
     * Checks if is rate limited.
     *
     * @param param0 the param0
     * @return the checks if is rate limited response
     */
    public IsRateLimitedResponse isRateLimited(IsRateLimitedRequest param0) {
        IsRateLimitedResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.isRateLimited(param0);
        return result;
    }

}
