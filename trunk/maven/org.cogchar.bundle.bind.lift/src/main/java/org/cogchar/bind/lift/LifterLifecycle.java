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
package org.cogchar.bind.lift;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import org.appdapter.core.item.FreeIdent;
import org.appdapter.core.item.Ident;
import org.cogchar.blob.emit.QueryInterface;
import org.robokind.api.common.lifecycle.AbstractLifecycleProvider;
import org.robokind.api.common.lifecycle.DependencyDescriptor.DependencyType;
import org.robokind.api.common.lifecycle.utils.DescriptorBuilder;
import org.robokind.api.common.lifecycle.utils.DescriptorListBuilder;



/**
 *
 * @author Ryan Biggs
 */
public class LifterLifecycle extends AbstractLifecycleProvider<LiftAmbassador.LiftAmbassadorInterface, LiftAmbassador.inputInterface> {

	private final static Logger theLogger =	Logger.getLogger(LifterLifecycle.class.getName());
	
	static final Ident HOME_LIFT_CONFIG_IDENT = new FreeIdent("urn:ftd:cogchar.org:2012:runtime#mainLiftConfig", "mainLiftConfig");
	
	private final static String queryEmitterId = "queryInterface";
	private final static String theLiftAppInterfaceId = "liftAppInterface";
	private final static String theLiftSceneInterfaceId = "liftSceneInterface";
	

	static class OurDescriptorBuilder {
		static DescriptorListBuilder get() {
			DescriptorListBuilder dlb = new DescriptorListBuilder()
            	.dependency(queryEmitterId, QueryInterface.class);				
			dlb = new DescriptorBuilder(dlb, theLiftAppInterfaceId, LiftAmbassador.LiftAppInterface.class, DependencyType.OPTIONAL);
			dlb = new DescriptorBuilder(dlb, theLiftSceneInterfaceId, LiftAmbassador.LiftSceneInterface.class, DependencyType.OPTIONAL);
			return dlb;
		}
	}
	
	public LifterLifecycle() {
		super(OurDescriptorBuilder.get().getDescriptors());
		
		if (myRegistrationProperties == null) {
			myRegistrationProperties = new Properties();
		}
		//myRegistrationProperties.put(Robot.PROP_ID, robotId.getRobtIdString()); // Not sure which ones we need yet
	}

	
	
	@Override
	protected synchronized LiftAmbassador.inputInterface create(Map<String, Object> dependencies) {
		theLogger.info("Creating LiftAmbassador.inputInterface in LifterLifecycle");
		LiftAmbassador.setAppInterface((LiftAmbassador.LiftAppInterface)dependencies.get(theLiftAppInterfaceId));
		LiftAmbassador.setSceneLauncher((LiftAmbassador.LiftSceneInterface)dependencies.get(theLiftSceneInterfaceId));
		connectWebContent((QueryInterface)dependencies.get(queryEmitterId));
		return new LiftAmbassador.inputInterface();
	}

	@Override
	protected void handleChange(String serviceId, Object dependency, Map<String, Object> availableDependencies) {
		//super.handleChange(name, dependency, availableDependencies); //Needed?
		if (queryEmitterId.equals(serviceId)) {
			connectWebContent((QueryInterface)dependency);
    	} else if (theLiftAppInterfaceId.equals(serviceId)){
        	LiftAmbassador.setAppInterface((LiftAmbassador.LiftAppInterface)dependency);
    	} else if (theLiftSceneInterfaceId.equals(serviceId)){
        	LiftAmbassador.setSceneLauncher((LiftAmbassador.LiftSceneInterface)dependency);
    	}
	}


	@Override
	public Class<LiftAmbassador.LiftAmbassadorInterface> getServiceClass() {
    	return LiftAmbassador.LiftAmbassadorInterface.class;
	}
	
	public void connectWebContent(QueryInterface qi) {
		// Provide queryInterface to LiftAmbassador so it can reload lift configs
		LiftAmbassador.setQueryInterface(qi);
		// Load web app "home" screen config
		LiftConfig lc = new LiftConfig(qi, HOME_LIFT_CONFIG_IDENT);
		LiftAmbassador.activateControlsFromConfig(lc);
		// Load "chat app" config
		ChatConfig cc = new ChatConfig(qi);
		LiftAmbassador.storeChatConfig(cc);
	}

}
