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

import org.appdapter.core.name.Ident;
import org.cogchar.api.perform.Performance;
import org.cogchar.api.perform.Media;

import org.cogchar.impl.perform.FancyTextPerfChan;
import org.cogchar.impl.perform.FancyTextPerf;
import org.cogchar.impl.perform.FancyTime;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.robokind.api.speech.SpeechService;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class SpeechOutputClient extends FancyTextPerfChan {

	BundleContext	myCachedBundleCtx;
	SpeechService	myCachedSpeechSvc;

	public SpeechOutputClient(BundleContext bundleCtx, Ident chanIdent) {
		super(chanIdent);
		myCachedBundleCtx = bundleCtx;
	}
	public SpeechOutputClient(SpeechService speechSvc, Ident chanIdent) {
		super(chanIdent);
		myCachedSpeechSvc = speechSvc;
	}

	
	@Override protected void attemptMediaPlayNow(Media<?> m) throws Throwable {
		if (!(m instanceof Media.Text<?>)) {
			throw new Exception("Got unexpected media type: " + m.getClass().getName());
		}
		Media.Text<?> textMedia = (Media.Text<?>) m;
		String textStr = textMedia.getFullText();
		if (myCachedSpeechSvc != null) {
			myCachedSpeechSvc.speak(textStr);
		} else {
			oldLookupServiceAndSpeakText(textStr);
		}
	}

//	 public long getTimestampMillisecUTC()

	/**
	 * Returns the name of the event of this event.
	 *
	 * @return name of the event of this event
	 */
//    public String getSpeechEventType();
	/**
	 * Returns the stream number for tts output the event originates from.
	 *
	 * @return stream number for tts output the event originates from
	 */
	//   public Long getStreamNumber();
	/**
	 * Returns the position of the speech request the event begins at.
	 *
	 * @return position of the speech request the event begins at
	 */
	//   public Integer getTextPosition();
	/**
	 * Returns the number of characters the event covers.
	 *
	 * @return number of characters the event covers
	 */
	//   public Integer getTextLength();
	/**
	 * Returns event data (usually phone or viseme id) associated with the start of the event.
	 *
	 * @return event data (usually phone or viseme id) associated with the start of the event
	 */
	//   public Integer getCurrentData();
	/**
	 * Returns event data (usually phone or viseme id) associated with the end of the event.
	 *
	 * @return event data (usually phone or viseme id) associated with the end of the event
	 */
//    public Integer getNextData();
	/**
	 * Returns any String data associated with the event (used for SAPI bookmark events).
	 *
	 * @return String data associated with the event (used for SAPI bookmark events)
	 */
//    public String getStringData();
	/**
	 *
	 * Returns the duration of the event in milliseconds. For word boundaries, this duration for speaking the word in
	 * milliseconds. For phonemes and visemes, this is the duration of event in milliseconds.
	 *
	 * @return duration of the event in milliseconds
	 */
	//   public Integer getDuration();
	
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
	public void oldLookupServiceAndSpeakText(String txt) {
		if (txt == null) {
			getLogger().warn("************************* Received null speech text, ignoring");
			return;
		}
		try {
			ServiceContext servCtx = oldLookupSpeechServiceContext();
			if (servCtx != null) {
				try {
					getLogger().info("Trying to speakText[{}]", txt);
					servCtx.speechService.speak(txt);
				} finally {
					servCtx.release();
				}
			} else {
				getLogger().warn("************************* speech-output ServiceContext == null, ignoring speech text: " + txt);
			}
		} catch (Throwable t) {
			getLogger().error("Problem in speakText(txt=[" + txt + "])", t);
		}
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