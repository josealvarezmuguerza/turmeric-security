/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/

package org.ebayopensource.turmeric.calculatortestservice.intf.gen;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.ebayopensource.turmeric.calculatortestservice.intf.AsyncCalculatorTestService;
import org.ebayopensource.turmeric.calcultor.v1.services.DoMultiplicationRequest;
import org.ebayopensource.turmeric.calcultor.v1.services.DoMultiplicationResponse;
import org.ebayopensource.turmeric.calcultor.v1.services.GetAdditionRequest;
import org.ebayopensource.turmeric.calcultor.v1.services.GetAdditionResponse;
import org.ebayopensource.turmeric.calcultor.v1.services.GetDivisionRequest;
import org.ebayopensource.turmeric.calcultor.v1.services.GetDivisionResponse;
import org.ebayopensource.turmeric.calcultor.v1.services.GetSubtractionRequest;
import org.ebayopensource.turmeric.calcultor.v1.services.GetSubtractionResponse;
import org.ebayopensource.turmeric.calcultor.v1.services.GetVersionRequest;
import org.ebayopensource.turmeric.calcultor.v1.services.GetVersionResponse;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceRuntimeException;
import org.ebayopensource.turmeric.runtime.common.types.Cookie;
import org.ebayopensource.turmeric.runtime.common.types.SOAHeaders;
import org.ebayopensource.turmeric.runtime.sif.service.Service;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;


/**
 * Note : Generated file, any changes will be lost upon regeneration.
 * 
 */
public class BaseCalculatorTestServiceConsumer {

    private URL m_serviceLocation = null;
    private AsyncCalculatorTestService m_proxy = null;
    private String m_authToken = null;
    private Cookie[] m_cookies;

    public BaseCalculatorTestServiceConsumer() {
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

    protected AsyncCalculatorTestService getProxy()
        throws ServiceException
    {
        String svcAdminName = "CalculatorTestService";
        Service service = ServiceFactory.create(svcAdminName, "CalculatorTestService", m_serviceLocation);
        m_proxy = service.getProxy();
        setUserProvidedSecurityCredentials(service);
        return m_proxy;
    }

    public Future<?> getAdditionAsync(GetAdditionRequest param0, AsyncHandler<GetAdditionResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getAdditionAsync(param0, param1);
        return result;
    }

    public Response<GetAdditionResponse> getAdditionAsync(GetAdditionRequest param0) {
        Response<GetAdditionResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getAdditionAsync(param0);
        return result;
    }

    public Future<?> getVersionAsync(GetVersionRequest param0, AsyncHandler<GetVersionResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getVersionAsync(param0, param1);
        return result;
    }

    public Response<GetVersionResponse> getVersionAsync(GetVersionRequest param0) {
        Response<GetVersionResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getVersionAsync(param0);
        return result;
    }

    public Future<?> getDivisionAsync(GetDivisionRequest param0, AsyncHandler<GetDivisionResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getDivisionAsync(param0, param1);
        return result;
    }

    public Response<GetDivisionResponse> getDivisionAsync(GetDivisionRequest param0) {
        Response<GetDivisionResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getDivisionAsync(param0);
        return result;
    }

    public Future<?> doMultiplicationAsync(DoMultiplicationRequest param0, AsyncHandler<DoMultiplicationResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.doMultiplicationAsync(param0, param1);
        return result;
    }

    public Response<DoMultiplicationResponse> doMultiplicationAsync(DoMultiplicationRequest param0) {
        Response<DoMultiplicationResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.doMultiplicationAsync(param0);
        return result;
    }

    public Future<?> getSubtractionAsync(GetSubtractionRequest param0, AsyncHandler<GetSubtractionResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getSubtractionAsync(param0, param1);
        return result;
    }

    public Response<GetSubtractionResponse> getSubtractionAsync(GetSubtractionRequest param0) {
        Response<GetSubtractionResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getSubtractionAsync(param0);
        return result;
    }

    public GetAdditionResponse getAddition(GetAdditionRequest param0) {
        GetAdditionResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getAddition(param0);
        return result;
    }

    public GetVersionResponse getVersion(GetVersionRequest param0) {
        GetVersionResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getVersion(param0);
        return result;
    }

    public GetDivisionResponse getDivision(GetDivisionRequest param0) {
        GetDivisionResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getDivision(param0);
        return result;
    }

    public DoMultiplicationResponse doMultiplication(DoMultiplicationRequest param0) {
        DoMultiplicationResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.doMultiplication(param0);
        return result;
    }

    public GetSubtractionResponse getSubtraction(GetSubtractionRequest param0) {
        GetSubtractionResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getSubtraction(param0);
        return result;
    }

}
