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

package org.cogchar.render.test;

import org.cogchar.api.space.MultiDimGridSpace;
import org.cogchar.api.space.GridSpaceFactory;
import org.cogchar.api.space.CellBlock;
import org.cogchar.api.space.CellRangeFactory;
import org.cogchar.api.space.PosBlock;

import com.jme3.scene.Geometry;

import org.appdapter.core.log.BasicDebugger;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class TrialNexus extends BasicDebugger {
	public void makeSheetspace() {
		
		MultiDimGridSpace deepSpace = GridSpaceFactory.makeSpace3D(7, -40.0f, 40.0f, 5, -20.0f, 20.0f, 9, -50.0f, 20.0f);

		getLogger().info("Space description={}", deepSpace.describe()); // cellFrom == 1 -> base-1 labelling
		CellBlock extrudedCellBlock = CellRangeFactory.makeBlock3D(3, 5, -1, 6, 2, 7);
		PosBlock extrudedPosBlock = deepSpace.computePosBlockForCellBlock(extrudedCellBlock);
		getLogger().info("Computed result PosBlock description={}", extrudedPosBlock.describe());	
	}	
}
