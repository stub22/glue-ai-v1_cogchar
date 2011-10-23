/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.render.opengl.bony.model;

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
}
