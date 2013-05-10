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

import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.BasicEntityAction;
import org.cogchar.api.thing.ThingActionConsumer;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.name.web.WebActionNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A class to hold actions for execution in the Lifter web interface
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class WebEntityAction extends BasicEntityAction {
	private static Logger theLogger = LoggerFactory.getLogger(WebEntityAction.class);
	
	public WebEntityAction(ThingActionSpec actionSpec) {
		super(actionSpec);
	}
	
	public Ident getConfigIdent() {
		return paramTVMap.getAsIdent(WebActionNames.CONFIG);
	}
	
	// For repo encoded control activation action URIs
	public Ident getControlActionUri() {
		return paramTVMap.getAsIdent(WebActionNames.WEBCONTROL_ACTION);
	}
	
	public Integer getSlotID() {
		return paramTVMap.getAsInteger(WebActionNames.SLOT);
	}
	
	public Ident getControlType() {
		return paramTVMap.getAsIdent(WebActionNames.TYPE);
	}
	
	public String getControlText() {
		return paramTVMap.getAsString(WebActionNames.TEXT);
	}
	
	public String getControlStyle() {
		return paramTVMap.getAsString(WebActionNames.STYLE);
	}
	
	public String getControlResource() {
		return paramTVMap.getAsString(WebActionNames.RESOURCE);
	}
	
	public Ident getControlAction() {
		return paramTVMap.getAsIdent(WebActionNames.ACTION);
	}
	
	public String getUserName() {
		return paramTVMap.getAsString(WebActionNames.USERNAME);
	}
	
	public String getUserClass() {
		return paramTVMap.getAsString(WebActionNames.USERCLASS);
	}
	
	public void perform(WebAppInterface la) {
		//	WebAppInterface la = WebAppInterfaceTracker.getTracker().getWebInterface();
		if (la == null) {
			theLogger.error("Attempting to perform a web action, but the WebAppInterface is not available!");
		} else {
			ThingActionSpec actionSpec = getActionSpec();
			
			String webUser = getUserName();
			String webUserClass = getUserClass();
			if (actionSpec.getTargetThingTypeID().equals(WebActionNames.WEBCONTROL)) { // Big ugly if-else-if chain must go -- really need switch on Ident! (or Scala...)
				// Assuming for now it's CREATE only
				Ident controlAction = getControlActionUri();
				if (controlAction != null) {
					la.activateControlAction(controlAction);
				} else {
					WebAppInterface.Control newCC = generateControlConfig(la);
					Integer slotNum = getSlotID();
					if (slotNum != null) {
						if (webUser != null) {
							// Activate for user
							la.activateControlFromConfigForUser(webUser, slotNum, newCC);
						} else if (webUserClass != null) {
							la.activateControlFromConfigForUserClass(webUserClass, slotNum, newCC);
						} else {
							la.activateControlFromConfig(slotNum, newCC);
						}
					} else {
						theLogger.warn("Could not display control by action spec -- desired control slot is null");
					}
				}
			} else if (actionSpec.getTargetThingTypeID().equals(WebActionNames.WEBCONFIG)) {
				// Assuming for now it's CREATE only
				Ident configIdent = getConfigIdent();
				if (configIdent != null) {
					if (webUser != null) {
						la.activateControlsFromUriForUser(webUser, configIdent);
					} else if (webUserClass != null) {
						la.activateControlsFromUriForUserClass(webUserClass, configIdent);
					} else {
						la.activateControlsFromUri(configIdent);
					}
				} else {
					theLogger.warn("Could not set web config by action spec -- desired config URI is null");
				}
			}
		}
	}
		// A method to generate a new ControlConfig for display from a WebAction
	protected WebAppInterface.Control generateControlConfig(WebAppInterface webInterface) {
		WebAppInterface.Control cc = webInterface.getNewControl();
		if (getControlType() == null) {
			cc.setType("NULLTYPE");
		} else {
			cc.setType(getControlType().getLocalName().toUpperCase()); // Ensures lc:type property is case insensitive to local name
		}
		cc.setAction(getControlAction());
		cc.setText(getControlText());
		cc.setStyle(getControlStyle());
		cc.setResource(getControlResource());
		return cc;
	}
	
	public static boolean makeAndPerformForTAS(ThingActionSpec actionSpec) {
		theLogger.info("The targetThingType is {}", actionSpec.getTargetThingTypeID()); // TEST ONLY
		if ((actionSpec.getTargetThingTypeID().equals(WebActionNames.WEBCONTROL)) || 
				(actionSpec.getTargetThingTypeID().equals(WebActionNames.WEBCONFIG))) {

			WebAppInterface la = WebAppInterfaceTracker.getTracker().getWebInterface();
			if (la != null) {
				WebEntityAction wa = new WebEntityAction(actionSpec);
				wa.perform(la);
				return true;
			} else {
				theLogger.error("Attempting to perform a web action, but the WebAppInterface is not available!");
			}
		} else {
			theLogger.warn("WebEntitySpace igoring irrelevant actionSpec {}", actionSpec);
		}
		return false;
	}
	
	public static class Consumer extends ThingActionConsumer {

		@Override public Status consumeAction(ThingActionSpec actionSpec, Ident srcGraphID) {
			return makeAndPerformForTAS(actionSpec) ? Status.CONSUMED : Status.IGNORED;
		}
		
	}
}
