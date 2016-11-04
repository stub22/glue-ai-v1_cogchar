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

/**
 * @author Stu B. <www.texpedient.com>
 */
trait CellIndex {
	// This is the index of some cell in some *single* particular dimension.    The first cell is 0.
	// The index might be fixed or changing.
	def getIndexFrom0 : Int 
}
class SimpleCellIndex(val myIndexFrom0 : Int) extends CellIndex {
	// The simple case of a fixed cell index.
	override def getIndexFrom0 : Int  = myIndexFrom0
}
trait CellIndexRange {
	// In general, an index range is defined by a first + last index, which may be changing or fixed.
	// Invariant:  First <= Last
	def getFirstCellIndex : CellIndex
	def getLastCellIndex : CellIndex
	def getSizeFrom1 : Int = getLastCellIndex.getIndexFrom0 - getFirstCellIndex.getIndexFrom0 + 1; // (Last==First) -> size==1
	def describe(cellFrom : Int) : String = "[first=" + (getFirstCellIndex.getIndexFrom0 + cellFrom) +
				", size=" + + getSizeFrom1 + ", last=" + (getLastCellIndex.getIndexFrom0 + cellFrom) + "]"
}
class FixedCellIndexRange (val myFirstCellIndex : CellIndex, val myLastCellIndex : CellIndex) extends CellIndexRange {
	val len = getSizeFrom1
	if (len < 1) {
		throw new RuntimeException("Bad length " + len + " for range " + describe(0))
	}
	override def getFirstCellIndex : CellIndex = myFirstCellIndex
	override def getLastCellIndex : CellIndex = myLastCellIndex
}
class CellBlock(val myCIRs : Array[CellIndexRange]) extends KnowsOrthoDim {
	// A rectilinear uniform clump of cells, usually considered within some multi-D grid space.
	// However, by itself, the CellBlock does not define any metric concepts, only index concepts.
	// The number of ortho dimensions is implied by the length of the myCIRs array.
	// Each of the entries in that array has a CellIndexRange = (possibly changing) pair of cell indices.

	override def getOrthoDimCount() = myCIRs.length
	
	def describe(cellFrom : Int) : String = {
		val buffer = new StringBuffer("[cirs=[")
		for ( cir <- myCIRs) {
			buffer.append(cir.describe(cellFrom) + ", ")
		}
		buffer.append("]]")
		buffer.toString
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
	def makeRanges3D(firstX: Int, lastX : Int, firstY : Int, lastY : Int, firstZ : Int, lastZ : Int) : Array[CellIndexRange] = {
		val ranges = new Array[CellIndexRange](3)
		ranges(0) = makeCellRangeFrom1(firstX, lastX)
		ranges(1) = makeCellRangeFrom1(firstY, lastY)
		ranges(2) = makeCellRangeFrom1(firstZ, lastZ)
		ranges
	}	
	// All args are 1-Based
	def makeBlock2D(firstX: Int, lastX : Int, firstY : Int, lastY : Int) : CellBlock = {
		val cellIndexRanges = makeRanges2D(firstX, lastX, firstY, lastY)
		new CellBlock(cellIndexRanges)
	}
	// All args are 1-Based
	def makeBlock3D(firstX: Int, lastX : Int, firstY : Int, lastY : Int, firstZ : Int, lastZ : Int) : CellBlock = {
		val cellIndexRanges = makeRanges3D(firstX, lastX, firstY, lastY, firstZ, lastZ)
		new CellBlock(cellIndexRanges)
	}	
	// Facilitate making "unit blocks", of length = 1 in each dimension.
	// A unit block is fully specified by an array of cellIndices, i.e. integers,  an equivalent construct.
	// A unitBlock is really just a handle for an integer tuple, applicable within our cell/pos/grid concepts.
	// Such unit blocks are not yet specially typed from a Java-perspective, though they could be.	
	def makeUnitBlock(indicesFrom1 : Array[Int]) : CellBlock = {
		val dimCount = indicesFrom1.length
		val ranges = new Array[CellIndexRange](dimCount)
		for (d <- 0 to (dimCount - 1)) {
			val cellIndex = indicesFrom1(d)
			ranges(d) = makeCellRangeFrom1(cellIndex, cellIndex)
		}
		new CellBlock(ranges)
	}

	
	def makeUnitBlock2D(xFrom1 : Int, yFrom1 : Int) : CellBlock = makeUnitBlock(Array(xFrom1, yFrom1))
	def makeUnitBlock3D(xFrom1 : Int, yFrom1 : Int, zFrom1 : Int) : CellBlock = makeUnitBlock(Array(xFrom1, yFrom1, zFrom1))
	
}