/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.render.goody.flat;

import org.cogchar.render.app.entity.GoodyAction;
import org.cogchar.name.goody.GoodyNames;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.render.app.entity.VWorldEntity;
import org.cogchar.render.sys.registry.RenderRegistryClient;
import org.cogchar.render.sys.goody.GoodyRenderRegistryClient;

/**
 * A goody to create the Cogchar ScoreBoard. Intended to completely replace original
 * org.cogchar.render.sys.physics.ScoreBoard, which can be removed entirely when no longer needed
 * for demo bundles
 * 
 * This is a "composite goody" which spawns an instance of BasicGoody2dImpl for each row of the scoreboard.
 * 
 * If we decide we need the original ScoreBoard class in the long run after all, this can be refactored as an extension
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */

// May eventually extend a standard "composite goody" superclass
// Presents a set of lines containing "labels" and "scores".
public class ScoreBoardGoody extends VWorldEntity implements GeneralScoreBoard {
	
	final static ColorRGBA MY_SCORE_COLOR = ColorRGBA.Magenta; // Likely only temporarily a constant
	
	int	myScreenWidth, myScreenHeight; // in pixels
	float myRowHeight; // as a fraction of window height
	Vector3f myPosition; // of top left corner of scoreboard in fraction of window width/height
	List<ScoreBoardGoody.Row>	myRows;

	@Override
	public void setRotation(Quaternion newRotation) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public class Row extends BasicGoody2dImpl {
		public Row(GoodyRenderRegistryClient aRenderRegCli, Ident uri, Vector3f rowPosition,	float textSize, ColorRGBA scoreColor) {
			super(aRenderRegCli, uri);
			//myLogger.info("In ScoreBoardGoody.Row, Window size is {}x{}, position is {}", new Object[]{myScreenWidth, myScreenHeight, rowPosition}); // TEST ONLY
			this.setPosition(rowPosition);
			setGoodyAttributes("_", textSize, scoreColor);
		}
		public void setScoreText(String scoreText) {
			setText(scoreText);
		}
	}
	public ScoreBoardGoody(GoodyRenderRegistryClient aRenderRegCli, Ident uri, Vector3f topPosition, 
				float rowHeight, int numRows, float textSize) {
		myRenderRegCli = aRenderRegCli;
		myUri = uri;
		myRows = new ArrayList<ScoreBoardGoody.Row>();
		myRowHeight = rowHeight;
		myPosition = topPosition;
		String baseUriString = uri.getAbsUriString();
		for (int rowIdx=0; rowIdx < numRows; rowIdx++) {
			Ident rowIdent = new FreeIdent(baseUriString + "Row" + rowIdx);
			ScoreBoardGoody.Row aLine = new ScoreBoardGoody.Row(aRenderRegCli, rowIdent, 
					getPositionForRow(rowIdx, topPosition), textSize, MY_SCORE_COLOR);
			myRows.add(aLine);
			aLine.setScoreText("line_" + rowIdx);
		}
		//BonyRenderContext.setScoreBoard(this); //needs to happen somewhere, probably not here!
	}
	@Override
	public void displayScore(int rowNum, String scoreText) {
		if ((rowNum >= 0) && (rowNum < myRows.size())) {
			ScoreBoardGoody.Row l = myRows.get(rowNum);
			l.setScoreText(scoreText);
		} else {
			myLogger.warn("A request was made to set text for Scoreboard row #{}, but that row does not exist!", rowNum);
		}
	}
	
	@Override
	public void attachToVirtualWorldNode(Node vWorldNode) {
		// Currently any specified node is ignored since we are attaching via the FlatOverlayMgr
		attachToVirtualWorldNode();
	}
	// For this composite goody, we must attach all rows
	public void attachToVirtualWorldNode() {
		if (!myRows.isEmpty()) {
			for (Row nextRow : myRows) {
				nextRow.attachToVirtualWorldNode();
			}
		} else {
			myLogger.warn("Attempting to attach scoreboard to virtual world, but it has no rows!");
		}
	}
	
	@Override
	public void detachFromVirtualWorldNode() {
		for (Row nextRow : myRows) {
			nextRow.detachFromVirtualWorldNode();
		}
	}
	
	@Override
	public void applyAction(GoodyAction ga) {
		switch (ga.getKind()) {
			case MOVE : {
				setPosition(ga.getLocationVector());
				break;
			}
			case SET : {
				String scoreText = ga.getSpecialString(GoodyNames.TEXT);
				int rowNum = 0;
				try {
					rowNum = Integer.valueOf(ga.getSpecialString(GoodyNames.SUBCOMPONENT));
				} catch (Exception e) {
					myLogger.error("Row (subcomponent) number not recognized for setting scoreboard, assuming 0");
				}
				displayScore(rowNum, scoreText);
				break;
			}
			default: {
				myLogger.error("Unknown action requested in Goody {}: {}", myUri.getLocalName(), ga.getKind().name());
			}
		}
	};
	
	@Override
	public void setPosition(Vector3f position) {
		for (int rowIdx=0; rowIdx < myRows.size(); rowIdx++) {
			Row nextRow = myRows.get(rowIdx);
			nextRow.setPosition(getPositionForRow(rowIdx, position));
		}
		myPosition = position;
	}
	
	@Override
	public void setUniformScaleFactor(Float scale) {
		myLogger.warn("Setting scale not currently implemented for the ScoreBoardGoody, coming soon...");
		/*// This throws a java.lang.IllegalArgumentException deep in jME's LWJGL bits for some reason
		// Will fix this soon, not very necessary for now and holding up a big commit...
		if (scale != null) {
			for (Row nextRow : myRows) {
				nextRow.setScale(scale);
			}
		}
		*/
	}
	
	private Vector3f getPositionForRow(int row, Vector3f scoreboardPosition) {
		float leftX = scoreboardPosition.getX();
		float topY = scoreboardPosition.getY() - row * myRowHeight;
		return new Vector3f(leftX, topY, 0);
	}
}