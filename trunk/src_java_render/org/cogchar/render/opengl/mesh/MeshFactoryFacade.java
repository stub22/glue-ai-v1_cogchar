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
package org.cogchar.render.opengl.mesh;

import org.cogchar.render.opengl.scene.ModelSpatialFactory;

/**
 * So far this "facade" merely inits the four current kinds of MeshFactories, and hands them out
 * to anyone who asks.
 * @author Stu B. <www.texpedient.com>
 */
public class MeshFactoryFacade {
	
	private		ShapeMeshFactory	myShapeMF;
	private		WireMeshFactory		myWireMF;
	private		FancyMeshFactory	myFancyMF;

	
	public MeshFactoryFacade() {
		
		myShapeMF = new ShapeMeshFactory();
		myWireMF = new WireMeshFactory();
		myFancyMF = new FancyMeshFactory();

	}
	
	public ShapeMeshFactory getShapeMF() {
		return myShapeMF;
	}
	public WireMeshFactory getWireMF() {
		return myWireMF;
	}
	public FancyMeshFactory getFancyMF() {
		return myFancyMF;
	}

}
