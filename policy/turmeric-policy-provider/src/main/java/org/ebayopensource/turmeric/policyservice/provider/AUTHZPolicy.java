package org.ebayopensource.turmeric.policyservice.provider;

import org.ebayopensource.turmeric.security.v1.services.EffectType;

public class AUTHZPolicy extends ListPolicyBase {

	@Override
	protected String getPolicyType() {
		return "AUTHZ";
	}

	@Override
	protected String getAction() {
	       return EffectType.ALLOW.name();
	}

}
