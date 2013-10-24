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
package org.cogchar.test.rendgood.basic;

import java.util.Random;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.TypedValueMap;
import org.cogchar.api.thing.WantsThingAction;
import org.cogchar.name.goody.GoodyNames;

import org.cogchar.api.vworld.GoodyActionParamWriter;
import org.cogchar.impl.thing.basic.BasicThingActionSpec;
import org.cogchar.impl.thing.basic.BasicTypedValueMap;
import org.cogchar.impl.thing.fancy.ConcreteTVM;
import org.cogchar.name.dir.NamespaceDir;
import org.cogchar.render.app.entity.GoodyFactory;
import org.cogchar.render.app.entity.GoodySpace;
import org.cogchar.render.goody.bit.TicTacGrid;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class LocalGoodyHarness {

	protected Random myRandomizer = new Random();
	protected Ident myAgentID = new FreeIdent(NamespaceDir.CCRT_NS + "test_agent_LGH");

	public static class GARecipe implements Cloneable {

		public Ident entityID = null;
		public Ident entityTypeID = null; // getEntityTypeID();
		public Ident verbID = null; // myPendingActionVerbID;	
		public float duration;
		public float locX, locY, locZ;
		public float rotX, rotY, rotZ, rotMag;
		public float scaleX, scaleY, scaleZ;
		public float scalarScale;
		public float sizeX, sizeY, sizeZ;
		public float colorR, colorG, colorB, colorA;
		public boolean flag_state, flag_useO, flag_clearMarks;
		public int rows;
		public int coordX, coordY;
		public String text;

		public void writeToMap(GoodyActionParamWriter gapw) {
			gapw.putDuration(duration);
			gapw.putLocation(locX, locY, locZ);
			gapw.putRotation(rotX, rotY, rotZ, rotMag);
			gapw.putScaleVec(scaleX, scaleY, scaleZ);
			gapw.putScaleUniform(scalarScale);
			gapw.putSize(sizeX, sizeY, sizeZ);
			gapw.putColor(colorR, colorG, colorB, colorA);
			gapw.putObjectAtName(GoodyNames.ROWS, rows);
			gapw.putObjectAtName(GoodyNames.COORDINATE_X, coordX);
			gapw.putObjectAtName(GoodyNames.COORDINATE_Y, coordY);
			gapw.putObjectAtName(GoodyNames.BOOLEAN_STATE, flag_state);
			gapw.putObjectAtName(GoodyNames.USE_O, flag_useO);
			gapw.putObjectAtName(GoodyNames.TEXT, text);
			gapw.putObjectAtName(TicTacGrid.CLEAR_IDENT, flag_clearMarks);
			
		}

		public GARecipe copyMe() {
			GARecipe c = null;
			try {
				c = (GARecipe) this.clone();
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return c;
		}
	}

	public void makeActionSpecAndSend(GARecipe gar) {
		BasicTypedValueMap btvm = new ConcreteTVM();
		GoodyActionParamWriter paramWriter = new GoodyActionParamWriter(btvm);
		gar.writeToMap(paramWriter);
		String mintedInstIdPrefix = NamespaceDir.CCRT_NS + "minted_";
		Ident actRecID = mintInstanceID(mintedInstIdPrefix);

		Ident srcAgentID = myAgentID;
		Long postedTStampMsec = System.currentTimeMillis();

		TypedValueMap valueMap = paramWriter.getValueMap();
		BasicThingActionSpec actionSpec = new BasicThingActionSpec(actRecID,
			gar.entityID, gar.entityTypeID, gar.verbID, srcAgentID, valueMap, postedTStampMsec);

		GoodySpace gSpace = getGoodySpace();
		Ident srcGraphID = srcAgentID;
		// This winds up calling enqueueForJmeAndWait in
		// BasicGoodyEntity.attachToVirtualWorldNode
		//  1) Why does it need to wait?  This slows down the action-consuming thread.
		//  2) If it does wait, and we are already on the JME thread when we call this - hello deadlock!
		WantsThingAction.ConsumpStatus consumpStatus = gSpace.consumeAction(actionSpec, srcGraphID);
	}

	public Ident mintInstanceID(String instanceLabel) {
		int rNum = myRandomizer.nextInt(Integer.MAX_VALUE);
		return null;  // new FreeIdent(NS_ROBOSTEPS_INST + instanceLabel + "_" + rNum);
	}

	private GoodySpace getGoodySpace() {
		GoodyFactory gFactory = GoodyFactory.getTheFactory();
		return gFactory.getGoodySpace();
	}
}
