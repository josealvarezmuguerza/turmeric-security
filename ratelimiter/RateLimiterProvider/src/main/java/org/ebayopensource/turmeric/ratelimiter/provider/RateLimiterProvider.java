/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 *******************************************************************************/
package org.ebayopensource.turmeric.ratelimiter.provider;

import org.ebayopensource.turmeric.services.ratelimiterservice.intf.RateLimiterService;

/**
 * A generic provider for adopters to implement their own rules for the Rate Limiter
 * service.  This currently extends the interface for RateLimiterService itself, but
 * may be extended later to include more specific functionality.
 * 
 * @author dcarver
 * @since 1.0.0
 */
public interface RateLimiterProvider extends RateLimiterService {

}
