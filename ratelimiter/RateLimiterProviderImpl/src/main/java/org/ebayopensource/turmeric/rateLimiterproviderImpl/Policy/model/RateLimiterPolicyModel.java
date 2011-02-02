package org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.model;

import java.util.Date;

import org.ebayopensource.turmeric.security.v1.services.RateLimiterStatus;

public class RateLimiterPolicyModel {
	private Date timestamp;
	private boolean active;
	private String ip;
	private RateLimiterStatus effect;
	private int count;
	private Long effectDuration;
	private Long rolloverPeriod;

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public RateLimiterStatus getEffect() {
		return effect;
	}

	public void setEffect(RateLimiterStatus effect) {
		this.effect = effect;
	}

	public Long getEffectDuration() {
		return effectDuration;
	}

	public void setEffectDuration(Long effectDuration) {
		this.effectDuration = effectDuration;
	}

	public Long getRolloverPeriod() {
		return rolloverPeriod;
	}

	public void setRolloverPeriod(Long rolloverPeriod) {
		this.rolloverPeriod = rolloverPeriod;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
