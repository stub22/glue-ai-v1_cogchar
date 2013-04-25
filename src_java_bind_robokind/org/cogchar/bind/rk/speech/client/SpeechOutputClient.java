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

import org.cogchar.impl.perform.FancyTextPerfChan;
import org.cogchar.impl.perform.FancyTextPerf;
import org.cogchar.impl.perform.FancyTextMedia;
import org.cogchar.impl.perform.FancyTextCursor;

import org.osgi.framework.BundleContext;
import org.robokind.api.animation.player.AnimationJob;
import org.robokind.api.speech.SpeechJob;

import org.robokind.api.speech.SpeechService;
import org.robokind.api.speech.utils.DefaultSpeechJob;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class SpeechOutputClient extends FancyTextPerfChan<SpeechJob> {

	private SpeechService myCachedSpeechSvc;
	private OldeSpeechOutCtxWrap myOldCtxWrap;

	public SpeechOutputClient(BundleContext bundleCtx, Ident chanIdent) {
		super(chanIdent);
		myOldCtxWrap = new OldeSpeechOutCtxWrap(bundleCtx);
	}

	public SpeechOutputClient(SpeechService speechSvc, Ident chanIdent) {
		super(chanIdent);
		myCachedSpeechSvc = speechSvc;
	}

	// Java thinks this method is "public", even though it's marked "protected" in the Scala.
	// https://issues.scala-lang.org/browse/SI-6097
	// Also, Java does not recognize the Throwable annotation.
	@Override
	public void fancyFastCueAndPlay(FancyTextMedia ftm, FancyTextCursor cuePos, FancyTextPerf perf) {
		String textStr = ftm.getFullText();
		// TODO:  "Cue" within the text, according to cursor
		// TODO:  Deliver progress updates to the perf.
		SpeechJob sjob = _directlyStartSpeakingText(textStr);
		registerOutJobForPerf(perf, sjob);
	}

	@Override
	public void updatePerfStatusQuickly(FancyTextPerf perf) {
		// TODO:  Keep track of job associated with the performance, and use perf.markState (and even perf.markCursor)
		boolean finished = false;
		SpeechJob jobToCheck = getOutJobOrNull(perf);
		if (jobToCheck != null) {
			int speechJobStatus = jobToCheck.getStatus();
			switch (speechJobStatus) {
				case DefaultSpeechJob.CANCELED:
				case DefaultSpeechJob.COMPLETE:
					finished = true;
					getLogger().info("Found speech-job status {}, (where CANCELED=" + 
							DefaultSpeechJob.CANCELED + " and COMPLETE=" + DefaultSpeechJob.COMPLETE  + 
							") so perf-monitor will now be STOPPED: {}", speechJobStatus, perf);
				break;
				case DefaultSpeechJob.RUNNING:
					getLogger().debug("Status is now RUNNING/PLAYING for perf {}", perf);
					perf.markFancyState(Performance.State.PLAYING);
				break;
				case DefaultSpeechJob.PENDING:
					getLogger().debug("Status is now PENDING/CUEING (= queing) for perf {}", perf);
					perf.markFancyState(Performance.State.CUEING);
				break;
				default:
					getLogger().info("Found unknown speech-job status {}, so perf will continue: {}", speechJobStatus, perf);
			}
		} else {
			finished = true;
			getLogger().error("Found no job for performance, marking STOPPED: {}", perf);
		}
		if (finished) {
			getLogger().info("Marking performance STOPPED: {} ", perf);
			markPerfStoppedAndForget(perf);
		}

	}
	// We want this method to be private, but currently it cannot be because _____

	@Deprecated
	public SpeechJob _directlyStartSpeakingText(String textStr) {
		if (myCachedSpeechSvc != null) {
			SpeechJob speechJob = myCachedSpeechSvc.speak(textStr);
			return speechJob;
		} else if (myOldCtxWrap != null) {
			return myOldCtxWrap.oldLookupServiceAndSpeakText(textStr);
		} else {
			return null;
		}
	}

	@Deprecated
	public void _directlyCancelAllRunningSpeechTasks() {
		if (myCachedSpeechSvc != null) {
			// TODO:  Plug in code to kill speech jobs.
			// myCachedSpeechSvc.???????
		} else if (myOldCtxWrap != null) {
			myOldCtxWrap.oldCancelAllRunningSpeechTasks();
		}
	}
}
/*
 * > On 4/8/2013 5:55 PM, Matt wrote: > I just changed the SpeechService so the speak(String) method now returns a
 * SpeechJob. > The SpeechJob has addListener(Listener<org.__jflux.api.core.playable.__Playable.PlayState>), this is >
 * also a PlayState in robokind which you do not want to use. > You will be notified when the speech begins with
 * RUNNING. There can be a delay between calling > speak(String) > and the speech job starting if other text is already
 * in the speech queue. You may miss the RUNNING > notification by the time you get a SpeechJob and are able to add the
 * listener. > When the speech is complete you will be notified with COMPLETED, and if it is canceled you will be >
 * notified with ABORTED. > On Mon, Apr 8, 2013 at 7:23 PM, StuB22 > > So, possibly on a very short or empty speech job
 * (e.g. "") , we could also > miss the "COMPLETED" notice?> > If so, can we additionally synchronously query the
 * SpeechJob for its status?
 *
 * On 4/8/2013 6:31 PM, Matt Stevenson wrote:> Yes, there is a getStatus() method. It returns an int for a status as
 * defined in DefaultSpeechJob (this was copied over from Robosteps). I will leave it this way for the show and clean it
 * up a little more next week. > I could also modify the speech job so if it is completed or canceled and a listener is
 * added, it will be > notified immediately. > > Another potential problem is sending a speech command at the same time
 * from a different instance. The speech > jobs would get confused about the events from the other commands, but you can
 * send two speech commands from a > single instance without a problem. >  *
 * //	Here are the methods we can call on SpeechEvent //	public long getTimestampMillisecUTC()
 *
 * Returns the name of the event of this event. @return name of the event of this event public String
 * getSpeechEventType(); Returns the stream number for tts output the event originates from. // public Long
 * getStreamNumber(); Returns the position of the speech request the event begins at. // public Integer
 * getTextPosition(); Returns the number of characters the event covers. @return number of characters the event covers
 * // public Integer getTextLength() Returns event data (usually phone or viseme id) associated with the start of the
 * event. // public Integer getCurrentData(); // public Integer getNextData(); Returns any String data associated with
 * the event (used for SAPI bookmark events). // public String getStringData(); Returns the duration of the event in
 * milliseconds. For word boundaries, this duration for speaking the word in milliseconds. For phonemes and visemes,
 * this is the duration of event in milliseconds. // public Integer getDuration();
 */

/*
 * /** Note This more flexible override is allowed but unnecessary if we are satisfied with the
 * fancy-typed(nonparametric) approach above. @Override protected <Cursor, M extends Media<Cursor>, Time> void
 * fastCueAndPlay(M m, Cursor startCursor, BasicPerformance<Cursor, M, Time> perf) throws Throwable  *
 */
