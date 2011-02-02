package org.ebayopensource.turmeric.test.services.policyenforcementhandler;

import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.turmeric.authentication.provider.AuthenticationException;
import org.ebayopensource.turmeric.authentication.provider.AuthenticationRequest;
import org.ebayopensource.turmeric.authentication.provider.AuthenticationResponse;
import org.ebayopensource.turmeric.authentication.provider.Authenticator;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;

public class TestAuthenticatePES implements Authenticator{
	private static List<String> s_requiredCredentials;
	public static final String CREDENTIAL_TOKEN = "testtoken";
	private String authnMethod = "TestAuthenticatePES";
	
	static {
		// initialize the static required credential list
		s_requiredCredentials = new ArrayList<String>();
		s_requiredCredentials.add(CREDENTIAL_TOKEN);
	}
 
	
	@Override
	public void initialize() throws AuthenticationException {
		// no init
	}
	
	@Override
	public String getAuthenticationMethod() {
		return authnMethod;
	}
	
	public TestAuthenticatePES(String authnMethod) {
		this.authnMethod = authnMethod;
	}

	
	/**
	 * Authenticate the subject from the incoming request
	 */
	public AuthenticationResponse authenticate(AuthenticationRequest authnRequest) {	
		List<SubjectType> sublist = new ArrayList<SubjectType>();
		SubjectType sub1 = new SubjectType();
		sub1.setValue("validtoken");
		sub1.setDomain("testdomain");
		sublist.add(sub1);
		AuthenticationResponse resp = new AuthenticationResponse();
		// just return success
		resp.setAuthenticationMethod(getAuthenticationMethod());
		resp.setAuthnSubjects(sublist);
		return resp;
	}
	
	/**
	 * Get the required credentials needed by the authenticator
	 */
	public List<String> getRequiredCredentials() {
		return s_requiredCredentials;
	}
}
