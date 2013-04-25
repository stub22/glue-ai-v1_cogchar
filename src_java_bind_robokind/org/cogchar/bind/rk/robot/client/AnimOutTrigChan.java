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
import org.cogchar.impl.perform.FancyTextPerfChan;
import org.cogchar.impl.perform.FancyTextPerf;
import org.cogchar.impl.perform.FancyTextMedia;
import org.cogchar.impl.perform.FancyTextCursor;
import org.cogchar.platform.util.ClassLoaderUtils;
import org.robokind.api.animation.Animation;
import org.robokind.api.animation.player.AnimationJob;
import org.robokind.api.common.utils.TimeUtils;

/**
 *
 * @author StuB22
 * 

 * 
 */


public class AnimOutTrigChan extends FancyTextPerfChan<AnimationJob> {
	private boolean myUseTempAnimsFlag = false;
	private RobotAnimContext myRobotAnimContext;

	public AnimOutTrigChan(Ident id, RobotAnimContext rac) {
		super(id);
		myRobotAnimContext = rac;
	}

	public void setUseTempAnims(boolean flag) {
		myUseTempAnimsFlag = flag;
	}

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
			// TODO:  Record the relationship between this job + perf.		
			AnimationJob animJob = myRobotAnimContext.startFullAnimationNow(anim);
			registerOutJobForPerf(perf, animJob);
		}
	}
	// TODO:  Keep track of job associated with the performance, and use perf.markState 
	// (and even perf.markCursor) to report on the job's status and position.
	
	@Override public void updatePerfStatusQuickly(FancyTextPerf perf) {
		// This method is invoked repeatedly as soon as we return from fancyFastCueAndPlay() above.
		// It will keep being invoked until we call perf.markState(STOPPING).
		// If multiple performances are outstanding on the channel, they will all be updated through
		// separate calls to this method (until we mark them each STOPPING).
	
		// We  must deliver the performance status updates in *this* method.
		// We must not deliver status updates to the perf in a separate callback thread
		// (e.g. the thread used by the job to deliver its events/callbacks).  
		// That approach risks contention in the behavior system, which we do not want.
		
		/* On 4/9/2013 2:11 PM, Matt Stevenson wrote:
All AnimationPlayers will now return an AnimationJob.  Previously the RemoteAnimationClient was returning null.
To check the status, check animJob.getRemainingTime(TimeUtils.now()).  If it is < 0, the the animation is complete.
Do not use any listeners, they are not implemented in the RemoteAnimationJob.
		 */
		
		boolean finished = false;
		AnimationJob jobToCheck = getOutJobOrNull(perf);
		if (jobToCheck != null) {
			long remainingTime = jobToCheck.getRemainingTime(TimeUtils.now());
			getLogger().debug("Animation remaining time is {}", remainingTime);
			// Optional Todo:  Use this remaining time to update the current-position-cursor of the performance.
			// Stu is using <= 0 rather than <0 , in case impl changes (0 time left should mean over, right?) - double check with Matt.
			if (remainingTime <= 0) {
				getLogger().debug("Animation remaining time is {}, which is <= 0, so perf is done.", remainingTime);
				finished = true;
			}
		} else {
			getLogger().error("Cannot find AnimationJob for performance, marking it stopped: {}", perf);
			finished = true;
		}
		if (finished) {
			getLogger().info("Marking performance [{}] stopped", perf);
			markPerfStoppedAndForget(perf);
		}
	}	
	
}
