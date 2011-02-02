package org.ebayopensource.turmeric.test.services.policyenforcementhandler;

import org.ebayopensource.turmeric.test.services.policyenforcementservice.PolicyEnforcementServiceAuthzNegativeTest;
import org.ebayopensource.turmeric.test.services.policyenforcementservice.PolicyEnforcementServiceTests;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


/**
 * @author sukoneru
 *
 */
@ RunWith(Suite.class)
@Suite.SuiteClasses({
	PolicyEnforcementHandlerTests.class,
	PolicyEnforcementHandlerNegativeTests.class
	}
)
public class PolicyEnforcementHandlerAllTests {
	@BeforeClass 
	public static void executeBeforeTests(){
		//Create a helper that returns valid token for your testcases
		//TestTokenRetrivalObject.setTokenRetrivalObject((SecurityTokenUtility) new TokenProviderHelper());
		
		//Also required to start the jetty server with PolicyEnforcementService and CalculatorTestService running on the server.
	}
}
