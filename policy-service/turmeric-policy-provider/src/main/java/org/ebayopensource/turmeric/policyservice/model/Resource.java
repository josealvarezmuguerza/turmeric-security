/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import org.ebayopensource.turmeric.utils.jpa.model.AuditablePersistent;

@Entity
public class Resource extends AuditablePersistent {

	private String resourceType;
	private String resourceName;
	private String description;

	@OneToMany( mappedBy = "resource",cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<Operation> operations = new HashSet<Operation>();

	protected Resource() {
	}

	public Resource(final String resourceType, final String resourceName,
			final String description) {
		this.resourceType = resourceType;
		this.resourceName = resourceName;
		this.description = description;
	}

	public void addOperations(final Set<Operation> operations) {
		this.operations.addAll(operations);
		for (Operation operation : operations) {
			if (operation.getResource() != this) {
				operation.setResource(this);
			}
		}

	}

	public Set<Operation> getOperations() {
		return operations;
	}

	public void setResourceType(final String resourceType) {
		this.resourceType = resourceType;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceName(final String resourceName) {
		this.resourceName = resourceName;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
