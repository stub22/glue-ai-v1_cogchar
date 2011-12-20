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
package org.cogchar.render.opengl.bony.sys;

import org.cogchar.render.opengl.bony.world.ScoreBoard;
import org.cogchar.render.opengl.bony.app.BonyVirtualCharApp;
import com.jme3.animation.AnimControl;
import java.io.File;
import java.util.List;
import javax.swing.JFrame;
import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.opengl.bony.state.FigureState;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class BonyContext {
	protected	BonyConfigEmitter		myConfigEmitter;
	protected	BonyVirtualCharApp		myApp;
	protected	VirtCharPanel			myPanel;   
	protected	JFrame					myFrame;
	protected	ScoreBoard				myScoreBoard;
	protected	List<AnimControl>		myAnimControls;
	protected	FigureState				myFigureState;

	public BonyContext(BonyConfigEmitter bce) { 
		myConfigEmitter = bce;
	}
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
	public void setFrame(JFrame jf) {
		myFrame = jf;
	}
	public JFrame getFrame() { 
		return myFrame;
	}
	public FigureState getFigureState() {
		return myFigureState;
	}
	public void setFigureState(FigureState fs) { 
		myFigureState = fs;
	}
	public BonyConfigEmitter getBonyConfigEmitter() { 
		return myConfigEmitter;
	}
	public File getJointConfigFileForChar(String bonyCharURI) {
		return myConfigEmitter.getJointConfigFileForChar(bonyCharURI);
	}
	public Vector3f getConfigVector3f(String vectorURI) {
		float[] xyz = myConfigEmitter.getNamedFloatVector(vectorURI);
		return JmonkeyMathObjFactory.makeVector(xyz);
	}
}
