package org.ebayopensource.turmeric.policyservice.model;


public enum EffectType {
	ALLOW("Allow"),
    FLAG("Flag"),
    CHALLENGE("Challenge"),
    BLOCK("Block"),
    SOFTLIMIT("Softlimit");
    private final String value;

    EffectType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EffectType fromValue(String v) {
        for (EffectType c: EffectType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
