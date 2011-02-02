package org.ebayopensource.turmeric.authentication.provider;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.ebayopensource.turmeric.authentication.model.BasicAuth;
import org.ebayopensource.turmeric.authentication.model.BasicAuthDAO;
import org.ebayopensource.turmeric.authentication.model.BasicAuthDAOImpl;
import org.ebayopensource.turmeric.policyservice.model.Subject;
import org.ebayopensource.turmeric.policyservice.model.SubjectDAO;
import org.ebayopensource.turmeric.policyservice.model.SubjectDAOImpl;
import org.ebayopensource.turmeric.policyservice.model.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.CredentialType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.utils.jpa.JPAAroundAdvice;
import org.ebayopensource.turmeric.utils.jpa.PersistenceContext;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.emory.mathcs.backport.java.util.Collections;

public class BasicAuthenticator implements Authenticator {
    private final EntityManagerFactory factory;
    private final Authenticator impl;
    
    public BasicAuthenticator(String type) {
    	 factory = PersistenceContext.createEntityManagerFactory("policyservice");
         
         ClassLoader classLoader = Authenticator.class.getClassLoader();
         Class[] interfaces = {Authenticator.class};
         Authenticator target = new BasicAuthenticatorImpl();
         impl = (Authenticator) Proxy.newProxyInstance(classLoader, interfaces, new JPAAroundAdvice(factory, target));
    }
    
    public BasicAuthenticator() {
        factory = PersistenceContext.createEntityManagerFactory("policyservice");
        
        ClassLoader classLoader = Authenticator.class.getClassLoader();
        Class[] interfaces = {Authenticator.class};
        Authenticator target = new BasicAuthenticatorImpl();
        impl = (Authenticator) Proxy.newProxyInstance(classLoader, interfaces, new JPAAroundAdvice(factory, target));
    }

    private class BasicAuthenticatorImpl implements Authenticator {
        private final static String CRED_SUBJECT = "userid";
        private final static String CRED_PASSWORD = "password";
        private final static String AUTHN_METHOD = "BASIC";
    
        SubjectDAO subjectDAO = new SubjectDAOImpl();
        BasicAuthDAO basicAuthDAO = new BasicAuthDAOImpl();
    
        @Override
        public void initialize() throws AuthenticationException {
        }
    
        @Override
        public AuthenticationResponse authenticate(AuthenticationRequest authnRequest)
                        throws AuthenticationException {
            CredentialType subject = authnRequest.getCredential(CRED_SUBJECT);
            CredentialType password = authnRequest.getCredential(CRED_PASSWORD);
    
            if (subject == null || subject.getValue() == null || subject.getValue().isEmpty() ||
                password == null || password.getValue() == null || password.getValue().isEmpty()) {
                return null;
            }
            
            String name = subject.getValue().trim();
            String pass = password.getValue().trim();
            
            Subject jpaSubject = subjectDAO.findSubjectByName(name);
            BasicAuth jpaAuth = basicAuthDAO.getBasicAuth(name);
            if (jpaSubject == null || jpaAuth == null ||
                !pass.equals(jpaAuth.getPassword())) {
                return null;
            }
            
            String type = jpaSubject.getSubjectType();
            List<SubjectGroup> jpaSubjectGroups = 
                subjectDAO.findSubjectGroupBySubjectName(name, type);
                
            AuthenticationResponse authnResponse = new AuthenticationResponse();
            authnResponse.setAuthenticationMethod(getAuthenticationMethod());
            
            SubjectType subj = new SubjectType();
            subj.setValue(name);
            subj.setDomain(type);
            authnResponse.setAuthnSubjects(Collections.singletonList(subj));
            
            List<SubjectGroupType> subjGroups = new ArrayList<SubjectGroupType>();
            for (SubjectGroup jpaSubjectGroup : jpaSubjectGroups) {
                SubjectGroupType subjGroup = new SubjectGroupType();
                subjGroup.setName(jpaSubjectGroup.getSubjectGroupName());
                subjGroup.setDomain(jpaSubjectGroup.getSubjectType());
                subjGroups.add(subjGroup);
            }
            authnResponse.setSubjectGroups(subjGroups);
            
            return authnResponse;
        }
    
        @Override
        public String getAuthenticationMethod() {
            return AUTHN_METHOD;
        }
    
        @Override
        public List<String> getRequiredCredentials() {
            return new ArrayList<String>(Arrays.asList(new String[] {CRED_SUBJECT, CRED_PASSWORD}));
        }
    }

    @Override
    public void initialize() throws AuthenticationException {
        impl.initialize(); 
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authnRequest)
                    throws AuthenticationException {
        return impl.authenticate(authnRequest);
    }

    @Override
    public String getAuthenticationMethod() {
        return impl.getAuthenticationMethod();
    }

    @Override
    public List<String> getRequiredCredentials() {
        return impl.getRequiredCredentials();
    }
}
