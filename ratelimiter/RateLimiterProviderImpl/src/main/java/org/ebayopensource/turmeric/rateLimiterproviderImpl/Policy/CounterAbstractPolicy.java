/**
 * 
 */
package org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.rateLimiterCounterProvider.RateLimiterCounterProvider;
import org.ebayopensource.turmeric.rateLimiterCounterProvider.Policy.model.RateLimiterPolicyModel;
import org.ebayopensource.turmeric.rateLimiterproviderImpl.counterprovider.config.RateLimiterCounterProviderFactory;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceRuntimeException;
import org.ebayopensource.turmeric.security.v1.services.EffectType;
import org.ebayopensource.turmeric.security.v1.services.Operation;
import org.ebayopensource.turmeric.security.v1.services.Policy;
import org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus;
import org.ebayopensource.turmeric.security.v1.services.Resource;
import org.ebayopensource.turmeric.security.v1.services.Resources;
import org.ebayopensource.turmeric.security.v1.services.Rule;
import org.ebayopensource.turmeric.utils.ContextUtils;

/**
 * The Class CounterAbstractPolicy.
 * 
 * @author gbaal
 */
public abstract class CounterAbstractPolicy {
	// variables
	// all hits
	/** The Constant HITS. */
	public static final String HITS = "HITS";
	// service.operation:count
	/** The Constant SERVICECOUNT. */
	public static final String SERVICECOUNT = ".count";
	// 101.12.69.105:hits
	/** The Constant IPHITS. */
	public static final String IPHITS = ":hits";

	private static RateLimiterCounterProvider counterProvider;

	private static final String counterProviderPropFilePath = "META-INF/soa/providers/config/RateLimiterProvider/counter_provider.properties";

	private static final String counterProviderPropKey = "preferred-provider";

	private static List<CommonErrorData> s_errorData = null;

	/**
	 * Instantiates a new counter abstract policy.
	 */
	public CounterAbstractPolicy() {
		super();
		getCounterProvider();
	}

	protected void getCounterProvider() {
		if (s_errorData != null) {
			throw new ServiceRuntimeException(s_errorData);
		}
		if (counterProvider == null) {
			try {
				counterProvider = RateLimiterCounterProviderFactory
						.create(getPreferredProvider());
			} catch (ServiceException se) {
				s_errorData = se.getErrorMessage().getError();
				throw new ServiceRuntimeException(s_errorData);
			}
		}
	}

	private static String getPreferredProvider() {
		ClassLoader classLoader = ContextUtils.getClassLoader();
		InputStream inStream = classLoader
				.getResourceAsStream(counterProviderPropFilePath);
		String provider = null;
		if (inStream != null) {
			Properties properties = new Properties();
			try {
				properties.load(inStream);
				provider = (String) properties.get(counterProviderPropKey);
			} catch (IOException e) {
				// ignore
			} finally {
				try {
					inStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return provider;
	}



	/**
	 * Increment counter.
	 * 
	 * @param ipOrSubjectGroup
	 *            the ip or subject group
	 * @param rateLimiterPolicyModel
	 *            the rate limiter policy model
	 */
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

			if (counterProvider.cointainKeyInActiveRL(ipOrSubjectGroup)) {
				counterProvider.incrementRLCounter(ipOrSubjectGroup);
			} else {
				counterProvider.setRLCounter(ipOrSubjectGroup, 1);
			}
		}

	}

	
	// check if we need to reset counter base on RolloverPeriod
	/**
	 * Reset counter.
	 * 
	 * @param ipOrSubjectGroup
	 *            the ip or subject group
	 * @param rule
	 *            the rule
	 */
	protected void resetCounter(String ipOrSubjectGroup, final Rule rule) {
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
		RateLimiterPolicyModel limiterPolicyModel = counterProvider
				.getActiveRL(ipOrSubjectGroup);
		if (limiterPolicyModel == null) {
			return;
		}

		if (limiterPolicyModel.getTimestamp() == null) {
			System.err.println("throw error1resetCounter" + ipOrSubjectGroup);
			return;
		}
		if (rule.getRolloverPeriod() == null) {
			System.err.println("throw error resetCounter" + ipOrSubjectGroup);
			return;

		}

		// add the the time it was added to the rolloverPeriod
		Long timeToReset = (limiterPolicyModel.getTimestamp().getTime() + rule
				.getRolloverPeriod() * 1000);
		// get current date
		java.util.Date date = new java.util.Date();
		// compare if current id after the reset date
		if (date.after(new Date(timeToReset))) {
			if (counterProvider.cointainKeyInActiveRL(ipOrSubjectGroup)) {
				// reset it
				counterProvider.setRLCounter(ipOrSubjectGroup, 1);
				counterProvider.setRLTimestamp(ipOrSubjectGroup, new Date());
				counterProvider.setRLActive(ipOrSubjectGroup, false);
				counterProvider.setRLEffectDuration(ipOrSubjectGroup, null);
				counterProvider.setRLEffect(ipOrSubjectGroup, null);
				// since reset remove it to activeEffects
				counterProvider.removeActiveEffect(ipOrSubjectGroup);

			}
		}

	}

	// add to database
	/**
	 * Adds the to active effects.
	 * 
	 * @param currentSubjectOrGroup
	 *            the current subject or group
	 * @param rule
	 *            the rule
	 * @param currentlimiterStatus
	 *            the currentlimiter status
	 */
	protected void addToActiveEffects(String currentSubjectOrGroup, Rule rule,
			RateLimiterStatus currentlimiterStatus) {
		if (currentSubjectOrGroup != null) {
			currentSubjectOrGroup = currentSubjectOrGroup.trim();
		}
		RateLimiterPolicyModel limiterPolicyModel = new RateLimiterPolicyModel();
		limiterPolicyModel.setTimestamp(new Date());
		limiterPolicyModel.setEffectDuration(new Date().getTime()
				+ rule.getEffectDuration() * 1000);
		limiterPolicyModel.setActive(true);
		limiterPolicyModel.setEffect(currentlimiterStatus);

		counterProvider.removeActiveEffect(currentSubjectOrGroup);
		counterProvider.addActiveEffect(currentSubjectOrGroup,
				limiterPolicyModel);

	}

	// checks and reset effect
	/**
	 * Check all effects.
	 */
	protected void checkAllEffects() {
		counterProvider.resetEffects();
	}

	/**
	 * Creates the service counters.
	 * 
	 * @param policy
	 *            the policy
	 */
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

	private void updateData(final List<Rule> rules) {
		for (String data : counterProvider.getActiveRLKeys()) {
			RateLimiterPolicyModel model = counterProvider.getActiveRL(data);
			Rule rule = null;
			if (model != null) {
				rule = findRule(rules, data);
				if (rule != null) {
					if (model.getRolloverPeriod() == null) {
						counterProvider.setRLRolloverPeriod(data,
								rule.getRolloverPeriod() * 1000);
					}
					if (model.getTimestamp() == null) {
						counterProvider.setRLTimestamp(data, new Date());
					}
					resetCounter(data, rule);
				}
			}

		}

	}

	/**
	 * Gets the limiter status.
	 * 
	 * @param type
	 *            the type
	 * @return the limiter status
	 */
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
						incrementCounter(
								resource.getResourceName().concat(":")
										.concat(operation.getOperationName())
										.concat(SERVICECOUNT),
								createModel(rules, SERVICECOUNT));
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
					+ rule.getEffectDuration() * 1000);
			model.setRolloverPeriod(rule.getRolloverPeriod() * 1000);
			model.setTimestamp(new Date());
		}
		return model;
	}

	/**
	 * Find rule.
	 * 
	 * @param rules
	 *            the rules
	 * @param name
	 *            the name
	 * @return the rule
	 */
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

	// special variable
	/**
	 * Checks if is special var.
	 * 
	 * @param str
	 *            the str
	 * @return true, if is special var
	 */
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

	/**
	 * Extract variable.
	 * 
	 * @param str2
	 *            the str2
	 * @return the string
	 */
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
	/**
	 * Gets the variable.
	 * 
	 * @param str
	 *            the str
	 * @param ipOrSubjectGroup
	 *            the ip or subject group
	 * @return the variable
	 * @throws Exception
	 *             the exception
	 */
	protected int getVariable(String str, String ipOrSubjectGroup)
			throws Exception {
		if (isIpCount(str)) {
			// assumption is per Ip hits so check if contains same Ip
			ipOrSubjectGroup = ipOrSubjectGroup.concat(IPHITS);

		} else {
			ipOrSubjectGroup = str;
		}
		RateLimiterPolicyModel model = counterProvider
				.getActiveRL(ipOrSubjectGroup);

		if (model == null) {
			throw new Exception(ipOrSubjectGroup + " not in database");

		}
		return model.getCount();
	}
}
