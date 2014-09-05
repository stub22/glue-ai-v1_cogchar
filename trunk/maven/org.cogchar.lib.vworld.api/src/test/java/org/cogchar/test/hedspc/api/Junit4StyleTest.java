/*
 *  Copyright 2014 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.test.hedspc.api;

import org.junit.Test;
import org.junit.Ignore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import static org.junit.Assert.*;

import junit.runner.Version;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * "Test class should have exactly one public zero-argument constructor."
 */
public class Junit4StyleTest {

	public Junit4StyleTest() { 
		super();
		System.out.println("Junit4StyleTest constructed under " + Version.id() 
			+ " but annotated methods will only be found if surefire + JUnit agree on prickly version constraints");
	}
	
	@Test
	public void methodNameDoesntMatter() {
		System.out.println("This method has a @Test annotation, and we are running under " + Version.id());
	}
	@Test
	public void letsBeWrng() {
		System.out.println("This method has a @Test annotation, and we are running under " + Version.id());
		assertTrue(false);
	}	
	
	@BeforeClass
	public static void setUpClass() throws Exception {
			System.out.println("Yes, let's setUpClass under " + Version.id() + ", this will happen before constructor");
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		System.out.println("Yes, let's tearDownClass");
	}
	
	@Before
	public void setUp() {
		System.out.println("setUp is happenin (before a test-method)");
	}
	
	@After
	public void tearDown() {
		System.out.println("dun got tore down (after a test-method)");
	}

}