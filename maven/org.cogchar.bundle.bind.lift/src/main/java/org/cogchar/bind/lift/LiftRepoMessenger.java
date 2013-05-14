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

package org.cogchar.bind.lift;

import org.cogchar.outer.client.web.WebUserActionParamWriter;
import java.util.Random;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.BasicThingActionSpec;
import org.cogchar.api.thing.BasicTypedValueMap;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.impl.thing.ConcreteTVM;
import org.cogchar.impl.thing.FancyThingModelWriter;
import org.cogchar.name.goody.GoodyNames;
import org.cogchar.name.lifter.LiftAN;
import org.cogchar.name.web.WebActionNames;
import org.cogchar.outer.client.AgentRepoClient;

/**
 *
 * A class to allow Lifter to post repo updates
 * Currently in prototype form
 * This class looks suspiciously like com.robosteps.api.core.ControllableSubSystem with other recycled Robosteps bits!
 * For starters, we're sending good ole ThingActions, but we may well make something more bespoke soon
 * 
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class LiftRepoMessenger extends BasicDebugger {
	
	protected	Random					myRandomizer = new Random();
	protected	AgentRepoClient			myAgentRepoClient = new AgentRepoClient();
	protected	String					myUpdateTargetURL, myUpdateGraphQN;
	protected	WebUserActionParamWriter	myPendingActionParamWriter;
	
	public LiftRepoMessenger(String updateTargetURL, String updateGraphQN) {
		myUpdateTargetURL = updateTargetURL;
		myUpdateGraphQN = updateGraphQN;
	}
	
	public Ident mintInstanceID(String instanceLabel) { 
		int rNum  = myRandomizer.nextInt(Integer.MAX_VALUE);
		return new FreeIdent(LiftAN.NS_LifterUserAction + instanceLabel + "_" + rNum);
	}
	
	private void sendActionSpec(ThingActionSpec actionSpec) {
		FancyThingModelWriter ftmw = new FancyThingModelWriter();
		String updateTxt = ftmw.writeTASpecToString(actionSpec, myUpdateGraphQN, myRandomizer);
		getLogger().debug("Sending update message:\n{}", updateTxt);
		boolean debugFlag = false;
		myAgentRepoClient.execRemoteSparqlUpdate(myUpdateTargetURL, updateTxt, debugFlag);
	}
	
	public WebUserActionParamWriter resetAndGetParamWriter() { 
		BasicTypedValueMap btvm = new ConcreteTVM();
		myPendingActionParamWriter = new WebUserActionParamWriter(btvm);
		return myPendingActionParamWriter;
	}
	
	protected void sendMessage(String actionRecordBase, String entityBase) { 
		//getLogger().info("Posting Lifter repo update message"); // TEST ONLY
		// The following IDs are currently being pulled from hand-waving; we'll want to formalize this stuff and at least move these ID strings elsewhere:
		Ident actRecID = mintInstanceID(actionRecordBase);
		Ident entityID = mintInstanceID(entityBase);
		Ident srcAgentID = mintInstanceID("liftMessageAgent");
		Ident verbID =  GoodyNames.ACTION_CREATE; // Probably shouldn't come from GoodyNames. Right now we are only CREATEing an output message
		TypedValueMap valueMap = myPendingActionParamWriter.getValueMap();
		BasicThingActionSpec actionSpec = 
				new BasicThingActionSpec(actRecID, entityID, getEntityTypeID(), verbID, srcAgentID, valueMap);	
		sendActionSpec(actionSpec);
	}
	
	protected Ident getEntityTypeID() {
		return WebActionNames.WEB_USER_INPUT;
	}
	
}
