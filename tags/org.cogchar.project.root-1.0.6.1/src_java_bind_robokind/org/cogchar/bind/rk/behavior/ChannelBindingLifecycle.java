/*
 * Copyright 2013 The Cogchar Project (www.cogchar.org).
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
package org.cogchar.bind.rk.behavior;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.cogchar.api.channel.Channel;

/*
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.perform.PerfChannel;
import org.cogchar.api.perform.Media;
import org.cogchar.bind.rk.robot.client.RobotAnimContext;
import org.cogchar.bind.rk.speech.client.SpeechOutputClient;
import org.cogchar.blob.emit.BehaviorConfigEmitter;
import org.cogchar.platform.util.ClassLoaderUtils;
import org.osgi.framework.BundleContext;
import org.robokind.api.animation.player.AnimationPlayer;
*/
import org.robokind.api.common.lifecycle.AbstractLifecycleProvider;
import org.robokind.api.common.lifecycle.DependencyDescriptor;
import org.robokind.api.common.lifecycle.utils.DescriptorBuilder;
import org.robokind.api.common.lifecycle.utils.DescriptorListBuilder;
// import org.robokind.api.speech.SpeechService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import org.osgi.framework.FrameworkUtil;

/**
 * A channelBindingLifecycle manages the service lifecycle for a Channel object.
 * The channel is identified by a URI, and defined by properties in a ChannelBindingConfig,
 * which is independently obtained, but usually comes from a Cogchar RDF-backed FancyChannelSpec.
 * 
 * TODO:  Capture formal unit tests that build ChannelBindingConfigs manually and test them
 * in different JFlux contexts.
 * 
 * @author Matthew Stevenson <www.robokind.org> 
 * @author Stub22
 */
public class ChannelBindingLifecycle extends AbstractLifecycleProvider<Channel, Channel> {
	// A channel depends on up to one "service" object dependency, supplied by the lifecycle system.
	// The config object describes this dependency (as class + filterSTring), as well as the URI 
	// property of the channel (which is used by lifecycles that want to depend on us, in *their* 
	// filter strings, e.g. in performance scenes that will only instantiate if all their REQUIRED
	// channels are present).  
	protected static String SERVICE_DEP_KEY = "serviceDep";
	public static String URI_PROPERTY_NAME = "URI";
	private static Logger theLogger =  LoggerFactory.getLogger(ChannelBindingLifecycle.class);
    private ChannelBindingConfig myBindingConfig;
    // Superclass takes 
	//  List<DependencyDescriptor(String dependencyName,   Class clazz, String filter)>
	// DescriptorListBuilder can add DescriptorBuilder dependency(String name, Class clazz){
    public ChannelBindingLifecycle(List<DependencyDescriptor> depDescList, ChannelBindingConfig conf) {
		// The super-init takes care of our upstream dependency registration.
		// Used to have a complex expression here that offers no diagnostic help on a NullPointerException stack trace.
        super(depDescList); 
        myBindingConfig = conf;
        myRegistrationProperties = new Properties();
		// Here we register our URI property for use by *downstream* lifecycles.
        myRegistrationProperties.put(URI_PROPERTY_NAME, conf.getChannelURI());
    }
    
    @Override protected Channel create(Map<String, Object> dependencies) {
        Object serviceDepObj = dependencies.get(SERVICE_DEP_KEY);
		theLogger.info("Got serviceDepObj object: {}", serviceDepObj);
		CCRK_ServiceChannelFactory factory = new CCRK_ServiceChannelFactory();		
		Channel channel = factory.makeServiceChannel(myBindingConfig, serviceDepObj);
        return channel;
    }

    @Override protected void handleChange(String dependencyKey, Object dependency, Map<String, Object> availableDependencies) {
        myService = isSatisfied() ? create(availableDependencies) : null;
    }

    @Override protected Class<Channel> getServiceClass() {
        return Channel.class;
    }
    
}
