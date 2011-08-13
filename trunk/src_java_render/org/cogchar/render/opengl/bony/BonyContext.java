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
package org.cogchar.render.opengl.bony;

import com.jme3.animation.AnimControl;
import java.util.List;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyContext {
	protected	BonyVirtualCharApp		myApp;
	protected	VirtCharPanel			myPanel;    
	protected	ScoreBoard				myScoreBoard;
	protected	List<AnimControl>		myAnimControls;

	public BonyVirtualCharApp getApp() {
		return myApp;
	}

	public void setApp(BonyVirtualCharApp app) {
		this.myApp = app;
	}

	public VirtCharPanel getPanel() {
		return myPanel;
	}

	public void setPanel(VirtCharPanel panel) {
		this.myPanel = panel;
	}

	public ScoreBoard getScoreBoard() {
		return myScoreBoard;
	}

	public void setScoreBoard(ScoreBoard scoreBoard) {
		this.myScoreBoard = scoreBoard;
	}

	public List<AnimControl> getAnimControls() {
		return myAnimControls;
	}

	public void setAnimControls(List<AnimControl> animControls) {
		this.myAnimControls = animControls;
	}

}
