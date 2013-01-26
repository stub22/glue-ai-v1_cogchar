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
package org.cogchar.app.puma.cgchr;



import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.appdapter.core.log.BasicDebugger;

import org.appdapter.core.name.Ident;

import org.osgi.framework.BundleContext;
import org.cogchar.bind.rk.robot.model.ModelRobot;
import org.cogchar.bind.rk.robot.model.ModelJoint;

import org.cogchar.render.model.bony.FigureState;

import org.cogchar.render.sys.context.CogcharRenderContext;
import org.cogchar.render.app.humanoid.HumanoidRenderContext;
import org.cogchar.render.model.humanoid.HumanoidFigure;

import org.appdapter.core.log.BasicDebugger;
import org.appdapter.help.repo.RepoClient;

import org.cogchar.api.humanoid.HumanoidConfig;
import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.cogchar.api.skeleton.config.BoneProjectionRange;
import org.cogchar.api.skeleton.config.BoneCN;
import org.cogchar.bind.rk.robot.client.RobotAnimContext;
import org.cogchar.bind.rk.robot.client.RobotVisemeClient;
import org.cogchar.bind.rk.robot.client.RobotAnimClient.BuiltinAnimKind;

import org.cogchar.bind.rk.robot.svc.ModelBlendingRobotServiceContext;
import org.cogchar.impl.perform.FancyTextChan;

import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.osgi.framework.ServiceRegistration;
import org.cogchar.platform.util.ClassLoaderUtils;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * A Puma character uses this class to control its embodiment, with or without an OpenGL avatar.
 * Robokind animation is available at two different levels:  Through the 
 * 
 * ModelBlendingRobotServiceContext
 * 
 */
public class PumaModelHumanoidMapper extends BasicDebugger {

	
	private	ModelBlendingRobotServiceContext		myMBRSC;
	// private	RobotAnimContext						myRAC;
	
	private	PumaVirtualWorldMapper					myVWorldMapper;
	private	Ident									myCharID;
	private	ServiceRegistration						myBoneRobotConfigServiceRegistration;	
	
	protected PumaModelHumanoidMapper(PumaVirtualWorldMapper vWorldMapper, BundleContext bundleCtx, Ident charIdent) {
		myCharID = charIdent;
		myVWorldMapper = vWorldMapper;
		myMBRSC = new ModelBlendingRobotServiceContext(bundleCtx); 
	}
	
	protected HumanoidRenderContext getHumanoidRenderContext() { 
		HumanoidRenderContext hrc = null;
		if (myVWorldMapper != null) { 
			hrc = myVWorldMapper.getHumanoidRenderContext();
		}
		return hrc;
	}
	/**
	 * Direct access to the Robokind-compliant Cogchar model of an Avatar's joints, at protected scope.
	 * @return 
	 */
	protected ModelRobot getBonyRobot() { 
		return myMBRSC.getRobot();
	}
	/**
	 * 
	 * @return 
	 */
	protected ModelBlendingRobotServiceContext getRobotServiceContext() { 
		return myMBRSC;
	}
	/**
	 * This method exposes our "best" AnimOutChan at protected scope.
	 * This is the main pathway for wiring animation triggers from behavior systems
	 * (whether local or remote).
	 * @return 
	 */

	protected boolean initVWorldHumanoid(RepoClient qi, final Ident qGraph, final HumanoidConfig hc) throws Throwable {
		if (myVWorldMapper != null) {
			HumanoidRenderContext hrc = myVWorldMapper.getHumanoidRenderContext();
			// New with "GlobalModes": we'll run hrc.setupHumanoidFigure from here now
			HumanoidFigure hf = hrc.getHumanoidFigureManager().setupHumanoidFigure(hrc, qi, myCharID, qGraph, hc);
			return (hf != null);
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param qi
	 * @param brc
	 * @param qGraph
	 * @param hc
	 * @param behavCE
	 * @return true  if the "boneRobot" is "OK".  That means it is animatable, but it may or may not have a VWorld humanoid figure.
	 * @throws Throwable 
	 */
	protected boolean initModelRobotUsingBoneRobotConfig(BoneRobotConfig brc) throws Throwable {

		if (brc != null) {
			// This creates our ModelRobot instance, and calls registerAndStart() in the RobotServiceContext base class.
			myMBRSC.makeModelRobotWithBlenderAndFrameSource(brc);
			return true;
		} else {
			return false;
		}
	}	
	protected boolean startVisemePump(List<ClassLoader> clsForRKConf)  {
		RobotVisemeClient robotVisCli = new RobotVisemeClient();
		BundleContext bunCtx = myMBRSC.getBundleContext();
		robotVisCli.startPumpingZenoAvatarVisemes(bunCtx, clsForRKConf);
		return true;
	}

	protected void updateModelRobotUsingBoneRobotConfig(BoneRobotConfig brc) throws Throwable {	
		ModelRobot targetRobot = getBonyRobot();
		boolean flag_hardResetGoalPosToDefault = false;
		targetRobot.updateConfig(brc, flag_hardResetGoalPosToDefault);
	}
	protected void connectToVirtualChar() throws Exception {
		final ModelRobot br = getBonyRobot();
		if (br == null) {
			getLogger().warn("connectToVirtualChar() aborting due to missing ModelRobot, for char: {}", myCharID);
			return;
		}
		final HumanoidFigure hf = getHumanoidFigure();
		if (hf != null) {
			// It is optional to create this state object if there is no humanoid figure to animate.
			// It could be used for some other programming purpose.
			FigureState fs = setupFigureState(br);
			hf.setFigureState(fs);
			br.registerMoveListener(new ModelRobot.MoveListener() {
				@Override public void notifyBonyRobotMoved(ModelRobot br) {
					HumanoidFigure hf = getHumanoidFigure();
					if (hf != null) {
						ModelToFigureStateMappingFuncs.propagateState(br, hf);
					}
				}
			});
		}
	}
	protected boolean connectBonyCharToRobokindSvcs(BundleContext bundleCtx, HumanoidConfig hc, Ident qGraph, RepoClient qi, BoneCN bqn, List<ClassLoader> clsForRKConf) throws Throwable {
		// We useta read from a TTL file with: 	boneRobotConf = readBoneRobotConfig(bonyConfigPathPerm, myInitialBonyRdfCL);
		BoneRobotConfig boneRobotConf = new BoneRobotConfig(qi, myCharID, qGraph, bqn); 	
		myBoneRobotConfigServiceRegistration = bundleCtx.registerService(BoneRobotConfig.class.getName(), boneRobotConf, null);
		//logInfo("Initializing new BoneRobotConfig: " + boneRobotConf.getFieldSummary()); // TEST ONLY
		boolean boneRobotOK = initModelRobotUsingBoneRobotConfig(boneRobotConf);
		if (boneRobotOK) {
			// This does nothing if there is no vWorld, or no human figure for this char in the vWorld.
			connectToVirtualChar();
			// This was an antiquated way of controlling initial char position, left here as reminder of the issue.
			// myPHM.applyInitialBoneRotations();
			startVisemePump(clsForRKConf);
			startJointGroup(hc, clsForRKConf);
			
		} else {
			getLogger().warn("connectBonyCharToRobokindSvcs() aborting due to failed boneRobot init, for charIdent: {}", myCharID);
		}
		return boneRobotOK;
	}
	protected void startJointGroup(HumanoidConfig hc, List<ClassLoader> possibleCLs) { 
		String jgFullPath = hc.myJointConfigPath;
		if (jgFullPath != null) {
			ClassLoader cl = ClassLoaderUtils.findResourceClassLoader(jgFullPath, possibleCLs);
			if (cl != null) {
				InputStream stream = cl.getResourceAsStream(jgFullPath);
				if (stream != null) {
					myMBRSC.startJointGroup(stream);
				}
			}
		}
	}

	
	protected HumanoidFigure getHumanoidFigure() {
		HumanoidFigure hf = null;
		if (myVWorldMapper != null) {
			HumanoidRenderContext hrc = myVWorldMapper.getHumanoidRenderContext();
			if (hrc != null) {
				hf = hrc.getHumanoidFigureManager().getHumanoidFigure(myCharID);
			}
		}
		return hf;
	}
	

	 
	private FigureState setupFigureState(ModelRobot br) { 

		FigureState fs = new FigureState();
		List<ModelJoint> allJoints = br.getJointList();
		for (ModelJoint mJoint : allJoints) {
			for (BoneProjectionRange bpr : mJoint.getBoneRotationRanges()) {
				String boneName = bpr.getBoneName();
				fs.obtainBoneState(boneName);
			}
		}
		return fs;
	}
	protected void registerFrameReceiver() throws Throwable { 
		ModelRobot mr = getBonyRobot();		
			/*
			 Robot.Id mrid = mr.getRobotId();		
			try {
		        RobotServiceFuncs.createAndRegisterFrameReceiver(bundleCtx, brid);
			} catch (Throwable t) {
				theLogger.warn("Could not register AMQP network server for robot with ID=" + brid, t);
			}
			 */
	}

}
