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

import org.ebayopensource.turmeric.utils.jpa.AbstractDAO;

/**
 * @author muguerza
 * 
 */
public class RuleDAOImpl extends AbstractDAO implements RuleDAO {

	@Override
	public void persistRule(Rule rule) {
		persistEntity(rule);
	}

	@Override
	public void persistCondition(Condition condition) {
		persistEntity(condition);
	}

	@Override
	public void persistExpression(Expression expression) {
		persistEntity(expression);
	}

	@Override
	public void persistPrimitiveValue(PrimitiveValue primitiveValue) {
		persistEntity(primitiveValue);
	}

	@Override
	public Rule findRuleById(long id) {
		return findEntity(Rule.class, id);
	}

	@Override
	public Condition findConditionById(long conditionId) {
		return findEntity(Condition.class, conditionId);
	}

	@Override
	public Expression findExpressionById(long expressionId) {
		return findEntity(Expression.class, expressionId);
	}

	@Override
	public PrimitiveValue findPrimitiveValueById(long primitiveValueId) {
		return findEntity(PrimitiveValue.class, primitiveValueId);
	}

	@Override
	public Condition findConditionByRuleId(long ruleId) {
		Rule rule = findRuleById(ruleId);
		if (rule != null && rule.getCondition() != null) {
			return rule.getCondition();
		}
		return null;
	}

	@Override
	public Expression findExpressionByConditionId(long conditionId) {
		Condition condition = findConditionById(conditionId);
		if (condition != null && condition.getExpression() != null) {
			return condition.getExpression();
		}
		return null;
	}

	@Override
	public PrimitiveValue findPrimitiveValueByExpressionId(long expressionId) {
		Expression expression = findExpressionById(expressionId);
		if (expression != null && expression.getPrimitiveValue() != null) {
			return expression.getPrimitiveValue();
		}
		return null;
	}

	@Override
	public Expression findExpressionByName(String expressionName) {
		return getSingleResultOrNull(Expression.class, "name", expressionName);
	}

	@Override
	public void removeRule(long ruleId) {
		removeEntity(Rule.class, ruleId);

	}

	@Override
	public void removePrimitiveValue(long primitiveValueId) {
		removeEntity(PrimitiveValue.class, primitiveValueId);

	}

	@Override
	public Rule findRuleByName(String name) {
		return getSingleResultOrNull(Rule.class, "ruleName", name);
	}

	@Override
	public boolean isRuleNameUsed(String ruleName) {
		return findRuleByName(ruleName) != null;
	}



	@Override
	public boolean isRuleValid(Rule rule,boolean allowNull) {
		// value should not be null as well as the type
		// rulename required
		boolean valid = false;
		if(rule != null
				&& rule.getCondition() != null
				&& rule.getCondition().getExpression() != null
				&& rule.getCondition().getExpression().getPrimitiveValue() != null
				&& rule.getCondition().getExpression().getPrimitiveValue() != null
				&& rule.getCondition().getExpression().getPrimitiveValue()
						.getValue() != null
				&& rule.getCondition().getExpression().getPrimitiveValue()
						.getValue().length() > 0
				&& rule.getCondition().getExpression().getPrimitiveValue()
						.getType() != null 
				&& (rule.getRuleName() != null || rule.getRuleName().length() != 0)
				){
			valid = isValidCondition(rule.getCondition().getExpression().getPrimitiveValue().getValue());
		   	
		}
			
		return valid;
	}
	private boolean isValidCondition(String value) {
		boolean flag =false;
		if( value!=null ){
			//FIXME should not be Hardcoded retrieve me from some where
			//if((value.contains(".hits") || value.contains(".count") || value.contains("HITS"))){
				String[] expression = {">","<" ,"==" ,"=>",">=","<=","=<"};
				String[] words;
				for(String val:expression){
						if(value ==null){
							break;
						}
						if (value.contains(val)) {
							words = value.split(val);
							if(words[1]!=null){
								words[1] = words[1].trim();
							}
							if(words[0]!=null){
								words[0] = words[0].trim();
							}
							try{
								Integer.valueOf(words[0]);
								flag = true;
							}catch (NumberFormatException e) {
								try{
									
									 Integer.valueOf(words[1].trim());
									 flag = true;
								}catch (NumberFormatException e1) {
//									e1.printStackTrace();
								}
							}
						}
				}
//			}
		}
		return flag;
	}
}
