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
package org.cogchar.render.opengl.bony.model;

import org.cogchar.render.opengl.bony.state.FigureState;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class HumanoidFigureModule extends RenderModule {
	private HumanoidFigure	myFigure;
	
	private BonyRenderContext  myBRC;
	
	public HumanoidFigureModule(HumanoidFigure hw, BonyRenderContext brc) {
		myFigure = hw;
		myBRC = brc;
		myRunDebugModulus = 100;
	}
	
	@Override protected void doRenderCycle(long runSeqNum, float tpf) {
		FigureState fs = myBRC.getFigureState();
		myFigure.applyFigureState(fs);
	}
}
