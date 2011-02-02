/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package org.ebayopensource.turmeric.test.services.handlers;

import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.handlers.BaseHandler;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageContext;
import org.ebayopensource.turmeric.runtime.spf.security.ServerSecurityContext;

public class TestAuthenticationHandler extends BaseHandler {

	@Override
	public void init(InitContext ctx) throws ServiceException {
		super.init(ctx);
	}

	@Override
	public void invoke(MessageContext ctx) throws ServiceException {

		ServerSecurityContext secCtx = (ServerSecurityContext) ctx
		.getSecurityContext();
			
		// propagate subject list to context
		String subjectName = "pzhao";
		String subjectType = "CORPUSER";
		
		secCtx.setAuthnSubject(subjectType,subjectName);
	}
	
}
