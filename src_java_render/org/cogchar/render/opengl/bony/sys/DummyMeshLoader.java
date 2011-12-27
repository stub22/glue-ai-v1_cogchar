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
package org.cogchar.render.opengl.bony.sys;

import com.jme3.scene.plugins.ogre.MeshLoader;
import com.jme3.asset.AssetInfo;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class DummyMeshLoader extends MeshLoader {
	static Logger theLogger = LoggerFactory.getLogger(DummyMeshLoader.class);
	
    public DummyMeshLoader() {
        super();
		theLogger.info("=============================== DummyMeshLoader constructing");
    }
	@Override public Object load(AssetInfo info) throws IOException {
		theLogger.info("============================== DummyMeshLoader: " + info);
		return super.load(info);
	}
}
