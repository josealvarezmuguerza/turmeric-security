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
import java.util.HashSet;
import java.util.Set;

import org.ebayopensource.turmeric.policyservice.model.AbstractJPATest;
import org.ebayopensource.turmeric.policyservice.model.Operation;
import org.ebayopensource.turmeric.policyservice.model.Resource;
import org.ebayopensource.turmeric.policyservice.model.Subject;
import org.ebayopensource.turmeric.utils.jpa.AbstractDAO;
import org.ebayopensource.turmeric.utils.jpa.JPAAroundAdvice;
import org.junit.Before;

public abstract class ResourceTestBase extends AbstractJPATest {
	private TestDAO testDAO;

	private static final String[][] subjectData = { { "jdoe", "USER",
			"John Doe", "", "jdoe@ebayopensource.org" } };

	private static final String[][] resourceData = {
			{ "GENERIC", "resource_name_generic", "a generic resource" },
			{ "OBJECT", "resource_name_object", "a object resource" },
			{ "URL", "resource_name_url", "a url resource" },
			{ "SERVICE", "resource_name_service", "a service resource" }, };

	private static final String[][] operationData = {
			{ "operation_name_1", "first operation" },
			{ "operation_name_2", "second operation" },
			{ "operation_new", "new operation" }, };

	@Before
	public void initDAO() {
		ClassLoader classLoader = TestDAO.class.getClassLoader();
		Class[] interfaces = { TestDAO.class };
		TestDAO target = new TestDAOImpl();
		testDAO = (TestDAO) Proxy.newProxyInstance(classLoader, interfaces,
				new JPAAroundAdvice(factory, target));

		initDatabase();
	}

	protected void initDatabase() {
		for (String[] subjectField : subjectData) {

			testDAO.persistEntity(new Subject(subjectField[0], subjectField[1],
					subjectField[2], subjectField[3], 0, subjectField[4]));
		}

		for (String[] operationField : operationData) {
			testDAO.persistEntity(new Operation(operationField[0],
					operationField[1]));
		}

		for (String[] resourceField : resourceData) {

			testDAO.persistEntity(new Resource(resourceField[0],
					resourceField[1], resourceField[2]));
		}

		// adding another resource with operations
		final Resource resourceWithOps = new Resource("GENERIC",
				"generic_rs_with_op1",
				"a Generic resource linked with operation 1");
		Set<Operation> operations = new HashSet<Operation>();
		operations.add(new Operation("operation_default", "operation_default_description"));
		resourceWithOps.addOperations(operations);

		testDAO.persistEntity(resourceWithOps);
	}

	public static interface TestDAO {
		public void persistEntity(Object entity);
	}

	public static class TestDAOImpl extends AbstractDAO implements TestDAO {
		public void persistEntity(Object entity) {
			try {
				super.persistEntity(entity);
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}
}
