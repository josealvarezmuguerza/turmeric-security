/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class RateLimiterUtilsTest.
 * 
 * @author gbaal
 */
public class RateLimiterUtilsTest {
	private RateLimiterUtils limiterUtils;

	/**
	 * Inits the.
	 */
	@Before
	public void init() {
		System.setProperty("log4j.configuration", "META-INF/config/cassandra/log4j.properties");
		System.setProperty("cassandra.config", "META-INF/config/cassandra/cassandra-test.yaml");
		limiterUtils = new RateLimiterUtils("test", false);
	}

	/**
	 * Tear down.
	 */
	@After
	public void tearDown() {
		limiterUtils = null;
	}

	/**
	 * Testsimple exp.
	 */
	@Test
	public void testsimpleExp() throws Exception {
		Boolean flag;
		flag = limiterUtils.getFinalresult("1>2");
		assertEquals("\"1>2\" should  be false", false, flag);
		flag = limiterUtils.getFinalresult(" 1   >  3  ");
		assertEquals("\" 1   >  3  \" should  be false", false, flag);
		flag = limiterUtils.getFinalresult("4 > 2");
		assertEquals("\"4 > 2\" should  be true", true, flag);
		flag = limiterUtils.getFinalresult("1>1");
		assertEquals("\"1>1 \" should  be false", false, flag);
	}

	/**
	 * Test or exp.
	 */
	@Test
	public void testOrExp() throws Exception {
		Boolean flag;
		flag = limiterUtils.getFinalresult("1>2||3>6");
		assertEquals("\"1>2||3>6\" should  be false", false, flag);
		flag = limiterUtils.getFinalresult(" 1   >  3  || 4 > 6 || 5 >7 ");
		assertEquals("\" 1   >  3  || 4 > 6 || 5 >7  \" should  be false",
				false, flag);
		flag = limiterUtils.getFinalresult("1   >  3  || 4 > 6  || 4 > 2");
		assertEquals("\"1   >  3  || 4 > 6  || 4 > 2\" should  be true", true,
				flag);
		flag = limiterUtils.getFinalresult("1>1 || 2>10 || 3>5");
		assertEquals("\"1>1 || 2>10 || 3>5 \" should  be false", false, flag);

	}

	/**
	 * Test and exp.
	 */
	@Test
	public void testAndExp() throws Exception {
		Boolean flag;
		flag = limiterUtils.getFinalresult("1>2&&3>6");
		assertEquals("\"1>2||3>6\" should  be false", false, flag);
		flag = limiterUtils.getFinalresult(" 1   >  3  && 4 > 6 && 5 >7 ");
		assertEquals("\" 1   >  3  || 4 > 6 || 5 >7  \" should  be false",
				false, flag);
		flag = limiterUtils.getFinalresult("1   <  3  && 4 < 6  && 4 > 2");
		assertEquals("\"1   <  3  && 4 < 6  && 4 > 2\" should  be true", true,
				flag);
	}

	/**
	 * Test init provider.
	 */
	@Test
	public void testInitProvider() {
		assertNotNull("limiterUtils is null", limiterUtils);
	}

	/**
	 * Test ivalid expression.
	 */
	@Test
	public void testIvalidExpression() {
		try {
			limiterUtils.getFinalresult("1>>2");
			fail("invalid expression should fail");
		} catch (Exception e) {
		}
		try {
			limiterUtils.getFinalresult("1>>2 2");
			fail("invalid expression should fail");
		} catch (Exception e) {
		}

		try {
			limiterUtils.getFinalresult("1>>2 && 2 >1");
			fail("invalid expression should fail");
		} catch (Exception e) {
		}

	}
}
