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

import org.appdapter.core.name.Ident;
import static org.cogchar.name.goody.GoodyNames.TYPE_BIT_BOX;
import static org.cogchar.name.goody.GoodyNames.TYPE_BIT_CUBE;
import static org.cogchar.name.goody.GoodyNames.TYPE_BOX;
import static org.cogchar.name.goody.GoodyNames.TYPE_CAMERA;
import static org.cogchar.name.goody.GoodyNames.TYPE_CROSSHAIR;
import static org.cogchar.name.goody.GoodyNames.TYPE_FLOOR;
import static org.cogchar.name.goody.GoodyNames.TYPE_SCOREBOARD;
import static org.cogchar.name.goody.GoodyNames.TYPE_TEXT;
import static org.cogchar.name.goody.GoodyNames.TYPE_TICTAC_GRID;
import static org.cogchar.name.goody.GoodyNames.TYPE_TICTAC_MARK;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class GoodyRenderTestContent {
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
	

	public void makeRegularGoodyOrbits() { 
		// Goal is to make at least one of each goody type, and make it move + rotate.  
		// Let's put them all (except for "floor" and other large/weird ones) orbiting in a circle (then ellipse!),
		// and rotating as they orbit.  We do this by pumping arrays of phased numbers to GoodyActions.
		// Each goody instance in this setup is called a "planet".
		//	ThingActionSpec	taMakeTTG 
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
	
/***** 
 	// ga.getGoodyID(), ga.getLocationVector(), ga.getRotationQuaternion(),	scale, bitBoxState);

		// ga.getSize()[0]
		// scale.getX(), ga.getColor(), 
		// ga.getText()); 
				Vector3f scale = ga.getVectorScale();
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
		 
 * 		ticTac.setLocation(locXtt, locYtt, locZtt);
		ticTac.setRotation(0.0f, 1.0f, 0.0f, rotDeg);
		ticTac.setScale(scale);
		// Let's change the default color (which happens to be blue) to yellow. Currently, this is only supported *on creation* for
		// TicTacGrid and the Floor [also now Text Things], but can be applied to the currently displayed geometry of any active thing after creation
		ticTac.setColor(1f,1f,0f);
				BasicTypedValueMap title_createParamMap = (BasicTypedValueMap) title.getParamWriter().getValueMap();		

		vWorld.sendUpdates();

		// boolean bitBoxState = Boolean.valueOf(ga.getSpecialString(GoodyNames.BOOLEAN_STATE));
		// boolean isAnO = Boolean.valueOf(ga.getSpecialString(GoodyNames.USE_O));
	// ga.getColor()	
		//  Scoreboard - int rows = Integer.valueOf(ga.getSpecialString(GoodyNames.ROWS));
		* 
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
	bb2_paramWriter.putLocation(locX2 - 10f, locY2 + 10f, locZ2 + 10f);
			bb2_paramWriter.putRotation(1.0f, 0.0f, 1.0f, rotDeg + 90f);
			// Here's a vector scaling. We can also use setScale/moveScale with either scalar or vector scalings
			// if we just want to adjust scale.
			bb2_paramWriter.putScale(3*scale, scale, 10*scale);
			* 
			* vWorld.removeAllThings();
			* 
			* 		ticTac.setRotation(0.0f, 1.0f, 0.0f, rotDeg);
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
 */	
}
