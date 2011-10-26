/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.render.opengl.bony.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author pow
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
}
