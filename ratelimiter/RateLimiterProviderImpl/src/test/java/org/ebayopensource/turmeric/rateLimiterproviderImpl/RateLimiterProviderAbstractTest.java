/**
 * 
 */
package org.ebayopensource.turmeric.rateLimiterproviderImpl;

import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.security.v1.services.Condition;
import org.ebayopensource.turmeric.security.v1.services.EffectType;
import org.ebayopensource.turmeric.security.v1.services.Expression;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesRequest;
import org.ebayopensource.turmeric.security.v1.services.FindPoliciesResponse;
import org.ebayopensource.turmeric.security.v1.services.IsRateLimitedRequest;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.PolicyOutputSelector;
import org.ebayopensource.turmeric.security.v1.services.PolicySet;
import org.ebayopensource.turmeric.security.v1.services.PrimitiveValue;
import org.ebayopensource.turmeric.security.v1.services.Query;
import org.ebayopensource.turmeric.security.v1.services.QueryCondition;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Resources;
import org.ebayopensource.turmeric.security.v1.services.Rule;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.ebayopensource.turmeric.security.v1.services.Subjects;
import org.ebayopensource.turmeric.security.v1.services.SupportedPrimitive;
import org.ebayopensource.turmeric.security.v1.services.Target;

/**
 * The Class RateLimiterProviderAbstractTest.
 *
 * @author gbaal
 */
public abstract class RateLimiterProviderAbstractTest {
	
	/** The VERSION. */
	final String VERSION = "1.0";
	
	/** The WHITELIST. */
	final String WHITELIST = "WHITELIST";
	
	/** The RL. */
	final String RL = "RL";
	
	/** The BLACKLIST. */
	final String BLACKLIST = "BLACKLIST";

	/**
	 * Gets the bL find policies request.
	 *
	 * @return the bL find policies request
	 */
	protected FindPoliciesRequest getBLFindPoliciesRequest() {
		PolicyKey policyKey = new PolicyKey();
		policyKey.setPolicyType(BLACKLIST);
		FindPoliciesRequest policyRequest = new FindPoliciesRequest();
		policyRequest.getPolicyKey().add(policyKey);
		policyRequest.setOutputSelector(PolicyOutputSelector.ALL);
		QueryCondition queryCondition = new QueryCondition();
		Query query = new Query();
		query.setQueryType("ActivePoliciesOnly");
		query.setQueryValue("TRUE");
		queryCondition.getQuery().add(query);
		policyRequest.setQueryCondition(queryCondition);
		return policyRequest;
	}

	/**
	 * Gets the wL find policies request.
	 *
	 * @return the wL find policies request
	 */
	protected FindPoliciesRequest getWLFindPoliciesRequest() {
		PolicyKey policyKey = new PolicyKey();
		policyKey.setPolicyType(WHITELIST);
		FindPoliciesRequest policyRequest = new FindPoliciesRequest();
		policyRequest.getPolicyKey().add(policyKey);
		policyRequest.setOutputSelector(PolicyOutputSelector.ALL);
		QueryCondition queryCondition = new QueryCondition();
		Query query = new Query();
		query.setQueryType("ActivePoliciesOnly");
		query.setQueryValue("TRUE");
		queryCondition.getQuery().add(query);
		policyRequest.setQueryCondition(queryCondition);
		return policyRequest;
	}

	/**
	 * Gets the rL find policies request.
	 *
	 * @return the rL find policies request
	 */
	protected FindPoliciesRequest getRLFindPoliciesRequest() {
		PolicyKey policyKey = new PolicyKey();
		policyKey.setPolicyType(RL);
		FindPoliciesRequest policyRequest = new FindPoliciesRequest();
		policyRequest.getPolicyKey().add(policyKey);
		policyRequest.setOutputSelector(PolicyOutputSelector.ALL);
		QueryCondition queryCondition = new QueryCondition();
		Query query = new Query();
		query.setQueryType("ActivePoliciesOnly");
		query.setQueryValue("TRUE");
		queryCondition.getQuery().add(query);
		policyRequest.setQueryCondition(queryCondition);
		return policyRequest;
	}

	/**
	 * Generate is rate limited request.
	 *
	 * @param rateLimitRequest the rate limit request
	 * @return the checks if is rate limited request
	 */
	protected IsRateLimitedRequest generateIsRateLimitedRequest(
			IsRateLimitedRequest rateLimitRequest) {
		rateLimitRequest.setOperationName("performSearch");
		rateLimitRequest.setResourceName("ServiceName");
		rateLimitRequest.setResourceType("SERVICE");

		return rateLimitRequest;
	}

	// create BlackList
	/**
	 * Generate bl find policies response.
	 *
	 * @return the find policies response
	 */
	protected FindPoliciesResponse generateBLFindPoliciesResponse() {
		FindPoliciesResponse findPoliciesResponse = initFindPoliciesResponse();
		findPoliciesResponse.getPolicySet().getPolicy().addAll(
				(generateBLPolicySet(BLACKLIST)));
		return findPoliciesResponse;
	}

	// create WHITELIST
	/**
	 * Generate wl find policies response.
	 *
	 * @return the find policies response
	 */
	protected FindPoliciesResponse generateWLFindPoliciesResponse() {
		FindPoliciesResponse findPoliciesResponse = initFindPoliciesResponse();
		findPoliciesResponse.getPolicySet().getPolicy().addAll(
				(generateBLPolicySet(WHITELIST)));
		return findPoliciesResponse;
	}

	// create RL
	/**
	 * Generate rl find policies response.
	 *
	 * @return the find policies response
	 */
	protected FindPoliciesResponse generateRLFindPoliciesResponse() {
		FindPoliciesResponse findPoliciesResponse = initFindPoliciesResponse();
		findPoliciesResponse.getPolicySet().getPolicy().addAll(
				(generateBLPolicySet(RL)));
		return findPoliciesResponse;
	}

	private FindPoliciesResponse initFindPoliciesResponse() {
		FindPoliciesResponse findPoliciesResponse = new FindPoliciesResponse();
		findPoliciesResponse.setAck(AckValue.SUCCESS);
		findPoliciesResponse.setPolicySet(new PolicySet());
		findPoliciesResponse.setVersion(VERSION);
		return findPoliciesResponse;
	}

	private List<Policy> generateBLPolicySet(String policyType) {
		List<Policy> policies = new ArrayList<Policy>();

		Policy policy = initPolicy(policyType);
		policy.setTarget(initTarget(policyType));
		if (WHITELIST.equalsIgnoreCase(policyType)
				|| RL.equalsIgnoreCase(policyType)) {
			Subject subject = new Subject();
			subject.setCreatedBy("test");
			subject.setDescription("desc dummy");
			subject.setSubjectName("1.1.1.1");
			subject.setSubjectType("IP");
			policy.getTarget().getSubjects().getSubject().add(subject);
		}
		policy.getRule().addAll(initRule());
		policies.add(policy);

		return policies;
	}

	private List<Rule> initRule() {
		List<Rule> rules = new ArrayList<Rule>();
		// rules per user ip or group
		Rule rule = new Rule();
		Condition condition = new Condition();
		Expression expression = new Expression();
		PrimitiveValue primitive = new PrimitiveValue();
		primitive.setType(SupportedPrimitive.STRING);
		primitive.setValue("10.2.124.3:hits >2");
		expression.setPrimitiveValue(primitive);
		condition.setExpression(expression);
		rule.setCondition(condition);
		rule.setEffect(EffectType.FLAG);
		rule.setEffectDuration(3000l);
		rule.setRolloverPeriod(20000l);
		rules.add(rule);

		Rule rule2 = new Rule();
		Condition condition2 = new Condition();
		Expression expression2 = new Expression();
		PrimitiveValue primitive2 = new PrimitiveValue();
		primitive2.setType(SupportedPrimitive.STRING);
		primitive2.setValue("10.2.124.5:hits >1");
		expression2.setPrimitiveValue(primitive2);
		condition2.setExpression(expression2);
		rule2.setCondition(condition2);
		rule2.setEffect(EffectType.CHALLENGE);
		rule2.setEffectDuration(5000l);
		rule2.setRolloverPeriod(20000l);
		rules.add(rule2);
		// HITS to all
		Rule rule3 = new Rule();
		Condition condition3 = new Condition();
		Expression expression3 = new Expression();
		PrimitiveValue primitive3 = new PrimitiveValue();
		primitive3.setType(SupportedPrimitive.STRING);
		primitive3.setValue("HITS > 8");
		expression3.setPrimitiveValue(primitive3);
		condition3.setExpression(expression3);
		rule3.setCondition(condition3);
		rule3.setEffect(EffectType.BLOCK);
		rule3.setEffectDuration(8000l);
		rule3.setRolloverPeriod(20000l);
		rules.add(rule3);

		// serviceCount
		Rule rule4 = new Rule();
		Condition condition4 = new Condition();
		Expression expression4 = new Expression();
		PrimitiveValue primitive4 = new PrimitiveValue();
		primitive4.setType(SupportedPrimitive.STRING);
		primitive4.setValue("ServiceName:checkout.count > 15");
		expression4.setPrimitiveValue(primitive4);
		condition4.setExpression(expression4);
		rule4.setCondition(condition4);
		rule4.setEffect(EffectType.BLOCK);
		rule4.setEffectDuration(8000l);
		rule4.setRolloverPeriod(20000l);
		rules.add(rule4);
		return rules;
	}

	private Target initTarget(String policyType) {
		Target target = new Target();

		target.setResources(initResources());
		target.setSubjects(initSubjects(policyType));
		return target;
	}

	private Subjects initSubjects(String policyType) {
		Subjects subjects = new Subjects();
		subjects.getSubject().addAll(initSubjectsList(policyType));
		subjects.getSubjectGroup().addAll(initSubjectGroup(policyType));
		return subjects;
	}

	private List<SubjectGroup> initSubjectGroup(String policyType) {
		List<SubjectGroup> groups = new ArrayList<SubjectGroup>();
		for (int i = 0; i < 2; i++) {
			SubjectGroup group = new SubjectGroup();
			group.setCreatedBy("test");
			group.setSubjectGroupName("group".concat(policyType));
			group.setSubjectType("IP");
			groups.add(group);
		}
		return groups;
	}

	/**
	 * 10.2.124.1 to 10.2.124.5 WL 10.2.124.3 to 10.2.124.12 RL 10.2.124.100 to
	 * 10.2.124.5 BL
	 * 
	 * @param policyType
	 * @return
	 */
	private List<Subject> initSubjectsList(String policyType) {
		List<Subject> subjects = new ArrayList<Subject>();
		int cnt = (RL.equalsIgnoreCase(policyType)) ? 11 : 6;
		for (int i = 0; i < cnt; i++) {
			Subject subject = new Subject();
			subject.setCreatedBy("test");
			subject.setDescription("desc".concat(policyType) + i);
			subject.setSubjectName(generateIp(policyType, i));
			subject.setSubjectType("IP");
			subjects.add(subject);

		}
		return subjects;
	}

	// blacklist Ip start at 10.2.124.100 and up

	private String generateIp(String policyType, int i) {
		int startip = 1;
		if (BLACKLIST.equalsIgnoreCase(policyType)) {
			startip = 100;
		} else if (RL.equalsIgnoreCase(policyType)) {
			startip = 3;
		}
		String ip = "10.2.124." + (startip + i);
		return ip;
	}

	private Resources initResources() {
		Resources resources = new Resources();

		Resource resource = new Resource();
		resource.setResourceId(1l);
		resource.setResourceName("ServiceName");
		resource.setResourceType("SERVICE");
		Operation operation = new Operation();
		operation.setDescription("desc");
		operation.setOperationName("checkout");
		operation.setResourceId(1l);
		resource.getOperation().add(operation);
		resources.getResource().add(resource);
		return resources;
	}

	private Policy initPolicy(String type) {
		Policy policy = new Policy();
		policy.setActive(true);
		policy.setPolicyType(type);
		policy.setLastModifiedBy("TEST");
		return policy;
	}

}
