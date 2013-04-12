/*
 *  Copyright 2011-2 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.bind.rk.robot.client;

import java.net.URL;
import org.appdapter.core.name.Ident;
import org.cogchar.api.perform.Media;
import org.cogchar.api.perform.Performance;
import org.cogchar.impl.perform.FancyTextPerf;
import org.cogchar.impl.perform.FancyTextChan;
import org.cogchar.impl.perform.FancyTime;
import org.cogchar.platform.util.ClassLoaderUtils;
import org.robokind.api.animation.Animation;

/**
 *
 * @author Owner
 */


public class AnimOutTrigChan extends FancyTextChan {
	private boolean myUseTempAnimsFlag = false;
	private RobotAnimContext myRobotAnimContext;

	public AnimOutTrigChan(Ident id, RobotAnimContext rac) {
		super(id);
		myRobotAnimContext = rac;
	}

	public void setUseTempAnims(boolean flag) {
		myUseTempAnimsFlag = flag;
	}

	@Override protected void attemptMediaStartNow(Media.Text m) throws Throwable {
		String animPathStr = m.getFullText();
		Animation anim = null;
		URL animResURL = ClassLoaderUtils.findResourceURL(animPathStr, myRobotAnimContext.myResourceCLs);
		if (animResURL != null) {
			getLogger().info("Found Animation Resource URL: " + animResURL);
			String aruString = animResURL.toExternalForm();
			anim = myRobotAnimContext.myAnimClient.readAnimationFromURL(aruString);
		} else {
			getLogger().warn("Cannot locate animMediaFile {} in classpath {}, now checking local files", animPathStr, myRobotAnimContext.myResourceCLs);
			String fullPath = null;
			// Temporarily we always use the temp path, because it's just a file and we don't have to turn
			// the resource lookup into a URL.
			//if (myUseTempAnimsFlag) {
			
			fullPath = myRobotAnimContext.myBehaviorCE.getRKAnimationTempFilePath(animPathStr);
			//} else {
			//	fullPath = myBehaviorCE.getRKAnimationPermPath(animPathStr);
			//}
			getLogger().info("Attempting to read animation from relative file path[" + fullPath + "]");
			anim = myRobotAnimContext.myAnimClient.readAnimationFromFile(fullPath);
		}
		if (anim != null) {
			myRobotAnimContext.startFullAnimationNow(anim);
		}
	}

	@Override
	public Performance<Media.Text, FancyTime> makePerformanceForMedia(Media.Text m) {
		return new FancyTextPerf(m, this);
	}
	
}
