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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.ebayopensource.turmeric.utils.jpa.model.AuditablePersistent;

/**
 * @author gbaal
 * 
 */
@Entity
public class PrimitiveValue extends AuditablePersistent {
	public void setValue(String value) {
		this.value = value;
	}

	public void setType(SupportedPrimitive type) {
		this.type = type;
	}

	public PrimitiveValue() {}
	
	public PrimitiveValue(String value, SupportedPrimitive type) {
		super();
		this.value = value;
		this.type = type;
	}

	private String value;
	@Enumerated(EnumType.ORDINAL)
	private SupportedPrimitive type;

	public String getValue() {
		return value;
	}

	public SupportedPrimitive getType() {
		return type;
	}
}
