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

package org.cogchar.bind.robokind.joint;

import org.robokind.motion.AbstractJoint;
import org.robokind.motion.config.JointConfig;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class BoneJoint extends AbstractJoint<JointConfig,SkeletonController> {
        /**
         * Creates a new BoneJoint from the given JointConfig and controller.
         * @param params JointConfig for the new Joint
         * @param controller the Joint's controller
         */
	protected BoneJoint(JointConfig jc, SkeletonController controller){
		super(jc,controller);
	}
    
}
