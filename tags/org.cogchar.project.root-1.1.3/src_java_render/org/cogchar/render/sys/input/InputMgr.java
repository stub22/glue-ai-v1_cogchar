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
package org.cogchar.render.sys.input;

import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class InputMgr {
	
	
	private		InputManager myJmeInputManager;
	
	public InputMgr(InputManager jmeIM) {
		myJmeInputManager = jmeIM;
	}
	public InputManager getJmeInputManager() { 
		return myJmeInputManager;
	}
	public void attachMouseButtonTriggerAndListener (ActionListener actionListener, 
					String actionName, int buttonID) {
		
		myJmeInputManager.addMapping(actionName, new MouseButtonTrigger(buttonID));
		myJmeInputManager.addListener(actionListener, actionName);
		
	}	
}
