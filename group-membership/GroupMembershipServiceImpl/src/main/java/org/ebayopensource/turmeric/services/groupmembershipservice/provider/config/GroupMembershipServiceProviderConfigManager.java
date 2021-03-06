/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.groupmembershipservice.provider.config;

import org.ebayopensource.turmeric.utils.config.BaseConfigManager;
import org.ebayopensource.turmeric.utils.config.exceptions.ConfigurationException;
import org.w3c.dom.Element;


public class GroupMembershipServiceProviderConfigManager extends BaseConfigManager { 
	
	private static final String CONFIG_FILENAME = "GroupMembershipServiceProviderConfig.xml";
	private static final String SCHEMA_FILENAME = "GroupMembershipServiceProviderConfig.xsd";
	private static final String ROOT_ELEMENT = "group-membership-service-provider-config";
    private static GroupMembershipServiceProviderConfigManager s_instance = null; 

	private GroupMembershipServiceProviderConfig m_config;
	
	

	public static GroupMembershipServiceProviderConfigManager getInstance() {
    	if (s_instance == null) {
    		s_instance = new GroupMembershipServiceProviderConfigManager();
    	}
    	return s_instance;
    }
	
	
	public synchronized GroupMembershipServiceProviderConfig getConfig() throws ConfigurationException {
		loadConfig();
		return m_config;
	}
	
	public synchronized GroupMembershipServiceProviderConfig getConfigForUpdate() throws ConfigurationException {
		loadConfig();
		if (m_config == null) {
			return null;
		}
		return m_config.copy();
	}
	
	
	public void map(Element rootData) throws ConfigurationException {
		// if no rootData, reset config to null (clean up previous instance)
		if (rootData == null) {
			m_config = null;
		} else {
			m_config = new GroupMembershipServiceProviderConfig();
			GroupMembershipServiceProviderConfigMapper.map(getConfigFileName(), rootData, m_config);				
		}
	}
	
	public String getConfigFileName() {
		return CONFIG_FILENAME;
	}

	public String getSchemaFileName() {
		return SCHEMA_FILENAME;

	}

	public String getRootElementName() {
		return ROOT_ELEMENT;
	}
	

}
