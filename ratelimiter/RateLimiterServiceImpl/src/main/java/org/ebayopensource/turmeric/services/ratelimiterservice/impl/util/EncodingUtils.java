/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.ratelimiterservice.impl.util;

import java.util.Arrays;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.ErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;

/**
 * Utility methods for encoding various classes to string (and decoding them from
 * a string, of course).
 * 
 * @author mpoplacenel
 */
public class EncodingUtils {


	
	/**
	 * Encode error message.
	 *
	 * @param ack the ack
	 * @param errMsg the err msg
	 * @return the string
	 */
	public static String encodeErrorMessage(AckValue ack, ErrorMessage errMsg) {
		StringBuilder sb = new StringBuilder();
		sb.append("ack=").append(String.valueOf(ack)).append(";");
		if (errMsg != null) {
			for (ErrorData errData : errMsg.getError()) {
				sb.append(';');
				sb
					.append("severity:").append(errData.getSeverity())
					.append(',')
					.append("errorId:").append(errData.getErrorId())
					.append(',')
					.append("exceptionId:").append(errData.getExceptionId())
					.append(',')
					.append("domain:").append(errData.getDomain())
					.append(',')
					.append("subdomain:").append(errData.getSubdomain())
					.append(',')
					.append("message:").append(errData.getMessage());
			}
		}
		
		return sb.toString();
	}




}