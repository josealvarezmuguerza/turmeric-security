/*******************************************************************************
 * Copyright (c) 2006-2011 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/

package org.ebayopensource.turmeric.test.services.authenticationservice;

import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.turmeric.authentication.provider.AuthenticationException;
import org.ebayopensource.turmeric.authentication.provider.AuthenticationRequest;
import org.ebayopensource.turmeric.authentication.provider.AuthenticationResponse;
import org.ebayopensource.turmeric.authentication.provider.Authenticator;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePairType;

/**
 * Anonymous authenticator (used when the request is meant to be public such
 * that there's no "real" authentication needed).
 *
 * @author sukoneru
 */

	public class AnonymousOSExtendedInfoAuthenticatorImpl implements Authenticator {
		private static List<String> s_requiredCredentials;
		private String authnMethod;
		
		static {
			// initialize the static required credential list
			s_requiredCredentials = new ArrayList<String>();
			// note that there is no required credentials needed by this
			// authenticator
		}
	 
		
		/* (non-Javadoc)
		 * @see org.ebayopensource.turmeric.authentication.provider.Authenticator#initialize()
		 */
		@Override
		public void initialize() throws AuthenticationException {
			// no init
		}
		
		/* (non-Javadoc)
		 * @see org.ebayopensource.turmeric.authentication.provider.Authenticator#getAuthenticationMethod()
		 */
		@Override
		public String getAuthenticationMethod() {
			return authnMethod;
		}
		
		/**
		 * Instantiates a new anonymous os extended info authenticator impl.
		 *
		 * @param authnMethod the authn method
		 */
		public AnonymousOSExtendedInfoAuthenticatorImpl(String authnMethod) {
			this.authnMethod = authnMethod;
		}

		
		/**
		 * Authenticate the subject from the incoming request.
		 *
		 * @param authnRequest the authn request
		 * @return the authentication response
		 */
		@Override
		public AuthenticationResponse authenticate(AuthenticationRequest authnRequest) {
			int num = 0;
			if(authnRequest.getCredential("TYPE")!=null)
			  num = Integer.parseInt(authnRequest.getCredential("TYPE").getValue());
			
			
			AuthenticationResponse resp = new AuthenticationResponse();
			switch(num){
			case 1:
				 KeyValuePairType kvpair = new KeyValuePairType();
				 kvpair.setKey("key1");
				 kvpair.setValue("value1");
				 resp.addExtendedInfo(kvpair );
				 break;
			case 2: 
				KeyValuePairType kvpair1 = new KeyValuePairType();
				 kvpair1.setKey("key1");
				 kvpair1.setValue("value1");
				KeyValuePairType kvpair2 = new KeyValuePairType();
				 kvpair2.setKey("key2");
				 kvpair2.setValue("value2");
				ArrayList<KeyValuePairType> list = new ArrayList<KeyValuePairType>();
				list.add(kvpair1);
				list.add(kvpair2);
				resp.setExtendedInfo(list);
				break;
			case 3:
				resp.addExtendedInfo(null);
				break;
			case 4:
				resp.setExtendedInfo(null);
				break;
			}  
			
			// just return success
			resp.setAuthenticationMethod(getAuthenticationMethod());
			return resp;
		}
		
		/**
		 * Get the required credentials needed by the authenticator.
		 *
		 * @return the required credentials
		 */
		@Override
		public List<String> getRequiredCredentials() {
			return s_requiredCredentials;
		}
		
	}
