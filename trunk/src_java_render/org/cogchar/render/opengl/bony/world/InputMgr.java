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
package org.cogchar.render.opengl.bony.world;

import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class InputMgr {
	
	public static String		SHOOT_ACTION_NAME	= "shoot";
	
	public static void attachShootTriggerAndListener (InputManager inMgr, ActionListener actionListener) {
		
		attachMouseButtonTriggerAndListener(inMgr, actionListener, SHOOT_ACTION_NAME, MouseInput.BUTTON_LEFT);
		
	}
	public static void attachMouseButtonTriggerAndListener (InputManager inMgr, ActionListener actionListener, 
					String trigName, int buttonID) {
		
		inMgr.addMapping(trigName, new MouseButtonTrigger(buttonID));
		inMgr.addListener(actionListener, trigName);
		
	}	
}
