/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.render.model.humanoid;

/**
 *
 * @author pow
 */
public class HumanoidBoneDesc {
	private String	mySpatialName;
	public HumanoidBoneDesc(String spatialName) {
		mySpatialName = spatialName;
	}
	public String	getSpatialName() { 
		return mySpatialName;
	}
	@Override public String toString() { 
		return "HBD[spatName=" + mySpatialName + "]";
	}
}
