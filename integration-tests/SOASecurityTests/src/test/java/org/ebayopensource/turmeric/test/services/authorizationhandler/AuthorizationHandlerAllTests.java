package org.ebayopensource.turmeric.test.services.authorizationhandler;

import org.ebayopensource.turmeric.test.services.authorizationservice.AuthorizationServiceCacheTestValidation;
import org.ebayopensource.turmeric.test.services.authorizationservice.AuthorizationServiceNegativeTest;
import org.ebayopensource.turmeric.test.services.authorizationservice.AuthorizationServiceTests;
import org.ebayopensource.turmeric.test.services.authorizationservice.FunctionalAuthzTests;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * @author sukoneru
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	AuthorizationHandlerTests.class
	}
)
public class AuthorizationHandlerAllTests {
	@BeforeClass 
	public static void executeBeforeTests(){
		//Create a helper that returns valid token for your testcases
		//TestTokenRetrivalObject.setTokenRetrivalObject((SecurityTokenUtility) new TokenProviderHelper());
	}
}
