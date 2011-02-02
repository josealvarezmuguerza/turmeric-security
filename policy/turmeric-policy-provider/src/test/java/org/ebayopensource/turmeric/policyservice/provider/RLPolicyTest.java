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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.ebayopensource.turmeric.policyservice.provider.common.PolicyEditObject;
import org.ebayopensource.turmeric.policyservice.provider.common.ResourcesEditObject;
import org.ebayopensource.turmeric.policyservice.provider.common.SubjectsEditObject;
import org.ebayopensource.turmeric.security.v1.services.Condition;
import org.ebayopensource.turmeric.security.v1.services.EffectType;
import org.ebayopensource.turmeric.security.v1.services.Expression;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.PrimitiveValue;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Rule;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.security.v1.services.SupportedPrimitive;
import org.ebayopensource.turmeric.utils.jpa.EntityManagerContext;
import org.junit.Test;

public class RLPolicyTest extends PolicyTestBase {
    @Test
    public void createPolicyTest() throws Exception {
        SubjectKey userKey = getUserKey("jdoe");
        USERSubject us = new USERSubject();
        GENERICResource gr = new GENERICResource();
        RLPolicy rlp = new RLPolicy();
        
        Policy policy = new Policy();
        policy.setPolicyName("adminRLl");
        policy.setPolicyType("RL");
        policy.setDescription("admin RL policy");
        
        Rule rule = createRule();
        policy.getRule().add(rule);
        
        PolicyEditObject polEdObj = new PolicyEditObject();
        
        /*
         * SUBJECTS
         */
        SubjectsEditObject subEdObj = new SubjectsEditObject();
        polEdObj.setSubjectsEditObject(subEdObj);
        
        List<Long> addSubjList = subEdObj.getAddSubjectList();
        addSubjList.add(us.getSubjectByName("admin").keySet().toArray(new Long[1])[0]);
        List<Long> addSubjGrpList = subEdObj.getAddSubjectGroupList();
        addSubjGrpList.add(us.getSubjectGroupInfoByName("managers").keySet().toArray(new Long[1])[0]);
        /*
         * RESOURCES
         */
        ResourcesEditObject resEdObj = new ResourcesEditObject();
        polEdObj.setResourcesEditObject(resEdObj);
        
        List<Long> addResList = resEdObj.getAddResourceList();
        addResList.add(gr.getResourceInfoByName("adminsvc").getResourceId());
        List<Long> addOpList = resEdObj.getAddOperationList();
        addOpList.add(gr.getOperationByName("adminsvc", "LOGIN").getOperationId());
        
        PolicyKey policyKey = rlp.createPolicy(policy, polEdObj, userKey);
        Long policyId = policyKey.getPolicyId();
        
        EntityManagerContext.open(factory);
        try {
            org.ebayopensource.turmeric.policyservice.model.Policy savedPolicy =
                EntityManagerContext.get().find(
                    org.ebayopensource.turmeric.policyservice.model.Policy.class, 
                    policyKey.getPolicyId());
            assertNotNull(savedPolicy);
            
            Map<Long, Subject> subjects = rlp.getSubjectAssignmentOfPolicy(policyId, null);
            assertEquals(1, subjects.size());

            Map<Long, SubjectGroup> subjectGroups = rlp.getSubjectGroupAssignmentOfPolicy(policyId, null);
            assertEquals(1, subjectGroups.size());

            Map<Long, Resource> resources = rlp.getResourceAssignmentOfPolicy(policyId, null);
            assertEquals(1, resources.size());

            Map<Long, Operation> operations = rlp.getOperationAssignmentOfPolicy(policyId, null);
            assertEquals(1, operations.size());  
            
            
            Map<Long, Rule> rules = rlp.getRuleAssignmentOfPolicy(policyId, null);
            assertEquals(1, rules.size());  
            
            
        } finally {
            EntityManagerContext.close();
        }
    }
    
 

    private Rule createRule() {
    	Rule rule = new Rule();
    	Condition condition = createCondition();
    	
    	
    	rule.setCondition(condition);
    	rule.setEffect(EffectType.BLOCK);
    	rule.setEffectDuration(3600L);
    	rule.setPriority(2);
    	rule.setRolloverPeriod(1L);
    	rule.setRuleName("RLRule");
    	
    	return rule;
	}

	private Condition createCondition() {
		Condition condition = new Condition();
		Expression expression = new Expression();
		expression.setName("HITS");
		expression.setComment("Service hits");
		PrimitiveValue value = new PrimitiveValue();
		value.setType(SupportedPrimitive.STRING);
		value.setValue("PaymentService:commit.count>5");

		expression.setPrimitiveValue(value);
		condition.setExpression(expression);
		return condition;

	}

	private SubjectKey getUserKey(String name) throws Exception
    {
        USERSubject userProvider = new USERSubject();
        Map<Long, Subject> usrMap = userProvider.getSubjectByName(name);

        SubjectKey userKey = new SubjectKey();
        Subject subject = usrMap.values().toArray(new Subject[1])[0];
        userKey.setSubjectId((Long)usrMap.keySet().toArray(new Long[1])[0]);
        userKey.setSubjectName(subject.getSubjectName());
        userKey.setSubjectType(subject.getSubjectType());
        
        return userKey;        
    }
	@Test
	public void isRuleNameUsedTest() throws Exception{
	       SubjectKey userKey = getUserKey("jdoe");
	        RLPolicy rlp = new RLPolicy();
	        
	        Policy policy = new Policy();
	        policy.setPolicyName("adminRLl");
	        policy.setPolicyType("RL");
	        policy.setDescription("admin RL policy");
	        
	        Rule rule = createRule();
	        
	        policy.getRule().add(rule);
	        
	        PolicyEditObject polEdObj = new PolicyEditObject();

	        assertFalse(rule.getRuleName()+" should not be present in db ",rlp.isRuleNameUsed(rule.getRuleName()));
	        PolicyKey policyKey = rlp.createPolicy(policy, polEdObj, userKey);
	        Long policyId = policyKey.getPolicyId();
	        assertNotNull("create of polocy should pass",policyId);
	        assertTrue(rule.getRuleName()+" should  be present in db ",rlp.isRuleNameUsed(rule.getRuleName()));
		  
	}
	@Test
	public void isRuleRequiredTest() throws Exception{
		RLPolicy rlp = new RLPolicy();
		WHITELISTPolicy policy = new WHITELISTPolicy();
		//FIXME
		assertTrue(rlp.isRuleRequired());
		assertFalse(policy.isRuleRequired());		  
	}
	@Test
	public void isRuleValid() throws Exception{
		RLPolicy rlp = new RLPolicy();
		 Rule rule = createRule();
		 //valid rule
		assertTrue(rlp.isRuleValid(rule));
		rule.getCondition().getExpression().setPrimitiveValue(null);
		// rule should be invalid
		assertFalse(rlp.isRuleValid(rule));
		
		  
	}
	
	
}

