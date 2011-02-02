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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManagerFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ebayopensource.turmeric.policyservice.exceptions.PolicyCreationException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyDeleteException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyFinderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyUpdateException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyValidationException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.Category;
import org.ebayopensource.turmeric.policyservice.model.AuditHistory;
import org.ebayopensource.turmeric.policyservice.model.EffectType;
import org.ebayopensource.turmeric.policyservice.model.PolicyDAO;
import org.ebayopensource.turmeric.policyservice.model.PolicyDAOImpl;
import org.ebayopensource.turmeric.policyservice.model.ResourceDAOImpl;
import org.ebayopensource.turmeric.policyservice.model.RuleDAO;
import org.ebayopensource.turmeric.policyservice.model.RuleDAOImpl;
import org.ebayopensource.turmeric.policyservice.model.SubjectDAOImpl;
import org.ebayopensource.turmeric.policyservice.model.SupportedPrimitive;
import org.ebayopensource.turmeric.policyservice.provider.common.PolicyBuilderObject;
import org.ebayopensource.turmeric.policyservice.provider.common.PolicyEditObject;
import org.ebayopensource.turmeric.policyservice.provider.utils.RuleHelper;
import org.ebayopensource.turmeric.security.v1.services.EntityHistory;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePair;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.PrimitiveValue;
import org.ebayopensource.turmeric.security.v1.services.Query;
import org.ebayopensource.turmeric.security.v1.services.QueryCondition;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Rule;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectTypeInfo;
import org.ebayopensource.turmeric.utils.jpa.JPAAroundAdvice;
import org.ebayopensource.turmeric.utils.jpa.PersistenceContext;
import org.ebayopensource.turmeric.utils.jpa.model.AuditContext;


public abstract class ListPolicyBase  implements PolicyTypeProvider {
    private final EntityManagerFactory factory;
    private final PolicyTypeProvider impl;

    protected abstract String getPolicyType();
    
    protected abstract String getAction();
    
    public ListPolicyBase() {
        factory = PersistenceContext.createEntityManagerFactory("policyservice");
        
        ClassLoader classLoader = PolicyTypeProvider.class.getClassLoader();
        Class[] interfaces = {PolicyTypeProvider.class};
        PolicyTypeProvider target = new ListPolicyBaseImpl(getPolicyType());
        impl = (PolicyTypeProvider) Proxy.newProxyInstance(classLoader, interfaces, new JPAAroundAdvice(factory, target));
    }
    
    private class ListPolicyBaseImpl extends PolicyBase {
        protected final PolicyDAO policyDAO;
        protected final String policyType;
        protected final RuleDAO ruleDAO;
        
        private ListPolicyBaseImpl(String policyType) {
            this.policyType = policyType;
            this.policyDAO = new PolicyDAOImpl();
            this.ruleDAO = new RuleDAOImpl();
        }
    
        @Override
        protected PolicyKey createPolicyInfo(Policy policy, SubjectKey createdBy)
                throws PolicyCreationException {
        	if (createdBy != null) {
        		AuditContext.setUser(createdBy.getSubjectName());
        	}
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    PolicyDAOImpl.convert(policy);
                policyDAO.persistPolicy(jpaPolicy);
                
                PolicyKey policyKey = new PolicyKey();
                policyKey.setPolicyId(jpaPolicy.getId());
                policyKey.setPolicyName(jpaPolicy.getPolicyName());
                policyKey.setPolicyType(jpaPolicy.getPolicyType());
                return policyKey;
            }
            catch (Exception ex) {
                throw new PolicyCreationException(Category.POLICY, policyType, null,
                                policy.getPolicyName(), "Failed to create policy", ex);
            }
            finally {
                AuditContext.clear();
            }
        }
    
        @Override
        protected PolicyKey updatePolicyInfo(Policy policy, SubjectKey modifiedBy)
                throws PolicyUpdateException {
        	if (modifiedBy != null) {
        		AuditContext.setUser(modifiedBy.getSubjectName());
        	}
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policy.getPolicyId());
    
                if (policy.getPolicyName() != null) {
                    jpaPolicy.setPolicyName(policy.getPolicyName());
                }
                if (policy.getPolicyType() != null) {
                    jpaPolicy.setPolicyType(policy.getPolicyType());
                }
                if (policy.getDescription() != null) {
                    jpaPolicy.setDescription(policy.getDescription());
                }
                if (policy.isActive() != null) {
                    jpaPolicy.setActive(policy.isActive());
                }
                
                //Rule updates
                if(policy.getRule() != null && !policy.getRule().isEmpty()){
                	Rule rule = policy.getRule().get(0);
                	if(jpaPolicy.getRules() != null && !jpaPolicy.getRules().isEmpty()){
                		org.ebayopensource.turmeric.policyservice.model.Rule jpaRule = jpaPolicy.getRules().get(0);
                		if(rule.getRuleName() != null){
                			jpaRule.setRuleName(rule.getRuleName());	
                		}
                		if(rule.getDescription()!=null){
                			jpaRule.setDescription(rule.getDescription());
                		}                		
                		if(rule.getEffect() != null){
                			jpaRule.setEffect(EffectType.fromValue(rule.getEffect().value()));
                		}
                		
                		if(rule.getEffectDuration()!=null){
                			jpaRule.setEffectDuration(rule.getEffectDuration());	
                		}
                		if(rule.getPriority() != null){
                			jpaRule.setPriority(rule.getPriority());	
                		}
                		if(rule.getRolloverPeriod()!= null){
                			jpaRule.setRolloverPeriod(rule.getRolloverPeriod());
                		}
                		
                		
                		if(rule.getAttribute()!= null && rule.getAttribute().size()>0){
            				for (KeyValuePair kvp: rule.getAttribute()) {
            					if("NotifyEmails".equals(kvp.getKey())){
            						// Policy Based Email Address
            						jpaRule.setNotifyEmails(kvp.getValue());
            					}else if("NotifyActive".equals(kvp.getKey())){
            						// Policy Based Email Address
            						jpaRule.setNotifyActive(Boolean.parseBoolean(kvp.getValue()));
            					}
            				}
            			}
                		
                		
                		/* 
                		 * condition's rule
                		 */
                		org.ebayopensource.turmeric.policyservice.model.Condition jpaCondition = jpaRule.getCondition();
                		org.ebayopensource.turmeric.policyservice.model.Expression jpaExpression = jpaCondition.getExpression();
                		org.ebayopensource.turmeric.policyservice.model.PrimitiveValue jpaPrimitiveValue = jpaExpression.getPrimitiveValue();

                		PrimitiveValue primitiveValue = rule.getCondition().getExpression().getPrimitiveValue();
                		jpaPrimitiveValue.setValue(primitiveValue.getValue());
                		if(primitiveValue.getType() != null){
                			jpaPrimitiveValue.setType(SupportedPrimitive.fromValue(primitiveValue.getType().value()));
                		}else{
                			jpaPrimitiveValue.setType(SupportedPrimitive.STRING);
                		}
                		jpaExpression.setPrimitiveValue(jpaPrimitiveValue);
                		jpaCondition.setExpression(jpaExpression);
                		jpaRule.setCondition(jpaCondition);
                	}
                }
                
                
                PolicyKey policyKey = new PolicyKey();
                policyKey.setPolicyId(jpaPolicy.getId());
                policyKey.setPolicyName(jpaPolicy.getPolicyName());
                policyKey.setPolicyType(jpaPolicy.getPolicyType());
                return policyKey;
            }
            catch (Exception ex) {
                throw new PolicyUpdateException(Category.POLICY, policyType,
                                policy.getPolicyId(), policy.getPolicyName(),
                                "Failed to update policy", ex);
            }
            finally {
                AuditContext.clear();
            }
        }
    
        @Override
        public void deletePolicy(Long policyId) throws PolicyDeleteException, PolicyUpdateException {
           policyDAO.removePolicy(policyId); 
        }
    
        @Override
        public Policy getPolicyInfo(Long policyId) throws PolicyFinderException {
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                return PolicyDAOImpl.convert(jpaPolicy);
            }
            catch (Exception ex) {
                throw new PolicyFinderException(Category.POLICY, policyType,
                                policyId, null, "Failed to retrieve policy", ex);
            }
        }
    
        @Override
        public Policy getPolicyInfo(String policyName) throws PolicyFinderException {
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyByName(policyName);
                return PolicyDAOImpl.convert(jpaPolicy);
            }
            catch (Exception ex) {
                throw new PolicyFinderException(Category.POLICY, policyType,
                                null, policyName, "Failed to retrieve policy", ex);
            }
        }
    
        @Override
        protected void addOperationAssignmentOfPolicy(Long policyId, List<Long> operations)
                throws PolicyUpdateException {
            long id = -1;
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.Operation> jpaOperations = 
                    jpaPolicy.getOperations();
                for (Long operationId : operations) {
                    id = operationId;
                    org.ebayopensource.turmeric.policyservice.model.Operation jpaOperation =
                        policyDAO.findOperationById(operationId);
                    jpaOperations.add(jpaOperation);
                }
            }
            catch (Exception ex) {
                throw new PolicyUpdateException((id < 0 ? Category.POLICY : Category.OPERATION),
                                policyType, (id < 0 ? policyId : id),
                                "Failed to assign operation to policy", ex);
            }
        }
    
        @Override
        protected void removeOperationAssignmentOfPolicy(Long policyId, List<Long> operations)
                throws PolicyUpdateException {
            long id = -1;
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.Operation> jpaOperations = 
                    jpaPolicy.getOperations();
                for (Long operationId : operations) {
                    id = operationId;
                    org.ebayopensource.turmeric.policyservice.model.Operation jpaOperation =
                        policyDAO.findOperationById(operationId);
                    jpaOperations.remove(jpaOperation);
                }
            }
            catch (Exception ex) {
                throw new PolicyUpdateException((id < 0 ? Category.POLICY : Category.OPERATION),
                                policyType, (id < 0 ? policyId : id),
                                "Failed to unassign operation from policy", ex);
            }
        }
    
        @Override
        public Map<Long, Operation> getOperationAssignmentOfPolicy(Long policyId,
                QueryCondition queryCondition) throws PolicyFinderException {
            long id = -1;
            Map<Long, Operation> result = new HashMap<Long, Operation>();
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.Operation> jpaOperations = 
                    jpaPolicy.getOperations();
                for (org.ebayopensource.turmeric.policyservice.model.Operation jpaOperation : jpaOperations) {
                    id = jpaOperation.getId();
                    result.put(Long.valueOf(id), ResourceDAOImpl.convert(jpaOperation));
                    id = -1;
                }
            }
            catch (Exception ex) {
                throw new PolicyFinderException((id < 0 ? Category.POLICY : Category.OPERATION),
                                policyType, (id < 0 ? policyId : id),
                                "Failed to retrieve operation assignment for policy", ex);
           }
            return result;
        }
    
        @Override
        protected void addResourceAssignmentOfPolicy(Long policyId, List<Long> resources) 
                throws PolicyUpdateException {
            long id = -1;
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.Resource> jpaResources = 
                    jpaPolicy.getResources();
                for (Long resourceId : resources) {
                    id = resourceId;
                    org.ebayopensource.turmeric.policyservice.model.Resource jpaResource =
                        policyDAO.findResourceById(resourceId);
                    jpaResources.add(jpaResource);
                }
            }
            catch (Exception ex) {
                throw new PolicyUpdateException((id<0 ? Category.POLICY:Category.RESOURCE), policyType,
                                (id<0 ? policyId:id), "Failed to assign resource to policy", ex);
            }
        }
    
        @Override
        protected void removeResourceAssignmentOfPolicy(Long policyId, List<Long> resources)
                throws PolicyUpdateException {
            long id = -1;
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.Resource> jpaResources = 
                    jpaPolicy.getResources();
                for (Long resourceId : resources) {
                    id = resourceId;
                    org.ebayopensource.turmeric.policyservice.model.Resource jpaResource =
                        policyDAO.findResourceById(resourceId);
                    jpaResources.remove(jpaResource);
                }
            }
            catch (Exception ex) {
                throw new PolicyUpdateException((id < 0 ? Category.POLICY : Category.RESOURCE),
                                policyType, (id < 0 ? policyId : id),
                                "Failed to unassign resource from policy", ex);
            }
        }
    
        @Override
        public Map<Long, Resource> getResourceAssignmentOfPolicy(Long policyId,
                QueryCondition queryCondition) throws PolicyFinderException {
            long id = -1;
            Map<Long, Resource> result = new HashMap<Long, Resource>();
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.Resource> jpaResources = 
                    jpaPolicy.getResources();
                for (org.ebayopensource.turmeric.policyservice.model.Resource jpaResource : jpaResources) {
                    id = jpaResource.getId();
                    result.put(Long.valueOf(id), ResourceDAOImpl.convert(jpaResource));
                    id = -1;
                }
            }
            catch (Exception ex) {
                throw new PolicyFinderException((id < 0 ? Category.POLICY : Category.RESOURCE),
                                policyType, (id < 0 ? policyId : id),
                                "Failed retrieve resource assignment for policy", ex);
            }
            return result;
        }
    
        @Override
        protected void addSubjectAssignmentOfPolicy(Long policyId, List<Long> subjects)
                throws PolicyUpdateException {
            long id = -1;
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.Subject> jpaSubjects = 
                    jpaPolicy.getSubjects();
                for (Long subjectId : subjects) {
                    id = subjectId;
                    org.ebayopensource.turmeric.policyservice.model.Subject jpaSubject =
                        policyDAO.findSubjectById(subjectId);
                    jpaSubjects.add(jpaSubject);
                }
            }
            catch (Exception ex) {
                throw new PolicyUpdateException((id<0 ? Category.POLICY:Category.SUBJECT), policyType,
                                (id<0 ? policyId:id), "Failed to assign subject to policy", ex);
            }
        }
    
        @Override
        protected void removeSubjectAssignmentOfPolicy(Long policyId, List<Long> subjects)
                throws PolicyUpdateException {
            long id = -1;
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.Subject> jpaSubjects = 
                    jpaPolicy.getSubjects();
                for (Long subjectId : subjects) {
                    id = subjectId;
                    org.ebayopensource.turmeric.policyservice.model.Subject jpaSubject =
                        policyDAO.findSubjectById(subjectId);
                    jpaSubjects.remove(jpaSubject);
                }
            }
            catch (Exception ex) {
                throw new PolicyUpdateException((id<0 ? Category.POLICY:Category.SUBJECT), policyType,
                                (id<0 ? policyId:id), "Failed to unassign subject from policy", ex);
            }
        }
    
        @Override
        public Map<Long, Subject> getSubjectAssignmentOfPolicy(Long policyId,
                QueryCondition queryCondition) throws PolicyFinderException {
            long id = -1;
            Map<Long, Subject> result = new HashMap<Long, Subject>();
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.Subject> jpaSubjects = 
                    jpaPolicy.getSubjects();
                for (org.ebayopensource.turmeric.policyservice.model.Subject jpaSubject : jpaSubjects) {
                    id = jpaSubject.getId();
                    result.put(Long.valueOf(id), SubjectDAOImpl.convert(jpaSubject));
                    id = -1;
                }
            }
            catch (Exception ex) {
                throw new PolicyFinderException((id<0 ? Category.POLICY:Category.SUBJECT), policyType,
                                (id<0 ? policyId:id), "Failed to retrieve subject assignment of policy", ex);
            }
            return result;
        }
    
        @Override
        protected void addSubjectGroupAssignmentOfPolicy(Long policyId, List<Long> subjectGroups)
                throws PolicyUpdateException {
            long id = -1;
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.SubjectGroup> jpaSubjectGroups = 
                    jpaPolicy.getSubjectGroups();
                for (Long subjectGroupId : subjectGroups) {
                    id = subjectGroupId;
                    org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup =
                        policyDAO.findSubjectGroupById(subjectGroupId);
                    jpaSubjectGroups.add(jpaSubjectGroup);
                }
            }
            catch (Exception ex) {
                throw new PolicyUpdateException((id<0 ? Category.POLICY:Category.SUBJECTGROUP), policyType,
                                (id<0 ? policyId:id), "Failed to assign subject group to policy", ex);
            }
        }
    
        @Override
        protected void removeSubjectGroupAssignmentOfPolicy(Long policyId, List<Long> subjectGroups)
                throws PolicyUpdateException {
            long id = -1;
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.SubjectGroup> jpaSubjectGroups = 
                    jpaPolicy.getSubjectGroups();
                for (Long subjectGroupId : subjectGroups) {
                    id = subjectGroupId;
                    org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup =
                        policyDAO.findSubjectGroupById(subjectGroupId);
                    jpaSubjectGroups.remove(jpaSubjectGroup);
                }
            }
            catch (Exception ex) {
                throw new PolicyUpdateException((id<0 ? Category.POLICY:Category.SUBJECTGROUP), policyType,
                                (id<0 ? policyId:id), "Failed to unassign subject group from policy", ex);
            }
        }
    
    	@Override
    	public Map<Long, SubjectGroup> getSubjectGroupAssignmentOfPolicy(Long policyId, QueryCondition queryCondition)
    			throws PolicyFinderException {
            long id = -1;
            Map<Long, SubjectGroup> result = new HashMap<Long, SubjectGroup>();
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.SubjectGroup> jpaSubjectGroups = 
                    jpaPolicy.getSubjectGroups();
                for (org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup : jpaSubjectGroups) {
                    id = jpaSubjectGroup.getId();
                    result.put(Long.valueOf(id), SubjectDAOImpl.convert(jpaSubjectGroup));
                    id = -1;
                }
            }
            catch (Exception ex) {
                throw new PolicyFinderException((id<0 ? Category.POLICY:Category.SUBJECTGROUP), policyType,
                                (id<0 ? policyId:id), "Failed to retrieve subject group assignment for policy", ex);
            }
            return result;
    	}
        
        @Override
        protected void addExclusionSubjectAssignmentOfPolicy(Long policyId, 
                List<Long> subjects) throws PolicyUpdateException {
            long id = -1;
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.Subject> jpaSubjects = 
                    jpaPolicy.getExclusionSubjects();
                for (Long subjectId : subjects) {
                    id = subjectId;
                    org.ebayopensource.turmeric.policyservice.model.Subject jpaSubject =
                        policyDAO.findSubjectById(subjectId);
                    jpaSubjects.add(jpaSubject);
                }
            }
            catch (Exception ex) {
                throw new PolicyUpdateException((id<0 ? Category.POLICY:Category.SUBJECT), policyType,
                                (id<0 ? policyId:id), "Failed to assign subject to policy", ex);
            }
        }
    
        @Override
        protected void removeExclusionSubjectAssignmentOfPolicy(Long policyId, 
                List<Long> subjects) throws PolicyUpdateException {
            long id = -1;
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.Subject> jpaSubjects = 
                    jpaPolicy.getExclusionSubjects();
                for (Long subjectId : subjects) {
                    id = subjectId;
                    org.ebayopensource.turmeric.policyservice.model.Subject jpaSubject =
                        policyDAO.findSubjectById(subjectId);
                    jpaSubjects.remove(jpaSubject);
                }
            }
            catch (Exception ex) {
                throw new PolicyUpdateException((id<0 ? Category.POLICY:Category.SUBJECT), policyType,
                                (id<0 ? policyId:id), "Failed to unassign subject from policy", ex);
            }
       }
        
        @Override
        public Map<Long, Subject> getExclusionSubjectAssignmentOfPolicy(
                Long policyId, QueryCondition queryCondition)
                throws PolicyFinderException {
            long id = -1;
            Map<Long, Subject> result = new HashMap<Long, Subject>();
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.Subject> jpaSubjects = 
                    jpaPolicy.getExclusionSubjects();
                for (org.ebayopensource.turmeric.policyservice.model.Subject jpaSubject : jpaSubjects) {
                    id = jpaSubject.getId();
                    result.put(Long.valueOf(id), SubjectDAOImpl.convert(jpaSubject));
                    id = -1;
                }
            }
            catch (Exception ex) {
                throw new PolicyFinderException((id<0 ? Category.POLICY:Category.SUBJECT), policyType,
                                (id<0 ? policyId:id), "Failed to retrieve subject assignment of policy", ex);
            }
            return result;
        }
    
        @Override
        protected void addExclusionSubjectGroupAssignmentOfPolicy(Long policyId,
                List<Long> subjectGroups) throws PolicyUpdateException {
            long id = -1;
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.SubjectGroup> jpaSubjectGroups = 
                    jpaPolicy.getExclusionSubjectGroups();
                for (Long subjectGroupId : subjectGroups) {
                    id = subjectGroupId;
                    org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup =
                        policyDAO.findSubjectGroupById(subjectGroupId);
                    jpaSubjectGroups.add(jpaSubjectGroup);
                }
            }
            catch (Exception ex) {
                throw new PolicyUpdateException((id<0 ? Category.POLICY:Category.SUBJECTGROUP), policyType,
                                (id<0 ? policyId:id), "Failed to assign subject group to policy", ex);
            }
        }
    
        @Override
        protected void removeExclusionSubjectGroupAssignmentOfPolicy(Long policyId,
                List<Long> subjectGroups) throws PolicyUpdateException {
            long id = -1;
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.SubjectGroup> jpaSubjectGroups = 
                    jpaPolicy.getExclusionSubjectGroups();
                for (Long subjectGroupId : subjectGroups) {
                    id = subjectGroupId;
                    org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup =
                        policyDAO.findSubjectGroupById(subjectGroupId);
                    jpaSubjectGroups.remove(jpaSubjectGroup);
                }
            }
            catch (Exception ex) {
                throw new PolicyUpdateException((id<0 ? Category.POLICY:Category.SUBJECTGROUP), policyType,
                                (id<0 ? policyId:id), "Failed to unassign subject group from policy", ex);
            }
        }
    
        @Override
        public Map<Long, SubjectGroup> getExclusionSubjectGroupAssignmentOfPolicy(
                Long policyId, QueryCondition queryCondition)
                throws PolicyFinderException {
            long id = -1;
            Map<Long, SubjectGroup> result = new HashMap<Long, SubjectGroup>();
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.SubjectGroup> jpaSubjectGroups = 
                    jpaPolicy.getExclusionSubjectGroups();
                for (org.ebayopensource.turmeric.policyservice.model.SubjectGroup jpaSubjectGroup : jpaSubjectGroups) {
                    id = jpaSubjectGroup.getId();
                    result.put(Long.valueOf(id), SubjectDAOImpl.convert(jpaSubjectGroup));
                    id = -1;
                }
            }
            catch (Exception ex) {
                throw new PolicyFinderException((id<0 ? Category.POLICY:Category.SUBJECTGROUP), policyType,
                                (id<0 ? policyId:id), "Failed to retrieve subject group assignment for policy", ex);
            }
            return result;
        }
    
        @Override
        public Map<Long, Rule> getRuleAssignmentOfPolicy(Long policyId, QueryCondition queryCondition)
                        throws PolicyFinderException {
            long id = -1;
            Map<Long, Rule> result = new HashMap<Long, Rule>();
            try {
                org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy = 
                    policyDAO.findPolicyById(policyId);
                List<org.ebayopensource.turmeric.policyservice.model.Rule> jpaRules = 
                    jpaPolicy.getRules();
                for (org.ebayopensource.turmeric.policyservice.model.Rule jpaRule : jpaRules) {
                    id = jpaRule.getId();
                    Rule converted = RuleHelper.convert(jpaRule);
                    if (converted != null) {
                        result.put(Long.valueOf(id), converted);
                    }
                    id = -1;
                }
            }
                catch (Exception ex) {
                    throw new PolicyFinderException((id < 0 ? Category.POLICY : Category.RULE),
                                    policyType, (id < 0 ? policyId : id),
                                    "Failed retrieve rule assignment for policy", ex);
                }
                return result;
            
           
        }
    
        @Override
    	public Map<Long, Policy> findPolicyInfoBySubject(Set<Long> subjectId,
    			QueryCondition queryCondition) throws PolicyFinderException {
            long sid = -1, pid = -1;
            Map<Long, Policy> result = new HashMap<Long, Policy>();
            try {
                for (Long subjId : subjectId) {
                    sid = subjId;
                    List<org.ebayopensource.turmeric.policyservice.model.Policy> jpaPolicies = 
                        policyDAO.findPolicyBySubjectId(subjId, policyType);
                    for (org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy : jpaPolicies) {
                        pid = jpaPolicy.getId();
                        result.put(Long.valueOf(pid), PolicyDAOImpl.convert(jpaPolicy));
                        pid = -1;
                    }
                }
            }
            catch (Exception ex) {
                throw new PolicyFinderException((pid < 0 ? Category.SUBJECT:Category.POLICY), policyType,
                                (pid < 0 ? sid:pid), "Failed to retrieve subject group assignment for policy", ex);
            }
            return result;
    	}
    
    	@Override
    	public Map<Long, Policy> findPolicyInfoByExclusionSubject(
    			Set<Long> subjectId, QueryCondition queryCondition)
    			throws PolicyFinderException {
            long sid = -1, pid = -1;
            Map<Long, Policy> result = new HashMap<Long, Policy>();
            try {
                for (Long subjId : subjectId) {
                    sid = subjId;
                    List<org.ebayopensource.turmeric.policyservice.model.Policy> jpaPolicies = 
                        policyDAO.findPolicyByExclusionSubjectId(subjId, policyType);
                    for (org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy : jpaPolicies) {
                        pid = jpaPolicy.getId();
                        result.put(Long.valueOf(pid), PolicyDAOImpl.convert(jpaPolicy));
                        pid = -1;
                    }
                }
            }
            catch (Exception ex) {
                throw new PolicyFinderException((pid < 0 ? Category.SUBJECT:Category.POLICY), policyType,
                                (pid < 0 ? sid:pid), "Failed to retrieve subject group assignment for policy", ex);
            }
            return result;
    	}
    
    	@Override
    	public Map<Long, Policy> findPolicyInfoBySubjectGroup(Set<Long> subjectGroupId, 
                QueryCondition queryCondition) throws PolicyFinderException {
            long sid = -1, pid = -1;
            Map<Long, Policy> result = new HashMap<Long, Policy>();
            try {
                for (Long subjGrpId : subjectGroupId) {
                    sid = subjGrpId;
                    List<org.ebayopensource.turmeric.policyservice.model.Policy> jpaPolicies = 
                        policyDAO.findPolicyBySubjectGroupId(subjGrpId, policyType);
                    for (org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy : jpaPolicies) {
                        pid = jpaPolicy.getId();
                        result.put(Long.valueOf(pid), PolicyDAOImpl.convert(jpaPolicy));
                        pid = -1;
                    }
                }
            }
            catch (Exception ex) {
                throw new PolicyFinderException((pid < 0 ? Category.SUBJECTGROUP:Category.POLICY), policyType,
                                (pid < 0 ? sid:pid), "Failed to retrieve subject group assignment for policy", ex);
            }
            return result;
    	}
    
    	@Override
    	public Map<Long, Policy> findPolicyInfoByExclusionSubjectGroup(
    			Set<Long> subjectGroupId, QueryCondition queryCondition)
    			throws org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException {
            long sid = -1, pid = -1;
            Map<Long, Policy> result = new HashMap<Long, Policy>();
            try {
                for (Long subjGrpId : subjectGroupId) {
                    sid = subjGrpId;
                    List<org.ebayopensource.turmeric.policyservice.model.Policy> jpaPolicies = 
                        policyDAO.findPolicyByExclusionSubjectGroupId(subjGrpId, policyType);
                    for (org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy : jpaPolicies) {
                        pid = jpaPolicy.getId();
                        result.put(Long.valueOf(pid), PolicyDAOImpl.convert(jpaPolicy));
                        pid = -1;
                    }
                }
            }
            catch (Exception ex) {
                throw new PolicyFinderException((pid < 0 ? Category.SUBJECTGROUP:Category.POLICY), policyType,
                                (pid < 0 ? sid:pid), "Failed to retrieve subject group assignment for policy", ex);
            }
            return result;
    	}
    
    	@Override
    	public Map<Long, Policy> findPolicyInfoBySubjectType(
    			Set<String> subjectType, QueryCondition queryCondition)
    			throws PolicyFinderException {
    		// N/A (check with pzhao)
    		return null;
    	}
    
    	@Override
    	public Map<Long, Policy> findPolicyInfoByOperation(Set<Long> operationId,
    			QueryCondition queryCondition) throws PolicyFinderException {
            long oid = -1, pid = -1;
            Map<Long, Policy> result = new HashMap<Long, Policy>();
            try {
                for (Long opId : operationId) {
                    oid = opId;
                    List<org.ebayopensource.turmeric.policyservice.model.Policy> jpaPolicies = 
                        policyDAO.findPolicyByOperationId(opId, policyType);
                    for (org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy : jpaPolicies) {
                        pid = jpaPolicy.getId();
                        result.put(Long.valueOf(pid), PolicyDAOImpl.convert(jpaPolicy));
                        pid = -1;
                    }
                }
            }
            catch (Exception ex) {
                throw new PolicyFinderException((pid < 0 ? Category.OPERATION:Category.POLICY), policyType,
                                (pid < 0 ? oid:pid), "Failed to retrieve subject group assignment for policy", ex);
            }
            return result;
    	}
    
    	@Override
    	public Map<Long, Policy> findPolicyInfoByResource(Set<Long> resourceId,
    			QueryCondition queryCondition) throws PolicyFinderException {
            long rid = -1, pid = -1;
            Map<Long, Policy> result = new HashMap<Long, Policy>();
            try {
                for (Long resId : resourceId) {
                    rid = resId;
                    List<org.ebayopensource.turmeric.policyservice.model.Policy> jpaPolicies = 
                        policyDAO.findPolicyByResourceId(resId, policyType);
                    for (org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy : jpaPolicies) {
                        pid = jpaPolicy.getId();
                        result.put(Long.valueOf(pid), PolicyDAOImpl.convert(jpaPolicy));
                        pid = -1;
                    }
                }
            }
            catch (Exception ex) {
                throw new PolicyFinderException((pid < 0 ? Category.RESOURCE:Category.POLICY), policyType,
                                (pid < 0 ? rid:pid), "Failed to retrieve subject group assignment for policy", ex);
            }
            return result;
    	}
    
    	@Override
    	public Map<Long, Policy> findPolicyInfo(PolicyKey policyKey, String effect,
    			QueryCondition queryCondition) throws PolicyFinderException {
            Map<Long, Policy> policies = new HashMap<Long, Policy>();

            if (effect == null) {
                effect = getAction();
            }

            Long id = policyKey.getPolicyId();
            String name = policyKey.getPolicyName();
            if (id != null) {
                policies.put(id, getPolicyInfo(id));
            } else if (name != null) {
                try {
                    List<org.ebayopensource.turmeric.policyservice.model.Policy> jpaPolicies =
                        policyDAO.findAllByName(name, policyType);
                    for (org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy : jpaPolicies) {
                        Policy policy = PolicyDAOImpl.convert(jpaPolicy);
                        policies.put(policy.getPolicyId(), policy);
                    }
                } catch (Exception ex) {
                    throw new PolicyFinderException(Category.POLICY, getPolicyType(),
                                    null, name, "Failed to find policies", ex);
                }
            } else  if (getAction().equalsIgnoreCase(effect)) {
                try {
                    List<org.ebayopensource.turmeric.policyservice.model.Policy> jpaPolicies =
                        policyDAO.findAllByType(policyType);
                    for (org.ebayopensource.turmeric.policyservice.model.Policy jpaPolicy : jpaPolicies) {
                        Policy policy = PolicyDAOImpl.convert(jpaPolicy);
                        policies.put(policy.getPolicyId(), policy);
                    }
                } catch (Exception ex) {
                    throw new PolicyFinderException(Category.POLICY, getPolicyType(),
                            "Failed to find policies for effect [" + effect + "]", ex);
                }
            } else {
                throw new PolicyFinderException(Category.POLICY, getPolicyType(),
                        "Failed to find policies for effect [" + effect + "]", null);

            }

            return policies;
    	}
    	
        @Override
        public List<EntityHistory> getAuditHistory(PolicyKey policyKey,
                        XMLGregorianCalendar startDate, XMLGregorianCalendar endDate)
                        throws PolicyFinderException {
            
            String policyType = policyKey.getPolicyType();
            if (policyType != null) {
                return getAuditHistoryByPolicyType(startDate, endDate, policyType);
        	}
        	
            return getAuditHistoryByIdOrName(policyKey, startDate, endDate);
        }

		private List<EntityHistory> getAuditHistoryByIdOrName(
				PolicyKey policyKey, XMLGregorianCalendar startDate,
				XMLGregorianCalendar endDate) {
			Long policyId = policyKey.getPolicyId();
            String policyName = policyKey.getPolicyName();
            if (policyId == null) {
            	if (policyName != null) {
	                org.ebayopensource.turmeric.policyservice.model.Policy policy = 
	                    policyDAO.findPolicyByName(policyKey.getPolicyName());
	                if (policy != null) {
	                	policyId = policy.getId();
	                }
                }
            }
            
			List<EntityHistory> entityHistory = new ArrayList<EntityHistory>();
            if (policyId != null) {
                List<AuditHistory> auditHistory = policyDAO.getPolicyHistory(
                                policyId.longValue(), 
                                startDate.toGregorianCalendar().getTime(), 
                                endDate.toGregorianCalendar().getTime());
                
                for (AuditHistory entry : auditHistory) {
                    entityHistory.add(AuditHistory.convert(entry));
                }
            }
            
            return entityHistory;
		}

		private List<EntityHistory> getAuditHistoryByPolicyType(
				XMLGregorianCalendar startDate, XMLGregorianCalendar endDate, String policyType) {
			List<EntityHistory> entityHistory = new ArrayList<EntityHistory>();
			List<org.ebayopensource.turmeric.policyservice.model.Policy> allPolicies = policyDAO.findAllByType(policyType);
			
			for (org.ebayopensource.turmeric.policyservice.model.Policy policy : allPolicies) {
				
				List<AuditHistory> auditHistory = policyDAO.getPolicyHistory(
						policy.getId().longValue(), 
			            startDate.toGregorianCalendar().getTime(), 
			            endDate.toGregorianCalendar().getTime());            		
			    for (AuditHistory entry : auditHistory) {
			        entityHistory.add(AuditHistory.convert(entry));
			    }
			}
			
			return entityHistory;
		}
        
        @Override
        public void audit(PolicyKey policyKey, String operationType, SubjectKey loginSubject)
                        throws PolicyFinderException {
            policyDAO.audit(policyKey, operationType, loginSubject);
        }

        @Override
        protected void addSubjectTypeAssignmentOfPolicy(Long policyId,
                        List<Long> addSubjectTypeList) {
            // N/A (check with pzhao)
        }
    
        @Override
        protected void removeSubjectTypeAssignmentOfPolicy(Long policyId,
                List<Long> removeSubjectTypeList) {
            // N/A (check with pzhao)
        }
    
        @Override
        public Map<Long, SubjectTypeInfo> getSubjectTypeAssignmentOfPolicy(
                Long policyId, QueryCondition queryCondition)
                throws PolicyFinderException {
            // N/A (check with pzhao)
            return null;
        }

		@Override
		protected boolean updateRuleOfPolicy(Long policyId,
				List<Rule> removeList, List<Rule> addList)
				throws PolicyUpdateException {
			boolean modified = false;
			org.ebayopensource.turmeric.policyservice.model.Policy policy = policyDAO
					.findPolicyById(policyId);
			if (policy != null && policy.getRules() != null) {
				try {

					// Remove from list
					for (Rule rule : removeList) {
						org.ebayopensource.turmeric.policyservice.model.Rule jpaRule = null;
						if (rule.getRuleId() != null) {
							jpaRule = ruleDAO.findRuleById(rule.getRuleId());
						} else if (rule.getRuleName() != null
								&& rule.getRuleName().trim().length() != 0) {
							jpaRule = ruleDAO
									.findRuleByName(rule.getRuleName());
						}
						if (jpaRule != null && jpaRule.getId() != null) {
							policy.getRules().remove(jpaRule);
							modified =true;
						}
					}
					
					// add new rule
					for (Rule rule : addList) {
						org.ebayopensource.turmeric.policyservice.model.Rule jpaRule = null;

						if (isRuleValid(rule)) {
							if (rule.getRuleId() != null) {
								jpaRule = ruleDAO
										.findRuleById(rule.getRuleId());
							} else if (rule.getRuleName() != null
									&& rule.getRuleName().trim().length() != 0) {
								jpaRule = ruleDAO.findRuleByName(rule
										.getRuleName());
							} else {
								jpaRule = RuleHelper.convert(rule);
							}
							if(jpaRule!=null){
							   policy.getRules().add(jpaRule);
							   modified =true;
							}
						}

					}

				} catch (Exception ex) {
					throw new PolicyUpdateException(Category.RULE, policyType,
							policyId, policy.getPolicyName(),
							"Failed to update policy", ex);
				}
			}
			return modified;
		}
    
        @Override
        public boolean isRuleNameUsed(String ruleName) throws PolicyFinderException {
          return ruleDAO.isRuleNameUsed(ruleName);
        }
    
        @Override
        public boolean isRuleRequired() throws PolicyProviderException {
        	
            return "RL".equalsIgnoreCase(getPolicyType());
        }
    
        @Override
        public boolean isRuleValid(Rule rule) throws PolicyProviderException {
        	
            return ruleDAO.isRuleValid(RuleHelper.convert(rule),isRuleRequired());
        }
    
        @Override
        public boolean isUpdateRequired(Date lastUpdated) throws PolicyProviderException {
            // N/A
            return false;
        }
    
		@Override
		public boolean validatePolicy(Policy policy,
				QueryCondition queryCondition) throws PolicyProviderException {
			if (policy != null) {
				if (policy.getRule() != null)
					for (Rule rule : policy.getRule()) {
						if(!isRuleValid(rule)){
							throw new PolicyValidationException(Category.RULE, policyType,
									"rule is incorrect");
							
						}
					}
				validateQueryCondition(queryCondition, policy);
				return true;
			}
			// not going to implement this method in initial phase.
			return false;
		}
        //  use to validate QueryCondition 
    	public void validateQueryCondition(QueryCondition condition,Policy policy) throws PolicyValidationException
    		 {
    		if (condition == null) {
				throw new PolicyValidationException(Category.QUERY, policyType,
						"please input policy and scope");
    		} else if (condition.getQuery() == null) {
    			throw new PolicyValidationException(Category.QUERY, policyType,
						"Invalid query condition");
    		} else {
    			for (Query query : condition.getQuery()) {
    				// check if one is null
    				if (query.getQueryType() == null
    						|| query.getQueryType().trim().length() == 0
    						|| query.getQueryValue() == null
    						|| query.getQueryValue().trim().length() == 0) {
    	    			throw new PolicyValidationException(Category.QUERY, policyType,
    							"Invalid query condition");
    				}
    				// trim it
    				String queryType = query.getQueryType().trim();
    				String queryValue = query.getQueryValue().trim();
    				// Effect/SubjectSearchScope/MaskedIds/ActivePoliciesOnly
    				// queryType
    				if (!("Effect".equalsIgnoreCase(queryType)
    						|| "SubjectSearchScope".equalsIgnoreCase(queryType)
    						|| "MaskedIds".equalsIgnoreCase(queryType) || "ActivePoliciesOnly"
    						.equalsIgnoreCase(queryType)||"ValidateRule"
    						.equalsIgnoreCase(queryType))) {
    	    			throw new PolicyValidationException(Category.QUERY, policyType,
    									"Invalid query type for condition");
    				}
    				// BLOCK|FLAG|CHALLENGE|ALLOW/TARGET|EXCLUDED|BOTH/TRUE|FALSE/TRUE|FALSE
    				// queryValue
    				// ActivePoliciesOnly and MaskedIds must be boolean
    				if (("ActivePoliciesOnly".equalsIgnoreCase(queryType) || "MaskedIds"
    						.equalsIgnoreCase(queryType))
    						&& (!("true".equalsIgnoreCase(queryValue) || "false"
    								.equalsIgnoreCase(queryValue)))) {
       	    			throw new PolicyValidationException(Category.QUERY, policyType,
    							"Invalid query value for condition");
    				}
    				// Effect allowed are BLOCK|FLAG|CHALLENGE|ALLOW
    				if ("Effect".equalsIgnoreCase(queryType)
    						&& (!("BLOCK".equalsIgnoreCase(queryValue)
    								|| "FLAG".equalsIgnoreCase(queryValue)
    								|| "ALLOW".equalsIgnoreCase(queryValue) || "CHALLENGE"
    								.equalsIgnoreCase(queryValue)))) {
       	    			throw new PolicyValidationException(Category.QUERY, policyType,
						"Invalid query value for condition");
    				}
    				// SubjectSearchScope TARGET|EXCLUDED|BOTH
    				if ("SubjectSearchScope".equalsIgnoreCase(queryType)
    						&& (!("TARGET".equalsIgnoreCase(queryValue)
    								|| "EXCLUDED".equalsIgnoreCase(queryValue) || "BOTH"
    								.equalsIgnoreCase(queryValue)))) {
       	    			throw new PolicyValidationException(Category.QUERY, policyType,
						"Invalid query value for condition");

    				}
    				//ValidateRule yes or no
    				if("ValidateRule".equalsIgnoreCase(queryType)&&(!("YES".equalsIgnoreCase(queryValue)
    								|| "NO".equalsIgnoreCase(queryValue)))){
       	    			throw new PolicyValidationException(Category.QUERY, policyType,
						"Invalid query value for condition");
    					
    				}
    			}

    		}

    	}
        @Override
        public boolean allowResourceLevel() {
            return true;
        }
    
        @Override
        public boolean allowGlobalLevel() {
            return true;
        }
    
        @Override
        public List<KeyValuePair> getMetaData(String queryValue) throws PolicyFinderException {
            if (queryValue.equals("Type")) {
                    List<KeyValuePair> ret = new ArrayList<KeyValuePair>();
                    KeyValuePair valuePair = new KeyValuePair();
                    valuePair.setKey(getPolicyType());
                    ret.add(valuePair);
                    return ret;
            }
                   
            return null;
        }

        @Override
        protected String getPolicyType() {
            return policyType;
        }
    }

    @Override
    public PolicyKey updatePolicy(Policy inputPolicy, PolicyEditObject policyEditObject,
                    SubjectKey modifiedBy) throws PolicyUpdateException, PolicyCreationException,
                    PolicyDeleteException {
        return impl.updatePolicy(inputPolicy, policyEditObject, modifiedBy);
    }

    @Override
    public PolicyKey createPolicy(Policy inputPolicy, PolicyEditObject policyEditObject,
                    SubjectKey createdBy) throws PolicyCreationException, PolicyUpdateException {
        return impl.createPolicy(inputPolicy, policyEditObject, createdBy);
    }

    @Override
    public void deletePolicy(Long policyId) throws PolicyDeleteException, PolicyUpdateException {
        impl.deletePolicy(policyId);
    }

    @Override
    public Map<Long, Rule> getRuleAssignmentOfPolicy(Long policyId, QueryCondition queryCondition)
                    throws PolicyFinderException {
        return impl.getRuleAssignmentOfPolicy(policyId, queryCondition);
    }

    @Override
    public boolean isRuleNameUsed(String ruleName) throws PolicyFinderException {
        return impl.isRuleNameUsed(ruleName);
    }

    @Override
    public boolean isRuleRequired() throws PolicyProviderException {
        return impl.isRuleRequired();
    }

    @Override
    public boolean isRuleValid(Rule rule) throws PolicyProviderException {
        return impl.isRuleValid(rule);
    }

    @Override
    public Map<Long, Resource> getResourceAssignmentOfPolicy(Long policyId,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.getResourceAssignmentOfPolicy(policyId, queryCondition);
    }

    @Override
    public Map<Long, Operation> getOperationAssignmentOfPolicy(Long policyId,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.getOperationAssignmentOfPolicy(policyId, queryCondition);
    }

    @Override
    public Map<Long, Subject> getSubjectAssignmentOfPolicy(Long policyId,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.getSubjectAssignmentOfPolicy(policyId, queryCondition);
    }

    @Override
    public Map<Long, Subject> getExclusionSubjectAssignmentOfPolicy(Long policyId,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.getExclusionSubjectAssignmentOfPolicy(policyId, queryCondition);
    }

    @Override
    public Map<Long, SubjectTypeInfo> getSubjectTypeAssignmentOfPolicy(Long policyId,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.getSubjectTypeAssignmentOfPolicy(policyId, queryCondition);
    }

    @Override
    public Map<Long, SubjectGroup> getSubjectGroupAssignmentOfPolicy(Long policyId,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.getSubjectGroupAssignmentOfPolicy(policyId, queryCondition);
    }

    @Override
    public Map<Long, SubjectGroup> getExclusionSubjectGroupAssignmentOfPolicy(Long policyId,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.getExclusionSubjectGroupAssignmentOfPolicy(policyId, queryCondition);
    }

    @Override
    public Map<Long, Policy> findPolicyInfoBySubject(Set<Long> subjectId,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.findPolicyInfoBySubject(subjectId, queryCondition);
    }

    @Override
    public Map<Long, Policy> findPolicyInfoByExclusionSubject(Set<Long> subjectId,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.findPolicyInfoByExclusionSubject(subjectId, queryCondition);
    }

    @Override
    public Map<Long, Policy> findPolicyInfoBySubjectGroup(Set<Long> subjectGroupId,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.findPolicyInfoBySubjectGroup(subjectGroupId, queryCondition);
    }

    @Override
    public Map<Long, Policy> findPolicyInfoByExclusionSubjectGroup(Set<Long> subjectGroupId,
                    QueryCondition queryCondition) throws PolicyProviderException {
        return impl.findPolicyInfoByExclusionSubjectGroup(subjectGroupId, queryCondition);
    }

    @Override
    public Map<Long, Policy> findPolicyInfoBySubjectType(Set<String> subjectType,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.findPolicyInfoBySubjectType(subjectType, queryCondition);
    }

    @Override
    public Map<Long, Policy> findPolicyInfoByOperation(Set<Long> operationId,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.findPolicyInfoByOperation(operationId, queryCondition);
    }

    @Override
    public Map<Long, Policy> findPolicyInfoByResource(Set<Long> resourceId,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.findPolicyInfoByResource(resourceId, queryCondition);
    }

    @Override
    public Map<Long, Policy> findPolicyInfo(PolicyKey policyKey, String effect,
                    QueryCondition queryCondition) throws PolicyFinderException {
        return impl.findPolicyInfo(policyKey, effect, queryCondition);
    }

    @Override
    public Policy getPolicyInfo(Long policyId) throws PolicyFinderException {
        return impl.getPolicyInfo(policyId);
    }

    @Override
    public Policy getPolicyInfo(String policyName) throws PolicyFinderException {
    	try {
    		return impl.getPolicyInfo(policyName);
    	} catch (Exception ex) {
    		
    	}
    	return null;
    }

    @Override
    public boolean isUpdateRequired(Date lastUpdated) throws PolicyProviderException {
        return impl.isUpdateRequired(lastUpdated);
    }

    @Override
    public boolean validatePolicy(Policy policy, QueryCondition queryCondition)
                    throws PolicyProviderException {
        return impl.validatePolicy(policy, queryCondition);
    }

    @Override
    public List<EntityHistory> getAuditHistory(PolicyKey policyKey, XMLGregorianCalendar startDate,
                    XMLGregorianCalendar endDate) throws PolicyFinderException {
        return impl.getAuditHistory(policyKey, startDate, endDate);
    }

    @Override
    public void audit(PolicyKey policyKey, String operationType, SubjectKey loginSubject)
                    throws PolicyFinderException {
        impl.audit(policyKey, operationType, loginSubject);
    }

    @Override
    public PolicyBuilderObject applyQueryCondition(PolicyBuilderObject builderObject,
                    QueryCondition queryCondition) {
        return impl.applyQueryCondition(builderObject, queryCondition);
    }

    @Override
    public boolean allowResourceLevel() {
        return impl.allowResourceLevel();
    }

    @Override
    public boolean allowGlobalLevel() {
        return impl.allowGlobalLevel();
    }

    @Override
    public List<KeyValuePair> getMetaData(String queryValue) throws PolicyFinderException {
        return impl.getMetaData(queryValue);
    }
}
