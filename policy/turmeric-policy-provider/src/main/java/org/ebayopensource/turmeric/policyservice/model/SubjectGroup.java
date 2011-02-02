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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.ebayopensource.turmeric.utils.jpa.model.AuditablePersistent;

@Entity
public class SubjectGroup extends AuditablePersistent {
    @ManyToMany
    private List<Subject> subjects;
    private String subjectGroupName;
    private String subjectType;
    private String subjectGroupCalculator;
    private boolean applyToEach;
    private boolean applyToAll;
    private String description;

    protected SubjectGroup() { }

    public SubjectGroup(String subjectGroupName,
                        String subjectType,
                        String subjectGroupCalculator,
                        boolean applyToEach,
                        boolean applyToAll,
                        String description) {
        this.subjectGroupName = subjectGroupName;
        this.subjectType = subjectType;
        this.subjectGroupCalculator = subjectGroupCalculator;
        this.applyToEach = applyToEach;
        this.applyToAll = applyToAll;
        this.description = description;
    }

    public List<Subject> getSubjects() {
        if (subjects == null) {
            subjects = new ArrayList<Subject>();
        }
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    public String getSubjectGroupName() {
        return subjectGroupName;
    }

    public void setSubjectGroupName(String subjectGroupName) {
        this.subjectGroupName = subjectGroupName;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectGroupCalculator() {
        return subjectGroupCalculator;
    }

    public void setSubjectGroupCalculator(String subjectGroupCalculator) {
        this.subjectGroupCalculator = subjectGroupCalculator;
    }

    public boolean getApplyToEach() {
        return applyToEach;
    }

    public void setApplyToEach(boolean applyToEach) {
        this.applyToEach = applyToEach;
    }

    public boolean getApplyToAll() {
        return applyToAll;
    }

    public void setApplyToAll(boolean applyToAll) {
        this.applyToAll = applyToAll;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
