package org.ebayopensource.turmeric.authentication.model;

import javax.persistence.Entity;

import org.ebayopensource.turmeric.utils.jpa.model.AuditablePersistent;

@Entity
public class BasicAuth extends AuditablePersistent{
    private String subjectName;
    private String password;
    
    public BasicAuth() {}

    public BasicAuth(String subjectName, String password) {
        this.subjectName = subjectName;
        this.password = password;
    }
    
    public String getSubjectName() {
        return subjectName;
    }
    
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
