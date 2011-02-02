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

import org.ebayopensource.turmeric.utils.jpa.model.AuditablePersistent;

@Entity
public class SubjectType extends AuditablePersistent {
    private String name;
    private String description;
    private boolean external;
    
    public SubjectType() {}
    
    public SubjectType(String name, String description, boolean external)
    {
        this.name = name;
        this.description = description;
        this.external = external;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isExternal() {
        return external;
    }
    public void setExternal(boolean external) {
        this.external = external;
    }
}
