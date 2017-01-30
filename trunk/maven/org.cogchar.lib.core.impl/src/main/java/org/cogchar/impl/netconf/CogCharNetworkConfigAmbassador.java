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
package org.cogchar.impl.netconf;

import org.cogchar.impl.web.config.WebappNetworkConfigHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.friendularity.bundle.netconfig.LinuxNetworkConfigurator;


/**
 * @author Ryan Biggs <rbiggs@skyriversoftware.com>
 */
public class CogCharNetworkConfigAmbassador implements WebappNetworkConfigHandle {

	private static final Logger theLogger = LoggerFactory.getLogger(CogCharNetworkConfigAmbassador.class);

	@Override
	public void configure(String ssid, String security, String key) {
		try {
			theLogger.info("Configuring network with SSID: {} and security type: {}", ssid, security);
			WiFiSecurity securityType = WiFiSecurity.valueOf(security);
			NetworkConfigurator configurator = new LinuxNetworkConfigurator();
			configurator.configureNetwork(new NetworkConfig(ssid, securityType, key));
		} catch (Exception e) {
			theLogger.warn("Could not match provided input \"{}\" with a valid security type", security);
		}
	}

}
