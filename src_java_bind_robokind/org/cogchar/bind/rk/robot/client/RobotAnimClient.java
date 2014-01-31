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



import org.apache.commons.configuration.ConfigurationException;
import org.osgi.framework.BundleContext;
import org.mechio.api.animation.Animation;
import org.mechio.api.animation.Channel;
import org.mechio.api.animation.MotionPath;
import org.mechio.api.animation.utils.AnimationUtils;
import org.mechio.api.animation.utils.ChannelsParameterSource;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import java.net.URL;
import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.cogchar.bind.rk.robot.model.ModelJoint;
import org.cogchar.bind.rk.robot.model.ModelRobot;
import org.mechio.api.animation.player.AnimationJob;
import org.mechio.api.animation.player.AnimationPlayer;
import org.mechio.api.animation.utils.ChannelsParameter;
import org.mechio.api.animation.xml.AnimationFileReader;
import org.mechio.api.animation.xml.AnimationXML;
import org.mechio.api.common.playable.PlayState;
import org.mechio.api.common.position.NormalizedDouble;
import org.mechio.api.motion.Joint;
import org.mechio.api.motion.Robot;
import org.mechio.impl.animation.xml.AnimationXMLReader;




/**
 * This class is able to invoke animations.
 * 
 * It currently holds two separate implementations, one used when we have an AnimPlayer already,
 * another when we want to search OSGi every time.
 * 
 * @author Stu B. <www.texpedient.com>
 */
public class RobotAnimClient extends BasicDebugger {
	private	BundleContext		myBundleCtx;
	private	String				myAnimPlayerOsgiFilterString;
	private AnimationFileReader	myAnimationReader;
	private	AnimationPlayer		myCachedAnimPlayer;
	
	public RobotAnimClient(BundleContext bundleCtx, String animationPlayerOsgiFilterString) throws Exception {
		myBundleCtx = bundleCtx;
		myAnimPlayerOsgiFilterString = animationPlayerOsgiFilterString;
	}
	public RobotAnimClient(AnimationPlayer cachedPlayer) {
		myCachedAnimPlayer = cachedPlayer;
	}
	public AnimationFileReader getAnimationReader() { 
		if (myAnimationReader == null) {
			myAnimationReader = AnimationXML.getRegisteredReader();
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
		if (myCachedAnimPlayer != null) {
			return myCachedAnimPlayer.playAnimation(anim, segBeginOffsetMsec, segEndOffsetMsec);
		} else {
	        return AnimationUtils.playAnimation(myBundleCtx, myAnimPlayerOsgiFilterString , anim, 
						segBeginOffsetMsec, segEndOffsetMsec);
		}
    }
	public AnimationJob playFullAnimationNow(Animation anim) { 
		long lengthMsec = anim.getLength();
		return playAnimationSegmentNow(anim, 0, lengthMsec);		
		
	/*"The PlayState is not implemented for the RemoteAnimationJob."
	To get correct "remainingTime", when using a networked animation player.
Before you play an animation set the start time and stop time on the animation, 
* or use the play animation with time args:
    anim.setStartTime(0);
    anim.setStopTime(anim.getLength());
or    animPlayer.playAnimation(anim, 0, anim.getLength());			 */
		/*
		 * if (myCachedAnimPlayer != null) {
		 
			// return myCachedAnimPlayer.playAnimation(anim);
		} else {
			return AnimationUtils.playAnimation(myBundleCtx, myAnimPlayerOsgiFilterString, anim);
		}	*/
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
			getLogger().warn("Could not 'COMPLETE' animationJob, so not removing it: [" + aj + "]");
			return false;
		}
	}
    public List<AnimationJob> getAllCurrentAnimationsForPlayer(AnimationPlayer ap) {
		return ap.getCurrentAnimations();
	}	
	public Animation readAnimationFromFile(String filepath){
        try{
            AnimationFileReader reader = getAnimationReader();
            if(reader == null){
                return null;
            }
            return reader.readAnimation(filepath);
        } catch(Throwable t){
             getLogger().error("Problem reading animation from {} ", filepath, t);
            return null;
        }
    }
	public Animation readAnimationFromHC(HierarchicalConfiguration hc) { 
		return AnimationXMLReader.readAnimation(hc);
	}
	public Animation readAnimationFromURL(String urlText) {
		Animation anim = null;
		try {
			HierarchicalConfiguration hc = readXmlConfigUrl(urlText);
			if (hc != null) {
				anim = readAnimationFromHC(hc);
			}
		} catch (Throwable t) {
			getLogger().error("Problem reading animation from {} ", urlText, t);
		}
		return anim;
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
            getLogger().warn("Cannont open Robokind animation XML  file [" + xmlConfFilePath + "]", t);
        }catch(Exception t){
            getLogger().error("Error reading Robokind animation XML  file  [" + xmlConfFilePath + "]", t);
        }		
		return config;
	}
	
	public HierarchicalConfiguration readXmlConfigUrl (String xmlConfUrl) {
		HierarchicalConfiguration config = null;
        try{
			URL url = new URL(xmlConfUrl);
            config = new XMLConfiguration(url);
        }catch (ConfigurationException t){
            getLogger().warn("Cannont open Robokind animation XML URL [" + xmlConfUrl  + "]", t);
        }catch(Exception t){
            getLogger().error("Error reading Robokind animation XML URL [" + xmlConfUrl + "]", t);
        }		
		return config;
	}	
	public enum BuiltinAnimKind {
		BAK_GOTO_DEFAULTS,
		BAK_GOTO_MAX_NORM,
		BAK_GOTO_MIN_NORM,
		BAK_DANGER_YOGA
	}
	public Animation makeBuiltinAnim(BuiltinAnimKind baKind, ModelRobot refBot) throws Exception {
		/* From Matt (in early 2012?):
		 * This is how the animation editor gets the available channels.  This
		 * uses a Robot and maps the JointIds to the Integers which are used as
		 * channel ids.  Right now, it just uses the Integer from the JointId.
		 * So an animation will only work for a single Robot.
		 * I need to make ids and position in the animations explicit, but it
		 * isn't as critical as fixing the motion was.
		 */
		
		Robot.Id robotID = refBot.getRobotId();
		
		ChannelsParameterSource cpSource = AnimationUtils.getChannelsParameterSource();
		getLogger().debug("channelParamSource={}", cpSource);
		List<ChannelsParameter> chanParams = cpSource.getChannelParameters();
		getLogger().debug("Test animation channels={}", chanParams);
		Animation anim = new Animation();
		//Create your channels and add points
		for (ChannelsParameter cp : chanParams) {
            int jointNum = cp.getChannelID();
            String name = cp.getChannelName();
			Channel chan = new Channel(jointNum, name);
			getLogger().debug("Creating MotionPath for channel jointNum={}, name={}", jointNum, name);
			//default path interpolation is a CSpline
			MotionPath path = new MotionPath();
			
			Joint.Id jointId = new Joint.Id(jointNum);
			Robot.JointId rJID = new Robot.JointId(robotID, jointId);
			ModelJoint mj = refBot.getJoint(rJID);
	
			//time in millisec, position in normalized range [0,1]
			// Need to get the joint so we can get the abs-default position.
			int gotoRampMsec = 1000;
			
			NormalizedDouble defaultPosNorm = mj.getDefaultPosition();
			double maxPosNorm = 1.0;
			double minPosNorm = 0.0;
			

			switch (baKind) {
				case BAK_GOTO_DEFAULTS:
					path.addPoint(gotoRampMsec, defaultPosNorm.getValue());
				break;
				case BAK_DANGER_YOGA:		
					path.addPoint(500, defaultPosNorm.getValue());
					path.addPoint(1500, 1.0);
					path.addPoint(2500, 0.0);
					path.addPoint(3500, defaultPosNorm.getValue());					
				break;	
			}			

			chan.addPath(path);
			anim.addChannel(chan);
		}
		return anim;
	}		

	
   // To use something other than file, we will go through a different constructor
   // for XMLConfiguration, such as the URL one, and then call:
    //  public static Animation readAnimation(HierarchicalConfiguration config){

   
}
