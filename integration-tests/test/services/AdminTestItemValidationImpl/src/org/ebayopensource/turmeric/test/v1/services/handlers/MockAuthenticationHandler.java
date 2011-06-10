/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.test.v1.services.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.handlers.BaseHandler;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
import org.ebayopensource.turmeric.runtime.common.pipeline.Pipeline.InitContext;
import org.ebayopensource.turmeric.runtime.spf.security.ServerSecurityContext;

public class MockAuthenticationHandler extends BaseHandler {

	public static final String ADMIN_SVC_TOKEN = "ADMIN_SVC_TOKEN";
	
	public static final String CACHE_TEST_TOKEN = "CACHE_TEST_TOKEN";
	
	public static final String UNAUTHZ_TOKEN = "UNAUTHZ_TOKEN";
	
	public static final String OS_TOKEN = "OS_TOKEN";
	
	private Class propsClass = MockAuthenticationHandler.class;
	
	public String propFileName = propsClass.getSimpleName()+".properties";
	
	@Override
	public void init(InitContext ctx) throws ServiceException {
		super.init(ctx);
	}

	@Override
	public void invoke(MessageContext ctx) throws ServiceException {

		Map<String, String> credentials = ctx.getSecurityContext().getCredentials();
		System.out.println(credentials);
		ServerSecurityContext secCtx = (ServerSecurityContext) ctx
				.getSecurityContext();
		String token = null;
		if(ctx.getRequestMessage().getTransportHeader("X-EBAY-SOA-SECURITY-TOKEN")!=null)
			token = ctx.getRequestMessage().getTransportHeader("X-EBAY-SOA-SECURITY-TOKEN");
		else if(ctx.getRequestMessage().getTransportHeader("X-TURMERIC-SECURITY-TOKEN")!=null)
			token = ctx.getRequestMessage().getTransportHeader("X-TURMERIC-SECURITY-TOKEN");
		
		Properties props = new Properties();	
		InputStream input = propsClass.getResourceAsStream(propFileName);
		try {
			if(input!=null)
			   props.load(input);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String value = null;
		if (ADMIN_SVC_TOKEN.equals(token)
				|| CACHE_TEST_TOKEN.equals(token)) {
			value = props.getProperty("CACHE_TEST_TOKEN");
		} else if (UNAUTHZ_TOKEN.equals(token)) {
			value = props.getProperty("UNAUTHZ_TOKEN");
		}else if(OS_TOKEN.equals(token)){
			value = props.getProperty("OS_TOKEN");
		}
		
		assignSubjectToSecurityCtx(value,secCtx);
	}
	
	public void assignSubjectToSecurityCtx(String value, ServerSecurityContext secCtx) throws ServiceException{
		StringTokenizer st = new StringTokenizer(value, ",");
		while (st.hasMoreElements()) {
		           String sub_info = st.nextToken();
		           String[] subject = sub_info.split(":");
		           secCtx.setAuthnSubject(subject[1].trim(), subject[0].trim());
		}
	}
	
}
