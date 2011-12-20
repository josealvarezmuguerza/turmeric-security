/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.rateLimiterCounterMapProviderImpl;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ebayopensource.turmeric.rateLimiterCounterProvider.Policy.model.RateLimiterPolicyModel;
import org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus;

/**
 * Unit test for simple RateLimiterCounterMapProviderImpl.
 * 
 * @author jamuguerza
 */
public class RateLimiterCounterMapProviderImplTest {

	private RateLimiterCounterMapProviderImpl providerImpl;
	private RateLimiterPolicyModel rateLimiterPolicyModel_1;
	private Method getActiveRLMapMethod;
	private Method getActiveEffectMapMethod;

	final private String rl_key = "demo_rl";
	final private String effect_key = "demo_effect";
	final private long createdTime = System.currentTimeMillis();


	
	@Before
	public void setup() {

		rateLimiterPolicyModel_1 = createModel();
		providerImpl = new RateLimiterCounterMapProviderImpl();
	
		try {
			getActiveRLMapMethod = providerImpl.getClass()
					.getDeclaredMethod("getActiveRLMap", null);
			getActiveRLMapMethod.setAccessible(true);
			
			getActiveEffectMapMethod = providerImpl.getClass()
			.getDeclaredMethod("getActiveEffectMap", null);
			getActiveEffectMapMethod.setAccessible(true);
	
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@After
	public void tearDown() {
		getActiveRLMapMethod = null;
		getActiveEffectMapMethod = null;
		providerImpl = null;
	}

	@Test
	public void testAddActiveRL() {
		providerImpl.addActiveRL(rl_key, rateLimiterPolicyModel_1);

		try {
			@SuppressWarnings("unchecked")
			Map<String, RateLimiterPolicyModel> activeRLMap = (Map<String, RateLimiterPolicyModel>) getActiveRLMapMethod
					.invoke(providerImpl, null);

			assertNotNull("activeRLMap should contain one element", activeRLMap);
			assertFalse("activeRLMap should contain one element", activeRLMap.isEmpty());
			assertNotNull("rateLimiterPolicyModel_1 element must exist for key"+ rl_key, activeRLMap.get(rl_key));
			assertEquals(rateLimiterPolicyModel_1.getCount(), activeRLMap.get(rl_key).getCount());
			assertEquals(rateLimiterPolicyModel_1.getEffect(),	activeRLMap.get(rl_key).getEffect());
			assertEquals(rateLimiterPolicyModel_1.getEffectDuration(), 	activeRLMap.get(rl_key).getEffectDuration());
			assertEquals(rateLimiterPolicyModel_1.getIp(), activeRLMap.get(rl_key)	.getIp());
			assertEquals(rateLimiterPolicyModel_1.getRolloverPeriod(),	activeRLMap.get(rl_key).getRolloverPeriod());
			assertEquals(new Date(createdTime).getTime(), activeRLMap.get(rl_key).getTimestamp().getTime());
		
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testAddActiveEffect() {
		providerImpl.addActiveEffect(effect_key, rateLimiterPolicyModel_1);

		try {
			Map<String, RateLimiterPolicyModel> activeEffectMap = (Map<String, RateLimiterPolicyModel>) getActiveEffectMapMethod
					.invoke(providerImpl, null);

			assertNotNull("activeEffectMap should contain one element", activeEffectMap);
			assertFalse("activeEffectMap should contain one element", activeEffectMap.isEmpty());
			assertNotNull("rateLimiterPolicyModel_1 element must exist for key"+ effect_key, activeEffectMap.get(effect_key));
			assertEquals(rateLimiterPolicyModel_1.getCount(), activeEffectMap.get(effect_key).getCount());
			assertEquals(rateLimiterPolicyModel_1.getEffect(),	activeEffectMap.get(effect_key).getEffect());
			assertEquals(rateLimiterPolicyModel_1.getEffectDuration(), 	activeEffectMap.get(effect_key).getEffectDuration());
			assertEquals(rateLimiterPolicyModel_1.getIp(), activeEffectMap.get(effect_key)	.getIp());
			assertEquals(rateLimiterPolicyModel_1.getRolloverPeriod(),	activeEffectMap.get(effect_key).getRolloverPeriod());
			assertEquals(new Date(createdTime).getTime(), activeEffectMap.get(effect_key).getTimestamp().getTime());
		
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	
	
	@Test
	public void testCointainKeyInActiveRL() {
		providerImpl.addActiveRL(rl_key, rateLimiterPolicyModel_1);

		assertTrue("No element found in activeRL map", providerImpl.cointainKeyInActiveRL(rl_key));
		assertFalse("An element was found with invalid key", providerImpl.cointainKeyInActiveRL(rl_key+"1"));
		assertFalse("An element was found with invalid key", providerImpl.cointainKeyInActiveRL(""));
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
		
		providerImpl.setRLCounter(rl_key, 0); //any value
		assertEquals("rl_key counter should be 1", 1,  providerImpl.getActiveRL(rl_key).getCount());

		providerImpl.setRLCounter(rl_key, 5);
		assertEquals("rl_key counter should be 1", 1,  providerImpl.getActiveRL(rl_key).getCount());
		
		assertEquals("rl_key counter should be 1", 1,  providerImpl.getActiveRL(rl_key + "01").getCount());
	}
	
	
	@Test
	public void testRessetEffect() {
		providerImpl.addActiveEffect(effect_key, createModel());
		providerImpl.addActiveRL(effect_key, createModel());
		RateLimiterPolicyModel rateLimiterPolicyModel_2 = createModel();
		rateLimiterPolicyModel_2.setEffectDuration(createdTime - 60 * 1000);
		providerImpl.addActiveEffect(effect_key + "01", rateLimiterPolicyModel_2);
		
		providerImpl.resetEffects();
		try {
			Map<String, RateLimiterPolicyModel> activeEffectMap = (Map<String, RateLimiterPolicyModel>) getActiveEffectMapMethod
			.invoke(providerImpl, null);
  
			assertNotNull(effect_key + " should not be null", activeEffectMap.get(effect_key));
			assertNull(effect_key + " 01 must be null", activeEffectMap.get(effect_key + "01"));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
			
			
		
	}
	
	
	@Test
	public void testSetRolloverPeriod() {
		providerImpl.addActiveRL(rl_key, createModel());
		providerImpl.addActiveRL(rl_key + "01", createModel());
		
		providerImpl.setRLRolloverPeriod(rl_key, 1000L);
		assertEquals("rl_key counter should be 1000 as Long",1000L,  providerImpl.getActiveRL(rl_key).getRolloverPeriod().longValue());

		assertEquals("rl_key counter should be 60 as Long", 60L,  providerImpl.getActiveRL(rl_key + "01").getRolloverPeriod().longValue());
	}
	
	@Test
	public void testSetEffect() {
		providerImpl.addActiveRL(rl_key, createModel());
		providerImpl.addActiveRL(rl_key + "01", createModel());
		
		providerImpl.setRLEffect(rl_key, RateLimiterStatus.SERVE_GIF);
		assertEquals("rl_key counter should be SERVE_GIF",RateLimiterStatus.SERVE_GIF,  providerImpl.getActiveRL(rl_key).getEffect());
		assertEquals("rl_key counter should be BLOCK",RateLimiterStatus.BLOCK,  providerImpl.getActiveRL(rl_key + "01").getEffect());

	}
	
	@Test
	public void testSetActive() {
		providerImpl.addActiveRL(rl_key, createModel());
		providerImpl.addActiveRL(rl_key + "01", createModel());
		
		providerImpl.setRLActive(rl_key, false);
		assertEquals("rl_key counter should be false", false,  providerImpl.getActiveRL(rl_key).isActive());
		assertEquals("rl_key counter should be true", true,  providerImpl.getActiveRL(rl_key + "01").isActive());
	}

	@Test
	public void testSetEffectDuration() {
		providerImpl.addActiveRL(rl_key, createModel());
		providerImpl.addActiveRL(rl_key + "01", createModel());
		long effectDuration = createdTime + 3600L * 1000; 
		providerImpl.setRLEffectDuration(rl_key, 5000L);
		assertEquals("rl_key counter should be 5000 as long",5000L,  providerImpl.getActiveRL(rl_key).getEffectDuration().longValue());
		assertEquals("rl_key counter should be " + effectDuration + " as Long",effectDuration ,  providerImpl.getActiveRL(rl_key + "01").getEffectDuration().longValue());
	}
	
	@Test
	public void testSetTimestamp() {
		providerImpl.addActiveRL(rl_key, createModel());
		providerImpl.addActiveRL(rl_key + "01", createModel());
		long newCreatedTime = System.currentTimeMillis();
		providerImpl.setRLTimestamp(rl_key, new Date(newCreatedTime));
		assertEquals("rl_key counter should be " + newCreatedTime, newCreatedTime,  providerImpl.getActiveRL(rl_key).getTimestamp().getTime());
		assertEquals("rl_key counter should be " + createdTime, createdTime,  providerImpl.getActiveRL(rl_key + "01").getTimestamp().getTime());
	}
	
	@Test
	public void testMixingMaps() {
		providerImpl.addActiveEffect(effect_key, rateLimiterPolicyModel_1);
		assertFalse("Element found in activeRL map while insertion was in Effect Map", providerImpl.cointainKeyInActiveRL(effect_key));
		try {
			@SuppressWarnings("unchecked")
			Map<String, RateLimiterPolicyModel> activeEffectMap = (Map<String, RateLimiterPolicyModel>) getActiveEffectMapMethod
					.invoke(providerImpl, null);
			
			assertTrue("Element must be present in Effect map", activeEffectMap.containsKey(effect_key));
		} catch (Exception e) {
			fail(e.getMessage());
		}

	}
	
	
	@Test
	public void testMixingMapsReverse() {
		providerImpl.addActiveRL(rl_key, rateLimiterPolicyModel_1);

		assertTrue("Element must be present in activeRL map ", providerImpl.cointainKeyInActiveRL(rl_key));

		try {
			@SuppressWarnings("unchecked")
			Map<String, RateLimiterPolicyModel> activeEffectMap = (Map<String, RateLimiterPolicyModel>) getActiveEffectMapMethod
					.invoke(providerImpl, null);
			
			assertFalse("Element found in Effect map while insertion was in RL Map", activeEffectMap.containsKey(rl_key));
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}
	
	
	@Test
	public void testGetActiveRL() {
		providerImpl.addActiveRL(rl_key, rateLimiterPolicyModel_1);
    	assertEquals("No element found in activeRL map", rateLimiterPolicyModel_1,  providerImpl.getActiveRL(rl_key));
		assertFalse("An element was found with invalid key", providerImpl.cointainKeyInActiveRL(rl_key+"1"));
		assertFalse("An element was found with invalid key", providerImpl.cointainKeyInActiveRL(""));
	}
			

	@Test
	public void testGetActiveRLKeys() {
		providerImpl.addActiveRL(rl_key, rateLimiterPolicyModel_1);
		providerImpl.addActiveRL(rl_key + "01", rateLimiterPolicyModel_1);
		providerImpl.addActiveRL(rl_key + "02", rateLimiterPolicyModel_1);

		assertNotNull("No activeRL map found",  providerImpl.getActiveRLKeys());
		assertEquals("activeRL map should have 3 elements",  3, providerImpl.getActiveRLKeys().size());
		
		Set<String> activeRLKeys = providerImpl.getActiveRLKeys();
		activeRLKeys.contains(rl_key );
		activeRLKeys.contains(rl_key + "01");
		activeRLKeys.contains(rl_key + "02");
		
	}
			
	@Test
	public void testRemoveActiveEffectElement() {
		providerImpl.addActiveEffect(effect_key, rateLimiterPolicyModel_1);
		providerImpl.addActiveEffect(effect_key + "01", rateLimiterPolicyModel_1);
		providerImpl.addActiveEffect(effect_key + "02", rateLimiterPolicyModel_1);

		try {
			@SuppressWarnings("unchecked")
			Map<String, RateLimiterPolicyModel> activeEffectMap = (Map<String, RateLimiterPolicyModel>) getActiveEffectMapMethod
					.invoke(providerImpl, null);

			assertNotNull("No activeEffect map found",  activeEffectMap);
			assertEquals("activeEffect map should have 3 elements",  3, activeEffectMap.size());
			
			providerImpl.removeActiveEffect(effect_key + "02");

			assertEquals("activeEffect map should have 2 elements",  2, activeEffectMap.size());
			assertNull("Effect_key02 value must not exist",  activeEffectMap.get(effect_key + "02"));
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

//	@Test
//	public void testRemoveActiveEffects() {
//		providerImpl.addActiveEffect(effect_key, rateLimiterPolicyModel_1);
//		providerImpl.addActiveEffect(effect_key + "01", rateLimiterPolicyModel_1);
//		providerImpl.addActiveEffect(effect_key + "02", rateLimiterPolicyModel_1);
//
//		try {
//			@SuppressWarnings("unchecked")
//			Map<String, RateLimiterPolicyModel> activeEffectMap = (Map<String, RateLimiterPolicyModel>) getActiveEffectMapMethod
//					.invoke(providerImpl, null);
//
//			assertNotNull("No activeEffect map found",  activeEffectMap);
//			assertEquals("activeEffect map should have 3 elements",  3, activeEffectMap.size());
//			
//			providerImpl.resetEffects();
//			
//			assertEquals("activeEffect map should be empty",  0, activeEffectMap.size());
//			
//		} catch (Exception e) {
//			fail(e.getMessage());
//		}
//	}
	
	

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

}
