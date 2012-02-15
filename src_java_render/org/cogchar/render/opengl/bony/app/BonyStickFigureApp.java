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

package org.cogchar.render.opengl.bony.app;

import com.jme3.animation.AnimControl;
import com.jme3.scene.Node;
import java.util.List;
import org.cogchar.render.opengl.bony.model.SpatialManipFuncs;
import org.cogchar.render.opengl.bony.model.StickFigureTwister;

import org.cogchar.blob.emit.BonyConfigEmitter;
import org.cogchar.render.opengl.bony.gui.VirtualCharacterPanel;
import org.cogchar.render.opengl.bony.sys.BonyRenderContext;
import org.cogchar.render.opengl.bony.sys.JmonkeyAssetLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author Stu B. <www.texpedient.com>
 */
public abstract class BonyStickFigureApp<BRCT extends BonyRenderContext> extends BonyVirtualCharApp<BRCT> {
	static Logger theLogger = LoggerFactory.getLogger(BonyStickFigureApp.class);

	
	public BonyStickFigureApp(BonyConfigEmitter bce) { 
		super(bce); 
	}

}
