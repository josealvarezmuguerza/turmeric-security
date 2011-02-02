/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.easymock.EasyMock;
import org.ebayopensource.turmeric.policyservice.provider.common.OperationEditObject;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.utils.jpa.EntityManagerContext;
import org.junit.Before;
import org.junit.Test;


public class GENERICResourceTest extends ResourceTestBase {

	private OperationEditObject operationEditObject;

	private GENERICResource genericResource;
	private Resource resource;
	private SubjectKey userKey;

	@Before
	public void setup() {
		operationEditObject = EasyMock.createMock(OperationEditObject.class);

		genericResource = new GENERICResource();
		resource = createResource("rs_test_1_name", "rs_test_1_description",
				"GENERIC");
	}

	@Test
	public void createResourceTest() throws Exception {

		EasyMock.reset(operationEditObject);
		// Operation List to add
		List<String> addOpNameList = new ArrayList<String>();
		addOpNameList.add("operation_1");
		addOpNameList.add("operation_2");
		EasyMock.expect(operationEditObject.getAddList()).andReturn(
				addOpNameList);

		EasyMock.replay(operationEditObject);

		userKey = getUserKey("jdoe");

		ResourceKey genericKey = genericResource.createResource(resource,
				operationEditObject, userKey);
		EasyMock.verify(operationEditObject);

		EntityManagerContext.open(factory);
		try {
			org.ebayopensource.turmeric.policyservice.model.Resource savedResource = EntityManagerContext
					.get()
					.find(org.ebayopensource.turmeric.policyservice.model.Resource.class,
							genericKey.getResourceId());
			assertNotNull(savedResource);
			assertEquals(2, savedResource.getOperations().size());
		} finally {
			EntityManagerContext.close();
		}

	}

	@Test
	public void updateResourceTest() throws Exception {

		userKey = getUserKey("jdoe");

		EasyMock.reset(operationEditObject);
		// Operation List to add
		List<String> addOpNameList = new ArrayList<String>();
		addOpNameList.add("operation_1");
		addOpNameList.add("operation_2");
		EasyMock.expect(operationEditObject.getAddList()).andReturn(
				addOpNameList);
		EasyMock.replay(operationEditObject);
		ResourceKey genericKey = genericResource.createResource(resource,
				operationEditObject, userKey);
		resource.setResourceId(genericKey.getResourceId());

		EasyMock.reset(operationEditObject);
		// Operation List to add
		addOpNameList = new ArrayList<String>();
		addOpNameList.add("operation_new");

		EasyMock.expect(operationEditObject.getAddList()).andReturn(
				addOpNameList);

		// Operation List to remove
		List<String> removeOpNameList = new ArrayList<String>();
		EasyMock.expect(operationEditObject.getRemoveList()).andReturn(
				removeOpNameList);
		EasyMock.replay(operationEditObject);

		genericKey = genericResource.updateResource(resource,
				operationEditObject, userKey);
		EasyMock.verify(operationEditObject);
		EntityManagerContext.open(factory);

		try {
			org.ebayopensource.turmeric.policyservice.model.Resource savedResource = EntityManagerContext
					.get()
					.find(org.ebayopensource.turmeric.policyservice.model.Resource.class,
							genericKey.getResourceId());
			assertNotNull(savedResource);

			Query query = EntityManagerContext
					.get()
					.createQuery(
							"select rs.operations from "
									+ "org.ebayopensource.turmeric.policyservice.model.Resource as rs "
									+ "where rs.id = "
									+ genericKey.getResourceId());
			@SuppressWarnings("unchecked")
			List<org.ebayopensource.turmeric.policyservice.model.Operation> operationList = query
					.getResultList();
			assertEquals(3, operationList.size());

		} finally {
			EntityManagerContext.close();
		}

	}

	@Test
	public void deleteResourceTest() throws Exception {

		EasyMock.reset(operationEditObject);
		// Operation List to add
		List<String> addOpNameList = new ArrayList<String>();
		addOpNameList.add("operation_1");
		addOpNameList.add("operation_2");
		EasyMock.expect(operationEditObject.getAddList()).andReturn(
				addOpNameList);

		EasyMock.replay(operationEditObject);

		userKey = getUserKey("jdoe");

		ResourceKey genericKey = genericResource.createResource(resource,
				operationEditObject, userKey);

		genericResource.deleteResource(genericKey.getResourceId());

		EntityManagerContext.open(factory);
		try {
			org.ebayopensource.turmeric.policyservice.model.Resource deletedResource = EntityManagerContext
					.get()
					.find(org.ebayopensource.turmeric.policyservice.model.Resource.class,
							genericKey.getResourceId());
			assertNull(deletedResource);
		} finally {
			EntityManagerContext.close();
		}
	}

	@Test
	public void createEmptyResourceTest() throws Exception {
		userKey = getUserKey("jdoe");

		EasyMock.reset(operationEditObject);
		// No Operation added
		EasyMock.expect(operationEditObject.getAddList()).andReturn(null);

		EasyMock.replay(operationEditObject);

		ResourceKey genericKey = genericResource.createResource(resource,
				operationEditObject, userKey);

		EasyMock.verify(operationEditObject);

		EntityManagerContext.open(factory);
		try {
			org.ebayopensource.turmeric.policyservice.model.Resource savedResource = EntityManagerContext
					.get()
					.find(org.ebayopensource.turmeric.policyservice.model.Resource.class,
							genericKey.getResourceId());
			assertNotNull(savedResource);
			assertEquals(0, savedResource.getOperations().size());
		} finally {
			EntityManagerContext.close();
		}
	}

	@Test
	public void createAnotherEmptyResourceTest() throws Exception {
		userKey = getUserKey("jdoe");

		ResourceKey genericKey = genericResource.createResource(resource, null,
				userKey);

		EntityManagerContext.open(factory);
		try {
			org.ebayopensource.turmeric.policyservice.model.Resource savedResource = EntityManagerContext
					.get()
					.find(org.ebayopensource.turmeric.policyservice.model.Resource.class,
							genericKey.getResourceId());
			assertNotNull(savedResource);
			assertEquals(0, savedResource.getOperations().size());
		} finally {
			EntityManagerContext.close();
		}
	}

	private Resource createResource(final String rdName,
			final String rsDescription, final String rsType) {
		final Resource resource = new Resource();
		resource.setResourceName(rdName);
		resource.setDescription(rsDescription);
		resource.setResourceType(rsType);
		return resource;
	}

	private SubjectKey getUserKey(final String name) throws Exception {
		final USERSubject userProvider = new USERSubject();
		final Map<Long, Subject> usrMap = userProvider.getSubjectByName(name);

		final SubjectKey userKey = new SubjectKey();
		final Subject subject = usrMap.values().toArray(new Subject[1])[0];
		userKey.setSubjectId((Long) usrMap.keySet().toArray(new Long[1])[0]);
		userKey.setSubjectName(subject.getSubjectName());
		userKey.setSubjectType(subject.getSubjectType());

		return userKey;
	}

}
