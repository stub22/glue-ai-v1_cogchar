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
import org.cogchar.api.perform.BasicPerformance;
import org.cogchar.api.perform.Media;
import org.cogchar.impl.perform.FancyTextPerfChan;
import org.cogchar.impl.perform.FancyTextPerf;
import org.cogchar.impl.perform.FancyTextMedia;
import org.cogchar.impl.perform.FancyTextCursor;
import org.cogchar.platform.util.ClassLoaderUtils;
import org.robokind.api.animation.Animation;
import org.robokind.api.animation.player.AnimationJob;

/**
 *
 * @author StuB22
 * 
 * On 4/9/2013 2:11 PM, Matt Stevenson wrote:
All AnimationPlayers will now return an AnimationJob.  Previously the RemoteAnimationClient was returning null.
To check the status, check animJob.getRemainingTime(TimeUtils.now()).  If it is < 0, the the animation is complete.
* Do not use any listeners, they are not implemented in the RemoteAnimationJob.

 * 
 */


public class AnimOutTrigChan extends FancyTextPerfChan {
	private boolean myUseTempAnimsFlag = false;
	private RobotAnimContext myRobotAnimContext;

	public AnimOutTrigChan(Ident id, RobotAnimContext rac) {
		super(id);
		myRobotAnimContext = rac;
	}

	public void setUseTempAnims(boolean flag) {
		myUseTempAnimsFlag = flag;
	}
	/*	
	@Override protected <Cursor, M extends Media<Cursor>, Time> void  fastCueAndPlay(M m, Cursor startCursor, 
				BasicPerformance<Cursor, M, Time> perf) throws Throwable {
		
		if (!(m instanceof Media.Text<?>)) {
			throw new Exception("Got unexpected media type: " + m.getClass().getName());
		}
		Media.Text<?> textMedia = (Media.Text<?>) m;
	*/
	// Java thinks this method is "public", even though it's marked "protected" in the Scala.
	// https://issues.scala-lang.org/browse/SI-6097
	// Also, Java does not recognize the Throwable annotation.
	@Override public void fancyFastCueAndPlay (FancyTextMedia textMedia, FancyTextCursor cuePos, FancyTextPerf perf)  {	
		String animPathStr = textMedia.getFullText();
		Animation anim = null;
		// Normally we expect the URL lookup for an animation file to work.  But if it does not, we look to an 
		// somewhat vestigal filesystem location called "the animationTempFilePath".  If that fails, we get
		// an error.
		URL animResURL = ClassLoaderUtils.findResourceURL(animPathStr, myRobotAnimContext.myResourceCLs);
		
		if (animResURL != null) {
			getLogger().info("Resolved animation resource URL: {}", animResURL);
			String aruString = animResURL.toExternalForm();
			anim = myRobotAnimContext.myAnimClient.readAnimationFromURL(aruString);
		} else {
			getLogger().warn("Cannot locate animMedia resource {} in classpath {}, now checking local files", animPathStr, myRobotAnimContext.myResourceCLs);
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
			// TODO:  "Cue" within the animation (technically, within the "name" of the animation, at present), 
			// according to cursor
			// TODO:  Deliver progress updates to the perf.			
			AnimationJob animJob = myRobotAnimContext.startFullAnimationNow(anim);
		}
	}
	
}
