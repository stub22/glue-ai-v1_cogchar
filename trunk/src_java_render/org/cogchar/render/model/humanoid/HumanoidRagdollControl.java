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

package org.cogchar.render.model.humanoid;

import com.jme3.bullet.control.KinematicRagdollControl;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class HumanoidRagdollControl extends KinematicRagdollControl {
	
	private static final Logger theLogger = LoggerFactory.getLogger(HumanoidRagdollControl.class);
	
    public HumanoidRagdollControl(float weightThreshold) {
		super(weightThreshold);
	}
	
	@Override public synchronized Mode getMode() {
		return super.getMode();
	}

	@Override public synchronized void setKinematicMode() {
		theLogger.info("setKinematicMode()");
		super.setKinematicMode();
	}

	@Override protected synchronized void setMode(Mode mode) {
		theLogger.info("setMode({})", mode);
		super.setMode(mode);
	}

	@Override public synchronized void setRagdollMode() {
		theLogger.info("setRagdollMode()");
		super.setRagdollMode();
	}

	@Override public synchronized void update(float tpf) {
		try {
			super.update(tpf);
		} catch (Throwable t) {
			theLogger.error("Error in RagdollUpdate - bone control weirdness?", t);
		}
	}

	@Override public synchronized void blendToKinematicMode(float blendTime) {
		theLogger.info("blendToKinematicMode({})", blendTime);
		super.blendToKinematicMode(blendTime);
	}
	
	@Override  public synchronized void collision(PhysicsCollisionEvent event) {
		// theLogger.info("collision({})", event);
		super.collision(event);
	}
}
