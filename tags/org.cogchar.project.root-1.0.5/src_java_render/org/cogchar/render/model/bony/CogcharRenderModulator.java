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
package org.cogchar.render.model.bony;

import org.appdapter.module.basic.BasicModulator;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class CogcharRenderModulator extends BasicModulator<CogcharRenderModulator> {

	private		float			myCurrentTimePerFrame;
	
	public CogcharRenderModulator() { 
		super(null, true);
		setDefaultContext(this);
	}
	public float getCurrentTimePerFrame() { 
		return myCurrentTimePerFrame;
	}	
	public synchronized void runOneCycle(float tpf) {
		myCurrentTimePerFrame = tpf;
		processOneBatch();
	}

}
