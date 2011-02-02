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
import java.util.Set;

import org.ebayopensource.turmeric.security.v1.services.OperationKey;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;

public interface ResourceDAO {

	void persistResource(Resource resource);
	
	void persistOperation(Operation operation);

	Resource findResourceById(long id);

	Resource findResourceByName(String name);

	List<Resource> findResourceByType(String type);

	Operation findOperationById(long operationId);

	Set<Operation> findOperationByResourceId(long resourceId);

	Resource findResourceByOperationId(long operationId);

	Operation findOperationByName(String resourceName, String operationName, String resourceType);

	void removeResource(long resourceId);

	void removeOperation(Long resourceId, String operationName);

    void audit(ResourceKey resourceKey, String operationType, SubjectKey loginSubject);

    void audit(OperationKey operationKey, String operationType, SubjectKey loginSubject);

    List<AuditHistory> getResourceHistory(long resourceId, Date start, Date end);

    List<AuditHistory> getOperationHistory(long operationId, Date start, Date end);

}
