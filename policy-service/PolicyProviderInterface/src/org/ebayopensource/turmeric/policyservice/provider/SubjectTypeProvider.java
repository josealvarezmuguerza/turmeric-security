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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.ebayopensource.turmeric.policyservice.exceptions.PolicyCreationException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyDeleteException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyFinderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyUpdateException;
import org.ebayopensource.turmeric.policyservice.provider.common.SubjectGroupEditObject;
import org.ebayopensource.turmeric.security.v1.services.EntityHistory;
import org.ebayopensource.turmeric.security.v1.services.GroupCalculatorInfo;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePair;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectTypeInfo;

/**
 * The interface for subject type provider. This interface need to be
 * implemented in order to provide detailed logic for each different subject
 * types.
 * 
 */
public interface SubjectTypeProvider {

	/**
	 * To retrieve subject based on its primary key.
	 * 
	 * @param id
	 *            The primary key of the subject to be retrieved.
	 * @return A Map containing single entry will be returned. Since the subject
	 *         object does not contain the primary key, the key will be held and
	 *         returned in the key of the map. The value will contain the actual
	 *         subject object.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding subject failed.
	 */
	Map<Long, Subject> getSubjectById(Long id) throws PolicyFinderException;

	/**
	 * To retrieve subject based on its unique name.
	 * 
	 * @param name
	 *            The unique name of the subject to be retrieved.
	 * @return A Map containing single entry will be returned. Since the subject
	 *         object does not contain the primary key, the key will be held and
	 *         returned in the key of the map. The value will contain the actual
	 *         subject object.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding subject failed.
	 */
	Map<Long, Subject> getSubjectByName(String name)
			throws PolicyFinderException;

	/**
	 * To retrieve subjects of this provider type.
	 * 
	 * @return A Map will be returned. The primary keys of the subjects will be
	 *         kept in the keys of the map and the values of the map will
	 *         contain the actual subject objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding subject type failed.
	 */
	Map<Long, Subject> getSubjectByType() throws PolicyFinderException;

	/**
	 * To create a persistent record for the given subject.
	 * 
	 * @param subject
	 *            The detail information of the subject to be created.
	 * @param createdBy
	 *            The subject who calls this method
	 * @return The subject key of the created subject will be returned.
	 * @throws PolicyCreationException
	 *             Throws this exception when creating subject failed.
	 */
	SubjectKey createSubject(Subject subject, SubjectKey createdBy)
			throws PolicyCreationException;

	/**
	 * To permanently delete the subject from the persistent storage.
	 * 
	 * @param subjectId
	 *            The primary key of the subject to be deleted.
	 * @throws PolicyDeleteException
	 *             Throws this exception when deleting subject failed.
	 */
	void deleteSubject(Long subjectId) throws PolicyDeleteException;

	/**
	 * To retrieve subject group which the given subject belongs to.
	 * 
	 * @param subjectId
	 *            The primary key of the subject.
	 * @return A Map containing single entry will be returned. Since the subject
	 *         group object does not contain the primary key, the key will be
	 *         held and returned in the key of the map. The value will contain
	 *         the actual subject group object.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding subject group failed.
	 */
	Map<Long, SubjectGroup> findSubjectGroupInfoBySubject(Long subjectId)
			throws PolicyFinderException;

	/**
	 * To retrieve subject group based on its primary key.
	 * 
	 * @param id
	 *            The primary key of the subject group.
	 * @return A Map containing single entry will be returned. Since the subject
	 *         group object does not contain the primary key, the key will be
	 *         held and returned in the key of the map. The value will contain
	 *         the actual subject group object.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding subject group failed.
	 */
	Map<Long, SubjectGroup> getSubjectGroupInfoById(Long id)
			throws PolicyFinderException;

	/**
	 * To retrieve the subject group by its unique name.
	 * 
	 * @param name
	 *            The unique subject group name
	 * @return A Map containing single entry will be returned. Since the subject
	 *         group object does not contain the primary key, the key will be
	 *         held and returned in the key of the map. The value will contain
	 *         the actual subject group object.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding subject group failed.
	 */
	Map<Long, SubjectGroup> getSubjectGroupInfoByName(String name)
			throws PolicyFinderException;

	/**
	 * To retrieve the subject groups of this provider type.
	 * 
	 * @return A Map containing single entry will be returned. Since the subject
	 *         group object does not contain the primary key, the key will be
	 *         held and returned in the key of the map. The value will contain
	 *         the actual subject group object.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding subject group failed.
	 */
	Map<Long, SubjectGroup> getSubjectGroupInfoByType()
			throws PolicyFinderException;

	/**
	 * Check if the current subject provider type is external.
	 * 
	 * @return true if is external and false internal.
	 * @throws PolicyProviderException
	 *             Throws this exception when finding subject failed.
	 */
	boolean isExternalSubjectType() throws PolicyProviderException;

	/**
	 * To retrieve external subject by the given name.
	 * 
	 * @param name
	 *            the name of the external subject
	 * @return A set of distinct subjects will be returned
	 * @throws PolicyFinderException
	 *             Throws this exception when finding subject failed.
	 */
	Set<Subject> getExternalSubjectByName(String name)
			throws PolicyFinderException;

	/**
	 * To retrieve the external subject by its primary key.
	 * 
	 * @param id
	 *            The primary key of the external subject to be retrieved.
	 * @return The subject object will be returned.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding subject failed.
	 */
	Subject getExternalSubjectById(Long id) throws PolicyFinderException;

	/**
	 * To retrieve subjects which have been assigned to the subject group.
	 * 
	 * @param subjectGroupId
	 *            The primary key of the subject group.
	 * @return A Map containing the subjects will be returned. The keys will be
	 *         containing the primary keys of the retrieved subjects. The values
	 *         of the map will contain the actual subject objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding subject failed.
	 */
	Map<Long, Subject> getSubjectAssignmentOfSubjectGroup(Long subjectGroupId)
			throws PolicyFinderException;

	/**
	 * To create a persistent record for the given subject group.
	 * 
	 * @param subjectGroup
	 *            The detail information of the subject to be created.
	 * @param subjectGroupEditObject
	 *            Contains a list of subjects to be assigned to the newly
	 *            created subject group
	 * @param createdBy
	 *            The subject who creates this subject group
	 * @return The key of the newly created subject group
	 * @throws PolicyCreationException
	 *             Throws this exception when creating subject group failed.
	 * @throws PolicyUpdateException
	 *             Throws this exception when updating subject group failed.
	 */
	SubjectGroupKey createSubjectGroup(SubjectGroup subjectGroup,
			SubjectGroupEditObject subjectGroupEditObject, SubjectKey createdBy)
			throws PolicyCreationException, PolicyUpdateException;

	/**
	 * To update the existing persistent subject group.
	 * 
	 * @param subjectGroup
	 *            the detail information to be updated.
	 * @param subjectGroupEditObject
	 *            Contains lists of subjects to be assigned an unassigned to the
	 *            subject group.
	 * @param createdBy
	 *            The subject who updates this subject group
	 * @return The key of the updated subject group
	 * @throws PolicyUpdateException
	 *             Throws this exception when updating subject group failed.
	 */
	SubjectGroupKey updateSubjectGroup(SubjectGroup subjectGroup,
			SubjectGroupEditObject subjectGroupEditObject, SubjectKey createdBy)
			throws PolicyUpdateException;

	/**
	 * To delete the persistent record of given subject group.
	 * 
	 * @param subjectGroupId
	 *            the Id of the subject group which is to be deleted
	 * @throws PolicyDeleteException
	 *             Throws this exception when deleting subject group failed.
	 * @throws PolicyUpdateException
	 *             Throws this exception when updating subject group failed.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding subject group failed.
	 */
	void deleteSubjectGroup(Long subjectGroupId) throws PolicyDeleteException,
			PolicyUpdateException, PolicyFinderException;

	/**
	 * Gets the group calculator information.
	 * 
	 * @param calculator
	 *            The name of the calculator
	 * @return The group calculator object
	 * @throws PolicyProviderException
	 *             Throws this exception when finding subject group calculator
	 *             failed.
	 */
	GroupCalculatorInfo getGroupCalculator(String calculator)
			throws PolicyProviderException;

	/**
	 * To retrieve the subject type info.
	 * 
	 * @return The subject type info object will be returned.
	 * @throws PolicyProviderException
	 *             Throws this exception when finding subject type failed.
	 */
	SubjectTypeInfo getSubjectTypeInfo() throws PolicyProviderException;

	/**
	 * Gets the audit history of given subject group between given dates.
	 * 
	 * @param subjectGroupKey
	 *            The key of the subject group to be audited
	 * @param startDate
	 *            Get history created after this date
	 * @param endDate
	 *            Get history created before this date
	 * @return A list of history is returned.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding history failed.
	 */
	List<EntityHistory> getAuditHistory(SubjectGroupKey subjectGroupKey,
			XMLGregorianCalendar startDate, XMLGregorianCalendar endDate)
			throws PolicyFinderException;

	/**
	 * Gets the audit history of given subject between given dates.
	 * 
	 * @param subjectKey
	 *            The key of the subject to be audited
	 * @param startDate
	 *            Get history created after this date
	 * @param endDate
	 *            Get history created before this date
	 * @return A map if history is returned. The keys of the map contains the
	 *         date of the history entry and the values contain the details of
	 *         the history
	 * @throws PolicyFinderException
	 *             Throws this exception when finding history failed.
	 */
	List<EntityHistory> getAuditHistory(SubjectKey subjectKey,
			XMLGregorianCalendar startDate, XMLGregorianCalendar endDate)
			throws PolicyFinderException;

	/**
	 * Audit the subject .
	 * 
	 * @param subjectKey
	 *            the policy key of the subject to be audited
	 * @param operationType
	 *            The audit operation type
	 * @param loginSubject
	 *            the subject key of the login subject
	 * @throws PolicyFinderException
	 *             Throws this exception when auditing
	 */
	void audit(SubjectKey subjectKey, String operationType,
			SubjectKey loginSubject) throws PolicyFinderException;

	/**
	 * Audit the subject group.
	 * 
	 * @param subjectGroupKey
	 *            the key of the subject group to be audited
	 * @param operationType
	 *            The audit operation type
	 * @param loginSubject
	 *            the subject key of the login subject
	 * @throws PolicyFinderException
	 *             Throws this exception when auditing
	 */
	void audit(SubjectGroupKey subjectGroupKey, String operationType,
			SubjectKey loginSubject) throws PolicyFinderException;

	/**
	 * To get the group calculator.
	 * 
	 * @return all the group calculators
	 * @throws PolicyFinderException
	 *             Throws this exception when finding subject group calculator.
	 */
	List<GroupCalculatorInfo> getGroupCalculators()
			throws PolicyFinderException;

	/**
	 * Retrieves the meta-data.
	 * 
	 * @param queryValue
	 *            The key of the meta-data to be retrieved
	 * @return The meta-data value is returned
	 * @throws PolicyFinderException
	 *             Throws this exception when retrieve meta-data
	 */
	List<KeyValuePair> getMetaData(String queryValue)
			throws PolicyFinderException;
}
