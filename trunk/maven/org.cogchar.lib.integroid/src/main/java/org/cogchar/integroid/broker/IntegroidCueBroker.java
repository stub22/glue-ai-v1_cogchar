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


package org.cogchar.integroid.broker;

import org.cogchar.animoid.broker.AnimoidCueSpaceStub;
import org.cogchar.api.convoid.cue.ConvoidCueSpace;
import org.cogchar.api.convoid.cue.ModeCue;
import org.cogchar.api.convoid.cue.VerbalCue;
import org.cogchar.api.integroid.cue.AwarenessCue;
import org.cogchar.api.integroid.cue.PersonCue;
import org.cogchar.zzz.platform.stub.CueBrokerStub;
import org.cogchar.zzz.platform.stub.JobConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


public class IntegroidCueBroker extends CueBrokerStub implements ConvoidCueSpace, AnimoidCueSpaceStub, JobConfig.Source {

	private static final Logger theLogger = LoggerFactory.getLogger(IntegroidCueBroker.class);

	public IntegroidCueBroker() { // Thalamus t, StatefulKnowledgeSession sks

		super(); // t, sks);
	}


	/* (non-Javadoc)
	 * @see com.hansonrobotics.convoid.brain.CueBrokerInterface#addVerbalCueForMeanings(java.util.List, double)
     */
	public VerbalCue addVerbalCueForMeanings(Map<String, Double> meanings, double strength) {
		VerbalCue vc = new VerbalCue();
		vc.setMeanings(meanings);
		stampAndRegisterCue(vc, strength);
		return vc;
	}

	/* (non-Javadoc)
	 * @see com.hansonrobotics.convoid.brain.CueBrokerInterface#addModeCueForName(java.lang.String, double)
     */
	public ModeCue addModeCueForName(String modeName, double strength) {
		// How does modeCue replacement work when it is triggered
		// from a behavior step? 

		// TODO: look for an existing mode cue with this name, and
		// if it exists, increase its strength and renew its stamp.
		// (Should that be done using rules?)
		ModeCue mc = new ModeCue(modeName);
		stampAndRegisterCue(mc, strength);
		return mc;
	}


	public synchronized void clearAllModeCues() {
		clearAllCuesMatching(ModeCue.class);
	}

	public synchronized void clearAllVerbalCues() {
		clearAllCuesMatching(VerbalCue.class);
	}

	public void registerPersonCue(PersonCue pc, double strength) {
		stampAndRegisterCue(pc, strength);
	}

	public AwarenessCue addAwarenessCue(double strength) {
		AwarenessCue ac = new AwarenessCue();
		stampAndRegisterCue(ac, strength);
		return ac;
	}

	public List<PersonCue> getAllPersonCues() {
		return this.getAllFactsMatchingClass(PersonCue.class);
	}

	public AwarenessCue getAwarenessCue() {
		try {
			return getSingleFactMatchingClass(AwarenessCue.class);
		} catch (Throwable t) {
			theLogger.warn("Can't find AwarenessCue");
		}
		return null;
	}

}
