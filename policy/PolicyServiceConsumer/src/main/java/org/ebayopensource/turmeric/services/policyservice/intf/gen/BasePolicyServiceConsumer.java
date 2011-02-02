/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/

package org.ebayopensource.turmeric.services.policyservice.intf.gen;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;

import org.ebayopensource.turmeric.policyservice.intf.AsyncPolicyService;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceRuntimeException;
import org.ebayopensource.turmeric.runtime.common.types.Cookie;
import org.ebayopensource.turmeric.runtime.common.types.SOAHeaders;
import org.ebayopensource.turmeric.runtime.sif.service.Service;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceInvokerOptions;
import org.ebayopensource.turmeric.security.v1.services.CreateExternalSubjectReferenceRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateExternalSubjectReferencesResponse;
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectsResponse;
import org.ebayopensource.turmeric.security.v1.services.DeletePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.DeletePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.DeleteResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.DeleteResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.DeleteSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.DeleteSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.DeleteSubjectsRequest;
import org.ebayopensource.turmeric.security.v1.services.DeleteSubjectsResponse;
import org.ebayopensource.turmeric.security.v1.services.DisablePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.DisablePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.EnablePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.EnablePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.FindExternalSubjectsRequest;
import org.ebayopensource.turmeric.security.v1.services.FindExternalSubjectsResponse;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesResponse;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectsRequest;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectsResponse;
import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.GetEntityHistoryRequest;
import org.ebayopensource.turmeric.security.v1.services.GetEntityHistoryResponse;
import org.ebayopensource.turmeric.security.v1.services.GetMetaDataRequest;
import org.ebayopensource.turmeric.security.v1.services.GetMetaDataResponse;
import org.ebayopensource.turmeric.security.v1.services.GetOperationsRequest;
import org.ebayopensource.turmeric.security.v1.services.GetOperationsResponse;
import org.ebayopensource.turmeric.security.v1.services.GetResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.GetResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.UpdatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.UpdatePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.UpdateResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.UpdateResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.UpdateSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.UpdateSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.ValidatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.ValidatePolicyResponse;

/**
 * Note : Generated file, any changes will be lost upon regeneration.
 * This class is not thread safe
 * 
 */
public class BasePolicyServiceConsumer {

    private URL m_serviceLocation = null;
    private final static String SVC_ADMIN_NAME = "PolicyService";
    private String m_clientName = "PolicyService";
    private String m_environment;
    private AsyncPolicyService m_proxy = null;
    private String m_authToken = null;
    private Cookie[] m_cookies;
    private Service m_service = null;

    public BasePolicyServiceConsumer() {
    }

    public BasePolicyServiceConsumer(String clientName)
        throws ServiceException
    {
        if (clientName == null) {
            throw new ServiceException("clientName can not be null");
        }
        m_clientName = clientName;
    }

    public BasePolicyServiceConsumer(String clientName, String environment)
        throws ServiceException
    {
        if (environment == null) {
            throw new ServiceException("environment can not be null");
        }
        if (clientName == null) {
            throw new ServiceException("clientName can not be null");
        }
        m_clientName = clientName;
        m_environment = environment;
    }

    /**
     * Use this method to initialize ConsumerApp after creating a Consumer instance
     * 
     */
    public void init()
        throws ServiceException
    {
        getService();
    }

    protected void setServiceLocation(String serviceLocation)
        throws MalformedURLException
    {
        m_serviceLocation = new URL(serviceLocation);
        if (m_service!= null) {
            m_service.setServiceLocation(m_serviceLocation);
        }
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

    /**
     * Use this method to get the Invoker Options on the Service and set them to user-preferences
     * 
     */
    public ServiceInvokerOptions getServiceInvokerOptions()
        throws ServiceException
    {
        m_service = getService();
        return m_service.getInvokerOptions();
    }

    protected AsyncPolicyService getProxy()
        throws ServiceException
    {
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
        if (m_service == null) {
            m_service = ServiceFactory.create(SVC_ADMIN_NAME, m_environment, m_clientName, m_serviceLocation);
        }
        setUserProvidedSecurityCredentials(m_service);
        return m_service;
    }

    public Future<?> deletePolicyAsync(DeletePolicyRequest param0, AsyncHandler<DeletePolicyResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.deletePolicyAsync(param0, param1);
        return result;
    }

    public Response<DeletePolicyResponse> deletePolicyAsync(DeletePolicyRequest param0) {
        Response<DeletePolicyResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.deletePolicyAsync(param0);
        return result;
    }

    public Future<?> findPoliciesAsync(FindPoliciesRequest param0, AsyncHandler<FindPoliciesResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.findPoliciesAsync(param0, param1);
        return result;
    }

    public Response<FindPoliciesResponse> findPoliciesAsync(FindPoliciesRequest param0) {
        Response<FindPoliciesResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.findPoliciesAsync(param0);
        return result;
    }

    public Future<?> getAuthenticationPolicyAsync(GetAuthenticationPolicyRequest param0, AsyncHandler<GetAuthenticationPolicyResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getAuthenticationPolicyAsync(param0, param1);
        return result;
    }

    public Response<GetAuthenticationPolicyResponse> getAuthenticationPolicyAsync(GetAuthenticationPolicyRequest param0) {
        Response<GetAuthenticationPolicyResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getAuthenticationPolicyAsync(param0);
        return result;
    }

    public Future<?> getResourcesAsync(GetResourcesRequest param0, AsyncHandler<GetResourcesResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getResourcesAsync(param0, param1);
        return result;
    }

    public Response<GetResourcesResponse> getResourcesAsync(GetResourcesRequest param0) {
        Response<GetResourcesResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getResourcesAsync(param0);
        return result;
    }

    public Future<?> createResourcesAsync(CreateResourcesRequest param0, AsyncHandler<CreateResourcesResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createResourcesAsync(param0, param1);
        return result;
    }

    public Response<CreateResourcesResponse> createResourcesAsync(CreateResourcesRequest param0) {
        Response<CreateResourcesResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createResourcesAsync(param0);
        return result;
    }

    public Future<?> createExternalSubjectReferencesAsync(CreateExternalSubjectReferenceRequest param0, AsyncHandler<CreateExternalSubjectReferencesResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createExternalSubjectReferencesAsync(param0, param1);
        return result;
    }

    public Response<CreateExternalSubjectReferencesResponse> createExternalSubjectReferencesAsync(CreateExternalSubjectReferenceRequest param0) {
        Response<CreateExternalSubjectReferencesResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createExternalSubjectReferencesAsync(param0);
        return result;
    }

    public Future<?> createPolicyAsync(CreatePolicyRequest param0, AsyncHandler<CreatePolicyResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createPolicyAsync(param0, param1);
        return result;
    }

    public Response<CreatePolicyResponse> createPolicyAsync(CreatePolicyRequest param0) {
        Response<CreatePolicyResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createPolicyAsync(param0);
        return result;
    }

    public Future<?> findExternalSubjectsAsync(FindExternalSubjectsRequest param0, AsyncHandler<FindExternalSubjectsResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.findExternalSubjectsAsync(param0, param1);
        return result;
    }

    public Response<FindExternalSubjectsResponse> findExternalSubjectsAsync(FindExternalSubjectsRequest param0) {
        Response<FindExternalSubjectsResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.findExternalSubjectsAsync(param0);
        return result;
    }

    public Future<?> getMetaDataAsync(GetMetaDataRequest param0, AsyncHandler<GetMetaDataResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getMetaDataAsync(param0, param1);
        return result;
    }

    public Response<GetMetaDataResponse> getMetaDataAsync(GetMetaDataRequest param0) {
        Response<GetMetaDataResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getMetaDataAsync(param0);
        return result;
    }

    public Future<?> deleteSubjectGroupsAsync(DeleteSubjectGroupsRequest param0, AsyncHandler<DeleteSubjectGroupsResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.deleteSubjectGroupsAsync(param0, param1);
        return result;
    }

    public Response<DeleteSubjectGroupsResponse> deleteSubjectGroupsAsync(DeleteSubjectGroupsRequest param0) {
        Response<DeleteSubjectGroupsResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.deleteSubjectGroupsAsync(param0);
        return result;
    }

    public Future<?> findSubjectGroupsAsync(FindSubjectGroupsRequest param0, AsyncHandler<FindSubjectGroupsResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.findSubjectGroupsAsync(param0, param1);
        return result;
    }

    public Response<FindSubjectGroupsResponse> findSubjectGroupsAsync(FindSubjectGroupsRequest param0) {
        Response<FindSubjectGroupsResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.findSubjectGroupsAsync(param0);
        return result;
    }

    public Future<?> createSubjectGroupsAsync(CreateSubjectGroupsRequest param0, AsyncHandler<CreateSubjectGroupsResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createSubjectGroupsAsync(param0, param1);
        return result;
    }

    public Response<CreateSubjectGroupsResponse> createSubjectGroupsAsync(CreateSubjectGroupsRequest param0) {
        Response<CreateSubjectGroupsResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createSubjectGroupsAsync(param0);
        return result;
    }

    public Future<?> updatePolicyAsync(UpdatePolicyRequest param0, AsyncHandler<UpdatePolicyResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.updatePolicyAsync(param0, param1);
        return result;
    }

    public Response<UpdatePolicyResponse> updatePolicyAsync(UpdatePolicyRequest param0) {
        Response<UpdatePolicyResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.updatePolicyAsync(param0);
        return result;
    }

    public Future<?> findSubjectsAsync(FindSubjectsRequest param0, AsyncHandler<FindSubjectsResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.findSubjectsAsync(param0, param1);
        return result;
    }

    public Response<FindSubjectsResponse> findSubjectsAsync(FindSubjectsRequest param0) {
        Response<FindSubjectsResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.findSubjectsAsync(param0);
        return result;
    }

    public Future<?> createSubjectsAsync(CreateSubjectsRequest param0, AsyncHandler<CreateSubjectsResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createSubjectsAsync(param0, param1);
        return result;
    }

    public Response<CreateSubjectsResponse> createSubjectsAsync(CreateSubjectsRequest param0) {
        Response<CreateSubjectsResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createSubjectsAsync(param0);
        return result;
    }

    public Future<?> getEntityHistoryAsync(GetEntityHistoryRequest param0, AsyncHandler<GetEntityHistoryResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getEntityHistoryAsync(param0, param1);
        return result;
    }

    public Response<GetEntityHistoryResponse> getEntityHistoryAsync(GetEntityHistoryRequest param0) {
        Response<GetEntityHistoryResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getEntityHistoryAsync(param0);
        return result;
    }

    public Future<?> updateSubjectGroupsAsync(UpdateSubjectGroupsRequest param0, AsyncHandler<UpdateSubjectGroupsResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.updateSubjectGroupsAsync(param0, param1);
        return result;
    }

    public Response<UpdateSubjectGroupsResponse> updateSubjectGroupsAsync(UpdateSubjectGroupsRequest param0) {
        Response<UpdateSubjectGroupsResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.updateSubjectGroupsAsync(param0);
        return result;
    }

    public Future<?> updateResourcesAsync(UpdateResourcesRequest param0, AsyncHandler<UpdateResourcesResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.updateResourcesAsync(param0, param1);
        return result;
    }

    public Response<UpdateResourcesResponse> updateResourcesAsync(UpdateResourcesRequest param0) {
        Response<UpdateResourcesResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.updateResourcesAsync(param0);
        return result;
    }

    public Future<?> getOperationsAsync(GetOperationsRequest param0, AsyncHandler<GetOperationsResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getOperationsAsync(param0, param1);
        return result;
    }

    public Response<GetOperationsResponse> getOperationsAsync(GetOperationsRequest param0) {
        Response<GetOperationsResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getOperationsAsync(param0);
        return result;
    }

    public Future<?> deleteSubjectsAsync(DeleteSubjectsRequest param0, AsyncHandler<DeleteSubjectsResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.deleteSubjectsAsync(param0, param1);
        return result;
    }

    public Response<DeleteSubjectsResponse> deleteSubjectsAsync(DeleteSubjectsRequest param0) {
        Response<DeleteSubjectsResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.deleteSubjectsAsync(param0);
        return result;
    }

    public Future<?> validatePolicyAsync(ValidatePolicyRequest param0, AsyncHandler<ValidatePolicyResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.validatePolicyAsync(param0, param1);
        return result;
    }

    public Response<ValidatePolicyResponse> validatePolicyAsync(ValidatePolicyRequest param0) {
        Response<ValidatePolicyResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.validatePolicyAsync(param0);
        return result;
    }

    public Future<?> deleteResourcesAsync(DeleteResourcesRequest param0, AsyncHandler<DeleteResourcesResponse> param1) {
        Future<?> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.deleteResourcesAsync(param0, param1);
        return result;
    }

    public Response<DeleteResourcesResponse> deleteResourcesAsync(DeleteResourcesRequest param0) {
        Response<DeleteResourcesResponse> result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.deleteResourcesAsync(param0);
        return result;
    }

    public DeletePolicyResponse deletePolicy(DeletePolicyRequest param0) {
        DeletePolicyResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.deletePolicy(param0);
        return result;
    }

    public FindPoliciesResponse findPolicies(FindPoliciesRequest param0) {
        FindPoliciesResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.findPolicies(param0);
        return result;
    }

    public GetAuthenticationPolicyResponse getAuthenticationPolicy(GetAuthenticationPolicyRequest param0) {
        GetAuthenticationPolicyResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getAuthenticationPolicy(param0);
        return result;
    }

    public GetResourcesResponse getResources(GetResourcesRequest param0) {
        GetResourcesResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getResources(param0);
        return result;
    }

    public CreateResourcesResponse createResources(CreateResourcesRequest param0) {
        CreateResourcesResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createResources(param0);
        return result;
    }

    public CreateExternalSubjectReferencesResponse createExternalSubjectReferences(CreateExternalSubjectReferenceRequest param0) {
        CreateExternalSubjectReferencesResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createExternalSubjectReferences(param0);
        return result;
    }

    public CreatePolicyResponse createPolicy(CreatePolicyRequest param0) {
        CreatePolicyResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createPolicy(param0);
        return result;
    }

    public FindExternalSubjectsResponse findExternalSubjects(FindExternalSubjectsRequest param0) {
        FindExternalSubjectsResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.findExternalSubjects(param0);
        return result;
    }

    public GetMetaDataResponse getMetaData(GetMetaDataRequest param0) {
        GetMetaDataResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getMetaData(param0);
        return result;
    }

    public DeleteSubjectGroupsResponse deleteSubjectGroups(DeleteSubjectGroupsRequest param0) {
        DeleteSubjectGroupsResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.deleteSubjectGroups(param0);
        return result;
    }

    public FindSubjectGroupsResponse findSubjectGroups(FindSubjectGroupsRequest param0) {
        FindSubjectGroupsResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.findSubjectGroups(param0);
        return result;
    }

    public CreateSubjectGroupsResponse createSubjectGroups(CreateSubjectGroupsRequest param0) {
        CreateSubjectGroupsResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createSubjectGroups(param0);
        return result;
    }

    public UpdatePolicyResponse updatePolicy(UpdatePolicyRequest param0) {
        UpdatePolicyResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.updatePolicy(param0);
        return result;
    }

    public FindSubjectsResponse findSubjects(FindSubjectsRequest param0) {
        FindSubjectsResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.findSubjects(param0);
        return result;
    }

    public CreateSubjectsResponse createSubjects(CreateSubjectsRequest param0) {
        CreateSubjectsResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.createSubjects(param0);
        return result;
    }

    public GetEntityHistoryResponse getEntityHistory(GetEntityHistoryRequest param0) {
        GetEntityHistoryResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getEntityHistory(param0);
        return result;
    }

    public UpdateSubjectGroupsResponse updateSubjectGroups(UpdateSubjectGroupsRequest param0) {
        UpdateSubjectGroupsResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.updateSubjectGroups(param0);
        return result;
    }

    public UpdateResourcesResponse updateResources(UpdateResourcesRequest param0) {
        UpdateResourcesResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.updateResources(param0);
        return result;
    }

    public GetOperationsResponse getOperations(GetOperationsRequest param0) {
        GetOperationsResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.getOperations(param0);
        return result;
    }

    public DeleteSubjectsResponse deleteSubjects(DeleteSubjectsRequest param0) {
        DeleteSubjectsResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.deleteSubjects(param0);
        return result;
    }

    public ValidatePolicyResponse validatePolicy(ValidatePolicyRequest param0) {
        ValidatePolicyResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.validatePolicy(param0);
        return result;
    }

    public DeleteResourcesResponse deleteResources(DeleteResourcesRequest param0) {
        DeleteResourcesResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.deleteResources(param0);
        return result;
    }
    
    public EnablePolicyResponse enablePolicy(EnablePolicyRequest param0) {
    	EnablePolicyResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.enablePolicy(param0);
        return result;
    }
    
    public DisablePolicyResponse disablePolicy(DisablePolicyRequest param0) {
    	DisablePolicyResponse result = null;
        try {
            m_proxy = getProxy();
        } catch (ServiceException serviceException) {
            throw ServiceRuntimeException.wrap(serviceException);
        }
        result = m_proxy.disablePolicy(param0);
        return result;
    }

}
