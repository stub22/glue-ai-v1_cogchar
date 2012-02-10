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
package org.cogchar.demo.render.opengl.brick;

import com.jme3.bullet.BulletAppState;
import com.jme3.material.Material;
import org.cogchar.render.opengl.app.DemoApp;
import org.cogchar.render.opengl.optic.MatFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BrickApp extends DemoApp {

	protected Material			myBrickMat, myRockMat,myPondMat;
	protected BulletAppState	myPhysAppState;

	@Override public void simpleInitApp() {
		super.simpleInitApp();

		myPhysAppState = new BulletAppState();

		initMaterials();

		//bulletAppState.getPhysicsSpace().enableDebug(assetManager);
	}

  /** Initialize the materials used in this scene. */
  public void initMaterials() {
	MatFactory mf = getMatMgr();
	
    myBrickMat = mf.getBrickWallMat();
    myRockMat = mf.makeRockMat();
    myPondMat = mf.getPondMat();
	
  }  
	
}
