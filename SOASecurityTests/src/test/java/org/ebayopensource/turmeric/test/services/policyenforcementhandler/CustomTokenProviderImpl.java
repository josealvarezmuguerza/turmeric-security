package org.ebayopensource.turmeric.test.services.policyenforcementhandler;

import java.util.Map;

import org.ebayopensource.turmeric.securitycommon.intf.provider.TokenProvider;

public class CustomTokenProviderImpl implements TokenProvider{
	
	@Override
	public void init(Map<String, String> options) {
		//no op for Class based Impl
		//To be used for File based or esams based impl		
	}
	
	@Override
	public String getToken( Map<String, String> options ) {				
		return "validtoken";
	}

	@Override
	public String getTokenType() {
	   return "credential-testtoken";
	}	
}
