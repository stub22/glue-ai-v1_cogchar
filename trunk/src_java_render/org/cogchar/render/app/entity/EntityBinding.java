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

package org.cogchar.render.app.entity;

import java.util.concurrent.Callable;
import org.cogchar.render.sys.task.Queuer;

/**
 * @author Stu B. <www.texpedient.com>
 */

public abstract class EntityBinding {
	private	Queuer	myQueuer;
	
	public EntityBinding(Queuer q) { 
		myQueuer = q;
	}
	protected void applyLocation(Queuer.QueueingStyle qStyle) {
		Callable locMutator = makeLocationMutator();
		if (locMutator != null) {
			myQueuer.enqueueForJme(locMutator, qStyle);
		}
	}
	
	protected void applyDirection(Queuer.QueueingStyle qStyle) {
	}
	
	protected void applySizes(Queuer.QueueingStyle qStyle) {
	}
	
	protected void applyColors(Queuer.QueueingStyle qStyle) {
	}
	
	protected Callable makeLocationMutator() {
		return new Callable() {
			@Override public Void call() throws Exception {	
				// n.setLocalTranslation(myX, myY, myZOrder);
				return null;
			}
					
		};
	}
	
}
