/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.policyservice.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.BaseResponse;
import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.errorlibrary.turmericpolicy.ErrorConstants;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyCreationException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyDeleteException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyFinderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyUpdateException;
import org.ebayopensource.turmeric.policyservice.exceptions.PolicyValidationException;
import org.ebayopensource.turmeric.policyservice.intf.PolicyService;
import org.ebayopensource.turmeric.policyservice.provider.AuthenticationProvider;
import org.ebayopensource.turmeric.policyservice.provider.PolicyTypeProvider;
import org.ebayopensource.turmeric.policyservice.provider.ResourceTypeProvider;
import org.ebayopensource.turmeric.policyservice.provider.SubjectTypeProvider;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorUtils;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceRuntimeException;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.runtime.common.utils.Preconditions;
import org.ebayopensource.turmeric.security.v1.services.CreateExternalSubjectReferenceRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateExternalSubjectReferencesResponse;
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.CreatePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectsRequest;
import org.ebayopensource.turmeric.security.v1.services.CreateSubjectsResponse;
import org.ebayopensource.turmeric.security.v1.services.DeletePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.DeletePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.DeleteResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.DeleteResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.DeleteSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.DeleteSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.DeleteSubjectsRequest;
import org.ebayopensource.turmeric.security.v1.services.DeleteSubjectsResponse;
import org.ebayopensource.turmeric.security.v1.services.DisablePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.DisablePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.EnablePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.EnablePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.EntityHistory;
import org.ebayopensource.turmeric.security.v1.services.FindExternalSubjectsRequest;
import org.ebayopensource.turmeric.security.v1.services.FindExternalSubjectsResponse;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesResponse;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectsRequest;
import org.ebayopensource.turmeric.security.v1.services.FindSubjectsResponse;
import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.GetEntityHistoryRequest;
import org.ebayopensource.turmeric.security.v1.services.GetEntityHistoryResponse;
import org.ebayopensource.turmeric.security.v1.services.GetMetaDataRequest;
import org.ebayopensource.turmeric.security.v1.services.GetMetaDataResponse;
import org.ebayopensource.turmeric.security.v1.services.GetOperationsRequest;
import org.ebayopensource.turmeric.security.v1.services.GetOperationsResponse;
import org.ebayopensource.turmeric.security.v1.services.GetResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.GetResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.KeyValuePair;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.OperationKey;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.PolicySet;
import org.ebayopensource.turmeric.security.v1.services.Query;
import org.ebayopensource.turmeric.security.v1.services.QueryCondition;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.security.v1.services.UpdateMode;
import org.ebayopensource.turmeric.security.v1.services.UpdatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.UpdatePolicyResponse;
import org.ebayopensource.turmeric.security.v1.services.UpdateResourcesRequest;
import org.ebayopensource.turmeric.security.v1.services.UpdateResourcesResponse;
import org.ebayopensource.turmeric.security.v1.services.UpdateSubjectGroupsRequest;
import org.ebayopensource.turmeric.security.v1.services.UpdateSubjectGroupsResponse;
import org.ebayopensource.turmeric.security.v1.services.ValidatePolicyRequest;
import org.ebayopensource.turmeric.security.v1.services.ValidatePolicyResponse;
import org.ebayopensource.turmeric.services.policyservice.provider.config.PolicyServiceProviderFactory;
import org.ebayopensource.turmeric.utils.ContextUtils;
 

public class PolicyServiceImpl extends SelfProvisioningPolicy
    implements PolicyService
{	
	private FindPolicyRequestHelper m_findPolicyReqHelper;
	private static final Logger s_Logger = LogManager.getInstance(PolicyServiceImpl.class);
	private static final String s_providerPropFilePath = 
		"META-INF/soa/services/config/PolicyService/service_provider.properties";
	private static final String s_providerPropKey = "preferred-provider";

	static String createType = "create";
	static String updateType = "update";
	static String deleteType = "delete";
	 
	static {
		
		ClassLoader classLoader = ContextUtils.getClassLoader();
		InputStream	inStream = classLoader.getResourceAsStream(s_providerPropFilePath);
		String provider = null;
		if (inStream != null) {
			Properties properties = new Properties();
			try {
				properties.load(inStream);
				provider = (String)properties.get(s_providerPropKey);
			} catch (IOException e) {
				// ignore
			}
			finally {
				try {
					inStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		try {
			PolicyServiceProviderFactory.initialize(provider);
		} catch (ServiceException e) {
			s_Logger.log(Level.SEVERE, "Initialization of provider factory failed: " + e.getMessage());
		}
	}
	
    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#deletePolicy(org.ebayopensource.turmeric.security.v1.services.DeletePolicyRequest)
     */
	@Override
    public DeletePolicyResponse deletePolicy(DeletePolicyRequest request) {
    	
    	DeletePolicyResponse response = new DeletePolicyResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
    		if (request == null || request.getPolicyKey() == null)
    			throwInvalidInputException("please input value for policy to delete");
    		
    		PolicyKey policyKey = request.getPolicyKey();
    		SubjectKey loginSubject = getLoginSubject();
    		deletePolicy(policyKey);
    		deleteProvisioningPolicy(policyKey, getLoginSubject());
    		audit(policyKey, deleteType, loginSubject);
    	
    		response.setAck(AckValue.SUCCESS);
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
        return response;
    }

   
    private void mapError(BaseResponse response,
			PolicyProviderException e) {
    	CommonErrorData errData = null;
    	// Log the error
    	s_Logger.log(Level.SEVERE, "policy service error", e);
    	if (e instanceof PolicyCreationException) {
    		errData = ErrorUtils.createErrorData(ErrorConstants.SVC_POLICYSERVICE_PROVIDER_CREATE_ERROR, 
    				ErrorConstants.ERRORDOMAIN.toString(), 
    				new Object[] {e.getCategory().toString(), e.getEntityType(), 
    								e.getEntityId(), e.getEntityName(), e.getMessage()});
    	} else if (e instanceof PolicyUpdateException) {
    		errData = ErrorUtils.createErrorData(ErrorConstants.SVC_POLICYSERVICE_PROVIDER_UPDATE_ERROR, 
    				ErrorConstants.ERRORDOMAIN.toString(), 
    				new Object[] {e.getCategory().toString(), e.getEntityType(), 
    								e.getEntityId(), e.getEntityName(), e.getMessage()});
    	} else if (e instanceof PolicyDeleteException) {
    		errData = ErrorUtils.createErrorData(ErrorConstants.SVC_POLICYSERVICE_PROVIDER_DELETE_ERROR, 
    				ErrorConstants.ERRORDOMAIN.toString(), 
    				new Object[] {e.getCategory().toString(), e.getEntityType(), 
    								e.getEntityId(), e.getEntityName(), e.getMessage()});
    	} else if (e instanceof PolicyFinderException) {
    		errData = ErrorUtils.createErrorData(ErrorConstants.SVC_POLICYSERVICE_PROVIDER_FINDER_ERROR, 
    				ErrorConstants.ERRORDOMAIN.toString(), 
    				new Object[] {e.getCategory().toString(), e.getEntityType(), 
    								e.getEntityId(), e.getEntityName(), e.getMessage()});
    		
    	} else if (e instanceof PolicyValidationException) {
    		errData = ErrorUtils.createErrorData(ErrorConstants.SVC_POLICYSERVICE_INVALID_INPUT_ERROR, 
    				ErrorConstants.ERRORDOMAIN.toString(), 
    				new Object[] {e.getCategory().toString(), e.getEntityType(), 
    								e.getEntityId(), e.getEntityName(), e.getMessage()});
    	} else {
    		errData = ErrorUtils.createErrorData(ErrorConstants.SVC_POLICYSERVICE_PROVIDER_MISC_ERROR, 
    				ErrorConstants.ERRORDOMAIN.toString(), 
    				new Object[] {e.getCategory().toString(), e.getEntityType(), 
    								e.getEntityId(), e.getEntityName(), e.getMessage()});
    	}
    	
    	mapError(response, errData);
	}


	private void mapError(BaseResponse response, ServiceException e) {
    	CommonErrorData errData = null;
    	if (e.getErrorMessage() == null || e.getErrorMessage().getError().isEmpty()) {
    		// create a new errorData
    		errData = ErrorUtils.createErrorData(ErrorConstants.SVC_POLICYSERVICE_INTERNAL_ERROR,
    				ErrorConstants.ERRORDOMAIN.toString());
    	} else
    		errData = e.getErrorMessage().getError().get(0);
    	
    	// Log the error
    	s_Logger.log(Level.SEVERE, "policy service error", e);
    	mapError(response, errData);
		
	}


	/**
	 * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#findPolicies(org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest)
	 */
	@Override
	public FindPoliciesResponse findPolicies(FindPoliciesRequest request) {
       
    	Preconditions.checkNotNull(request, 
    			new ServiceRuntimeException(
    					ErrorUtils.createErrorData(
    							ErrorConstants.SVC_POLICYSERVICE_INVALID_INPUT_ERROR, 
    							ErrorConstants.ERRORDOMAIN.toString(), new Object[]{"Request cannot be empty"})));
    	
    	m_findPolicyReqHelper = new FindPolicyRequestHelper(request);
    	FindPoliciesResponse lastResponse = new FindPoliciesResponse();
    	
    	try {
    		m_findPolicyReqHelper.validate();
    		
			if (m_findPolicyReqHelper.isTimeBasedRequest()) {
				if (! isTimeBasedUpdateNeeded()) {
					// No updates.. just send a SUCCESS Ack
					lastResponse.setPolicySet(new PolicySet());
					lastResponse.setAck(AckValue.SUCCESS);
					return lastResponse;
				}	
			}

			/**
			 * Use a Set to eliminate duplicate PolicyKey's from being submitted
			 */
			Set<String> policyTypesCalled = new HashSet<String>();
			for(PolicyKey key : m_findPolicyReqHelper.getPolicyKeyList()) {
				if (policyTypesCalled.add(key.getPolicyType().toString())) {
					FindPoliciesResponse response = findPoliciesInternal(key.getPolicyType());
					lastResponse = merge(response, lastResponse);
				}
			}
			
			if (lastResponse.getPolicySet() == null)
				lastResponse.setPolicySet(new PolicySet());
			lastResponse.setAck(AckValue.SUCCESS);
		} catch (PolicyProviderException e) {
			mapError(lastResponse, e);
		} catch (ServiceException e) {
			mapError(lastResponse, e);
		} 
    	   	
        return lastResponse;
    }
    
	private void mapError(BaseResponse lastResponse, CommonErrorData errData) {
		lastResponse.setAck(AckValue.FAILURE);
		ErrorMessage errorMessage = new ErrorMessage();
		errorMessage.getError().add(errData);
		lastResponse.setErrorMessage(errorMessage);
	}


	private FindPoliciesResponse merge(FindPoliciesResponse response,
			FindPoliciesResponse lastResponse) {
		
		if (lastResponse == null)
			return response;
		
		if (response == null)
			return lastResponse;
		
		if(lastResponse.getPolicySet() == null)
			return response;
		
		if(response.getPolicySet() == null)
			return lastResponse;
		
		Preconditions.checkArgument((lastResponse.getPolicySet() != null  && 
									 lastResponse.getPolicySet().getLastModified() == null) &&
									 (lastResponse.getPolicySet() != null  && 
									  lastResponse.getPolicySet().getPolicy().isEmpty() == false) &&
									 (response.getPolicySet() != null  && 
									  response.getPolicySet().getLastModified() == null) &&
									 (response.getPolicySet() != null  && 
									  response.getPolicySet().getPolicy().isEmpty() == false));

		FindPoliciesResponse mergedResponse = new FindPoliciesResponse();
		mergedResponse.setPolicySet(new PolicySet());
		mergedResponse.getPolicySet().setPolicyCombiningAlgId(lastResponse.getPolicySet().getPolicyCombiningAlgId());
		
		if (response.getPolicySet() != null) {
			mergedResponse.getPolicySet().getPolicy().addAll(response.getPolicySet().getPolicy());
		}
		if (lastResponse.getPolicySet() != null)
			mergedResponse.getPolicySet().getPolicy().addAll(lastResponse.getPolicySet().getPolicy());

		// Find the max timestamp
		Date timestamp1 = response.getPolicySet().getLastModified().toGregorianCalendar().getTime();
		Date timestamp2 = lastResponse.getPolicySet().getLastModified().toGregorianCalendar().getTime();
		if (timestamp1 != null && timestamp2 != null)
			mergedResponse
					.getPolicySet().setLastModified((timestamp1.after(timestamp2) ? toXMLGregorianCalendar(timestamp1)
							: toXMLGregorianCalendar(timestamp2)));
		else if (timestamp1 != null && timestamp2 == null)
			mergedResponse.getPolicySet().setLastModified(toXMLGregorianCalendar(timestamp1));
		else
			mergedResponse.getPolicySet().setLastModified(toXMLGregorianCalendar(timestamp2));

		mergedResponse.setTimestamp(mergedResponse.getPolicySet().getLastModified());
		return mergedResponse;
	}


	private XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
      XMLGregorianCalendar startTimeCal = null;
      if (date == null)
          return null;
      try {
          DatatypeFactory df = DatatypeFactory.newInstance();
          GregorianCalendar cal = (GregorianCalendar) GregorianCalendar.getInstance();
          cal.setTime(date);
          startTimeCal = df.newXMLGregorianCalendar(cal);

      } catch ( DatatypeConfigurationException e ) {
      	throw new IllegalArgumentException( e );
      }
      return startTimeCal;
	}

	private boolean isTimeBasedUpdateNeeded() throws ServiceException, PolicyProviderException {
		Date lastUpdated = m_findPolicyReqHelper.getLastModified();
		if (lastUpdated == null)
			return false;
		for ( String policyType : PolicyServiceProviderFactory.getPolicyTypes()) {
			PolicyTypeProvider provider = PolicyServiceProviderFactory.getPolicyTypeProvider(policyType);
			if (provider.isUpdateRequired(lastUpdated))
				return true;
		}
		return false;
	}
	
    private FindPoliciesResponse findPoliciesInternal(String policyType) throws  ServiceException, PolicyProviderException {
    	// If it is a timestamp based request, or no filters are specified, then get all policies
		if ( m_findPolicyReqHelper.getLastModified() != null) {
			return findPoliciesByPolicyTypeOnly(policyType );
		}  
		else if (m_findPolicyReqHelper.isPolicyKeyLookupOnly()) {
			return findPoliciesByPolicyKeyOnly(policyType);
		}  else if (!m_findPolicyReqHelper.isFiltered()) {
			return findPoliciesByPolicyTypeOnly(policyType );
		} else if (m_findPolicyReqHelper.isSubjectFiltered()) {
			return findPoliciesBySubject(policyType);
		} else if (m_findPolicyReqHelper.isSubjectGroupFiltered()) {
			return findPoliciesBySubjectGroup(policyType);
		} else if (m_findPolicyReqHelper.isOperationFiltered()) {
			return findPoliciesByOperation(policyType);
			
		} else if (m_findPolicyReqHelper.isResourceFiltered()) {
			return findPoliciesByResource(policyType);
		}	// if filtered by effect only
		else if (m_findPolicyReqHelper.isEffectFilteredOnly() ) {
			return findPoliciesByPolicyTypeOnly(policyType);
		} else{
			return findPoliciesByPolicyKeyOnly(policyType);
		}
	}

    private FindPoliciesResponse findPoliciesByResource(
			String policyType) throws ServiceException,  PolicyProviderException {
		List<ResourceKey> resourceList = m_findPolicyReqHelper.getResourceList();
		Set<Long> resourceIds = new HashSet<Long>();
		Set<Long> operationIds = new HashSet<Long>();
		for (ResourceKey resourceKey : resourceList) {
			if (resourceKey == null ) {
				throwInvalidInputException("please input value for resource");
			}
			ResourceTypeProvider resourceTypeProvider = PolicyServiceProviderFactory.getResourceTypeProvider(resourceKey.getResourceType());

			if (resourceKey.getResourceName() == null && resourceKey.getResourceId() == null) {
				throwInvalidInputException("please input value for resource name or resource Id");
			}
			
			if (resourceKey.getResourceName() != null) {
				Resource resource = resourceTypeProvider.getResourceInfoByName(resourceKey.getResourceName());
				if (resource == null) {
					s_Logger.log(Level.WARNING, "resource with resourceName: " + 
							resourceKey.getResourceName() + " not found");
				} else
					resourceKey.setResourceId(resource.getResourceId());
			}
			if (resourceKey.getResourceId() != null) {
				resourceIds.add(resourceKey.getResourceId());
				
				// get operations associated with the resource
				List<Operation> operations = resourceTypeProvider.getOperationByResourceId(resourceKey.getResourceId());
				if (operations != null && operations.isEmpty() == false) {
					for (Operation operation : operations) {
						operationIds.add(operation.getOperationId());
					}
				}
			}
			
		}
		
		List<Policy> policies = findPoliciesByResource(resourceIds, policyType);
		List<Policy> opPolicies = findPoliciesByOperation(operationIds, policyType);
		policies = merge(opPolicies, policies);
		FindPoliciesResponse response = null;
		if (policies != null && !policies.isEmpty())
			response  = populatePolicies(policies);
		return response;
	}


	private FindPoliciesResponse findPoliciesByOperation(
			String policyType) throws ServiceException,  PolicyProviderException {
		List<OperationKey> operationList = m_findPolicyReqHelper.getOperationList();
		Set<Long> operationIds = new HashSet<Long>();
		List<Policy> policies = new ArrayList<Policy>();
		
		for (OperationKey opKey : operationList) {
			if (opKey == null) {
				throwInvalidInputException("please input value for operation");
			}
			
			ResourceTypeProvider resourceTypeProvider = PolicyServiceProviderFactory.getResourceTypeProvider(opKey.getResourceType());
			if (opKey.getOperationId() == null && (opKey.getOperationName() == null || opKey.getResourceName() == null))
			{
				throwInvalidInputException("please input value for resource/operation name or operation Id");				
			}
			
			if (opKey.getOperationName() != null) {
				Operation operation = resourceTypeProvider.getOperationByName(opKey.getResourceName(), opKey.getOperationName());
				if (operation == null) {
					s_Logger.log(Level.WARNING, "operation with operationName: " + 
							opKey.getOperationName() + " for resource: " + opKey.getResourceName() + " not found");
				} else
					opKey.setOperationId(operation.getOperationId());
			}
			if (opKey.getOperationId() != null) {
				operationIds.add(opKey.getOperationId());
			}
		}
		
		policies = findPoliciesByOperation(operationIds, policyType);
		FindPoliciesResponse response = null;
		if (policies != null && !policies.isEmpty())
			response  = populatePolicies(policies);
		return response;
	}


	private List<Policy> findPoliciesByOperation(
			Set<Long> operationIds, String policyType) throws ServiceException,  PolicyProviderException {
		PolicyTypeProvider provider = PolicyServiceProviderFactory.getPolicyTypeProvider(policyType);
    	List<Policy> policies = new ArrayList<Policy>();
    	if (operationIds == null || operationIds.isEmpty())
    		return policies;
    	
    	Map<Long, Policy> result = provider.findPolicyInfoByOperation(operationIds, m_findPolicyReqHelper.getQueryCondition());
    	if (result == null || result.isEmpty())
    		return policies;
    	map(result, policies);
    	return policies;
	}
	
	private void map(Map<Long, Policy> result, List<Policy> policies) {
		if (result == null || result.isEmpty())
    		return ;
		
		for (Map.Entry<Long, Policy> entry : result.entrySet()) {
			policies.add(entry.getValue());
		}
		
	}


	private List<Policy> findPoliciesByResource(
			Set<Long> resourceIds, String policyType) throws ServiceException,  PolicyProviderException {
		PolicyTypeProvider provider = PolicyServiceProviderFactory.getPolicyTypeProvider(policyType);
    	List<Policy> policies = new ArrayList<Policy>();
    	if (resourceIds == null || resourceIds.isEmpty())
    		return policies;
    	
    	Map<Long, Policy> result = provider.findPolicyInfoByResource(resourceIds, m_findPolicyReqHelper.getQueryCondition());
    	if (result == null || result.isEmpty())
    		return policies;
    	map(result, policies);
    	return policies;
	}


	private FindPoliciesResponse findPoliciesBySubjectGroup(
			String policyType) throws ServiceException,  PolicyProviderException {
    	List<SubjectGroupKey> subjectGrpKeyList = m_findPolicyReqHelper.getSubjectGroupList();
    	FindPoliciesResponse response = null;
    	List<Policy> policies = new ArrayList<Policy>();
    	Set<Long> subjectGroupIds = new HashSet<Long>();
    	Set<String> subjectGroupTypes = new HashSet<String>();
    	
    	for (SubjectGroupKey subjectGroupKey : subjectGrpKeyList) {
			if (subjectGroupKey == null) {
				throwInvalidInputException("please input subject group");
			}
			
			SubjectTypeProvider subjProvider = 
				PolicyServiceProviderFactory.getSubjectTypeProvider(subjectGroupKey.getSubjectType());
			
			if (subjectGroupKey.getSubjectGroupName() == null && subjectGroupKey.getSubjectGroupId() == null ) {
				throwInvalidInputException("please input value for subject group name or subject group Id");
			}
			
			if (subjectGroupKey.getSubjectGroupName() != null) {
				Map<Long, SubjectGroup> subjectGroupMap = subjProvider.getSubjectGroupInfoByName(subjectGroupKey.getSubjectGroupName());
				if (subjectGroupMap.isEmpty()) {
					s_Logger.log(Level.WARNING, "SubjectGroup with name: " + 
							subjectGroupKey.getSubjectGroupName() + " not found");					
				} else
					subjectGroupKey.setSubjectGroupId(subjectGroupMap.keySet().iterator().next());			
			}
			
			if (subjectGroupKey.getSubjectGroupId() != null)
				subjectGroupIds.add(subjectGroupKey.getSubjectGroupId());
			
			subjectGroupTypes.add(subjectGroupKey.getSubjectType());
		}
    	
    	List<Policy> sbjPolicies = findPoliciesBySubjectGroup(subjectGroupIds, policyType);
    	policies = merge(sbjPolicies, policies);
    	sbjPolicies = findPoliciesBySubjectType(subjectGroupTypes, policyType);
    	policies = merge(sbjPolicies, policies);
    	if (policies != null && !policies.isEmpty())
			response = populatePolicies(policies);
		return response;
    	
 	}

    private List<Policy> findPoliciesBySubjectGroup(Set<Long> subjectGrpIds, String policyType) throws ServiceException,  PolicyProviderException {
    	PolicyTypeProvider provider = PolicyServiceProviderFactory.getPolicyTypeProvider(policyType);
    	List<Policy> policies = new ArrayList<Policy>();
    	if (subjectGrpIds == null || subjectGrpIds.isEmpty())
    		return policies;
    	if (m_findPolicyReqHelper.findInclusions()) {
			Map<Long, Policy> sbjPolicies = provider.findPolicyInfoBySubjectGroup(subjectGrpIds, m_findPolicyReqHelper.getQueryCondition());
			if (sbjPolicies != null && !sbjPolicies.isEmpty()) {
				map(sbjPolicies, policies);
			}
		}
		if (m_findPolicyReqHelper.findExclusions()) {
			Map<Long, Policy> sbjPolicies = provider.findPolicyInfoByExclusionSubjectGroup(subjectGrpIds , m_findPolicyReqHelper.getQueryCondition());
			if (sbjPolicies != null && !sbjPolicies.isEmpty()) {
				map(sbjPolicies, policies);
			}
		}
		
		return policies;
    }
    
    private List<Policy> findPoliciesBySubject(Set<Long> subjectIds, String policyType) throws ServiceException,  PolicyProviderException {
    	PolicyTypeProvider provider = PolicyServiceProviderFactory.getPolicyTypeProvider(policyType);
    	List<Policy> policies = new ArrayList<Policy>();
    	if (subjectIds == null || subjectIds.isEmpty())
    		return policies;
    	if (m_findPolicyReqHelper.findInclusions()) {
    		Map<Long, Policy> sbjPolicies = provider.findPolicyInfoBySubject(subjectIds, m_findPolicyReqHelper.getQueryCondition());
    		if (sbjPolicies != null && !sbjPolicies.isEmpty()) {
				map(sbjPolicies, policies);
			}
		}
		if (m_findPolicyReqHelper.findExclusions()) {
			Map<Long, Policy> sbjPolicies = provider.findPolicyInfoByExclusionSubject(subjectIds, m_findPolicyReqHelper.getQueryCondition());
    		if (sbjPolicies != null && !sbjPolicies.isEmpty()) {
				map(sbjPolicies, policies);
			}
		}
		
		return policies;
    }
    
    private List<Policy> findPoliciesBySubjectType(Set<String> subjectTypes, String policyType) throws ServiceException,  PolicyProviderException {
    	PolicyTypeProvider provider = PolicyServiceProviderFactory.getPolicyTypeProvider(policyType);
    	List<Policy> policies = new ArrayList<Policy>();
    	if (subjectTypes == null || subjectTypes.isEmpty())
    		return policies;
    	
    	Map<Long, Policy> sbjPolicies = provider.findPolicyInfoBySubjectType( subjectTypes, m_findPolicyReqHelper.getQueryCondition());
    	if (sbjPolicies != null && !sbjPolicies.isEmpty()) {
			map(sbjPolicies, policies);
		}
		
		return policies;
    }

	private FindPoliciesResponse findPoliciesBySubject(String policyType) throws ServiceException,  PolicyProviderException {
    	List<SubjectKey> subjectKeyList = m_findPolicyReqHelper.getSubjectList();
    	Set<Long> subjectIds = new HashSet<Long>();
    	Set<String> subjectTypes = new HashSet<String>();
    	FindPoliciesResponse response = null;
    	List<Policy> policies = new ArrayList<Policy>();
    	for (SubjectKey subjectKey : subjectKeyList) {
			if (subjectKey == null) {
				throwInvalidInputException("please input value for subject"); 
			}
			
			SubjectTypeProvider subjProvider = 
				PolicyServiceProviderFactory.getSubjectTypeProvider(subjectKey.getSubjectType());
			
			if(subjectKey.getSubjectName() == null && subjectKey.getSubjectId() == null) {
				throwInvalidInputException("please input value for subject name or subject Id");
			}
			if (subjectKey.getSubjectName() != null) {
				Map<Long, Subject> subjectMap = subjProvider.getSubjectByName(subjectKey.getSubjectName());
				if (subjectMap.isEmpty()) {
					s_Logger.log(Level.WARNING, "Subject with name: " + 
							subjectKey.getSubjectName() + " not found");					
				} else
					subjectKey.setSubjectId(subjectMap.keySet().iterator().next());
			}
			if (subjectKey.getSubjectId() != null) {
				subjectIds.add(subjectKey.getSubjectId());
			} else {
				// Get the subject type policies
				subjectTypes.add(subjectKey.getSubjectType());	
			}			
		}
    	
    	List<Policy> sbjPolicies = findPoliciesBySubject(subjectIds, policyType);
    	policies = merge(sbjPolicies, policies);
    	sbjPolicies = findPoliciesBySubjectType(subjectTypes, policyType);
    	policies = merge(sbjPolicies, policies);
    	if (policies != null && !policies.isEmpty())
			response = populatePolicies(policies);
		return response;
	}


	private List<Policy> merge(List<Policy> policies, List<Policy> lastPolicies) {
		// filtering of duplicate entries is done during policy building/population
		if (lastPolicies == null)
			return policies;
		if (policies != null) 
			lastPolicies.addAll(policies);
		return lastPolicies;
		
	}


	private FindPoliciesResponse findPoliciesByPolicyKeyOnly(
			String policyType) throws ServiceException,  PolicyProviderException {
		List<PolicyKey> policyKeys = m_findPolicyReqHelper.getPolicyKeyList(); 
		PolicyTypeProvider provider = PolicyServiceProviderFactory.getPolicyTypeProvider(policyType);
		FindPoliciesResponse response = null;
    	List<Policy> policies = new ArrayList<Policy>();
    	String effect = m_findPolicyReqHelper.getEffect();
		for (PolicyKey policyKey : policyKeys) {
			if (policyKey == null) {
				throwInvalidInputException("please input value for policy");
			}
			if (policyKey.getPolicyType().equals(policyType)) {
					Map<Long, Policy> allPoliciesMap = provider.findPolicyInfo(policyKey, effect, m_findPolicyReqHelper.getQueryCondition());
					if (allPoliciesMap != null && !allPoliciesMap.isEmpty()) {
						policies.addAll(allPoliciesMap.values());
					}					
			}
		}
		if (!policies.isEmpty())
			response = populatePolicies(policies);
		return response;
	}


	private FindPoliciesResponse findPoliciesByPolicyTypeOnly(String policyType) throws ServiceException,  PolicyProviderException {
    	PolicyTypeProvider provider = PolicyServiceProviderFactory.getPolicyTypeProvider(policyType);
    	PolicyKey key = new PolicyKey();
    	key.setPolicyType(policyType);
    	String effect = m_findPolicyReqHelper.getEffect();
		Map<Long,Policy> policiesMap = provider.findPolicyInfo(key, effect, m_findPolicyReqHelper.getQueryCondition());
		if (policiesMap == null || policiesMap.isEmpty())
			return null;
		FindPoliciesResponse response = populatePolicies(new ArrayList<Policy>(policiesMap.values()));	
		return response;
	}


	private FindPoliciesResponse populatePolicies(List<Policy> policies) throws  PolicyProviderException, ServiceException {
		FindPoliciesResponse response = new FindPoliciesResponse();
		List<Policy> policyList = new ArrayList<Policy>();
		Set<Long> policyIds = new HashSet<Long>();
		if (policies == null)
			return response;
		for (Policy policy : policies) {
			if ( policyIds.add(policy.getPolicyId()) && shouldOutputPolicy(policy)) {
				PolicyBuilder builder = new PolicyBuilder(m_findPolicyReqHelper, policy);
				builder.populatePolicy();
				policyList.add(policy);
			}
			updateLastModifiedTime(response, policy.getLastModified());
		}
		
		PolicySet policySet = new PolicySet();
		policySet
        .setPolicyCombiningAlgId("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
		policySet.setLastModified(response.getTimestamp());
		response.setPolicySet(policySet);
		response.getPolicySet().getPolicy().addAll(policyList);	
		
		return response;
	}


	private void updateLastModifiedTime(FindPoliciesResponse response,
			XMLGregorianCalendar lastModified) {
		
		if (lastModified == null ) return;
		
		if (response.getTimestamp() == null) {
			response.setTimestamp(lastModified);
			return;
		}
		if (lastModified.compare(response.getTimestamp()) > 0) 
			response.setTimestamp(lastModified);
		return;
	}


	private boolean shouldOutputPolicy(Policy policy) {
		return !m_findPolicyReqHelper.outputActivePoliciesOnly() || policy.isActive();
	}


	/**
	 * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#getAuthenticationPolicy(org.ebayopensource.turmeric.security.v1.services.GetAuthenticationPolicyRequest)
	 */
	@Override
	public GetAuthenticationPolicyResponse getAuthenticationPolicy(GetAuthenticationPolicyRequest request) {
    	GetAuthenticationPolicyResponse response = new GetAuthenticationPolicyResponse();
		try {
			AuthenticationProvider provider = PolicyServiceProviderFactory.getAuthenticationProvider();
			response = provider.getAuthenticationPolicy(request);
			response.setAck(AckValue.SUCCESS);
		} catch (ServiceException e) {
			mapError(response, e);
		} catch (PolicyProviderException e) {
			mapError(response, e);
		}
		return response;
    	
    }

    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#getResources(org.ebayopensource.turmeric.security.v1.services.GetResourcesRequest)
     */
	@Override
    public GetResourcesResponse getResources(GetResourcesRequest request) {
    	GetResourcesResponse response = new GetResourcesResponse();
    	response.setAck(AckValue.FAILURE);
    	try{
    		if (request == null ||
    				request.getResourceKey() == null ||
    				request.getResourceKey().isEmpty())
    			throwInvalidInputException("please input value for resource");
    		
    		for (ResourceKey resourcekey: request.getResourceKey())
    		{
    			Map<Long, Resource> result =  getResource(resourcekey);
    			if (!result.isEmpty())
    				response.getResources().addAll(result.values());
    		}
    		response.setAck(AckValue.SUCCESS);
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	return response;
    }
   
    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#createResources(org.ebayopensource.turmeric.security.v1.services.CreateResourcesRequest)
     */
	@Override
    public CreateResourcesResponse createResources(CreateResourcesRequest request) {
    	
    	CreateResourcesResponse response = new CreateResourcesResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
    		if (request == null || 
    				request.getResources() == null || 
    				request.getResources().isEmpty())
    			throwInvalidInputException("please input value for resource to create");
    			
    		List<Resource> inputList = request.getResources();
    		SubjectKey loginSubject = getLoginSubject();
    		for (Resource resource: inputList)
    		{
    			ResourceKey key = createResource(resource,loginSubject);
    			audit(key, createType, loginSubject);
    	    	response.getResourceIds().add(key.getResourceId());    				

    		}
        	response.setAck(AckValue.SUCCESS);
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
		
    	return response;
    }

    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#createExternalSubjectReferences(org.ebayopensource.turmeric.security.v1.services.CreateExternalSubjectReferenceRequest)
     */
	@Override
    public CreateExternalSubjectReferencesResponse createExternalSubjectReferences(CreateExternalSubjectReferenceRequest request) {
        
    	CreateExternalSubjectReferencesResponse response = new CreateExternalSubjectReferencesResponse();
    	response.setAck(AckValue.FAILURE);
    	//this one should not be supported any more
    	try {
    		if (request == null || 
    				request.getSubject()== null || 
    				request.getSubject().isEmpty())
    			throwInvalidInputException("please input value for subject to create");
    		
    		List<Long> retList = new ArrayList<Long>();
    		SubjectKey loginSubject = getLoginSubject();
    		for (Subject subject: request.getSubject())
    		{	
    			SubjectKey subjectKey = createSubject(subject, true,  loginSubject);
    			audit(subjectKey, createType, loginSubject);
    			if (subjectKey != null)
    				retList.add(subjectKey.getSubjectId());
    		}
    		
    		response.getSubjectIds().addAll(retList);
        	response.setAck(AckValue.SUCCESS);
    		
    	} catch (ServiceException e){
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	
    	return response;
    }

    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#createPolicy(org.ebayopensource.turmeric.security.v1.services.CreatePolicyRequest)
     */
	@Override
    public CreatePolicyResponse createPolicy(CreatePolicyRequest request) {
    	CreatePolicyResponse response = new CreatePolicyResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
    		if (request == null || request.getPolicy() == null)
    			throwInvalidInputException("please input value for policy to create");
    		
    		Policy inputPolicy = request.getPolicy();
    		Policy currPolicy = validatePolicyInfo(inputPolicy);
    		if (currPolicy != null )
				throwInvalidInputException("the policy already exists");
    		SubjectKey loginSubject = getLoginSubject();
    		PolicyKey policyKey = createPolicy(inputPolicy, loginSubject);
	    	createProvisioningPolicy(policyKey, loginSubject);
	    	
	    	audit(policyKey, createType, loginSubject);
	    	
	    	response.setAck(AckValue.SUCCESS);
	    	response.setPolicyId(policyKey.getPolicyId());
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	
    	return response;
    }
    
    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#findExternalSubjects(org.ebayopensource.turmeric.security.v1.services.FindExternalSubjectsRequest)
     */
	@Override
    public FindExternalSubjectsResponse findExternalSubjects(FindExternalSubjectsRequest request) {
    	FindExternalSubjectsResponse response = new FindExternalSubjectsResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
    		if (request == null || 
    				request.getSubjectQuery() == null ||
    				request.getSubjectQuery().getSubjectKey() == null ||
    				request.getSubjectQuery().getSubjectKey().isEmpty())
    			throwInvalidInputException("please input value for subject query");
    		
    		for (SubjectKey subjectKey : request.getSubjectQuery().getSubjectKey())
    		{
    			Set<Subject> work = findExternalSubject(subjectKey);
    			if (work != null)
    	    		response.getSubjects().addAll(work);
    		}
    		
    		response.setAck(AckValue.SUCCESS);
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	
    	return response;
    }

    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#getMetaData(org.ebayopensource.turmeric.security.v1.services.GetMetaDataRequest)
     */
	@Override
    public GetMetaDataResponse getMetaData(GetMetaDataRequest request) 
    {
		GetMetaDataResponse response = new GetMetaDataResponse();
		response.setAck(AckValue.FAILURE);
		try {
			QueryCondition queryCondition = request.getQueryCondition();
			if (request == null || queryCondition == null) 
				throwInvalidInputException("please input value for query condition");
			
			List<Query> querys = queryCondition.getQuery();
			if (querys == null || querys.size() == 0)
				throwInvalidInputException("please input value for query condition");
			
			List<KeyValuePair> ret = null;	
			for (Query query: querys)
			{
				String queryType = query.getQueryType();	
				String queryValue = query.getQueryValue();
				if  (queryType == null || queryValue == null)
					throwInvalidInputException("queryType & queryValue cannot be null");
				
				if (queryType.equalsIgnoreCase("POLICY_TYPE")) {
					Set<String> policyTypes = PolicyServiceProviderFactory.getPolicyTypes();
					 for (String type: policyTypes)
					 {
						 PolicyTypeProvider policyTypeProvider = PolicyServiceProviderFactory.
							getPolicyTypeProvider(type);
						 ret = policyTypeProvider.getMetaData(queryValue); 
						 if (ret != null)
							 response.getMetadataValue().addAll(ret);
					 }
				} else if (queryType.equalsIgnoreCase("SUBJECT_TYPE")) {
					Set<String> subjectTypes= PolicyServiceProviderFactory.getSubjectTypes();	
					for (String type: subjectTypes)
					{
						SubjectTypeProvider subjectTypeProvider = PolicyServiceProviderFactory.
							getSubjectTypeProvider(type);
						ret = subjectTypeProvider.getMetaData(queryValue);
						if (ret != null)
							 response.getMetadataValue().addAll(ret);
					}
				} else if (queryType.equalsIgnoreCase("RESOURCE_TYPE")) {
					Set<String> resourceTypes = PolicyServiceProviderFactory.getResourceTypes();
					 for (String type: resourceTypes)
					 {
						 ResourceTypeProvider resourceTypeProvider = PolicyServiceProviderFactory.
							getResourceTypeProvider(type);
						 ret =  resourceTypeProvider.getMetaData(queryValue); 
						 if (ret != null)
							 response.getMetadataValue().addAll(ret);
					 }
				} else {
					throwInvalidInputException("only the following Query types are supported: POLICY_TYPE, SUBJECT_TYPE, RESOUCE_TYPE");
				}
			}
			
			response.setAck(AckValue.SUCCESS);
			
		} catch (ServiceException e)
		{
			mapError(response, e);	
		} catch (PolicyProviderException e) {
			mapError(response, e);
		}
		return response;
    }

    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#deleteSubjectGroups(org.ebayopensource.turmeric.security.v1.services.DeleteSubjectGroupsRequest)
     */
	@Override
    public DeleteSubjectGroupsResponse deleteSubjectGroups(DeleteSubjectGroupsRequest request) {
    	DeleteSubjectGroupsResponse response = new DeleteSubjectGroupsResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
    		if (request == null || 
    				request.getSubjectGroupKey() == null || 
    				request.getSubjectGroupKey().isEmpty())
    			throwInvalidInputException("please input value for subject group");
    		
    		SubjectKey loginSubject = getLoginSubject();
    		for (SubjectGroupKey key: request.getSubjectGroupKey())
    		{
    			SubjectGroupKey subjectGroupKey = deleteSubjectGroup(key) ;
    			if (subjectGroupKey != null)
    				deleteProvisioningPolicy(subjectGroupKey, loginSubject);
    			
    			audit(subjectGroupKey, deleteType, loginSubject);
    			
    		}
    		response.setAck(AckValue.SUCCESS);
    		
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	return response;
    }    

    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#updatePolicy(org.ebayopensource.turmeric.security.v1.services.UpdatePolicyRequest)
     */
	@Override
    public UpdatePolicyResponse updatePolicy(UpdatePolicyRequest request) {
    	UpdatePolicyResponse response = new UpdatePolicyResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
    		if (request == null || request.getPolicy() == null) {
    			throwInvalidInputException("please input value for policy");
    		}
    		if(request.getUpdateMode() == null) {
    			throwInvalidInputException("please input value for update mode");
    		}
    		
    		Policy inputPolicy = request.getPolicy();
    		
    		Policy currPolicy = validatePolicyInfo(inputPolicy);
    		if (currPolicy == null )
				throwInvalidInputException("the Policy does not exist");
    		
    		String oldName = currPolicy.getPolicyName();
    		SubjectKey loginSubject = getLoginSubject();
    		PolicyKey policyKey = updatePolicy(inputPolicy, currPolicy, request.getUpdateMode(), loginSubject);	
    		audit(policyKey, updateType, loginSubject);
    		
    		if (oldName != null && !oldName.equals(policyKey.getPolicyName()))
    		{
    			updateProvisioningPolicy( 
    					oldName,policyKey, loginSubject) ;
    		}
    		response.setAck(AckValue.SUCCESS);
    		
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	
    	return response;
    }
    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#findSubjects(org.ebayopensource.turmeric.security.v1.services.FindSubjectsRequest)
     */
	@Override
    public FindSubjectsResponse findSubjects(FindSubjectsRequest request) {
    	FindSubjectsResponse response = new FindSubjectsResponse();
    	response.setAck(AckValue.FAILURE);
    	
    	try {
    		Iterator<SubjectKey> iter = request.getSubjectQuery().getSubjectKey().iterator();
    		Map<Long, Subject> result = new HashMap<Long, Subject>();
    		while (iter.hasNext())
    		{
    			SubjectKey key = iter.next();
    			
    			Map<Long, Subject> map = findSubject(key);
    			if (map != null && !map.isEmpty())
    				result.putAll(map);
    		}
    		
    		Iterator<Entry<Long,Subject>> subjectIter = result.entrySet().iterator();
    		while (subjectIter.hasNext())
    		{
    			Entry<Long,Subject>  entry= subjectIter.next();
    			Subject subject = entry.getValue();
    			Utils.setSubjectId(subject, entry.getKey());
    			response.getSubjects().add(subject);
    		}
    		response.setAck(AckValue.SUCCESS);
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	
    	return response;
    }

    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#createSubjects(org.ebayopensource.turmeric.security.v1.services.CreateSubjectsRequest)
     */
	@Override
    public CreateSubjectsResponse createSubjects(CreateSubjectsRequest request) {
    	CreateSubjectsResponse response = new CreateSubjectsResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
    		if (request == null || 
    				request.getSubjects() == null || 
    				request.getSubjects().isEmpty())
    			throwInvalidInputException("please input value for subject");
    		
    		SubjectKey loginSubject = getLoginSubject();
    		for (Subject subject: request.getSubjects())
    		{
    			SubjectKey key  = createSubject(subject, false,  loginSubject);
    			audit(key, createType, loginSubject);
    	    	response.getSubjectIds().add(key.getSubjectId());
    		}
    		
    		response.setAck(AckValue.SUCCESS);
    		
    	} catch (ServiceException e){
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	return response;
    }
 
	/**
	 * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#getEntityHistory(org.ebayopensource.turmeric.security.v1.services.GetEntityHistoryRequest)
	 */
	@Override
	public GetEntityHistoryResponse getEntityHistory(
			GetEntityHistoryRequest request) {
		GetEntityHistoryResponse response = new GetEntityHistoryResponse();
		response.setAck(AckValue.FAILURE);
		try {
			if (request == null)
				throwInvalidInputException("please input value for search criteria");

			XMLGregorianCalendar startDate = request.getStartDate();
			XMLGregorianCalendar endDate = request.getEndDate();
			if (startDate == null || endDate == null
					|| startDate.compare(endDate) > 0)
				throwInvalidInputException("please input value for valid start/end date");

			if ((request.getSubjectKey() == null || request.getSubjectKey()
					.isEmpty())
					&& (request.getSubjectGroupKey() == null || request
							.getSubjectGroupKey().isEmpty())
					&& (request.getResourceKey() == null || request
							.getResourceKey().isEmpty())
					&& (request.getPolicyKey() == null || request
							.getPolicyKey().isEmpty())
					&& (request.getOperationKey() == null || request
									.getOperationKey().isEmpty()))
				throwInvalidInputException("please input value for search info for subject, subject group, resource, operation or policy");

			List<EntityHistory> retList = new ArrayList<EntityHistory>();
			for (PolicyKey policyKey : request.getPolicyKey()) {
				List<EntityHistory> list = getEntityHistory(policyKey,
						startDate, endDate);
				if (list != null)
					retList.addAll(list);
			}

			for (ResourceKey resourceKey : request.getResourceKey()) {
				List<EntityHistory> list = getEntityHistory(resourceKey,
						startDate, endDate);
				if (list != null)
					retList.addAll(list);
			}
			
			for (OperationKey operationKey : request.getOperationKey()) {
				List<EntityHistory> list = getEntityHistory(operationKey,
						startDate, endDate);
				if (list != null)
					retList.addAll(list);
			}

			for (SubjectGroupKey subjectGroupKey : request.getSubjectGroupKey()) {
				List<EntityHistory> list = getEntityHistory(subjectGroupKey,
						startDate, endDate);
				if (list != null)
					retList.addAll(list);
			}

			for (SubjectKey subjectKey : request.getSubjectKey()) {
				List<EntityHistory> list = getEntityHistory(subjectKey,
						startDate, endDate);
				if (list != null)
					retList.addAll(list);
			}

			Collections.sort(retList, new Comparator<EntityHistory>() {
				public int compare(EntityHistory a, EntityHistory b) {
					return a.getAuditDate().compare(b.getAuditDate());
				}
			});

			response.getEntityHistories().addAll(retList);
			response.setAck(AckValue.SUCCESS);

		} catch (ServiceException e) {
			mapError(response, e);
		} catch (PolicyProviderException e) {
			mapError(response, e);
		}
		return response;
	}

    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#updateSubjectGroups(org.ebayopensource.turmeric.security.v1.services.UpdateSubjectGroupsRequest)
     */
	@Override
    public UpdateSubjectGroupsResponse updateSubjectGroups(UpdateSubjectGroupsRequest request) {
    	UpdateSubjectGroupsResponse response = new UpdateSubjectGroupsResponse();
    	try {
    		if (request == null ||
    				request.getSubjectGroups() == null ||
    				request.getSubjectGroups().isEmpty()) {
    			throwInvalidInputException("please input value for subject group");    			
    		}
    		if (request.getUpdateMode() == null) {
    			throwInvalidInputException("please input value for update mode");
    		}
    		
    		UpdateMode mode = request.getUpdateMode();
    		for (SubjectGroup inputSubjectGroup: request.getSubjectGroups())
    		{
    			SubjectGroup currSubjectGroup = validateSubjectGroupInfo(inputSubjectGroup);
    			if (currSubjectGroup == null)
    				throwInvalidInputException("the Subject group does not exist");
    				
    			String oldName = currSubjectGroup.getSubjectGroupName();
    			SubjectKey loginSubject = getLoginSubject();
    			SubjectGroupKey subjectGroupKey = updateSubjectGroup(inputSubjectGroup, currSubjectGroup,  mode, loginSubject);
    			audit(subjectGroupKey, updateType, loginSubject);
    			
    			if (oldName != null && !oldName.equals(subjectGroupKey.getSubjectGroupName()))
    			{
    				updateProvisioningPolicy(oldName,subjectGroupKey, loginSubject) ;
    			}
    		}
    		response.setAck(AckValue.SUCCESS);
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
    	}
    	return response;
    }
    
    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#createSubjectGroups(org.ebayopensource.turmeric.security.v1.services.CreateSubjectGroupsRequest)
     */
	@Override
    public CreateSubjectGroupsResponse createSubjectGroups(CreateSubjectGroupsRequest request) {
    	CreateSubjectGroupsResponse response = new CreateSubjectGroupsResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
    		if (request == null || 
    				request.getSubjectGroups() == null ||
    				request.getSubjectGroups().isEmpty())
    			throwInvalidInputException("please input value for subject group");
    		
    		
    		for (SubjectGroup subjectGroup : request.getSubjectGroups())
    		{
    			if (subjectGroup == null)
    				continue;
    			
    			SubjectGroup currSubjectGroup = validateSubjectGroupInfo(subjectGroup);
    			if (currSubjectGroup != null)
    				throwInvalidInputException("the subject group already exists");
    			SubjectKey loginSubject = getLoginSubject();
    			SubjectGroupKey subjectGroupKey= createSubjectGroup(subjectGroup,  loginSubject);
				createProvisioningPolicy(subjectGroupKey, loginSubject);
				
				SubjectTypeProvider provider = PolicyServiceProviderFactory.
					getSubjectTypeProvider(subjectGroupKey.getSubjectType());
				provider.audit(subjectGroupKey, createType,loginSubject);
			
				response.getSubjectGroupIds().add(subjectGroupKey.getSubjectGroupId());
    		}
    		response.setAck(AckValue.SUCCESS);
    		
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	return response;
    }
    

    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#updateResources(org.ebayopensource.turmeric.security.v1.services.UpdateResourcesRequest)
     */
	@Override
    public UpdateResourcesResponse updateResources(UpdateResourcesRequest request) {
    	UpdateResourcesResponse response = new UpdateResourcesResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
    		if (request == null || 
    				request.getResources() == null ||
    				request.getResources().isEmpty()) {
    			throwInvalidInputException("please input value for resource");    			
    		}
    		if(request.getUpdateMode() == null ) {
    			throwInvalidInputException("please input value for update mode");
    		}
    		
    		UpdateMode updateMode = request.getUpdateMode();
    		for (Resource resource: request.getResources())
    		{
    			SubjectKey loginSubject = getLoginSubject();
    			ResourceKey key =updateResource(resource, updateMode , loginSubject);
    			audit(key, updateType, loginSubject);
    		}
    	
    		response.setAck(AckValue.SUCCESS);
    		
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	return response;
    }
    

    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#getOperations(org.ebayopensource.turmeric.security.v1.services.GetOperationsRequest)
     */
	@Override
    public GetOperationsResponse getOperations(GetOperationsRequest request) {
    	GetOperationsResponse response = new GetOperationsResponse();
    	response.setAck(AckValue.FAILURE);
    	try{
    		if (request == null)
    			throwInvalidInputException("please input value for operation");
    		
    		List<ResourceKey> inputResourceList = request.getResourceKey();
    		List<OperationKey> inputOperationList = request.getOperationKey();
    		if ((inputResourceList == null || inputResourceList.isEmpty()) && 
    			(inputOperationList == null || inputOperationList.isEmpty()))
    			throwInvalidInputException("please input value for resource and operation");
    		
    		Map<Long, Operation> resultMap = new HashMap<Long,Operation>();
    		if (inputOperationList != null && !inputOperationList.isEmpty())
    		{
    			for (OperationKey operationKey:inputOperationList)
    			{
    				Operation op = getOperation(operationKey);
    				if (op != null)
    					resultMap.put(op.getOperationId(), op);
    			}
    		}
    		
    		Map<Long, Resource> resourceMap = new HashMap<Long,Resource>();
    		if (inputResourceList != null && !inputResourceList.isEmpty())
    		{
    			for (ResourceKey resourceKey: inputResourceList)
    			{
    				Map<Long, Resource> map = getResource(resourceKey);
    				if (map != null && !map.isEmpty())
    					resourceMap.putAll(map);
    			}
    		}
    		
    		if (!resourceMap.isEmpty())
    		{
    			Iterator<Entry<Long, Resource>> resourceIter = resourceMap.entrySet().iterator();
        		while (resourceIter.hasNext())
        		{
        			Entry<Long, Resource> entry = resourceIter.next();
        			Resource resource = entry.getValue();
        			List<Operation> list = resource.getOperation();
        			if (list != null && !list.isEmpty())
        			{
        				Iterator<Operation> iterOp = list.iterator();
        				while (iterOp.hasNext())
        				{
        					Operation op = iterOp.next();
        					resultMap.put(op.getOperationId(), op);
        				}
        			}
        		}
    		}
    	    		
    		response.getOperations().addAll(resultMap.values());
    		response.setAck(AckValue.SUCCESS);
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	return response;
    }
    
    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#deleteSubjects(org.ebayopensource.turmeric.security.v1.services.DeleteSubjectsRequest)
     */
	@Override
    public DeleteSubjectsResponse deleteSubjects(DeleteSubjectsRequest request) {
    	DeleteSubjectsResponse response = new DeleteSubjectsResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
    		if (request == null ||
    				request.getSubjectKey() == null ||
    				request.getSubjectKey().isEmpty())
    			throwInvalidInputException("please input value for subject");
    		
    		SubjectKey loginSubject = getLoginSubject();
    		for (SubjectKey subjectKey: request.getSubjectKey())
    		{
    			deleteSubject(subjectKey);
    			audit(subjectKey,deleteType, loginSubject);
    		}
    		
    		response.setAck(AckValue.SUCCESS);	
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	
		return response;
    }

    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#validatePolicy(org.ebayopensource.turmeric.security.v1.services.ValidatePolicyRequest)
     */
	@Override
    public ValidatePolicyResponse validatePolicy(ValidatePolicyRequest request) 
    {
    	ValidatePolicyResponse response = new ValidatePolicyResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
	    	if ( request == null || request.getScope() == null || request.getPolicy() == null)
				throwInvalidInputException("please input policy and scope");
			
	    	Policy policy = request.getPolicy();
	    	PolicyTypeProvider provider = PolicyServiceProviderFactory.getPolicyTypeProvider(policy.getPolicyType().toString());
			boolean isValid = provider.validatePolicy(policy, request.getScope());
			
			response.setValidationStatus(isValid);
			response.setAck(AckValue.SUCCESS);
		} catch (ServiceException e) {
			mapError(response, e);
		} catch (PolicyProviderException e) {
			mapError(response, e);
		}
		
		return response;
    }
 
    /**
     * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#deleteResources(org.ebayopensource.turmeric.security.v1.services.DeleteResourcesRequest)
     */
	@Override
    public DeleteResourcesResponse deleteResources(DeleteResourcesRequest request) {
    	DeleteResourcesResponse response = new DeleteResourcesResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
    		if (request == null || 
    				request.getResourceKey() == null ||
    				request.getResourceKey().isEmpty())
    			throwInvalidInputException("please input value for resource");
    		SubjectKey loginSubject = getLoginSubject();
    		for (ResourceKey key: request.getResourceKey())
    		{
    			deleteResource(key);
    			audit(key, deleteType, loginSubject);
    		}
    		
    		response.setAck(AckValue.SUCCESS);
    		
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	return response;
    }
    

	

	
	/**
	 * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#findSubjectGroups(org.ebayopensource.turmeric.security.v1.services.FindSubjectGroupsRequest)
	 */
	@Override
	public FindSubjectGroupsResponse findSubjectGroups(FindSubjectGroupsRequest request) {
		FindSubjectGroupsResponse response = new FindSubjectGroupsResponse();
		response.setAck(AckValue.FAILURE);
		try{
			if (request == null || 
				request.getSubjectGroupQuery() == null ||
				request.getSubjectGroupQuery().getSubjectGroupKey() == null)
				throwInvalidInputException("please input value for subject group query");
			
			Map<Long, SubjectGroup> result = new HashMap<Long, SubjectGroup>();
			Iterator<SubjectGroupKey> iter = request.getSubjectGroupQuery().getSubjectGroupKey().iterator();
			while (iter.hasNext())
			{
				SubjectGroupKey key = iter.next();
				Map<Long, SubjectGroup> map = findSubjectGroup(key);
				if (map == null || map.isEmpty())
					continue;
				
				result.putAll(map);
			}
			
			// check if subject group's subjects to be fetched too
			boolean includeMember = request.getSubjectGroupQuery().isIncludeSubjects();
			
			Iterator<Entry<Long,SubjectGroup>> sgIter = result.entrySet().iterator();
			while (sgIter.hasNext())
			{
				Entry<Long,SubjectGroup> entrySG = sgIter.next();
				Long subjectGroupId = entrySG.getKey();
				SubjectGroup sg = entrySG.getValue();
				if (includeMember)
				{
					SubjectGroupKey key = new SubjectGroupKey();
					key.setSubjectGroupId(subjectGroupId);
					key.setSubjectType(sg.getSubjectType());
					Map<Long, Subject> map= getSubjects(key);
					sg.getSubject().addAll(map.values());
				}
				
				Utils.setSubjectGroupId(sg, subjectGroupId);
				response.getSubjectGroups().add(sg);
			}
			response.setAck(AckValue.SUCCESS);
		} catch (ServiceException e)
		{
			mapError(response, e);
		} catch (PolicyProviderException e) {
			mapError(response, e);
		}
		return response;
	}


	/**
	 * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#disablePolicy(org.ebayopensource.turmeric.security.v1.services.DisablePolicyRequest)
	 */
	@Override
	public DisablePolicyResponse disablePolicy(
			DisablePolicyRequest request) {
		
		DisablePolicyResponse response = new DisablePolicyResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
    		if (request == null || request.getPolicyKey() == null) {
    			throwInvalidInputException("please input value for policy key");
    		}
    			
    		PolicyKey inputPolicyKey = request.getPolicyKey();
    		
    		Policy currPolicy = getPolicyInfo(inputPolicyKey);
    		if (currPolicy == null )
				throwInvalidInputException("the Policy does not exist");
			
    		PolicyTypeProvider provider = PolicyServiceProviderFactory
    				.getPolicyTypeProvider(currPolicy.getPolicyType());
    		
    		currPolicy.setActive(Boolean.FALSE);
    		SubjectKey loginSubject = getLoginSubject();
    		PolicyKey policyKey = provider.updatePolicy(
    				currPolicy, 
    				null,
    				loginSubject
    				);
    		
    		audit(policyKey, updateType, loginSubject);
    		
    		response.setAck(AckValue.SUCCESS);
    		
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	
    	return response;
	}


	/**
	 * @see org.ebayopensource.turmeric.policyservice.intf.PolicyService#enablePolicy(org.ebayopensource.turmeric.security.v1.services.EnablePolicyRequest)
	 */
	@Override
	public EnablePolicyResponse enablePolicy(
			EnablePolicyRequest request) {
		EnablePolicyResponse response = new EnablePolicyResponse();
    	response.setAck(AckValue.FAILURE);
    	try {
    		if (request == null || request.getPolicyKey() == null) {
    			throwInvalidInputException("please input value for policy key");
    		}
    			
    		SubjectKey loginSubject = getLoginSubject();
    		PolicyKey policyKey = enablePolicy(request.getPolicyKey(), getLoginSubject());
    		
    		audit(policyKey, updateType, loginSubject);
    		response.setAck(AckValue.SUCCESS);
    		
    	} catch (ServiceException e)
    	{
    		mapError(response, e);
    	} catch (PolicyProviderException e) {
    		mapError(response, e);
		}
    	
    	return response;
	}

}
