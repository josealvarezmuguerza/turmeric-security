/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.groupmembershipservice;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	org.ebayopensource.turmeric.groupmembershipservice.GetGroupMembersTests.class,
	org.ebayopensource.turmeric.groupmembershipservice.SOAGroupMembershipServiceQATests.class,
	org.ebayopensource.turmeric.groupmembershipservice.SOAGroupMembershipServiceTests.class
	}
)
public class AllTests {
	@BeforeClass 
	public static void executeBeforeTests(){
		//Create a helper that returns valid token for your testcases
		//TestTokenRetrivalObject.setTokenRetrivalObject((SecurityTokenUtility) new TokenProviderHelper());
	}
}
