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
 *
 */

package org.cogchar.render.model.bony;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

/**
 * A collection of BoneStates, indexed by name.
 * 
 * @author Stu B. <www.texpedient.com>
 */
public class FigureState {
	private Map<String, BoneState>	myBoneStateMap = new HashMap<String, BoneState>();
	
	public BoneState registerBoneState(String boneName) {
		BoneState bs = new BoneState(boneName);
		myBoneStateMap.put(boneName, bs);
		return bs;
	}
	
	public BoneState getBoneState(String boneName) {
		return myBoneStateMap.get(boneName);
	}
	public BoneState obtainBoneState(String boneName) {
		BoneState bs = getBoneState(boneName);
		if (bs == null) {
			bs = registerBoneState(boneName);
		}
		return bs;
	}
	public Collection<BoneState> getBoneStates() {
		return myBoneStateMap.values();
	}
	@Override public String toString() { 
		return "FigureState[" + myBoneStateMap + "]";
	}
}
