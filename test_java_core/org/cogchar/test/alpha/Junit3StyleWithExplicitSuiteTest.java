/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cogchar.test.alpha;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class Junit3StyleWithExplicitSuiteTest extends TestCase {
	
	public Junit3StyleWithExplicitSuiteTest(String testName) {
		super(testName);
		System.out.println("Junit3StyleWithExplicitSuiteTest - constructed under Junit version=" + junit.runner.Version.id());
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(Junit3StyleWithExplicitSuiteTest.class);
		System.out.println("Junit3StyleWithExplicitSuiteTest - made suite: " + suite);
		return suite;
	}
	
	@Override
	protected void setUp() throws Exception {
		System.out.println("Junit3StyleWithExplicitSuiteTest - setUp(clownCar)");
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		System.out.println("Junit3StyleWithExplicitSuiteTest - tearDown(clownCheek)");
		super.tearDown();
	}
	public void testOfStrength() {
		System.out.println("Junit3StyleWithExplicitSuiteTest - this method name starts with test");
	}
	
}
