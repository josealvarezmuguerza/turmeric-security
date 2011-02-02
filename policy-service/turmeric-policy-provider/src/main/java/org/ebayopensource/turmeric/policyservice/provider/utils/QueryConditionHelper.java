/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.policyservice.provider.utils;

import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.turmeric.security.v1.services.Query;
import org.ebayopensource.turmeric.security.v1.services.QueryCondition;

public class QueryConditionHelper {

    private QueryCondition queryCondition = null;

    public QueryConditionHelper(QueryCondition queryCondition) {
        this.queryCondition = queryCondition;
    }

    private String getQueryValue(String queryType) {
        List<Query> queryList = getQueryList();
        if (queryList == null || queryList.size() == 0)
            return null;
        for (Query query : queryList)
            if (queryType.equals(query.getQueryType()))
                return query.getQueryValue();
        return null;
    }

    private List<Query> getQueryList() {
        if (queryCondition == null) {
            return new ArrayList<Query>();
        }
        return queryCondition.getQuery();
    }

    private boolean isQueryTypeSpecified(String queryType) {
        return getQueryValue(queryType) != null;
    }

    public String getSearchScope() {
        String value = getQueryValue("SubjectSearchScope");
        return value == null ? "TARGET" : value;
    }

    public boolean isSearchScopeSpecified() {
        return isQueryTypeSpecified("SubjectSearchScope");
    }

    public String getEffect() {
        return getQueryValue("Effect");
    }

    public boolean isEffectSpecified() {
        return isQueryTypeSpecified("Effect");
    }

    public boolean isRuleConditionFormatSpecified() {
        return isQueryTypeSpecified("RuleConditionFormat");
    }

    public boolean isRuleConditionExpanded() {
        String value = getQueryValue("RuleConditionFormat");
        return value == null ? false : "OPERATIONID".equalsIgnoreCase(value);
    }

    public boolean isTargetExpandResourcesSpecified() {
        return isQueryTypeSpecified("ExpandResourceLevelPolicies");
    }

    public boolean isTargetResourcesLevelExpanded() {
        String value = getQueryValue("ExpandResourceLevelPolicies");
        return value == null ? false : "TRUE".equalsIgnoreCase(value);
    }

    public boolean isIncludeOperationLevelPoliciesSpecified() {
        return isQueryTypeSpecified("IncludeOperationLevelPolicies");
    }

    public boolean isToIncludeOperationLevelPolicies() {
        String value = getQueryValue("IncludeOperationLevelPolicies");
        return value == null ? true : "TRUE".equalsIgnoreCase(value);
    }

    public boolean isIdMasked() {
        String value = getQueryValue("MaskedIds");
        return value == null ? false : value.equalsIgnoreCase("TRUE");
    }

    public boolean isActivePoliciesRequestedOnly() {
        String value = getQueryValue("ActivePoliciesOnly");
        return value == null ? true : value.equalsIgnoreCase("TRUE");
    }
}

