package org.ebayopensource.turmeric.test.services.authorizationservice;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ebayopensource.turmeric.security.v1.services.AuthorizeRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeResponseType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;

import org.ebayopensource.turmeric.test.services.utils.CommonUtils;
import org.ebayopensource.turmeric.test.services.utils.TestDataReader;
import static org.junit.Assert.*;

public class CreateValidateAuthz extends CommonUtils {
	
	public void populateAuthzRequest(TestDataReader reader, AuthorizeRequestType req, String request_id){
		 String resName = reader.getPreEntryValue(request_id, "request_resourceName");
	     String opName =  reader.getPreEntryValue(request_id, "request_operationName");
	     String resType = reader.getPreEntryValue(request_id, "request_resourceType");
	     
	     if(resName!=null){
	    	 if(resName.equals("null"))
	    		 req.setResourceName(null);
	    	 else
	    		 req.setResourceName(resName);
	     }
	     
	     if(opName!=null){
	    	 if(opName.equals("null"))
	    		 req.setOperationName(null);
	    	 else
	    		 req.setOperationName(opName);
	     }
	     
	     if(resType!=null){
	    	 if(resType.equals("null"))
	    		 req.setResourceType(null);
	    	 else
	    		 req.setResourceType(resType);
	     }
	}
	
	public void populateSubjectDetails(TestDataReader reader, AuthorizeRequestType req, String request_id){
		String reqsub = reader.getPreEntryValue(request_id,"request_subjects");
		SubjectType subject = null;
		
		if(reqsub!=null){
			String[] subKeys = reqsub.split(",");
			String[] values = null;
			
				for(String sub: subKeys){
					if(sub.isEmpty()){
						subject = new SubjectType();
						req.getSubject().add(subject);
					} else{
						 values = sub.split(":");
						 subject = new SubjectType();
						 
						 if(values[0].equals("null"))
							 subject.setValue(null);
						 else
							 subject.setValue(values[0]);
						 
						 if(values.length==1)
							 subject.setDomain("");
						 else if(values[1].equals("null"))
							 subject.setDomain(null);
						 else
							 subject.setDomain(values[1]);
						 req.getSubject().add(subject);
					}
			  }
		}
	}
	
   public void validateOutput(TestDataReader reader,AuthorizeResponseType result,String request_id){
	   String response = reader.getPreEntryValue(request_id,"response_ackvalue").trim();
	   if(response!=null){
		   if(result.getAck().toString().equalsIgnoreCase("SUCCESS")){
			   assertEquals("Should return succcess",response,result.getAck().toString());
			   assertTrue(result.getErrorMessage() == null);
		   }
		   else if(result.getAck().toString().equalsIgnoreCase("FAILURE")){
			   assertEquals("Should return succcess",response,result.getAck().toString());
			   assertTrue(result.getErrorMessage() != null);
		   }
	   }
   }
   
   public static int totalTestCount(Properties props){
		Pattern pattern = Pattern.compile("testcase(\\d+)"+"."+"number");
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
}
