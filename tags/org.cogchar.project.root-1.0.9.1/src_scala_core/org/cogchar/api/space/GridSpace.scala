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

package org.cogchar.api.space

import org.appdapter.core.log.BasicDebugger;

/**
 * A grid space is a rectilinear array of cells, usually in 2 or 3 dimensions.
 * It is embedded in some real (float-valued, or more numerically sophisticated) R^n space.
 * @author Stu B. <www.texpedient.com>
 */



// The "GridSpace" combines the ideas of the CellRange indexing with the PosRange metric positions+lengths.
trait GridSpaceDim {
	// Each dimension of some grid space is defined by its metric length, position, and cell count, which may be fixed or changing.
	def		getLength : Float
	def		getCellCount : Int 
	def		getFullPosRange : PosRange 
	def		getLengthPerCell : Float = getLength / getCellCount ;
	
	def calcPosRange (cellRange : CellIndexRange) : PosRange = {
		val lenPerCell = getLengthPerCell
		val firstCellMinPos = getFullPosRange.getMin + cellRange.getFirstCellIndex.getIndexFrom0 * lenPerCell;
		val lastCellMaxPos = getFullPosRange.getMin + (cellRange.getLastCellIndex.getIndexFrom0 + 1) * lenPerCell;
		new FixedPosRange(firstCellMinPos, lastCellMaxPos);
	}
	def describe() : String = "[count=" + getCellCount + ", length=" + getLength + ", fullPosRange=" + 
			getFullPosRange.describe + "]"

}


class FixedGridSpaceDim(val myCellCount : Int, val myFullPosRange : PosRange) extends GridSpaceDim {
	// In this simple case, the number of cells and the metric length + position are immutable constructor parameters.
	override def	getLength : Float = myFullPosRange.getLength;
	override def	getFullPosRange : PosRange = myFullPosRange
	def		getCellCount : Int = myCellCount;
}


trait MultiDimGridSpace extends KnowsOrthoDim {
	// A multi-dimensional grid, which in principle could dynamically change shape, dimension, position, ...
	def getDim(indexFrom0 : Int) : GridSpaceDim
	def describe() : String 
	// Given some arbitrary grid space, compute the position range of this cell block in that grid space.
	// Our cells do not need to be "inside" the grid space, however the size of our cells is presumed to
	// be the same as what is in the grid space, and our position is determined by the obvious extension
	// of the source grid space to the index range of our space.
	def computePosBlockForCellBlock(cb : CellBlock) : PosBlock = {

		assertSameDim(cb)
		
		val dimCount = getOrthoDimCount
		val resArr = new Array[PosRange](dimCount)
		for (d <- 0 to (dimCount - 1)) {
			val cir = cb.myCIRs(d)
			val gsd : GridSpaceDim = getDim(d)
			resArr(d) = gsd.calcPosRange(cir)
		}
		new PosBlock(resArr)
	}	
}


class FixedMultiDimGridSpace(val myDims : Array[GridSpaceDim]) extends MultiDimGridSpace {
	// A multiD grid with a fixed set of dimensions, although those dimensions may in principle be dynamically changing.
	override def getOrthoDimCount() = myDims.length
	override def getDim(indexFrom0 : Int) : GridSpaceDim = myDims(indexFrom0)
	override def describe() : String = {
		val buffer = new StringBuffer("[dims=[")	
		for ( dim <- myDims) {
			buffer.append(dim.describe + ", ")
		}
		buffer.append("]")
		buffer.toString
	}
}

object GridSpaceFactory {
	// Make 
	def makeSpaceDim(cellCount : Int, minPos : Float, maxPos : Float ) : GridSpaceDim = {
		val posRange = new FixedPosRange(minPos, maxPos);
		new FixedGridSpaceDim(cellCount, posRange)
	}
	def makeCenteredSpaceDim(cellCount : Int, centerPos : Float, length : Float ) : GridSpaceDim = {
		makeSpaceDim(cellCount, centerPos - length / 2, centerPos + length /2);
	}	
	def makeSpace2D(cellCountX : Int, minPosX : Float, maxPosX : Float, cellCountY : Int, minPosY : Float, maxPosY : Float) : MultiDimGridSpace = {
		val dims = new Array[GridSpaceDim](2)
		dims(0) = makeSpaceDim(cellCountX, minPosX, maxPosX)
		dims(1) = makeSpaceDim(cellCountY, minPosY, maxPosY)
		val space = new FixedMultiDimGridSpace(dims)
		space
	}
	def makeSpace3D(cellCountX : Int, minPosX : Float, maxPosX : Float, cellCountY : Int, minPosY : Float, maxPosY : Float,
				cellCountZ : Int, minPosZ : Float, maxPosZ : Float) : MultiDimGridSpace = {
		val dims = new Array[GridSpaceDim](3)
		dims(0) = makeSpaceDim(cellCountX, minPosX, maxPosX)
		dims(1) = makeSpaceDim(cellCountY, minPosY, maxPosY)
		dims(2) = makeSpaceDim(cellCountZ, minPosZ, maxPosZ)
		val space = new FixedMultiDimGridSpace(dims)
		space
	}	
} 

object GridSpaceTest extends BasicDebugger {

	def go : Unit = {
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
