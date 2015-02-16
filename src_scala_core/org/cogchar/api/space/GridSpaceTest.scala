/*
 *  Copyright 2015 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.api.space
import org.appdapter.core.log.BasicDebugger;
/**
 * @author Stu B. <www.texpedient.com>
 */

object GridSpaceTest extends BasicDebugger {
	def main(args : Array[String]) : Unit = {
		goGoGo
	}
	def goGoGo : Unit = {
		getLogger().info("Hello Dear User!  We will now go()")
		
		// This block from x=3,y=-1 to x=5,y=6 extends "beyond" its implied containing cell space, which starts at x=1,y=1
		val cellBlock = CellRangeFactory.makeBlock2D(3, 5, -1, 6)
		getLogger().info("CellBlock description={}", cellBlock.describe(1)) // cellFrom == 1 -> base-1 labelling

		val space2D : MultiDimGridSpace = GridSpaceFactory.makeSpace2D(5, 80.0f, 120.0f, 7, -20.0f, 15.0f)
		getLogger().info("2D Space description={}", space2D.describe()) // cellFrom == 1 -> base-1 labelling

		val posBlock = space2D.computePosBlockForCellBlock(cellBlock);
		getLogger().info("Computed result PosBlock description={}", posBlock.describe)
		val vecOnDiag = posBlock.getVecFromMainDiagonal(2.0f)
		getLogger().info("Vec on pos-block diag at 2.0f * MAX ={}", vecOnDiag)
		
		val vecAtMin = posBlock.getVecFromMainDiagonal(0.0f)
		getLogger().info("Vec on pos-block diag at 0.0f * MAX ={}", vecAtMin)		
		
		val blockAt729 = CellRangeFactory.makeUnitBlock3D(7, 2, 9)
		getLogger().info("3D unit block at 7,2,9 description={}", blockAt729.describe(1))
		
		val space3D : MultiDimGridSpace = GridSpaceFactory.makeSpace3D(7, -40.0f, 40.0f, 5, -20.0f, 20.0f, 9, -50.0f, 20.0f);
		getLogger().info("3D Space description={}", space2D.describe()) // cellFrom == 1 -> base-1 labelling
		
		getLogger().info("We are now done go()ne.  Goodbye Dear User!")

	}
	
}