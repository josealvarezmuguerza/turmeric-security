package org.ebayopensource.turmeric.ratelimiter;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.ebayopensource.turmeric.runtime.spf.pipeline.SPFServlet;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import com.eviware.soapui.tools.SoapUITestCaseRunner;

public class SetupTest extends DBTestCase {
	private Server server;
	private HttpClient client;

	@Override
	protected IDataSet getDataSet() throws Exception {
		return new XmlDataSet(SetupTest.class.getClassLoader()
				.getResourceAsStream("export.xml"));

	}

	public SetupTest() {
		super();
		EntityManagerFactory factory = Persistence
				.createEntityManagerFactory("policyservice");
		System.setProperty(
				PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS,
				System.getProperty("driver", "org.hsqldb.jdbcDriver"));
		System.setProperty(
				PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL,
				System.getProperty("url", "jdbc:hsqldb:mem:turmeric"));
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME,
				System.getProperty("username", "sa"));
		String password = System.getProperty("password","");
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD,
				password != null ? password : "");

	}
	
	

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		startServer();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		stopServer();
	}

	public void startServer() throws Exception {
		server = new Server();
		Connector connector = new SelectChannelConnector();
		connector.setPort(8080);
		server.addConnector(connector);

		String contextPath = "/security";
		ServletContextHandler context = null;
		context = new ServletContextHandler(server, contextPath,
				ServletContextHandler.SESSIONS);

		String servletPath = "";
		ServletHolder servletHolder = null;
		// PolicyEnforcementServiceV1
		servletHolder = new ServletHolder(SPFServlet.class);
		servletHolder.setInitOrder(1);
		servletHolder.setInitParameter("SOA_SERVICE_NAME",
				"PolicyEnforcementService");
		servletHolder.setName("PolicyEnforcementServiceV1");
		context.addServlet(servletHolder, servletPath
				+ "/PolicyEnforcementServiceV1");
		// AuthorizationServiceV1
		servletHolder = new ServletHolder(SPFServlet.class);
		servletHolder.setInitOrder(1);
		servletHolder.setInitParameter("SOA_SERVICE_NAME",
				"AuthorizationService");
		servletHolder.setName("AuthorizationServiceV1");
		context.addServlet(servletHolder, servletPath
				+ "/AuthorizationServiceV1");
		// AuthenticationServiceV1
		servletHolder = new ServletHolder(SPFServlet.class);
		servletHolder.setInitOrder(1);
		servletHolder.setInitParameter("SOA_SERVICE_NAME",
				"AuthenticationService");
		servletHolder.setName("AuthenticationServiceV1");
		context.addServlet(servletHolder, servletPath
				+ "/AuthenticationServiceV1");

		// GroupMembershipServiceV1
		servletHolder = new ServletHolder(SPFServlet.class);
		servletHolder.setInitOrder(1);
		servletHolder.setInitParameter("SOA_SERVICE_NAME",
				"GroupMembershipService");
		servletHolder.setName("GroupMembershipServiceV1");
		context.addServlet(servletHolder, servletPath
				+ "/GroupMembershipServiceV1");

		// RateLimiterServiceV1
		servletHolder = new ServletHolder(SPFServlet.class);
		servletHolder.setInitOrder(1);
		servletHolder
				.setInitParameter("SOA_SERVICE_NAME", "RateLimiterService");
		servletHolder.setName("RateLimiterServiceV1");
		context.addServlet(servletHolder, servletPath + "/RateLimiterServiceV1");
		
		// PolicyServiceV1
		servletHolder = new ServletHolder(SPFServlet.class);
		servletHolder.setInitOrder(1);
		servletHolder
				.setInitParameter("SOA_SERVICE_NAME", "PolicyService");
		servletHolder.setName("PolicyServiceV1");
		context.addServlet(servletHolder, servletPath + "/PolicyServiceV1");

		server.start();
	}
	
	public void stopServer() throws Exception {
        if (client != null) {
            client.stop();
        }

        if (server != null) {
            server.stop();
            server.join();
        }
    }

	@Test
	public void testRL() throws Exception {
		SoapUITestCaseRunner runner = new SoapUITestCaseRunner();
		String soapuiProjectPath = System.getProperty("soapui-project");
		System.out.println("soapuiProjectPath="+soapuiProjectPath);
		runner.setProjectFile(soapuiProjectPath);
		runner.setPrintReport(true);
		runner.setJUnitReport(true);
		runner.setExportAll(true);
		runner.setOutputFolder("target/surefire-reports");
		runner.setIgnoreError(true);
		runner.run();

	}

	@Override
	protected DatabaseOperation getSetUpOperation() throws Exception {
		return DatabaseOperation.CLEAN_INSERT;
	}

}
