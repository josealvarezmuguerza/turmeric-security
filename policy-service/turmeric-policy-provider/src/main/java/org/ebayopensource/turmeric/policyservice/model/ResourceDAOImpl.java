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
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.ebayopensource.turmeric.policyservice.exceptions.PolicyFinderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.Category;
import org.ebayopensource.turmeric.security.v1.services.OperationKey;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.utils.jpa.AbstractDAO;

public class ResourceDAOImpl extends AbstractDAO implements ResourceDAO {

	@Override
	public void persistResource(final Resource resource) {
		persistEntity(resource);
	}

	@Override
	public void persistOperation(final Operation operation) {
		persistEntity(operation);
	}

	@Override
	public Resource findResourceById(final long id) {
		return findEntity(Resource.class, id);
	}

	@Override
	public Resource findResourceByName(final String name) {
		return getSingleResultOrNull(Resource.class, "resourceName", name);
	}

	@Override
	public List<Resource> findResourceByType(final String type) {
		return getResultList(Resource.class, "resourceType", type);
	}

	/*
	 * It removes a Resource entity an all its assigned operations
	 * 
	 * @see
	 * org.ebayopensource.turmeric.policyservice.model.ResourceDAO#removeResource
	 * (long)
	 */
	@Override
	public void removeResource(final long resourceId) {
		removeEntity(Resource.class, resourceId);
	}

	@Override
	public void removeOperation(final Long resourceId,
			final String operationName) {

		final Resource resource = findResourceById(resourceId);
		final Set<Operation> operations = resource.getOperations();

		for (Operation operation : operations) {
			if (operationName.equals(operation.getOperationName())) {
				removeOperation(operation.getId());
			}
		}
	}

	private void removeOperation(final long operationId) {
		removeEntity(Operation.class, operationId);
	}

	@Override
	public Operation findOperationById(final long operationId) {
		return findEntity(Operation.class, operationId);
	}

	@Override
	public Operation findOperationByName(final String resourceName,
			final String operationName, final String resourceType) {
		final StringBuilder jpql = new StringBuilder();
		jpql.append(" from ").append(Operation.class.getName())
				.append(" as op ");
		jpql.append(" where op.resource.resourceName = :resourceName ");
		jpql.append(" and op.operationName = :operationName ");
		jpql.append(" and op.resource.resourceType = :resourceType ");

		final EntityManager entityManager = getEntityManager();
		Query query = entityManager.createQuery(jpql.toString());
		query.setParameter("resourceName", resourceName);
		query.setParameter("operationName", operationName);
		query.setParameter("resourceType", resourceType);

		return getSingleResultOrNull(query);
	}

	@Override
	public Set<Operation> findOperationByResourceId(final long resourceId) {
		return findEntity(Resource.class, resourceId).getOperations();
	}

	@Override
	public Resource findResourceByOperationId(final long operationId) {

		final Operation op = findEntity(Operation.class, operationId);

		final StringBuilder jpql = new StringBuilder();
		jpql.append("from ").append(Resource.class.getName()).append(" as rs ");
		jpql.append(" where id = :id");

		final EntityManager entityManager = getEntityManager();
		final Query query = entityManager.createQuery(jpql.toString());
		query.setParameter("id", op.getResource().getId());

		return getSingleResultOrNull(query);

	}
    
	public static Resource convert(final org.ebayopensource.turmeric.security.v1.services.Resource resource) {

        final List<org.ebayopensource.turmeric.policyservice.model.Operation> operationList = 
            new ArrayList<org.ebayopensource.turmeric.policyservice.model.Operation>();
        final List<org.ebayopensource.turmeric.security.v1.services.Operation> operations = 
            resource.getOperation();
        if (operations != null && !operations.isEmpty()) {
            for (org.ebayopensource.turmeric.security.v1.services.Operation operation : operations) {
                operationList.add(convert(operation));
            }
        }

        return new org.ebayopensource.turmeric.policyservice.model.Resource(
                resource.getResourceType(), resource.getResourceName(),
                resource.getDescription());
    }

	public static org.ebayopensource.turmeric.security.v1.services.Resource convert(final Resource jpaResource)
            throws PolicyFinderException {
        final org.ebayopensource.turmeric.security.v1.services.Resource result = 
            new org.ebayopensource.turmeric.security.v1.services.Resource();
        result.setResourceName(jpaResource.getResourceName());
        result.setResourceType(jpaResource.getResourceType());
        result.setDescription(jpaResource.getDescription());
        result.setResourceId(jpaResource.getId());
        return result;
    }

	public static Operation convert(final org.ebayopensource.turmeric.security.v1.services.Operation operation) {
        return new org.ebayopensource.turmeric.policyservice.model.Operation(
                operation.getOperationName(), operation.getDescription());
    }

	public static org.ebayopensource.turmeric.security.v1.services.Operation convert(final Operation jpaOperation)
            throws PolicyFinderException {
        final org.ebayopensource.turmeric.security.v1.services.Operation result = 
            new org.ebayopensource.turmeric.security.v1.services.Operation();
        result.setOperationName(jpaOperation.getOperationName());
        result.setDescription(jpaOperation.getDescription());
        result.setOperationId(jpaOperation.getId());
        if(jpaOperation.getResource()!=null){
          result.setResourceId(jpaOperation.getResource().getId());
        }
        return result;
    }

    @Override
    public List<AuditHistory> getResourceHistory(long resourceId, Date start, Date end) {
        return getResultList(AuditHistory.class, "category", Category.RESOURCE.name(), 
                        "entityId", resourceId, "auditInfo.createdOn", start, end);
    }

    @Override
    public List<AuditHistory> getOperationHistory(long operationId, Date start, Date end) {
        return getResultList(AuditHistory.class, "category", Category.OPERATION.name(), 
                        "entityId", operationId, "auditInfo.createdOn", start, end);
    }
    
    @Override
    public void audit(ResourceKey resourceKey, String operationType, SubjectKey loginSubject) {
        persistEntity(AuditHistory.newRecord(resourceKey, operationType, loginSubject));
    }
    
    @Override
    public void audit(OperationKey operationKey, String operationType, SubjectKey loginSubject) {
        persistEntity(AuditHistory.newRecord(operationKey, operationType, loginSubject));
    }
}
