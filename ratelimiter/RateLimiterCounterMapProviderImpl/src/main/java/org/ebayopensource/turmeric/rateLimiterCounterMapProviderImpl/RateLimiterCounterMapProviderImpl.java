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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ebayopensource.turmeric.rateLimiterCounterProvider.RateLimiterCounterProvider;
import org.ebayopensource.turmeric.rateLimiterCounterProvider.Policy.model.RateLimiterPolicyModel;
import org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus;

/**
 * The Class RateLimiterCounterMapProviderImpl.
 * 
 * @author jamuguerza
 */
public class RateLimiterCounterMapProviderImpl implements
		RateLimiterCounterProvider {

	/** The active rl map. */
	private static Map<String, RateLimiterPolicyModel> activeRLMap;

	/** The active effect map. */
	private static Map<String, RateLimiterPolicyModel> activeEffectMap;

	/**
	 * Gets the active rl.
	 * 
	 * @return the active rl
	 */
	private Map<String, RateLimiterPolicyModel> getActiveRLMap() {
		if (activeRLMap == null) {
			activeRLMap = new ConcurrentHashMap<String, RateLimiterPolicyModel>();
		}
		return activeRLMap;
	}

	/**
	 * Gets the active effects.
	 * 
	 * @return the active effects
	 */
	private Map<String, RateLimiterPolicyModel> getActiveEffectMap() {
		if (activeEffectMap == null) {
			activeEffectMap = new ConcurrentHashMap<String, RateLimiterPolicyModel>();
		}
		return activeEffectMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#cointainKeyInActiveRL(java.lang.String)
	 */
	public boolean cointainKeyInActiveRL(final String key) {
		return getActiveRLMap().containsKey(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#addActiveRL(java.lang.String,
	 * org.ebayopensource
	 * .turmeric.rateLimiterCounterProvider.Policy.model.RateLimiterPolicyModel)
	 */
	public void addActiveRL(final String key,
			final RateLimiterPolicyModel rateLimiterPolicyModel) {
		getActiveRLMap().put(key, rateLimiterPolicyModel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#addActiveEffect(java.lang.String,
	 * org.ebayopensource
	 * .turmeric.rateLimiterCounterProvider.Policy.model.RateLimiterPolicyModel)
	 */
	public void addActiveEffect(final String key,
			final RateLimiterPolicyModel rateLimiterPolicyModel) {
		getActiveEffectMap().put(key, rateLimiterPolicyModel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#removeActiveEffect(java.lang.String)
	 */
	public void removeActiveEffect(final String key) {
		getActiveEffectMap().remove(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#getActiveRL(java.lang.String)
	 */
	public RateLimiterPolicyModel getActiveRL(final String key) {
		return getActiveRLMap().get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#incrementRLCounter(java.lang.String)
	 */
	public void incrementRLCounter(final String key) {
		RateLimiterPolicyModel limiterPolicyModel = getActiveRLMap().get(key);
		getActiveRLMap().get(key).setCount(limiterPolicyModel.getCount() + 1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#setRLCounter(java.lang.String, int)
	 */
	public void setRLCounter(final String key, int i) {
		RateLimiterPolicyModel rateLimiterPolicyModel = getActiveRLMap().get(
				key);
		if (rateLimiterPolicyModel == null) {
			rateLimiterPolicyModel = new RateLimiterPolicyModel();
			getActiveRLMap().put(key, rateLimiterPolicyModel);
		}
		getActiveRLMap().get(key).setCount(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#setRLTimestamp(java.lang.String,
	 * java.util.Date)
	 */
	public void setRLTimestamp(final String key, final Date date) {
		getActiveRLMap().get(key).setTimestamp(date);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#setRLActive(java.lang.String, boolean)
	 */
	public void setRLActive(final String key, boolean b) {
		getActiveRLMap().get(key).setActive(b);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#setRLEffectDuration(java.lang.String,
	 * java.lang.Long)
	 */
	public void setRLEffectDuration(final String key, final Long duration) {
		getActiveRLMap().get(key).setEffectDuration(duration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#setRLEffect(java.lang.String,
	 * org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus)
	 */
	public void setRLEffect(final String key, final RateLimiterStatus effect) {
		getActiveRLMap().get(key).setEffect(effect);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#setRLRolloverPeriod(java.lang.String,
	 * java.lang.Long)
	 */
	public void setRLRolloverPeriod(final String key, final Long rollover) {
		getActiveRLMap().get(key).setRolloverPeriod(rollover);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#resetEffects()
	 */
	public void resetEffects() {
		for (Map.Entry<String, RateLimiterPolicyModel> entry : getActiveEffectMap()
				.entrySet()) {
			resetEffect(entry.getKey());
		}
	}

	// remove from database if effect duration is < now
	/**
	 * Reset effect.
	 * 
	 * @param currentSubjectOrGroup
	 *            the current subject or group
	 */
	private void resetEffect(String currentSubjectOrGroup) {
		if (currentSubjectOrGroup != null) {
			currentSubjectOrGroup = currentSubjectOrGroup.trim();
		}
		if (getActiveEffectMap().containsKey(currentSubjectOrGroup)) {
			RateLimiterPolicyModel limiterPolicyModel = getActiveEffectMap()
					.get(currentSubjectOrGroup);

			// get current date
			java.util.Date date = new java.util.Date();
			if (date.after(new Date(limiterPolicyModel.getEffectDuration()))) {
				// remove it
				getActiveEffectMap().remove(currentSubjectOrGroup);

				if (getActiveRLMap().containsKey(currentSubjectOrGroup)) {
					getActiveRLMap().remove(currentSubjectOrGroup);
					getActiveRLMap().put(currentSubjectOrGroup,
							new RateLimiterPolicyModel());
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ebayopensource.turmeric.rateLimiterCounterProvider.
	 * RateLimiterCounterProvider#getActiveRLKeys()
	 */
	public Set<String> getActiveRLKeys() {
		return getActiveRLMap().keySet();
	}

}
