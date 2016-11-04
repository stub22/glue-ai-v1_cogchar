/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.api.web;

/**
 * A "one man registry" for the WebAppInterface. Probably just a temporary construct.
 * WebAppInterface is implemented in LiftAmbassador. LiftAmbassador.inputInterface implements LiftAmbassadorInterface,
 * which is managed by LifterLifecycle and thus is in the OSGi registry. Quite likely the blank LiftAmbassadorInterface
 * should be replaced with the WebAppInterface or something similar. Then perhaps the lifecycle system can be used to 
 * resolve this dependency in classes that require it instead of manually looking it up here.
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class WebAppInterfaceTracker {
	
	private static WebAppInterfaceTracker theTracker;
	
	private WebAppInterface myInterface;
	
	public static WebAppInterfaceTracker getTracker() {
		if (theTracker == null) {
			theTracker = new WebAppInterfaceTracker();
		}
		return theTracker;
	}
	
	public void setWebInterface(WebAppInterface wai) {
		myInterface = wai;
	}
	
	public WebAppInterface getWebInterface() {
		return myInterface;
	}
}
