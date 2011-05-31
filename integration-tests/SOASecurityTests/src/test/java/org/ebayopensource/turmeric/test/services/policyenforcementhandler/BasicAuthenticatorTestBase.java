package org.ebayopensource.turmeric.test.services.policyenforcementhandler;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.ebayopensource.turmeric.policyservice.model.BasicAuth;
import org.ebayopensource.turmeric.policyservice.model.Subject;
import org.ebayopensource.turmeric.policyservice.model.SubjectGroup;
import org.ebayopensource.turmeric.policyservice.model.SubjectType;
import org.ebayopensource.turmeric.utils.jpa.AbstractDAO;
import org.ebayopensource.turmeric.utils.jpa.JPAAroundAdvice;
import org.junit.Before;

public abstract class BasicAuthenticatorTestBase extends AbstractJPATest{
    private TestDAO testDAO;
    private static final String[][] typeData = {
        {"APP", "Application"},
        {"DEV", "Developer"},
        {"IP", "IP address"},
        {"USER", "User"},
    };
    
    private static final String[][] subjectData = {
        {"jdoe", "USER", "John Doe", "", "jdoe@ebayopensource.org"},
        {"msmith", "USER", "Mary Smith", "", "msmith@ebayopensource.org"},
        {"bwhite", "USER", "Brian White", "", "djames@ebayopensource.org"},
    };
    
    private static final String[][] subjectGroupData = {
        {"everybody", "USER", "", "Everyone"},  
        {"managers", "USER", "", "Managers"},  
    };
    
    private static final String[][] basicAuthData = {
        {"jdoe","secret"},
    };
    
    @Before
    public void initDAO() {
        ClassLoader classLoader = TestDAO.class.getClassLoader();
        Class[] interfaces = {TestDAO.class};
        TestDAO target = new TestDAOImpl();
        testDAO = (TestDAO) Proxy.newProxyInstance(classLoader, interfaces, new JPAAroundAdvice(factory, target));
        
        initDatabase();
    }
    
    protected void initDatabase() {
        for (String[] type : typeData) {
            testDAO.persistEntity(new SubjectType(type[0], type[1], false));
        }
        
        List<Subject> jpaSubjects = new ArrayList<Subject>();
        for (String[] subjectField : subjectData) {
            Subject jpaSubject = new Subject(subjectField[0], subjectField[1], subjectField[2], subjectField[3], 0, subjectField[4]);
            jpaSubjects.add(jpaSubject);
            testDAO.persistEntity(jpaSubject);
        }

        int idx = 0;
        for (String[] subjectGroupField : subjectGroupData) {
            SubjectGroup jpaSubjectGroup = new SubjectGroup(subjectGroupField[0], subjectGroupField[1], subjectGroupField[2], true, true, subjectGroupField[3]);
            if (idx++ == 0) {
                jpaSubjectGroup.getSubjects().addAll(jpaSubjects);
            } else {
                jpaSubjectGroup.getSubjects().add(jpaSubjects.get(0));
            }
            testDAO.persistEntity(jpaSubjectGroup);
        }
        
        for (String[] basicAuth : basicAuthData) {
            testDAO.persistEntity(new BasicAuth(basicAuth[0], basicAuth[1]));
        }
    }

    public static interface TestDAO {
        public void persistEntity(Object entity);
    }
    
    public static class TestDAOImpl extends AbstractDAO implements TestDAO{
        @Override
		public void persistEntity(Object entity) {
            try {
                super.persistEntity(entity);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }
}
