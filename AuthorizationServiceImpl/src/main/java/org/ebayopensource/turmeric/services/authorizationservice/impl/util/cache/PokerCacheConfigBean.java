/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl.util.cache;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.ebayopensource.turmeric.utils.cache.PokerCache;

import com.ebay.kernel.bean.configuration.BaseConfigBean;
import com.ebay.kernel.bean.configuration.BeanConfigCategoryInfo;
import com.ebay.kernel.bean.configuration.BeanPropertyInfo;
import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;

/**
 * Configuration Management Bean to represent (and configure) a {@link PokerCache}.
 * 
 * @author mpoplacenel
 */
public class PokerCacheConfigBean extends BaseConfigBean {

	private static final long serialVersionUID = 0L;
	
	/**
	 * Name of the poker property - this should match the name of the member variable
	 * {@link #poker}.
	 */
	protected static final String POKER_PROPERTY_NAME = "poker";
	
	/**
	 * The name of the "poke" property to be used from the configuration page. 
	 */
	protected static final String POKER_PROPERTY_EXTERNAL_NAME = "pokeCache";
	
	private final transient PokerCache cache;
	
	private final String name;
	
	/**
	 * This will be got and set through reflection by the Config Bean fwk.
	 */
	private Boolean poker;
	
	private static final Logger LOGGER = Logger.getInstance(PokerCacheConfigBean.class);
	
	private static final BeanPropertyInfo POKE_CACHE_PROP_INFO = 
		createBeanPropertyInfo(POKER_PROPERTY_NAME, POKER_PROPERTY_EXTERNAL_NAME, true);
	
	/**
	 * Constructor.
	 * @param name the name of the bean. 
	 * @param cache the {@link PokerCache} instance to represent. 
	 */
	public PokerCacheConfigBean(String name, PokerCache cache) {
		this.name = name;
		this.cache = cache;
		if (this.cache != null) {
			this.poker = this.cache.isPoker();
			this.cache.addPokerCacheListener(new PokerCache.PokerCacheListener() {
				
				@Override
				public void pokerOn(PokerCache source) {
					throw new IllegalArgumentException("PokerOn is not supported");
				}
				
				@Override
				public void pokerOff(PokerCache source) {
					PokerCacheConfigBean.this.changeProperty(POKE_CACHE_PROP_INFO, Boolean.TRUE, Boolean.FALSE);
				}
			});
		}
			
		try {
			BeanConfigCategoryInfo beanInfo =
				BeanConfigCategoryInfo.createBeanConfigCategoryInfo(
					"org.ebayopensource.turmeric.services.authorizationservice.impl.util.cache." + name + ".Cache",
					null, // alias
					"org.ebayopensource.turmeric.server", 
					false, // persistent
					true, //ops managable
					null, // persistent file
					"Poker and Refresh Cache Bean for " + name, // description
					true // return an existing one
				);
			
			init(beanInfo, true);
			if (this.cache != null) {
				addPropertyChangeListener(new PropertyChangeListener() {
					@Override
					public void propertyChange(PropertyChangeEvent event) {
						if (POKER_PROPERTY_EXTERNAL_NAME.equals(event.getPropertyName())
								&& event.getNewValue() != null
								&& ((Boolean) event.getNewValue()).booleanValue()
								&& !event.getNewValue().equals(event.getOldValue())) {
							PokerCacheConfigBean.this.cache.poke();
						}
					}
				});
			}
		} catch (Exception e) {
			if (LOGGER.isLogEnabled(LogLevel.ERROR))
				LOGGER.log(LogLevel.ERROR, e);
		} catch (Throwable th) {
			if (LOGGER.isLogEnabled(LogLevel.ERROR))
				LOGGER.log(LogLevel.ERROR, th);
		}
		
	}
	
	/**
	 * Specifies if the poker is set or not. 
	 * @return <code>true</code> if the poker is set, <code>false</code> otherwise. 
	 */
	public Boolean isPoker() {
		return this.poker;
	}
	
	/**
	 * Getter for the name of the bean. 
	 * @return the name of the bean. 
	 */
	public String getName() {
		return this.name;
	}
	
}
