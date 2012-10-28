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

package org.cogchar.app.puma.boot;
import org.cogchar.blob.emit.RenderConfigEmitter;
import org.cogchar.platform.trigger.CogcharScreenBox;
import org.cogchar.render.app.bony.BonyGameFeatureAdapter;
import org.cogchar.render.app.bony.BonyRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.model.humanoid.HumanoidFigure;
import org.cogchar.render.model.humanoid.HumanoidFigureManager;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class PumaContextCommandBox extends CogcharScreenBox {
	private		PumaAppContext		myPAC;
	
	public PumaContextCommandBox(PumaAppContext pac) {
		myPAC = pac;
	}
	protected HumanoidRenderContext getHRC() { 
		return myPAC.getOrMakeVWorldMapper().getHumanoidRenderContext();
	}
	public BonyGameFeatureAdapter getGameFeatureAdapter() { 
		return getHRC().getGameFeatureAdapter();
	}
	public HumanoidFigureManager getFigureManager() { 
		return getHRC().getHumanoidFigureManager();
	}
	public void resetMainCameraLocation() { 
		getHRC().setDefaultCameraLocation();
	}
	public void updateConfigByRequest(String request, final boolean resetMainConfigFlag) {
		myPAC.updateConfigByRequest(request, resetMainConfigFlag);
	}
	public HumanoidFigure getSinbad() { 
		BonyRenderContext brc = getHRC();
		RenderConfigEmitter bce = brc.getConfigEmitter();
		HumanoidFigureManager hfm = getFigureManager();
		return hfm.getHumanoidFigure(bce.SINBAD_CHAR_IDENT());
	}	
	
}
