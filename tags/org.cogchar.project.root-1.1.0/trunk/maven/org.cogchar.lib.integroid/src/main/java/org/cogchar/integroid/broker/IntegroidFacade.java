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


import java.beans.PropertyChangeEvent;

import java.util.Random;

import javax.swing.SwingUtilities;
import org.cogchar.animoid.broker.AnimoidFacade;
import org.cogchar.convoid.broker.ConvoidFacade;
import org.cogchar.convoid.broker.ConvoidFacadeSource;
import org.cogchar.convoid.broker.IRemoteResponseInterface;
import org.cogchar.api.convoid.act.Category;
import org.cogchar.convoid.job.AgendaManager;
import org.cogchar.zzz.api.platform.cues.ThoughtCue;
import org.cogchar.zzz.api.platform.cues.VariableCue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class IntegroidFacade implements ConvoidFacadeSource, ReactionProcessor { // { extends ThalamusBroker implements ReactionProcessor, ConvoidFacadeSource {
	private static Logger	theLogger = LoggerFactory.getLogger(IntegroidFacade.class.getName());
	static {
		// theLogger.setLevel(Level.ALL);
	}
	
	private		IntegroidJobBroker			myJobBroker;
	private		IntegroidCueBroker			myCueBroker;
	//private		IntegroidWrapper			myWrapper;
	private		Random						myRandom;
	
	public IntegroidFacade() { // Thalamus t, StatefulKnowledgeSession sks) {
		/*
		super(t, sks);
		myJobBroker = new IntegroidJobBroker(t, sks);
		myCueBroker = new IntegroidCueBroker(t, sks);
		 * 
		 */
		myRandom = new Random();
	}
	
	public IntegroidJobBroker getJobBroker() {
		return myJobBroker;
	}
	public IntegroidCueBroker getCueBroker() {
		return myCueBroker;
	}	
	 public ConvoidFacade getConvoidFacade() {
		return myJobBroker.getConvoidFacade();
	}

	public AnimoidFacade getAnimoidFacade() {
		return myJobBroker.getAnimoidFacade();
	}	

	public void initConvoidFacade(Category rootCat, IRemoteResponseInterface remote, AgendaManager am) {
		// ConvoidFacade cf = new ConvoidFacade(myCueBroker, myJobBroker, rootCat, remote, am);
		// myJobBroker.setConvoidFacade(cf);
	}

	public void doSomething(String whatToDo) {
		theLogger.debug("got instruction to: " + whatToDo);
	}

	public synchronized void processAllDecisions() {
		// mySKS.fireAllRules();
	}
	public void propertyChange(PropertyChangeEvent evt) {
		
	}	
	public void processWhenSafe() {
		theLogger.debug("Requesting delayed decision processing");
		// need to push in VerbalCue and usually modeCue
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				theLogger.debug("======================= Decision processing START");
				try {
					processAllDecisions();  
				} catch (Throwable t) {
					theLogger.error("Delayed decision processor caught exception.", t);
				}
				theLogger.debug("======================= Decision processing END");
			}
		});
	}
	public void establishNetworkWrapper() throws Throwable {
	//	myWrapper = IntegroidWrapper.createAndRegister(this);
	//	myJobBroker.addJobListener(myWrapper);
	//	myCueBroker.addCueListener(myWrapper);
	}
	public Random getRandom() {
		return myRandom;
	}

	public ThoughtCue getThoughtCue(String name) {
		return myCueBroker.getNamedCue(ThoughtCue.class, name);
	}
	public VariableCue getVariableCue(String name) {
		return myCueBroker.getNamedCue(VariableCue.class, name);
	}
	
	 
}
