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
 * It is embedded in some real (float-computed or more sophisticated) R^n space.
 * @author Stu B. <www.texpedient.com>
 */

trait PosVec {
	
}

trait PosRange {
	val	POS_FRAC_MIN = 0.0f
	val POS_FRAC_CENTER = 0.5f
	val POS_FRAC_MAX = 1.0f;
	def  getLength : Float
	def getCenter : Float
	def getMin : Float
	def getMax : Float
	// frac==0.0 -> min, frac==0.5 -> center, frac==1.0 -> max  --- but extension of the line also works.
	def getFracPos (frac : Float) : Float = getMin + frac * getLength
	def describe : String = "[min=" + getMin + ", cen=" + getCenter + ", len=" + getLength + ", max=" + getMax + "]"
}
class FixedPosRange(val myMinPos : Float, val myMaxPos : Float) extends PosRange {
	override def  getLength : Float = myMaxPos - myMinPos;
	override def getCenter : Float = myMinPos + getLength / 2.0f;
	def getMin : Float = myMinPos
	def getMax : Float = myMaxPos
}
// This is the index of some cell in some *single* particular dimension
trait CellIndex {
	def getIndexFrom0 : Int 
}
class SimpleCellIndex(val myIndexFrom0 : Int) extends CellIndex {
	override def getIndexFrom0 : Int  = myIndexFrom0
}
trait CellIndexRange {
	def getFirstCellIndex : CellIndex
	def getLastCellIndex : CellIndex
	def getSizeFrom1 : Int = getLastCellIndex.getIndexFrom0 - getFirstCellIndex.getIndexFrom0 + 1; // (Last==First) -> size==1
	def describe(cellFrom : Int) : String = "[first=" + (getFirstCellIndex.getIndexFrom0 + cellFrom) +
				", size=" + + getSizeFrom1 + ", last=" + (getLastCellIndex.getIndexFrom0 + cellFrom) + "]"
}
class FixedCellIndexRange (val myFirstCellIndex : CellIndex, val myLastCellIndex : CellIndex) extends CellIndexRange {
	override def getFirstCellIndex : CellIndex = myFirstCellIndex
	override def getLastCellIndex : CellIndex = myLastCellIndex
}
trait GridSpaceDim {
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
	override def	getLength : Float = myFullPosRange.getLength;
	override def	getFullPosRange : PosRange = myFullPosRange
	def		getCellCount : Int = myCellCount;
}

trait KnowsOrthoDim {
	def		getOrthoDimCount	: Int
}
trait MultiDimGridSpace extends KnowsOrthoDim {
	def getDim(indexFrom0 : Int) : GridSpaceDim
	def describe() : String 
}
class FixedMultiDimGridSpace(val myDims : Array[GridSpaceDim]) extends MultiDimGridSpace {
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
// A rectilinear uniform clump of cells
class CellBlock(val myCIRs : Array[CellIndexRange]) extends KnowsOrthoDim {
	override def getOrthoDimCount() = myCIRs.length
	def computePosBlockInSpace(mdgs : MultiDimGridSpace) : PosBlock = {
		val dimCount = getOrthoDimCount
		val resArr = new Array[PosRange](dimCount)
		for (d <- 0 to (dimCount - 1)) {
			val cir = myCIRs(d)
			val gsd = mdgs.getDim(d)
			resArr(d) = gsd.calcPosRange(cir)
		}
		new PosBlock(resArr)
	}
	
	def getSubCellBlock(indices : Array[Int]) = {
		
	}
	def describe(cellFrom : Int) : String = {
		val buffer = new StringBuffer("[cirs=[")
		for ( cir <- myCIRs) {
			buffer.append(cir.describe(cellFrom) + ", ")
		}
		buffer.append("]]")
		buffer.toString
	}	
}
// A rectilinear piece of physical-ish ("real") space
class PosBlock(val myPRs : Array[PosRange]) extends KnowsOrthoDim {
	override def getOrthoDimCount() = myPRs.length
	def describe() : String = {
		val buffer = new StringBuffer("[")
		for ( pr <- myPRs) {
			buffer.append(pr.describe + ", ")
		}
		buffer.append("]")
		buffer.toString
	}
	//0.0 -> min, 0.5 -> center, 1.0 -> max
	def getVecFromMainDiagonal(p: Float) : Array[Float] = {
		val dimCount = getOrthoDimCount
		val res = new Array[Float](dimCount)
		for (d <- 0 to (dimCount - 1)) {
			val posRange : PosRange = myPRs(d)
			val fracPos : Float = posRange.getFracPos(p)
			res(d) = fracPos
		}
		res
	}
}


object CellSpaceFactory {
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
} 
object CellRangeFactory {
	
	def makeCellRangeFrom1(firstCellIndexFrom1 : Int, lastCellIndexFrom1 : Int) : CellIndexRange = {
		val firstCellIndex = new SimpleCellIndex(firstCellIndexFrom1 - 1)
		val lastCellIndex = new SimpleCellIndex(lastCellIndexFrom1 -1)
		new FixedCellIndexRange(firstCellIndex, lastCellIndex)
	}
	// All args are 1-Based
	def makeRanges2D(firstX: Int, lastX : Int, firstY : Int, lastY : Int ) : Array[CellIndexRange] = {
		val ranges = new Array[CellIndexRange](2)
		ranges(0) = makeCellRangeFrom1(firstX, lastX)
		ranges(1) = makeCellRangeFrom1(firstY, lastY)
		ranges
	}
	// All args are 1-Based
	def makeBlock2D(firstX: Int, lastX : Int, firstY : Int, lastY : Int) : CellBlock = {
		val cellIndexRanges = makeRanges2D(firstX, lastX, firstY, lastY)
		new CellBlock(cellIndexRanges)
	}
	def makeUnitBlock(indicesFrom1 : Array[Int]) : CellBlock = {
		val dimCount = indicesFrom1.length
		val ranges = new Array[CellIndexRange](dimCount)
		for (d <- 0 to (dimCount - 1)) {
			val cellIndex = indicesFrom1(d)
			ranges(d) = makeCellRangeFrom1(cellIndex, cellIndex)
		}
		new CellBlock(ranges)
	}
	// Facilitate making blocks of "just one cell" (in each dimension)
	def makeUnitBlock2D(xFrom1 : Int, yFrom1 : Int) : CellBlock = makeUnitBlock(Array(xFrom1, yFrom1))
	def makeUnitBlock3D(xFrom1 : Int, yFrom1 : Int, zFrom1 : Int) : CellBlock = makeUnitBlock(Array(xFrom1, yFrom1, zFrom1))
	
}

object CellSpaceTest extends BasicDebugger {


	def go : Unit = {
		getLogger().info("Hello Dear User!")
		
		// This block from x=3,y=-1 to x=5,y=6 extends "beyond" its implied containing cell space, which starts at x=1,y=1
		val cellBlock = CellRangeFactory.makeBlock2D(3, 5, -1, 6)
		getLogger().info("CellBlock description={}", cellBlock.describe(1)) // cellFrom == 1 -> base-1 labelling

		val space = CellSpaceFactory.makeSpace2D(5, 80.0f, 120.0f, 7, -20.0f, 15.0f)
		getLogger().info("Space description={}", space.describe()) // cellFrom == 1 -> base-1 labelling

		val posBlock = cellBlock.computePosBlockInSpace(space)
		getLogger().info("Computed result PosBlock description={}", posBlock.describe)
		val vecOnDiag = posBlock.getVecFromMainDiagonal(2.0f)
		getLogger().info("Vec on pos-block diag at 2.0f * MAX ={}", vecOnDiag)
		
		val vecAtMin = posBlock.getVecFromMainDiagonal(0.0f)
		getLogger().info("Vec on pos-block diag at 0.0f * MAX ={}", vecAtMin)		
		
		val blockAt729 = CellRangeFactory.makeUnitBlock3D(7, 2, 9)
		getLogger().info("blockAt729 description={}", blockAt729.describe(1))
		
	}
	
}