/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package com.ebay.services.authenticationservice.test;

import junit.framework.TestCase;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.security.SOACredentialConstants;
import org.ebayopensource.turmeric.runtime.sif.service.Service;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;
import org.ebayopensource.turmeric.security.v1.services.AuthenticateRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthenticateResponseType;
import org.ebayopensource.turmeric.security.v1.services.CredentialType;
import org.junit.Ignore;

import com.ebay.services.authenticationservice.intf.AuthenticationService;

public class AuthenticationServiceTest
    extends TestCase
{

	// valid ebay token for userid = apitest11    appid = 182
	public static final String VALID_EBAY_TOKEN = 
		"AgAAAA**AQAAAA**aAAAAA**IviiSQ**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6wJnY+gD5iFoA+dj6x9nY+seQ**tgAAAA**AAMAAA**+kAj2cX7qonMpms0DwGwhrfmYUPlEfHuwouvQ206kyTqFHKN8D/hzaVOe/Id/0uZhQjl1HSsnowkmoY+wwOMFwJUoTrvo7HrTLVvxefnFVgTLmZSeCeURJoxEX47UPFmPIeDUmxhhItyL8tpn6sASKn1Uxbz9v1MXN7eYG1DQn+6cFOicIhidinPD4UFc9Z4ATgU6eLyoShuqyBMR2hZleCckIGHX18daRAjjnLDUpgg7tHvsZXEB47A3vLFYUTWRIseGw+lYwPgePHrAJcMhQ6hMa9Nf9UaV53sPESwHptx2V/JX+qYBTWk2PGWpdIqsDBP1QpNsuQOTivGiOzDHaS+AukcHkgCJHObcpNOg5AauaX2pFKhWjrNg/zd+/mvNBzDQ7vxRAH4r8k1LhIkzbXNR6I7yg5a5AgGYkbpHdzwJMjgFslbWRnI0avfTpzXZmu0LBmptlzNXE7s7e7Ap1BE3P0OYiCcFIuRDvcmCBiE8eHXI1WI9nbMKv75/ertqnknCsNcsjNwPfmRT7/w4qT0ev+7jAgfTJqAEwV2RI0Ijta8Dq/gR2Y6unj6gEj9cU9eZxy3Jm+c0OQhIK+s+RwiDrS4ksryhQhKoeXlZMjOgN6OAWSWJ3JzTtv1eF8D0djUVAwMMR9p8382YU+fSwNm1LHx6vB6e8C7BCt8gGv9epU1Iz0yO45t4i5YitUZIaJCrvcOeupxiB7ZZy2pN+lNrzYH1JSGLOG8KIiSbT0L1LV4vElxb4zIcd0CFfMO";

	// invalid ebay token (bad characters in the end of token)
	public static final String INVALID_EBAY_TOKEN_BAD_CONTENT = 
		"AgAAAA**AQAAAA**aAAAAA**USbSRg**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6wJnY+gD5iFoA+dj6x9nY+seQ**tgAAAA**AAMAAA**lMf1hg+zmaGVf0f7ClHrSPlnYU8kyhSG2UtJMiWFQRiHytFmknZYfM5GE6jCZeb2zWJZb7wmagA7EuxoQrkkCV5mlHXMgbUb71XRNsvYvtCmpv2Wr1fEp3YcV3Rf2hMWBKuRItqt+LkVJ8Ydw4mHiXCL9AfQQeJ0Uz++ZDjkGXa3Ybo/IHwr/ayCd5tVhqLBEKx6QmzX6U6IPJuuJG1meQJSaiMUARsMGkkAPTWYC6vbYTBDVwtMVs9ej4OCdZbkzv+O9tI+qDPsluuN7EyK2+DkxQIo7GrOZ0KSYzrn1vm03eg7mGYoKQ+D3Sdn0Ul2P30Szn+qcz9bpHv0zecYEu8iLkGbQyBEXr/3ylSqeFBtsodjubsOlCOU+VaPH7/jNwd7odIUrTAfXL6YLOXopJP74nRVOITmYT6ScIXKZ8/uSFEUlO0Y6+LfTJTm+3Q6/NoGgRea/RiNOfCGXEXPIp+JGSHHKQTSjGUp0jjueL7fyf6sFDIKV+fMlw9nIl/jJ6+E5Ld/DQQ7Zm3oFj1qYffg5SzLxhgaoZHiet9Qeku0koDNmMd7g5g2pMrysJTQlyTq2eOjcUMCpRoJpz1Q3FqlfqMTjsU+Gp+J02KM0FyhV/pXWyREuwsQ5w4KkObLDhxQu4TbFV10fW3vN1aPxTWQRk+n99EeVZ8p6rmHL8yShBYsiKwKECBc/q4ez/VnwTDD5w3o8jQT/DYwc4LELMszMfbhnA2cBv3dr6peKFfaSrOP0YZv3T4Fo3o1f5PyBAD";

	// invalid ebay token (incorrect token type)
	public static final String INVALID_EBAY_TOKEN_INCORRECT_TYPE = 
		"AGAAAA**AQAAAA**aAAAAA**USbSRg**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6wJnY+gD5iFoA+dj6x9nY+seQ**tgAAAA**AAMAAA**lMf1hg+zmaGVf0f7ClHrSPlnYU8kyhSG2UtJMiWFQRiHytFmknZYfM5GE6jCZeb2zWJZb7wmagA7EuxoQrkkCV5mlHXMgbUb71XRNsvYvtCmpv2Wr1fEp3YcV3Rf2hMWBKuRItqt+LkVJ8Ydw4mHiXCL9AfQQeJ0Uz++ZDjkGXa3Ybo/IHwr/ayCd5tVhqLBEKx6QmzX6U6IPJuuJG1meQJSaiMUARsMGkkAPTWYC6vbYTBDVwtMVs9ej4OCdZbkzv+O9tI+qDPsluuN7EyK2+DkxQIo7GrOZ0KSYzrn1vm03eg7mGYoKQ+D3Sdn0Ul2P30Szn+qcz9bpHv0zecYEu8iLkGbQyBEXr/3ylSqeFBtsodjubsOlCOU+VaPH7/jNwd7odIUrTAfXL6YLOXopJP74nRVOITmYT6ScIXKZ8/uSFEUlO0Y6+LfTJTm+3Q6/NoGgRea/RiNOfCGXEXPIp+JGSHHKQTSjGUp0jjueL7fyf6sFDIKV+fMlw9nIl/jJ6+E5Ld/DQQ7Zm3oFj1qYffg5SzLxhgaoZHiet9Qeku0koDNmMd7g5g2pMrysJTQlyTq2eOjcUMCpRoJpz1Q3FqlfqMTjsU+Gp+J02KM0FyhV/pXWyREuwsQ5w4KkObLDhxQu4TbFV10fW3vN1aPxTWQRk+n99EeVZ8p6rmHL8yShBYsiKwKECBc/q4ez/VnwTDD5w3o8jQT/DYwc4LELMszMfbhnA2cBv3dr6peKFfaSrOP0YZv3T4Fo3o1f5PyBAD";
	
	public static final String REVOKED_EBAY_TOKEN = 
		"AgAAAA**AQAAAA**aAAAAA**Jv/CRw**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6wJnY+gD5iFowidj6x9nY+seQ**aSIAAA**AAMAAA**TyXcOMxUULrZqB49orH+VwzqdkPoZ9XWK7A9LAGhSjvovqawx27GQ0ERC76JODDOTAJZUn67tUIWb4jURJIOOuOv5EMsF0ilDsqYwUXxdw4L/J8IswehFO2lJEsVTvoPXeIFNCFDZRN0ZPiVSLDVteONbm2IYN+E5FG1ZsiXxDfGKaAt5zPAcc3Z4TjWw3G6rIhpX5odLhyjymqfkZdEAAnB1aKtcRHBJuSbsZAEy0BPvaUMZ7a8s0EWaTcBHqPrrRCu0wdlRWNgMRIkCvuSc5R2quF0SKYa52gtyeLRSZEnz16c3WQVwerUuO7fpzkKhqDYsWILJ3jgeh8g4uNrViLhmtj1Tb4ZCU0Fq2JgcLd55l2+eCxe0S4dt9h9Y/ub5ouEbA5pGg9VR+obPdRafz9r5qDBw3BSnZzuGYKWSHzWiJdxQDEoEAEfx1M5Vbq+Jp7vNd9ZFl7KZvFPVWmWllefco7EyuJz5IniL7RHHMT5XXCV1x50ZOvdnokFCbS3I3cyYWVgihu0FqbbBQhE998L3VutWpLrlYCuE37Win5Uf+qMIGuvO07G0lxgNzCNbgGSzh+X6ZD/hpksDRUz5Qc4n9kwr4/FZDoRsddSPS7pPlchJV3sRuP5z3rlrUYAkyLPrGOQVwtoaumaJY2r3h3IeH54uOXA5f5PYbNoBWTeA2SuQrRApwATpSGksMKSRzVB3NU51rPi/URrFNAEtxePReI3FGtb48cXmMHEaDoBsVi8AeFpBmOPD0SUY2rW";
		//"AgAAAA**AQAAAA**aAAAAA**Am3gRg**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6wJnY+gD5iFoQudj6x9nY+seQ**tgAAAA**AAMAAA**rdWx95P/WJIQAgnd/8DvYcsU3xU+R0UDlovw/E6wUZq2BPnnm/SoQ6ndoS93u3GOguvE5eFTX1MtN1LujuNwrwsA75af884AgFRypguZhHtNyyVTmkwDFRgdrECtbh2CmSQmWVuaxrL/cN00lyn0z5bLrFMxAf/hN45Nrj+gCSwXstbrNsKXgcN2yjh9mEvNAXNyuMR4pG6uPCH264/L13LNjWEADko6JHlFgDfWVKcQy9B8SpF5zOB/E80XEKk16t341zgpnKVUpazaGS3X7XFgEcJX0nQKuL6nnEAGzi21ndH2xKnktQzOf/Xn6Wme1UBE55N+YowLicYQnojLdqbqIKmhkmypTZ8hRrUkmbqZQzDaMW5hAdKPNr8iNTLggXubzNN1/E5G40x/6THOzCmEcWxDKTIJ5KbXAkTtWZ0/2gQ0Q+1G8uP5EzS9h5YSlf3YuiMUrFNBrmL2VoRiHF+/ORvSAponytl8WSGW0x2OuH5Fo/NVnCA0BYGdMmaRHbFuWpj5EO77KMXszjCp4X1yi7vHA3UrsfupXP8vQI3s0nBmQ0mG8A6mn/l0WLzSCxeZC6R7trkkvZq6eP5/sXu0pNcindOyvtUxKToSFbDc6fwH6Y4RA0I/6SUlE65UTf6ANfDot4ro21ZOFqawIUoSatjMSyDYXTl8IAOp3jfiEvS1DMl95MPnPNucvFb6HfnAcCVTEVwFmQUx37TDhTUv45IYSLOaaRAI671NuHE/+5UY7RvX1v7AwjETnhR7";

	public static final String HARD_EXPIRED_TOKEN =
		"AgAAAA**AQAAAA**aAAAAA**Q3OfRw**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6wJnY+gD5iFoA+dj6x9nY+seQ**aSIAAA**AAMAAA**eektuVmqlq++XOiAfgALvE/715cQNxHFk2wJzdxCIw7GfJKrA0dih9ScMjPduGsX57Zpa/XUktssvDGR8KwCKvCM9kkaVzPDL5M2PB7UlHOcE4fl/3j+FP3i475ZeiFF1YmtCwA/W2uqOkT405uGmGp+yeGheXApRipLYM/dpdyO/jnhrleTF64lfMCYtdm0qO19VFoWr0MMRNVCCKHwZafBEsTIvzBuRNxp6GttkIEYVAcavf5wc4taAwFuseRD7f4xyoTEr3pZngKRmWAZkHXMvmbpLp4REWax/Kb9kEuS/2IpCqFiR8xOuKcW/sHiUN51t4mauymMpZSYGG4mLLrRss15Vhz65Ad9t0PFKUL/BTL+Nvv27/ACB7NZeVoDCX+2XmvwFkvkLJe3rSt0wUIffUZgP0aaDTm6/2fnAWxww436MiRNGYEWQoW4b2Edlq+Mq2Np9MthIPuPoVaPW+70xDv/gmvlVUyYKUb2+pR3bK2aP+maC80JHMVuvpQQMnfUjCtH34BJsxK8CTAMc8flf3yPbP7fnQcvWr7aaDgiDIByzp+B9VIFqlOpzkcobanaM4cTlthotc+5E1bud8mFyzHZjYwvbVCUa7yg7e2yhU0Xe9hH0ZxjPlPWbcd7UVoxSBdX2P7wmZ4ESV9fsWUCK1p7ZLcKnttA8hTes7BG9EtpfLRo2EBAieDegl5WztNITfMkeIlyeq9GgaCrNBubfd6oFBgQGixPiKDcYRUhGm6k0qbxSQVmLmlAqJtm";

	/*
	public static String createValidAssertion(String appName, String devName, String userName) throws Exception {
			Map<String,String> result = new HashMap<String,String>();
			//result.put(SOASubjectTypeConstants.SUBJECT_TYPE_PROCID, Long.toString(procId));
			result.put(SOASubjectTypeConstants.SUBJECT_TYPE_APPNAME, appName);
			result.put(SOASubjectTypeConstants.SUBJECT_TYPE_DEVID, devName);
			result.put(SOASubjectTypeConstants.SUBJECT_TYPE_USERNAME, userName);
			return SOAIpeHelper.wrap(result);
		}
	 */

    private AuthenticationService m_proxy = null;

    public AuthenticationServiceTest(String testcaseName) {
        super(testcaseName);
    }

    private AuthenticationService getProxy()
        throws ServiceException
    {
        if (m_proxy == null) {
            String svcAdminName = "AuthenticationService";
            Service service = ServiceFactory.create(svcAdminName, "AuthenticationService", null);
            m_proxy = service.getProxy();
        }
        return m_proxy;
    }

    public void testNothing() {
    }

    //  test1ext:myTestOperation => token authn method
    @Ignore
    public void donttestAuthenticate_token_success()
        throws Exception
    {
        AuthenticateResponseType result = null;
        AuthenticateRequestType req = new AuthenticateRequestType();
        req.setResourceName("test1ext");
        req.setOperationName("myTestOperation");
        req.setResourceType("Service");
        CredentialType credType = new CredentialType();
        credType.setName(SOACredentialConstants.CREDENTIAL_TOKEN);
        credType.setValue(VALID_EBAY_TOKEN);
        req.getCredential().add(credType);

        result = getProxy().authenticate(req);

        assertTrue(result.getAck() == AckValue.SUCCESS);
        assertTrue(result.getAuthenticationMethod().equalsIgnoreCase("token"));
    }
    
    // test1ext:myTestOperation => token authn method
    @Ignore
    public void donttestAuthenticate_token_failure()
    	throws Exception
	{
	    AuthenticateResponseType result = null;
	    AuthenticateRequestType req = new AuthenticateRequestType();
	    req.setResourceName("test1ext");
	    req.setOperationName("myTestOperation"); 
	    req.setResourceType("Service");
	    CredentialType credType = new CredentialType();
	    credType.setName(SOACredentialConstants.CREDENTIAL_TOKEN);
	    credType.setValue(INVALID_EBAY_TOKEN_BAD_CONTENT);
	    req.getCredential().add(credType);
	
	    result = getProxy().authenticate(req);
	
	    assertTrue(result.getAck() == AckValue.FAILURE);
	    assertTrue(result.getAuthenticationMethod().equalsIgnoreCase("token"));
	    assertTrue(result.getErrorMessage() != null);
//	    assertTrue(result.getErrorMessage().getError().get(0).getErrorId() 
//	    		== ErrorConstants.SVC_SECURITY_AUTHN_INTERNAL_ERROR.get;
	    
	}
    
    //  test1ext:serviceChainingOperation => assertion authn method
    /*
    public void testAuthenticate_assertion_success()
        throws Exception
    {
        AuthenticateResponseType result = null;
        AuthenticateRequestType req = new AuthenticateRequestType();
        req.setResourceName("test1ext");
        req.setOperationName("serviceChainingOperation");
        req.setResourceType("Service");
        CredentialType credType = new CredentialType();
        credType.setName(SOACredentialConstants.CREDENTIAL_ASSERTION);
        credType.setValue(createValidAssertion("app1", "dev1", "user1"));
        req.getCredential().add(credType);

        result = getProxy().authenticate(req);

        assertTrue(result.isError()==false);
        assertTrue(result.getAuthenticationMethod().equalsIgnoreCase("assertion"));
    }
    */
    
    // test1ext:serviceChainingOperation => assertion authn method
    @Ignore
    public void donttestAuthenticate_assertion_failure()
    	throws Exception
	{
	    AuthenticateResponseType result = null;
	    AuthenticateRequestType req = new AuthenticateRequestType();
	    req.setResourceName("test1ext");
	    req.setOperationName("serviceChainingOperation"); 
	    req.setResourceType("Service");
	    CredentialType credType = new CredentialType();
        credType.setName(SOACredentialConstants.CREDENTIAL_ASSERTION);
        credType.setValue("JUNK_ASSERTION_BLOB");
	    req.getCredential().add(credType);
	
	    result = getProxy().authenticate(req);
	
	    assertTrue(result.getAck() == AckValue.FAILURE);
	    assertTrue(result.getAuthenticationMethod().equalsIgnoreCase("assertion"));
	    assertTrue(result.getErrorMessage() != null);
//	    assertTrue(result.getErrorMessage().getError().get(0).getErrorId() 
//	    		== ApplicationErrorTypes.APP_AUTHN_INVALID_ASSERTION.getId());
	    
	}

}
