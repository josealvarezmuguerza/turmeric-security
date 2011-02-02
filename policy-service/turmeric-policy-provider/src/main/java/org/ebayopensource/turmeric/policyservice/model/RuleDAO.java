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


/**
 * @author muguerza
 * 
 */
public interface RuleDAO {

	void persistRule(Rule rule);
	
	void persistCondition(Condition condition);

	void persistExpression(Expression expression);
	
	void persistPrimitiveValue(PrimitiveValue primitiveValue);

	
	Rule findRuleById(long id);
	
	Rule findRuleByName(String name);

	Condition findConditionById(long conditionId);
	
	Expression findExpressionById(long expressionId);

	PrimitiveValue findPrimitiveValueById(long primitiveValueId);
	
	Condition findConditionByRuleId(long ruleId);

	
	Expression findExpressionByConditionId(long conditionId);

	PrimitiveValue findPrimitiveValueByExpressionId(long expressionId);

	
	Expression findExpressionByName(String expressionName);
  
    
	void removeRule(long ruleId);

	void removePrimitiveValue(long primitiveValueId);
 
	boolean isRuleNameUsed(String ruleName);
	
	boolean isRuleValid(Rule rule, boolean allowNull);

	//	void removeCondition(Long ruleId, String conditionId);

//    void audit(RuleKey ruleKey,  SubjectKey loginSubject);

}
