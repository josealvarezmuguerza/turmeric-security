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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.ebayopensource.turmeric.utils.jpa.model.AuditablePersistent;

@Entity
public class Policy extends AuditablePersistent{
    private String policyName;
    private String policyType;
    private String description;
    private boolean active;
    
    @ManyToMany
    private List<Resource> resources;

    @ManyToMany
    private List<Operation> operations;
    
    @ManyToMany
    private List<Subject> subjects;

    @ManyToMany
    private List<SubjectGroup> subjectGroups;

    @ManyToMany
    @JoinTable(name="Policy_ExclusionSubjects")
    private List<Subject> exclusionSubjects;

    @ManyToMany
    @JoinTable(name="Policy_ExclusionSubjectGroups")
    private List<SubjectGroup> exclusionSubjectGroups;
    
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Rule> rules = new ArrayList<Rule>();

	public Policy() {}
    
    public Policy(String policyName, String policyType, String description) {
        this.policyName = policyName;
        this.policyType = policyType;
        this.description = description;
    }
    
    public String getPolicyName() {
        return policyName;
    }
    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }
    public String getPolicyType() {
        return policyType;
    }
    public void setPolicyType(String policyType) {
        this.policyType = policyType;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Resource> getResources() {
        if (resources == null) {
            resources = new ArrayList<Resource>();
        }
        return resources;
    }
    
    public List<Operation> getOperations() {
        if (operations == null) {
            operations = new ArrayList<Operation>();
        }
        return operations;
    }
    
    public List<Subject> getSubjects() {
        if (subjects == null) {
            subjects = new ArrayList<Subject>();
        }
        return subjects;
    }
    
    public List<SubjectGroup> getSubjectGroups() {
        if (subjectGroups == null) {
            subjectGroups = new ArrayList<SubjectGroup>();
        }
        return subjectGroups;
    }

    public List<Subject> getExclusionSubjects() {
        if (exclusionSubjects == null) {
            exclusionSubjects = new ArrayList<Subject>();
        }
        return exclusionSubjects;
    }

    public List<SubjectGroup> getExclusionSubjectGroups() {
        if (exclusionSubjectGroups == null) {
            exclusionSubjectGroups = new ArrayList<SubjectGroup>();
        }
        return exclusionSubjectGroups;
    }
    
    public List<Rule> getRules() {
        if (rules == null) {
            rules = new ArrayList<Rule>();
        }
        return rules;
    }
}
