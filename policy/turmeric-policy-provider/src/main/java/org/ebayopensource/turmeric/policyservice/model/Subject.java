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
public class Subject extends AuditablePersistent {
    private String subjectName;
    private String subjectType;
    private String description;
    private String ipMask;
    private long externalSubjectId;
    private String emailContact;

    public Subject() {}
    
    public Subject(String subjectName,
                   String subjectType,
                   String description,
                   String ipMask,
                   long externalSubjectId,
                   String emailContact) {
        this.subjectName = subjectName;
        this.subjectType = subjectType;
        this.description = description;
        this.ipMask = ipMask;
        this.externalSubjectId = externalSubjectId;
        this.emailContact = emailContact;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpMask() {
        return ipMask;
    }

    public void setIpMask(String ipMask) {
        this.ipMask = ipMask;
    }

    public long getExternalSubjectId() {
        return externalSubjectId;
    }

    public void setExternalSubjectId(long externalSubjectId) {
        this.externalSubjectId = externalSubjectId;
    }

    public String getEmailContact() {
        return emailContact;
    }

    public void setEmailContact(String emailContact) {
        this.emailContact = emailContact;
    }
}
