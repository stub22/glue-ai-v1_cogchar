package org.cogchar.render.test;

import jme3test.TestChooser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        // assertTrue( true );
		/*
		TestChooser.main(new String[0]);
		try {
			Thread.sleep(10*60*1000);
		} catch(Throwable t) {
			t.printStackTrace();
		}
		 * 
		 */
    }
}
