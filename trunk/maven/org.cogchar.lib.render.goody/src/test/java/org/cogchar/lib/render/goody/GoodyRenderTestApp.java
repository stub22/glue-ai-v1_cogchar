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
import com.jme3.renderer.ViewPort;
import com.jme3.input.FlyByCamera;

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.vworld.GoodyActionParamWriter;
import org.cogchar.impl.thing.basic.BasicThingActionSpec;
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
import org.cogchar.render.app.entity.GoodySpace;
import org.cogchar.render.sys.goody.GoodyModularRenderContext;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClientImpl;

import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.platform.gui.keybind.KeyBindingConfig;
import org.cogchar.platform.trigger.CommandSpace;
import org.cogchar.render.app.bony.BonyVirtualCharApp;
import org.cogchar.render.app.entity.GoodyFactory;
import org.cogchar.render.app.entity.VWorldEntityActionConsumer;
import org.cogchar.render.sys.input.VW_InputBindingFuncs;


/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Goal is to use the Goody-ThingAction API to directly (without a repo) make each of the Goody types display and do 
 * each of its "moves", in a way that is straightforward to check visually, allowing us to check position, size, 
 * color of everything displayed.
 */

public class GoodyRenderTestApp extends BonyVirtualCharApp<GoodyModularRenderContext> {

	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		RenderConfigEmitter rce = new RenderConfigEmitter();
		GoodyRenderTestApp app = new GoodyRenderTestApp(rce);
		app.start();
	}
	public GoodyRenderTestApp(RenderConfigEmitter rce) { 
		super(rce);
	}
	@Override protected GoodyModularRenderContext makeCogcharRenderContext() {
		GoodyRenderRegistryClient grrc = new GoodyRenderRegistryClientImpl();
		RenderConfigEmitter rce = getConfigEmitter();
		GoodyModularRenderContext gmrc = new GoodyModularRenderContext(grrc, rce);
		gmrc.setApp(this);
		return gmrc;
	}
	@Override public void simpleInitApp() {
		getLogger().info("Hooray for Goodies!");
		super.simpleInitApp();
		FlyByCamera fbc = getFlyByCamera();
		fbc.setMoveSpeed(20);
				
		GoodyModularRenderContext renderCtx = getBonyRenderContext();
		GoodyRenderRegistryClient grrc = renderCtx.getGoodyRenderRegistryClient();
		GoodyFactory gFactory = GoodyFactory.createTheFactory(grrc, renderCtx);	
		
		initContent();
		// hideJmonkeyDebugInfo();
	}
	private GoodySpace getGoodySpace() { 
		GoodyFactory gFactory = GoodyFactory.getTheFactory();
		return gFactory.getGoodySpace();
	}
	private void initContent() {
		ViewPort  pvp = getPrimaryAppViewPort();		
		pvp.setBackgroundColor(ColorRGBA.Blue);
		shedLight();
		// Hook-in for Goody system
		
		GoodySpace gSpace = getGoodySpace();
		// hrwMapper.addHumanoidGoodies(veActConsumer, hrc);		
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
		static final float	locX1 = 28.0f, locY1 = 19.0f, locZ1 = 16.0f;
		static final float	locX2 = locX1, locY2 = 30.0f, locZ2 = locZ1;
		static final float	locXtt = -locX1, locYtt = locY2, locZtt = -locZ1;
		static final float	scale = 3.0f;
		static final float	rotDeg = 33.0f;
		static final float	standardDuration = 10f; // seconds
	
	 
	
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

		
		vWorld.sendUpdates();
		bb1_createParamMap.putValueAtName(GoodyNames.BOOLEAN_STATE, "true");* 
		ticTac_createParamMap.putValueAtName(GoodyNames.COORDINATE_X, 2);
		ticTac_createParamMap.putValueAtName(GoodyNames.COORDINATE_Y, 2);
		ticTac_createParamMap.putValueAtName(GoodyNames.USE_O, "false");
		title_createParamMap.putValueAtName(GoodyNames.TEXT, "A VirtualWorld Test");* 
		* 
		ticTac.myPendingActionVerbID = GoodyNames.ACTION_SET;
		* 	bb2.myPendingActionVerbID = GoodyNames.ACTION_MOVE;* 
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
* 			bb2_paramWriter.putLocation(locX2 - 10f, locY2 + 10f, locZ2 + 10f);
			bb2_paramWriter.putRotation(1.0f, 0.0f, 1.0f, rotDeg + 90f);
			// Here's a vector scaling. We can also use setScale/moveScale with either scalar or vector scalings
			// if we just want to adjust scale.
			bb2_paramWriter.putScale(3*scale, scale, 10*scale);
			* 
			* vWorld.removeAllThings();
			* 
			* 		ticTac.setRotation(0.0f, 1.0f, 0.0f, rotDeg);
			* 
			* 
		BasicTypedValueMap btvm = new ConcreteTVM();
		GoodyActionParamWriter gapw = new GoodyActionParamWriter(btvm);
		
		gapw.putLocation(locX, locY, locZ);
		Ident actRecID = new FreeIdent("action_#" + ran.nextInt());
		Ident tgtThingTypeID = GoodyNames.TYPE_BIT_BOX;
		Ident srcAgentID = null;
		Long postedTStampMsec = System.currentTimeMillis();
		BasicThingActionSpec btas = new BasicThingActionSpec(actRecID, tgtThingID, tgtThingTypeID, verbID, srcAgentID, paramTVMap, postedTStampMsec);	
		sendThingActionSpec(btas, ran, debugFlag);
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
	/*
	public void refreshInputBindingsAndHelpScreen(KeyBindingConfig keyBindConfig, CommandSpace cspace) {
		RenderRegistryClient rrc = getRenderRegistryClient();
		VW_InputBindingFuncs.setupKeyBindingsAndHelpScreen(rrc, keyBindConfig, getAppStub(), 
					getJMonkeyAppSettings(), cspace);
	}	
	*/
}
