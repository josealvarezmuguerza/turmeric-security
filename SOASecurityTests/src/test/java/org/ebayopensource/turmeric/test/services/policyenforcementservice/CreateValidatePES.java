package org.ebayopensource.turmeric.test.services.policyenforcementservice;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ebayopensource.turmeric.security.v1.services.KeyValuePair;
import org.ebayopensource.turmeric.security.v1.services.OperationKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.security.v1.services.VerifyAccessRequest;
import org.ebayopensource.turmeric.test.services.utils.CommonUtils;
import org.ebayopensource.turmeric.test.services.utils.SecurityTokenUtility;
import org.ebayopensource.turmeric.test.services.utils.TestDataReader;

public class CreateValidatePES extends CommonUtils {

	public void populatePESRequest(SecurityTokenUtility util,
			TestDataReader reader, VerifyAccessRequest request,
			String request_id) throws Exception {
		OperationKey opKey = null;
		String resName = reader.getPreEntryValue(request_id,
				"request_resourceName");
		String resType = reader.getPreEntryValue(request_id,
				"request_resourceType");
		String opName = reader.getPreEntryValue(request_id,
				"request_operationName");
		if (resName.equals("null") && resType.equals("null")
				&& opName.equals("null")) {
			opKey = null;
		} else {
			opKey = new OperationKey();
			if (!resName.equals("null"))
				opKey.setResourceName(resName);
			else
				opKey.setResourceName(null);
			if (!resType.equals("null"))
				opKey.setResourceType(resType);
			else
				opKey.setResourceType(null);
			if (!opName.equals("null"))
				opKey.setOperationName(opName);
			else
				opKey.setOperationName(null);
		}

		request.setOperationKey(opKey);

		String pType = null;
		// List<String> policTypes = null;
		String value = reader.getPreEntryValue(request_id,
				"request_policytypes");
		if (value != null && !value.equals("null")) {
			// policTypes = new ArrayList<String>();
			String[] types = value.split(",");
			for (String v : types) {
				request.getPolicyType().add(v);
				// if(!v.isEmpty())
				// pType = PolicyType.valueOf(v);
				// policTypes.add(pType);
			}
		}

		// request.getPolicyType().addAll(policTypes);
		//rlcalcsvctest1:userid,passwd:password
		String reqsub = reader.getPreEntryValue(request_id,
				"request_credentialsubjects");
		String credential = reader.getPreEntryValue(request_id,
				"request_credential");//BASIC
		if (reqsub != null && !reqsub.equals("")) {
			String[] credentials = reqsub.split(",");
			for (String cred : credentials) {
				if (credential != null && !credential.equals("")) {
//					HashMap<String, String> mp = (HashMap) util
//							.getSecurityToken(credential,
//									createsubjectMap(credential, reqsub));
					String[] credPair = cred.split(":");
					KeyValuePair credType = new KeyValuePair();
					credType.setKey(credPair[1]);//<-userid, password
					credType.setValue(credPair[0]);//<-rlcalcsvctest1, passwd
					request.getCredential().add(credType);
				}
			}

		}

		String vanillaSub = reader.getPreEntryValue(request_id,
				"request_subjects");
		if (vanillaSub != null && vanillaSub.equals("null")) {
			SubjectType subject = null;
			request.getSubject().add(subject);
		} else if (vanillaSub != null && !vanillaSub.equals("")) {
			String[] subs = vanillaSub.trim().split(",");
			String[] vals = null;
			SubjectType subject = null;
			for (String v : subs) {
				vals = v.split(":");
				subject = new SubjectType();
				if (vals.length == 1)
					subject.setDomain("");
				else if (!vals[1].equals("null"))
					subject.setDomain(vals[1]);
				else
					subject.setDomain(null);
				if (!vals[0].equals("null"))
					subject.setValue(vals[0]);
				else
					subject.setValue(null);
				request.getSubject().add(subject);
			}
		}

		String additionalcredential = reader.getPreEntryValue(request_id,
				"request_additionalcredential_namevalue");
		if (additionalcredential != null && !additionalcredential.equals("")) {
			String[] keys = additionalcredential.trim().split(":");
			KeyValuePair credType = new KeyValuePair();
			credType.setKey(keys[0]);
			credType.setValue(keys[1]);
			request.getCredential().add(credType);
		}

	}

	public HashMap<String, String> createsubjectMap(String credential,
			String reqsub) {
		HashMap hm = new HashMap();
		if (!reqsub.equals("") && reqsub != null) {
			String[] subKeys = reqsub.split(",");
			String[] values = null;
			for (String sub : subKeys) {
				values = sub.split(":");
				if (values.length == 1)
					hm.put(credential, values[0]);
				else
					hm.put(values[1], values[0]);
			}
		}
		return hm;
	}

	public static int totalTestCount(Properties props) {
		Pattern pattern = Pattern.compile("testcase(\\d+)" + "." + "number");
		Matcher matcher = null;
		Set s = props.keySet();
		Iterator ite = s.iterator();

		int max = 0;
		int value;
		while (ite.hasNext()) {
			matcher = pattern.matcher(ite.next().toString());
			if (matcher.find()) {
				value = Integer.parseInt(matcher.group(1));
				if (value > max)
					max = value;
			}
		}
		System.out.println("max value= " + max);
		return max;
	}
}
