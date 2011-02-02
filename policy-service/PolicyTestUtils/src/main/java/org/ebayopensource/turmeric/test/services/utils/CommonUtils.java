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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CommonUtils {

	   public static int maxPolicies(Properties props){
			Pattern pattern = Pattern.compile("policyName"+"_"+"policy(\\d+)");
			Matcher matcher = null;	
			Set s = props.keySet();
			Iterator ite = s.iterator();

			int max = 0;
			int value;
			while(ite.hasNext()){
				matcher = pattern.matcher(ite.next().toString());
				if(matcher.find()){
					value = Integer.parseInt(matcher.group(1));
					if(value>max)
						max = value;
				}
			}
	     return max;
		}
	   
	   public static void createPolicies(TestDataReader reader, int max){
		   	for(int i=0; i <= max ; i++){
		   		try {
						PolicyDataModelHelper.createPolicyObject(reader,"policy"+i);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		   	}
	   }
	   
	   public static void cleanUpPolicy(TestDataReader reader, int max){
		      for(int i=0; i <= max ; i++){
		        try{
					 PolicyDataModelHelper.deletePolicyObject(reader, "policy"+i);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		      }

	   }
	   
	   public static int maxSG(Properties props){
			Pattern pattern = Pattern.compile("subjectGroupDetails"+"_"+"policy(\\d+)");
			Matcher matcher = null;	
			Set s = props.keySet();
			Iterator ite = s.iterator();

			int max = 0;
			int value;
			while(ite.hasNext()){
				matcher = pattern.matcher(ite.next().toString());
				if(matcher.find()){
					value = Integer.parseInt(matcher.group(1));
					if(value>max)
						max = value;
				}
			}
	     return max;
		}
	   
	   public static void createSG(TestDataReader reader, int max){
		   	for(int i=0; i <= max ; i++){
		   		try {
		   			 SubjectGroupModelHelper.createSGObject(reader, "policy"+i);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		   	}
	   }
	   
	   public static void cleanUpSG(TestDataReader reader, int max){
		      for(int i=0; i <= max ; i++){
		        try{
		        	SubjectGroupModelHelper.deletePolicyObject(reader, "policy"+i);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		      }

	   }
	   
	  public static int totalPolicies(Class className) throws IOException{
			Properties props = new Properties();	
			InputStream input = className.getResourceAsStream(className.getSimpleName()+".properties");
			props.load(input);
			
			Pattern pattern = Pattern.compile("policyName"+"_"+"policy(\\d)");
			Matcher matcher = null;	
			Set s = props.keySet();
			Iterator ite = s.iterator();

			int max = 0;
			int value;
			while(ite.hasNext()){
				matcher = pattern.matcher(ite.next().toString());
				if(matcher.find()){
					value = Integer.parseInt(matcher.group(1));
					if(value>max)
						max = value;
				}
			}
			System.out.println("max value="+max);
			return max;
		}
}
