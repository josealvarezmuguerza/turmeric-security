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

import org.ebayopensource.turmeric.utils.jpa.model.AuditablePersistent;

/**
 * 
 * @author jose
 *
 */
@Entity
public class BasicAuth extends AuditablePersistent{
    private String subjectName;
    private String password;
    
    public BasicAuth() {}

    public BasicAuth(String subjectName, String password) {
        this.subjectName = subjectName;
        this.password = password;
    }
    
    public String getSubjectName() {
        return subjectName;
    }
    
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
