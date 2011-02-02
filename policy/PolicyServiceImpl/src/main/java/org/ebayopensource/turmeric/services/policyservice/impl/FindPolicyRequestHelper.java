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

import java.util.Date;
import java.util.List;

import org.ebayopensource.turmeric.errorlibrary.turmericpolicy.ErrorConstants;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorUtils;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest;
import org.ebayopensource.turmeric.security.v1.services.OperationKey;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.PolicyOutputSelector;
import org.ebayopensource.turmeric.security.v1.services.Query;
import org.ebayopensource.turmeric.security.v1.services.QueryCondition;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;


class FindPolicyRequestHelper {
	private FindPoliciesRequest m_request;
	private Date m_lastModified;
	
	 FindPolicyRequestHelper(FindPoliciesRequest request) {
		m_request = request;
		if (request.getLastUpdatedDate() != null)
			m_lastModified = request.getLastUpdatedDate().toGregorianCalendar().getTime();
	}
	
	 boolean isTimeBasedRequest() {
		return m_lastModified != null;
	}

	 Date getLastModified() {
		return m_lastModified;
	}

	 List<PolicyKey> getPolicyKeyList() {
		return m_request.getPolicyKey();
	}

	 void validate() throws ServiceException  {
		// check PolicyType list for policyType values
		if (m_request.getPolicyKey() == null || m_request.getPolicyKey().isEmpty())	
				throw new ServiceException(ErrorUtils.createErrorData(
						ErrorConstants.SVC_POLICYSERVICE_INVALID_INPUT_ERROR, 
						ErrorConstants.ERRORDOMAIN.toString(), new Object[]{"Policy key cannot be empty"}));
		
		for(PolicyKey key: m_request.getPolicyKey()) 
		{
			if (key.getPolicyType() == null)
				throw new ServiceException(ErrorUtils.createErrorData(
						ErrorConstants.SVC_POLICYSERVICE_INVALID_INPUT_ERROR, 
						ErrorConstants.ERRORDOMAIN.toString(), new Object[]{"Policy type cannot be empty"}));
		}
		
	}

	 boolean isFiltered() {
		 return
	        isSubjectFiltered() || isSubjectGroupFiltered() ||
	        isOperationFiltered() || isResourceFiltered() ||
	        isEffectSpecified() || isSearchScopeSpecified();

	}

	 boolean isSearchScopeSpecified() {
		return isQueryTypeSpecified( "SubjectSearchScope" );
	}

	 boolean isEffectSpecified() {
		return isQueryTypeSpecified( "Effect" );

	}
	
	  String getEffect() {
	        return getQueryValue( "Effect" );
	    }

	private boolean isQueryTypeSpecified(String queryType) {
		return getQueryValue( queryType ) != null;
    }


	private String getQueryValue(String queryType) {
		List<Query> queryList = getQueryList();
        if ( queryList == null || queryList.size() == 0 )
            return null;
        for ( Query query : queryList )
            if ( queryType.equals( query.getQueryType() ) )
                return query.getQueryValue();
        return null;
	}

	private List<Query> getQueryList() {
		return m_request.getQueryCondition() == null ? null : m_request.getQueryCondition().getQuery();
	}

	 boolean isResourceFiltered() {
		return getResourceList() != null && getResourceList().size() > 0;

	}

	 List<ResourceKey> getResourceList() {
		return m_request.getResourceKey();
	}

	 boolean isOperationFiltered() {
        return getOperationList() != null && getOperationList().size() > 0;
	}

	 List<OperationKey> getOperationList() {
		return m_request.getOperationKey();
	}

	 boolean isSubjectGroupFiltered() {
		return getSubjectGroupList() != null && getSubjectGroupList().size() > 0;
	}

	 List<SubjectGroupKey> getSubjectGroupList() {
		return m_request.getSubjectGroupKey();
	}

	 boolean isSubjectFiltered() {
		 return getSubjectList() != null && getSubjectList().size() > 0;

	}

	 List<SubjectKey> getSubjectList() {
		return m_request.getSubjectKey();
	}
	
	 boolean isEffectFilteredOnly() {
		return !isPolicyKeyLookupOnly() && isEffectSpecified() &&
		!isSubjectFiltered() && !isSubjectGroupFiltered() &&
		!isOperationFiltered() && !isResourceFiltered();
	}
	
	 boolean isPolicyKeyLookupOnly() {
		for ( PolicyKey policyKey : m_request.getPolicyKey() ) {
			if ( policyKey.getPolicyType() != null && ( policyKey.getPolicyName() != null || policyKey.getPolicyId() != null ) )
				return true;
		}
		return false;
	}

	 boolean outputActivePoliciesOnly() {
		String value = getQueryValue( "ActivePoliciesOnly" );
        return value == null ? true : value.equalsIgnoreCase( "TRUE" );

	}

	 boolean outputAll() {
        return m_request.getOutputSelector() == null ? true : m_request.getOutputSelector() == PolicyOutputSelector.ALL;
    }
    
     boolean outputRules() {
        return outputAll() || m_request.getOutputSelector() == PolicyOutputSelector.RULES;
    }
    

     boolean outputResources() {
        return outputAll() || m_request.getOutputSelector() == PolicyOutputSelector.RESOURCES;
    }

     boolean outputSubjects() {
        return outputAll() || m_request.getOutputSelector() == PolicyOutputSelector.SUBJECTS;
    }
    
     boolean outputSubjectGroups() {
        return outputAll() || m_request.getOutputSelector() == PolicyOutputSelector.SUBJECTS || 
        m_request.getOutputSelector() == PolicyOutputSelector.SUBJECTGROUPS;
    }

	  boolean isTargetExpandResourcesSpecified() {
	    return isQueryTypeSpecified( "ExpandResourceLevelPolicies" );
	 }
	 
	  String getSearchScope() {
	        String value = getQueryValue( "SubjectSearchScope" );
	        return value == null ? "TARGET" : value;
	 }


	 boolean findInclusions() {
			String searchScope = getSearchScope();
			return searchScope == null ? true : "TARGET".equalsIgnoreCase( searchScope.trim() ) || "BOTH".equalsIgnoreCase( searchScope.trim() );
	}
	
	 boolean findExclusions() {
		String searchScope = getSearchScope();
		return searchScope == null ? false : "EXCLUDED".equalsIgnoreCase( searchScope.trim() ) || "BOTH".equalsIgnoreCase( searchScope.trim() );
	}

	 QueryCondition getQueryCondition() {
		return m_request.getQueryCondition();
	}
}
