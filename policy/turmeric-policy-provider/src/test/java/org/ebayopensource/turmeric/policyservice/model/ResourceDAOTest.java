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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ebayopensource.turmeric.utils.jpa.EntityManagerContext;
import org.ebayopensource.turmeric.utils.jpa.JPAAroundAdvice;
import org.junit.Before;
import org.junit.Test;

public class ResourceDAOTest extends AbstractJPATest {

	private ResourceDAO resourceDAO;

	private Resource rs1;
	private Resource rs2;
	private Resource rs3;
	private Resource rs4;

	private Operation op1;
	private Operation op2;
	private Operation op3;
	private Operation op4;

	private static final String OBJECT_TYPE = "OBJECT";
	private static final String URL_TYPE = "URL";
	private static final String SERVICE_TYPE = "SERVICE";
	private static final String GENERIC_TYPE = "GENERIC";

	@Before
	public void initDAO() {
		ClassLoader classLoader = ResourceDAO.class.getClassLoader();
		Class[] interfaces = { ResourceDAO.class };
		ResourceDAO target = new ResourceDAOImpl();
		resourceDAO = (ResourceDAO) Proxy.newProxyInstance(classLoader,
				interfaces, new JPAAroundAdvice(factory, target));

		rs1 = new Resource(GENERIC_TYPE, "resource_name_1",
				"resource_description_1");
		rs2 = new Resource(GENERIC_TYPE, "resource_name_2",
				"resource_description_2");
		rs3 = new Resource(OBJECT_TYPE, "resource_name_3",
				"resource_description_3");
		rs4 = new Resource(SERVICE_TYPE, "resource_name_4", "resource_desicription_4");
		

		op1 = new Operation("operation_name_1", "op_description_1");
		op2 = new Operation("operation_name_2", "op_description_2");
		op3 = new Operation("operation_name_3", "op_description_3");
		op4 = new Operation("operation_name_4", "op_description_4");
	}

	@Test
	public void testPersistResource() throws Exception {

		resourceDAO.persistResource(rs1);

		EntityManagerContext.open(factory);
		try {
			Resource savedResource = EntityManagerContext.get().find(
					Resource.class, rs1.getId());
			assertNotNull(savedResource);
			assertEquals(GENERIC_TYPE, savedResource.getResourceType());

		} finally {
			EntityManagerContext.close();
		}
	}

	@Test
	public void testPersistOperation() throws Exception {

		resourceDAO.persistOperation(op1);

		EntityManagerContext.open(factory);
		try {
			Operation savedOperation = EntityManagerContext.get().find(
					Operation.class, op1.getId());
			assertNotNull(savedOperation);
			assertEquals("operation_name_1", savedOperation.getOperationName());

		} finally {
			EntityManagerContext.close();
		}
	}

	@Test
	public void testFindResourceById() throws Exception {
		resourceDAO.persistResource(rs1);

		Resource resource = resourceDAO.findResourceById(rs1.getId());
		assertNotNull(resource);
		assertEquals("resource_name_1", resource.getResourceName());
	}

	@Test
	public void testFindResourceByType() throws Exception {

		resourceDAO.persistResource(rs1);
		resourceDAO.persistResource(rs2);
		resourceDAO.persistResource(rs3);

		List<Resource> resources = resourceDAO.findResourceByType(GENERIC_TYPE);
		assertNotNull(resources);
		assertEquals(2, resources.size());
	}

	@Test
	public void testFindResourceByName() throws Exception {

		resourceDAO.persistResource(rs1);
		resourceDAO.persistResource(rs2);
		resourceDAO.persistResource(rs3);

		Resource resource = resourceDAO.findResourceByName("resource_name_2");
		assertNotNull(resource);
		assertEquals("resource_name_2", resource.getResourceName());
	}

	@Test
	public void testFindOperationById() throws Exception {
		resourceDAO.persistOperation(op1);

		Operation operation = resourceDAO.findOperationById(op1.getId());
		assertNotNull(operation);
		assertEquals("operation_name_1", operation.getOperationName());
	}

	@Test
	public void findOperationByName() throws Exception {

		Set<Operation> operations = new HashSet<Operation>();
		operations.add(op1);
		operations.add(op2);

		rs1.addOperations(operations);

		resourceDAO.persistResource(rs1);
		Operation operation = resourceDAO.findOperationByName(
				rs1.getResourceName(), op2.getOperationName(), GENERIC_TYPE);
		assertNotNull(operation);
		assertEquals("operation_name_2", operation.getOperationName());
	}
	
	@Test
	public void findOperationByNameServiceTypetest() throws Exception {

		Set<Operation> operations = new HashSet<Operation>();
		operations.add(op1);
		operations.add(op2);
		operations.add(op4);

		rs4.addOperations(operations);

		resourceDAO.persistResource(rs4);
		Operation operation = resourceDAO.findOperationByName(
				rs4.getResourceName(), op4.getOperationName(), SERVICE_TYPE);
		assertNotNull(operation);
		assertEquals("operation_name_4", operation.getOperationName());
	}

	@Test
	public void findOperationByResourceId() throws Exception {
		Set<Operation> operations_for_rs1 = new HashSet<Operation>();
		operations_for_rs1.add(op1);
		operations_for_rs1.add(op2);

		Set<Operation> operations_for_rs2 = new HashSet<Operation>();
		operations_for_rs2.add(op3);

		rs1.addOperations(operations_for_rs1);
		rs2.addOperations(operations_for_rs2);

		resourceDAO.persistResource(rs1);
		resourceDAO.persistResource(rs2);

		Set<Operation> operations = resourceDAO.findOperationByResourceId(rs2
				.getId());
		assertNotNull(operations);
		assertEquals(1, operations.size());

	}

	@Test
	public void findResourceByOperationId() throws Exception {
		Set<Operation> operations = new HashSet<Operation>();
		operations.add(op1);
		operations.add(op2);

		rs1.addOperations(operations);

		resourceDAO.persistResource(rs1);

		Resource resource = resourceDAO.findResourceByOperationId(op2.getId());
		assertNotNull(resource);
		assertEquals("resource_name_1", resource.getResourceName());
	}

	@Test
	public void removeResource() throws Exception {
		Set<Operation> operations = new HashSet<Operation>();
		operations.add(op1);
		rs1.addOperations(operations);

		resourceDAO.persistResource(rs1);

		EntityManagerContext.open(factory);
		try {
			Resource savedResource = EntityManagerContext.get().find(
					Resource.class, rs1.getId());
			assertNotNull(savedResource);

			resourceDAO.removeResource(rs1.getId());
			savedResource = EntityManagerContext.get().find(Resource.class,
					rs1.getId());
			assertNull(savedResource);

			Operation savedOperation = EntityManagerContext.get().find(
					Operation.class, op1.getId());
			assertNull(savedOperation);

		} finally {
			EntityManagerContext.close();
		}

	}

	@Test
	public void removeOperation() throws Exception {
        EntityManagerContext.open(factory);
        try {
    		resourceDAO.persistResource(rs1);
    		op1.setResource(rs1);
    		resourceDAO.persistOperation(op1);
    		rs1.getOperations().add(op1);
        } finally {
            EntityManagerContext.close();
        }
		
		EntityManagerContext.open(factory);
		try {
			Operation savedOperation = EntityManagerContext.get().find(
					Operation.class, op1.getId());
			assertNotNull(savedOperation);

			resourceDAO.removeOperation(op1.getResource().getId(),
					op1.getOperationName());

			savedOperation = EntityManagerContext.get().find(Operation.class,
					op1.getId());
			assertNull(savedOperation);

		} finally {
			EntityManagerContext.close();
		}
	}

}
