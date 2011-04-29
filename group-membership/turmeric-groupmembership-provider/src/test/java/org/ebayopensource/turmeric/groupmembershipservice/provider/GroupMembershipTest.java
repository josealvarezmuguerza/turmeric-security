package org.ebayopensource.turmeric.groupmembershipservice.provider;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.ebayopensource.turmeric.security.v1.services.GroupMembersType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKey;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupKeyType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.junit.Test;

public class GroupMembershipTest extends GroupMembershipTestBase {
    @Test
    public void getSubjectGroupsBySubjectTest() throws Exception {
        SubjectType adminSubject = new SubjectType();
        adminSubject.setDomain("USER");
        adminSubject.setValue("admin");
        
        SubjectType userSubject = new SubjectType();
        userSubject.setDomain("USER");
        userSubject.setValue("asmith");
        
        GroupMembershipProviderImpl impl = new GroupMembershipProviderImpl();
        List<SubjectGroupType> groups = impl.getSubjectGroupsBySubject(adminSubject);
        assertEquals(2, groups.size());
        
        groups = impl.getSubjectGroupsBySubject(userSubject);
        assertEquals(1, groups.size());
        assertEquals("Everyone", groups.get(0).getName());
    }

    @Test
    public void getSubjectGroupByKey() throws Exception {
        SubjectGroupKeyType subjectGroupKey = new SubjectGroupKeyType();
        subjectGroupKey.setName("Admins");
        
        GroupMembershipProviderImpl impl = new GroupMembershipProviderImpl();
        GroupMembersType members = impl.getSubjectGroupByKey(subjectGroupKey);
        assertEquals(2, members.getMemberSubjects().size());
    }
}
