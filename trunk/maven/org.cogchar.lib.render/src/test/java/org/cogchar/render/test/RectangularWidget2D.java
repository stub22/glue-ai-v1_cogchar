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

package org.cogchar.render.test;

import com.jme3.scene.Node;
import java.util.concurrent.Callable;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.cogchar.render.sys.task.Queuer;
import org.cogchar.render.sys.registry.RenderRegistryClient;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class RectangularWidget2D extends BasicDebugger implements ParamValueListener {
	protected	Ident		myIdent;	
	protected	Node		myNode, myParentNode;		
	public enum CoordName {
		X, Y, ZOrder, Width, Height
	}
	// If we consider that each of these coords might have some normal range...
	protected Integer		myX = 0, myY = 0, myWidth = 100, myHeight = 100;
	protected Float		myZOrder = -0.5f;	
	
	private	Queuer	myQueuer;
	
	public RectangularWidget2D(RenderRegistryClient rrc, Ident requiredID) {
		myIdent = requiredID;
		myQueuer = new Queuer(rrc);
	}
	
	protected Node getMyNode() { 
		if (myNode == null) {
			myNode = new Node("TextBox2D-mainNode:" + myIdent.getLocalName());
		}
		return myNode;
	}	
	public void setCoordinates(Integer x, Integer y, Float zOrder, Integer width, Integer height, Queuer.QueueingStyle qStyle) { 
		myX = x;   myY = y;  myZOrder = zOrder;  myWidth = width;   myHeight = height;
		applyTranslation(qStyle);
	}
	protected void applyTranslation(Queuer.QueueingStyle qStyle) {
		if ((myX != null) && (myY != null)) { 
			final Node n = getMyNode();
			myQueuer.enqueueForJme(new Callable() { // Do this on main render thread
				@Override public Void call() throws Exception {	
					n.setLocalTranslation(myX, myY, myZOrder);
					return null;
				}
			}, qStyle);

		}
	}
	@Override public void setNormalizedNumericParam(String paramName, float normZeroToOne) {
		CoordName cname = CoordName.valueOf(paramName);
		if (cname != null) {
			setOneCoordFractionOfRange(cname, normZeroToOne);
		}
	}
	// Set one coordinate toAppropriate to call from a MIDI-CC callback, with CC-val divided by 127 or whatev.
	private void setOneCoordFractionOfRange(CoordName coord, float normZeroToOne) {
		Queuer.QueueingStyle qStyle = Queuer.QueueingStyle.QUEUE_AND_RETURN;
		switch (coord) {
			case X:
				myX = MathUtils.getIntValInRange(0, 640, normZeroToOne);
				applyTranslation(qStyle);
			break;
			case Y:
				myY = MathUtils.getIntValInRange(0, 480, normZeroToOne);
				applyTranslation(qStyle);
			break;
			case ZOrder:
				myZOrder = MathUtils.getFloatValInRange(-10.0f, 10.0f, normZeroToOne);
				applyTranslation(qStyle);
			break;
			case Width:
				myWidth = MathUtils.getIntValInRange(0, 640, normZeroToOne);
			break;				
			case Height:
				myHeight = MathUtils.getIntValInRange(0, 480, normZeroToOne);
			break;				
		}
	}

}
