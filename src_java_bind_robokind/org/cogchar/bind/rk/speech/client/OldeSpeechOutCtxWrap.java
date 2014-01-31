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
package org.cogchar.bind.rk.speech.client;

import org.appdapter.core.log.BasicDebugger;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.mechio.api.speech.SpeechJob;

import org.mechio.api.speech.SpeechService;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class OldeSpeechOutCtxWrap extends BasicDebugger {
	private BundleContext			myCachedBundleCtx;

	protected OldeSpeechOutCtxWrap(BundleContext bundleCtx) {
		myCachedBundleCtx = bundleCtx;
	}
	private class ServiceContext {

		public ServiceReference serviceRef;
		public SpeechService speechService;

		public void release() {
			if (serviceRef != null) {
				myCachedBundleCtx.ungetService(serviceRef);
			}
		}

		public void speak() {
		}
	}
	@Deprecated
	public SpeechJob oldLookupServiceAndSpeakText(String txt) {
		if (txt == null) {
			getLogger().warn("************************* Received null speech text, ignoring");
			return null;
		}
		SpeechJob resultJob = null;
		try {
			ServiceContext servCtx = oldLookupSpeechServiceContext();
			if (servCtx != null) {
				try {
					getLogger().info("Trying to speakText[{}]", txt);
					resultJob = servCtx.speechService.speak(txt); 
				} finally {
					servCtx.release();
				}
			} else {
				getLogger().warn("************************* speech-output ServiceContext == null, ignoring speech text: " + txt);
			}
		} catch (Throwable t) {
			getLogger().error("Problem in speakText(txt=[" + txt + "])", t);
		}
		return resultJob;
	}
	@Deprecated
	public void oldCancelAllRunningSpeechTasks() {
		try {
			ServiceContext servCtx = oldLookupSpeechServiceContext();
			if (servCtx != null) {
				try {
					logWarning("************************* We don't have speech-cancel feature yet, sorry");
					// TODO:  Plug in code to kill speech jobs.
					// servCtx.speechService.???????
				} finally {
					servCtx.release();
				}
			} else {
				logWarning("**************** speech-output ServiceContext == null,  ignoring request to cancelAllRunningSpeechTasks");
			}
		} catch (Throwable t) {
			logError("Exception in cancelAllRunningSpeechTasks()", t);
		}
	}

	private ServiceContext oldLookupSpeechServiceContext() throws Throwable {
		ServiceContext servCtx = new ServiceContext();
		servCtx.serviceRef = myCachedBundleCtx.getServiceReference(SpeechService.class.getName());
		if (servCtx.serviceRef != null) {
			try {
				Object serviceObj = myCachedBundleCtx.getService(servCtx.serviceRef);
				if (serviceObj != null) {
					servCtx.speechService = (SpeechService) serviceObj;
				}
			} finally {
				if (servCtx.speechService == null) {
					servCtx.release();
					return null;
				}
			}
			return servCtx;
		} else {
			return null;
		}
	}	
}
