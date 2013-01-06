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

package org.cogchar.render.opengl.scene;

import com.jme3.animation.LoopMode;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.slf4j.Logger;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public class AbstractThingCinematicMgr extends BasicDebugger {
	
	protected Logger myLogger = getLoggerForClass(this.getClass());
	protected CogcharRenderContext myCRC;
	
	/* This will replace common methods in PathMgr/SpatialAnimMgr soon
	public void storeAnimationsFromConfig(SpatialActionSetConfig config, CogcharRenderContext crc) {

		myCRC = crc;

		// Next, we build the cinematics, using named tracks/waypoints/rotations if required.
		for (SpatialActionConfig sac : config.mySACs) {
			buildAnimation(sac);
		}
			
	}
	*/
	
	protected boolean noPosition(float[] waypointDef) {
        return (new Float(waypointDef[0]).isNaN()) || (new Float(waypointDef[1]).isNaN()) || (new Float(waypointDef[2]).isNaN());
    }
	
	protected LoopMode setLoopMode(String modeString) {
        LoopMode loopJmeType = null;
        for (LoopMode testType : LoopMode.values()) {
            if (modeString.equals(testType.toString())) {
                loopJmeType = testType;
            }
        }
        return loopJmeType;
    }
	
	public enum ControlAction {

        PLAY, STOP, PAUSE
    }
	
}
