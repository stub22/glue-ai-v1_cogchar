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

package org.cogchar.bind.rk.robot.client;
import com.hp.hpl.jena.rdf.model.Model;
import java.net.URL;
import org.appdapter.core.name.Ident;
import org.cogchar.impl.perform.FancyTextPerfChan;
import org.cogchar.impl.perform.FancyTextPerf;
import org.cogchar.impl.perform.FancyTextMedia;
import org.cogchar.impl.perform.FancyTextCursor;
import org.robokind.api.animation.Animation;
import org.robokind.api.animation.player.AnimationJob;

import org.cogchar.impl.perform.MediaHandle;
import org.cogchar.impl.perform.MediaPathResolver;
import org.cogchar.impl.perform.FancyUrlMediaHandle;
import org.cogchar.impl.perform.FancyMediaHandleCache;
import scala.Option;
import scala.Some;
/**
 *
 * @author StuB22
 */

public class CachingAnimPerfChan extends FancyTextPerfChan<AnimationJob> {
	private		RobotAnimClient		myAnimClient;
	public CachingAnimPerfChan(Ident id, RobotAnimContext rac) {
		super(id);
		myAnimClient = rac.myAnimClient;
	}
	
	@Override public void fancyFastCueAndPlay(FancyTextMedia ftm, FancyTextCursor cur, FancyTextPerf perf) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
		
	@Override public void updatePerfStatusQuickly(FancyTextPerf perf) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	@Override public void requestOutJobCancel(AnimationJob aj) {
		getLogger().info("************* Cancelling AnimationJob  on chan [" + getName() + "]");
		long stopTime = 0;
		// This method is defined by the Playable interface.
		aj.stop(stopTime);
	}	
/*

	static class AnimMediaHandle extends FancyUrlMediaHandle<Animation> {
		private	RobotAnimClient	myAnimClient;
		public AnimMediaHandle(RobotAnimClient animClient, Ident mediaID, MediaPathResolver resolver) {
				// Model pathModel, Ident pathPropID, java.util.List<ClassLoader> cloaders) {
			super (mediaID, resolver); // pathModel, pathPropID, cloaders);
			myAnimClient = animClient;
		}
		@Override public Option<Animation> getMediaFromUrl(URL url) {
			String urlString = url.toExternalForm();
			Animation anim = myAnimClient.readAnimationFromURL(urlString);
			return new Some<Animation>(anim);
		}

	}
	
	static class AnimMediaHandleCache extends FancyMediaHandleCache<Animation> {
		private	RobotAnimClient	myAnimClient;
		public AnimMediaHandleCache(RobotAnimClient animClient, Model pathModel, Ident pathPropID, java.util.List<ClassLoader> cloaders) {
			super (pathModel, pathPropID, cloaders);
			myAnimClient = animClient;
		}
		@Override public MediaHandle<Animation> makeMediaHandle(Ident mediaID, MediaPathResolver resolver) {
			// Model pathModel, Ident pathPropID, java.util.List<ClassLoader> cloaders)  {
			return new AnimMediaHandle(myAnimClient, mediaID, resolver); //  , pathModel, pathPropID, cloaders);
		}
	}
	* 
	*/ 
}
