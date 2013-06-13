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
import org.appdapter.help.repo.RepoClient;
import org.cogchar.api.channel.Channel;

import org.robokind.api.common.lifecycle.AbstractLifecycleProvider;
import org.robokind.api.common.lifecycle.DependencyDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	
	// The main problem we still have is with graph lookup within the channel itself, 
	// illustrated by these approaches to solution.
	
	// PROPOSAL 1  A channel may also depend on a RepoClient.  This makes channels essentially "more powerful"
	// in their own right, which is certainly convenient but has its downside.  We would need to add some
	// additional dependency+filter logic to the Channel hierarchy.    Currently we have a partial workaround
	// version of this implementation.

	// Current workaround as of 2013-06-10:  CCRK_ServiceFactory has access to a myWorkaroundRepoClient, 
	// which it plugs into our downstream perf channels as a crude version of PROPOSAL 1.  

	// PROPOSAL 2:  Any GraphChannel may easily depend on a RepoClient as its one service, and other channels
	// may easily find that GraphChannel.   The easilyFind part is not yet implemented; it would be
	// a lookup operation in ChannelSpace, probably using the GraphChannelHub AKA RepoFabric. 
	// Here we would be more completely depending on the BehaviorSystem wiring, which is probably good
	// for coherence of the total system.  This approach gives more control to dynamic soft-channel wiring for
	// playing Scenes vs. the pre-wired channels, which more deeply embed the application in proposal #1 above.  
	
	
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
	
	private static CCRK_ServiceChannelFactory theWorkaroundSCF = new CCRK_ServiceChannelFactory();
	
	public static void setTheWorkaroundRepoClient(RepoClient repoClient) {
		theWorkaroundSCF.myWorkaroundRepoClient = repoClient;
	}
    protected CCRK_ServiceChannelFactory getServiceChannelFactory() {
		return theWorkaroundSCF;
	}

    @Override protected Channel create(Map<String, Object> dependencies) {
        Object serviceDepObj = dependencies.get(SERVICE_DEP_KEY);
		theLogger.info("Got serviceDepObj object: {}", serviceDepObj);
		CCRK_ServiceChannelFactory servChanFactory = getServiceChannelFactory();
		Channel channel = servChanFactory.makeServiceChannel(myBindingConfig, serviceDepObj);
        return channel;
    }

    @Override protected void handleChange(String dependencyKey, Object dependency, Map<String, Object> availableDependencies) {
        myService = isSatisfied() ? create(availableDependencies) : null;
    }

    @Override protected Class<Channel> getServiceClass() {
        return Channel.class;
    }
    
}
