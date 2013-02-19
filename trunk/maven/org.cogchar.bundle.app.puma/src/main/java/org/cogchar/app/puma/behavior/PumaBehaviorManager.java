/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.app.puma.behavior;

import java.util.ArrayList;
import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.appdapter.core.name.Ident;
import org.cogchar.app.puma.config.PumaContextMediator;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.app.puma.behavior.PumaBehaviorAgent;
import org.cogchar.app.puma.body.PumaBodyGateway;
import org.cogchar.app.puma.body.PumaDualBody;
import org.cogchar.bind.rk.robot.svc.*;
import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.osgi.framework.BundleContext;
/**
 * @author Stu B. <www.texpedient.com>
 */

public class PumaBehaviorManager extends BasicDebugger {
	private List<PumaBehaviorAgent>		myBehaviorAgentList = new ArrayList<PumaBehaviorAgent>();
	
	private	BehaviorConfigEmitter	myBehavCE;
	
	public void init(PumaRegistryClient prc) { 
		PumaContextMediator mediator = prc.getCtxMediator(null);
		myBehavCE = new BehaviorConfigEmitter();
		String sysContextURI = mediator.getSysContextRootURI();
		if (sysContextURI != null) {
			myBehavCE.setSystemContextURI(sysContextURI);
		}
		String filesysRootPath = mediator.getOptionalFilesysRoot();
		if (filesysRootPath != null) {
			myBehavCE.setLocalFileRootDir(filesysRootPath);
		}		
	}
	
	public void makeAgentForBody(BundleContext bunCtx, PumaRegistryClient pRegCli, PumaDualBody pdb, Ident agentID) { 
		
		PumaBehaviorAgent pbAgent = new PumaBehaviorAgent(agentID, myBehavCE);
		pbAgent.initMappers(pRegCli);
		PumaBodyGateway pBodGate = pdb.getBodyGateway();
		if (pBodGate != null) {
			RobotServiceContext  robotSvcCtx = pBodGate.getRobotServiceContext();
			if (robotSvcCtx != null) {
				getLogger().info("Connecting RobotServiceContext for agent {}", agentID);
				pbAgent.connectRobotServiceContext(robotSvcCtx);
			}
		}
		String chanGraphQN =  "ccrt:chan_sheet_AZR50",  behavGraphQN  =  "hrk:behav_file_44";
		pbAgent.setupAndStart(bunCtx, pRegCli, chanGraphQN, behavGraphQN);
		
		myBehaviorAgentList.add(pbAgent);
	}
	public void stopAllAgents() {
		for (PumaBehaviorAgent pba : myBehaviorAgentList) {
			pba.stopEverything();
		}
	}
}
