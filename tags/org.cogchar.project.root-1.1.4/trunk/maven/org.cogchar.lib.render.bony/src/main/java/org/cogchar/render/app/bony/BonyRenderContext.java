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
 */
package org.cogchar.render.app.bony;

import com.jme3.animation.AnimControl;
import java.util.List;
import javax.swing.JFrame;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.render.app.core.WorkaroundAppStub;
import org.cogchar.render.gui.bony.VirtualCharacterPanel;
import org.cogchar.render.sys.context.ConfiguredPhysicalModularRenderContext;
import org.cogchar.render.sys.context.WorkaroundFuncsMustDie;
import org.cogchar.render.sys.registry.RenderRegistryClient;
// import org.cogchar.render.goody.flat.GeneralScoreBoard;
// import org.cogchar.render.goody.flat.ScoreBoard;



/**
 * The contents of this class are pimples to be squeezed.
 * @author Stu B. <www.texpedient.com>
 */
public class BonyRenderContext extends ConfiguredPhysicalModularRenderContext {
	// TODO:  We want this app pointer to go away.
	protected	BonyVirtualCharApp		myApp;
	
	protected	VirtualCharacterPanel	myPanel;
	protected	JFrame					myFrame;

	protected	List<AnimControl>		myAnimControls;


	public BonyRenderContext(RenderRegistryClient rrc, RenderConfigEmitter rce) { 
		super(rrc, rce);
	}
	@Override public void completeInit() {
		super.completeInit();
		// RenderRegistryClient rrc = getRenderRegistryClient();
		WorkaroundAppStub was = getAppStub();
		WorkaroundFuncsMustDie.setupRegularCameraLightAndViewport(was);
	}
	
	// TODO:  We want this app pointer to go away.
	public BonyVirtualCharApp getApp() {
		return myApp;
	}

	// TODO:  We want this app pointer to go away.
	public void setApp(BonyVirtualCharApp app) {
		this.myApp = app;
	}


	public VirtualCharacterPanel getPanel() {
		return myPanel;
	}

	public void setPanel(VirtualCharacterPanel panel) {
		this.myPanel = panel;
		setWindowStatusReader(panel);
	}


	public List<AnimControl> getAnimControls() {
		return myAnimControls;
	}

	public void setAnimControls(List<AnimControl> animControls) {
		this.myAnimControls = animControls;
	}
	public void setFrame(JFrame jf) {
		myFrame = jf;
	}
	public JFrame getFrame() { 
		return myFrame;
	}
	

}
