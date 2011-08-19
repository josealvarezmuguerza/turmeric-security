/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.rateLimiterCounterProvider;

import java.util.Date;
import java.util.Set;

import org.ebayopensource.turmeric.rateLimiterCounterProvider.Policy.model.RateLimiterPolicyModel;
import org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus;

/**
 * A generic provider for adopters to implement their own rules for the Rate
 * Limiter service. This currently extends the interface for RateLimiterService
 * itself, but may be extended later to include more specific functionality.
 * 
 * @author jamuguerza
 */
public interface RateLimiterCounterProvider {

	
	/**
	 * Cointain key in active rl.
	 *
	 * @param key the key
	 * @return true, if cointain key in active rl
	 */
	public boolean cointainKeyInActiveRL(String key);

	/**
	 * Adds the active rl.
	 *
	 * @param key the key
	 * @param rateLimiterPolicyModel the rate limiter policy model
	 */
	public void addActiveRL(String key,
			RateLimiterPolicyModel rateLimiterPolicyModel);

	/**
	 * Gets the active rl.
	 *
	 * @param key the key
	 * @return the active rl
	 */
	public RateLimiterPolicyModel getActiveRL(String key);

	/**
	 * Adds the active effect.
	 *
	 * @param key the key
	 * @param rateLimiterPolicyModel the rate limiter policy model
	 */
	public void addActiveEffect(String key,
			RateLimiterPolicyModel rateLimiterPolicyModel);

	/**
	 * Removes the active effect.
	 *
	 * @param key the key
	 */
	public void removeActiveEffect(String key);

	/**
	 * Increment rl counter.
	 *
	 * @param key the key
	 */
	public void incrementRLCounter(String key);

	/**
	 * Sets the rl counter.
	 *
	 * @param key the key
	 * @param i the i
	 */
	public void setRLCounter(String key, int i);

	/**
	 * Sets the rl timestamp.
	 *
	 * @param key the key
	 * @param date the date
	 */
	public void setRLTimestamp(String key, Date date);

	/**
	 * Sets the rl active.
	 *
	 * @param key the key
	 * @param b the b
	 */
	public void setRLActive(String key, boolean b);

	/**
	 * Sets the rl effect duration.
	 *
	 * @param key the key
	 * @param duration the duration
	 */
	public void setRLEffectDuration(String key, Long duration);

	/**
	 * Sets the rl effect.
	 *
	 * @param key the key
	 * @param effect the effect
	 */
	public void setRLEffect(String key, RateLimiterStatus effect);

	/**
	 * Sets the rl rollover period.
	 *
	 * @param ket the ket
	 * @param rollover the rollover
	 */
	public void setRLRolloverPeriod(String ket, Long rollover);

	/**
	 * Reset effects.
	 */
	public void resetEffects();

	/**
	 * Gets the active rl keys.
	 *
	 * @return the active rl keys
	 */
	public Set<String> getActiveRLKeys();

}
