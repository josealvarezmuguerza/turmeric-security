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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.ebayopensource.turmeric.utils.jpa.model.AuditablePersistent;

@Entity
public class Operation extends AuditablePersistent {
 
	private String operationName;
    private String description;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="resource_id" )
    private Resource resource;


	protected Operation(){
	}
	
	public Operation( final String operationName, final String description) {
		this.operationName = operationName;
		this.description = description;
	}

    
	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(final String operationName) {
		this.operationName = operationName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setResource(final Resource resource) {
		this.resource = resource;
		if(!resource.getOperations().contains(this)){
			resource.getOperations().add(this);
		}
	}

	public Resource getResource() {
		return resource;
	}

	

}
