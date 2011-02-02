package org.ebayopensource.turmeric.groupmembershipservice.provider;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.utils.DomParseUtils;
import org.ebayopensource.turmeric.utils.config.exceptions.PolicyProviderException;
import org.ebayopensource.turmeric.utils.config.impl.BaseFilePolicyProvider;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class CalculatedGroupMembershipProviderImpl extends
		BaseFilePolicyProvider {
	
	protected static final String CALCULATED_SUBJECT_GROUP_CONFIG_FILENAME = "CalculatedSubjectGroupConfig.xml";

	protected static final String CALCULATED_SUBJECT_GROUP_CONFIG_SCHEMA = "CalculatedSubjectGroupConfig.xsd";

	protected static final String CALCULATED_SUBJECT_GROUP_CONFIG_ROOT_ELEMENT = "subject-group-config";

	protected Map<String, SubjectGroupType> m_subjectGroupToSubjectGroupClassInfoCache = new HashMap<String, SubjectGroupType>();
	
	private static Logger s_logger = LogManager.getInstance(CalculatedGroupMembershipProviderImpl.class);
	
	private static CalculatedGroupMembershipProviderImpl s_instance = new CalculatedGroupMembershipProviderImpl();

	private CalculatedGroupMembershipProviderImpl() {	
	}
	
	public static CalculatedGroupMembershipProviderImpl getInstance() {
		return s_instance;
	}
	
	
	@Override
	protected String getPolicyFileName() {
		return m_policyPath  + CALCULATED_SUBJECT_GROUP_CONFIG_FILENAME;
	}

	@Override
	protected String getPolicyRootElement() {
		return CALCULATED_SUBJECT_GROUP_CONFIG_ROOT_ELEMENT;
	}

	@Override
	protected String getPolicySchemaName() {
		return m_schemaPath + CALCULATED_SUBJECT_GROUP_CONFIG_SCHEMA;
	}

	@Override
	protected void mapPolicyData(Element policyData)
			throws PolicyProviderException {
		if (policyData == null)
			return;

		m_subjectGroupToSubjectGroupClassInfoCache.clear();
		
		try {
			NodeList subjectGroupList = DomParseUtils
					.getImmediateChildrenByTagName(
							policyData, "subject-group");
			if (subjectGroupList == null || subjectGroupList.getLength() <= 0)
				return;

			// iterate through the subject group list
			for (int i = 0; i < subjectGroupList.getLength(); i++) {
				Element subjectGroupElement = (Element) subjectGroupList
						.item(i);
				String subjectGroupName = DomParseUtils.getElementText(
						getPolicyFileName(), subjectGroupElement, "name");
				String subjectGroupType = DomParseUtils.getElementText(
						getPolicyFileName(), subjectGroupElement, "subjectType");
				String subjectGroupClassName = DomParseUtils.getElementText(
						getPolicyFileName(), subjectGroupElement, "className");

				SubjectGroupType sg = new SubjectGroupType();
				sg.setName(subjectGroupName);
				sg.setDomain(subjectGroupType);
				sg.setCalculator(subjectGroupClassName);
				
				m_subjectGroupToSubjectGroupClassInfoCache.put(subjectGroupName,
						sg);
					
			}
		} catch (Exception e) {
			s_logger.log(Level.SEVERE, "error", e);
			throw new PolicyProviderException(
					"Error in mapping calculated subject group policy: "
							+ e.getMessage(), e);
		}
		
	}
	
	public String getCalculatedSubjectGroupConfigFileName() {
		return m_policyPath + CALCULATED_SUBJECT_GROUP_CONFIG_FILENAME;
	}

	public String getCalculatedSubjectGroupConfigSchemaName() {
		return m_schemaPath + CALCULATED_SUBJECT_GROUP_CONFIG_SCHEMA;
	}

	public String getCalculatedSubjectGroupConfigRootElement() {
		return CALCULATED_SUBJECT_GROUP_CONFIG_ROOT_ELEMENT;
	}

	public SubjectGroupType getCalculatedSG(SubjectGroupType subjectGroup) {

		return m_subjectGroupToSubjectGroupClassInfoCache.get(subjectGroup.getCalculator());
		
	}
	
	public Collection<SubjectGroupType> getAllCalculatedSGs() {
		return m_subjectGroupToSubjectGroupClassInfoCache.values();
	}

}
