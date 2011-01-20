/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl.cache;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.services.authorizationservice.impl.util.EncodingUtils;
import org.ebayopensource.turmeric.utils.cache.AbstractCache.CacheBuildingException;


/**
 * Cache building exception representing an error that occurred during a web service
 * invocation internal to the builder. 
 * 
 * @author mpoplacenel
 */
public class WSCacheBuildingException extends CacheBuildingException {
	
	private static final long serialVersionUID = -1L;
	
	private AckValue m_ack;

	private ErrorMessage m_errMsg;

	/**
	 * Constructor. 
	 * @param ack the ACK. 
	 * @param message the error message. 
	 */
	public WSCacheBuildingException(AckValue ack, ErrorMessage errMsg) {
		super(EncodingUtils.encodeErrorMessage(ack, errMsg));
		
		m_errMsg = errMsg;
		m_ack = ack;
	}

	/**
	 * Getter for the error message.
	 * @return the error message.
	 */
	public ErrorMessage getErrorMessage() {
		return m_errMsg;
	}

	/**
	 * Getter for the ACK.
	 * @return the ACK. 
	 */
	public AckValue getAck() {
		return m_ack;
	}

}
