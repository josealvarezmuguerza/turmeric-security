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

import java.util.Date;
import java.util.List;

import org.ebayopensource.turmeric.authentication.model.BasicAuth;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;

public interface SubjectDAO {
    void persistSubject(Subject subject);

    void persistSubjectGroup(SubjectGroup subjectGroup);

    void removeSubject(long id);

    void removeSubjectGroup(long id);

    Subject findSubjectById(long id);

    Subject findSubjectByName(String name);

	List<BasicAuth> findExternalSubjects();
    
    List<Subject> findSubjectByType(String type);

    SubjectGroup findSubjectGroupById(Long id);

    SubjectGroup findSubjectGroupByName(String name);

    List<SubjectGroup> findSubjectGroupByType(String type);

    List<SubjectGroup> findSubjectGroupBySubjectName(String name, String subjectType);

    SubjectType findSubjectTypeByName(String subjectType);

    List<Subject> findAllSubjectByName(String name, String subjectType);

    List<SubjectGroup> findAllSubjectGroupByName(String name, String subjectType);

    List<AuditHistory> getSubjectHistory(long subjectId, Date begin, Date end);

    List<AuditHistory> getSubjectGroupHistory(long subjectGroupId, Date begin, Date end);

    void audit(SubjectKey subjectKey, String operationType,
			SubjectKey loginSubject);

	void audit(SubjectGroupKey subjectGroupKey, String operationType,
			SubjectKey loginSubject);

}
