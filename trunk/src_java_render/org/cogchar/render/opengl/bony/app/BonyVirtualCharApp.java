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

import org.cogchar.render.opengl.app.PhysicalApp;
import java.awt.Canvas;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.opengl.bony.sys.BonyCanvasFuncs;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.bony.gui.VirtualCharacterPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class BonyVirtualCharApp<BRCT extends BonyRenderContext> extends PhysicalApp<BRCT> {
	static Logger theLogger = LoggerFactory.getLogger(BonyVirtualCharApp.class);

    private     boolean                 myCanvasStartedFlag;

	public BonyVirtualCharApp(BonyConfigEmitter bce) {
		super(bce);
        myCanvasStartedFlag = false;
	}
	public BRCT getBonyRenderContext() { 
		return getRenderContext();
	}
	public BonyConfigEmitter getBonyConfigEmitter() { 
		return (BonyConfigEmitter) myConfigEmitter;
	}

	public void initCharPanelWithCanvas(VirtualCharacterPanel vcp) { 
		// Works
		applySettings();
		this.createCanvas();
		// Does not work at this time or subsq:
		//applySettings();
		Canvas c = BonyCanvasFuncs.makeAWTCanvas(this);
		vcp.setRenderCanvas(c);
		getBonyRenderContext().setPanel(vcp);
		// assetManager does not exist until start is called, triggering simpleInit callback.
	}
	public void startJMonkeyCanvas() { 
		theLogger.info("*********** startJMonkeyCanvas is starting");
		this.startCanvas();  		// equivalent to this?:     start(JmeContext.Type.Canvas);
		
		Future fut = enqueue(new Callable() {
			public Object call() throws Exception {
				theLogger.info("*********** Enqueued call is executing");
				return null;
			}
		});
		theLogger.info("*********** startJMonkeyCanvas is waiting for the future to come");
//to retrieve return value (waits for call to finish, fire&forget otherwise):
		Object unusedResult;
		try {
			unusedResult = fut.get();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		theLogger.info("*********** startJMonkeyCanvas sees that the future has arrived, so it is returning");
        myCanvasStartedFlag = true;
	}
    
    public boolean isCanvasStarted(){
        return myCanvasStartedFlag;
    }

	@Override public void simpleInitApp() {
		theLogger.info("*********** BonyVirtualCharApp.simpleInitApp() is starting");
		super.simpleInitApp();
		// (Finally!) Perform actions that cannot be done until JME3 engine is running (which is now!)
		BRCT ctx = getBonyRenderContext();
		// TODO:  Refactor out direct references to App into calls to context.

		BonyCanvasFuncs.setupCameraLightAndViewport(ctx);
		//BonyCanvasFuncs.initScoreBoard(myContext);
		theLogger.info("*********** BonyVirtualCharApp.simpleInitApp() is finished");
	}
	
}
