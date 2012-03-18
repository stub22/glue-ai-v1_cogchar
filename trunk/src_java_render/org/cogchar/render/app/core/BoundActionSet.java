/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.app.core;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BoundActionSet {
	/*
   static void setupActionListeners(InputManager inputManager, final HumanoidRenderContext ctx, 
		   List<BoundAction> potentialActions) {
        BoundAction pavals[] = 
		List<String> actionNamesList = new ArrayList<String>();
		boolean minSimMode = ctx.getBonyConfigEmitter().isMinimalSim();
        for (int pai =0; pai < pavals.length; pai++) { 
            PlayerAction pa = pavals[pai];
			String actionName = pa.name();
			if (minSimMode) {
				if (!pa.includedInMinSim()) {
					continue;
				}
			}
			actionNamesList.add(actionName);
            inputManager.addMapping(actionName, pa.makeJME3InputTriggers());
        }
		String actionNames[] = new String[actionNamesList.size()];
		actionNamesList.toArray(actionNames);
        inputManager.addListener(new ActionListener() {

            public void onAction(String name, boolean isPressed, float tpf) {
                PlayerAction action = PlayerAction.valueOf(name);
                if ((action != null) && isPressed) {
                    action.act(ctx);
                }
            }
        }, actionNames);       
    }      	

	 */
}
