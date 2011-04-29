/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import oasis.names.tc.xacml._2_0.policy.schema.os.AttributeValueType;
import oasis.names.tc.xacml._2_0.policy.schema.os.SubjectAttributeDesignatorType;
import oasis.names.tc.xacml._2_0.policy.schema.os.SubjectMatchType;

import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;


/**
 * The Class Utils.
 */
public class Utils
{
	private static Logger s_Logger = LogManager.getInstance(Utils.class);
	
	/**
	 * Sets the subject group id.
	 *
	 * @param sg the sg
	 * @param id the id
	 */
	public static void setSubjectGroupId(SubjectGroup sg, Long id)
	{
		SubjectMatchType subjectMatchType = null;
		subjectMatchType= getSubjectMatchType(id);
		sg.setSubjectMatch( subjectMatchType );
	}
	
	/**
	 * Sets the exclusion subject group id.
	 *
	 * @param sg the sg
	 * @param id the id
	 */
	public static void setExclusionSubjectGroupId(SubjectGroup sg, Long id)
	{
		SubjectMatchType subjectMatchType = null;
		String idString = "(?!" + id + ")";
		subjectMatchType = getSubjectMatchType(idString);

		sg.setSubjectMatch( subjectMatchType );
	}
	
	/**
	 * Gets the subject match type.
	 *
	 * @param id the id
	 * @return the subject match type
	 */
	public static SubjectMatchType getSubjectMatchType(Long id)
	{
		SubjectMatchType subjectMatchType = new SubjectMatchType();
		subjectMatchType.setMatchId( "urn:oasis:names:tc:xacml:1.0:function:integer-equal" );
		AttributeValueType attributeValueType = new AttributeValueType();
		attributeValueType.setDataType( "http://www.w3.org/2001/XMLSchema#integer" );
		attributeValueType.getContent().add( id.toString() );  // id is used as-is
		subjectMatchType.setAttributeValue( attributeValueType );

		SubjectAttributeDesignatorType subjectAttributeDesignatorType = new SubjectAttributeDesignatorType();
		subjectAttributeDesignatorType.setDataType( "http://www.w3.org/2001/XMLSchema#integer" );
		subjectAttributeDesignatorType.setAttributeId( "urn:oasis:names:tc:xacml:1.0:subject:subject-id" );
		subjectMatchType.setSubjectAttributeDesignator( subjectAttributeDesignatorType );
		return subjectMatchType;
	}
	
	/**
	 * Gets the subject match type.
	 *
	 * @param idStr the id str
	 * @return the subject match type
	 */
	public static SubjectMatchType getSubjectMatchType(String idStr)
	{
		SubjectMatchType subjectMatchType = new SubjectMatchType();
        subjectMatchType.setMatchId( "urn:oasis:names:tc:xacml:1.0:function:string-regexp-match" );
        AttributeValueType attributeValueType = new AttributeValueType();
        attributeValueType.setDataType( "http://www.w3.org/2001/XMLSchema#string" );
        attributeValueType.getContent().add( idStr);
        subjectMatchType.setAttributeValue( attributeValueType );

        SubjectAttributeDesignatorType subjectAttributeDesignatorType = new SubjectAttributeDesignatorType();
        subjectAttributeDesignatorType.setDataType( "http://www.w3.org/2001/XMLSchema#string" );
        subjectAttributeDesignatorType.setAttributeId( "urn:oasis:names:tc:xacml:1.0:subject:subject-id" );
        subjectMatchType.setSubjectAttributeDesignator( subjectAttributeDesignatorType );

		return subjectMatchType;
	}
		
	/**
	 * Sets the subject id.
	 *
	 * @param subject the subject
	 * @param id the id
	 */
	public static void setSubjectId(Subject subject, Long id)
	{
		SubjectMatchType subjectMatchType = null;
		subjectMatchType= getSubjectMatchType(id);
		subject.getSubjectMatch().add( subjectMatchType );
	}
	
	/**
	 * Sets the exclusion subject id.
	 *
	 * @param subject the subject
	 * @param id the id
	 */
	public static void setExclusionSubjectId(Subject subject, Long id)
	{
		SubjectMatchType subjectMatchType = null;
		String idString = "(?!" + id + ")";
		subjectMatchType = getSubjectMatchType(idString);
		subject.getSubjectMatch().add( subjectMatchType );
	}
	
	/**
	 * Sets the all subject id.
	 *
	 * @param subject the new all subject id
	 */
	public static void setAllSubjectId(Subject subject) {
		String idString = "[0-9]+";
		SubjectMatchType subjectMatchType = getSubjectMatchType(idString);
		subject.getSubjectMatch().add( subjectMatchType );
	}

	/**
	 * Gets the subject id.
	 *
	 * @param subject the subject
	 * @return the subject id
	 */
	public static Long getSubjectId(Subject subject)
	{
		Long id = null;
		Iterator<SubjectMatchType> iter = subject.getSubjectMatch().iterator();
		while (iter.hasNext())
		{
			SubjectMatchType matchType = iter.next();
			id = getIdFromSubjectMatch(matchType);
			if (id != null)
				return id;
		}
		return id;
	}
	
	/**
	 * Gets the subject group id.
	 *
	 * @param sg the sg
	 * @return the subject group id
	 */
	public static Long getSubjectGroupId(SubjectGroup sg )
	{	
		SubjectMatchType matchType = sg.getSubjectMatch();
		
		return getIdFromSubjectMatch(matchType);
	}
	
	/**
	 * Gets the id from subject match.
	 *
	 * @param matchType the match type
	 * @return the id from subject match
	 */
	public static Long getIdFromSubjectMatch(SubjectMatchType matchType)
	{
		Long subjectId = null;
		
		if (matchType != null && matchType.getSubjectAttributeDesignator().
					getAttributeId().equals("urn:oasis:names:tc:xacml:1.0:subject:subject-id"))
		{
			AttributeValueType attributeValue = matchType.getAttributeValue();
			 
			String idString = attributeValue.getContent().get(0).toString();
            if ("urn:oasis:names:tc:xacml:1.0:function:integer-equal".equals(matchType.getMatchId())) {
                try {
                    subjectId = Long.parseLong(idString);
                } catch (Exception e) {
                	s_Logger.log(Level.WARNING, 
                			"invalid subject Id " + 
                			idString);
                }
            }

            if ("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match".equals(matchType.getMatchId())) {
                try {
                    subjectId = Long.parseLong(idString.substring(3, idString.length() -1));
                } catch (Exception e) {
                	s_Logger.log(Level.WARNING, 
                			"invalid external subject Id " + 
                			idString);
                }
            }
		}
			
		return subjectId;
	}
	
	/**
	 * Checks if is exclusion.
	 *
	 * @param subject the subject
	 * @return true, if is exclusion
	 */
	public static boolean isExclusion(Subject subject)
	{
		List<SubjectMatchType> subjectMatchs = subject.getSubjectMatch();
		return isExclusion(subjectMatchs);
	}
	
	/**
	 * Checks if is subject type.
	 *
	 * @param subject the subject
	 * @return true, if is subject type
	 */
	public static boolean isSubjectType(Subject subject)
	{
		List<SubjectMatchType> subjectMatchs = subject.getSubjectMatch();
		return isSubjectType(subjectMatchs);
	}
	
	/**
	 * Checks if is exclusion.
	 *
	 * @param subjectGroup the subject group
	 * @return true, if is exclusion
	 */
	public static boolean isExclusion(SubjectGroup subjectGroup)
	{
		SubjectMatchType subjectMatchType = subjectGroup.getSubjectMatch();
		List<SubjectMatchType> subjectMatchs = new ArrayList<SubjectMatchType>();
		subjectMatchs.add(subjectMatchType);
		return isExclusion(subjectMatchs);
	}
	
	/**
	 * Checks if is exclusion.
	 *
	 * @param matchTypes the match types
	 * @return true, if is exclusion
	 */
	public static boolean isExclusion(List<SubjectMatchType> matchTypes) {
		boolean isExclusion = false;
		for (SubjectMatchType matchType : matchTypes) {
			if (matchType != null && "urn:oasis:names:tc:xacml:1.0:function:string-regexp-match".equals(matchType.getMatchId())) {
				if (getIdFromSubjectMatch(matchType) != null)
					isExclusion = true;
				break;
			}
		}
		return isExclusion;
	}
	
	/**
	 * Checks if is subject type.
	 *
	 * @param matchTypes the match types
	 * @return true, if is subject type
	 */
	public static boolean isSubjectType(List<SubjectMatchType> matchTypes) {
		boolean isSubjectType = false;
		for (SubjectMatchType matchType : matchTypes) {
			if (matchType != null && "urn:oasis:names:tc:xacml:1.0:function:string-regexp-match".equals(matchType.getMatchId())) {
				if (getIdFromSubjectMatch(matchType) == null)
					isSubjectType = true;
				break;
			}
		}
		return isSubjectType;
	}
}
