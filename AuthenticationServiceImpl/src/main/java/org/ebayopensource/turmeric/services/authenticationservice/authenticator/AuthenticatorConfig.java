/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authenticationservice.authenticator;

import java.util.HashMap;
import java.util.Map;

import org.ebayopensource.turmeric.utils.ConfigUtils;

/**
 * This class represents the Authenticator configuration
 * <p>
 * @author gyue
 */
public class AuthenticatorConfig {
	String m_name;
	String m_classname;
	String m_supportedAuthnMethod;
	Map<String, String> m_options;
	
	/**
	 * @return the classname
	 */
	public String getClassname() {
		return m_classname;
	}
	/**
	 * @param classname the classname to set
	 */
	public void setClassname(String classname) {
		m_classname = classname;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return m_name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		m_name = name;
	}
	
	/**
	 * @return supported authencation method
	 */
	public String getSupportedAuthenticationMethod() {
		return m_supportedAuthnMethod;
	}
	/**
	 * @param supported authencation method
	 */
	public void setSupportedAuthenticationMethod(String authnMethod) {
		m_supportedAuthnMethod = authnMethod;
	}
	/**
	 * @return the options
	 */
	public Map<String, String> getOptions() {
		if (m_options == null) {
			m_options = new HashMap<String, String>();
		}
			
		return m_options;
	}
	/**
	 * @param options the options to set
	 */
	public void setOptions(Map<String, String> options) {
		m_options = options;
	}
	
	public AuthenticatorConfig copy() {
		AuthenticatorConfig result = new AuthenticatorConfig();
		result.m_classname = m_classname;
		result.m_name = m_name;
		result.m_supportedAuthnMethod = m_supportedAuthnMethod;
		result.m_options = new HashMap<String, String>(getOptions());
		return result;
	}
	
	public void dump(StringBuffer sb) {
		sb.append(  "Authenticator: " 	+ m_name + '\n');
		sb.append("  class name: "		+m_classname + '\n');
		sb.append("  authn method: "	+m_supportedAuthnMethod + '\n');
		if (m_options != null)
			ConfigUtils.dumpStringMap(sb, m_options, "  ");
	}
}
