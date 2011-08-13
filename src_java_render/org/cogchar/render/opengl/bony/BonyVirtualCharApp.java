/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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
 * 
 * 
 * This file also contains fragments copied from the JMonkeyEngine test code.
 * See http://www.jmonkeyengine.org
 */

package org.cogchar.render.opengl.bony;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.app.SimpleApplication;
import java.awt.Canvas;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class BonyVirtualCharApp extends SimpleApplication {

	// private		AnimChannel				channel;

	protected	BonyContext				myContext = new BonyContext();


	public BonyVirtualCharApp() {
		super();
		myContext = new BonyContext();
		myContext.setApp(this);
	}
	public BonyContext getBonyContext() { 
		return myContext;
	}
	public void initCharPanelWithCanvas() { 		
		this.createCanvas();
		Canvas c = BonyGUI.makeAWTCanvas(this);
		VirtCharPanel vcp = new VirtCharPanel();
		vcp.setRenderCanvas(c);
		myContext.setPanel(vcp);
		// assetManager does not exist until start is called, triggering simpleInit callback.
	}
	public void startJMonkeyCanvas() { 
		System.out.println("*********** startJMonkeyCanvas is starting");
		this.startCanvas();  		// equivalent to this?:     start(JmeContext.Type.Canvas);
		
		Future fut = enqueue(new Callable() {
			public Object call() throws Exception {
				System.out.println("*********** Enqueued call is executing");
				return null;
			}
		});
//to retrieve return value (waits for call to finish, fire&forget otherwise):
		Object unusedResult;
		try {
			unusedResult = fut.get();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		System.out.println("*********** startJMonkeyCanvas is returning");
	}

	@Override public void simpleInitApp() {
		System.out.println("*********** BonyVirtualCharApp.simpleInitApp() is starting");
		// Perform actions that cannot be done until engine is running.
		BonyGUI.setupCameraLightAndViewport(myContext);
		BonyGUI.initScoreBoard(myContext);
		System.out.println("*********** BonyVirtualCharApp.simpleInitApp() is finished");
	}

    @Override  public void initialize() {
		System.out.println("********************* StickFigureTest.initialize() called, calling super.initialize()");
		super.initialize();
		System.out.println("********************* StickFigureTest.initialize() returning");
	}	
	
}
