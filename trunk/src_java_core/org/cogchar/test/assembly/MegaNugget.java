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
package org.cogchar.test.assembly;
import java.util.List;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class MegaNugget extends Nugget {
	public	String		myGaucho;
	public	Long		myCount;
	public	Double		myAngle;
	public	List<Object>	myOtherNugs;
	public	List<Object>	myTriggers;
	public	Nugget			myFriendlyNug;
	// public	List
	
	@Override protected String getFieldSummary() {
		return super.getFieldSummary() + ", gaucho=[" + myGaucho + "], count=[" + myCount + "], angle=[" + myAngle 
					+ "], otherNugs=[" + myOtherNugs + "], triggers=[" + myTriggers + "], friendlyNug=[" + myFriendlyNug + "]";
	}		 
}
