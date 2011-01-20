/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *    
 * http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.securitycommon.intf.provider;

import java.util.Map;

/**
 * This Interface is used to specify set of standard methods 
 * that, the token provider impl should provide for 
 * ClientTokenRetrievalHandler to read the token. 
 * 
 * @author prjande
 */
public interface TokenProvider {

	/**
	 * Initialize the token provider.
	 * @param initOptions custom options passed from the caller
	 */
	public void init(Map<String, String> initOptions);

	/**
	 * Get the type of token.
	 * @return the type of token in a String
	 */
	public String getTokenType();

	/**
	 * Get the token.
	 * @param options custom options passed from the caller
	 * @return the token in a String
	 */
	public String getToken(Map<String, String> options);

}