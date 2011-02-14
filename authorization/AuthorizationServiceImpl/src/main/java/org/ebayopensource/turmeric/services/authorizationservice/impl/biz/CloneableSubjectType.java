/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl.biz;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.services.authorizationservice.impl.util.EncodingUtils;
import org.ebayopensource.turmeric.utils.ObjectUtils;


/**
 * "Smart" extension of a subject - implements equals(), hashCode(), toString() 
 * (so it can be used in collections), has more constructors etc.  
 *  
 * @author mpoplacenel
 */
public class CloneableSubjectType extends SubjectType {
	
	/**
	 * POOL domain type. 
	 */
	public static final String POOL = "POOL";
	/**
	 * IP domain type. 
	 */
	public static final String IP = "IP";
	private static final int BITSINIPV4 = 32;

	/**
	 * Copy constructor. 
	 * @param subjectType the subject type to copy. 
	 */
	public CloneableSubjectType(SubjectType subjectType) {
		this(subjectType.getDomain(), subjectType.getValue());
	}
	
	/**
	 * Explicit constructor. 
	 * @param domain the domain. 
	 * @param value the subject name.
	 */
	public CloneableSubjectType(String domain, String value) {
		setDomain(domain);
		setValue(value);
	}

	/**
	 * Contents-based equals.
	 * @param o the other object. 
	 * @return <code>true</code> if objects are equal in reference or contents, 
	 * <code>false</code> otherwise. 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (o == this) return true;
		if (!getClass().equals(o.getClass())) return false;
		CloneableSubjectType that = (CloneableSubjectType) o;
		return ObjectUtils.bothNullOrEqual(getDomain(), that.getDomain())
			&& ObjectUtils.bothNullOrEqual(getValue(), that.getValue());
	}

	@Override
	/**
	 * Contents-based hash code. 
	 * @return a hash code assembled from the combination of the hash codes
	 * of the object's properties. 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return ((getDomain() == null) ? 0 : getDomain().hashCode()) * 31 
			+ ((getValue() == null) ? 0 : getValue().hashCode());
	}

	/**
	 * Human-friendly string representation. 
	 * @return the subject group {@link EncodingUtils#encodeSubjectGroupKey(String, String, String) encoded}. 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String superStr = super.toString();
		return superStr.substring(superStr.lastIndexOf(".") + 1) + "(" 
			+ EncodingUtils.encodeSubjectKey(getDomain(), getValue())
			+ ")";
	}

	/**
	 * Checks if the subject "matches" another. Two subjects match if
	 * they have the same case-insensitive domain (incl. <code>null</code>) and same name
	 * or if they are matching {@link #IP IPs}. Note the names are case-insensitive for {@link #POOL} domains. 
	 * @param subject the subject to compare against. 
	 * @return <code>true</code> if matching, <code>false</code> otherwise.
	 */
	public boolean match(CloneableSubjectType subject) {
		boolean result = false;
		if ( subject == null ) return false;
		if ( getDomain().equalsIgnoreCase(subject.getDomain())
			&& getValue().equals(subject.getValue())) {
			result = true;
		}
		
		result |= matchPool(subject);
		result |= matchIpAddress(subject);
		return result;
	}
	
	private boolean matchPool(CloneableSubjectType subject) {
		return POOL.equalsIgnoreCase(subject.getDomain()) 
		&& getDomain().equalsIgnoreCase(subject.getDomain()) 
		&& getValue().equalsIgnoreCase(subject.getValue());
	}

	private boolean matchIpAddress(CloneableSubjectType subject) {
		boolean result = false;
		if (IP.equalsIgnoreCase( subject.getDomain()) &&
				IP.equalsIgnoreCase(getDomain())) {
			if (getValue().equalsIgnoreCase(subject.getValue())) {
				return true;
			} else if ( getValue().indexOf("/") > 0  ) {
				result = matchIpSubrange ( subject ) ;
			}
		}		
		return result;
	}
	
	private boolean matchIpSubrange(CloneableSubjectType subject) {
		boolean result = false;
		
		//1. convert reqContext.m_value and this.m_value to int
		int requestIP = convertIPFromStringToInt(subject.getValue());
		int index = getValue().indexOf("/");
		int range = Integer.parseInt(getValue().substring(index + 1));
		int policyIP = convertIPFromStringToInt(getValue().substring(0, index));
		//2. get the mask from this.m_value
		int subrangemask = ((policyIP >> (BITSINIPV4 - range)) << BITSINIPV4 - range);
		//3 
		int requestIPSubrange = ((requestIP >> (BITSINIPV4 - range)) << BITSINIPV4 - range);
		//3. and with the mask and if the results are equal to the mask it is a match
		result = requestIPSubrange == subrangemask;
		
		return result;
	}
	
	private int convertIPFromStringToInt(String ip) {
		try {
			InetAddress ia = InetAddress.getByName(ip); 
			 byte[] b = ia.getAddress(); 
			 return ((b[0] << 24) & 0xFF000000) + ((b[1] << 16) & 0x00FF0000) 
			 		+ ((b[2] << 8) & 0x0000FF00) + ((b[3] << 0) & 0x000000FF);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Invalid IP :" + ip + ": " + e.getMessage());
		} 
	}
	
}