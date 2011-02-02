package org.ebayopensource.turmeric.policyservice.provider.utils;

import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.turmeric.policyservice.model.Condition;
import org.ebayopensource.turmeric.policyservice.model.Expression;
import org.ebayopensource.turmeric.policyservice.model.PrimitiveValue;
import org.ebayopensource.turmeric.policyservice.model.Rule;
import org.ebayopensource.turmeric.policyservice.model.SupportedPrimitive;
import org.ebayopensource.turmeric.policyservice.model.EffectType;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePair;

public class RuleHelper {
    private RuleHelper() {}
    
    public static List<org.ebayopensource.turmeric.security.v1.services.Rule> convert(List<Rule> jpaRules){
    	List<org.ebayopensource.turmeric.security.v1.services.Rule>  rules = new ArrayList<org.ebayopensource.turmeric.security.v1.services.Rule>();
    	if(jpaRules!=null){
    		for(Rule jpaRule : jpaRules){
    		    org.ebayopensource.turmeric.security.v1.services.Rule converted =
    		        convert(jpaRule);
		        if (converted != null) {
		            rules.add(converted);
		        }
    		//just one rule can be created from UI
		        break;
    		}
    	}
    	return rules;
    }
   
    public static org.ebayopensource.turmeric.security.v1.services.Rule convert(Rule jpaRule){
    	// for now check all
        if(jpaRule==null || jpaRule.getCondition()==null || jpaRule.getCondition().getExpression()==null || jpaRule.getCondition().getExpression().getPrimitiveValue()==null){
        	return null;
        }
    	org.ebayopensource.turmeric.security.v1.services.Rule rule = new org.ebayopensource.turmeric.security.v1.services.Rule();
    	rule.setEffect(org.ebayopensource.turmeric.security.v1.services.EffectType.
    	                fromValue(jpaRule.getEffect().value()));
    	rule.setEffectDuration(jpaRule.getEffectDuration());
    	rule.setRolloverPeriod(jpaRule.getRolloverPeriod());
    	rule.setRuleId(jpaRule.getId());
    	rule.setPriority(jpaRule.getPriority());
    	rule.setRuleName(rule.getRuleName());
    	KeyValuePair kvpEmails = new KeyValuePair();
    	kvpEmails.setKey("NotifyEmails");
    	kvpEmails.setValue(jpaRule.getNotifyEmails());
    	rule.getAttribute().add(kvpEmails);
    	
    	KeyValuePair kvpActive = new KeyValuePair();
    	kvpActive.setKey("NotifyActive");
    	kvpActive.setValue(Boolean.toString(jpaRule.isNotifyActive()));
    	rule.getAttribute().add(kvpActive);
    	
    	// FIXME WHERE to get version
//	    	rule.setVersion();
        rule.setCondition(convert(jpaRule.getCondition()));
        return rule;
    }
    
    private static org.ebayopensource.turmeric.security.v1.services.Condition convert(Condition jpaCondition){
    	if(jpaCondition==null){
    		return null;
    	}
    	org.ebayopensource.turmeric.security.v1.services.Condition condition = new org.ebayopensource.turmeric.security.v1.services.Condition();
    	condition.setExpression(convert(jpaCondition.getExpression()));
    	return condition;
    }
    
    private static org.ebayopensource.turmeric.security.v1.services.Expression convert(Expression jpaExpression){
    	if(jpaExpression==null){
    		return null;
    	}
    	org.ebayopensource.turmeric.security.v1.services.Expression expression = new org.ebayopensource.turmeric.security.v1.services.Expression();
        expression.setPrimitiveValue(convert(jpaExpression.getPrimitiveValue()));
        expression.setComment(jpaExpression.getComment());
        expression.setName(jpaExpression.getName());
    	return expression;
    }
    
    private static org.ebayopensource.turmeric.security.v1.services.PrimitiveValue convert(PrimitiveValue jpaPrimitiveValue){
    	if( jpaPrimitiveValue== null ){
    		return null;
    	}
    	org.ebayopensource.turmeric.security.v1.services.PrimitiveValue primitiveValue = new org.ebayopensource.turmeric.security.v1.services.PrimitiveValue();
    	primitiveValue.setType(convert(jpaPrimitiveValue.getType()));
    	primitiveValue.setValue(jpaPrimitiveValue.getValue());
    	return primitiveValue;
    }
    
    private static org.ebayopensource.turmeric.security.v1.services.SupportedPrimitive convert( SupportedPrimitive primitive){
    	 org.ebayopensource.turmeric.security.v1.services.SupportedPrimitive supportedPrimitive=null;
    	if(SupportedPrimitive.BOOLEAN.equals(primitive)){
    		supportedPrimitive = org.ebayopensource.turmeric.security.v1.services.SupportedPrimitive.BOOLEAN;
    	} else if(SupportedPrimitive.STRING.equals(primitive)){
    		supportedPrimitive = org.ebayopensource.turmeric.security.v1.services.SupportedPrimitive.STRING;
    	}
    	return supportedPrimitive;
    }

    public static Rule convert(final org.ebayopensource.turmeric.security.v1.services.Rule rule) {
        if(rule==null || rule.getCondition()==null || rule.getCondition().getExpression()==null || rule.getCondition().getExpression().getPrimitiveValue()==null){
            return null;
        }
        
        String notifyEmails = null;
        boolean notifyActive = false;
        if(rule.getAttribute()!= null && rule.getAttribute().size() >0 ){
        	for (int i = 0; i < rule.getAttribute().size(); i++) {
        		org.ebayopensource.turmeric.security.v1.services.KeyValuePair keyValuePair = rule.getAttribute().get(i);
        		if("NotifyEmails".equalsIgnoreCase(keyValuePair.getKey())){
        			notifyEmails = 	keyValuePair.getValue();
        		}
        		if("NotifyActive".equalsIgnoreCase(keyValuePair.getKey())){
        			notifyActive = 	Boolean.parseBoolean(keyValuePair.getValue());
        		}
        	}
        	
		}
        
        return new Rule(rule.getRuleName(), rule.getDescription(), rule.getEffectDuration(), 
                        rule.getRolloverPeriod(), rule.getPriority(), convert(rule.getEffect()),
                        convert(rule.getCondition()), notifyEmails, notifyActive);
    }
    
    public static Condition convert(final org.ebayopensource.turmeric.security.v1.services.Condition condition) {
        org.ebayopensource.turmeric.policyservice.model.Condition conditionResult = null;
        if(null != condition){
            conditionResult = new org.ebayopensource.turmeric.policyservice.model.Condition(
                    convert(condition.getExpression()));
        }
        
        return conditionResult ;
    }
    
    public static Expression convert(final org.ebayopensource.turmeric.security.v1.services.Expression expression) {
        return new org.ebayopensource.turmeric.policyservice.model.Expression(
                convert(expression.getPrimitiveValue()), expression.getComment(), expression.getName());
    }
    
    public static PrimitiveValue convert(final org.ebayopensource.turmeric.security.v1.services.PrimitiveValue pv) {
        
        return new org.ebayopensource.turmeric.policyservice.model.PrimitiveValue(
                pv.getValue(), (pv.getType()==null?SupportedPrimitive.STRING:org.ebayopensource.turmeric.policyservice.model.SupportedPrimitive.fromValue(pv.getType().value())));
    }
    
    public static EffectType convert(final org.ebayopensource.turmeric.security.v1.services.EffectType effectType) {
        return EffectType.valueOf(effectType.name());
    }
}
