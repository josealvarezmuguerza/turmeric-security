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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.ebayopensource.turmeric.rateLimiterproviderImpl.Policy.util.Utils;
import org.ebayopensource.turmeric.security.v1.services.Subject;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroup;
import org.junit.Test;

/**
 * The Class UtilsTest.
 * 
 * @author jamuguerza
 */
public class UtilsTest {

	@Test
	public void testSetAllSubjectId(){
		String id = "[0-9]+";
		Subject subject = new Subject();
		Utils.setAllSubjectId(subject);
		assertNotNull("Subject must not be null", subject);
		assertNotNull("SubjectMatch must contain at least 1 element", subject.getSubjectMatch());
		assertEquals("SubjectMatch must contain at least 1 element", 1, subject.getSubjectMatch().size());
		assertEquals("Attr value id must be this String chain: " + id, id, subject.getSubjectMatch().get(0).getAttributeValue().getContent().get(0));
	}
	

	@Test
	public void testSetSubjectId(){
		Long id = 1L;
		Subject subject = new Subject();
		Utils.setSubjectId(subject, id);
		assertNotNull("Subject must not be null", subject);
		assertNotNull("SubjectMatch must contain at least 1 element", subject.getSubjectMatch());
		assertEquals("SubjectMatch must contain at least 1 element", 1, subject.getSubjectMatch().size());
		assertEquals("Attr value id must be 1 as String", id.toString(), subject.getSubjectMatch().get(0).getAttributeValue().getContent().get(0));
	}
	
	@Test
	public void testSetExclusionSubjectId(){
		Long id = 1L;
		Subject subject = new Subject();
		Utils.setExclusionSubjectId(subject, id);
		assertNotNull("Subject must not be null", subject);
		assertNotNull("SubjectMatch must contain at least 1 element", subject.getSubjectMatch());
		assertEquals("SubjectMatch must contain at least 1 element", 1, subject.getSubjectMatch().size());
		
		assertEquals("Attr value id must be this String chain '(?!1)'", "(?!" + id.toString() + ")", subject.getSubjectMatch().get(0).getAttributeValue().getContent().get(0));
	}
	
	@Test
	public void testSetSubjectGroupId(){
		Long id = 1L;
		SubjectGroup sg = new SubjectGroup();
		Utils.setSubjectGroupId(sg, id);
		assertNotNull("SubjectGroup must not be null", sg);
		assertNotNull("SubjectMatch must contain at least 1 element", sg.getSubjectMatch());
		assertEquals("Attr value id must be 1 as String", id.toString(), sg.getSubjectMatch().getAttributeValue().getContent().get(0));
	}
	
	@Test
	public void testSetExclusionSubjectGroupId(){
		Long id = 1L;
		SubjectGroup sg= new SubjectGroup();
		Utils.setExclusionSubjectGroupId(sg, id);
		assertNotNull("SubjectGroup must not be null", sg);
		assertNotNull("SubjectMatch must contain at least 1 element", sg.getSubjectMatch());
    	assertEquals("Attr value id must be this String chain '(?!1)'", "(?!" + id.toString() + ")", sg.getSubjectMatch().getAttributeValue().getContent().get(0));
	}
	
	
	@Test
	public void testGetIdFromSubjectMatch(){
		final Long id = 1L;
		Subject subject = new Subject();
		Utils.setSubjectId(subject, id);
		Long idFromSubjectMatch = Utils.getIdFromSubjectMatch(subject.getSubjectMatch().get(0));
		assertNotNull(idFromSubjectMatch);
		assertEquals("ID should be equals to 1 Long type", id, idFromSubjectMatch);
	}
	
	@Test
	public void testGetIdFromSubject(){
		final Long id = 1L;
		Subject subject = new Subject();
		Utils.setSubjectId(subject, id);
		Long idFromSubjectMatch = Utils.getSubjectId(subject);
		assertNotNull(idFromSubjectMatch);
		assertEquals("ID should be equals to 1 Long type", id, idFromSubjectMatch);
	}

	@Test
	public void testGetIdFromSubjectGroup(){
		final Long id = 1L;
		SubjectGroup sg = new SubjectGroup();
		Utils.setSubjectGroupId(sg, id);
		Long idFromSubjectMatch = Utils.getSubjectGroupId(sg);
		assertNotNull(idFromSubjectMatch);
		assertEquals("ID should be equals to 1 Long type", id, idFromSubjectMatch);
	}
	
	
	@Test
	public void testGetIdFromExclusionSubject(){
		final Long id = 1L;
		Subject subject = new Subject();
		Utils.setExclusionSubjectId(subject, id);
		Long idFromSubjectMatch = Utils.getSubjectId(subject);
		assertNotNull(idFromSubjectMatch);
		assertEquals("ID should be equals to 1 Long type", id, idFromSubjectMatch);
	}
	
	@Test
	public void testGetIdFromExclusionSubjectGroup(){
		final Long id = 1L;
		SubjectGroup sg = new SubjectGroup();
		Utils.setExclusionSubjectGroupId(sg, id);
		Long idFromSubjectMatch = Utils.getSubjectGroupId(sg);
		assertNotNull(idFromSubjectMatch);
		assertEquals("ID should be equals to 1 Long type", id, idFromSubjectMatch);
	}
	
}
