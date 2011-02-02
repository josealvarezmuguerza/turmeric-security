
/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 ******************************************************************************/
package org.ebayopensource.turmeric.policyservice.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.ebayopensource.turmeric.utils.jpa.model.AuditablePersistent;

/**
 * @author gbaal
 * 
 */
@Entity
public class Rule extends AuditablePersistent {

	
	public Rule() { }
	
	public Rule(String ruleName, String description, Long effectDuration,
			Long rolloverPeriod, Integer priority, EffectType effect,
			Condition condition,String notifyEmails, boolean notifyActive ) {
		super();
		this.ruleName = ruleName;
		this.description = description;
		this.effectDuration = effectDuration;
		this.rolloverPeriod = rolloverPeriod;
		this.priority = priority;
		this.effect = effect;
		this.condition = condition;
		this.notifyEmails = notifyEmails;
		this.notifyActive = notifyActive;
	}
	private String notifyEmails;
	private boolean notifyActive;
	
	private String ruleName;
	private String description;
	private Long effectDuration;
	private Long rolloverPeriod;
	private Integer priority;
	@Enumerated(EnumType.ORDINAL)
	private EffectType effect;

//	@ManyToOne(fetch=FetchType.EAGER)
//    @JoinColumn(name="policy_id" )
//    private Policy policy;
	
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Condition condition;

	public String getRuleName() {
		return ruleName;
	}

	public String getDescription() {
		return description;
	}

	public Long getEffectDuration() {
		return effectDuration;
	}

	public Long getRolloverPeriod() {
		return rolloverPeriod;
	}

	public Integer getPriority() {
		return priority;
	}

	public Condition getCondition() {
		return condition;
	}
	public EffectType getEffect() {
		return effect;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEffectDuration(Long effectDuration) {
		this.effectDuration = effectDuration;
	}

	public void setRolloverPeriod(Long rolloverPeriod) {
		this.rolloverPeriod = rolloverPeriod;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public void setEffect(EffectType effect) {
		this.effect = effect;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}
	public String getNotifyEmails() {
		return notifyEmails;
	}

	public void setNotifyEmails(String notifyEmails) {
		this.notifyEmails = notifyEmails;
	}

	public boolean isNotifyActive() {
		return notifyActive;
	}

	public void setNotifyActive(boolean notifyActive) {
		this.notifyActive = notifyActive;
	}
}
