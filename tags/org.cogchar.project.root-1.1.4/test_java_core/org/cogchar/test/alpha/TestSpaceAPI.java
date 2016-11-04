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

package org.cogchar.test.alpha;
import org.cogchar.api.space.*;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class TestSpaceAPI {
	static org.slf4j.Logger theLogger = LoggerFactory.getLogger(TestSpaceAPI.class);

	public static void main(String args[]) {
		org.apache.log4j.BasicConfigurator.configure();
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
		// This scala test object is defined in GridSpace.scala, in this same maven project.
		org.cogchar.api.space.GridSpaceTest.goGoGo();  // compiles and runs fine despite Netbeans highlighter complaint.
		
		wozers();
	}
	public static void wozers() {	
		MultiDimGridSpace space = GridSpaceFactory.makeSpace2D(5, 80.0f, 120.0f, 7, -20.0f, 15.0f);

		theLogger.info("Space description={}", space.describe()); // cellFrom == 1 -> base-1 labelling
		CellBlock cellBlock = CellRangeFactory.makeBlock2D(3, 5, -1, 6);
		PosBlock posBlock = space.computePosBlockForCellBlock(cellBlock);
		theLogger.info("Computed result PosBlock description[xBlock,yBlock]={}", posBlock.describe());
	}
}	
