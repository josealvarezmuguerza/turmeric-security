/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.calculatortestservice.impl;

import org.ebayopensource.turmeric.calculatortestservice.intf.CalculatorTestService;
import org.ebayopensource.turmeric.calcultor.v1.services.DoMultiplicationRequest;
import org.ebayopensource.turmeric.calcultor.v1.services.DoMultiplicationResponse;
import org.ebayopensource.turmeric.calcultor.v1.services.GetAdditionRequest;
import org.ebayopensource.turmeric.calcultor.v1.services.GetAdditionResponse;
import org.ebayopensource.turmeric.calcultor.v1.services.GetDivisionRequest;
import org.ebayopensource.turmeric.calcultor.v1.services.GetDivisionResponse;
import org.ebayopensource.turmeric.calcultor.v1.services.GetSquareRootRequest;
import org.ebayopensource.turmeric.calcultor.v1.services.GetSquareRootResponse;
import org.ebayopensource.turmeric.calcultor.v1.services.GetSubtractionRequest;
import org.ebayopensource.turmeric.calcultor.v1.services.GetSubtractionResponse;
import org.ebayopensource.turmeric.calcultor.v1.services.GetVersionRequest;
import org.ebayopensource.turmeric.calcultor.v1.services.GetVersionResponse;


public class CalculatorTestServiceImpl
    implements CalculatorTestService
{


    public GetVersionResponse getVersion(GetVersionRequest request) {
    	GetVersionResponse resp = new GetVersionResponse();
    	resp.setVersion("v1.0");
    	return resp;
    }

	public GetAdditionResponse getAddition(GetAdditionRequest request) {
		GetAdditionResponse resp = new GetAdditionResponse();
		try {
			resp.setResult(request.getParamOne() + request.getParamTwo());
		} catch (Exception e) {
			System.out.println("The Exception in getAddition method of CalculatorTest Service is ==> " + e.getMessage());
			resp.setResult(0);
		}
		return resp;
	}

	public DoMultiplicationResponse doMultiplication(DoMultiplicationRequest request) {
		DoMultiplicationResponse resp = new DoMultiplicationResponse();
		try {
			resp.setResult(request.getParamOne() * request.getParamTwo());
		} catch (Exception e) {
			System.out.println("The Exception in doMultiplication method of CalculatorTest Service is ==> " + e.getMessage());
			resp.setResult(0);
		}
		return resp;
	}

	public GetDivisionResponse getDivision(GetDivisionRequest request) {
		GetDivisionResponse resp = new GetDivisionResponse();
		try {
			resp.setResult(request.getParamOne() / request.getParamTwo());
		} catch (Exception e) {
			System.out.println("The Exception in getDivision method of CalculatorTest Service is ==> " + e.getMessage());
			resp.setResult(0);
		}	
		return resp;
	}

	public GetSubtractionResponse getSubtraction(GetSubtractionRequest request) {
		GetSubtractionResponse resp = new GetSubtractionResponse();
		try {
			resp.setResult(request.getParamOne() - request.getParamTwo());
		} catch (Exception e) {
			System.out.println("The Exception in getSubtraction method of CalculatorTest Service is ==> " + e.getMessage());
			resp.setResult(0);
		}	
		return resp;
	}

	@Override
	public GetSquareRootResponse getSquareRoot(
			GetSquareRootRequest getSquareRootRequest) {
		GetSquareRootResponse resp = new GetSquareRootResponse();
		try {
			resp.setResult(Math.sqrt(getSquareRootRequest.getNumber()));
		} catch (Exception e) {
			System.out.println("The Exception in squaretoot of CalculatorTest Service is ==> " + e.getMessage());
			resp.setResult(0);
		}	
		return resp;
	}

}
