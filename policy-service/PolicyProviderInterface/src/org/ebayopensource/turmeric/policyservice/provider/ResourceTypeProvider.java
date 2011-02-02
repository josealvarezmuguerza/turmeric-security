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

import javax.xml.datatype.XMLGregorianCalendar;

import org.ebayopensource.turmeric.policyservice.exceptions.PolicyCreationException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyDeleteException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyFinderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyUpdateException;
import org.ebayopensource.turmeric.policyservice.provider.common.OperationEditObject;
import org.ebayopensource.turmeric.security.v1.services.EntityHistory;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePair;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.OperationKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;

/**
 * The interface for resource type provider. This interface need to be
 * implemented in order to provide detailed logic for each different resource
 * types.
 * 
 */
public interface ResourceTypeProvider {

	/**
	 * To create a new resource in the persistent storage. Also, operations will
	 * be created and assigned to the resource.
	 * 
	 * @param resource
	 *            The details of the resource to be created.
	 * @param operationEditObject
	 *            Contains the names of the operations to be created and
	 *            assigned to the resource. The operations are assumed not
	 *            existing.
	 * @param createdBy
	 *            The subject who creates this resource
	 * @return The resource key of the newly created resource will be returned.
	 * @throws PolicyCreationException
	 *             Throws this exception when creating resource failed.
	 */
	ResourceKey createResource(Resource resource,
			OperationEditObject operationEditObject, SubjectKey createdBy)
			throws PolicyCreationException;

	/**
	 * To update a resource of given ID. Both the record itself and the
	 * relationships to operations will be updated.
	 * 
	 * @param resource
	 *            The resource which is to be updated
	 * @param operationEditObject
	 *            Contains the names of the operations to be created and
	 *            removed.
	 * @param modifiedBy
	 *            The subject who is trying to modify the resource
	 * @return The resource key of the updated the resource will be returned.
	 * @throws PolicyUpdateException
	 *             Throws this exception when updating resource failed.
	 * @throws PolicyCreationException
	 *             Throws this exception when creating resource failed.
	 * @throws PolicyDeleteException
	 *             Throws this exception when deleting resource failed.
	 */
	ResourceKey updateResource(Resource resource,
			OperationEditObject operationEditObject, SubjectKey modifiedBy)
			throws PolicyUpdateException, PolicyCreationException,
			PolicyDeleteException;

	/**
	 * To permanently delete the resource from the persistent storage.
	 * 
	 * @param resourceId
	 *            The primary key of the resource to be deleted.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding resource failed.
	 * @throws PolicyDeleteException
	 *             Throws this exception when deleting resource failed.
	 */
	void deleteResource(Long resourceId) throws PolicyFinderException,
			PolicyDeleteException;

	/**
	 * To retrieve resource based on the given primary key.
	 * 
	 * @param resourceId
	 *            The primary key of the resource
	 * @return The resource object will be returned
	 * @throws PolicyFinderException
	 *             Throws this exception when finding resource failed.
	 */
	Resource getResourceInfoById(Long resourceId) throws PolicyFinderException;

	/**
	 * To retrieve resource based on the given unique resource name.
	 * 
	 * @param resourceName
	 *            The unique resource name.
	 * @return The resource object will be returned.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding resource failed.
	 */
	Resource getResourceInfoByName(String resourceName)
			throws PolicyFinderException;

	/**
	 * To retrieve resources of this provider type.
	 * 
	 * @return A Map containing distinct resources will be returned. The keys
	 *         will contain the primary keys of the resources and the values
	 *         will contain the actual resource objects.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding resource failed.
	 */
	Map<Long, Resource> getResourceInfoByType() throws PolicyFinderException;

	/**
	 * To retrieve operation based on the operation id and resource name.
	 * 
	 * @param resourceName
	 *            The resource name which the operation is assigned to.
	 * @param operationId
	 *            The primary key of the operation to retrieve.
	 * @return The actual operation object will be returned.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding resource failed.
	 */
	Operation getOperationById(String resourceName, Long operationId)
			throws PolicyFinderException;

	/**
	 * To retrieve the operation based on the operation name and its assigned
	 * resource name.
	 * 
	 * @param resourceName
	 *            The name of the resource which the operation was assigned to.
	 * @param operationName
	 *            The name of the operation.
	 * @return The actual operation object will be returned.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding resource failed.
	 */
	Operation getOperationByName(String resourceName, String operationName)
			throws PolicyFinderException;

	/**
	 * To retrieve all operations which have been assigned to the given resource.
	 * 
	 * @param resourceId
	 *            The primary key of the resource.
	 * @return List of operations will be returned.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding resource failed.
	 */
	List<Operation> getOperationByResourceId(Long resourceId)
			throws PolicyFinderException;

	/**
	 * To retrieve the resource which the given operation was assigned to.
	 * 
	 * @param operationId
	 *            The primary key of the operation.
	 * @return The resource object will be returned
	 * @throws PolicyFinderException
	 *             Throws this exception when finding resource failed.
	 */
	Resource getResourceInfoByOperationId(Long operationId)
			throws PolicyFinderException;

	/**
	 * Gets the audit history of given resource between given dates.
	 * 
	 * @param resourceKey
	 *            The key of the resource to be audited
	 * @param startDate
	 *            Get history created after this date
	 * @param endDate
	 *            Get history created before this date
	 * @return A list of history is returned.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding resource failed.
	 */
	List<EntityHistory> getAuditHistory(ResourceKey resourceKey,
			XMLGregorianCalendar startDate, XMLGregorianCalendar endDate)
			throws PolicyFinderException;


	/**
	 * Gets the audit history of given operation between given dates.
	 * 
	 * @param operationKey
	 *            The key of the resource to be audited
	 * @param startDate
	 *            Get history created after this date
	 * @param endDate
	 *            Get history created before this date
	 * @return A list of history is returned.
	 * @throws PolicyFinderException
	 *             Throws this exception when finding resource failed.
	 */
	List<EntityHistory> getAuditHistory(OperationKey operationKey,
			XMLGregorianCalendar startDate, XMLGregorianCalendar endDate)
			throws PolicyFinderException;

	
	/**
	 * To audit the given resource.
	 * 
	 * @param resourceKey
	 *            The key of the resource which need to be audited
	 * @param operationType
	 *            The type of the action to be audited
	 * @param loginSubject
	 *            The key of the login subject to be audited
	 * @throws PolicyFinderException
	 *             Throws this exception when finding resource failed.
	 */
	void audit(ResourceKey resourceKey, String operationType,
			SubjectKey loginSubject) throws PolicyFinderException;

	/**
	 * To audit the given resource.
	 * 
	 * @param operationKey
	 *            The key of the resource which need to be audited
	 * @param operationType
	 *            The type of the action to be audited
	 * @param loginSubject
	 *            The key of the login subject to be audited
	 * @throws PolicyFinderException
	 *             Throws this exception when finding resource failed.
	 */
	void audit(OperationKey operationKey, String operationType,
			SubjectKey loginSubject) throws PolicyFinderException;

	/**
	 * Retrieves the meta-data.
	 * 
	 * @param queryValue
	 *            The key of the meta-data to be retrieved
	 * @return The meta-data value is returned
	 * @throws PolicyFinderException
	 *             Throws this exception when finding resource failed.
	 */
	List<KeyValuePair> getMetaData(String queryValue)
			throws PolicyFinderException;
}
