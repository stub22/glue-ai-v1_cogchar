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

package org.cogchar.lib.render.goody;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import org.cogchar.render.app.core.CogcharPresumedApp;
import org.cogchar.render.opengl.scene.TextMgr;
import com.jme3.math.ColorRGBA;
import org.appdapter.core.name.Ident;
import org.cogchar.impl.thing.basic.BasicTypedValueMap;
import org.cogchar.name.goody.GoodyNames;

import org.cogchar.render.app.core.CogcharPresumedApp;
import org.cogchar.render.goody.basic.GoodyBox;
import org.cogchar.render.goody.basic.VirtualFloor;
import org.cogchar.render.goody.bit.BitBox;
import org.cogchar.render.goody.bit.BitCube;
import org.cogchar.render.goody.bit.TicTacGrid;
import org.cogchar.render.goody.bit.TicTacMark;
import org.cogchar.render.goody.flat.CrossHairGoody;
import org.cogchar.render.goody.flat.ScoreBoardGoody;
import org.cogchar.render.goody.flat.TextGoody;
import org.cogchar.render.optic.goody.VWorldCameraEntity;
import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.sys.context.ConfiguredPhysicalModularRenderContext;


import org.cogchar.render.sys.context.CoreFeatureAdapter;
import org.cogchar.render.sys.registry.RenderRegistryClient;

import static org.cogchar.name.goody.GoodyNames.*;
/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Goal is to make each of the Goody types display and do each of its "moves", in 
 * a way that is straightforward to check visually, allowing us to check position,
 * size, color of everything displayed.
 */

public class GoodyRenderTestSteps extends CogcharPresumedApp {
	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		GoodyRenderTestSteps app = new GoodyRenderTestSteps();
		app.start();
	}

	@Override protected CogcharRenderContext makeCogcharRenderContext() {
		return new ConfiguredPhysicalModularRenderContext();
	}
	@Override public void simpleInitApp() {
		getLogger().info("Hooray for Goodies!");
		super.simpleInitApp();
		flyCam.setMoveSpeed(20);
		initContent();
	}

	public void initContent() {
		viewPort.setBackgroundColor(ColorRGBA.Blue);
		shedLight();
	}
	
	static class GoodyOuterWrapper {
		public double	relativeOrbitPhaseRad; 
	}
	// State
	int myTotalStepCum = 0;
	double myRotPhaseRadCum = 0.0;
	
	// Config
	int myPlanetCount = 17;
	int myMinorPosStepCount = 11;
	int myGlobalPosCycleLen = myPlanetCount * myMinorPosStepCount;
	double posPhaseRadPerStep = 2.0 * Math.PI / myGlobalPosCycleLen;
	double	rotPhaseRadPerStep = 0.03;
	
	Ident	goodyTypes[] = {TYPE_BIT_BOX, TYPE_BIT_CUBE, TYPE_FLOOR, TYPE_TICTAC_MARK, TYPE_TICTAC_GRID, 
		TYPE_CROSSHAIR, TYPE_SCOREBOARD, TYPE_TEXT, TYPE_BOX, TYPE_CAMERA};
	
	 
	
	// ga.getGoodyID(), ga.getLocationVector(), ga.getRotationQuaternion(),	scale, bitBoxState);
		// boolean bitBoxState = Boolean.valueOf(ga.getSpecialString(GoodyNames.BOOLEAN_STATE));
		// boolean isAnO = Boolean.valueOf(ga.getSpecialString(GoodyNames.USE_O));
	// ga.getColor()	
		//  Scoreboard - int rows = Integer.valueOf(ga.getSpecialString(GoodyNames.ROWS));
		// ga.getSize()[0]
		// scale.getX(), ga.getColor(), 
		// ga.getText()); 
/*
 * 				Vector3f scale = ga.getVectorScale();
				Float scalarScale = ga.getScale();
				if ((scale == null) && (scalarScale != null)) {
					scale = new Vector3f(scalarScale, scalarScale, scalarScale);
				}
				* 
					// scale.getX() should return scalarScale if that is provided, or use Robosteps API scalar scale which
					// is represented as a vector scale with identical components
	
 	
				} else if (GoodyNames.TYPE_CAMERA.equals(ga.getType())) {
					Ident cameraUri = ga.getGoodyID();
					if (myActionConsumer.getGoody(cameraUri) == null) { //Otherwise this camera wrapper is already created
						theLogger.info("Adding a VWorldCameraEntity for {}", cameraUri);
						// This evidences the fact that the CameraMgr needs to switch to URIs to identify cameras, not strings:
						Camera cam = myRRC.getOpticCameraFacade(null).getNamedCamera(cameraUri.getLocalName());
						if (cam != null) {
							newGoody = (new VWorldCameraEntity(myRRC, cameraUri, cam));
			*/
	
	
	public void makeRegularGoodyOrbit() { 
		// Goal is to make at least one of each goody type, and make it move + rotate.  
		// Let's put them all (except for "floor" and other large/weird ones) orbiting in a circle (then ellipse!),
		// and rotating as they orbit.  We do this by pumping arrays of phased numbers to GoodyActions.
		// Each goody instance in this setup is called a "planet".
		
		
	}
	public void updatePlanets() {
		myTotalStepCum ++;
		myRotPhaseRadCum += rotPhaseRadPerStep;
		updateGoodyPlanetPositions();
		updateGoodyPlanetRotations();
	}
	private void updateGoodyPlanetPositions() {
		int myPosOffset = myTotalStepCum % myGlobalPosCycleLen;
		double myPosPhase = myPosOffset * posPhaseRadPerStep;
		
	}
	private void updateGoodyPlanetRotations() { 
		
	}
	private void putIrregularValues(int coordX, int coordY, boolean useO) {
		
	}
/*
 * 		ticTac.setLocation(locXtt, locYtt, locZtt);
		ticTac.setRotation(0.0f, 1.0f, 0.0f, rotDeg);
		ticTac.setScale(scale);
		// Let's change the default color (which happens to be blue) to yellow. Currently, this is only supported *on creation* for
		// TicTacGrid and the Floor [also now Text Things], but can be applied to the currently displayed geometry of any active thing after creation
		ticTac.setColor(1f,1f,0f);
				BasicTypedValueMap title_createParamMap = (BasicTypedValueMap) title.getParamWriter().getValueMap();		
		title_createParamMap.putValueAtName(GoodyNames.TEXT, "A VirtualWorld Test");
		
		vWorld.sendUpdates();
		ticTac_createParamMap.putValueAtName(GoodyNames.COORDINATE_X, 2);
		ticTac_createParamMap.putValueAtName(GoodyNames.COORDINATE_Y, 2);
		ticTac_createParamMap.putValueAtName(GoodyNames.USE_O, "false");
		ticTac.myPendingActionVerbID = GoodyNames.ACTION_SET;
		bb1.moveLocation(locX1 + 20f, locY1 + 35f, locZ1, standardDuration);
		
		bb2_paramWriter.putScale(3*scale, scale, 10*scale);* 
		bb2_paramWriter.putDuration(standardDuration);
					* 
	public void putLocation(float locX, float locY, float locZ) {
	public void putRotation(float rotAxisX, float rotAxisY, float rotAxisZ, float magDeg) 
	public void putSize(float sizeX, float sizeY, float sizeZ) 
	public void putScale(float scalarScale) 
	putScale(float scaleX, float scaleY, float scaleZ) 
	public void putDuration(float duration) 	
	public void putColor(float colorR, float colorG, float colorB, float colorAlpha) 
* 
		* 
		
		* 
 */	
	private void shedLight() {

		// ConfiguredPhysicalModularRenderContext cpmrc = (ConfiguredPhysicalModularRenderContext) getRenderContext();
		CogcharRenderContext cpmrc = getRenderContext();
		CoreFeatureAdapter.setupLight(cpmrc);
		shedMoreLight(cpmrc);
	}

	private void shedMoreLight(CogcharRenderContext crc) {
		RenderRegistryClient rrc = crc.getRenderRegistryClient();
		Vector3f otherLightDir = new Vector3f(0.1f, 0.7f, 1.0f).normalizeLocal();
		DirectionalLight odl = rrc.getOpticLightFacade(null).makeWhiteOpaqueDirectionalLight(otherLightDir);
		CoreFeatureAdapter.addLightToRootNode(crc, odl);
	}
}
