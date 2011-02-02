/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.ebayopensource.turmeric.test.services.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class TestDataReader {
    Properties props;
       
    public TestDataReader(Class testClass) throws IOException{
    	String propFileName = testClass.getSimpleName()+".properties";
    	loadProperties(testClass, propFileName);
    }
    
    public TestDataReader(Class testClass, String propFileName)throws IOException{
    	loadProperties(testClass, propFileName);
    }
	public InputStream getInputStream(String a) {
		InputStream is = getClass().getResourceAsStream( a );
	
		if(is ==null){
			is =ClassLoader.getSystemResourceAsStream(a);
		}
		if(is ==null){
			URL resource = Thread.currentThread().getContextClassLoader().getSystemClassLoader().getResource(a);    
		    try {
				is = new FileInputStream(new File(resource.toExternalForm()));
		    }catch (Exception e) {
		    	e.printStackTrace();
				
			}
		}
		
		return is;
	}
    private void loadProperties(Class testClass, String propFileName) throws IOException{
    	if (props == null) {
			props = new Properties();	
			InputStream input = testClass.getResourceAsStream(propFileName);
			if(input==null){
			   input =	getInputStream(propFileName);
			}
			System.out.println("input"+input);
			System.out.println("propFileName"+propFileName);
			//GetGroupMembersTests.properties
			
			if(input ==null && propFileName!=null){
				String packagestr= (testClass.getPackage()!=null?testClass.getPackage().getName() :null );
				if (packagestr!=null){
					String newPropFile = (packagestr.trim().replaceAll("\\.", "/")).concat("/").concat(propFileName);
					System.out.println("newPropFile" +newPropFile);
					 input =	getInputStream(newPropFile);
				}
			}
			props.load(input);
		}
    }
    
    public String getEntryValue(String entryName){
    	return props.getProperty(entryName);
    }
    
    public String getEntryValue(String testCaseId, String entryName){
    	return props.getProperty(entryName+"_"+testCaseId);
    }
    
    public String getPreEntryValue(String testCaseId, String entryName){
    	return props.getProperty(testCaseId+"."+entryName);
    }
	
    public void unloadProperties() {
    	props = null;
    }
    
    public Properties getProps(){
    	return props;
    }

}
