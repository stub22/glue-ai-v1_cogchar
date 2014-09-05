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



trait PosRange {
	// A range of 1D space with a min and max value.  min <= max, numerically.   Min and Max might be changing, or fixed.
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
	// The simple case of a range in 1D space with fixed min and max.  min <= max.
	if (myMinPos > myMaxPos) {
		throw new RuntimeException("Cannot construct FixedPosRange with min " + myMinPos + " > max " + myMaxPos)
	}
	override def  getLength : Float = myMaxPos - myMinPos;
	override def getCenter : Float = myMinPos + getLength / 2.0f;
	def getMin : Float = myMinPos
	def getMax : Float = myMaxPos
}
// A rectilinear piece of physical-ish ("real") metric space, defined by an array of position ranges.
// Each PosRange defines the extent of this block in one dimension.
// By itself, this PosBlock does not define any Cells, it is purely a metric space concept.
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
	// Calculates an array of positions (one for each dimension), on the main diagonal line of the pos block
	// (the line running from min-pos in all dimensions to max-pos in all dimensions).
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