/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *    
 * http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.securitycommon.spf.impl.handlers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.handler.HandlerPreconditions;
import org.ebayopensource.turmeric.runtime.common.impl.handlers.BaseHandler;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;


/**
 * This handler extracts credential from HTTP header and sets them to security context.
 * @author gyue
 */
public class ServerCredentialHandler extends BaseHandler {

	private final String CREDENTIAL_PREFIX = "credential";
	private final String DELIMITER = "-";
	private Map<String, String> m_credentialNameHeaderMap;
	
	@Override
	public void init(InitContext ctx) throws ServiceException
	{
		super.init(ctx);	
		HandlerPreconditions.checkServerSide(ctx, this.getClass()); // Server Side Only
		
		m_credentialNameHeaderMap = new HashMap<String, String>();
				
		Map<String,String> options = ctx.getOptions();
		Iterator<String> i = options.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			if (key.toLowerCase().startsWith(CREDENTIAL_PREFIX)) {
				String name = key.substring(key.indexOf(DELIMITER) + 1); 
				m_credentialNameHeaderMap.put(name.toLowerCase(), options.get(key));
			}
		}

	}
	
	@Override
	public void invoke(MessageContext ctx) throws ServiceException {

		// extract the header from message context and puts it to security context
		Iterator<String> i = m_credentialNameHeaderMap.keySet().iterator();
		while (i.hasNext()) {
			String credName = i.next();
			String headerName = m_credentialNameHeaderMap.get(credName);
			String headerValue = ctx.getRequestMessage().getTransportHeader(headerName);
			if (headerValue != null) {
				// header found. Insert to credential map
				ctx.getSecurityContext().setCredential(credName, headerValue);
			}
		}
	}
	

}
