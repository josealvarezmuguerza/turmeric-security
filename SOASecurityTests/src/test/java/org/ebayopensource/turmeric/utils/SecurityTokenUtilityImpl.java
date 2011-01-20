package org.ebayopensource.turmeric.utils;

import java.util.HashMap;
import java.util.Map;

import org.ebayopensource.turmeric.test.services.utils.SecurityTokenUtility;

public class SecurityTokenUtilityImpl implements SecurityTokenUtility {

	@Override
	public String getSecurityToken() throws Exception {
		System.out.println("SecurityTokenUtilityImpl.getSecurityToken");
		return "token";
	}

	@Override
	public String getSecurityTokenHeader() throws Exception {
		System.out.println("SecurityTokenUtilityImpl.getSecurityTokenHeader");
		return "X-TURMERIC-SECURITY-TOKEN";
	}

	@Override
	public Map<String, String> getSecurityToken(String type,
			Map<String, String> subjects) throws Exception {
		System.out.println("SecurityTokenUtilityImpl.getSecurityToken(String type,Map<String, String> subjects)");
		Map<String, String> result = new HashMap<String, String>();
		result.put("userid", "user");
		result.put("password", "password");
		return result;
	}

}
