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
package org.cogchar.bind.rk.speech.client;

import org.cogchar.bind.rk.robot.client.RobotAnimClient;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.robokind.api.speech.SpeechService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class SpeechOutputClient {
	static Logger theLogger = LoggerFactory.getLogger(RobotAnimClient.class);
	BundleContext	myBundleCtx;
	public SpeechOutputClient(BundleContext bundleCtx){
		myBundleCtx = bundleCtx;
	}
    public void speakText (String txt) throws Throwable {
		theLogger.info("Trying to speakText: " + txt);
        if(txt == null){
             throw new Exception("Speech text is null");
        }
        ServiceReference ref = myBundleCtx.getServiceReference(SpeechService.class.getName());
		try {
	        if(ref == null){
		        throw new Exception("Cannot find Speech Service");
		    }
			Object serviceObj = myBundleCtx.getService(ref);
			if(serviceObj == null){
				throw new Exception ("SpeechService object is null");
			}
			SpeechService ss = (SpeechService) serviceObj;
			ss.speak(txt);
		} finally {
			if (ref != null) {
		        myBundleCtx.ungetService(ref);
			}
		}
       
    }	
}