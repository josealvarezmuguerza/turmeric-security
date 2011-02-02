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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import oasis.names.tc.xacml._2_0.policy.schema.os.SubjectMatchType;

import org.ebayopensource.turmeric.authentication.model.BasicAuth;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyFinderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.Category;
import org.ebayopensource.turmeric.policyservice.provider.USERSubject;
import org.ebayopensource.turmeric.policyservice.provider.utils.PolicyServiceUtils;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.utils.jpa.AbstractDAO;
import org.ebayopensource.turmeric.utils.jpa.model.AuditInfo;

public class SubjectDAOImpl extends AbstractDAO implements SubjectDAO {
	@Override
	public void persistSubject(Subject subject) {
		persistEntity(subject);
	}

	@Override
	public void persistSubjectGroup(SubjectGroup subjectGroup) {
		persistEntity(subjectGroup);
	}

	@Override
	public void removeSubject(long id) {
		removeEntity(Subject.class, id);
	}

	@Override
	public void removeSubjectGroup(long id) {
		removeEntity(SubjectGroup.class, id);
	}

	@Override
	public Subject findSubjectById(long id) {
		return findEntity(Subject.class, id);
	}

	@Override
	public Subject findSubjectByName(String name) {
		return getSingleResultOrNull(Subject.class, "subjectName", name);
	}

	@Override
	public List<Subject> findAllSubjectByName(String name, String subjectType) {
		return getWildcardResultList(Subject.class, "subjectType", subjectType,
				"subjectName", name);
	}

	@Override
	public List<Subject> findSubjectByType(String type) {
		return getResultList(Subject.class, "subjectType", type);
	}

	@Override
	public SubjectGroup findSubjectGroupById(Long id) {
		return findEntity(SubjectGroup.class, id);
	}

	@Override
	public SubjectGroup findSubjectGroupByName(String name) {
		return getSingleResultOrNull(SubjectGroup.class, "subjectGroupName",
				name);
	}

	@Override
	public List<SubjectGroup> findAllSubjectGroupByName(String name,
			String subjectType) {
		return getWildcardResultList(SubjectGroup.class, "subjectType",
				subjectType, "subjectGroupName", name);
	}

	@Override
	public List<SubjectGroup> findSubjectGroupByType(String type) {
		return getResultList(SubjectGroup.class, "subjectType", type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<SubjectGroup> findSubjectGroupBySubjectName(String name,
			String subjectType) {
		return findEntityByMemberValue(SubjectGroup.class, "subjectType",
				subjectType, "subjects", "subjectName", name);
	}

	@Override
	public SubjectType findSubjectTypeByName(String name) {
		return getSingleResultOrNull(SubjectType.class, "name", name);
	}

	@Override
	public List<AuditHistory> getSubjectHistory(long subjectId, Date start,
			Date end) {
		return getResultList(AuditHistory.class, "category",
				Category.SUBJECT.name(), "entityId", subjectId,
				"auditInfo.createdOn", start, end);
	}

	@Override
	public List<AuditHistory> getSubjectGroupHistory(long subjectId,
			Date start, Date end) {
		return getResultList(AuditHistory.class, "category",
				Category.SUBJECTGROUP.name(), "entityId", subjectId,
				"auditInfo.createdOn", start, end);
	}

	@Override
	public void audit(SubjectKey subjectKey, String operationType,
			SubjectKey loginSubject) {
		persistEntity(AuditHistory.newRecord(subjectKey, operationType,
				loginSubject));
	}

	@Override
	public void audit(SubjectGroupKey subjectGroupKey, String operationType,
			SubjectKey loginSubject) {
		persistEntity(AuditHistory.newRecord(subjectGroupKey, operationType,
				loginSubject));
	}

	public static org.ebayopensource.turmeric.security.v1.services.Subject convert(
			Subject jpaSubject) throws PolicyFinderException {
		org.ebayopensource.turmeric.security.v1.services.Subject result = new org.ebayopensource.turmeric.security.v1.services.Subject();

		result.setSubjectName(jpaSubject.getSubjectName());
		result.setSubjectType(jpaSubject.getSubjectType());
		result.setDescription(jpaSubject.getDescription());
		result.setIpMask(jpaSubject.getIpMask());
		result.setExternalSubjectId(jpaSubject.getExternalSubjectId());
		result.setEmailContact(jpaSubject.getEmailContact());

		AuditInfo auditInfo = jpaSubject.getAuditInfo();
		if (auditInfo != null) {
			result.setCreatedBy(auditInfo.getCreatedBy());

			try {
				GregorianCalendar updatedOn = new GregorianCalendar();
				Date updateDate = auditInfo.getUpdatedOn();
				updatedOn.setTime(updateDate == null ? auditInfo.getCreatedOn()
						: updateDate);
				result.setLastUpdatedDate(DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(updatedOn));
			} catch (DatatypeConfigurationException ex) {
				throw new PolicyFinderException(Category.SUBJECT,
						jpaSubject.getSubjectType(), null,
						jpaSubject.getSubjectName(),
						"Failed to convert subject", ex);
			}
		}

		return result;
	}

	public static Subject convert(
			org.ebayopensource.turmeric.security.v1.services.Subject subject) {
		Long extId = subject.getExternalSubjectId();
		return new Subject(subject.getSubjectName(), subject.getSubjectType(),
				subject.getDescription(), subject.getIpMask(),
				(extId == null ? 0 : extId.longValue()),
				subject.getEmailContact());
	}

	public static org.ebayopensource.turmeric.security.v1.services.SubjectGroup convert(
			SubjectGroup jpaSubjectGroup) throws PolicyFinderException {
		org.ebayopensource.turmeric.security.v1.services.SubjectGroup result = new org.ebayopensource.turmeric.security.v1.services.SubjectGroup();
		result.setSubjectGroupName(jpaSubjectGroup.getSubjectGroupName());
		result.setSubjectType(jpaSubjectGroup.getSubjectType());
		result.setSubjectGroupCalculator(jpaSubjectGroup
				.getSubjectGroupCalculator());
		result.setApplyToEach(jpaSubjectGroup.getApplyToEach());
		result.setApplyToAll(jpaSubjectGroup.getApplyToAll());
		result.setDescription(jpaSubjectGroup.getDescription());

		PolicyServiceUtils.setSubjectGroupId(result, jpaSubjectGroup.getId());

		AuditInfo auditInfo = jpaSubjectGroup.getAuditInfo();
		result.setCreatedBy(auditInfo.getCreatedBy());
		result.setLastModifiedBy(auditInfo.getUpdatedBy());

		try {
			GregorianCalendar updatedOn = new GregorianCalendar();
			Date updateDate = auditInfo.getUpdatedOn();
			updatedOn.setTime(updateDate == null ? auditInfo.getCreatedOn()
					: updateDate);
			result.setLastUpdatedDate(DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(updatedOn));
		} catch (DatatypeConfigurationException ex) {
			throw new PolicyFinderException(Category.SUBJECTGROUP,
					jpaSubjectGroup.getSubjectType(), null,
					jpaSubjectGroup.getSubjectGroupName(),
					"Failed to convert subject group", ex);
		}

		return result;
	}

	public static SubjectGroup convert(
			org.ebayopensource.turmeric.security.v1.services.SubjectGroup subjectGroup) {
		return new SubjectGroup(subjectGroup.getSubjectGroupName(),
				subjectGroup.getSubjectType(),
				subjectGroup.getSubjectGroupCalculator(),
				subjectGroup.isApplyToEach(), subjectGroup.isApplyToAll(),
				subjectGroup.getDescription());
	}

	//
	// public static
	// org.ebayopensource.turmeric.security.v1.services.GroupCalculatorInfo
	// convert(GroupCalculator jpaGroupCalculator) {
	// org.ebayopensource.turmeric.security.v1.services.GroupCalculatorInfo
	// result =
	// new
	// org.ebayopensource.turmeric.security.v1.services.GroupCalculatorInfo();
	// result.setName(jpaGroupCalculator.getName());
	// result.setSubjectTypeName(jpaGroupCalculator.getSubjectTypeName());
	// result.setDescription(jpaGroupCalculator.getDescription());
	//
	// return result;
	// }

	@Override
	public List<BasicAuth> findExternalSubjects() {
		return getWildcardResultList(BasicAuth.class, "subjectName", null);
	}

	/*
	 * Converts an external subject type into a internal Subject type
	 */
	public static org.ebayopensource.turmeric.security.v1.services.Subject convert(
			BasicAuth externalSubject) throws PolicyFinderException {
		org.ebayopensource.turmeric.security.v1.services.Subject subject = new org.ebayopensource.turmeric.security.v1.services.Subject();

		subject.setSubjectName(externalSubject.getSubjectName());
		subject.setExternalSubjectId(1L);
		subject.setExternalSubjectId(externalSubject.getId());

		AuditInfo auditInfo = externalSubject.getAuditInfo();
		if (auditInfo != null) {
			subject.setCreatedBy(auditInfo.getCreatedBy());

			try {
				GregorianCalendar updatedOn = new GregorianCalendar();
				Date updateDate = auditInfo.getUpdatedOn();
				updatedOn.setTime(updateDate == null ? auditInfo.getCreatedOn()
						: updateDate);
				subject.setLastUpdatedDate(DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(updatedOn));
			} catch (DatatypeConfigurationException ex) {
				throw new PolicyFinderException(Category.SUBJECT, "EXTERNAL",
						null, externalSubject.getSubjectName(),
						"Failed to convert subject", ex);
			}
		}
		return subject;

	}
}
