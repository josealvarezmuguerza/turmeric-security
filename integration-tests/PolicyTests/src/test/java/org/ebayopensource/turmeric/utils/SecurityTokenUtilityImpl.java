package org.ebayopensource.turmeric.utils;

import java.util.Map;

import org.ebayopensource.turmeric.test.services.utils.SecurityTokenUtility;

public class SecurityTokenUtilityImpl implements SecurityTokenUtility {

	@Override
	public String getSecurityToken() throws Exception {
		// TODO Auto-generated method stub
		return "security token value";
	}

	@Override
	public String getSecurityTokenHeader() throws Exception {
		// TODO Auto-generated method stub
		return "X-TURMERIC-SECURITY-TOKEN";
	}

	@Override
	public Map<String, String> getSecurityToken(String type,
			Map<String, String> subjects) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
