/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.services.authorizationservice.impl.biz;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.Set;

import org.ebayopensource.turmeric.common.v1.types.AckValue;
import org.ebayopensource.turmeric.common.v1.types.CommonErrorData;
import org.ebayopensource.turmeric.common.v1.types.ErrorMessage;
import org.ebayopensource.turmeric.common.v1.types.ErrorParameter;
import org.ebayopensource.turmeric.errorlibrary.turmericsecurity.ErrorConstants;
import org.ebayopensource.turmeric.runtime.common.exceptions.ErrorDataFactory;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeRequestType;
import org.ebayopensource.turmeric.security.v1.services.AuthorizeResponseType;
import org.ebayopensource.turmeric.services.authorizationservice.impl.AuthorizationException;

import com.ebay.kernel.logger.LogLevel;
import com.ebay.kernel.logger.Logger;

/**
 * This class encompasses the top-level orchestration logic of the new Authorization Service Impl.
 * It must be a singleton, as it holds the Authorization Service Cache.   
 * 
 * @author dmuthiayen (orig SecurityFramework.AuthorizationServiceImpl this is derived from), mpoplacenel
 */
public class InternalAuthorizationServiceImpl {
	
	/**
	 * cacheOn property name. 
	 */
	public static final String CACHE_ON_PROP = "cacheOn";

	/**
	 * Configuration file name. 
	 */
	public static final String CONFIG_FILE_NAME = "AuthorizationServiceConfig.properties";

	/**
	 * Refresh interval property name. 
	 */
	public static final String REFRESH_INTERVAL_PROP = "refreshInterval";

	/**
	 * Start time of day property name. 
	 */
	public static final String START_TIME_OF_DAY_PROP = "startTimeOfDay";

	/**
	 * Default refresh interval (0). 
	 */
	public static final long DEFAULT_REFRESH_INTERVAL = 0L;
	
	/**
	 * Default start time of the day (2 am). 
	 */
	public static final int DEFAULT_START_TIME_OF_DAY = 7200;
	
	/**
	 * Configuration base path. 
	 */
	public static final String CONFIG_BASE_PATH = "META-INF/security/";
	
	/**
	 * Configuration folder name. 
	 */
	public static final String CONFIG_FOLDER = "config";
	
	/**
	 * Cache name. 
	 */
	public static final String AUTHZ_CACHE_NAME = "AuthorizationCache";

	private static final Logger LOGGER = Logger.getInstance(InternalAuthorizationServiceImpl.class);
	
	private static String s_configFolder = CONFIG_FOLDER;
	
	private static final InternalAuthorizationServiceImpl INSTANCE = new InternalAuthorizationServiceImpl();
	
	/**
	 * Singleton instance accessor. 
	 * @return the singleton instance. 
	 */
	public static InternalAuthorizationServiceImpl getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Changes the configuration base folder path (as a resource). This method should
	 * only be used by tests!
	 * 
	 * @param configResourcePath the new value. 
	 * @return the old value. 
	 */
	public static synchronized String setConfiguration(String configResourcePath) {
		String oldConfigFolder = s_configFolder;
		s_configFolder = configResourcePath;
		
		return oldConfigFolder;
	}
	
	/**
	 * Getter for the configuration folder. 
	 * @return the configuration folder. 
	 */
	public static synchronized String getConfiguration() {
		return s_configFolder;
	}
	
	private static int parseIntOption(final Properties props, final String optionName) {
		String optionStr = props.getProperty(optionName);
		int optionInt = 0;
		if (optionStr != null) {
			try {
				optionInt = Integer.parseInt(optionStr);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid value for " + optionName, e);
			}
		}
		
		return optionInt;
	}
	
	/**
	 * Configuration bean. 
	 */
	protected final InternalAuthorizerConfig m_config;
	
	/**
	 * Initialization exception - captured and served upon any subsequent
	 * invocation (had any occurred, of course). 
	 */
	private final Throwable m_initExc;
	
	/**
	 * The helper class carrying out the actual business. 
	 */
	protected final Authorizer m_authorizer;
	
	/**
	 * protected constructor - only to be used when (and if) sub-classed.
	 */
	protected InternalAuthorizationServiceImpl() {
		Throwable tmpInitExc = null;
		Authorizer authorizer = null;
		Properties configProperties = null;
		InternalAuthorizerConfig config = new InternalAuthorizerConfig(true, 
				DEFAULT_REFRESH_INTERVAL, DEFAULT_START_TIME_OF_DAY);
		try {
			// load the authorizer configured in the system
			final String configFolder = getConfiguration();
			final String fullResourcePath = CONFIG_BASE_PATH + configFolder + "/" + CONFIG_FILE_NAME;
			try {
				InputStream cfgIS = getClass().getClassLoader().getResourceAsStream(fullResourcePath);
				if (cfgIS == null) {
					throw new IllegalStateException("Could not find configuration resource " + fullResourcePath);
				}
				configProperties = new Properties();
				configProperties.load(cfgIS);
				
				config = initFromProperties(configProperties);
			} catch (Throwable thr) {
				LOGGER.log(LogLevel.ERROR, "Could not load configuration resource " + fullResourcePath, thr);
			}
			authorizer = createAuthorizer(config);
		} catch (Throwable thr) {
			tmpInitExc = thr;
			LOGGER.log(LogLevel.ERROR, thr);
		}
		m_authorizer = authorizer;
		m_initExc = tmpInitExc;
		m_config = config;
	}
	
	/**
	 * Creates a new authorizer with the specified configuration. 
	 * @param config the authorizer configuration. 
	 * @return the newly-created authorizer. 
	 */
	protected Authorizer createAuthorizer(InternalAuthorizerConfig config) {
		return new Authorizer(config.isCacheOn(), config.getRefreshInterval(), config.getStartTimeOfDay());
	}

	/**
	 * Creates a configuration bean from the given properties. 
	 * @param configProperties the configuration properties
	 * @return the newly-created configuration bean. 
	 */
	protected InternalAuthorizerConfig initFromProperties(Properties configProperties) {
		boolean cacheOn = Boolean.valueOf(configProperties.getProperty(CACHE_ON_PROP));
		long refreshInterval = parseIntOption(configProperties, REFRESH_INTERVAL_PROP);
		int startTimeOfDay = parseIntOption(configProperties, START_TIME_OF_DAY_PROP);
		
		return new InternalAuthorizerConfig(cacheOn, refreshInterval, startTimeOfDay);
	}
	
	/**
	 * {@link Authorizer#initialize(String) Initializes} the authorizer for the given service. 
	 * @param serviceName the service to run the init sequence for. 
	 * @throws AuthorizationException for problems encountered during the init sequence. 
	 */
	public void initialize(String serviceName) throws AuthorizationException {
		// initialize the cache
		m_authorizer.initialize(serviceName);
	}
	
	/**
	 * {@link Authorizer#poke() Pokes} the authorizer's cache. 
	 * @return <code>{@link Authorizer#poke() authorizer.poke()}</code>
	 */
	public boolean poke() {
		return m_authorizer.poke();
	}
	
	/**
	 * Authorizes the incoming request. This method will only throw in FUBAR sort of
	 * circumstances, otherwise it will report the problems nicely as part of the RRE.
	 * 
	 * @param authzRequest the request to authorize.
	 * 
	 * @return the response (potentially containing an error). 
	 */
	public AuthorizeResponseType authorize(AuthorizeRequestType authzRequest) {
		try {
			return authorizeOrThrow(authzRequest);
		} catch (AuthorizationException e) {
			// All system errors come in the form of exception.
			// Here, map the exception to error response,
			// as we never throw exception from service impl
			if (LOGGER.isLogEnabled(LogLevel.ERROR)) {
				LOGGER.log(LogLevel.ERROR, "Exception thrown while authorize() request: " + e.getMessage(), e);
			}
			//e.printStackTrace();
			return mapError(authzRequest, e);
		}
	}
	
	/**
	 * Tells if the cache is ON. 
	 * @return <code>true</code> if cache is ON, <code>false</code> otherwise. 
	 */
	public boolean isCacheOn() {
		return m_authorizer.isCacheOn();
	}
	
	/**
	 * Sets the cache ON or OFF. 
	 * @param newValue <code>true</code> for ON, <code>false</code> for OFF. 
	 * @return <code>{@link Authorizer#setCacheOn(boolean) authorizer.setCacheOn(newValue)}</code>.
	 */
	public boolean setCacheOn(boolean newValue) {
		return m_authorizer.setCacheOn(newValue);
	}

	/**
	 * Provides the cached policy names. 
	 * @return <code>{@link Authorizer#getCachedPolicyResources() getCachedPolicyResources()}</code>.
	 */
	public Set<String> getCachedPolicyResources() {
		return m_authorizer.getCachedPolicyResources();
	}
	
	/**
	 * Provides the hit count for the specified policy. 
	 * @param resOpKey the policy key. 
	 * @return <code>{@link Authorizer#getPolicyStat() getPolicyStat()}</code>.
	 */
	public long getPolicyStat(String resOpKey) {
		return m_authorizer.getPolicyStat(resOpKey);
	}

	/**
	 * Calls {@link Authorizer#authorize(AuthorizeRequestType)} and takes care of error
	 * translation. This method throws an {@link AuthorizationException} for any problem
	 * encountered. 
	 */
	private AuthorizeResponseType authorizeOrThrow(AuthorizeRequestType authzRequest)
	throws AuthorizationException {
		checkNotNull(authzRequest, "authorization request");
        checkNotNull(authzRequest.getResourceType(), "resource type");
        checkNotNull(authzRequest.getResourceName(), "resource name");
        checkNotNull(authzRequest.getOperationName(), "operation name");

		if (authzRequest.getSubject() == null
				|| authzRequest.getSubject().size() <= 0) {
			throw new AuthorizationException(
					ErrorConstants.SVC_SECURITY_SYS_AUTHZ_MISSING_AUTHZ_REQUEST,
					"authorization request does not have a subject");
		}

        if (m_initExc != null) {
			throw new AuthorizationException(ErrorConstants.SVC_SECURITY_APP_AUTHZ_INTERNAL_ERROR, 
					"Previously failed to initialize - attaching the original exception as cause", 
					m_initExc);
		}
		AuthorizeResponseType authzResp = null; 
		try {
			// step 5: invoke authorize() on the authorizer
			authzResp = m_authorizer.authorize(authzRequest);
		} catch (AuthorizationException ae) {
			throw ae;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.log(LogLevel.ERROR, e);
			// catch all runtime exception, and convert it to application
			// specific error
			throw new AuthorizationException(
					ErrorConstants.SVC_SECURITY_APP_AUTHZ_INTERNAL_ERROR,
					"Internal error in executing authorizer logic: "
							+ e.getMessage(), e);
		}
		
		return authzResp;
	}
	
	private void checkNotNull(final Object value, final String label)
	throws AuthorizationException {
		if (value == null) {
		    throw new AuthorizationException(
		            ErrorConstants.SVC_SECURITY_SYS_AUTHZ_MISSING_AUTHZ_REQUEST,
		            label + " is null");
		}
}

	/** 
	 * error mapping from AuthorizationException to AuthorizationResponse 
	 */
	private AuthorizeResponseType mapError(AuthorizeRequestType authzReq, AuthorizationException ex) {
		AuthorizeResponseType respType = new AuthorizeResponseType();
		// TODO: map security framework errorId to service specific errors
		String errorId = ex.getErrorId();
		Object[] errArgArr = AuthorizationServiceImplUtils.createErrorArguments(ex.getMessage(), authzReq, null);
    	CommonErrorData errorData = 
    		ErrorDataFactory.createErrorData(errorId, ErrorConstants.ERRORDOMAIN.toString(), errArgArr);
		
		respType.setAck(AckValue.FAILURE);
		ErrorMessage errMsg = new ErrorMessage();
		respType.setErrorMessage(errMsg);
		errMsg.getError().add(errorData);
		
		ErrorParameter stackTraceErrParam = new ErrorParameter();
		errorData.getParameter().add(stackTraceErrParam);
		stackTraceErrParam.setName("stackTrace");
		stackTraceErrParam.setValue(createStackTrace(ex));
		
		return respType;
	}

	private String createStackTrace(AuthorizationException ex) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		
		return sw.toString();
	}

	/**
	 * Immutable configuration bean. 
	 * @author mpoplacenel
	 */
	protected static class InternalAuthorizerConfig {
		
		private final boolean cacheOn;
		
		private final long refreshInterval;
		
		private final int startTimeOfDay;

		/**
		 * Constructor. 
		 * @param cacheOn cache status flag. 
		 * @param refreshInterval the refresh interval in mSecs. 
		 * @param startTimeOfDay the start time of the day in seconds since midnight. 
		 */
		public InternalAuthorizerConfig(boolean cacheOn, long refreshInterval, int startTimeOfDay) {
			super();
			
			this.cacheOn = cacheOn;
			this.refreshInterval = refreshInterval;
			this.startTimeOfDay = startTimeOfDay;
		}

		/**
		 * Getter for the cacheOn property. 
		 * @return the value of the cacheOn property. 
		 */
		public boolean isCacheOn() {
			return cacheOn;
		}

		/**
		 * Getter for the refreshInterval property. 
		 * @return the value of the refreshInterval property. 
		 */
		public long getRefreshInterval() {
			return refreshInterval;
		}

		/**
		 * Getter for the startTimeOfDay property. 
		 * @return the value of the startTimeOfDay property. 
		 */
		public int getStartTimeOfDay() {
			return startTimeOfDay;
		}
		
	}
}