/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	org.ebayopensource.turmeric.policyservice.resource.AllTests.class,
	org.ebayopensource.turmeric.policyservice.subject.AllTests.class,
	org.ebayopensource.turmeric.policyservice.subjectgroup.AllTests.class,
	org.ebayopensource.turmeric.policyservice.createpolicyapi.AllTests.class,
	org.ebayopensource.turmeric.policyservice.updatepolicyapi.AllTests.class,
	org.ebayopensource.turmeric.policyservice.deletepolicyapi.AllTests.class,
	org.ebayopensource.turmeric.policyservice.findpolicyapi.AllTests.class,
	org.ebayopensource.turmeric.policyservice.validatepolicyapi.AllTests.class,
	org.ebayopensource.turmeric.policyservice.getmetadata.AllTests.class,
	org.ebayopensource.turmeric.policyservice.getmetadata.AllTests.class,
	org.ebayopensource.turmeric.policyservice.getauthnpolicy.AllTests.class,
	org.ebayopensource.turmeric.policyservice.getentityhistory.AllTests.class
	}
)

public class AllTests {
}


