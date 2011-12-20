/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.rateLimiterCounterCassandraProviderImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.ebayopensource.turmeric.manager.cassandra.server.CassandraTestManager;
import org.ebayopensource.turmeric.rateLimiterCounterCassandraProviderImpl.dao.ActiveEffectDao;
import org.ebayopensource.turmeric.rateLimiterCounterCassandraProviderImpl.dao.ActiveEffectDaoImpl;
import org.ebayopensource.turmeric.rateLimiterCounterCassandraProviderImpl.dao.ActiveRLDao;
import org.ebayopensource.turmeric.rateLimiterCounterCassandraProviderImpl.dao.ActiveRLDaoImpl;
import org.ebayopensource.turmeric.rateLimiterCounterProvider.Policy.model.RateLimiterPolicyModel;
import org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;


/**
 * Unit test for simple RateLimiterCounterCassandraProviderImpl
 * @author jamuguerza.
 */
public class RateLimiterCounterCassandraProviderImplTest {
	RateLimiterCounterCassandraProviderImpl providerImpl = null;
	final private String rl_key = "demo_rl";
	final private String effect_key = "demo_effect";
	private RateLimiterPolicyModel rateLimiterPolicyModel_1;
	final private long createdTime = System.currentTimeMillis();

    
	@BeforeClass
	public static void setupCassandraConfigFile() throws Exception {
		System.setProperty("log4j.configuration", "META-INF/config/cassandra/log4j.properties");
		System.setProperty("cassandra.config", "META-INF/config/cassandra/cassandra-test.yaml");
		CassandraTestManager.initialize();
		
		ActiveRLDao activeRLDao = new ActiveRLDaoImpl("TestCluster", "127.0.1.10", "rl", "activeRL", String.class);
		ActiveEffectDao activeEffectDao = new ActiveEffectDaoImpl("TestCluster", "127.0.1.10", "rl", "activeEffect", String.class);

	}

	
	@Before
	public void setUp() throws Exception {
//		CassandraTestManager.cleanUpCassandraDirs();
		rateLimiterPolicyModel_1 = createModel();
		providerImpl = new RateLimiterCounterCassandraProviderImpl();
	}

	@After
	public void tearDown() {
		providerImpl = null;
		rateLimiterPolicyModel_1 = null;

		CassandraTestManager.cleanUpCassandraDirs();
	}
	
	private RateLimiterPolicyModel createModel() {
		rateLimiterPolicyModel_1 = new RateLimiterPolicyModel();

		rateLimiterPolicyModel_1.setActive(true);
		rateLimiterPolicyModel_1.setCount(1);
		rateLimiterPolicyModel_1.setEffect(RateLimiterStatus.BLOCK);
		rateLimiterPolicyModel_1.setRolloverPeriod(60L);
		rateLimiterPolicyModel_1.setTimestamp(new Date(createdTime));
		rateLimiterPolicyModel_1.setEffectDuration(createdTime + 3600L * 1000 );
		rateLimiterPolicyModel_1.setIp("127.0.0.1");
		return rateLimiterPolicyModel_1;
	}
	
	@Test
	public void testGetCassandraConfig() {
		Method getCassandraConfigMethod;
		try {
			getCassandraConfigMethod = RateLimiterCounterCassandraProviderImpl.class.getDeclaredMethod("getCassandraConfig", null);
			getCassandraConfigMethod.setAccessible(true);
			getCassandraConfigMethod.invoke(providerImpl, null);
		
			//host
			String hostValue = getFieldValue(providerImpl, "host");
			assertEquals("Host:port value does not map", "127.0.1.10:9160", hostValue);

			//clusterName
			String clusterNameValue = getFieldValue(providerImpl, "clusterName");
			assertEquals("clusterName value does not map", "TestCluster", clusterNameValue);
			
			//keyspace
			String keyspaceValue = getFieldValue(providerImpl, "keyspace");
			assertEquals("keyspace value does not map", "rl", keyspaceValue);
			
			//activeRLCF
			String activeRLCFValue = getFieldValue(providerImpl, "activeRLCF");
			assertEquals(" activeRLCF value does not map", "activeRL", activeRLCFValue);
			
			//activeEffectCF
			String activeEffectCFValue = getFieldValue(providerImpl, "activeEffectCF");
			assertEquals(" activeEffectCF value does not map", "activeEffect", activeEffectCFValue);
			
			//embedded
			String embeddedValue = getFieldValue(providerImpl, "embedded");
			assertEquals("embedded value does not map", "true", embeddedValue);
			
		} catch (Exception e) {
			fail("cannot invoke getCassandraConfig method ");
		}
	}

	private String getFieldValue(final RateLimiterCounterCassandraProviderImpl rLCounterImpl,
			final String fieldName) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
		Field field = RateLimiterCounterCassandraProviderImpl.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return (String) field.get(rLCounterImpl);		
	}

	
	@Test
	public void testGetActiveRLKeys() {
		Set<String> activeRLKeys = providerImpl.getActiveRLKeys();
		assertNotNull(activeRLKeys);
		assertTrue(activeRLKeys.isEmpty());
		
		providerImpl.addActiveRL(rl_key, rateLimiterPolicyModel_1);
		RateLimiterPolicyModel rateLimiterPolicyModel_2 = new 	RateLimiterPolicyModel();
		rateLimiterPolicyModel_2.setActive(false);
		rateLimiterPolicyModel_2.setCount(rateLimiterPolicyModel_1.getCount());
		rateLimiterPolicyModel_2.setEffect(rateLimiterPolicyModel_1.getEffect());
		rateLimiterPolicyModel_2.setEffectDuration(rateLimiterPolicyModel_1.getEffectDuration());
		rateLimiterPolicyModel_2.setIp(rateLimiterPolicyModel_1.getIp());
		rateLimiterPolicyModel_2.setRolloverPeriod(rateLimiterPolicyModel_1.getRolloverPeriod());
		rateLimiterPolicyModel_2.setTimestamp(rateLimiterPolicyModel_1.getTimestamp());
		
		providerImpl.addActiveRL(rl_key, rateLimiterPolicyModel_1);
		providerImpl.addActiveRL(rl_key+"01", rateLimiterPolicyModel_2);
		Set<String> activeRLKeys2 = providerImpl.getActiveRLKeys(); 
		assertEquals("activeRL should have 2 elements", 2, activeRLKeys2.size());
		assertTrue(rl_key + " should be in activeRLCF ", activeRLKeys2.contains(rl_key));
		assertTrue(rl_key+"01" + " should be in activeRLCF ", activeRLKeys2.contains(rl_key+"01"));
	}
	
	@Test
	public void testAddActiveRL() {
		providerImpl.addActiveRL(rl_key, rateLimiterPolicyModel_1);

		RateLimiterPolicyModel activeRL = providerImpl.getActiveRL(rl_key) ;
		assertNotNull("rateLimiterPolicyModel_1 element must exist for key"+ rl_key, activeRL );
		assertEquals(rateLimiterPolicyModel_1.getCount(), activeRL.getCount());
		assertEquals(rateLimiterPolicyModel_1.getEffect(),	activeRL.getEffect());
		assertEquals(rateLimiterPolicyModel_1.getEffectDuration(), 	activeRL.getEffectDuration());
		assertEquals(rateLimiterPolicyModel_1.getIp(), activeRL.getIp());
		assertEquals(rateLimiterPolicyModel_1.getRolloverPeriod(),	activeRL.getRolloverPeriod());
		assertEquals(new Date(createdTime).getTime(), activeRL.getTimestamp().getTime());
		
	}
	
	@Test
	public void testAddActiveEffect() throws Exception {
		providerImpl.addActiveEffect(effect_key, rateLimiterPolicyModel_1);
		
		Field field = RateLimiterCounterCassandraProviderImpl.class.getDeclaredField("activeEffectDao");
		field.setAccessible(true);
		ActiveEffectDaoImpl activeEffectDaoImpl =  (ActiveEffectDaoImpl) field.get(providerImpl);		
	
		assertTrue(effect_key + " key must exist in effect CF ", activeEffectDaoImpl.containsKey(effect_key));
		
		RateLimiterPolicyModel activeEffect = activeEffectDaoImpl.find(effect_key);
		assertNotNull("rateLimiterPolicyModel_1 element must exist for key"+ effect_key, activeEffect);
		assertEquals(rateLimiterPolicyModel_1.getCount(), activeEffect.getCount());
		assertEquals(rateLimiterPolicyModel_1.getEffect(),	activeEffect.getEffect());
		assertEquals(rateLimiterPolicyModel_1.getEffectDuration(), 	activeEffect.getEffectDuration());
		assertEquals(rateLimiterPolicyModel_1.getIp(), activeEffect.getIp());
		assertEquals(rateLimiterPolicyModel_1.getRolloverPeriod(),	activeEffect.getRolloverPeriod());
		assertEquals(new Date(createdTime).getTime(), activeEffect.getTimestamp().getTime());
		
	}
	
	@Test
	public void testCointainKeyInActiveRL() {
		providerImpl.addActiveRL(rl_key, rateLimiterPolicyModel_1);
		assertTrue(rl_key + " key must exist in activeRLCF ", providerImpl.cointainKeyInActiveRL(rl_key) );
	}
	
	@Test
	public void testIncrementRLCounter() {
		providerImpl.addActiveRL(rl_key, createModel());
		providerImpl.addActiveRL(rl_key + "01", createModel());
		
		assertTrue("No element found in activeRL map", providerImpl.cointainKeyInActiveRL(rl_key));
		
		providerImpl.incrementRLCounter(rl_key);
		assertEquals("rl_key counter should be 2", 2,  providerImpl.getActiveRL(rl_key).getCount());

		providerImpl.incrementRLCounter(rl_key);
		assertEquals("rl_key counter should be 3", 3,  providerImpl.getActiveRL(rl_key).getCount());
		
		assertEquals("rl_key counter should be 1", 1,  providerImpl.getActiveRL(rl_key + "01").getCount());
	}

	@Test
	public void testSetRLCounter() {
		
		providerImpl.setRLCounter(rl_key, 1);
		assertEquals("rl_key counter should be 1", 1,  providerImpl.getActiveRL(rl_key).getCount());

		providerImpl.setRLCounter(rl_key, 5);
		assertEquals("rl_key counter should be 5", 5,  providerImpl.getActiveRL(rl_key).getCount());
		
		assertEquals("rl_key counter should be 1", 1,  providerImpl.getActiveRL(rl_key + "01").getCount());
	}
	
	@Test
	public void testRessetEffect() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		providerImpl.addActiveEffect(effect_key, createModel());
		RateLimiterPolicyModel rateLimiterPolicyModel_2 = createModel();
		rateLimiterPolicyModel_2.setEffectDuration(createdTime - 60 * 1000);
		providerImpl.addActiveEffect(effect_key + "01", rateLimiterPolicyModel_2);
		
		providerImpl.resetEffects();
		
		Field field = RateLimiterCounterCassandraProviderImpl.class.getDeclaredField("activeEffectDao");
		field.setAccessible(true);
		ActiveEffectDaoImpl activeEffectDaoImpl =  (ActiveEffectDaoImpl) field.get(providerImpl);		
	
		assertTrue(effect_key + " key must exist in effect CF ", activeEffectDaoImpl.containsKey(effect_key));
		assertNotNull(effect_key + " should not be null", activeEffectDaoImpl.find(effect_key));
		assertNull(effect_key + " 01 must be null", activeEffectDaoImpl.find(effect_key + "01"));
	}
	
	
	
}
