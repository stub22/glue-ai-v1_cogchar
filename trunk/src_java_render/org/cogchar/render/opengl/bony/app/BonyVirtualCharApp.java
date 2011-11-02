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

package org.cogchar.render.opengl.bony.app;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import java.awt.Canvas;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.cogchar.render.opengl.bony.sys.BonyCanvasFuncs;
import org.cogchar.render.opengl.bony.sys.BonyContext;
import org.cogchar.render.opengl.bony.sys.VirtCharPanel;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class BonyVirtualCharApp extends DemoApp {

	// private		AnimChannel				channel;

	protected	BonyContext				myContext = new BonyContext();
    private     boolean                 myCanvasStartedFlag;
	private		String					myLWJGL_RendererName;
	private		int						myCanvasWidth, myCanvasHeight;

	public BonyVirtualCharApp(String lwjglRendererName, int canvasWidth, int canvasHeight) {
		super(lwjglRendererName);
		myLWJGL_RendererName = lwjglRendererName;
		myCanvasWidth = canvasWidth;
		myCanvasHeight = canvasHeight;
		myContext = new BonyContext();
		myContext.setApp(this);
        myCanvasStartedFlag = false;
	}
	public BonyContext getBonyContext() { 
		return myContext;
	}
	protected void applySettings() { 
	/* http://jmonkeyengine.org/wiki/doku.php/jme3:intermediate:appsettings
	 * Every class that extends jme3.app.SimpleApplication has properties 
	 * that can be configured by customizing a com.jme3.system.AppSettings 
	 * object. Configure the settings before you call app.start() on 
	 * the application object. If you change display settings during runtime, 
	 * call app.restart() to make them take effect.
	 */
		AppSettings settings = new AppSettings(true);
		settings.setRenderer(myLWJGL_RendererName);
		settings.setWidth(myCanvasWidth);
		settings.setHeight(myCanvasHeight);
		setSettings(settings);			

	}
	public void initCharPanelWithCanvas() { 
		// Works
		applySettings();
		this.createCanvas();
		// Does not work at this time or subsq:
		//applySettings();
		Canvas c = BonyCanvasFuncs.makeAWTCanvas(this);
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
        myCanvasStartedFlag = true;
	}
    
    public boolean isCanvasStarted(){
        return myCanvasStartedFlag;
    }

	@Override public void simpleInitApp() {
		System.out.println("*********** BonyVirtualCharApp.simpleInitApp() is starting");
		// Perform actions that cannot be done until engine is running.
		BonyCanvasFuncs.setupCameraLightAndViewport(myContext);
		//BonyCanvasFuncs.initScoreBoard(myContext);
		System.out.println("*********** BonyVirtualCharApp.simpleInitApp() is finished");
	}

    @Override  public void initialize() {
		System.out.println("********************* StickFigureTest.initialize() called, calling super.initialize()");
		super.initialize();
		System.out.println("********************* StickFigureTest.initialize() returning");
	}	
	
}
