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

import org.cogchar.animoid.job.AnimationExecJob;
import org.cogchar.api.animoid.protocol.Library;
import org.cogchar.api.integroid.cue.AwarenessCue;
import org.cogchar.zzz.api.platform.cues.NamedCue;
import org.cogchar.zzz.api.platform.cues.NowCue;
import org.cogchar.zzz.api.platform.cues.TextCue;
import org.cogchar.zzz.api.platform.cues.ThoughtCue;
import org.cogchar.zzz.api.platform.cues.TimerCue;
import org.cogchar.zzz.api.platform.cues.VariableCue;
import org.cogchar.zzz.platform.stub.CueStub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class IntegroidHelpFuncs {
	private static final Logger theLogger = LoggerFactory.getLogger(IntegroidHelpFuncs.class);

	public static VariableCue setVariable(IntegroidFacade igf, String varName, String varVal, double strength) {
		return igf.getCueBroker().setVariableCue(varName, varVal, strength);
	}

	public static AwarenessCue addAwarenessCue(IntegroidFacade igf, double strength) {
		return igf.getCueBroker().addAwarenessCue(strength);
	}

	public static TextCue addTextCue(IntegroidFacade igf, String channelName, String txtVal, double strength) {
		return igf.getCueBroker().addTextCue(channelName, txtVal, strength);
	}

	public static TimerCue addTimerCue(IntegroidFacade igf, String timerName, Integer durationSec) {
		return igf.getCueBroker().addTimerCue(timerName, durationSec);
	}

	public static void clearCue(IntegroidFacade igf, CueStub c) {
		// igf.getCueBroker().clearCue(c);
	}

	/*
		public static void clearAllThoughtCues(IntegroidFacade igf) {
			igf.getCueBroker().clearAllThoughtCues();
		}
	*/
	public static void clearMatchingNamedCues(IntegroidFacade igf, NamedCue nc) {
		igf.getCueBroker().clearMatchingNamedCues(nc);
	}

	public static void clearMatchingNamedCues(IntegroidFacade igf, String name) {
		igf.getCueBroker().clearMatchingNamedCues(name);
	}

	public static ThoughtCue addThoughtCueForName(IntegroidFacade igf, String thoughtName, double strength) {
		return igf.getCueBroker().addThoughtCueForName(thoughtName, strength);
	}


	public static void playAnimation(IntegroidFacade igf, String animName, String gestureName,
									 double rashAllowMult, double rashBonusAllow) {
		theLogger.info("Playing animation: " + animName + " with gesture name: " + gestureName);
		if (animName != null) {
			igf.getJobBroker().playAnimation(animName, gestureName, rashAllowMult, rashBonusAllow);
		} else {
			theLogger.warn("Ignoring request to play null animation");
		}
	}

	public static void clearAnimationJob(IntegroidFacade igf, AnimationExecJob aej) {
		theLogger.info("Clearing Animation Job with createStamp=" + aej.getCreateStampMsec()
				+ ": " + aej);
		IntegroidJobBroker jobBroker = igf.getJobBroker();
		jobBroker.terminateAndClearJob(aej);
	}

	public static void advanceNowCue(IntegroidFacade igf, Long nextNowDurationMsec, Double strength) {
		try {
			NowCue nowCue = igf.getCueBroker().getSolitaryNowCue();
			if (nowCue == null) {
				Long prevDurationMsec = 100L;  // Default value to use on first now cue only
				nowCue = igf.getCueBroker().addNowCue(prevDurationMsec, prevDurationMsec, strength);
			} else {
				nowCue.updateNow();
			}
		} catch (Throwable t) {
		}
	}

	public static Integer randomWholeNumber(IntegroidFacade igf, Integer upperBoundExclusive) {
		return igf.getRandom().nextInt(upperBoundExclusive);
	}

	public static String randomString(IntegroidFacade igf, String... choices) {
		int choiceIndex = randomWholeNumber(igf, choices.length);
		return choices[choiceIndex];
	}


	public static List<String> getAnimNamesMatchingRegexp
			(IntegroidFacade igf, String regexpText) {
		Pattern pat = Pattern.compile(regexpText);
		List<String> resultList = new ArrayList<>();
		Library lib = igf.getAnimoidFacade().getAnimationLibrary();
		if (lib != null) {
			List<String> allAnimationNames = lib.getAnimationNames();
			for (String animName : allAnimationNames) {
				Matcher m = pat.matcher(animName);
				if (m.matches()) {
					resultList.add(animName);
				}
			}
		} else {
			theLogger.warn("No animation library is available!");
		}
		return resultList;
	}

	public static List<String> getAnimNamesStartingWith
			(IntegroidFacade igf, String namePrefix) {
		List<String> resultList = new ArrayList<>();
		Library lib = igf.getAnimoidFacade().getAnimationLibrary();
		if (lib != null) {
			List<String> allAnimationNames = lib.getAnimationNames();
			for (String animName : allAnimationNames) {
				if (animName.startsWith(namePrefix)) {
					resultList.add(animName);
				}
			}
		} else {
			theLogger.warn("No animation library is available!");
		}
		return resultList;
	}

	public static List<String> getAnimNamesMatchingTypes
			(IntegroidFacade igf, String mainType,
			 String subType, String subSubType) {
		StringBuffer prefixBuffer = new StringBuffer();
		if (mainType != null) {
			prefixBuffer.append(mainType);
			if (subType != null) {
				prefixBuffer.append("_" + subType);
				if (subSubType != null) {
					prefixBuffer.append("_" + subSubType);
				}
			}
		} else {
			throw new RuntimeException("MainType is required");
		}
		String namePrefix = prefixBuffer.toString();
		return getAnimNamesStartingWith(igf, namePrefix);
	}

	public static String randomAnimNameWithPrefix(IntegroidFacade igf,
												  String namePrefix) {
		List<String> eligibleNames = getAnimNamesStartingWith(igf, namePrefix);
		if (eligibleNames.size() == 0) {
			theLogger.warn("No eligible names for prefix: " + namePrefix);
			return null;
		}
		theLogger.info("EligibleNames for prefix[" + namePrefix + "]=" + eligibleNames);
		Integer randomIndex = randomWholeNumber(igf, eligibleNames.size());
		String selectedAnimName = eligibleNames.get(randomIndex);
		theLogger.info("SelectedName: " + selectedAnimName);
		return selectedAnimName;
	}

	public static String randomAnimNameMatchingRegexp(IntegroidFacade igf,
													  String nameRegexpText) {
		List<String> eligibleNames = getAnimNamesMatchingRegexp(igf, nameRegexpText);
		if (eligibleNames.size() == 0) {
			theLogger.warn("No eligible names matching regexp: " + nameRegexpText);
			return null;
		}
		theLogger.info("EligibleNames for regexp[" + nameRegexpText + "]=" + eligibleNames);
		Integer randomIndex = randomWholeNumber(igf, eligibleNames.size());
		String selectedAnimName = eligibleNames.get(randomIndex);
		theLogger.info("SelectedName: " + selectedAnimName);
		return selectedAnimName;
	}
}
