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

    @Override
    public void restored() {
        String startupMsg = getClass().getCanonicalName();
		System.out.println("[System.out]" + startupMsg);
		theLogger.info("[SLF4J]" + startupMsg);
        
		String resPath = "logging/log4j.properties";
        String moduleName = "org_cogchar_nbui_render";
        
        File file = InstalledFileLocator.getDefault().locate(resPath, moduleName, false);
		System.out.println("InstalledFileLocator resolved " + resPath + " to " + file.getAbsolutePath());
        try{
            URL localURL = file.toURI().toURL();
            System.out.println("[System.out] " + getClass().getCanonicalName() + " is forcing Log4J to read config from: " + localURL);
            PropertyConfigurator.configure(localURL);
            theLogger.info("[SLF4J]" + startupMsg);
            theLogger.info("Is SLF4J->Log4J logging working?");
        }catch(MalformedURLException ex){
            System.out.println("Bad URL from file name: " + file.getAbsoluteFile());
            System.out.println("Cannot start SLF4J Logging.");
            ex.printStackTrace();
        }
    }
}
