/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.provider;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ebayopensource.turmeric.authentication.model.BasicAuth;
import org.ebayopensource.turmeric.groupmembershipservice.provider.CalculatedGroupMembershipProviderImpl;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyCreationException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyDeleteException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyFinderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.Category;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyUpdateException;
import org.ebayopensource.turmeric.policyservice.model.AuditHistory;
import org.ebayopensource.turmeric.policyservice.model.SubjectDAO;
import org.ebayopensource.turmeric.policyservice.model.SubjectDAOImpl;
import org.ebayopensource.turmeric.policyservice.provider.common.SubjectGroupEditObject;
import org.ebayopensource.turmeric.policyservice.provider.utils.PolicyServiceUtils;
import org.ebayopensource.turmeric.security.v1.services.EntityHistory;
import org.ebayopensource.turmeric.security.v1.services.GroupCalculatorInfo;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePair;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectTypeInfo;
import org.ebayopensource.turmeric.utils.jpa.JPAAroundAdvice;
import org.ebayopensource.turmeric.utils.jpa.PersistenceContext;
import org.ebayopensource.turmeric.utils.jpa.model.AuditContext;

public abstract class SubjectBase implements SubjectTypeProvider {
	private final EntityManagerFactory factory;
	private final SubjectTypeProvider impl;

	protected abstract String getSubjectType();

	public SubjectBase() {
		factory = PersistenceContext
				.createEntityManagerFactory("policyservice");

		ClassLoader classLoader = SubjectTypeProvider.class.getClassLoader();
		Class[] interfaces = { SubjectTypeProvider.class };
		SubjectTypeProvider target = new SubjectBaseImpl(getSubjectType());
		impl = (SubjectTypeProvider) Proxy.newProxyInstance(classLoader,
				interfaces, new JPAAroundAdvice(factory, target));
	}

	private class SubjectBaseImpl implements SubjectTypeProvider {
		private final SubjectDAO subjectDAO;
//		private final BasicAuthDAO basicAuthDAO;
		private final String subjectType;

		private final CalculatedGroupMembershipProviderImpl calcProvider = CalculatedGroupMembershipProviderImpl
				.getInstance();

		public SubjectBaseImpl(String subjectType) {
			this.subjectType = subjectType;
			this.subjectDAO = new SubjectDAOImpl();
//			this.basicAuthDAO = new BasicAuthDAOImpl();
		}

		@Override
		public Map<Long, Subject> getSubjectById(Long id)
				throws PolicyFinderException {
			Map<Long, Subject> result = new HashMap<Long, Subject>();
			org.ebayopensource.turmeric.policyservice.model.Subject jpaSubject = subjectDAO
					.findSubjectById(id);
			if (jpaSubject != null) {
				result.put(jpaSubject.getId(),
						SubjectDAOImpl.convert(jpaSubject));
			}

			return result;
		}

		@Override
		public Map<Long, Subject> getSubjectByName(String name)
				throws PolicyFinderException {
			Map<Long, Subject> result = new HashMap<Long, Subject>();
			List<org.ebayopensource.turmeric.policyservice.model.Subject> jpaSubjects = subjectDAO
					.findAllSubjectByName(name, subjectType);
			if (jpaSubjects != null) {
				for (org.ebayopensource.turmeric.policyservice.model.Subject jpaSubject : jpaSubjects) {
					result.put(jpaSubject.getId(),
							SubjectDAOImpl.convert(jpaSubject));
				}
			}

			return result;
		}

		@Override
		public Map<Long, Subject> getSubjectByType()
				throws PolicyFinderException {
			Map<Long, Subject> result = new HashMap<Long, Subject>();
			List<org.ebayopensource.turmeric.policyservice.model.Subject> jpaSubjectList = subjectDAO
					.findSubjectByType(subjectType);
			if (jpaSubjectList != null && !jpaSubjectList.isEmpty()) {
				for (org.ebayopensource.turmeric.policyservice.model.Subject jpaSubject : jpaSubjectList) {
					result.put(jpaSubject.getId(),
							SubjectDAOImpl.convert(jpaSubject));
				}

			}
			return result;
		}

		@Override
		public SubjectKey createSubject(Subject subject, SubjectKey createdBy)
				throws PolicyCreationException {
			if (createdBy != null) {
				AuditContext.setUser(createdBy.getSubjectName());
			}
			try {
				org.ebayopensource.turmeric.policyservice.model.Subject jpaSubject = SubjectDAOImpl
						.convert(subject);

				subjectDAO.persistSubject(jpaSubject);
				SubjectKey subjectKey = new SubjectKey();
				subjectKey.setSubjectId(jpaSubject.getId());
				subjectKey.setSubjectName(subject.getSubjectName());
				subjectKey.setSubjectType(subject.getSubjectType());
				return subjectKey;
			} catch (Exception ex) {
				throw new PolicyCreationException(Category.SUBJECT,
						subjectType, null, subject.getSubjectName(),
						"Failed to create subject", ex);
			} finally {
				AuditContext.clear();
			}
		}

		@Override
		public void deleteSubject(Long subjectId) throws PolicyDeleteException {
			subjectDAO.removeSubject(subjectId);
		}

		@Override
		public Map<Long, SubjectGroup> findSubjectGroupInfoBySubject(
				Long subjectId) throws PolicyFinderException {
			Map<Long, SubjectGroup> result = new HashMap<Long, SubjectGroup>();
			org.ebayopensource.turmeric.policyservice.model.Subject subject = subjectDAO
					.findSubjectById(subjectId);
			List<org.ebayopensource.turmeric.policyservice.model.SubjectGroup> jpaSubjectGroupList = subjectDAO
					.findSubjectGroupBySubjectName(subject.getSubjectName(),
							subjectType);
			if (jpaSubjectGroupList != null && !jpaSubjectGroupList.isEmpty()) {
				for (org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup : jpaSubjectGroupList) {
					result.put(jpaSubjectGroup.getId(),
							SubjectDAOImpl.convert(jpaSubjectGroup));
				}
			}

			return result;
		}

		@Override
		public Map<Long, SubjectGroup> getSubjectGroupInfoById(Long id)
				throws PolicyFinderException {
			Map<Long, SubjectGroup> result = new HashMap<Long, SubjectGroup>();
			org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup = subjectDAO
					.findSubjectGroupById(id);
			if (jpaSubjectGroup != null) {
				result.put(jpaSubjectGroup.getId(),
						SubjectDAOImpl.convert(jpaSubjectGroup));
			}

			return result;
		}

		@Override
		public Map<Long, SubjectGroup> getSubjectGroupInfoByName(String name)
				throws PolicyFinderException {
			Map<Long, SubjectGroup> result = new HashMap<Long, SubjectGroup>();
			List<org.ebayopensource.turmeric.policyservice.model.SubjectGroup> jpaSubjectGroups = subjectDAO
					.findAllSubjectGroupByName(name, subjectType);
			if (jpaSubjectGroups != null) {
				for (org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup : jpaSubjectGroups) {
					result.put(jpaSubjectGroup.getId(),
							SubjectDAOImpl.convert(jpaSubjectGroup));
				}
			}

			return result;
		}

		@Override
		public Map<Long, SubjectGroup> getSubjectGroupInfoByType()
				throws PolicyFinderException {
			Map<Long, SubjectGroup> result = new HashMap<Long, SubjectGroup>();
			List<org.ebayopensource.turmeric.policyservice.model.SubjectGroup> jpaSubjectGroupList = subjectDAO
					.findSubjectGroupByType(subjectType);
			if (jpaSubjectGroupList != null && !jpaSubjectGroupList.isEmpty()) {
				for (org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup : jpaSubjectGroupList) {
					result.put(jpaSubjectGroup.getId(),
							SubjectDAOImpl.convert(jpaSubjectGroup));
				}
			}

			return result;
		}

		@Override
		public boolean isExternalSubjectType() throws PolicyProviderException {
			return false;
		}

		@Override
		public Set<Subject> getExternalSubjectByName(String name)
				throws PolicyFinderException {
			Set<Subject> result = new HashSet<Subject>();

			List<BasicAuth> externalSubjects = subjectDAO.findExternalSubjects();
			
			if (externalSubjects != null) {
				for (BasicAuth basicAuth : externalSubjects) {
					result.add(SubjectDAOImpl.convert(basicAuth));
				}
			}

			return result;
			
		}

		@Override
		public Subject getExternalSubjectById(Long id)
				throws PolicyFinderException {
			return null;
		}

		@Override
		public Map<Long, Subject> getSubjectAssignmentOfSubjectGroup(
				Long subjectGroupId) throws PolicyFinderException {
			Map<Long, Subject> result = new HashMap<Long, Subject>();
			org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup = subjectDAO
					.findSubjectGroupById(subjectGroupId);
			List<org.ebayopensource.turmeric.policyservice.model.Subject> jpaSubjectList = jpaSubjectGroup
					.getSubjects();
			if (jpaSubjectList != null && !jpaSubjectList.isEmpty()) {
				for (org.ebayopensource.turmeric.policyservice.model.Subject jpaSubject : jpaSubjectList) {
					result.put(jpaSubject.getId(),
							SubjectDAOImpl.convert(jpaSubject));
				}
			}

			return result;
		}

		@Override
		public SubjectGroupKey createSubjectGroup(SubjectGroup subjectGroup,
				SubjectGroupEditObject subjectGroupEditObject,
				SubjectKey createdBy) throws PolicyCreationException,
				PolicyUpdateException {
			if (createdBy != null) {
				AuditContext.setUser(createdBy.getSubjectName());
			}
			try {
				org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup = SubjectDAOImpl
						.convert(subjectGroup);

				List<org.ebayopensource.turmeric.policyservice.model.Subject> jpaSubjectList = jpaSubjectGroup
						.getSubjects();

				for (Long addId : subjectGroupEditObject.getAddSubjectList()) {
					org.ebayopensource.turmeric.policyservice.model.Subject subject = subjectDAO
							.findSubjectById(addId);
					if (subject != null) {
						jpaSubjectList.add(subject);
					}
				}

				subjectDAO.persistSubjectGroup(jpaSubjectGroup);

				SubjectGroupKey subjectGroupKey = new SubjectGroupKey();
				subjectGroupKey.setSubjectGroupId(jpaSubjectGroup.getId());
				subjectGroupKey.setSubjectGroupName(jpaSubjectGroup
						.getSubjectGroupName());
				subjectGroupKey
						.setSubjectType(jpaSubjectGroup.getSubjectType());
				return subjectGroupKey;
			} catch (Exception ex) {
				throw new PolicyCreationException(Category.SUBJECTGROUP,
						subjectType, null, subjectGroup.getSubjectGroupName(),
						"Failed to create subject group", ex);
			} finally {
				AuditContext.clear();
			}
		}

		@Override
		public SubjectGroupKey updateSubjectGroup(SubjectGroup subjectGroup,
				SubjectGroupEditObject subjectGroupEditObject,
				SubjectKey createdBy) throws PolicyUpdateException {
			if (createdBy != null) {
				AuditContext.setUser(createdBy.getSubjectName());
			}
			try {
				Long id = PolicyServiceUtils.getSubjectGroupId(subjectGroup);
				org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup = null;
				if (id == null) {
					jpaSubjectGroup = subjectDAO
							.findSubjectGroupByName(subjectGroup
									.getSubjectGroupName());
				} else {
					jpaSubjectGroup = subjectDAO.findSubjectGroupById(id);
				}
				if (subjectGroup.getSubjectType() != null) {
					jpaSubjectGroup.setSubjectType(subjectGroup
							.getSubjectType());
				}
				if (subjectGroup.getSubjectGroupName() != null) {
					jpaSubjectGroup.setSubjectGroupName(subjectGroup
							.getSubjectGroupName());
				}
				if (subjectGroup.getDescription() != null) {
					jpaSubjectGroup.setDescription(subjectGroup
							.getDescription());
				}
				if (subjectGroup.getSubjectGroupCalculator() != null) {
					jpaSubjectGroup.setSubjectGroupCalculator(subjectGroup
							.getSubjectGroupCalculator());
				}
				jpaSubjectGroup.setApplyToEach(subjectGroup.isApplyToEach());
				jpaSubjectGroup.setApplyToAll(subjectGroup.isApplyToAll());

				List<org.ebayopensource.turmeric.policyservice.model.Subject> jpaSubjectList = jpaSubjectGroup
						.getSubjects();

				for (Long removeId : subjectGroupEditObject
						.getRemoveSubjectList()) {
					org.ebayopensource.turmeric.policyservice.model.Subject subject = subjectDAO
							.findSubjectById(removeId);
					if (subject != null) {
						jpaSubjectList.remove(subject);
					}
				}

				for (Long addId : subjectGroupEditObject.getAddSubjectList()) {
					org.ebayopensource.turmeric.policyservice.model.Subject subject = subjectDAO
							.findSubjectById(addId);
					if (subject != null) {
						jpaSubjectList.add(subject);
					}
				}

				SubjectGroupKey subjectGroupKey = new SubjectGroupKey();
				subjectGroupKey.setSubjectGroupId(jpaSubjectGroup.getId());
				subjectGroupKey.setSubjectGroupName(jpaSubjectGroup
						.getSubjectGroupName());
				subjectGroupKey
						.setSubjectType(jpaSubjectGroup.getSubjectType());
				return subjectGroupKey;
			} catch (Exception ex) {
				throw new PolicyUpdateException(Category.SUBJECTGROUP,
						subjectType, null, subjectGroup.getSubjectGroupName(),
						"Failed to update subject group", ex);
			} finally {
				AuditContext.clear();
			}

		}

		@Override
		public void deleteSubjectGroup(Long subjectGroupId)
				throws PolicyDeleteException, PolicyUpdateException,
				PolicyFinderException {
			subjectDAO.removeSubjectGroup(subjectGroupId);
		}

		@Override
		public GroupCalculatorInfo getGroupCalculator(String calculator)
				throws PolicyProviderException {
			GroupCalculatorInfo calculatorInfo = null;
			try {
				calcProvider.initialize();
			} catch (org.ebayopensource.turmeric.utils.config.exceptions.PolicyProviderException e) {
				throw new PolicyFinderException(Category.SUBJECTGROUP,
						getSubjectType(), "calculator initialization error ", e);
			}
			SubjectGroupType subjectGroupType = new SubjectGroupType();
			subjectGroupType.setCalculator(calculator);
			subjectGroupType.setDomain(getSubjectType());
			SubjectGroupType calculatedSG = calcProvider
					.getCalculatedSG(subjectGroupType);
			if (calculatedSG != null
					&& calculatedSG.getDomain().equalsIgnoreCase(
							getSubjectType())) {
				calculatorInfo = new GroupCalculatorInfo();
				calculatorInfo.setName(calculatedSG.getName());
				calculatorInfo.setSubjectTypeName(calculatedSG.getDomain());
			}
			return calculatorInfo;
		}

		@Override
		public List<GroupCalculatorInfo> getGroupCalculators()
				throws PolicyFinderException {
			List<GroupCalculatorInfo> calcGroupInfos = new ArrayList<GroupCalculatorInfo>();
			try {
				calcProvider.initialize();
			} catch (org.ebayopensource.turmeric.utils.config.exceptions.PolicyProviderException e) {
				throw new PolicyFinderException(Category.SUBJECTGROUP,
						getSubjectType(), "calculator initialization error ", e);
			}
			Collection<SubjectGroupType> allCalculatedSGs = calcProvider
					.getAllCalculatedSGs();
			if (allCalculatedSGs != null && allCalculatedSGs.isEmpty() == false) {
				for (SubjectGroupType subjectGroupType : allCalculatedSGs) {
					if (subjectGroupType.getDomain().equalsIgnoreCase(
							getSubjectType())) {
						GroupCalculatorInfo gcInfo = new GroupCalculatorInfo();
						gcInfo.setName(subjectGroupType.getName());
						gcInfo.setSubjectTypeName(subjectGroupType.getDomain());
						calcGroupInfos.add(gcInfo);
					}
				}
			}
			return calcGroupInfos;
		}

		@Override
		public SubjectTypeInfo getSubjectTypeInfo()
				throws PolicyProviderException {
			org.ebayopensource.turmeric.policyservice.model.SubjectType jpaSubjectType = subjectDAO
					.findSubjectTypeByName(subjectType);
			SubjectTypeInfo ret = new SubjectTypeInfo();
			ret.setExternal(isExternalSubjectType());
			if (jpaSubjectType != null) {
				ret.setId(jpaSubjectType.getId());
			}
			ret.setName(subjectType);
			return ret;
		}

		@Override
		public List<EntityHistory> getAuditHistory(
				SubjectGroupKey subjectGroupKey,
				XMLGregorianCalendar startDate, XMLGregorianCalendar endDate)
				throws PolicyFinderException {
			List<EntityHistory> entityHistory = new ArrayList<EntityHistory>();

			Long subjectGroupId = subjectGroupKey.getSubjectGroupId();
			if (subjectGroupId == null) {
				org.ebayopensource.turmeric.policyservice.model.SubjectGroup subjectGroup = subjectDAO
						.findSubjectGroupByName(subjectGroupKey
								.getSubjectGroupName());
				if (subjectGroup != null) {
					subjectGroupId = subjectGroup.getId();
				}
			}

			if (subjectGroupId != null) {
				List<AuditHistory> auditHistory = subjectDAO
						.getSubjectGroupHistory(subjectGroupId.longValue(),
								startDate.toGregorianCalendar().getTime(),
								endDate.toGregorianCalendar().getTime());

				for (AuditHistory entry : auditHistory) {
					entityHistory.add(AuditHistory.convert(entry));
				}
			}

			return entityHistory;
		}

		@Override
		public List<EntityHistory> getAuditHistory(SubjectKey subjectKey,
				XMLGregorianCalendar startDate, XMLGregorianCalendar endDate)
				throws PolicyFinderException {
			List<EntityHistory> entityHistory = new ArrayList<EntityHistory>();

			Long subjectId = subjectKey.getSubjectId();
			if (subjectId == null) {
				org.ebayopensource.turmeric.policyservice.model.Subject subject = subjectDAO
						.findSubjectByName(subjectKey.getSubjectName());
				if (subject != null) {
					subjectId = subject.getId();
				}
			}

			if (subjectId != null) {
				List<AuditHistory> auditHistory = subjectDAO.getSubjectHistory(
						subjectId.longValue(), startDate.toGregorianCalendar()
								.getTime(), endDate.toGregorianCalendar()
								.getTime());

				for (AuditHistory entry : auditHistory) {
					entityHistory.add(AuditHistory.convert(entry));
				}
			}

			return entityHistory;
		}

		@Override
		public void audit(SubjectKey subjectKey, String operationType,
				SubjectKey loginSubject) throws PolicyFinderException {
			subjectDAO.audit(subjectKey, operationType, loginSubject);
		}

		@Override
		public void audit(SubjectGroupKey subjectGroupKey,
				String operationType, SubjectKey loginSubject)
				throws PolicyFinderException {
			subjectDAO.audit(subjectGroupKey, operationType, loginSubject);
		}

		@Override
		public List<KeyValuePair> getMetaData(String queryValue)
				throws PolicyFinderException {
			if (queryValue.equals("Type")) {
				List<KeyValuePair> ret = new ArrayList<KeyValuePair>();
				KeyValuePair valuePair = new KeyValuePair();
				valuePair.setKey(subjectType);
				try {
					valuePair.setValue(isExternalSubjectType() ? "1" : "0");
				} catch (PolicyProviderException ex) {
					throw new PolicyFinderException(Category.SUBJECT,
							subjectType, ex);
				}
				ret.add(valuePair);
				return ret;
			}

			if (queryValue.equals("SubjectGroupCalculator")) {
				List<GroupCalculatorInfo> calcList = getGroupCalculators();
				if (calcList == null || calcList.size() == 0)
					return null;

				List<KeyValuePair> ret = new ArrayList<KeyValuePair>();
				for (GroupCalculatorInfo info : calcList) {
					KeyValuePair valuePair = new KeyValuePair();
					valuePair.setKey(info.getName());
					valuePair.setValue(info.getSubjectTypeName());
					ret.add(valuePair);
				}

				return ret;
			}

			return null;
		}
	}

	// The following methods are needed to allow wrapping this class with JPA
	// transaction wrapper

	@Override
	public Map<Long, Subject> getSubjectById(Long id)
			throws PolicyFinderException {
		return impl.getSubjectById(id);
	}

	@Override
	public Map<Long, Subject> getSubjectByName(String name)
			throws PolicyFinderException {
		return impl.getSubjectByName(name);
	}

	@Override
	public Map<Long, Subject> getSubjectByType() throws PolicyFinderException {
		return impl.getSubjectByType();
	}

	@Override
	public SubjectKey createSubject(Subject subject, SubjectKey createdBy)
			throws PolicyCreationException {
		return impl.createSubject(subject, createdBy);
	}

	@Override
	public void deleteSubject(Long subjectId) throws PolicyDeleteException {
		impl.deleteSubject(subjectId);
	}

	@Override
	public Map<Long, SubjectGroup> findSubjectGroupInfoBySubject(Long subjectId)
			throws PolicyFinderException {
		return impl.findSubjectGroupInfoBySubject(subjectId);
	}

	@Override
	public Map<Long, SubjectGroup> getSubjectGroupInfoById(Long id)
			throws PolicyFinderException {
		return impl.getSubjectGroupInfoById(id);
	}

	@Override
	public Map<Long, SubjectGroup> getSubjectGroupInfoByName(String name)
			throws PolicyFinderException {
		return impl.getSubjectGroupInfoByName(name);
	}

	@Override
	public Map<Long, SubjectGroup> getSubjectGroupInfoByType()
			throws PolicyFinderException {
		return impl.getSubjectGroupInfoByType();
	}

	@Override
	public boolean isExternalSubjectType() throws PolicyProviderException {
		return false;
	}

	@Override
	public Set<Subject> getExternalSubjectByName(String name)
			throws PolicyFinderException {
		return impl.getExternalSubjectByName(name);
	}

	@Override
	public Subject getExternalSubjectById(Long id) throws PolicyFinderException {
		return null;
	}

	@Override
	public Map<Long, Subject> getSubjectAssignmentOfSubjectGroup(
			Long subjectGroupId) throws PolicyFinderException {
		return impl.getSubjectAssignmentOfSubjectGroup(subjectGroupId);
	}

	@Override
	public SubjectGroupKey createSubjectGroup(SubjectGroup subjectGroup,
			SubjectGroupEditObject subjectGroupEditObject, SubjectKey createdBy)
			throws PolicyCreationException, PolicyUpdateException {
		return impl.createSubjectGroup(subjectGroup, subjectGroupEditObject,
				createdBy);
	}

	@Override
	public SubjectGroupKey updateSubjectGroup(SubjectGroup subjectGroup,
			SubjectGroupEditObject subjectGroupEditObject, SubjectKey createdBy)
			throws PolicyUpdateException {
		return impl.updateSubjectGroup(subjectGroup, subjectGroupEditObject,
				createdBy);
	}

	@Override
	public void deleteSubjectGroup(Long subjectGroupId)
			throws PolicyDeleteException, PolicyUpdateException,
			PolicyFinderException {
		impl.deleteSubjectGroup(subjectGroupId);
	}

	@Override
	public GroupCalculatorInfo getGroupCalculator(String calculator)
			throws PolicyProviderException {
		return impl.getGroupCalculator(calculator);
	}

	@Override
	public SubjectTypeInfo getSubjectTypeInfo() throws PolicyProviderException {
		return impl.getSubjectTypeInfo();
	}

	@Override
	public List<EntityHistory> getAuditHistory(SubjectGroupKey subjectGroupKey,
			XMLGregorianCalendar startDate, XMLGregorianCalendar endDate)
			throws PolicyFinderException {
		return impl.getAuditHistory(subjectGroupKey, startDate, endDate);
	}

	@Override
	public List<EntityHistory> getAuditHistory(SubjectKey subjectKey,
			XMLGregorianCalendar startDate, XMLGregorianCalendar endDate)
			throws PolicyFinderException {
		return impl.getAuditHistory(subjectKey, startDate, endDate);
	}

	@Override
	public void audit(SubjectKey subjectKey, String operationType,
			SubjectKey loginSubject) throws PolicyFinderException {
		impl.audit(subjectKey, operationType, loginSubject);
	}

	@Override
	public void audit(SubjectGroupKey subjectGroupKey, String operationType,
			SubjectKey loginSubject) throws PolicyFinderException {
		impl.audit(subjectGroupKey, operationType, loginSubject);
	}

	@Override
	public List<GroupCalculatorInfo> getGroupCalculators()
			throws PolicyFinderException {
		return impl.getGroupCalculators();
	}

	@Override
	public List<KeyValuePair> getMetaData(String queryValue)
			throws PolicyFinderException {
		return impl.getMetaData(queryValue);
	}

}
