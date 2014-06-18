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

package org.cogchar.bind.mio.robot.client;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import org.appdapter.core.log.BasicDebugger;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.lifecycle.utils.ManagedServiceFactory;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponentFactory;
import org.jflux.api.common.rk.utils.RKConstants;
import org.mechio.api.motion.Robot;
import org.mechio.integration.motion_speech.VisemeMotionUtils;
import org.appdapter.core.name.Ident;
import org.jflux.impl.services.rk.lifecycle.utils.ManagedServiceGroup;

import org.cogchar.platform.util.ClassLoaderUtils;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class RobotVisemeClient extends BasicDebugger {

	public  void startPumpingZenoAvatarVisemes(BundleContext bunCtx, List<ClassLoader> clsForRKConf, Robot.Id robotId) { 
		String visConfResPath = "rk_conf/VisemeConf_AZR50_A12.json";
		try {
			startPumpingVisemeAnimation(bunCtx, null, null, visConfResPath, clsForRKConf, robotId);
		} catch (Throwable t) {
			getLogger().error("Problem starting Zeno viseme-pump with conf path " + visConfResPath , t);
		}
	}
	public  void startPumpingVisemeAnimation(BundleContext bunCtx, Ident speechOutChanID, Ident charBodyChanID,
					String visConfResPath, List<ClassLoader> clsForRKConf, Robot.Id robotId) throws Throwable { 
		// As of 2012-11-23, the code below is based on sample code shown in comments of:
		// org.mechio.integration.motion_speech.Activator
		ManagedServiceFactory fact = new OSGiComponentFactory(bunCtx);
		String speechServiceId = RKConstants.DEFAULT_SPEECH_ID;
		// Old way required an actual filesystem file, i.e. java.io.File (or path to same)
		// String filePath = "path/to/VisemeConf.json";
		// VisemeMotionUtils.startVisemeFrameSourceGroup(fact, new Robot.Id(RKConstants.VIRTUAL_R50_ID), speechServiceId, filePath);
		// New awesome-r way allows for any input stream.  We typically use a classpath resource.
		
		URL visConfResURL = ClassLoaderUtils.findResourceURL(visConfResPath, clsForRKConf);
		InputStream visemeConfigStream = visConfResURL.openStream();
		getLogger().info("Opened viseme conf stream from " + visConfResURL.toExternalForm());
		ManagedServiceGroup visFrameSourceServGroup = VisemeMotionUtils.startVisemeFrameSourceStreamGroup(fact, 
						robotId, speechServiceId, visemeConfigStream);
	}
}
