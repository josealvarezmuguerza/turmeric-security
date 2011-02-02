package org.ebayopensource.turmeric.test.services.authorizationservice;

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.ebayopensource.test.v1.services.CallInitializerRequest;
import org.ebayopensource.test.v1.services.CallInitializerResponse;
import org.ebayopensource.test.v1.services.GetAuthzCacheKeysRequest;
import org.ebayopensource.test.v1.services.GetAuthzCacheKeysResponse;
import org.ebayopensource.test.v1.services.ResOpType;
import org.ebayopensource.turmeric.common.v1.types.ErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.common.v1.types.ErrorParameter;
import org.ebayopensource.turmeric.runtime.common.exceptions.ServiceException;
import org.ebayopensource.turmeric.runtime.common.impl.utils.LogManager;
import org.ebayopensource.turmeric.runtime.common.types.SOAHeaders;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeResponseType;
import org.ebayopensource.turmeric.security.v1.services.SubjectGroupType;
import org.ebayopensource.turmeric.security.v1.services.SubjectType;
import org.ebayopensource.turmeric.services.authorizationservice.impl.biz.InternalAuthorizationServiceImpl;
import org.ebayopensource.turmeric.services.authorizationservice.intf.gen.BaseAuthorizationServiceConsumer;
import org.ebayopensource.turmeric.test.v1.services.admintestitemvalidation.intf.admintestitemvalidation.gen.SharedAdminTestItemValidationConsumer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ebay.kernel.bean.configuration.BeanConfigCategoryInfo;
import com.ebay.kernel.bean.configuration.ConfigCategoryCreateException;
import com.ebay.kernel.service.invocation.SvcChannelStatus;
import com.ebay.kernel.service.invocation.SvcInvocationConfig;
import com.ebay.kernel.service.invocation.actionmanager.RemoteSvcInvocationActionManagerAdapter;
import com.ebay.kernel.service.invocation.client.exception.BaseClientSideException;
import com.ebay.kernel.service.invocation.client.http.HttpClient;
import com.ebay.kernel.service.invocation.client.http.Request;
import com.ebay.kernel.service.invocation.client.http.Response;

/**
 * Utilities for testing the Authorization Service. 
 * 
 * @author mpoplacenel
 */
public class AuthorizationServiceTestUtils {
	
	private static final Logger LOGGER = LogManager.getInstance(AuthorizationServiceTestUtils.class);
	
	private static final String POKE_CACHE_PROPERTY_NAME = "pokeCache";
	
	private static final String REMOTE_URL = "http://localhost:8080/ws/spf";

	public static final String GET_POKER_URL = "http://localhost:8080/admin/v3console/ViewConfigCategoryXml"
		+ "?id=org.ebayopensource.turmeric.server.AuthorizationCache.Cache&forceXml=true";

	public static final String POKE_CACHE_URL = "http://localhost:8080/admin/v3console/UpdateConfigCategoryXml"
		+ "?id=org.ebayopensource.turmeric.server.AuthorizationCache.Cache&forceXml=true&" + POKE_CACHE_PROPERTY_NAME + "=true";

	public static final String SERVICE_NAME = "AdminTestItemValidation";

	public static final String OPERATION_NAME = "getAuthzCacheKeys";
	
	public static final String DOMAIN = "USER";
	
	public static final String USERNAME = "adminauthztest2";

	public static final String ADMIN_SVC_TOKEN = 
		"AgAAAA**AQAAAA**aAAAAA**yZQ7TA**nY+sHZ2PrBmdj6wVnY+sEZ2PrA2dj6AEkIKjCpSEqQidj6x9nY+seQ**tgAAAA**AAMAAA**iu2y6dwIdoKmBFX6KiJb5vT/xjhUBG24hWSUdh9mKs3KmWsiuuh5SyjkMlLVpWnqMtnu4lW61vUD+/QOLA9diCoe8rrSb1q5uVutc2G7q8GOBjZvfKgmJxI9RA0Y2EYXX7QxzueSHLICy3lVKaA2FjM65hcmIplxo9DDA9F+Zj/SRGRGw9pi/ChIftTCevtBYrGmsrRO6NV03bJaposmDgrcwM+AN7ZXPVM82LcJRnQO9EzYIDlM3e2VwEwG/ZcSMHKX1JSO8lul4+Z/QQ3tyxT5EHXlDGG7pbTTizpG5vhzDh5V/XWF85hxv4wWxK/xMljaBMNrrqAs0CWPB1XSpddiddRyQlhpw4rN9WobyUE/LEKsxJQNbfPoFcJkI22Zwx90urv8j1Oy4UX7/sDvO1fROnbbgtW5bcqnxwI/U7fGeRzT2HTt5BOn9a9QH5522TujepMCuc5Dgf4Aa9L4xfQhCyEZSUS4MC86Td3Vf+Bf7rvCV5/kqS1JibeIYHYvDCSO0xAr3c22obPV9s0FnSHGraAur9UVeZ/g8EffCIbQkj9qUtxOsLK5edFZ6P8815humCjbOt82Kz7EvYfzXecFffARCfRXMDnrONv3YzUoLsICzEi8QDmFA3oNxHK7aRUKaREeJjCmQVG6neD+GXoVpdFFJmLFJIwnton7i77wVf2M9wYDPNIY8IJyc374sUh87nzZwh8KgRR/i5nyqfFgdOjDgRuTHxf1BURpiCBihisO6o2uJv/zYjXPoR+8";

	private static final AuthorizationServiceTestUtils LOCAL_INSTANCE = new AuthorizationServiceTestUtils(false);
	
	private static final AuthorizationServiceTestUtils REMOTE_INSTANCE = new AuthorizationServiceTestUtils(true);
	
	public static final AuthorizationServiceTestUtils getInstance(boolean remote) {
		return remote ? REMOTE_INSTANCE : LOCAL_INSTANCE;
	}
	
	public static Response callURL(final String urlString)
	throws ConfigCategoryCreateException, MalformedURLException, BaseClientSideException {
		if (LOGGER.isLoggable(Level.FINE)) LOGGER.fine("Calling URL: " + urlString);
		SvcInvocationConfig svcInvocationConfig = new SvcInvocationConfig(
				BeanConfigCategoryInfo
						.createBeanConfigCategoryInfo(
								"org.ebayopensource.turmeric.authorizationservice.AuthorizationServiceCacheTest",
								null, "test", false, false, null, null, true),
				"AuthorizationServiceCacheTest", SvcChannelStatus.MARK_UP,
				"localhost", "8080", false, false);
		svcInvocationConfig.createConnectionConfig(4, 8);
		HttpClient httpClient = new HttpClient(svcInvocationConfig,
				new RemoteSvcInvocationActionManagerAdapter(
						svcInvocationConfig, 2, 10000));

		Request request = new Request(urlString);
		request.setMethod(Request.GET);

		return httpClient.invoke(request);
	}

	public static String getPropertyValueFromXML(String responseXML, String propertyName) {
		try {
			String value = null;
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(responseXML)));
			doc.getDocumentElement().normalize();
			NodeList listOfProperties = doc.getElementsByTagName("ConfigCategory");
			for (int i = 0; i < listOfProperties.getLength(); i++) {
	
				Node listOfProperty = listOfProperties.item(i);
				if (listOfProperty.getNodeType() == Node.ELEMENT_NODE) {
					Element firstPropertyElement = (Element) listOfProperty;
					NodeList list = firstPropertyElement
							.getElementsByTagName("attribute");
					for (int j = 0; j < list.getLength(); j++) {
						Element PropElement = (Element) list.item(j);
	
						String key = PropElement.getAttribute("name");
						if (key.contentEquals(propertyName))
							value = PropElement.getAttribute("value");
					}
	
				}
			}
			return value;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String getPokerXML() {
		try{
		    Response response = AuthorizationServiceTestUtils.callURL(GET_POKER_URL);
			if (response.getStatusCode() != 200) {
				throw new RuntimeException("HTTP client returned " + response.getStatusCode() 
						+ ": " + response.getMessage());
			}
			
			return response.getBody();
	} catch (RuntimeException e) {
		throw e;
	} catch (Exception e) {
		throw new RuntimeException(e);
	}
  }
	
	private final boolean remote;
	
	private AtomicBoolean initialized = new AtomicBoolean(false);
	
	private AuthorizationServiceTestUtils(boolean remote) {
		// private constructor to prevent instantiation
		this.remote = remote;
	}
	
	public boolean isRemote() {
		return remote;
	}

	public boolean pokeCache() {
		try {
			if (this.remote) {
				if (!initialized.getAndSet(true)) { // invoke once, to make config bean show up
					makeTestAuthzSvcCall();
				}
				Response response = callURL(POKE_CACHE_URL);
				if (LOGGER.isLoggable(Level.FINE)) LOGGER.fine("Got " + response.getBody());
				String valueStr = getPropertyValueFromXML(response.getBody(), POKE_CACHE_PROPERTY_NAME);
				return Boolean.valueOf(valueStr);
			} else {
				return InternalAuthorizationServiceImpl.getInstance().poke();
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> makeTestAuthzSvcCall() throws ServiceException,
			MalformedURLException {
		return invokeAuthzSvc(SERVICE_NAME, OPERATION_NAME, DOMAIN, USERNAME);
	}
	
	public boolean getPoker() {
		if (this.remote) {
			String xml = getPokerXML();
			String valueStr = AuthorizationServiceTestUtils.getPropertyValueFromXML(xml, POKE_CACHE_PROPERTY_NAME);
			return Boolean.valueOf(valueStr);
		} else {
			return false; // InternalAuthorizationServiceImpl.getInstance().getPoker();
		}
	}
	
	public List<String> invokeAuthzSvc(String svcNm, String opNm, String domain, String subjNm) 
	throws ServiceException, MalformedURLException {
		AuthorizeResponseType authzResp = authorize(svcNm,
				opNm, domain, subjNm);
		
		List<SubjectGroupType> resolvedSGs = authzResp.getResolvedSubjectGroup();
		List<String> sgNames = new ArrayList<String>(resolvedSGs.size());
		for (SubjectGroupType sg : resolvedSGs) {
			sgNames.add(sg.getName());
		}
		
		return sgNames;
	}

	/**
	 * Invoke the authorization service.
	 * 
	 * @param svcNm service name
	 * @param opNm operation name
	 * @param domain subject domain/type
	 * @param subjNm subject name
	 * 
	 * @return the response object
	 * 
	 * @throws ServiceException
	 * @throws MalformedURLException
	 */
	public AuthorizeResponseType authorize(String svcNm, String opNm, String domain, String subjNm) 
	throws ServiceException, MalformedURLException {
		
		BaseAuthorizationServiceConsumer authzConsm;
		if (this.remote) {
			authzConsm = new BaseAuthorizationServiceConsumer("AuthzRemote");
			authzConsm.getService().getInvokerOptions().setTransportName("HTTP11");
			authzConsm.getService().setServiceLocation(new URL(REMOTE_URL));
		} else {
			authzConsm = new BaseAuthorizationServiceConsumer();
			authzConsm.getService().getInvokerOptions().setTransportName("LOCAL");
		}
		
		SubjectType subject = new SubjectType();
		subject.setDomain(domain);
		subject.setValue(subjNm);
		
		AuthorizeRequestType authzReq = new AuthorizeRequestType();
		authzReq.setResourceType("SERVICE");
		authzReq.setResourceName(svcNm);
		authzReq.setOperationName(opNm);
		authzReq.getSubject().add(subject);
		
		AuthorizeResponseType authzResp = authzConsm.authorize(authzReq);
		
		return authzResp;
	}
	
	public List<String> callInitializer(String initializerClassName, List<String> serviceNames) {
		CallInitializerRequest req = new CallInitializerRequest();
		req.setInitializerClassName(initializerClassName);
		req.getServiceName().addAll(serviceNames);
		
		CallInitializerResponse resp;
		try {
			SharedAdminTestItemValidationConsumer adminSvcConsumer = createAdminServiceConsumer(ADMIN_SVC_TOKEN);
			resp = adminSvcConsumer.callInitializer(req);
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		}
		return resp.getServiceName();
	}
	
	////////////////////// GET-STATS ////////////////////////////////
	public GetAuthzCacheKeysResponse getAuthzCacheKeys(GetAuthzCacheKeysRequest req) throws ServiceException {
		return getAuthzCacheKeys(req, ADMIN_SVC_TOKEN);
	}
	
	public GetAuthzCacheKeysResponse getAuthzCacheKeys(final GetAuthzCacheKeysRequest req, final String token) 
	throws ServiceException {
		return getAuthzCacheKeys(req,token,null);
	}
	
	public GetAuthzCacheKeysResponse getAuthzCacheKeys(final GetAuthzCacheKeysRequest req, final String token, final String header) 
	throws ServiceException {
		SharedAdminTestItemValidationConsumer adminSvcConsumer = createAdminServiceConsumer(token,header);
		
		return adminSvcConsumer.getAuthzCacheKeys(req);
	}
	
	private SharedAdminTestItemValidationConsumer createAdminServiceConsumer(
			final String token) throws ServiceException {
		return createAdminServiceConsumer(token,null);
	}

	private SharedAdminTestItemValidationConsumer createAdminServiceConsumer(
			final String token, final String header) throws ServiceException {
		SharedAdminTestItemValidationConsumer adminSvcConsumer = this.remote 
			? new SharedAdminTestItemValidationConsumer("AdminTestItemValidationConsumer", "remote")
			: new SharedAdminTestItemValidationConsumer("AdminTestItemValidationConsumer", "local");
		if (!this.remote) {
			adminSvcConsumer.getServiceInvokerOptions().setTransportName("LOCAL");
			try {
				adminSvcConsumer.getService().setServiceLocation(new URL("http://localhost:8080/ws/spf"));
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
		if(header!=null)
			adminSvcConsumer.getService().setSessionTransportHeader(
        		header,
        		token);
		else
			adminSvcConsumer.getService().setSessionTransportHeader(
	        		SOAHeaders.AUTH_TOKEN,
	        		token);
		return adminSvcConsumer;
	}
	
	public GetAuthzCacheKeysResponse invokeAdminService_getAuthzCacheKeys() 
	throws ServiceException {
		return invokeAdminService_getAuthzCacheKeys(ADMIN_SVC_TOKEN);
	}
	
	public GetAuthzCacheKeysResponse invokeAdminService_getAuthzCacheKeys(final String token) 
	throws ServiceException {
		return invokeAdminService_getAuthzCacheKeys(token,null);
	}
	
	public GetAuthzCacheKeysResponse invokeAdminService_getAuthzCacheKeys(final String token, final String header) 
	throws ServiceException {
		GetAuthzCacheKeysRequest authzCachePolStatReq = new GetAuthzCacheKeysRequest();
		ResOpType resOpType = new ResOpType();
		authzCachePolStatReq.setResOp(resOpType);
		
		authzCachePolStatReq.setSGName("All");
		resOpType.setResourceName("All");
		
		return getAuthzCacheKeys(authzCachePolStatReq, token, header);
	}

	public static String errorMessageToString(ErrorMessage errorMessage) {
		if (errorMessage == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (ErrorData ed : errorMessage.getError()) {
			sb
				.append("id=").append(ed.getErrorId())
				.append('\n')
				.append("category=").append(ed.getCategory())
				.append('\n')
				.append("domain=").append(ed.getDomain())
				.append('\n')
				.append("subdomain=").append(ed.getSubdomain())
				.append('\n')
				.append("message=").append(ed.getMessage())
				.append('\n')
				.append("exceptionId=").append(ed.getExceptionId())
				.append('\n')
				.append("params=[");
			final List<ErrorParameter> errParamList = ed.getParameter();
			sb.append(errorParametersToString(errParamList));
		}
		return sb.toString();
	}

	/**
	 * Append the specified error parameters to the given {@link StringBuilder}.
	 * 
	 * @param sb buffer to append to
	 * @param errParamList the error parameter list
	 */
	public static String errorParametersToString(final List<ErrorParameter> errParamList) {
		int i = 0;
		StringBuilder sb = new StringBuilder();
		for (ErrorParameter ep : errParamList) {
			if (i++ > 0) {
				sb.append(", \n\t");
			}
			sb.append(ep.getName()).append('=').append(ep.getValue());
		}
		
		return sb.toString();
	}
	

	
}