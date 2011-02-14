/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.authentication.provider;

import java.util.List;

/**
 * Interface for authenticator. Examples are TokenAuthenticator,
 * AssertionAuthenticator etc...
 * 
 * @author gyue
 * 
 */
public interface Authenticator {

	/**
	 * Initialize the authenticator.
	 * 
	 * @throws AuthenticationException if initialization failed.
	 */
	public void initialize() throws AuthenticationException;

	/**
	 * Authenticate the incoming request.
	 * 
	 * @param authnRequest the @link AuthenticationRequest
	 * @return the @link AuthenticationResponse after authentication. 
	 * @throws AuthenticationException if authentication failed.
	 */
	public AuthenticationResponse authenticate(
			AuthenticationRequest authnRequest) throws AuthenticationException;

	/**
	 * Get the supported authentication method.
	 * 
	 * @return the supported authentication method.
	 */
	public String getAuthenticationMethod();

	/**
	 * Get all the credentials required by this authenticator to properly
	 * authenticate the incoming request.
	 * 
	 * @return list of credentials.
	 */
	public List<String> getRequiredCredentials();

}
