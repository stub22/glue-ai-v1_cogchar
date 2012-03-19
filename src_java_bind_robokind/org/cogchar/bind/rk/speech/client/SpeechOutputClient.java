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
import org.cogchar.api.perform.TextChannel;
import org.cogchar.api.perform.Performance;
import org.cogchar.api.perform.BasicPerformance;

import org.cogchar.impl.perform.FancyTextChan;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.robokind.api.speech.SpeechService;
import org.appdapter.api.module.Module.State;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class SpeechOutputClient extends FancyTextChan {
	static Logger theLogger = LoggerFactory.getLogger(SpeechOutputClient.class);
	BundleContext	myBundleCtx;

	
	public SpeechOutputClient(BundleContext bundleCtx) {
		super("SpeechOut_" + System.currentTimeMillis() % 100000);
		myBundleCtx = bundleCtx;
	}
	public class ServiceContext  {
		public	ServiceReference	serviceRef; 
		public	SpeechService		speechService;
		public  void release() {
			if (serviceRef != null) {
				myBundleCtx.ungetService(serviceRef);
			}
		}
		public void speak() { 
		}
		
	}
	public ServiceContext lookupSpeechService() throws Throwable {
		ServiceContext servCtx = new ServiceContext();
		servCtx.serviceRef = myBundleCtx.getServiceReference(SpeechService.class.getName());
		if(servCtx.serviceRef != null){
			try {
				Object serviceObj = myBundleCtx.getService(servCtx.serviceRef);
				if(serviceObj != null){
					servCtx.speechService = (SpeechService) serviceObj;
				}
			} finally {
				if (servCtx.speechService == null) {
					servCtx.release();
					return null;
				}
			}
			return servCtx;
		} else {
			return null;
		}
	}
	
    public void speakText (String txt) throws Throwable {
		theLogger.info("Trying to speakText: " + txt);
        if(txt == null){
             throw new Exception("Speech text is null");
        }
		ServiceContext servCtx = lookupSpeechService();
		if (servCtx != null) {
			try {
				if (servCtx != null) {
					servCtx.speechService.speak(txt);
				}
			} finally {
				servCtx.release();
			}
		}
    }
	@Override  public void startTextPerformance (String txt) throws Throwable {
		speakText(txt);
	}

}
