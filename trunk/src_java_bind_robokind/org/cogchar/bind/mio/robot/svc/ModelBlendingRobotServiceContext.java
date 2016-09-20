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
package org.cogchar.bind.mio.robot.svc;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cogchar.api.skeleton.config.BoneRobotConfig;
import org.cogchar.bind.mio.robot.model.ModelRobot;
import org.cogchar.bind.mio.robot.model.ModelRobotFactory;
import org.jflux.api.core.config.Configuration;
import org.jflux.impl.messaging.rk.config.RKMessagingConfigUtils;
import org.mechio.api.motion.Robot;
import org.osgi.framework.BundleContext;

import org.jflux.impl.services.rk.lifecycle.config.RKLifecycleConfigUtils.GenericLifecycleFactory;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponentFactory;
import static org.cogchar.bind.mio.osgi.MechIOBindingConfigUtils.*;
import org.jflux.impl.services.rk.lifecycle.ManagedService;


/**
 * @author Stu B. <www.texpedient.com>
 */
public class ModelBlendingRobotServiceContext extends BlendingRobotServiceContext<ModelRobot> {
	
	// A static list of all the ManagedServices registered via RKMessagingConfigUtils.registerConnectionConfig
	// We'll need this to clear 'em all out on "mode" change
	private static List<ManagedService> registeredConnectionConfigServices = new ArrayList<ManagedService>();
	

	public ModelBlendingRobotServiceContext(BundleContext bundleCtx) {
		super(bundleCtx);
	}

	public void makeModelRobotWithBlenderAndFrameSource(BoneRobotConfig config ) throws Throwable {
		getLogger().info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& START makeModelRobot__ for robot {} ",  config.myRobotName);		
		getLogger().debug("BoneRobtConfig details: {} ",  config);
		//Create your Robot and register it
		ModelRobot br = ModelRobotFactory.buildRobot(config);
        if(br == null){
            getLogger().warn("Error building ModelRobot from config {}",  config);
            return;
        }
		getLogger().info("Fetching connectionConf = getValue(Configuration.class, MSGCONF_ROBOT_HOST [={}])", MSGCONF_ROBOT_HOST);
        Configuration<String> connectionConf = getValue(Configuration.class, MSGCONF_ROBOT_HOST);

		getLogger().info("Fetched connectionConf={}, and myBundleCtx.bundle={}", connectionConf, myBundleCtx.getBundle());
		Properties noProps = null;
		OSGiComponentFactory osgiCompFactory = new OSGiComponentFactory(myBundleCtx);
		getLogger().info("Made OSGiComponentFactory={}, now calling RKMessagingConfigUtils.registerConnectionConfig.", osgiCompFactory);
		// Note that as of 2016-09-20, this step is what triggers the first wave of lifecycle dependency matches,
		// and thus leads to our first logged messages from the JFlux+OSGi Service Manager threads.
        ManagedService connectionConfigService = RKMessagingConfigUtils.registerConnectionConfig(MSGCONF_ROBOT_HOST,
						connectionConf,	noProps, osgiCompFactory); // new OSGiComponentFactory(myBundleCtx));
		getLogger().info("RKMessagingConfigUtils.registerConnectionConfig returned connConfigSvc={}", connectionConfigService);
		registeredConnectionConfigServices.add(connectionConfigService);
		registerAndStart(br, MSGCONF_ROBOT_HOST);
		getLogger().info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& END makeModelRobotWithBlenderAndFrameSource ");
	}	
	

	// Old test method, currently unused.
	@Deprecated public void registerDummyModelRobot() throws Throwable {
		logInfo("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& START registerDummyBlendingRobot");
		//Create your Robot and register it
		Robot.Id hbID = new Robot.Id("temp"); // HARDCODED_DUMMY_ROBOT_ID);
		ModelRobot br = new ModelRobot(hbID);
        Configuration<String> connectionConf = getValue(Configuration.class, MSGCONF_ROBOT_HOST);
        new OSGiComponent(myBundleCtx, 
                new GenericLifecycleFactory().adapt(connectionConf)).start();
		//BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JTwentyTwo", 0.5, 0.2);
		//BonyRobotUtils.makeBonyJointForRobot(myBonyRobot, 22, "JNinetyNine", 0.8, 0.9);
		registerAndStart(br, MSGCONF_ROBOT_HOST);
		logInfo("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& END registerDummyBlendingRobot");
	}
	
	public static void clearRobots() {
		BlendingRobotServiceContext.clearRobots();
		for (ManagedService connectionConfigSvc : registeredConnectionConfigServices) {
			connectionConfigSvc.dispose();
		}
		registeredConnectionConfigServices.clear();
	}

}
