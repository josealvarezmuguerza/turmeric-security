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

import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.services.authorizationservice.impl.util.EncodingUtils;
import org.ebayopensource.turmeric.utils.ObjectUtils;


/**
 * "Smart" extension of a subject group - implements equals(), hashCode(), toString() 
 * (so it can be used in collections), has more constructors etc.  
 * 
 * @author mpoplacenel
 */
public class CloneableSubjectGroupType extends SubjectGroupType {
	
	/**
	 * Shallow-clone constructor.
	 * @param subjectGroupType the subject group to clone. 
	 */ 
	public CloneableSubjectGroupType(SubjectGroupType subjectGroupType) {
		this(subjectGroupType.getDomain(), subjectGroupType.getName(), subjectGroupType.getCalculator());
	}

	/**
	 * Explicit constructor. 
	 * @param domain the domain. 
	 * @param name the name. 
	 * @param calculator the calculator string. 
	 */
	public CloneableSubjectGroupType(String domain, String name, String calculator) {
		this.domain = domain;
		this.name = name;
		this.calculator = calculator;
	}
	
	/**
	 * Converts current object to a {@link SubjectGroupType} object. 
	 * @return a newly-created {@link SubjectGroupType} object
	 * with the same contents as this guy. 
	 */
	public SubjectGroupType toSubjectGroupType() {
		SubjectGroupType sg = new SubjectGroupType();
		sg.setName(getName());
		sg.setDomain(getDomain());
		sg.setCalculator(getCalculator());
		
		return sg;
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
		if (!getClass().equals(o.getClass())) return false;
		CloneableSubjectGroupType that = (CloneableSubjectGroupType) o;
		return ObjectUtils.bothNullOrEqual(getDomain(), that.getDomain())
			&& ObjectUtils.bothNullOrEqual(getName(), that.getName())
			&& isCalculated() == that.isCalculated();
	}

	/**
	 * Contents-based hash code. 
	 * @return a hash code assembled from the combination of the hash codes
	 * of the object's properties. 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ((getDomain() == null) ? 0 : getDomain().hashCode()) * 31 * 31 
			+ ((getName() == null) ? 0 : getName().hashCode()) * 31
			+ (isCalculated() ? 1 : 0);
	}

	/**
	 * Specifies if the subject is calculated (non-null calculator string)
	 * or not. 
	 * @return <code>true</code> if the subject is calculated,
	 * <code>false</code> otherwise. 
	 */
	public boolean isCalculated() {
		return getCalculator() != null && getCalculator().trim().length() > 0;
	}

	/**
	 * Human-friendly string representation. 
	 * @return the subject group {@link EncodingUtils#encodeSubjectGroupKey(String, String, String) encoded}. 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return EncodingUtils.encodeSubjectGroupKey(getDomain(), getName(), getCalculator());
	}

}