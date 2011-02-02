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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Proxy;

import org.ebayopensource.turmeric.utils.jpa.JPAAroundAdvice;
import org.junit.Before;
import org.junit.Test;

/**
 * @author gbaal
 * 
 */
public class RuleDaoTest extends AbstractJPATest {
	RuleDAO ruleDAO;

	@Before
	public void initDAO() {
		ClassLoader classLoader = RuleDAO.class.getClassLoader();
		Class[] interfaces = { RuleDAO.class };
		RuleDAO target = new RuleDAOImpl();
		ruleDAO = (RuleDAO) Proxy.newProxyInstance(classLoader, interfaces,
				new JPAAroundAdvice(factory, target));
	}

	private PrimitiveValue createPrimitiveValue() {
		PrimitiveValue value = new PrimitiveValue();
		value.setType(SupportedPrimitive.STRING);
		value.setValue("PaymentService:commit.count>5");
		return value;
	}

	private Rule createRule() {
		Rule rule = new Rule();
		rule.setCondition(createCondition());
		rule.setEffect(EffectType.BLOCK);
		rule.setEffectDuration(3600L);
		rule.setPriority(2);
		rule.setRolloverPeriod(1L);
		rule.setRuleName("RLRule");

		return rule;
	}

	private Condition createCondition() {
		Condition condition = new Condition();

		Expression expression = createExpression();
		condition.setExpression(expression);
		return condition;
	}

	private Expression createExpression() {
		PrimitiveValue value = createPrimitiveValue();
		Expression expression = new Expression(value, "Service count", "HITS");
		expression.setPrimitiveValue(value);
		return expression;
	}

	@Test
	public void findConditionByIdTest() {
		Condition condition = createCondition();
		ruleDAO.persistCondition(condition);
		assertNotNull(condition.getId());
		assertNotNull(ruleDAO.findConditionById(condition.getId()));
	}

	@Test
	public void findConditionByRuleIdTest() {
		Rule rule = createRule();
		ruleDAO.persistRule(rule);
		assertNotNull(rule.getId());
		assertNotNull(ruleDAO.findConditionByRuleId(rule.getId()));
	}

	@Test
	public void findExpressionByConditionIdTest() {
		Condition condition = createCondition();
		ruleDAO.persistCondition(condition);
		assertNotNull(condition.getId());
		assertNotNull(ruleDAO.findExpressionByConditionId(condition.getId()));
	}

	@Test
	public void findExpressionByIdTest() {
		Expression expression = createExpression();
		ruleDAO.persistExpression(expression);
		assertNotNull(expression.getId());
		assertNotNull(ruleDAO.findExpressionById(expression.getId()));
	}

	@Test
	public void findExpressionByNameTest() {
		Expression expression = createExpression();
		ruleDAO.persistExpression(expression);
		assertNotNull(expression.getId());
		assertNotNull(ruleDAO.findExpressionByName(expression.getName()));
	}

	@Test
	public void findPrimitiveValueByExpressionIdTest() {
		Expression expression = createExpression();
		ruleDAO.persistExpression(expression);
		assertNotNull(expression.getId());
		assertNotNull(ruleDAO.findPrimitiveValueByExpressionId(expression
				.getId()));
	}

	@Test
	public void findPrimitiveValueByIdTest() {
		PrimitiveValue primitiveValue = createPrimitiveValue();
		ruleDAO.persistPrimitiveValue(primitiveValue);
		assertNotNull(primitiveValue.getId());
		assertNotNull(ruleDAO.findPrimitiveValueById(primitiveValue.getId()));

	}

	@Test
	public void findRuleByIdTest() {
		Rule rule = createRule();
		ruleDAO.persistRule(rule);
		assertNotNull(rule.getId());
		assertNotNull(ruleDAO.findRuleById(rule.getId()));
	}

	@Test
	public void findRuleByNameTest() {
		Rule rule = createRule();
		ruleDAO.persistRule(rule);
		assertNotNull(rule.getId());
		assertNotNull(ruleDAO.findRuleByName(rule.getRuleName()));
	}

	@Test
	public void persistConditionTest() {
		Condition condition = createCondition();
		ruleDAO.persistCondition(condition);
		assertNotNull(condition.getId());

	}

	@Test
	public void persistExpressionTest() {
		Expression expression = createExpression();
		ruleDAO.persistExpression(expression);
		assertNotNull(expression.getId());

	}

	@Test
	public void persistPrimitiveValueTest() {
		PrimitiveValue primitiveValue = createPrimitiveValue();
		ruleDAO.persistPrimitiveValue(primitiveValue);
		assertNotNull(primitiveValue.getId());

	}

	@Test
	public void persistRuleTest() {
		Rule rule = createRule();
		ruleDAO.persistRule(rule);
		assertNotNull(rule.getId());

	}

	@Test
	public void removePrimitiveValue() {
		PrimitiveValue primitiveValue = createPrimitiveValue();
		ruleDAO.persistPrimitiveValue(primitiveValue);
		Long id = primitiveValue.getId();
		assertNotNull(id);
		ruleDAO.removePrimitiveValue(id);
		assertNull(ruleDAO.findPrimitiveValueById(id));

	}

	@Test
	public void removeRule() {
		Rule rule = createRule();
		ruleDAO.persistRule(rule);
		Long id = rule.getId();
		String name = rule.getRuleName();
		assertNotNull(id);
		ruleDAO.removeRule(id);
		assertNull(ruleDAO.findRuleById(id));
		assertNull(ruleDAO.findRuleByName(name));
	}

	@Test
	public void isRuleNameUsedTest() {
		Rule rule = createRule();
		assertFalse(ruleDAO.isRuleNameUsed(rule.getRuleName()));
		ruleDAO.persistRule(rule);
		assertTrue(ruleDAO.isRuleNameUsed(rule.getRuleName()));

	}



	@Test
	public void isRuleValidTest() {
		Rule rule = createRule();
		assertTrue(ruleDAO.isRuleValid(rule,false));
		rule.getCondition().getExpression().setPrimitiveValue(null);
		assertFalse(ruleDAO.isRuleValid(rule,false));
		assertFalse(ruleDAO.isRuleValid(null, true));
		
	}

}
