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

package org.cogchar.api.space

/**
 * @author Stu B. <www.texpedient.com>
 */

trait Field {
}
trait Real extends Field {
}

class Vector3[Component <: Field] (val x: Component, val y: Component, val z:Component) {
}
class Vector3R(x: Real, y: Real, z:Real) extends Vector3[Real](x,y,z) {
}
trait VectorOperator[VSpace] {
	def apply(in: VSpace) : VSpace;
}
trait VectorOp3R extends VectorOperator[Vector3R] {
}
trait LinearOp3R extends VectorOp3R {
}
trait RotationOp3R extends LinearOp3R {
}
abstract class Rotation_YZX_Op3R(val xRot_3rd : Double, val yRot_1st : Double, val zRot_2nd: Double) extends RotationOp3R {
}
abstract class TranslationOp3R(val xTrans : Double, val yTrans : Double, val zTrans : Double) extends LinearOp3R  {
}
abstract class ScaleOp3R (val xScale : Double, val yScale : Double, val zScale : Double) extends LinearOp3R {
}
class CompoundLinearOp3R(val opList : List[LinearOp3R]) extends LinearOp3R {
	def apply(in: Vector3R) : Vector3R = {
		null; // need foldLeft or somethin
	}
}

