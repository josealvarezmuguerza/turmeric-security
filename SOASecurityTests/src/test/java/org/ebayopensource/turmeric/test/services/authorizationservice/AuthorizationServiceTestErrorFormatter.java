/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.test.services.authorizationservice;

import java.util.List;

import org.ebayopensource.turmeric.common.v1.types.ErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.common.v1.types.ErrorParameter;

/**
 * Utilities for testing the Authorization Service. 
 * 
 * @author mpoplacenel
 */
public class AuthorizationServiceTestErrorFormatter {
	
	public static String errorMessageToString(ErrorMessage errorMessage) {
		if (errorMessage == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (ErrorData ed : errorMessage.getError()) {
			sb
				.append("id=").append(ed.getErrorId())
				.append('\n')
				.append("category=").append(ed.getCategory())
				.append('\n')
				.append("domain=").append(ed.getDomain())
				.append('\n')
				.append("subdomain=").append(ed.getSubdomain())
				.append('\n')
				.append("message=").append(ed.getMessage())
				.append('\n')
				.append("exceptionId=").append(ed.getExceptionId())
				.append('\n')
				.append("params=[");
			final List<ErrorParameter> errParamList = ed.getParameter();
			sb.append(errorParametersToString(errParamList));
		}
		return sb.toString();
	}

	/**
	 * Append the specified error parameters to the given {@link StringBuilder}.
	 * 
	 * @param sb buffer to append to
	 * @param errParamList the error parameter list
	 */
	public static String errorParametersToString(final List<ErrorParameter> errParamList) {
		int i = 0;
		StringBuilder sb = new StringBuilder();
		for (ErrorParameter ep : errParamList) {
			if (i++ > 0) {
				sb.append(", \n\t");
			}
			sb.append(ep.getName()).append('=').append(ep.getValue());
		}
		
		return sb.toString();
	}
	

	
}