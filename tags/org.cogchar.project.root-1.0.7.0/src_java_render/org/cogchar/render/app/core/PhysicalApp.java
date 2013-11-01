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
package org.cogchar.render.app.core;

import org.cogchar.render.sys.context.PhysicalModularRenderContext;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.render.sys.physics.PhysicsStuffBuilder;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * 		// Comment in PhysicsSpace says to setGravity "before creating physics objects".
		// ps.setGravity(Vector3f.ZERO);
		// Turn on the blue wireframe collision bounds.
		// ps.enableDebug(asstMgr);
 */
public abstract class PhysicalApp<PMRCT extends PhysicalModularRenderContext> extends CogcharPresumedApp<PMRCT> {
	
	public PhysicalApp(RenderConfigEmitter rce) {
		super(rce);
	}

}
