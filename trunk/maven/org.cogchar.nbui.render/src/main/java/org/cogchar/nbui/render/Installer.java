/*
 * Copyright 2011 Hanson Robokind LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cogchar.nbui.render;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.log4j.PropertyConfigurator;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Installer extends ModuleInstall {
	static Logger theLogger = LoggerFactory.getLogger(Installer.class);
	static String LOG4J_PROPS_PATH = "config/cogchar/logging_temp/log4j_cogchar_dev.properties";
	static String VIRTCHAR_NB_MODULE_DIR = "org_cogchar_nbui_render";
	
    @Override public void restored() {
        logInfo(".restored() - BEGIN");
        

        
        File file = InstalledFileLocator.getDefault().locate(LOG4J_PROPS_PATH, VIRTCHAR_NB_MODULE_DIR, false);
		logInfo("InstalledFileLocator resolved path[" + LOG4J_PROPS_PATH + "] in module[" + VIRTCHAR_NB_MODULE_DIR + "] to " + file.getAbsolutePath());
        try{
            URL localURL = file.toURI().toURL();
            logInfo("Forcing Log4J to read config from: " + localURL);
            PropertyConfigurator.configure(localURL);
            logInfo("Is SLF4J working under Netigso?");
        }catch(MalformedURLException ex){
            logInfo("Bad URL from file name: " + file.getAbsoluteFile());
			logInfo("Cannot start SLF4J Logging.");
            ex.printStackTrace();
        }
    }
	private void logInfo(String msg) {
		String smsg = "[Simulator-Installer]-" + msg;
		System.out.println("[System.out]-" + getClass().getCanonicalName() + "-" + smsg);
		theLogger.info("[SLF4J]" + smsg);
	}
}
