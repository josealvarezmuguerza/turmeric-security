/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.test.services.extended.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.transport.http.HTTPConstants;
//import org.apache.commons.lang.StringUtils;
import org.ebayopensource.turmeric.runtime.binding.BindingConstants;
import org.ebayopensource.turmeric.runtime.common.binding.DataBindingDesc;
import org.ebayopensource.turmeric.runtime.common.impl.attachment.BaseMessageAttachments;
import org.ebayopensource.turmeric.runtime.common.impl.attachment.InboundMessageAttachments;
import org.ebayopensource.turmeric.runtime.common.impl.attachment.OutboundMessageAttachments;
import org.ebayopensource.turmeric.runtime.common.impl.internal.pipeline.BaseMessageImpl;
import org.ebayopensource.turmeric.runtime.common.impl.internal.pipeline.InboundMessageImpl;
import org.ebayopensource.turmeric.runtime.common.impl.internal.pipeline.OutboundMessageImpl;
import org.ebayopensource.turmeric.runtime.common.impl.internal.service.ProtocolProcessorDesc;
import org.ebayopensource.turmeric.runtime.common.impl.internal.service.ServiceDesc;
import org.ebayopensource.turmeric.runtime.common.pipeline.MessageProcessingStage;
import org.ebayopensource.turmeric.runtime.common.pipeline.OutboundMessage;
import org.ebayopensource.turmeric.runtime.common.pipeline.Transport;
import org.ebayopensource.turmeric.runtime.common.service.ServiceOperationDesc;
import org.ebayopensource.turmeric.runtime.common.types.G11nOptions;
import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
import org.ebayopensource.turmeric.runtime.common.types.SOAHeaders;
import org.ebayopensource.turmeric.runtime.common.types.ServiceAddress;
import org.ebayopensource.turmeric.runtime.sif.impl.internal.pipeline.ClientMessageContextImpl;
import org.ebayopensource.turmeric.runtime.sif.impl.internal.service.ClientServiceDesc;
import org.ebayopensource.turmeric.runtime.sif.impl.internal.service.ClientServiceDescFactory;
import org.ebayopensource.turmeric.runtime.sif.pipeline.ClientMessageContext;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;
import org.ebayopensource.turmeric.runtime.sif.service.ServiceInvokerOptions;
import org.ebayopensource.turmeric.runtime.spf.impl.internal.pipeline.ServerMessageContextImpl;
import org.ebayopensource.turmeric.runtime.spf.impl.internal.pipeline.ServerMessageProcessor;
import org.ebayopensource.turmeric.runtime.spf.impl.internal.service.ServerServiceDesc;
import org.ebayopensource.turmeric.runtime.spf.impl.internal.service.ServerServiceDescFactory;
import org.ebayopensource.turmeric.runtime.spf.pipeline.ServerMessageContext;
import org.ebayopensource.turmeric.test.services.extended.util.TestTransport;

/**
 * @author wdeng
 */
public class TestUtils {
	public static final String PAYLOAD_UNORDERED_NV = "Unordered NV";

	private static final HashMap<String, String> s_mimeTypes = new HashMap<String, String>();
	static {
		s_mimeTypes.put(BindingConstants.PAYLOAD_JSON, SOAConstants.MIME_JSON);
		s_mimeTypes.put(BindingConstants.PAYLOAD_NV, SOAConstants.MIME_NV);
		s_mimeTypes.put(PAYLOAD_UNORDERED_NV, SOAConstants.MIME_NV);
		s_mimeTypes.put(BindingConstants.PAYLOAD_XML, SOAConstants.MIME_XML);
		s_mimeTypes.put(BindingConstants.PAYLOAD_FAST_INFOSET, SOAConstants.MIME_FAST_INFOSET);

	}


	// Attachment
	public final static String ATTACHMENT_CONTENT_TYPE_STRING =
												"multipart/related;boundary=MIMEBoundaryurn_uuid_9E55D9AADCAC7C46E811592318362121; " +
												"type=\"application/xop+xml\";start=\"<0.urn:uuid:9E55D9AADCAC7C46E811592318362122.org>" +
												"\";start-info=\"text/xml\"; charset=UTF-8";


	public static final String ELEMENT_ORDERING_PRESERVE_VALUE = "true";
//TODO: when package annotation is implemented use these right ones
	public static final String XML_INPUT_BODY = "<?xml version='1.0' encoding='UTF-8'?><ns2:MyMessage xmlns:ns2=\"http://www.ebay.com/test/soaframework/sample/service/message\"><ns2:body>SOA SOA, SOS.</ns2:body><ns2:recipients><entry><key>soa@ebay.com</key><value><ns2:city>San Jose</ns2:city><ns2:emailAddress>soa@ebay.com</ns2:emailAddress><ns2:postCode>95125</ns2:postCode><ns2:state>CA</ns2:state><ns2:streetNumber>2145</ns2:streetNumber></value></entry></ns2:recipients><ns2:subject>Test SOA JAXB XML ser/deser</ns2:subject></ns2:MyMessage>";
	public static final String JSON_INPUT_BODY = "{\"jsonns.ns\":\"http://www.ebay.com/test/soaframework/sample/service/message\",\"ns.MyMessage\":{\"ns.body\":\"SOA SOA, SOS.\",\"ns.recipients\":{\"entry\":{\"key\":\"soa@ebay.com\",\"value\":{\"ns.city\":\"San Jose\",\"ns.emailAddress\":\"soa@ebay.com\",\"ns.postCode\":\"95125\",\"ns.state\":\"CA\",\"ns.streetNumber\":\"2145\"}}},\"ns.subject\":\"Test SOA JAXB XML ser/deser\"}}";
	public static final String NV_INPUT_BODY = "nvns:ns2=\"http://www.ebay.com/test/soaframework/sample/service/message\"&ns2:MyMessage.ns2:body=\"SOA+SOA%2C+SOS.\"&ns2:MyMessage.ns2:recipients.entry.key=\"soa%40ebay.com\"&ns2:MyMessage.ns2:recipients.entry.value.ns2:city=\"San+Jose\"&ns2:MyMessage.ns2:recipients.entry.value.ns2:emailAddress=\"soa%40ebay.com\"&ns2:MyMessage.ns2:recipients.entry.value.ns2:postCode=\"95125\"&ns2:MyMessage.ns2:recipients.entry.value.ns2:state=\"CA\"&ns2:MyMessage.ns2:recipients.entry.value.ns2:streetNumber=\"2145\"&ns2:MyMessage.ns2:subject=\"Test+SOA+JAXB+XML+ser%2Fdeser\"";

/*	public static final String XML_INPUT_BODY = "<?xml version='1.0' encoding='UTF-8'?><ns2:MyMessage xmlns:ns2=\"http://www.ebay.com/test/soaframework/sample/service/message\"><ns2:body>SOA SOA, SOS.</ns2:body><ns2:recipients><entry><key>soa@ebay.com</key><value><city>San Jose</city><emailAddress>soa@ebay.com</emailAddress><postCode>95125</postCode><state>CA</state><streetNumber>2145</streetNumber></value></entry></ns2:recipients><ns2:subject>Test SOA JAXB XML ser/deser</ns2:subject></ns2:MyMessage>";
	public static final String JSON_INPUT_BODY = "{\"ns.MyMessage\":{\"body\":\"SOA SOA, SOS.\",\"ns.recipients\":{\"entry\":{\"key\":\"soa@ebay.com\",\"value\":{\"city\":\"San Jose\",\"emailAddress\":\"soa@ebay.com\",\"postCode\":\"95125\",\"state\":\"CA\",\"streetNumber\":\"2145\"}}},\"subject\":\"Test SOA JAXB XML ser/deser\"}}";
	public static final String NV_INPUT_BODY = "nvns:ns2=\"http://www.ebay.com/test/soaframework/sample/service/message\"&ns2:MyMessage.ns2:body=\"SOA+SOA%2C+SOS.\"&ns2:MyMessage.ns2:recipients.entry.key=\"soa%40ebay.com\"&ns2:MyMessage.ns2:recipients.entry.value.city=\"San+Jose\"&ns2:MyMessage.ns2:recipients.entry.value.emailAddress=\"soa%40ebay.com\"&ns2:MyMessage.ns2:recipients.entry.value.postCode=\"95125\"&ns2:MyMessage.ns2:recipients.entry.value.state=\"CA\"&ns2:MyMessage.ns2:recipients.entry.value.streetNumber=\"2145\"&ns2:MyMessage.ns2:subject=\"Test+SOA+JAXB+XML+ser%2Fdeser\"";
*/
	public static final String SOA_MESSAGE_PROTOCOL_VALUE = "TEST_CTX_CREATE";


	public static ServerMessageContext createServerMessageContext(String bindingName, String serviceName, String opName, String messageProtocol,
		String payload, URL serviceAddressUrl, String contentType) throws Exception
	{
		return createServerMessageContext(bindingName, serviceName, opName, messageProtocol,
			(payload != null ? payload.getBytes() : null), serviceAddressUrl, contentType);
	}

	public static ServerMessageContext createServerMessageContext(String bindingName, String serviceName, String opName, String messageProtocol,
		byte[] payload, URL serviceAddressUrl, String contentType) throws Exception
	{
		ServerMessageProcessor.getInstance();
		ServerServiceDesc serverDesc = ServerServiceDescFactory.getInstance().getServiceDesc(serviceName);

		ProtocolProcessorDesc protocolProcessor;
		if (messageProtocol != null) {
			protocolProcessor = serverDesc.getProtocolProcessor(messageProtocol);
		} else {
			protocolProcessor = serverDesc.getNullProtocolProcessor();
		}

		ServiceOperationDesc operation = serverDesc.getOperation(opName);

		DataBindingDesc dbDesc = createTestDataBinding(bindingName, serverDesc);

		G11nOptions g11nOptions = new G11nOptions();

		ServiceAddress clientAddress = new ServiceAddress(null);
		ServiceAddress serviceAddress = new ServiceAddress(serviceAddressUrl);
		Map<String,String> headers = new HashMap<String, String>();

		headers.put(SOAHeaders.ELEMENT_ORDERING_PRESERVE, ELEMENT_ORDERING_PRESERVE_VALUE);
		headers.put("Connection", "Keep-Alive");
		headers.put("Host", "localhost");
		if (contentType == null) {
			if (messageProtocol != null && messageProtocol.equals(SOAConstants.MSG_PROTOCOL_SOAP_12)) {
				headers.put("Content-Type".toUpperCase(), "application/soap+xml");
			} else {
				headers.put("Content-Type".toUpperCase(), "text/xml");
			}
		} else{
			headers.put("Content-Type", contentType);
		}
		headers.put("SOAPAction", opName);

		String payloadType = dbDesc.getPayloadType();
		headers.put(SOAHeaders.REQUEST_DATA_FORMAT, payloadType);
		headers.put(SOAHeaders.RESPONSE_DATA_FORMAT, payloadType);

		if (null == payload && contentType != ATTACHMENT_CONTENT_TYPE_STRING) {
			payload = XML_INPUT_BODY.getBytes();
			if (payloadType.equals("NV")) {
				payload = NV_INPUT_BODY.getBytes();
			} else if (payloadType.equals("JSON")) {
				payload = JSON_INPUT_BODY.getBytes();
			}
		}

		InputStream inputStream = null;
		if (null != payload) {
			inputStream = new ByteArrayInputStream(payload);
		}

		boolean isAttachment = (contentType != null && contentType.contains(HTTPConstants.MEDIA_TYPE_MULTIPART_RELATED));
		// Attachment handling
		BaseMessageAttachments attachments = null;
		if (isAttachment) {
			try {
				attachments = new InboundMessageAttachments(inputStream, contentType);
			} catch(Exception e) {
				e.printStackTrace();
				throw e;
			}
		}

		Transport transport = new TestTransport();

		InboundMessageImpl requestMsg = new InboundMessageImpl(true,
			SOA_MESSAGE_PROTOCOL_VALUE, dbDesc, g11nOptions, headers, null, null, attachments, operation);
		if (isAttachment && null != inputStream) {
			inputStream= attachments.getInputStreamForMasterMessage();
		}
		if (null != inputStream)
			requestMsg.setInputStream(inputStream);


		BaseMessageAttachments outAttachments = null;
		if (isAttachment) {
			outAttachments = new OutboundMessageAttachments(messageProtocol);
		}
		OutboundMessageImpl responseMsg = new OutboundMessageImpl(false,
			SOA_MESSAGE_PROTOCOL_VALUE, dbDesc, g11nOptions, null, null, null, outAttachments, operation, false, 0);

		Charset effectiveCharset = serverDesc.getServiceCharset();
		if (effectiveCharset == null) {
			effectiveCharset = g11nOptions.getCharset();
		}

		ServerMessageContextImpl ctx = new ServerMessageContextImpl(
			serverDesc, operation, protocolProcessor, transport,
			requestMsg, responseMsg, serviceAddress, null, clientAddress, null, null,
			effectiveCharset, serviceAddressUrl == null ? "none" : serviceAddressUrl.toString(),
			"(none)", 0, null);

		return ctx;
	}


	public static ServerMessageContext createServerMessageContext(
			String bindingName, String serviceName, String operationName, String messageProtocol,
			URL serviceAddressUrl) throws Exception {
		ServerMessageContext ctx = createServerMessageContext(bindingName,
				serviceName, operationName, messageProtocol, "Dummy", serviceAddressUrl, null);

		OutboundMessage outMsg = (OutboundMessage)ctx.getResponseMessage();
		((ServerMessageContextImpl)ctx).changeProcessingStage(MessageProcessingStage.RESPONSE_DISPATCH);
		String payload = null;

//		if (operationName != null &&
//				(	operationName.equalsIgnoreCase("customError2") ||
//					operationName.equalsIgnoreCase("myTestOperation") ||
//					operationName.equalsIgnoreCase("serviceChainingOperation"))) {
//			outMsg.setParam(0, createTestMessage());
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			outMsg.serialize(out);
//			payload = out.toString();
//		}
		System.out.println("Payload: " + payload);
		return createServerMessageContext(bindingName, serviceName, operationName, messageProtocol,
			payload, serviceAddressUrl, null);
	}
	
	public static ServerMessageContext createServerMessageContextWithPayload(
			String bindingName, String serviceName, String operationName, String messageProtocol,
			URL serviceAddressUrl, String payload) throws Exception {
		ServerMessageContext ctx = createServerMessageContext(bindingName,
				serviceName, operationName, messageProtocol, "Dummy", serviceAddressUrl, null);

		((ServerMessageContextImpl)ctx).changeProcessingStage(MessageProcessingStage.RESPONSE_DISPATCH);

		System.out.println("Payload: " + payload);
		return createServerMessageContext(bindingName, serviceName, operationName, messageProtocol,
			payload, serviceAddressUrl, null);
	}

	public static ClientMessageContext createClientMessageContext(String bindingName, String serviceName, String messageProtocol,
			Object outParam, String inPayload, URL serviceAddressUrl, Map<String,String> extraHeaders,
			boolean isREST, int maxUrlLen, String clientName) throws Exception {
		return createClientMessageContext(bindingName,serviceName,messageProtocol,
				outParam,inPayload,serviceAddressUrl,extraHeaders,
				isREST,maxUrlLen,clientName,new TestTransport(),"myTestOperation");
	}
	
	public static ClientMessageContext createClientMessageContext(String bindingName, String serviceName, String messageProtocol,
			Object outParam, String inPayload, URL serviceAddressUrl, Map<String,String> extraHeaders,
			boolean isREST, int maxUrlLen, String clientName, String consumerId) throws Exception {
		return createClientMessageContext(bindingName,serviceName,messageProtocol,
				outParam,inPayload,serviceAddressUrl,extraHeaders,
				isREST,maxUrlLen,clientName,new TestTransport(),"myTestOperation", consumerId);
	}

	public static ClientMessageContext createClientMessageContext(String bindingName, String serviceName, String messageProtocol,
			Object outParam, String inPayload, URL serviceAddressUrl, Map<String,String> extraHeaders,
			boolean isREST, int maxUrlLen, String clientName, Transport transport, String opName) throws Exception {
		return createClientMessageContext(bindingName, serviceName, messageProtocol,
			 outParam, inPayload, serviceAddressUrl, extraHeaders,
		     isREST, maxUrlLen, clientName, transport, opName, null);
	}

	public static ClientMessageContext createClientMessageContext(String bindingName, String serviceName, String messageProtocol,
																	Object outParam, String inPayload, URL serviceAddressUrl, Map<String,String> extraHeaders,
																	boolean isREST, int maxUrlLen, String clientName, Transport transport, String opName, String consumer_id) throws Exception {
		ClientServiceDesc serviceDesc = ClientServiceDescFactory.getInstance().getServiceDesc(serviceName, clientName);

		ProtocolProcessorDesc protocolProcessor;
		if (messageProtocol != null) {
			protocolProcessor = serviceDesc.getProtocolProcessor(messageProtocol);
		} else {
			protocolProcessor = serviceDesc.getNullProtocolProcessor();
		}

		ServiceOperationDesc operation = serviceDesc.getOperation(opName);
		DataBindingDesc dbDesc = createTestDataBinding(bindingName, serviceDesc);
		G11nOptions g11nOptions = new G11nOptions();

		ServiceAddress serviceAddress = new ServiceAddress(serviceAddressUrl);
		Map<String,String> headers = new HashMap<String, String>();
		String payloadType = dbDesc.getPayloadType();
		headers.put(SOAHeaders.REQUEST_DATA_FORMAT, payloadType);
		headers.put(SOAHeaders.RESPONSE_DATA_FORMAT, payloadType);
		if (extraHeaders != null) {
			headers.putAll(extraHeaders);
		}

		BaseMessageImpl requestMsg = new OutboundMessageImpl(true,
				SOA_MESSAGE_PROTOCOL_VALUE, dbDesc, g11nOptions, headers, null, null, null, operation, isREST, maxUrlLen);

		InboundMessageImpl responseMsg = new InboundMessageImpl(false,
			SOA_MESSAGE_PROTOCOL_VALUE, dbDesc, g11nOptions, null, null, null, null, operation);

		ServiceInvokerOptions invokerOptions = new ServiceInvokerOptions();

		String serviceVersion = null;
		String responseTransport = null;
		String useCase = null;
		String consumerId = consumer_id;

		ClientMessageContextImpl ctx = new ClientMessageContextImpl(
				serviceDesc, operation, protocolProcessor, transport,
				requestMsg, responseMsg, serviceAddress, null, serviceVersion, invokerOptions,
				responseTransport, useCase, consumerId, g11nOptions.getCharset(),
				(serviceAddressUrl == null ?  null : serviceAddressUrl.toString()));

		if (outParam != null) {
			// this has to happen after client message context is created and assoicated w/ requestMsg
			requestMsg.setParam(0, outParam);
		}
		if (inPayload != null) {
			InputStream inputStream = new ByteArrayInputStream(inPayload.getBytes());
			responseMsg.setInputStream(inputStream);
		}

		return ctx;
	}
	
	private static DataBindingDesc createTestDataBinding(String bindingName, ServiceDesc serviceDesc) {
 	 return serviceDesc.getDataBindingDesc(bindingName);
	}

	public static boolean equals(Object obj1, Object obj2) {
		if (null == obj1) {
			return null == obj2;
		}
		return obj1.equals(obj2);
	}

}
