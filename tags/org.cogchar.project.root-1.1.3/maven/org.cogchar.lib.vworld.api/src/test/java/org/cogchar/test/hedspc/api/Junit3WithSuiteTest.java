package org.cogchar.test.hedspc.api;

// Junit3 imports
import junit.framework.Test;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertTrue;
import junit.framework.TestSuite;

/**
 * Class name must begin or end with "Test" (or "TestCase") to match Surefire default includes patterns.
 * 
 
 */
public class Junit3WithSuiteTest  extends TestCase {  // this superclass is needed in JUnit3.
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public Junit3WithSuiteTest( String testName ) {
        super( testName );
		System.out.println("Junit3StyleTest is constructed with name " + testName + ", junit version = " + junit.runner.Version.id());
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()    {
		System.out.println("Construcing an explicit Junit3 suite, which is not necessary, under junit version: " + junit.runner.Version.id());
        return new TestSuite( Junit3WithSuiteTest.class );
    }


    public void testMeSomethingGood()
    {
		System.out.println("testMeSomethingGood invoked due to Junit3 naming convention, under junit version: " + junit.runner.Version.id());
        assertTrue( true );
    }
    public void testMeSomethingBad()
    {
		System.out.println("testMeSomethingBad invoked due to Junit3 naming convention, under junit version: " + junit.runner.Version.id());
	// Unless  surefire has testFailureIgnore, any failure like this will cause the build to fail.
       // assertTrue( false );
    }	
}
