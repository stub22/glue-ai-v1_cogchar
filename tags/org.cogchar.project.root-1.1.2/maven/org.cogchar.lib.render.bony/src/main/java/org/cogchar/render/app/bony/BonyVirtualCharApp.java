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

package org.cogchar.render.app.bony;

import org.cogchar.render.app.core.PhysicalApp;
import java.awt.Canvas;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.render.sys.context.WorkaroundFuncsMustDie;
import org.cogchar.render.gui.bony.VirtualCharacterPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Assumes that we want to run our application inside a Swing VirtualCharacterPanel 
 * (rather than a standalone native OpenGL window).
 * @author Stu B. <www.texpedient.com>
 */

public abstract class BonyVirtualCharApp<BRCT extends BonyRenderContext> extends PhysicalApp<BRCT> {
	static Logger theLogger = LoggerFactory.getLogger(BonyVirtualCharApp.class);

    private     boolean                 myCanvasStartedFlag;

	public BonyVirtualCharApp(RenderConfigEmitter rce) {
		super(rce);
        myCanvasStartedFlag = false;
	}
	public BRCT getBonyRenderContext() { 
		return getRenderContext();
	}

	public void initCharPanelWithCanvas(VirtualCharacterPanel vcp) { 
		// Works
		applySettings();
		hideJmonkeyDebugInfo();
		this.createCanvas();
		// Does not work at this time or subsq:
		//applySettings();
		Canvas c = WorkaroundFuncsMustDie.makeAWTCanvas(this);
		vcp.setRenderCanvas(c);
		getBonyRenderContext().setPanel(vcp);
		// assetManager does not exist until start is called, triggering simpleInit callback.
	}
	/**
	 * Blocks until all canvas init is complete, including execution of the simpleInitApp methods.
	 */
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
	// This will get called during the startJMonkeyCanvas invocation above, *or* during any alternative
	// App-start invoked from a test harness, e.g. GoodyRenderTestApp.
	@Override public void simpleInitApp() {
		theLogger.info("*********** BonyVirtualCharApp.simpleInitApp() is starting");
		super.simpleInitApp();
		theLogger.info("*********** BonyVirtualCharApp.simpleInitApp() is finished - JME3 infrastructre is ready.");
	}
	
}
