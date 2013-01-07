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

package org.cogchar.app.puma.cgchr;

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.cogchar.app.puma.boot.PumaContextCommandBox;
import org.cogchar.bind.cogbot.main.CogbotCommunicator;
import org.cogchar.bind.lift.LiftAmbassador;
import org.cogchar.render.model.databalls.BallBuilder;
import org.cogchar.render.opengl.scene.PathMgr;
import org.cogchar.render.opengl.scene.SpatialAnimMgr;
/**

/**
 * @author Stu B. <www.texpedient.com>
 */

class CommandTargetForUseFromWeb extends BasicDebugger implements LiftAmbassador.LiftAppInterface {
	private PumaContextCommandBox		myPCCB;
	private String						myCogbotConvoUrl;
	private CogbotCommunicator			myCogbotComm;
	private final PumaWebMapper			myWebMapper;

	public CommandTargetForUseFromWeb(PumaContextCommandBox pccb, final PumaWebMapper outer) {
		this.myWebMapper = outer;
		myPCCB = pccb;
	}

	@Override public boolean triggerAnimation(Ident uri) {
		boolean success = myPCCB.getPathMgr().controlAnimationByName(uri, PathMgr.ControlAction.PLAY);
		if (!success) {
			success = myPCCB.getThingAnimMgr().controlAnimationByName(uri, SpatialAnimMgr.ControlAction.PLAY);
		}
		return success;
	}

	@Override public boolean stopAnimation(Ident uri) {
		boolean success = myPCCB.getPathMgr().controlAnimationByName(uri, PathMgr.ControlAction.STOP);
		if (!success) {
			success = myPCCB.getThingAnimMgr().controlAnimationByName(uri, SpatialAnimMgr.ControlAction.STOP);
		}
		return success;
	}

	@Override public String queryCogbot(String query, String url) {
		if ((myCogbotComm == null) || (!url.equals(myCogbotConvoUrl))) {
			myCogbotConvoUrl = url;
			myCogbotComm = new CogbotCommunicator(myCogbotConvoUrl);
		}
		return myCogbotComm.getResponse(query).getResponse();
	}

	@Override public boolean performDataballAction(String action, String text) {
		return BallBuilder.getTheBallBuilder().performAction(action, text);
	}

	@Override public boolean performUpdate(String request) {
		boolean forceFreshDefaultRepo = false;
		boolean success = false;
		if (myPCCB != null) {
			success = myPCCB.updateConfigByRequest(request, forceFreshDefaultRepo);
		} else {
			getLogger().warn("Update requested, but PumaWebMapper cannot find PumaAppContext for RQ=" + request);
		}
		return success;
	}

}
