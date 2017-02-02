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


import org.cogchar.animoid.broker.AnimoidFacade;
import org.cogchar.animoid.broker.AnimoidJobSpace;
import org.cogchar.convoid.broker.ConvoidFacade;
import org.cogchar.convoid.job.ConvoidJobSpace;
import org.cogchar.zzz.platform.stub.JobBrokerStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import org.drools.WorkingMemory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class IntegroidJobBroker extends JobBrokerStub implements ConvoidJobSpace, AnimoidJobSpace {
	private static final Logger theLogger = LoggerFactory.getLogger(IntegroidJobBroker.class);

	protected AnimoidFacade myAnimoidFacade;
	protected ConvoidFacade myConvoidFacade;

	public IntegroidJobBroker() { // Thalamus t, StatefulKnowledgeSession sks) {
		super(); // t, sks);
	}

	public void setAnimoidFacade(AnimoidFacade af) {
		myAnimoidFacade = af;
	}

	public AnimoidFacade getAnimoidFacade() {
		return myAnimoidFacade;
	}

	public ConvoidFacade getConvoidFacade() {
		return myConvoidFacade;
	}

	public void setConvoidFacade(ConvoidFacade cf) {
		myConvoidFacade = cf;
	}

	public void playAnimation(String animName, String gestureName, double rashAllowMult, double rashBonusAllow) {
		myAnimoidFacade.playAnimation(animName, gestureName, rashAllowMult, rashBonusAllow);
	}

	public void setSpeakingState() {

	}


}
