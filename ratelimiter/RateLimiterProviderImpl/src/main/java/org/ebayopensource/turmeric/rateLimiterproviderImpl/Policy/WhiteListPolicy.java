/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy;

import java.util.List;

import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedResponse;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.services.ratelimiterservice.impl.RateLimiterException;

public class WhiteListPolicy extends AbstractPolicy {

	private static List<Policy> whitelistPolicy;

	public WhiteListPolicy(IsRateLimitedRequest rlRequest) {
		super(rlRequest);
	}

	@Override
	public boolean isExcluded() {
		return false;
	}

	@Override
	public boolean isIncluded() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return whitelistPolicy == null || whitelistPolicy.isEmpty();
	}

	@Override
	public String getPolicyType() {
		return "WHITELIST";
	}

	@Override
	public IsRateLimitedResponse evaluate(IsRateLimitedResponse response,
			IsRateLimitedRequest rlRequest) throws RateLimiterException {
		return checkWhiteList(response, rlRequest, super.requestSubjects,
				super.requestSubjectGroups);
	}

	private IsRateLimitedResponse checkWhiteList(
			IsRateLimitedResponse response, IsRateLimitedRequest request,
			List<String> subjects, List<String> subjectGroups)
			throws RateLimiterException {

		if (!response.getStatus().equals(RateLimiterStatus.BLOCK)) {
			whitelistPolicy = super.getPolicies();
			// set it to block if its found in the wl it will be set as Serve_ok
			if (whitelistPolicy != null && !whitelistPolicy.isEmpty()) {
				response.setStatus(RateLimiterStatus.BLOCK);
								
				for (Policy p : whitelistPolicy) {
					if (isPolicySubjectGroupValid(p)) {

						// check subjectgroup
						for (SubjectGroup subjectGroup : p.getTarget()
								.getSubjects().getSubjectGroup()) {
							if (subjectGroups.contains(subjectGroup
									.getSubjectGroupName().trim())) {
								response.setStatus(RateLimiterStatus.SERVE_OK);
								// checkRateLimiter(response, request, subjects,
								// domain);
								return response;
							}

						}
						// check subject
						for (Subject wl : p.getTarget().getSubjects()
								.getSubject()) {
							if (subjects.contains(wl.getSubjectName().trim())) {
								response.setStatus(RateLimiterStatus.SERVE_OK);
								// checkRateLimiter(response, request, subjects,
								// domain);
								return response;
							}
						}
					}

				}
			}
		}

		return response;
	}

}
