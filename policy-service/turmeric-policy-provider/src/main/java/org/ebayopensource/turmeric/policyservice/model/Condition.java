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
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.ebayopensource.turmeric.utils.jpa.model.AuditablePersistent;

/**
 * @author gbaal
 * 
 */
@Entity(name="ConditionTbl")
public class Condition extends AuditablePersistent {
	
	public Condition() { }

	public Condition(Expression expression) {
		super();
		this.expression = expression;
	}

		
	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Expression expression;

	public Expression getExpression() {
		return expression;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

}
