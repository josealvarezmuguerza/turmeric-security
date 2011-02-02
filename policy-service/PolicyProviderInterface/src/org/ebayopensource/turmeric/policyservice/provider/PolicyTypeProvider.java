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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.ebayopensource.turmeric.policyservice.exceptions.PolicyCreationException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyDeleteException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyFinderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyUpdateException;
import org.ebayopensource.turmeric.policyservice.provider.common.PolicyBuilderObject;
import org.ebayopensource.turmeric.policyservice.provider.common.PolicyEditObject;
import org.ebayopensource.turmeric.security.v1.services.EntityHistory;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePair;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.QueryCondition;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Rule;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectTypeInfo;

/**
 * PolicyTypeProvider is the interface for all custom policy providers. This
 * interface need to be implemented in order to provide the detail logic for
 * each different providers.
 */
public interface PolicyTypeProvider {

	/**
	 * To update the policy of given policy ID. All relationship to the joined
	 * records will be updated. No validation will be performed on all the
	 * joined records. Only rules will be actually created or deleted.
	 * Resources, operations, subject types, subjects and subject groups should
	 * be existing and will be only assigned/unassigned.
	 * 
	 * @param inputPolicy
	 *            The updating information of the policy
	 * @param policyEditObject
	 *            The updating information of the rules, resources and subjects
	 * @param modifiedBy
	 *            The subject who calls this method
	 * @return The key of the updated policy
	 * @throws PolicyUpdateException
	 *             Throws this exception when updating failed.
	 * @throws PolicyCreationException
	 *             Throws this exception when creating failed.
	 * @throws PolicyDeleteException
	 *             Throws this exception when deleting failed.
	 */
	PolicyKey updatePolicy(Policy inputPolicy,
			PolicyEditObject policyEditObject, SubjectKey modifiedBy)
			throws PolicyUpdateException, PolicyCreationException,
			PolicyDeleteException;

	/**
	 * To create a new policy record in the persistent storage. All relationship
	 * to the joined records will be created or updated. No validation will be
	 * performed on any existing records. Rules will be actually created in the
	 * persistent storage and assigned to the policy. Resources, operations,
	 * subject types, subjects and subject groups should be already existing and
	 * will be only assigned to the policy.
	 * 
	 * @param inputPolicy
	 *            The updating information of the policy
	 * @param policyEditObject
	 *            The updating information of the rules, resources and subjects
	 * @param createdBy
	 *            The subject who calls this method
	 * @return The key of the created policy
	 * @throws PolicyUpdateException
	 *             Throws this exception when updating failed.
	 * @throws PolicyCreationException
	 *             Throws this exception when creating failed.
	 */
	PolicyKey createPolicy(Policy inputPolicy,
			PolicyEditObject policyEditObject, SubjectKey createdBy)
			throws PolicyCreationException, PolicyUpdateException;

	/**
	 * To delete a policy of given policy ID. All relationship to joined the
	 * records will be removed.
	 * 
	 * @param policyId
	 *            The primary key of the policy which is to be deleted.
	 * @throws PolicyUpdateException
	 *             Throws this exception when updating failed.
	 * @throws PolicyDeleteException
	 *             Throws this exception when deleting failed.
	 */
	void deletePolicy(Long policyId) throws PolicyDeleteException,
			PolicyUpdateException;

	/**
	 * To retrieve the rules of the given policy Id from the persistent storage.
	 * 
	 * @param policyId
	 *            The primary key of the policy which used to retrieve the
	 *            related rules
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map object containing distinct rules will be returned. The keys
	 *         of the map are the primary keys of the rules and the values of
	 *         the map contains the relative rule objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, Rule> getRuleAssignmentOfPolicy(Long policyId,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve a rule based on given rule name from persistent storage.
	 * 
	 * @param ruleName
	 *            is the rule has been used
	 * @return The rule object contains full details will be returned
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	boolean isRuleNameUsed(String ruleName) throws PolicyFinderException;

	/**
	 * Check if rules are required of this policy type.
	 * 
	 * @return true when required and false otherwise
	 * @throws PolicyProviderException
	 *             policy related exception
	 */
	boolean isRuleRequired() throws PolicyProviderException;

	/**
	 * Check if the given rule is valid.
	 * 
	 * @param rule rule to be checked.
	 * @return true when valid and false otherwise
	 * @throws PolicyProviderException
	 *             policy related exception
	 */
	boolean isRuleValid(Rule rule) throws PolicyProviderException;

	/**
	 * To retrieve all resources which have been assigned to the given policy.
	 * 
	 * @param policyId
	 *            The primary key of the policy which used to retrieve the
	 *            related resources
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map object containing distinct resources will be returned. The
	 *         keys of the map are the primary keys of the resources and the
	 *         values of the map contains the relative resource objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, Resource> getResourceAssignmentOfPolicy(Long policyId,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve all operations which have been assigned to the given policy.
	 * 
	 * @param policyId
	 *            The primary key of the policy which used to retrieve the
	 *            related operations
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map object containing distinct operations will be returned. The
	 *         keys of the map are the primary keys of the operations and the
	 *         values of the map contains the relative operation objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, Operation> getOperationAssignmentOfPolicy(Long policyId,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve all subjects which have been assigned to the give policy.
	 * 
	 * @param policyId
	 *            The primary key of the policy which used to retrieve all
	 *            previous assigned subjects.
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map object containing distinct subjects will be returned. The
	 *         keys of the map are the primary keys of the subjects and the
	 *         values of the map contains the relative subject objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, Subject> getSubjectAssignmentOfPolicy(Long policyId,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve all exclusion subjects from a policy.
	 * 
	 * @param policyId
	 *            The primary key of the policy which used to retrieve all
	 *            previous assigned exclusion subjects.
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map object containing distinct subjects will be returned. The
	 *         keys of the map are the primary keys of the subjects and the
	 *         values of the map contains the relative subject objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, Subject> getExclusionSubjectAssignmentOfPolicy(Long policyId,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve all subject types of a policy.
	 * 
	 * @param policyId
	 *            The primary key of the policy.
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A list of subject types.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, SubjectTypeInfo> getSubjectTypeAssignmentOfPolicy(Long policyId,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve all assigned subject groups from a given policy.
	 * 
	 * @param policyId
	 *            The primary key of the policy which used to retrieve all
	 *            related subject groups
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map object containing distinct subject groups will be returned.
	 *         The keys of the map are the primary keys of the subject groups
	 *         and the values of the map contains the relative subject group
	 *         objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, SubjectGroup> getSubjectGroupAssignmentOfPolicy(Long policyId,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve all exclusion subject groups from a policy.
	 * 
	 * @param policyId
	 *            The primary key of policy which used to retrieve all related
	 *            exclusion subject groups
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map object containing distinct exclusion subject groups will be
	 *         returned. The keys of the map are the primary keys of the
	 *         exclusion subject groups and the values of the map contains the
	 *         relative exclusion subject group objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, SubjectGroup> getExclusionSubjectGroupAssignmentOfPolicy(
			Long policyId, QueryCondition queryCondition)
			throws PolicyFinderException;

	/**
	 * To retrieve all policies which the given subjects have been assigned to.
	 * 
	 * @param subjectId
	 *            List of primary keys of the subjects which used to retrieve
	 *            all policies that have been assigned to.
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map containing distinct policies will be returned. The keys
	 *         will be the policy primary keys and the values will contain the
	 *         policy objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, Policy> findPolicyInfoBySubject(Set<Long> subjectId,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve all policies which the given exclusion subjects have been
	 * assigned to.
	 * 
	 * @param subjectId
	 *            List of primary keys of the exclusion subjects which used to
	 *            retrieve all policies that have been assigned to.
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map containing distinct policies will be returned. The keys
	 *         will be the policy primary keys and the values will contain the
	 *         policy objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, Policy> findPolicyInfoByExclusionSubject(Set<Long> subjectId,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve all policies which the given subject groups have been
	 * assigned to.
	 * 
	 * @param subjectGroupId
	 *            List of primary keys of the subject groups which used to
	 *            retrieve all policies that have been assigned to.
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map containing distinct policies will be returned. The keys
	 *         will be the policy primary keys and the values will contain the
	 *         policy objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, Policy> findPolicyInfoBySubjectGroup(Set<Long> subjectGroupId,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve all policies which the given exclusion subject groups have
	 * been assigned to.
	 * 
	 * @param subjectGroupId
	 *            List of primary keys of the exclusion subject groups which
	 *            used to retrieve all policies that have been assigned to.
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map containing distinct policies will be returned. The keys
	 *         will be the policy primary keys and the values will contain the
	 *         policy objects.
	 * @throws PolicyProviderException
	 *             policy related exception
	 */
	Map<Long, Policy> findPolicyInfoByExclusionSubjectGroup(
			Set<Long> subjectGroupId, QueryCondition queryCondition)
			throws PolicyProviderException;

	/**
	 * To retrieve all policies which have the given subject type been assigned.
	 * to.
	 * 
	 * @param subjectType
	 *            The list of subject type names
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map containing distinct policies will be returned. The keys
	 *         will be the policy primary keys and the values will contain the
	 *         policy objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, Policy> findPolicyInfoBySubjectType(Set<String> subjectType,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve all policies which the given operations have been assigned
	 * to.
	 * 
	 * @param operationId
	 *            List of primary keys of the operations which used to retrieve
	 *            all policies that have been assigned to.
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map containing distinct policies will be returned. The keys
	 *         will be the policy primary keys and the values will contain the
	 *         policy objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, Policy> findPolicyInfoByOperation(Set<Long> operationId,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve all policies which the given resources have been assigned to.
	 * 
	 * @param resourceId
	 *            List of primary keys of the resources which used to retrieve
	 *            all policies that have been assigned to.
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map containing distinct policies will be returned. The keys
	 *         will be the policy primary keys and the values will contain the
	 *         policy objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, Policy> findPolicyInfoByResource(Set<Long> resourceId,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve all policies based on given policy key and effect.
	 * 
	 * @param policyKey
	 *            The policy key used to retrieve the policies. ID will be used
	 *            if the key contains ID. If no ID is given, but name is given,
	 *            both name and effect will be used as the criteria. If ID and
	 *            name are both missing, effect will be used only.
	 * @param effect
	 *            The effect name.
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return A Map containing distinct policies will be returned. The keys
	 *         will be the policy primary keys and the values will contain the
	 *         policy objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Map<Long, Policy> findPolicyInfo(PolicyKey policyKey, String effect,
			QueryCondition queryCondition) throws PolicyFinderException;

	/**
	 * To retrieve the policy of given primary key.
	 * 
	 * @param policyId
	 *            The primary key of the policy to be retrieved
	 * @return The object of the retrieved policy.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Policy getPolicyInfo(Long policyId) throws PolicyFinderException;

	/**
	 * To retrieve the policy of given unique policy name.
	 * 
	 * @param policyName
	 *            The unique policy name of the policy to be retrieved
	 * @return The object of the retrieved policy.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	Policy getPolicyInfo(String policyName) throws PolicyFinderException;

	/**
	 * Check if there is any update after the given last update date.
	 * 
	 * @param lastUpdated
	 *            Last update date
	 * @return True if update is required, otherwise false.
	 * @throws PolicyProviderException
	 *             policy related exception
	 */
	boolean isUpdateRequired(Date lastUpdated) throws PolicyProviderException;

	/**
	 * Validates a policy.
	 * 
	 * @param policy
	 *            The policy to be validated.
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return True if valid and otherwise false.
	 * @throws PolicyProviderException
	 *             policy related exception
	 */
	boolean validatePolicy(Policy policy, QueryCondition queryCondition)
			throws PolicyProviderException;

	/**
	 * Gets the audit history of given policy between given dates.
	 * 
	 * @param policyKey
	 *            The key of the policy to be audited
	 * @param startDate
	 *            Get history created after this date
	 * @param endDate
	 *            Get history created before this date
	 * @return A list of history is returned.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	List<EntityHistory> getAuditHistory(PolicyKey policyKey,
			XMLGregorianCalendar startDate, XMLGregorianCalendar endDate)
			throws PolicyFinderException;

	/**
	 * Audit the policy.
	 * 
	 * @param policyKey
	 *            the policy key of the policy to be audited
	 * @param operationType
	 *            The type of action to be audited
	 * @param loginSubject
	 *            the subject key of the login subject to be audited
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	void audit(PolicyKey policyKey, String operationType,
			SubjectKey loginSubject) throws PolicyFinderException;

	/**
	 * Apply a list of query conditions to the builder object.
	 * 
	 * @param builderObject
	 *            The builder object which the query condition will be applied
	 *            to
	 * @param queryCondition
	 *            A list of query conditions need to be applied to the result.
	 * @return The result builder object is returned.
	 */
	PolicyBuilderObject applyQueryCondition(PolicyBuilderObject builderObject,
			QueryCondition queryCondition);

	/**
	 * Check if the policy type allows operation level of resources.
	 * 
	 * @return True if operation level of resource is allow and false otherwise.
	 */
	boolean allowResourceLevel();

	/**
	 * Check if the policy type allows global type.
	 * 
	 * @return True if allows and false otherwise.
	 */
	boolean allowGlobalLevel();

	/**
	 * Retrieves the meta-data.
	 * 
	 * @param queryValue
	 *            The key of the meta-data to be retrieved
	 * @return The meta-data value is returned
	 * @throws PolicyFinderException
	 *             Throws this exception when finding failed.
	 */
	List<KeyValuePair> getMetaData(String queryValue)
			throws PolicyFinderException;
}
