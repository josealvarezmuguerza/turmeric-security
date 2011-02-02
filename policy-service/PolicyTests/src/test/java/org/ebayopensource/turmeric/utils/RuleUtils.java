/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/

package org.ebayopensource.turmeric.utils;

import org.ebayopensource.turmeric.policyservice.model.RuleDAOImpl;
import org.ebayopensource.turmeric.policyservice.provider.utils.RuleHelper;
import org.ebayopensource.turmeric.security.v1.services.*;

/**
 * @author gbaal
 * 
 */
public class RuleUtils {
	private RuleUtils() {
		super();
	}

	public static Rule generateValidRule() {
		Rule rule = new Rule();
		Condition condition = new Condition();
		Expression expression = new Expression();
		condition.setExpression(expression);
		PrimitiveValue primitiveValue = new PrimitiveValue();
		primitiveValue.setType(SupportedPrimitive.STRING);
		primitiveValue.setValue("Test_Service_01:operation.count>1500");
		expression.setPrimitiveValue(primitiveValue);
		rule.setCondition(condition);
		rule.setPriority(0);
		rule.setDescription("Desc ");
		rule.setRuleName("dummytest");
		rule.setEffect(EffectType.BLOCK);
		return rule;
	}

	public static Policy updatePolicyRule(Policy policy) {
		if (policy != null && "RL".equalsIgnoreCase(policy.getPolicyType())) {
			if ((policy.getRule() == null || (policy.getRule().isEmpty()))
					) {
				policy.getRule().add(generateValidRule());
				
			}else if(policy.getRule() != null && !new RuleDAOImpl()
							.isRuleValid(RuleHelper.convert(policy.getRule()
									.get(0)), false)){
				policy.getRule().clear();
				policy.getRule().add(generateValidRule());
			}
		}
		return policy;
	}
}
