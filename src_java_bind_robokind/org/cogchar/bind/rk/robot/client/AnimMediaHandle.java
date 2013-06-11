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

import org.cogchar.blob.emit.BehaviorConfigEmitter;

import org.cogchar.impl.perform.FancyMediaHandleCache;
import org.cogchar.impl.perform.FancyUrlMediaHandle;
import org.cogchar.impl.perform.MediaHandle;
import org.cogchar.impl.perform.MediaPathFinder;
import org.cogchar.impl.perform.UrlSearcher;
import org.robokind.api.animation.Animation;
import scala.Option;
import scala.Some;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class AnimMediaHandle extends FancyUrlMediaHandle<Animation> {

	private RobotAnimClient myAnimClient;

	public AnimMediaHandle(RobotAnimClient animClient, Ident mediaID, MediaPathFinder pathFinder, UrlSearcher urlSearcher) {
		// Model pathModel, Ident pathPropID, java.util.List<ClassLoader> cloaders) {
		super(mediaID, pathFinder, urlSearcher); // pathModel, pathPropID, cloaders);
		myAnimClient = animClient;
	}

	@Override public Option<Animation> getMediaFromUrl(URL url) {
		String urlString = url.toExternalForm();
		Animation anim = myAnimClient.readAnimationFromURL(urlString);
		return new Some<Animation>(anim);
	}

	public static class Cache extends FancyMediaHandleCache<Animation> {

		private RobotAnimClient myAnimClient;

		public Cache(RobotAnimClient animClient, MediaPathFinder pathFinder, UrlSearcher urlSearcher) { // Model pathModel, Ident pathPropID, java.util.List<ClassLoader> cloaders) {
			super(pathFinder, urlSearcher); // pathModel, //  pathPropID, cloaders);
			myAnimClient = animClient;
		}

		@Override public FancyUrlMediaHandle<Animation> makeFancyUrlMediaHandle(Ident mediaID, MediaPathFinder pathFinder, UrlSearcher urlSearcher) { // MediaPathResolver resolver) {
			return new AnimMediaHandle(myAnimClient, mediaID, pathFinder, urlSearcher); //  resolver); 
		}
	}
}
