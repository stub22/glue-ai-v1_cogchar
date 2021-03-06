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

package org.cogchar.impl.perform.basic;

import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.BasicEntityAction;
import org.cogchar.impl.thing.route.BasicThingActionConsumer;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.api.web.WebEntityAction;
import org.cogchar.name.web.WebActionNames;
import org.cogchar.name.web.WebUserActionNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * This class was used as a test that we can receive animURIs through ThingActions,
 * but it is being replaced with proper scene+guard+filter+graphChannel wiring, as
 * discussed in the doc:  BudE_Missing_Technology_Pieces
 * 

 
 * Email from RyanB in May 2013:
 * 
" the animations in the Lifter animation list page should now generate ThingAction repo updates. 
These updates have entity type:

WebActionNames.WEB_USER_INPUT = http://www.cogchar.org/lift/config#userinput
[The http://www.cogchar.org/lift/config# prefix is overused and should only correspond to LiftConfigs; [We'll] probably address that soon]

and the animation URI appears at the parameter called:
WebUserActionNames.ACTION = http://www.cogchar.org/lift/user/action#action
"
 */

public class AnimLaunchEntityAction extends BasicEntityAction {
	private static Logger theLogger = LoggerFactory.getLogger(WebEntityAction.class);
	
	public AnimLaunchEntityAction(ThingActionSpec actionSpec) {
		super(actionSpec);
	}
	public static class Consumer extends BasicThingActionConsumer {

		@Override public BasicThingActionConsumer.ConsumpStatus consumeAction(ThingActionSpec actionSpec, Ident srcGraphID) {
			Ident			tgtThingEntityTypeID = actionSpec.getTargetThingTypeID();
		
			// We want this comparison to be done by the behavior mapper of the scene, rather than here in launch action.
			if (WebActionNames.WEB_USER_INPUT.equals(tgtThingEntityTypeID)) {
				getLogger().info("Found WebUserInput action, is it an animation launch?");
				TypedValueMap	paramTVM = actionSpec.getParamTVM();
				Ident			animID = paramTVM.getAsIdent(WebUserActionNames.ACTION);	
				getLogger().info("Got animID: {} ", animID);
				if (animID != null) {
					
				}
				return ConsumpStatus.USED;
			}
			return ConsumpStatus.IGNORED;
		}
		
	}

}
