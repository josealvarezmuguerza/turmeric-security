/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy;

import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.util.RateLimiterUtils;
import org.ebayopensource.turmeric.security.v1.services.Condition;
import org.ebayopensource.turmeric.security.v1.services.EffectType;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedResponse;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PrimitiveValue;
import org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus;
import org.ebayopensource.turmeric.security.v1.services.Rule;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.SupportedPrimitive;
import org.ebayopensource.turmeric.services.ratelimiterservice.impl.RateLimiterException;

/**
 * The Class RateLimiterPolicy.
 */
public class RateLimiterPolicy extends AbstractPolicy {
	private List<Policy> rateLimeterPolicy = new ArrayList<Policy>();
	private String currentSubjectOrGroup;
	private RateLimiterStatus currentlimiterStatus;
	private Rule currentRule;

	/**
	 * Instantiates a new rate limiter policy.
	 *
	 * @param rlRequest the rl request
	 */
	public RateLimiterPolicy(IsRateLimitedRequest rlRequest) {
		super(rlRequest);
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.AbstractPolicy#isExcluded()
	 */
	@Override
	public boolean isExcluded() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.AbstractPolicy#isIncluded()
	 */
	@Override
	public boolean isIncluded() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.AbstractPolicy#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return rateLimeterPolicy == null || rateLimeterPolicy.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.AbstractPolicy#getPolicyType()
	 */
	@Override
	public String getPolicyType() {
		return "RL";
	}

	/* (non-Javadoc)
	 * @see org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.AbstractPolicy#evaluate(org.ebayopensource.turmeric.security.v1.services.IsRateLimitedResponse, org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest)
	 */
	@Override
	public IsRateLimitedResponse evaluate(IsRateLimitedResponse response,
			IsRateLimitedRequest rlRequest) throws RateLimiterException {

		try {
			return checkRateLimiter(response, rlRequest, super.requestSubjects,
					super.requestSubjectGroups);
		} catch (Exception e) {
			
		}
		return response;
	}

	private IsRateLimitedResponse checkRateLimiter(
			IsRateLimitedResponse response, IsRateLimitedRequest request,
			List<String> subjects, List<String> subjectGroups) throws Exception {
		// we only allow ok
		if (RateLimiterStatus.SERVE_OK.equals(response.getStatus())) {

			rateLimeterPolicy = super.getPolicies();

			// (super.isFoundInRequest(request, subjects, subjectGroups,
			// rateLimeterPolicy));
			if (rateLimeterPolicy != null && !rateLimeterPolicy.isEmpty()) {
				for (Policy p : rateLimeterPolicy) {
					if (isPolicySubjectGroupValid(p)) {
						// adds resource for service counts
						super.createServiceCounters(p);

						for (SubjectGroup subjectGroup : p.getTarget()
								.getSubjects().getSubjectGroup()) {
							// we only check attributes for Inclusion
							if (!org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.util.Utils
									.isExclusion(subjectGroup)) {
								if (subjectGroups.contains(subjectGroup
										.getSubjectGroupName().trim())) {
									// checkRateLimiter(response, request,
									// subjects,
									// domain);
									response = evaluateAttribute(response,
											subjectGroup.getSubjectGroupName(),
											p);
								}
							}

						}
						for (Subject subject : p.getTarget().getSubjects()
								.getSubject()) {
							// we only check attributes for Inclusion
							if (!org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.util.Utils
									.isExclusion(subject)) {
								if (subjects.contains(subject.getSubjectName()
										.trim())) {
									// checkRateLimiter(response, request,
									// subjects,
									// domain);
									response = evaluateAttribute(response,
											subject.getSubjectName(), p);
								}
							}
						}

						// if its the Highest no need to check other RL
						if (RateLimiterStatus.BLOCK
								.equals(response.getStatus())) {
							super.addToActiveEffects(currentSubjectOrGroup,
									currentRule, currentlimiterStatus);
							return response;
						}
					}
				}
				if (RateLimiterStatus.SERVE_GIF.equals(response.getStatus())) {
					// Implement ratelimiter captcha information here.
				}
			}

		}
		super.addToActiveEffects(currentSubjectOrGroup, currentRule,
				currentlimiterStatus);

		return response;

	}

	// Evaluate condition
	private IsRateLimitedResponse evaluateAttribute(
			IsRateLimitedResponse response, String ipOrSubjectGroup,
			Policy policy) throws Exception {
		Condition condition = null;
		for (Rule rule : policy.getRule()) {
			// // now we have the rollover period
			// super.resetCounter(ipOrSubjectGroup, rule);
			// // try to reset HITS
			// super.resetCounter(HITS, rule);
			condition = rule.getCondition();

			// if null nothing to evaluate
			if (condition != null && condition.getExpression() != null) {
				// if true set the effect or the highest severity
				if (processPrimitiveValue(condition.getExpression()
						.getPrimitiveValue(), ipOrSubjectGroup)) {
					response.setStatus(convertGetTheHigestSeverity(
							rule.getEffect(), response, true));
					// will be use to populate effect duration
					if (!response.getStatus().equals(currentlimiterStatus)) {
						currentlimiterStatus = response.getStatus();

						currentSubjectOrGroup = extractVariable(condition
								.getExpression().getPrimitiveValue().getValue());
						if (currentSubjectOrGroup == null) {
							currentSubjectOrGroup = ipOrSubjectGroup;
						}
						currentRule = rule;

					}

				}
			}

		}
		return response;
	}

	// check primitive value
	private boolean processPrimitiveValue(PrimitiveValue primitiveValue,
			String ipOrSubjectGroup) throws Exception {
		// check if valid
		if (primitiveValue != null
				&& SupportedPrimitive.STRING.equals(primitiveValue.getType())
				&& primitiveValue.getValue() != null) {
			if ((primitiveValue.getValue().contains(":hits") && primitiveValue
					.getValue().contains(ipOrSubjectGroup))
					|| !primitiveValue.getValue().contains(":hits")) {
				return new RateLimiterUtils(ipOrSubjectGroup)
						.getFinalresult(primitiveValue.getValue());
			}
		}

		return false;
	}

	/**
	 * covert EffectType to RateLimiterStatus and get the highest severity.
	 *
	 * @param type the type
	 * @param response the response
	 * @param getTheHighest the get the highest
	 * @return the rate limiter status
	 */
	public RateLimiterStatus convertGetTheHigestSeverity(EffectType type,
			IsRateLimitedResponse response, boolean getTheHighest) {
		RateLimiterStatus rateLimiterStatus = getlimiterStatus(type);
		if (getTheHighest && response.getStatus() != null) {
			// since block is the highest
			if (response.getStatus().equals(RateLimiterStatus.BLOCK)
					|| (rateLimiterStatus.equals(RateLimiterStatus.BLOCK))) {
				rateLimiterStatus = RateLimiterStatus.BLOCK;

			} else if (response.getStatus().equals(RateLimiterStatus.SERVE_GIF)
					|| (rateLimiterStatus.equals(RateLimiterStatus.SERVE_GIF))) {
				rateLimiterStatus = RateLimiterStatus.SERVE_GIF;
				// throws java.lang.NoSuchFieldError: SOFT_LIMIT??
				// } else if (response.getStatus()
				// .equals(RateLimiterStatus.SOFT_LIMIT)
				// || (rateLimiterStatus.equals(RateLimiterStatus.SOFT_LIMIT)))
				// {
				// rateLimiterStatus = RateLimiterStatus.SOFT_LIMIT;
			} else if ("SOFT_LIMIT".equalsIgnoreCase(response.getStatus()
					.value())
					|| "SOFT_LIMIT".equalsIgnoreCase(rateLimiterStatus.value())) {
				rateLimiterStatus = RateLimiterStatus.SOFT_LIMIT;
			} else if (response.getStatus().equals(RateLimiterStatus.FLAG)
					|| (rateLimiterStatus.equals(RateLimiterStatus.FLAG))) {
				rateLimiterStatus = RateLimiterStatus.FLAG;
			} else if (response.getStatus().equals(
					RateLimiterStatus.UNSUPPORTED)
					|| rateLimiterStatus.equals(RateLimiterStatus.UNSUPPORTED)) {
				rateLimiterStatus = RateLimiterStatus.UNSUPPORTED;
			}

		}

		return rateLimiterStatus;
	}

}
