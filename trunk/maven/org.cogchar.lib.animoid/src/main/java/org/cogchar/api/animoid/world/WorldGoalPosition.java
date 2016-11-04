/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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


package org.cogchar.api.animoid.world;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class WorldGoalPosition {
	public Double		degrees;
	public Double		deltaDegrees;
	public WorldGoalPosition(double deg, double deltaDeg) {
		degrees = deg;
		deltaDegrees = deltaDeg;
	}
	public WorldGoalPosition makeAdjustedPosition(double changeDeg) {
		return new WorldGoalPosition(degrees + changeDeg, deltaDegrees + changeDeg);
	}
	@Override public String toString() {
		return "WorldGoalPosition[deg=" + degrees + ", deltaDeg=" + deltaDegrees + "]";
	}
}
