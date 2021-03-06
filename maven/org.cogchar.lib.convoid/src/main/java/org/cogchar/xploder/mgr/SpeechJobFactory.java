/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.xploder.mgr;

import org.cogchar.api.convoid.act.Category;
import org.cogchar.api.convoid.act.Step;
import org.cogchar.convoid.job.SpeechJob;
import org.cogchar.convoid.player.IBehaviorPlayable;
import org.cogchar.convoid.player.SpeechPlayer;
import org.cogchar.xploder.cursors.CategoryCursor;
import org.cogchar.xploder.cursors.CursorFactory;
import org.cogchar.xploder.cursors.IConvoidCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * @author Matt Stevenson
 */
public class SpeechJobFactory {
	private static final Logger theLogger = LoggerFactory.getLogger(SpeechJobFactory.class);
	private String myBehaviorType;
	private Class<? extends SpeechJob> myJobClass;
	private Double myThreshold;
	private Long myResetTime;

	public SpeechJobFactory(String type, Class<? extends SpeechJob> clss, Double thresh, Long reset) {
		myBehaviorType = type;
		myJobClass = clss;
		myThreshold = thresh;
		myResetTime = reset;
	}

	public SpeechJob buildJob(CategoryCursor cc) {
		try {
			Constructor cons = getJobClass().getConstructor(CategoryCursor.class);
			return (SpeechJob) cons.newInstance(cc);
		} catch (Throwable t) {
			theLogger.warn("Unable to build a job for the given category: " + cc.getName());
		}
		return null;
	}

	public IBehaviorPlayable buildPlayer(Step step, IConvoidCursor cc, SpeechJob job) {
		if (job.getClass() != getJobClass()) {
			throw new IllegalArgumentException(this.getClass().getSimpleName() +
					" can only take " + getJobClass().getSimpleName() + ", it was given: "
					+ job.getClass());
		}
		job.setCurrentCursor(cc);
		return new SpeechPlayer(step, job);
	}

	public String getBehaviorType() {
		return myBehaviorType;
	}

	public CursorGroup buildCursorGroup(Category rootCat) {
		return createCursorGroup(rootCat, myThreshold, myResetTime);
	}

	public Class<? extends SpeechJob> getJobClass() {
		return myJobClass;
	}

	protected CursorGroup createCursorGroup(Category root, double thresh, long reset) {
		if (root == null) {
			throw new IllegalArgumentException("Root category cannot be null");
		}
		List<IConvoidCursor> cursors = CursorFactory.buildAllCursorsForCategory(root, myBehaviorType);
		CursorGroup g = new CursorGroup(cursors, thresh, reset, this);
		g.initializeJobs();
		return g;
	}
}
