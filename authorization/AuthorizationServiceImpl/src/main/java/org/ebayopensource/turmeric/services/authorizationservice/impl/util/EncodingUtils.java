/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl.util;

import java.util.Arrays;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.ErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.services.authorizationservice.impl.biz.CloneableSubjectGroupType;
import org.ebayopensource.turmeric.services.authorizationservice.impl.biz.CloneableSubjectType;


/**
 * Utility methods for encoding various classes to a string (and decoding them from
 * such an encoded string, of course).
 * 
 * @author mpoplacenel
 */
public class EncodingUtils {

	/**
	 * The separator to be used to separate the components in the encoded version. 
	 */
	public static String SEPARATOR = "~";
	/**
	 * The {@link #SEPARATOR} encoded in the Unicode format (e.g. "\\uNNNN"). 
	 */
	public static final String SEPARATOR_MASK = maskString(SEPARATOR);
	/**
	 * The string to represent a null component in the encoded version. 
	 */
	public static String NULL = "__";
	/**
	 * The {@link #NULL} string encoded in the Unicode format (e.g. "\\uNNNN"). 
	 */
	public static String NULL_MASK = maskString(NULL);
	
	/**
	 * Encode a string in its Unicode format (e.g. "\\uNNNN").
	 * @param string the string to encode.
	 * @return the Unicode representation of the string. 
	 */
	public static String maskString(String string) {
		if (string == null) return null;
		StringBuilder sb = new StringBuilder("\\u");
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);
			sb.append((short) c);
		}
		
		return sb.toString();
	}

	/**
	 * Encodes an array of strings into a single string. 
	 * @param strings the array of strings to encode. 
	 * @return the encoded representation of the strings. 
	 */
	public static String encodeKey(String... strings) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String string : strings) {
			if (i++ > 0) sb.append(SEPARATOR);
			sb.append(encodeNull(encodeString(string)));
		}
		return sb.toString();
	}

	/**
	 * Decode a given previously-encoded string to its components. 
	 * @param key the encoded string ("key") to decode
	 * @return the identified components.
	 */
	public static String[] decodeKey(String key) {
		String[] decodedKeys = key.split(SEPARATOR);
		for (int i = 0; i < decodedKeys.length; i++) {
			decodedKeys[i] = decodeString(decodeNull(decodedKeys[i]));
		}
		
		return decodedKeys;
	}
	
	/**
	 * Encode a single string by representing the NULL and replacing the separator. 
	 * This is called by {@link #encodeKey(String...)}
	 * for each component of the given array. 
	 * @param string the string to encode. 
	 * @return the encoded representation of the given string. 
	 */
	public static String encodeString(String string) {
		return string == null ? null : string.replace(NULL, NULL_MASK).replace(SEPARATOR, SEPARATOR_MASK);
	}
	
	/**
	 * Decode a single string. This is called by {@link #decodeKey(String)} for
	 * each of the identified components. 
	 * @param string the string to decode. 
	 * @return the decoded version of the string. 
	 */
	public static String decodeString(String string) {
		return string == null ? null : string.replace(SEPARATOR_MASK, SEPARATOR);
	}

	/**
	 * Encode the given string if it's null. 
	 * @param key the string to encode. 
	 * @return the string if it's not <code>null</code>, or {@link #NULL} otherwise. 
	 */
	public static String encodeNull(String key) {
		return key == null ? NULL : key;
	}

	/**
	 * Decode the string by parsing the {@link #NULL} representation. 
	 * @param encodedKey the encoded string to decode. 
	 * @return the string if it's not {@link #NULL}, or <code>null</code> otherwise. 
	 */
	public static String decodeNull(String encodedKey) {
		return NULL.equals(encodedKey) ? null : encodedKey;
	}

	/**
	 * Encode the information about a subject group. 
	 * @param domain the subject group's domain. 
	 * @param name the subject group's name.
	 * @param calculator the subject group's calculator string. 
	 * @return the encoded representation of the subject group.
	 */
	public static String encodeSubjectGroupKey(String domain, String name, String calculator) {
		return encodeKey(domain, name, calculator);
	}

	/**
	 * Decode an encoded subject group key. 
	 * @param key the encoded representation of the subject group.
	 * @return the decoded subject group.
	 */
	public static CloneableSubjectGroupType decodeSubjectGroupKey(String key) {
		String[] decodedKey = decodeKey(key);
		assert decodedKey != null && decodedKey.length == 3 
			: "Decoded key should have exactly 2 elements but is " 
				+ (decodedKey == null ? "[NULL]" : Arrays.asList(decodedKey));
		
		SubjectGroupType sg = new SubjectGroupType();
		sg.setDomain(decodedKey[0]);
		sg.setName(decodedKey[1]);
		sg.setCalculator(decodedKey[2] == null || decodedKey[2].equals("0") ? null : decodedKey[2]);
		
		return new CloneableSubjectGroupType(sg);
	}

	/**
	 * Decode an encoded subject key. 
	 * @param key the encoded representation of the subject.
	 * @return the decoded subject.
	 */
	public static CloneableSubjectType decodeSubjectKey(String key) {
		String[] decodedKey = decodeKey(key);
		assert decodedKey != null && decodedKey.length == 2 
			: "Decoded key should have exactly 2 elements but is " 
				+ (decodedKey == null ? "[NULL]" : Arrays.asList(decodedKey));
		
		SubjectType subj = new SubjectType();
		subj.setDomain(decodedKey[0]);
		subj.setValue(decodedKey[1]);
		
		return new CloneableSubjectType(subj);
	}

	/**
	 * Encode the information about a subject. 
	 * @param domain the subject's domain. 
	 * @param name the subject's name.
	 * @return the encoded representation of the subject.
	 */
	public static String encodeSubjectKey(String domain, String name) {
		return encodeKey(domain, name);
	}
	
	/**
	 * Encode an ACK + error message combination to a single String. 
	 * @param ack the ACK to encode. 
	 * @param errMsg the error message to encode. 
	 * @return the encoded error message. 
	 */
	public static String encodeErrorMessage(AckValue ack, ErrorMessage errMsg) {
		StringBuilder sb = new StringBuilder();
		sb.append("ack=").append(String.valueOf(ack)).append(";");
		if (errMsg != null) {
			for (ErrorData errData : errMsg.getError()) {
				sb.append(';');
				sb
					.append("severity:").append(errData.getSeverity())
					.append(',')
					.append("errorId:").append(errData.getErrorId())
					.append(',')
					.append("exceptionId:").append(errData.getExceptionId())
					.append(',')
					.append("domain:").append(errData.getDomain())
					.append(',')
					.append("subdomain:").append(errData.getSubdomain())
					.append(',')
					.append("message:").append(errData.getMessage());
			}
		}
		
		return sb.toString();
	}

	private EncodingUtils() {
		// no instances
	}

	/**
	 * Encode the components of a {@link ResOpKey} to a single string. 
	 * @param resType the resource type. 
	 * @param resName the resource name. 
	 * @param opName the operation name. 
	 * @return the encoded representation of the {@link ResOpKey}. 
	 */
	public static String encodeResOpKey(String resType, String resName, String opName) {
		return encodeKey(resType, resName, opName);
	}
	
	/**
	 * Encode a {@link ResOpKey} to a single string. 
	 * @param resOpKey the {@link ResOpKey} object. 
	 * @return the encoded representation of the {@link ResOpKey}. 
	 */
	public static String encodeResOpKey(ResOpKey resOpKey) {
		return encodeResOpKey(resOpKey.getResourceType(), resOpKey.getResourceName(), resOpKey.getOperationName());
	}

	/**
	 * Decode a {@link ResOpKey} from an encoded string. 
	 * @param key the encoded representation of the {@link ResOpKey}. 
	 * @return resOpKey the {@link ResOpKey} object. 
	 */
	public static ResOpKey decodeResOpKey(String key) {
		String[] decodedKey = decodeKey(key);
		if (decodedKey == null || decodedKey.length > 3) 
			throw new IllegalArgumentException(
					"Decoded key should be non-null and have no more than 3 elements, and now it is " 
					+ (decodedKey == null ? "[NULL]" : Arrays.asList(decodedKey)));
		String opNm = decodedKey.length > 2 ? decodedKey[2] : "";
		String resNm = decodedKey.length > 1 ? decodedKey[1] : "";
		String resTyp = decodedKey.length > 0 ? decodedKey[0] : "";
		return new ResOpKey(resTyp, resNm, opNm);
	}

}