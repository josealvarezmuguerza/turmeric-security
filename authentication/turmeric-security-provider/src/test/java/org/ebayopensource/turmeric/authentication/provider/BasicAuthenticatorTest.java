package org.ebayopensource.turmeric.authentication.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.turmeric.security.v1.services.CredentialType;
import org.junit.Test;

public class BasicAuthenticatorTest extends BasicAuthenticatorTestBase {
    @Test
    public void authenticateTest() throws Exception {
        Authenticator auth = new BasicAuthenticator();
        
        List<CredentialType> credentials = new ArrayList<CredentialType>();
        CredentialType subject = new CredentialType();
        subject.setName("userid");
        subject.setValue("jdoe");
        credentials.add(subject);
        CredentialType password = new CredentialType();
        password.setName("password");
        password.setValue("secret");
        credentials.add(password);
        
        AuthenticationRequest request = new AuthenticationRequest();
        request.setCredentials(credentials);
        
        AuthenticationResponse response = auth.authenticate(request);
        
        assertNotNull(response);
        assertEquals("BASIC", response.getAuthenticationMethod());
        assertEquals("jdoe", response.getAuthnSubject("USER").getValue());
        assertEquals(2, response.getSubjectGroups().size());
    }

}
