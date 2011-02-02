/**
 * 
 */
package org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.model.RateLimiterPolicyModel;
import org.ebayopensource.turmeric.security.v1.services.EffectType;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Resources;
import org.ebayopensource.turmeric.security.v1.services.Rule;

/**
 * @author gbaal
 * 
 */
public abstract class CounterAbstractPolicy {
	// variables
	// all hits
	public static final String HITS = "HITS";
	// service.operation:count
	public static final String SERVICECOUNT = ".count";
	// 101.12.69.105:hits
	public static final String IPHITS = ":hits";
	private static Map<String, RateLimiterPolicyModel> activeRL;
	private static Map<String, RateLimiterPolicyModel> activeEffect;

	public CounterAbstractPolicy() {
		super();
		// Initialised maps
		getActiveRL();
		getActiveEffects();
	}

	protected Map<String, RateLimiterPolicyModel> getActiveRL() {
		activeRL = (activeRL == null) ? new HashMap<String, RateLimiterPolicyModel>()
				: activeRL;
		return activeRL;
	}

	protected Map<String, RateLimiterPolicyModel> getActiveEffects() {
		activeEffect = (activeEffect == null) ? new HashMap<String, RateLimiterPolicyModel>()
				: activeEffect;
		return activeEffect;
	}

	protected void incrementCounter(String ipOrSubjectGroup,
			RateLimiterPolicyModel rateLimiterPolicyModel) {
		if (!isSpecialVAR(ipOrSubjectGroup)) {
			ipOrSubjectGroup = ipOrSubjectGroup.concat(IPHITS);
		}
		if (ipOrSubjectGroup != null) {
			ipOrSubjectGroup = ipOrSubjectGroup.trim();
		}
		if (rateLimiterPolicyModel != null) {

			if (rateLimiterPolicyModel.getTimestamp() == null)
				rateLimiterPolicyModel.setTimestamp(new Date());
			if (getActiveRL().containsKey(ipOrSubjectGroup)) {
				RateLimiterPolicyModel limiterPolicyModel = getActiveRL().get(
						ipOrSubjectGroup);
				getActiveRL().get(ipOrSubjectGroup).setCount(
						limiterPolicyModel.getCount() + 1);

			} else {
				rateLimiterPolicyModel.setCount(1);
				getActiveRL().put(ipOrSubjectGroup, rateLimiterPolicyModel);
			}
		}

	}

	// check if we need to reset counter base on RolloverPeriod
	protected void resetCounter(String ipOrSubjectGroup, Rule rule) {
		if (!rule.getCondition().getExpression().getPrimitiveValue().getValue()
				.contains(ipOrSubjectGroup)) {
			return;
		}
		if (!isSpecialVAR(ipOrSubjectGroup)) {
			ipOrSubjectGroup = ipOrSubjectGroup.concat(IPHITS);
		}
		if (ipOrSubjectGroup != null) {
			ipOrSubjectGroup = ipOrSubjectGroup.trim();
		}
		RateLimiterPolicyModel limiterPolicyModel = getActiveRL().get(
				ipOrSubjectGroup);
		if (limiterPolicyModel != null && rule != null) {
			if (limiterPolicyModel.getTimestamp() == null) {
				System.err.println("throw error1resetCounter"
						+ ipOrSubjectGroup);
				return;
			}
			if (rule.getRolloverPeriod() == null) {
				System.err.println("throw error resetCounter"
						+ ipOrSubjectGroup);
				return;

			}

			// add the the time it was added to the rolloverPeriod
			Long timeToReset = (limiterPolicyModel.getTimestamp().getTime() + rule
					.getRolloverPeriod());
			// get current date
			java.util.Date date = new java.util.Date();
			// compare if current id after the reset date
			if (date.after(new Date(timeToReset))) {
				if (getActiveRL().containsKey(ipOrSubjectGroup)) {
					// reset it
					getActiveRL().get(ipOrSubjectGroup).setCount(1);
					getActiveRL().get(ipOrSubjectGroup)
							.setTimestamp(new Date());
					getActiveRL().get(ipOrSubjectGroup).setActive(false);
					getActiveRL().get(ipOrSubjectGroup).setEffectDuration(null);
					getActiveRL().get(ipOrSubjectGroup).setEffect(null);
					// since reset remove it to activeEffects
					getActiveEffects().remove(ipOrSubjectGroup);
				}
			}

		}

	}

	// add to database
	protected void addToActiveEffects(String currentSubjectOrGroup, Rule rule,
			RateLimiterStatus currentlimiterStatus) {
		if (currentSubjectOrGroup != null) {
			currentSubjectOrGroup = currentSubjectOrGroup.trim();
		}
		RateLimiterPolicyModel limiterPolicyModel = new RateLimiterPolicyModel();
		limiterPolicyModel.setTimestamp(new Date());
		limiterPolicyModel.setEffectDuration(new Date().getTime()
				+ rule.getEffectDuration());
		limiterPolicyModel.setActive(true);
		limiterPolicyModel.setEffect(currentlimiterStatus);

		getActiveEffects().remove(currentSubjectOrGroup);
		getActiveEffects().put(currentSubjectOrGroup, limiterPolicyModel);
	}

	// checks and reset effect
	protected void checkAllEffects() {
		for (Map.Entry<String, RateLimiterPolicyModel> entry : getActiveEffects()
				.entrySet()) {
			resetEffect(entry.getKey());
		}

	}

	protected void createServiceCounters(Policy policy) {
		if (policy == null)
			return;
		Resources resources = policy.getTarget().getResources();
		if (resources != null) {
			generateServiceCountName(resources, policy.getRule());

		}
		updateData(policy.getRule());
		// deletes all effect where effect duration < now
		checkAllEffects();
	}

	private void updateData(List<Rule> rules) {
		for (String data : getActiveRL().keySet()) {
			RateLimiterPolicyModel model = getActiveRL().get(data);
			Rule rule = null;
			if (model != null) {
				rule = findRule(rules, data);
				if (rule != null) {
					if (model.getRolloverPeriod() == null) {
						getActiveRL().get(data).setRolloverPeriod(
								rule.getRolloverPeriod());
					}
					if (model.getTimestamp() == null) {
						getActiveRL().get(data).setTimestamp(new Date());
					}
					resetCounter(data, rule);
				}
			}

		}

	}

	protected RateLimiterStatus getlimiterStatus(EffectType type) {
		RateLimiterStatus rateLimiterStatus = null;
		// convert type
		switch (type) {
		case ALLOW:
			rateLimiterStatus = RateLimiterStatus.SERVE_OK;
			break;
		case FLAG:
			rateLimiterStatus = RateLimiterStatus.FLAG;
			break;
		case CHALLENGE:
			rateLimiterStatus = RateLimiterStatus.SERVE_GIF;
			break;
		case BLOCK:
			rateLimiterStatus = RateLimiterStatus.BLOCK;
			break;
		case SOFTLIMIT:
			rateLimiterStatus = RateLimiterStatus.SOFT_LIMIT;
			break;
		default:
			rateLimiterStatus = RateLimiterStatus.UNSUPPORTED;
		}
		return rateLimiterStatus;
	}

	private void generateServiceCountName(Resources resources, List<Rule> rules) {
		for (Resource resource : resources.getResource()) {
			if (resource != null && resource.getResourceName() != null
					&& resource.getResourceName().trim().length() > 0
					&& resource.getOperation() != null) {
				for (Operation operation : resource.getOperation()) {
					if (operation != null
							&& operation.getOperationName().trim().length() > 0) {
						incrementCounter(resource.getResourceName().concat(":")
								.concat(operation.getOperationName()).concat(
										SERVICECOUNT), createModel(rules,
								SERVICECOUNT));
					}
				}

			}
		}

	}

	private RateLimiterPolicyModel createModel(List<Rule> rules, String name) {
		RateLimiterPolicyModel model = new RateLimiterPolicyModel();
		Rule rule = findRule(rules, name);
		if (rule != null) {
			model.setEffectDuration(new Date().getTime()
					+ rule.getEffectDuration());
			model.setRolloverPeriod(rule.getRolloverPeriod());
			model.setTimestamp(new Date());
		}
		return model;
	}

	protected Rule findRule(List<Rule> rules, String name) {
		if (rules != null && name != null)
			for (Rule rule : rules) {
				if (rule != null
						&& rule.getCondition() != null
						&& rule.getCondition().getExpression() != null
						&& rule.getCondition().getExpression()
								.getPrimitiveValue() != null) {
					if (rule.getCondition().getExpression().getPrimitiveValue()
							.getValue().contains(name)) {
						return rule;
					}
				}
			}
		return null;
	}

	// remove form database if effect duration is < now
	protected void resetEffect(String currentSubjectOrGroup) {
		if (currentSubjectOrGroup != null) {
			currentSubjectOrGroup = currentSubjectOrGroup.trim();
		}
		if (getActiveEffects().containsKey(currentSubjectOrGroup)) {
			RateLimiterPolicyModel limiterPolicyModel = getActiveEffects().get(
					currentSubjectOrGroup);

			// get current date
			java.util.Date date = new java.util.Date();
			if (date.after(new Date(limiterPolicyModel.getEffectDuration()))) {
				// remove it
				getActiveEffects().remove(currentSubjectOrGroup);

				if (getActiveRL().containsKey(currentSubjectOrGroup)) {
					getActiveRL().remove(currentSubjectOrGroup);
					getActiveRL().put(currentSubjectOrGroup,
							new RateLimiterPolicyModel());
				}
			}
		}
	}

	// special variable
	public static boolean isSpecialVAR(String str) {
		// hits for all
		if (HITS.equalsIgnoreCase(str)) {
			return true;
		} else if (str.contains(SERVICECOUNT)) {
			// add variable that counts service
			return true;
		}
		return false;
	}

	public String extractVariable(String str2) {
		String[] delim = { ">", "<", "=" };
		String[] words;
		for (String del : delim) {
			if (str2 == null) {
				break;
			}
			if (str2.contains(del)) {
				words = str2.split(del);
				str2 = getExp(words);
			}
		}

		return str2 != null ? str2.trim() : str2;
	}

	private String getExp(String[] str) {
		String temp = null;
		for (String s : str) {
			if (s.contains(IPHITS)) {
				temp = s;
			} else if (s.contains(SERVICECOUNT)) {
				temp = s;
			} else if (s.contains(HITS)) {
				temp = s;
			}
		}
		return temp;
	}

	private boolean isIpCount(String str) {
		boolean flag = false;
		if (str != null) {
			flag = str.contains(":hits");
		}
		return flag;
	}

	// retrieve from database or variable
	// ex testService:op1.count retrieve it from somewhere
	// if not found throw Error
	protected int getVariable(String str, String ipOrSubjectGroup)
			throws Exception {
		if (isIpCount(str)) {
			// assumption is per Ip hits so check if contains same Ip
			ipOrSubjectGroup = ipOrSubjectGroup.concat(IPHITS);

		} else {
			ipOrSubjectGroup = str;
		}
		RateLimiterPolicyModel model = getActiveRL().get(ipOrSubjectGroup);

		if (model == null) {
			throw new Exception(ipOrSubjectGroup + " not in database");

		}
		return model.getCount();
	}
}
