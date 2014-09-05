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

import junit.framework.TestCase;

/**
 *
 * @author Stu B. <www.texpedient.com>
 * Classname must end or begin in "Test" to be found by default Surefire pattern.
 * http://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html#includes
 * 
 * Junit3Style = extends TestCase, does not use @Test annotations on methods.
 */
public class Junit3StyleTest extends TestCase {
	
	public Junit3StyleTest(String testName) {
		super(testName);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	// TODO add test methods here. The name must begin with 'test'. For example:
	// public void testHello() {}
	public void testOfWill() {
		System.out.println("Junit3StyleTest - this method name starts with test");
	}	
}
