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
package org.cogchar.render.sys.goody;

import java.awt.Dimension;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.app.entity.VWorldEntityActionConsumer;
import org.cogchar.render.goody.basic.DataballGoodyBuilder;
import org.cogchar.render.goody.flat.GeneralScoreBoard;
import org.cogchar.render.gui.bony.VirtualCharacterPanel;
import org.cogchar.render.sys.module.ModularRenderContext;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.scene.Node;
import org.cogchar.render.goody.flat.ScoreBoard;
import org.cogchar.render.goody.physical.ProjectileLauncher;
import org.cogchar.render.goody.physical.GoodyPhysicsStuffBuilder;
import org.cogchar.render.sys.registry.RenderRegistryClient;
// import org.cogchar.render.sys.physics.PhysicsStuffBuilder;

/**
 *
 * @author Owner
 */
public class GoodyModularRenderContext extends BonyRenderContext {
	private		DataballGoodyBuilder			myBallBuilder;
	private		boolean							thisBallBuilderSet;
	private		VWorldEntityActionConsumer		myEntitySpace;						
	private		boolean							thisEntitySpaceSet;
	
	protected	GeneralScoreBoard				myScoreBoard;

	private GoodyGameFeatureAdapter		myGameFeatureAdapter;
	
	private Dimension myScreenDimension = new Dimension();
	private Dimension lastScreenDimension = new Dimension();
	
	public GoodyModularRenderContext(GoodyRenderRegistryClient grrc, RenderConfigEmitter rce) { 
		super(grrc, rce);
		myGameFeatureAdapter = new GoodyGameFeatureAdapter(this);
	}
	public GoodyGameFeatureAdapter getGameFeatureAdapter() {
		return myGameFeatureAdapter;
	}
	public GoodyRenderRegistryClient getGoodyRenderRegistryClient(){ 
		return (GoodyRenderRegistryClient) getRenderRegistryClient();
	}	
	@Override public void completeInit() {
		super.completeInit();
		initPhysicsStuffBuilder();
	}
	@Override public void postInitLaunch() {
		myGameFeatureAdapter.initFeatures();
	}	
	protected void initPhysicsStuffBuilder() {  
		PhysicsSpace ps = getPhysicsSpace();
		// TODO: Check config for initial debug setting
		Node rootNode = getRenderRegistryClient().getJme3RootDeepNode(null);					
		myPSB =  new GoodyPhysicsStuffBuilder(this, ps, rootNode);	
	}
	// Adding a method to manage locally stored BallBuilder instance. We could get it using BallBuilder.getBallBuilder
	// each time we need it, but that's once per update cycle. 
	public void setTheBallBuilder(DataballGoodyBuilder theBallBuilder) {
		myBallBuilder = theBallBuilder;
		if (myBallBuilder != null) {
			thisBallBuilderSet = true; // In theory, this variable allows a fast boolean check in doUpdate instead of having to check for null each update
		}
	}
	
	public void setTheEntitySpace(VWorldEntityActionConsumer theSpace) {
		myEntitySpace = theSpace;
		if (theSpace != null) {
			thisEntitySpaceSet = true; // In theory, this variable allows a fast boolean check in doUpdate instead of having to check for null each update
		}
	}	
	@Override public void doUpdate(float tpf) {
		// Update screen dimension for 2D Goodies:
		if (thisEntitySpaceSet) {
			VirtualCharacterPanel vcp = getPanel();
			if (vcp != null) {
				myScreenDimension = vcp.getSize(myScreenDimension);
				if (!myScreenDimension.equals(lastScreenDimension)) {
					myEntitySpace.applyNewScreenDimension(myScreenDimension);
					lastScreenDimension = (Dimension)myScreenDimension.clone();
				}
			}
		}
		
		if (thisBallBuilderSet) {myBallBuilder.applyUpdates(tpf);} // tpf is passed to BallBuilder only for debugging now; it may get used more broadly eventally or may be removed from the method
		
		super.doUpdate(tpf);
	}	
	public GeneralScoreBoard getScoreBoard() {
		return myScoreBoard;
	}

	public void setScoreBoard(GeneralScoreBoard scoreBoard) {
		this.myScoreBoard = scoreBoard;
	}

	public void initScoreBoard() {
		SimpleApplication app = getApp(); // Should be from registry, not this way
		AppSettings settings = app.getContext().getSettings(); // Should be from registry, not this way
		int numScoreRows = 4;
		int rowHeight = 50;
		int boardWidth = settings.getWidth();
		int baseX = 20;
		int baseY = settings.getHeight() - numScoreRows * rowHeight;
		float textSizeMult = 0.5f;
		ScoreBoard sb = new ScoreBoard(app.getAssetManager(), app.getGuiNode(), baseX, baseY, boardWidth, rowHeight, numScoreRows, textSizeMult);
		setScoreBoard(sb); // Goofy way to do it
	}	

}
