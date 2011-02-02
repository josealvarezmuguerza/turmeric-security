package org.ebayopensource.turmeric.policyservice.model;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.persistence.Entity;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.ebayopensource.turmeric.policyservice.exceptions.PolicyProviderException.Category;
import org.ebayopensource.turmeric.security.v1.services.EntityHistory;
import org.ebayopensource.turmeric.security.v1.services.OperationKey;
import org.ebayopensource.turmeric.security.v1.services.PolicyKey;
import org.ebayopensource.turmeric.security.v1.services.ResourceKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectKey;
import org.ebayopensource.turmeric.utils.jpa.model.AuditInfo;
import org.ebayopensource.turmeric.utils.jpa.model.AuditablePersistent;

@Entity
public class AuditHistory extends AuditablePersistent {
    private static final String UNKNOWN = "unknown";

	private static final String AUDIT_TEMPLATE = "%1$s[%4$s:%3$s:%2$d] %5$s @[%8$s:%7$s:%6$d]";
    
	private String category;
	private long entityId;
	private String entityName;
	private String entityType;
	private String operationType;
	private long subjectId;
	private String subjectName;
	private String subjectType;
	private String comment;
	
	public AuditHistory() {}
		
	public AuditHistory(String category, long entityId, String entityName, String entityType,
						String operationType, long subjectId, String subjectName, String subjectType)
	{
		this.category = category;
		this.entityId = entityId;
		this.entityName = entityName;
		this.entityType = entityType;
		this.operationType = operationType;
		this.subjectId = subjectId;
		this.subjectName = subjectName;
		this.subjectType = subjectType;
		this.comment = String.format(AUDIT_TEMPLATE, category, entityId, entityName, entityType,
		                operationType, subjectId, subjectName, subjectType);
	}
	
	public String getCategory() {
		return category;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public long getEntityId() {
		return entityId;
	}
	
	public void setEntityId(long entityId) {
		this.entityId = entityId;
	}
	
	public String getEntityName() {
		return entityName;
	}
	
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	
	public String getEntityType() {
		return entityType;
	}
	
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	
	public String getOperationType() {
		return operationType;
	}
	
	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	
	public long getSubjectId() {
		return subjectId;
	}
	
	public void setSubjectId(long subjectId) {
		this.subjectId = subjectId;
	}
	
	public String getSubjectName() {
		return subjectName;
	}
	
	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}
	
	public String getSubjectType() {
		return subjectType;
	}
	
	public void setSubjectType(String subjectType) {
		this.subjectType = subjectType;
	}

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }   
    
	public static AuditHistory newRecord(SubjectKey subjectKey, String operationType, SubjectKey loginSubject) {
		String subjectName = UNKNOWN;
		long subjectId = 0;
		String subjectType = UNKNOWN;
		if (loginSubject != null) {
			subjectName = loginSubject.getSubjectName();
			subjectId = loginSubject.getSubjectId();
			subjectType = loginSubject.getSubjectType();
		}
		return new AuditHistory( Category.SUBJECT.name(), subjectKey.getSubjectId(), 
			subjectKey.getSubjectName(), subjectKey.getSubjectType(),
			operationType, subjectId, subjectName, subjectType);
	}

	public static AuditHistory newRecord(SubjectGroupKey subjectGroupKey, String operationType, SubjectKey loginSubject) {
		String subjectName = UNKNOWN;
		long subjectId = 0;
		String subjectType = UNKNOWN;
		if (loginSubject != null) {
			subjectName = loginSubject.getSubjectName();
			subjectId = loginSubject.getSubjectId();
			subjectType = loginSubject.getSubjectType();
		}
		return new AuditHistory(Category.SUBJECTGROUP.name(), subjectGroupKey.getSubjectGroupId(),
			subjectGroupKey.getSubjectGroupName(), subjectGroupKey.getSubjectType(),
			operationType, subjectId, 
			subjectName, subjectType);
	}

	public static AuditHistory newRecord(ResourceKey resourceKey, String operationType, SubjectKey loginSubject) {
		String subjectName = UNKNOWN;
		long subjectId = 0;
		String subjectType = UNKNOWN;
		if (loginSubject != null) {
			subjectName = loginSubject.getSubjectName();
			subjectId = loginSubject.getSubjectId();
			subjectType = loginSubject.getSubjectType();
		}
		return new AuditHistory(Category.RESOURCE.name(), resourceKey.getResourceId(),
			resourceKey.getResourceName(), resourceKey.getResourceType(),
			operationType, subjectId, subjectName, subjectType);
	}
	
	public static AuditHistory newRecord(OperationKey operationKey, String operationType, SubjectKey loginSubject) {
		String subjectName = UNKNOWN;
		long subjectId = 0;
		String subjectType = UNKNOWN;
		if (loginSubject != null) {
			subjectName = loginSubject.getSubjectName();
			subjectId = loginSubject.getSubjectId();
			subjectType = loginSubject.getSubjectType();
		}
		return new AuditHistory(Category.OPERATION.name(), operationKey.getOperationId(),
			operationKey.getOperationName(), operationKey.getResourceType(),
			operationType, subjectId, subjectName, subjectType);
	}
	
	public static AuditHistory newRecord(PolicyKey policyKey, String operationType, SubjectKey loginSubject) {
		String subjectName = UNKNOWN;
		long subjectId = 0;
		String subjectType = UNKNOWN;
		if (loginSubject != null) {
			subjectName = loginSubject.getSubjectName();
			subjectId = loginSubject.getSubjectId();
			subjectType = loginSubject.getSubjectType();
		}
		return new AuditHistory(Category.POLICY.name(), policyKey.getPolicyId(),
			policyKey.getPolicyName(), policyKey.getPolicyType(),
			operationType, subjectId, subjectName, subjectType);
	}

    public static EntityHistory convert(AuditHistory auditEntry) {
        
        XMLGregorianCalendar xgcDate = null;
        try {
            GregorianCalendar gcDate = new GregorianCalendar();
            AuditInfo auditInfo = auditEntry.getAuditInfo();
            Date auditDate = auditInfo.getUpdatedOn();
            gcDate.setTime(auditDate == null ? auditInfo.getCreatedOn() : auditDate);
            xgcDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcDate);
        }
        catch (DatatypeConfigurationException ex) {}
        
        EntityHistory entityEntry = new EntityHistory();
        entityEntry.setAuditDate(xgcDate);
        entityEntry.setLoginSubject(auditEntry.getSubjectName());
        entityEntry.setAuditType(auditEntry.getOperationType());
        entityEntry.setComments(auditEntry.getComment());
        return entityEntry;
    }
}
