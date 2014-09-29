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

package org.cogchar.impl.web.in;

import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.SerTypedValueMap;
import org.cogchar.api.web.in.WebSessionActionParamWriter;
import org.cogchar.api.web.in.WebSessionInputSender;
import org.cogchar.impl.thing.route.BasicThingActionForwarder;
import org.cogchar.impl.thing.basic.BasicThingActionSpec;
import org.cogchar.impl.thing.basic.BasicTypedValueMap;
import org.cogchar.impl.thing.basic.BasicTypedValueMapWithConversion;

import org.cogchar.name.goody.GoodyNames;
import org.cogchar.name.web.WebActionNames;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class WebSessionInputForwarder extends BasicThingActionForwarder implements WebSessionInputSender  {

	protected	WebSessionActionParamWriter		myPendingActionParamWriter;
		
	public WebSessionInputForwarder(String updateTargetURL, String updateGraphQN) {
		super(updateTargetURL, updateGraphQN);
	}
	protected Ident getEntityTypeID() {
		return WebActionNames.WEB_USER_INPUT;
	}
	@Override public WebSessionActionParamWriter resetAndGetParamWriter() { 
		BasicTypedValueMap btvm = new BasicTypedValueMapWithConversion();
		myPendingActionParamWriter = new WebSessionActionParamWriter(btvm);
		return myPendingActionParamWriter;
	}
	
	@Override public void sendMessage(String actionRecordBase, String entityBase) { 
		//getLogger().info("Posting Lifter repo update message"); // TEST ONLY
		// The following IDs are currently being pulled from hand-waving; we'll want to formalize this stuff and at least move these ID strings elsewhere:
		Ident actRecID = mintInstanceID(actionRecordBase);
		Ident entityID = mintInstanceID(entityBase);
		Ident srcAgentID = mintInstanceID("liftMessageAgent");
		Ident verbID =  GoodyNames.ACTION_CREATE; // Probably shouldn't come from GoodyNames. Right now we are only CREATEing an output message
		SerTypedValueMap valueMap = myPendingActionParamWriter.getValueMap();
		Long postedTStampMsec = System.currentTimeMillis();
		BasicThingActionSpec actionSpec = 
				new BasicThingActionSpec(actRecID, entityID, getEntityTypeID(), verbID, srcAgentID, valueMap, postedTStampMsec);	
		sendActionSpec(actionSpec);
	}	

}
