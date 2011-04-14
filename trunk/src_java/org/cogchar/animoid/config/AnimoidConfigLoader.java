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

package org.cogchar.animoid.config;

import java.io.FileReader;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;


import org.cogchar.animoid.gaze.GazeStrategyCue;
import org.cogchar.animoid.protocol.Robot;
import org.cogchar.animoid.world.WorldJoint;


/**
 * @author Stu Baurmann
 */

public class AnimoidConfigLoader {
	public static String testFilename = 	"C:\\_hanson\\_mount\\jira_svn\\convoid_trunk\\temp_config\\msi_hk01\\_humankind\\bina\\animoid\\animoid_bina.xml";
	
	public static XStream buildDom4jXStreamForRead() {
		Dom4JDriver dom4jDriver = new Dom4JDriver();
		XStream xstream = new XStream(dom4jDriver);
		initConfigXStream(xstream);
		return xstream;
	}
	public static void initConfigXStream(XStream xstream) {
		xstream.alias("GazeJoint", GazeJoint.class);
		xstream.alias("GazeStrategy", GazeStrategyCue.class);
		xstream.alias("GazeJointStrategy", GazeJointStrategy.class);
		xstream.alias("AnimoidConfig", AnimoidConfig.class);
		xstream.alias("GlanceStrategy", GlanceStrategy.class);
		xstream.alias("FaceNoticeConfig", FaceNoticeConfig.class);
		xstream.alias("StereoGazeConfig", StereoGazeConfig.class);
		xstream.alias("FreckleMatchConfig", FreckleMatchConfig.class);
		xstream.alias("AnimationBlendConfig", AnimationBlendConfig.class);

		
		xstream.addImplicitCollection(AnimoidConfig.class, "myGazeStrategies", GazeStrategyCue.class);
		xstream.addImplicitCollection(AnimoidConfig.class, "myGazeJoints", GazeJoint.class);
		xstream.addImplicitCollection(GazeStrategyCue.class, "myJointLinks", GazeJointStrategy.class);
		xstream.useAttributeFor(GazeJoint.class, "positiveDirection");
		xstream.useAttributeFor(WorldJoint.class, "logicalJointID");
		xstream.useAttributeFor(WorldJoint.class, "rangeOfMotionDegrees");
		xstream.useAttributeFor(GazeStrategyCue.class, "name");
		xstream.useAttributeFor(GazeStrategyCue.class, "motionStyle");
		xstream.useAttributeFor(GazeJointStrategy.class, "logicalJointID");
		xstream.useAttributeFor(ViewPort.class, "widthPixels");
		xstream.useAttributeFor(ViewPort.class, "widthDegrees");
		xstream.useAttributeFor(ViewPort.class, "heightPixels");
		xstream.useAttributeFor(ViewPort.class, "heightDegrees");
		xstream.useAttributeFor(ViewPort.class, "azSkewDegrees");
		xstream.useAttributeFor(ViewPort.class, "elSkewDegrees");
			
		xstream.aliasField("GlanceStrategy", GazeStrategyCue.class, "glanceStrategy");
		xstream.aliasField("ViewPort", AnimoidConfig.class, "myViewPort");
		xstream.aliasField("FaceNoticeConfig", AnimoidConfig.class, "myFaceNoticeConfig");		
		xstream.aliasField("StereoGazeConfig", AnimoidConfig.class, "myStereoGazeConfig");
		xstream.aliasField("FreckleMatchConfig", AnimoidConfig.class, "myFreckleMatchConfig");
		xstream.aliasField("AnimationBlendConfig", AnimoidConfig.class, "myAnimationBlendConfig");
		/* Switched to elements for these to facilitate commenting in the XML
		xstream.useAttributeFor(FaceNoticeConfig.class, "initialStrength");
		...
		*/
	}
	public static AnimoidConfig loadAnimoidConfig(String filename, Robot mainRobot,
				Integer msecPerFrame, Double frameSmoothingFactor) throws Throwable {

		XStream xstream = buildDom4jXStreamForRead();
		FileReader fread = new FileReader(filename);
		AnimoidConfig animoidConfig = (AnimoidConfig) xstream.fromXML(fread);
		if (mainRobot != null) {
			animoidConfig.completeInit(mainRobot, msecPerFrame, frameSmoothingFactor);
		}
		return animoidConfig;
	}
	
	public static void main(String[] args) {
		try { 
			AnimoidConfig animoidConfig = loadAnimoidConfig(testFilename, null, 100, 1.3);
			
			System.out.println("Loaded animoidConfig: " + animoidConfig);
		} catch (Throwable t) {
			System.err.println("Caught: " + t);
			t.printStackTrace();
		}
	}
}
