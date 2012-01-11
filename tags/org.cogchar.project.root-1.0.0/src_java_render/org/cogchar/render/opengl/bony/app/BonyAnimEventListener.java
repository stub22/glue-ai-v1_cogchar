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
package org.cogchar.render.opengl.bony.app;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyAnimEventListener  implements AnimEventListener  {
	@Override public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
		if (animName.equals("Walk") || animName.equals("Dodge")) {
			channel.setAnim("stand", 0.50f);
			channel.setLoopMode(LoopMode.DontLoop);
			channel.setSpeed(1f);
		}
	}

	@Override public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
		System.out.println("onAnimChange: name=" + animName + ", ctrl=" + control + ", chn=" + channel);
	}	
}
