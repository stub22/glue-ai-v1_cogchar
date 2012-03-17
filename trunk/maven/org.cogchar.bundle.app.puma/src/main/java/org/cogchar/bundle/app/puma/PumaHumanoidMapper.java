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
package org.cogchar.bundle.app.puma;

import org.cogchar.bind.rk.robot.svc.ModelBlendingRobotServiceContext;

import java.util.List;
import java.util.Set;
import org.osgi.framework.BundleContext;
import org.cogchar.bind.rk.robot.model.ModelRobot;
import org.cogchar.bind.rk.robot.model.ModelJoint;

import org.cogchar.render.opengl.bony.state.FigureState;
import org.cogchar.render.opengl.bony.demo.HumanoidRenderContext;
import org.cogchar.render.opengl.bony.model.HumanoidFigure;
import org.cogchar.bind.rk.robot.config.BoneProjectionRange;
import org.appdapter.bind.rdf.jena.model.AssemblerUtils;
import org.cogchar.bind.rk.robot.config.BoneRobotConfig;
import org.cogchar.bind.rk.robot.config.BoneProjectionRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import sun.net.www.http.Hurryable;
/**
 * @author Stu B. <www.texpedient.com>
 */
public class PumaHumanoidMapper {
	static Logger theLogger = LoggerFactory.getLogger(PumaHumanoidMapper.class);	
	
	private	ModelBlendingRobotServiceContext		myMBRSC;
	private	HumanoidRenderContext					myHRC;
	private	String									myCharURI;
	
	public PumaHumanoidMapper(HumanoidRenderContext brc, BundleContext bundleCtx, String charURI) {
		myHRC = brc;
		myMBRSC = new ModelBlendingRobotServiceContext(bundleCtx);
		myCharURI = charURI;
	}
	
	private void logInfo(String txt) { 
		theLogger.info(txt);
	}
	public HumanoidRenderContext getHumanoidRenderContext() { 
		return myHRC;
	}
	public ModelRobot getBonyRobot() { 
		return myMBRSC.getRobot();
	}
	public ModelBlendingRobotServiceContext getRobotServiceContext() { 
		return myMBRSC;
	}
	
	public void initModelRobotUsingBoneRobotConfig(BoneRobotConfig brc) throws Throwable {	
		myMBRSC.makeModelRobotWithBlenderAndFrameSource(brc);
	}
	public void updateModelRobotUsingBoneRobotConfig(BoneRobotConfig brc) throws Throwable {	
		ModelRobot targetRobot = getBonyRobot();
		targetRobot.updateConfig(brc);
	}

	
	public void connectToVirtualChar() throws Exception {
		setupFigureState();
		final ModelRobot br = getBonyRobot();
		br.registerMoveListener(new ModelRobot.MoveListener() {
			@Override public void notifyBonyRobotMoved(ModelRobot br) {
				HumanoidFigure hf = getHumanoidFigure();
				ModelToFigureStateMappingFuncs.propagateState(br, hf);
			}
			
		});
	}
	public HumanoidFigure getHumanoidFigure() { 
		return myHRC.getHumanoidFigure(myCharURI);
	}
	public void setupFigureState() { 
		ModelRobot br = getBonyRobot();
		FigureState fs = new FigureState();
		List<ModelJoint> allJoints = br.getJointList();
		for (ModelJoint mJoint : allJoints) {
			for (BoneProjectionRange bpr : mJoint.getBoneRotationRanges()) {
				String boneName = bpr.getBoneName();
				fs.obtainBoneState(boneName);
			}
		}
		HumanoidFigure hf = getHumanoidFigure();
		hf.setFigureState(fs);
	}
	public void registerFrameReceiver() throws Throwable { 
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
