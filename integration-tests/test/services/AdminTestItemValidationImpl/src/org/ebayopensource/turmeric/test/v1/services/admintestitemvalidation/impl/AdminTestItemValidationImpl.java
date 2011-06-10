/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.test.v1.services.admintestitemvalidation.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.test.v1.services.AddItemRequest;
import org.ebayopensource.test.v1.services.AddItemResponse;
import org.ebayopensource.test.v1.services.CallInitializerRequest;
import org.ebayopensource.test.v1.services.CallInitializerResponse;
import org.ebayopensource.test.v1.services.GetAuthzCacheKeysRequest;
import org.ebayopensource.test.v1.services.GetAuthzCacheKeysResponse;
import org.ebayopensource.test.v1.services.GetItemRequest;
import org.ebayopensource.test.v1.services.GetItemResponse;
import org.ebayopensource.test.v1.services.GetVersionRequest;
import org.ebayopensource.test.v1.services.GetVersionResponse;
import org.ebayopensource.test.v1.services.ResOpType;
import org.ebayopensource.test.v1.services.ResourceInfoType;
import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.runtime.spf.impl.internal.config.Initializer;
import org.ebayopensource.turmeric.runtime.spf.impl.internal.config.Initializer.InitializerException;
import org.ebayopensource.turmeric.services.authorizationservice.impl.biz.InternalAuthorizationServiceImpl;
import org.ebayopensource.turmeric.services.authorizationservice.impl.util.EncodingUtils;
import org.ebayopensource.turmeric.services.authorizationservice.impl.util.ResOpKey;
import org.ebayopensource.turmeric.test.v1.services.admintestitemvalidation.intf.AdminTestItemValidation;

public class AdminTestItemValidationImpl
    implements AdminTestItemValidation
{

	private static final Logger LOGGER = LogManager.getInstance(AdminTestItemValidationImpl.class);

    public GetVersionResponse getVersion(GetVersionRequest param0) {
        return null;
    }

    public AddItemResponse addItem(AddItemRequest param0) {
    	AddItemResponse resp = new AddItemResponse();
    	resp.setAck(AckValue.SUCCESS);
    	resp.setItemId("1111111");
        return resp;
    }

    public GetItemResponse getItem(GetItemRequest param0) {
    	GetItemResponse resp = new GetItemResponse();
    	resp.setAck(AckValue.SUCCESS);
    	resp.setItemName("IPOD");
        return resp;
    }

	@Override
	public GetAuthzCacheKeysResponse getAuthzCacheKeys(
			GetAuthzCacheKeysRequest getAuthzCacheKeysRequest) {
		String resName = getAuthzCacheKeysRequest.getResOp().getResourceName();
		String resType = getAuthzCacheKeysRequest.getResOp().getResourceType();
		String opName = getAuthzCacheKeysRequest.getResOp().getOperationName();
		String sgName = getAuthzCacheKeysRequest.getSGName();
		GetAuthzCacheKeysResponse response = new GetAuthzCacheKeysResponse();
		InternalAuthorizationServiceImpl svc = InternalAuthorizationServiceImpl.getInstance();
		
		for (String key : svc.getCachedPolicyResources()) {
			ResOpKey resOpKey = EncodingUtils.decodeResOpKey(key);
			final String resourceName = resOpKey.getResourceName();
			final String resourceType = resOpKey.getResourceType();
			final String operationName = resOpKey.getOperationName();
			final boolean resNameAccepted = (resName == null 
					|| resName.equalsIgnoreCase("All") 
					|| resName.equalsIgnoreCase(resourceName));
			final boolean resTypeAccepted = (resType == null 
					|| resType.equalsIgnoreCase("All") 
					|| resType.equalsIgnoreCase(resourceType));
			final boolean opNameAccepted = (opName == null 
					|| opName.equalsIgnoreCase("All") 
					|| opName.equalsIgnoreCase(operationName));
			if (resNameAccepted && resTypeAccepted && opNameAccepted) {
				ResourceInfoType resInfoType = new ResourceInfoType();
				response.getResourceInfo().add(resInfoType);
				ResOpType resOpType = new ResOpType();
				resInfoType.setResOp(resOpType);
				resOpType.setResourceName(resourceName);
				resOpType.setResourceType(resourceType);
				resOpType.setOperationName(resOpKey.getOperationName());
				resInfoType.setHits(svc.getPolicyStat(key));
			}
		}
		
		if (sgName != null) {
			LOGGER.warning("Subject Group queries are no longer supported - there's no more Group Membership cache");
		}
	
		return response;
	}

	@Override
	public CallInitializerResponse callInitializer(CallInitializerRequest callInitializerRequest) {
		
		try {
			Class<?> clazz = Class.forName(callInitializerRequest.getInitializerClassName());
			Initializer initializer = (Initializer) clazz.newInstance();
			for (String serviceName : callInitializerRequest.getServiceName()) {
				initializer.initialize(serviceName);
			}
			List<String> serviceNames = initializer.getServiceNames();
			if (LOGGER.isLoggable(Level.FINE)) LOGGER.fine("Initializer recorded services: " + serviceNames);
			CallInitializerResponse resp = new CallInitializerResponse();
			resp.getServiceName().addAll(serviceNames);
			
			return resp;
		} catch (RuntimeException e) {
			throw e;
		} catch (InitializerException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

}
