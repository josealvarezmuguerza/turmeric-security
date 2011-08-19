package org.ebayopensource.turmeric.rateLimiterCounterCassandraProviderImpl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple RateLimiterCounterCassandraProviderImpl
 * @author jamuguerza.
 */
public class RateLimiterCounterCassandraProviderImplTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public RateLimiterCounterCassandraProviderImplTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( RateLimiterCounterCassandraProviderImplTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
