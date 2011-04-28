/*******************************************************************************
 * Copyright (c) 2006-2011 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.utils;

import java.util.Map;

import org.ebayopensource.turmeric.test.services.utils.SecurityTokenUtility;

public class SecurityTokenUtilityImpl implements SecurityTokenUtility {

	@Override
	public String getSecurityToken() throws Exception {
		return "security token value";
	}

	@Override
	public String getSecurityTokenHeader() throws Exception {
		return "X-TURMERIC-SECURITY-TOKEN";
	}

	@Override
	public Map<String, String> getSecurityToken(String type,
			Map<String, String> subjects) throws Exception {
		return null;
	}

}
