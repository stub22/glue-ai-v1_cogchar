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

package org.cogchar.platform.stub;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.cogchar.platform.cues.NamedCue;
import org.cogchar.platform.cues.NowCue;
import org.cogchar.platform.cues.TextCue;
import org.cogchar.platform.cues.ThoughtCue;
import org.cogchar.platform.cues.TimerCue;
import org.cogchar.platform.cues.VariableCue;
import org.cogchar.platform.util.TimeUtils;

/**
 *
 * @author Stu B. <www.texpedient.com>
 */
public class CueBrokerStub extends ThalamusBrokerStub implements CueSpaceStub {
	private static Logger	theLogger = Logger.getLogger(CueBrokerStub.class.getName());	

	// private List<CueListener> myCueListeners = new ArrayList<CueListener>();
	
	/**
	 * 
	 * @param t
	 * @param sks
	 */
	public CueBrokerStub() { // Thalamus t, StatefulKnowledgeSession sks) {
		super(); 
		// super(t, sks);
	}
	/**
	 * 
	 * @param c
	 * @param strength
	 */
	protected synchronized void stampAndRegisterCue(CueStub c, Double strength) {
		long now = TimeUtils.currentTimeMillis();
		c.setCreateStampMsec(now);
		c.setStrength(strength);
	//	myThalamus.setLastCueAdded(c);
		// Problem with the auto-update deal is that each property change is handled
		// by Drools with a retract-and-assert, allowing all rules involving all
		// properties to re-match.  
		// mySKS.insert(c, c.autoUpdateOnPropertyChange());
		// So, we have replaced with explicit broker update.
//		mySKS.insert(c);
// 		c.setBroker(this);
	}	
	/**
	 * 
	 * @param c
	 */
	@Override public synchronized void clearCue(CueStub c) {
		theLogger.info("Clearing cue: " + c);
		/*
		if (retractFactForObject(c)) {
			myThalamus.setLastCueRemoved(c);
		}
		 * 
		 */
	}
	@Override public void broadcastCueUpdate(CueStub c) {
		notifyCueUpdated(c);
	}
	/**
	 * 
	 */
	@Override public synchronized void clearAllCues() {
		clearAllCuesMatching(CueStub.class);
	}

	/**
	 * 
	 * @param prevDurationMsec
	 * @param nextDurationMsec
	 * @param strength
	 * @return
	 */
	@Override  public NowCue addNowCue(Long prevDurationMsec, Long nextDurationMsec, Double strength) {
		NowCue nc = new NowCue(prevDurationMsec, nextDurationMsec);
		this.stampAndRegisterCue(nc, strength);
		return nc;
	}
	/**
	 * 
	 */
	@Override  public void clearAllNowCues() {
		clearAllCuesMatching(NowCue.class);
	}
	/**
	 * 
	 * @return
	 */
	@Override  public NowCue getSolitaryNowCue() {
		NowCue result = null;
		List<NowCue> allNowCues = getAllFactsMatchingClass(NowCue.class);
		if (allNowCues.size() == 1) {
			return allNowCues.get(0);
		}
		if (allNowCues.size() > 1) {
			throw new RuntimeException("Expected one NowCue, but got " + allNowCues.size());
		}
		if (allNowCues.size() == 0) {
			theLogger.warning("Could not find solitary NowCue [this is OK at startup]");
		}
		return result;
	}


	@Override public TextCue addTextCue(String textChannelName, String textData, Double strength) {
		TextCue tc = new TextCue(textChannelName, textData);
		stampAndRegisterCue(tc, strength);
		return tc;
	}

	@Override public TimerCue addTimerCue(String timerName, Integer durationSec){
        TimerCue tc = new TimerCue(timerName, durationSec);
        stampAndRegisterCue(tc, 1.0);
        return tc;
    }
	@Override public ThoughtCue addThoughtCueForName(String thoughtName, double strength){
        ThoughtCue tc = new ThoughtCue(thoughtName);
        stampAndRegisterCue(tc, strength);
        return tc;
    }


	@Override public List<VariableCue> getAllVariableCues() {
		return getAllFactsMatchingClass(VariableCue.class);
	}
	
	@Override public VariableCue setVariableCue(String varName, String varVal, Double strength) {
		List<VariableCue> allVarCues = getAllVariableCues();
		VariableCue target = null;
		for (VariableCue vc: allVarCues) {
			if (vc.getName().equals(varName)) {
				target = vc;
				target.setValue(varVal);
				target.setStrength(strength);
				break;
			}
		}
		if (target == null) {
			target = new VariableCue(varName, varVal);
			stampAndRegisterCue(target, strength);
		}
		return target;
	}
	/**

	public void addCueListener(CueListener cl) {
		myCueListeners.add(cl);
	}
	public void removeCueListener(CueListener cl) {
		myCueListeners.remove(cl);
	}
	 * @param c
	 */
	protected synchronized void notifyCuePosted(CueStub c) {
		theLogger.finer("notifyCuePosted:" + c.getTypeString());
//		for (CueListener cl: myCueListeners) {
//			cl.notifyCuePosted(c);
//		}
	}
	protected synchronized void notifyCueUpdated(CueStub c) {
		theLogger.finer("notifyCueUpdated:" + c.getTypeString());
//		for (CueListener cl: myCueListeners) {
//			cl.notifyCueUpdated(c);
//		}
	}
	/**
	 * 
	 * @param c
	 */
	protected synchronized void notifyCueCleared(CueStub c) {
		theLogger.finer("notifyCueCleared:" + c.getTypeString());
		//for (CueListener cl: myCueListeners) {
		//	cl.notifyCueCleared(c);
		//}
	}
	/**
	 * 
	 * @param clazz
	 */
	public synchronized void clearAllCuesMatching
				(Class<? extends CueStub> clazz) {
		/*
		List<FactHandle> targets = getAllFactHandlesMatchingClass(clazz);
		for (FactHandle fh: targets) {
			// TODO: this explicit casting to fact types is kinda
			// hokey.
			
			// TODO:  Concerned about the robustness of the delete-
			// duality.  Need to periodically absolute-sync the
			// the workingMemory and the thalamus structures.			
			Object factObj = getFactObjectFromHandle(fh);
			theLogger.info("Retracting factHandle=" + fh + " for fact=" + factObj);
			retractFactForHandle(fh);
			if (factObj instanceof Cue) {
				Cue cue = (Cue) factObj;
				theLogger.info("Removing thalamus cue: " + cue);
				myThalamus.setLastCueRemoved(cue);
			} 
		}
		 * 
		 */
	}
	/**
	 * 
	 * @param name
	 */
	@Override public synchronized void clearMatchingNamedCues(String name) {
		/*
		int clearedCount = 0;
		Class clazz = NamedCue.class;
		List<FactHandle> targets = getAllFactHandlesMatchingClass(clazz);
		for (FactHandle fh: targets) {
			Object factObj = mySKS.getObject(fh);
			if (factObj instanceof NamedCue) {
				NamedCue ncue = (NamedCue) factObj;		
				if (ncue.getName().equals(name)) {
					theLogger.finer("TDA: Retracting factHandle: " + fh);
					mySKS.retract(fh);
					theLogger.finer("TDA: Removing thalamus cue: " + ncue);
					myThalamus.setLastCueRemoved(ncue);
					clearedCount++;
				}
			} else {
				theLogger.severe("Found non-named cue: " + factObj);
			}
		}
		theLogger.finer("Cleared " + clearedCount + " cues named: " + name);
		 * 
		 */
	}
	/**
	 * 
	 * @param nc
	 */
	@Override public synchronized void clearMatchingNamedCues(NamedCue nc) {
		String name = nc.getName();
		clearMatchingNamedCues(name);
	}

	/**
	 * 
	 * @return
	 */
	@Override public JobConfig getJobConfig() {
		return new VariableCueJobConfig();
	}
	private class VariableCueJobConfig extends HashMap<String, String> implements JobConfig {
		VariableCueJobConfig() {
			List<VariableCue> varCueList = getAllVariableCues();
			for (VariableCue vc: varCueList) {
				put(vc.getName(), vc.getValue());
			}
		}
	}	
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();
		Object propertyValue = evt.getNewValue();
		theLogger.fine("propertyChange:  " + propertyName + " := " + propertyValue);
		if (java.beans.Beans.isDesignTime()) {
			theLogger.fine("It's design time!  No further processing of event");
			return;
		}
		/*
		if (propertyName.equals(Thalamus.PROP_LAST_CUE_ADDED)) {
			notifyCuePosted((Cue) propertyValue);
		} else 	if (propertyName.equals(Thalamus.PROP_LAST_CUE_REMOVED)) {
			notifyCueCleared((Cue) propertyValue);
		}
		 * 
		 */
	}

    public void addCue(CueStub c){
        this.stampAndRegisterCue(c, c.getStrength());
    }
	@Override public <NCT extends NamedCue>  NCT getNamedCue(Class<NCT> clazz, String cueName) {
		List<NCT> allCues = getAllFactsMatchingClass(clazz);
		for (NCT cue : allCues) {
			String cn = cue.getName();
			if (cn != null) {
				if (cn.equals(cueName)) {
					return cue;
				}
			}
		}
		return null;
	}
}
