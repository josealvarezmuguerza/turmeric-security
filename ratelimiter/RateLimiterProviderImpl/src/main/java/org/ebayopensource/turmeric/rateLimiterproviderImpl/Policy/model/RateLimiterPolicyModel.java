package org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.model;

import java.util.Date;

import org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus;

/**
 * The Class RateLimiterPolicyModel.
 */
public class RateLimiterPolicyModel {
	private Date timestamp;
	private boolean active;
	private String ip;
	private RateLimiterStatus effect;
	private int count;
	private Long effectDuration;
	private Long rolloverPeriod;

	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		if (timestamp == null) {
			return null;
		}
		return new Date(timestamp.getTime());
	}

	/**
	 * Sets the timestamp.
	 *
	 * @param timestamp the new timestamp
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = null;
		if (timestamp != null) {
			this.timestamp = new Date(timestamp.getTime());
		}
	}

	/**
	 * Checks if is active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets the active.
	 *
	 * @param active the new active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Gets the ip.
	 *
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Sets the ip.
	 *
	 * @param ip the new ip
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * Gets the effect.
	 *
	 * @return the effect
	 */
	public RateLimiterStatus getEffect() {
		return effect;
	}

	/**
	 * Sets the effect.
	 *
	 * @param effect the new effect
	 */
	public void setEffect(RateLimiterStatus effect) {
		this.effect = effect;
	}

	/**
	 * Gets the effect duration.
	 *
	 * @return the effect duration
	 */
	public Long getEffectDuration() {
		return effectDuration;
	}

	/**
	 * Sets the effect duration.
	 *
	 * @param effectDuration the new effect duration
	 */
	public void setEffectDuration(Long effectDuration) {
		this.effectDuration = effectDuration;
	}

	/**
	 * Gets the rollover period.
	 *
	 * @return the rollover period
	 */
	public Long getRolloverPeriod() {
		return rolloverPeriod;
	}

	/**
	 * Sets the rollover period.
	 *
	 * @param rolloverPeriod the new rollover period
	 */
	public void setRolloverPeriod(Long rolloverPeriod) {
		this.rolloverPeriod = rolloverPeriod;
	}

	/**
	 * Gets the count.
	 *
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Sets the count.
	 *
	 * @param count the new count
	 */
	public void setCount(int count) {
		this.count = count;
	}

}
