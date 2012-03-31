/*
 *  Copyright 2011-2 by The Cogchar Project (www.cogchar.org).
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

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.configuration.ConfigurationException;
import org.osgi.framework.BundleContext;
import org.robokind.api.animation.Animation;
import org.robokind.api.animation.Channel;
import org.robokind.api.animation.MotionPath;
import org.robokind.api.animation.utils.AnimationUtils;
import org.robokind.api.animation.utils.ChannelsParameterSource;

import org.robokind.impl.animation.xml.AnimationXMLReader;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import java.net.URL;
import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.robokind.api.animation.player.AnimationJob;
import org.robokind.api.animation.player.AnimationPlayer;
import org.robokind.api.common.playable.PlayState;


/**
 * This class is able to invoke animations
 * 
 * @author Stu B. <www.texpedient.com>
 */
public class RobotAnimClient extends BasicDebugger {
	private	BundleContext		myBundleCtx;
	private	String				myAnimPlayerOsgiFilterString;
	private AnimationXMLReader	myAnimationReader;
	
	public RobotAnimClient(BundleContext bundleCtx, String animationPlayerOsgiFilterString) throws Exception {
		myBundleCtx = bundleCtx;
		myAnimPlayerOsgiFilterString = animationPlayerOsgiFilterString;
	}
	public AnimationXMLReader getAnimationReader() { 
		if (myAnimationReader == null) {
			myAnimationReader = new AnimationXMLReader();
		}
		return myAnimationReader;
	}

	/*
	 *  @param anim Animation to play
     * @param segmentBeginOffsetMsec Animation start time in milliseconds from the beginning 
     * of the animation
     * @param segmentEndOffsetMsec Animation stop time in milliseconds from the beginning 
     * of the animation
     * @return AnimationJob created from playing the Animation, returns null
     * if unsuccessful
     */	
	public AnimationJob playAnimationSegmentNow(Animation anim, long segBeginOffsetMsec, long segEndOffsetMsec){
        return AnimationUtils.playAnimation(myBundleCtx, myAnimPlayerOsgiFilterString , anim, 
						segBeginOffsetMsec, segEndOffsetMsec);
    }
	public AnimationJob playFullAnimationNow(Animation anim) { 
        return AnimationUtils.playAnimation(myBundleCtx, myAnimPlayerOsgiFilterString, anim);
    }
	
	/*
	*	PENDING -		* A Playable which has not been started.
	*	RUNNING -		* A Playable which is currently running.
	*	PAUSED -		* A Playable which has been paused.
	*	STOPPED -		* A Playable which has been stopped before completion.
	*	COMPLETED -		* A Playable which is completed and no longer running.
	*/
	
	public boolean markAnimationJobComplete(AnimationJob aj) {
		PlayState curPlayState = aj.getPlayState();
		if (curPlayState == PlayState.COMPLETED) {
			return true;
		} else {
			long nowMsec = System.currentTimeMillis();
			return aj.complete(nowMsec);
		}
	}
	public boolean endAndClearAnimationJob(AnimationJob aj) {
		boolean markedOK = markAnimationJobComplete(aj);
		if (markedOK) {
			AnimationPlayer player = aj.getSource();
			player.removeAnimationJob(aj);
			return true;
		} else {
			logWarning("Could not 'COMPLETE' animationJob, so not removing it: [" + aj + "]");
			return false;
		}
	}
    public List<AnimationJob> getAllCurrentAnimationsForPlayer(AnimationPlayer ap) {
		return ap.getCurrentAnimations();
	}	
	public Animation readAnimationFromFile(String filepath){
        try{
            return new AnimationXMLReader().readAnimation(filepath);
        } catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public Animation readAnimation(HierarchicalConfiguration config){
		AnimationXMLReader axr = getAnimationReader();
		return AnimationXMLReader.readAnimation(config);
	}
	/**
	 * http://commons.apache.org/configuration/apidocs/org/apache/commons/configuration/XMLConfiguration.html
	 * @param xmlConfFilePath
	 * @return 
	 */
	public HierarchicalConfiguration readXmlConfigFile (String xmlConfFilePath) {
		HierarchicalConfiguration config = null;
        try{
            config = new XMLConfiguration(xmlConfFilePath);
        }catch (ConfigurationException t){
            logWarning("Cannont open Robokind animation XML  file [" + xmlConfFilePath + "]", t);
        }catch(Exception t){
            logError("Error reading Robokind animation XML  file  [" + xmlConfFilePath + "]", t);
        }		
		return config;
	}
	
	public HierarchicalConfiguration readXmlConfigUrl (String xmlConfUrl) {
		HierarchicalConfiguration config = null;
        try{
			URL url = new URL(xmlConfUrl);
            config = new XMLConfiguration(url);
        }catch (ConfigurationException t){
            logWarning("Cannont open Robokind animation XML URL [" + xmlConfUrl  + "]", t);
        }catch(Exception t){
            logError("Error reading Robokind animation XML URL [" + xmlConfUrl + "]", t);
        }		
		return config;
	}	
	public Animation makeDangerYogaAnim() throws Exception {
		/* This is how the animation editor gets the available channels.  This
		 * uses a Robot and maps the JointIds to the Integers which are used as
		 * channel ids.  Right now, it just uses the Integer from the JointId.
		 * So an animation will only work for a single Robot.
		 * I need to make ids and position in the animations explicit, but it
		 * isn't as critical as fixing the motion was.
		 */

		ChannelsParameterSource cpSource = AnimationUtils.getChannelsParameterSource();
		logInfo(IMPO_LO, "channelParamSource=" + cpSource);
		Map<Integer, String> chanNames = cpSource.getChannelNames();
		logInfo("Test animation channelNames=" + chanNames);
		Animation anim = new Animation();
		//Create your channels and add points
		for (Entry<Integer, String> e : chanNames.entrySet()) {
			Channel chan = new Channel(e.getKey(), e.getValue());
			//default path interpolation is a CSpline
			MotionPath path = new MotionPath();
			//time in millisec, position [0,1]
			path.addPoint(0, 0.5);
			path.addPoint(1000, 1.0);
			path.addPoint(3000, 0.0);
			path.addPoint(4000, 0.5);
			chan.addPath(path);
			anim.addChannel(chan);
		}
		return anim;
	}	
	
   // To use something other than file, we will go through a different constructor
   // for XMLConfiguration, such as the URL one, and then call:
    //  public static Animation readAnimation(HierarchicalConfiguration config){

   
}
