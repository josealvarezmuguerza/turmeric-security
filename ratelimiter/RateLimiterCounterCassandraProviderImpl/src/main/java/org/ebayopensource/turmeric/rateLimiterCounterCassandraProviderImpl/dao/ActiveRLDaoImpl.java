/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.rateLimiterCounterCassandraProviderImpl.dao;

import org.ebayopensource.turmeric.rateLimiterCounterCassandraProviderImpl.model.ActiveRL;
import org.ebayopensource.turmeric.utils.cassandra.dao.AbstractColumnFamilyDao;


/*
 *  @author jamuguerza
 */
public class ActiveRLDaoImpl extends AbstractColumnFamilyDao<String, ActiveRL>
		implements ActiveRLDao {

	/**
	 * Instantiates a new active rl dao impl.
	 *
	 * @param host the host
	 * @param keySpace the key space
	 * @param cf the cf
	 */
	public ActiveRLDaoImpl(final String host, final String keySpace, final String cf) {
		super(host, keySpace, String.class, ActiveRL.class, cf);
	}

	

}