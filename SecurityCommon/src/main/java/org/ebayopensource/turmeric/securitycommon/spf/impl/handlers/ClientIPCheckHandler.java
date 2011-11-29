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

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.handler.HandlerPreconditions;
import org.ebayopensource.turmeric.runtime.common.impl.handlers.BaseHandler;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;

/**
 * This handler extracts IP from HTTP header and sets them to security context.
 * 
 * @author jamuguerza
 */
public class ClientIPCheckHandler extends BaseHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ebayopensource.turmeric.runtime.common.impl.handlers.BaseHandler#
	 * init(
	 * org.ebayopensource.turmeric.runtime.common.pipeline.Handler.InitContext)
	 */
	@Override
	public void init(InitContext ctx) throws ServiceException {
		super.init(ctx);
		HandlerPreconditions.checkServerSide(ctx, this.getClass()); // Server
																	// Side Only
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ebayopensource.turmeric.runtime.common.impl.handlers.BaseHandler#
	 * invoke
	 * (org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext)
	 */
	@Override
	public void invoke(MessageContext ctx) throws ServiceException {
		String ipAddress = ctx.getClientAddress().getIpAddress();
		if (ipAddress != null && !(ipAddress.isEmpty())) {
			ctx.setProperty(SOAConstants.CTX_PROP_TRANSPORT_CLIENT_SOURCE_IP,
					ipAddress);
		}
	}

}
