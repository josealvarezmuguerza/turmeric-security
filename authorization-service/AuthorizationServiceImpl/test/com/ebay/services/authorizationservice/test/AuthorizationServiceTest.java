/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package com.ebay.services.authorizationservice.test;

import junit.framework.TestCase;

import com.ebay.marketplace.services.AckValue;
import com.ebay.marketplace.services.AuthorizeRequestType;
import com.ebay.marketplace.services.AuthorizeResponseType;
//import com.ebay.marketplace.services.ContextInfoType;
import com.ebay.marketplace.services.SubjectType;
import com.ebay.securityframework.common.errors.ApplicationErrorTypes;
import com.ebay.services.authorizationservice.intf.AuthorizationService;
import com.ebay.soaframework.common.exceptions.ServiceException;
import com.ebay.soaframework.sif.service.Service;
import com.ebay.soaframework.sif.service.ServiceFactory;

public class AuthorizationServiceTest
    extends TestCase
{

    private AuthorizationService m_proxy = null;

    public AuthorizationServiceTest(String testcaseName) {
        super(testcaseName);
    }

    private AuthorizationService getProxy()
        throws ServiceException
    {
        if (m_proxy == null) {
            String svcAdminName = "AuthorizationService";
            Service service = ServiceFactory.create(svcAdminName, "default", null);
            m_proxy = service.getProxy();
        }
        return m_proxy;
    }

    public void testAuthorize_success()
        throws Exception
    {
        AuthorizeResponseType result = null;
        AuthorizeRequestType req = new AuthorizeRequestType();
        req.setResourceName("test1ext");
        req.setOperationName("myVoidReturnOperation");
        req.setResourceType("SERVICE");

		SubjectType subject = new SubjectType();
		subject.setDomain("USER");
		subject.setValue("user1");
		req.getSubject().add(subject);
		
//		ContextInfoType contextInfo = new ContextInfoType();
//		contextInfo.setType("IPADDRESS");
//		contextInfo.setValue("1.2.3.4");
//		req.getContextInfo().add(contextInfo);

        result = getProxy().authorize(req);

        assertTrue(result.getAck() == AckValue.SUCCESS);
    }
    
    public void testAuthorize_failure()
    	throws Exception
	{
    	AuthorizeResponseType result = null;
	    AuthorizeRequestType req = new AuthorizeRequestType();
	    req.setResourceName("test1ext");
	    req.setOperationName("myVoidReturnOperation"); 
	    req.setResourceType("SERVICE");

		SubjectType subject = new SubjectType();
		subject.setDomain("USER");
		subject.setValue("user11111");
		req.getSubject().add(subject);
		
//		ContextInfoType contextInfo = new ContextInfoType();
//		contextInfo.setType("IPADDRESS");
//		contextInfo.setValue("1.2.3.4");
//		req.getContextInfo().add(contextInfo);

	    result = getProxy().authorize(req);
	
	    assertTrue(result.getAck() == AckValue.FAILURE);
	    assertTrue(result.getErrorMessage() != null);
	    assertTrue(result.getErrorMessage().getError().get(0).getErrorId() 
	    		== ApplicationErrorTypes.APP_AUTHZ_INTERNAL_ERROR.getId());
	}
}
