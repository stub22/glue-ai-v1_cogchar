/*
 * Copyright 2013 The Friendularity Project (www.friendularity.org).
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
package org.cogchar.bundle.temp.wire;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.appdapter.bind.rdf.jena.assembly.KnownComponentImpl;
import org.appdapter.core.matdat.BoundModelProvider;
import org.appdapter.core.matdat.EnhancedRepoClient;
import org.appdapter.core.matdat.ModelProviderFactory;
import org.appdapter.core.matdat.PipelineQuerySpec;
import org.appdapter.core.name.Ident;
import org.jflux.api.registry.Registry;
import org.jflux.impl.registry.OSGiRegistry;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.lifecycle.ServiceLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;

/**
 * Wires up the ServiceManager system, pulling in the data from the spreadsheet 
 * and building up the object.
 *
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */


public class ServiceManagerWiring {
    
    // Group key and QName for each kind of spec service
    public static final String GROUP_KEY_FOR_LIFECYCLE_SPEC =
            "LifecycleSpecGroupId";
    public static final String LIFECYCLE_GROUP_QN ="LifecycleGroup";

    public static final String GROUP_KEY_FOR_PROPERTY_SPEC =
            "PropertySpecGroupId";
    public static final String PROPERTY_GROUP_QN = "PropertyGroup";
    
    public static final String GROUP_KEY_FOR_REGISTRATION_STRATEGY_SPEC =
            "RegistrationStrategySpecGroupId";
    public static final String REGISTRATION_STRATEGY_GROUP_QN =
            "RegistrationStrategyGroup";
    
    public static final String GROUP_KEY_FOR_SERVICE_BINDING_SPEC =
            "ServiceBindingSpecGroupId";
    public static final String SERVICE_BINDING_GROUP_QN = "ServiceBindingGroup";
    
    public static final String GROUP_KEY_FOR_SERVICE_MANAGER_SPEC =
            "ServiceManagerSpecGroupId";
    public static final String SERVICE_MANAGER_GROUP_QN = "ServiceManagerGroup";
    
	public final static String  PIPELINE_GRAPH_QN = "csi:pipeline_sheet_22";
	public final static String  PIPE_QUERY_QN = "ccrt:find_pipes_77";
	public final static String  PIPE_SOURCE_QUERY_QN = "ccrt:find_pipe_sources_99";
	public final static PipelineQuerySpec myDefaultPipelineQuerySpec = new PipelineQuerySpec(
            PIPE_QUERY_QN, PIPE_SOURCE_QUERY_QN, PIPELINE_GRAPH_QN);
    
    /**
     * Load up the Specs for all the components needed to build the
     * serviceManagers. Build them and register them with the JFlux system.
     * Once these specs are available, the Extender can take over and build them
     * into their final objects.
     * 
     * @param context The bundle context to register them in
     * @param defaultDemoRepoClient The Repo client to pull RDF data from
     * @param lifecycleDefSheetQN The QName for the lifecycle definitions
     * @param serviceManagerSheetQN The QName for the serviceManager definitions
     */
    public static List<ManagedService> loadAndRegisterSpecs(
            BundleContext context,
            EnhancedRepoClient defaultDemoRepoClient,
            String lifecycleDefSheetQN,
            String serviceManagerSheetQN,
            String mergedSheetQN) {
        
        // This list provides access to all the services created in this process
        List<ManagedService> managedServicesForServiceManagerSpecs =
                new ArrayList();
        
		Ident derivedBehavGraphID =   defaultDemoRepoClient.makeIdentForQName(mergedSheetQN);
		BoundModelProvider derivedBMP = 
                ModelProviderFactory.makeOneDerivedModelProvider(
                        defaultDemoRepoClient, myDefaultPipelineQuerySpec, derivedBehavGraphID);
        
        Set<Object> allSpecs = derivedBMP.assembleModelRoots();

        // Load the lifecycles, triggering the ServiceLifecycleSpecBuilder 
//        Set<Object> lifecycleSheetSpecs =
//                loadSpecs(defaultDemoRepoClient, lifecycleDefSheetQN);
//        
        // Filter and Type check the specs
//        List<ServiceLifecycleSpec> lifecycleSpecs =
//                filterSpecs(ServiceLifecycleSpec.class, lifecycleSheetSpecs);
//        
//        // Register the filtered specs
//        for( ServiceLifecycleSpec lifecycleSheetSpec : lifecycleSpecs ) {
//            ManagedService<ServiceLifecycleSpec> lifecycleSpecManagedService = 
//                    registerSpec(
//                        context, 
//                        ServiceLifecycleSpec.class, 
//                        lifecycleSheetSpec, 
//                        GROUP_KEY_FOR_LIFECYCLE_SPEC, 
//                        LIFECYCLE_GROUP_QN);
//            managedServicesForServiceManagerSpecs.add(
//                    lifecycleSpecManagedService);
//        }
        
        // Now with the lifecycles registered and available, the serviceManagers
        // can be created.
        
        // Load the serviceManagers and thier components, triggering the related
        // builders
//        Set<Object> serviceManagerSheetSpecs = loadSpecs(
//                defaultDemoRepoClient, serviceManagerSheetQN);
//        
//        
//        Set<Object> lifecycleSheetSpecs2 =
//                loadSpecs(defaultDemoRepoClient, mergedSheetQN);
//        
//        Set<Object> serviceManagerSheetSpecs2 = loadSpecs(
//                defaultDemoRepoClient, mergedSheetQN);
        
        // Filter and Type check the specs
//        List<PropertySpec> propertySpecs =
//                filterSpecs(PropertySpec.class, allSpecs);
//        
//        List<DefaultRegistrationStrategySpec> registrationStrategySpecs =
//                filterSpecs(
//                    DefaultRegistrationStrategySpec.class,
//                    allSpecs);
//        
//        List<ServiceBindingSpec> serviceBindingSpecs =
//                filterSpecs(ServiceBindingSpec.class, allSpecs);
//        
        List<ServiceManagerSpec> serviceManagerSpecs = 
                filterSpecs(ServiceManagerSpec.class, allSpecs);
        
        // Register the filtered Specs
//        for( PropertySpec propertySpec : propertySpecs ) {
//            ManagedService<PropertySpec> propertySpecManagedService = 
//                    registerSpec(
//                        context,
//                        PropertySpec.class, 
//                        propertySpec, 
//                        GROUP_KEY_FOR_LIFECYCLE_SPEC, 
//                        LIFECYCLE_GROUP_QN);
//            managedServicesForServiceManagerSpecs.add(
//                    propertySpecManagedService);
//        }
//
//        for( DefaultRegistrationStrategySpec registrationStrategySpec :
//                registrationStrategySpecs ) {
//            
//            ManagedService<DefaultRegistrationStrategySpec>
//                    registrationStrategySpecManagedService = 
//                        registerSpec(
//                            context,
//                            DefaultRegistrationStrategySpec.class, 
//                            registrationStrategySpec, 
//                            GROUP_KEY_FOR_LIFECYCLE_SPEC, 
//                            LIFECYCLE_GROUP_QN);
//            managedServicesForServiceManagerSpecs.add(
//                    registrationStrategySpecManagedService);
//        }
//        
//        for( ServiceBindingSpec serviceBindingSpec : serviceBindingSpecs ) {
//            ManagedService<ServiceBindingSpec> serviceBindingSpecManagedService= 
//                    registerSpec(
//                        context,
//                        ServiceBindingSpec.class, 
//                        serviceBindingSpec, 
//                        GROUP_KEY_FOR_LIFECYCLE_SPEC, 
//                        LIFECYCLE_GROUP_QN);
//            managedServicesForServiceManagerSpecs.add(
//                    serviceBindingSpecManagedService);
//        }

        for( ServiceManagerSpec serviceManagerSpec : serviceManagerSpecs ) {
            ManagedService<ServiceManagerSpec> serviceManagerSpecManagedService= 
                    registerSpec(
                        context,
                        ServiceManagerSpec.class, 
                        serviceManagerSpec, 
                        GROUP_KEY_FOR_LIFECYCLE_SPEC, 
                        LIFECYCLE_GROUP_QN);
            managedServicesForServiceManagerSpecs.add(
                    serviceManagerSpecManagedService);
        }
        
        
        // Return the managedServices for the specs that are used to build up
        // the service managers.
        return managedServicesForServiceManagerSpecs;
    }
    
    /**
     * Launches the serviceManagerSpecExtender, which handles the gritty details
     * of creating the actual ServiceManager object (finally!).
     * 
     * @param bundleCtx The bundle context
     * @param optionalSpecFilter A filter to apply to the specs, optional
     * @return The created extender
     */
    public static ServiceManagerSpecExtender startSpecExtender(
            BundleContext bundleCtx, String optionalSpecFilter) {
        Registry reg = new OSGiRegistry(bundleCtx);
        ServiceManagerSpecExtender serviceManagerSpecExtender = new ServiceManagerSpecExtender(
                bundleCtx, reg, optionalSpecFilter);
        serviceManagerSpecExtender.start();
        return serviceManagerSpecExtender;
    }
    
    /**
     * Collects the data from the repo, building it into untyped spec objects
     * 
     * @param defaultDemoRepoClient
     * @param graphQN
     * @return 
     */
    private static Set<Object> loadSpecs(
            EnhancedRepoClient defaultDemoRepoClient, String graphQN) {
        // Determine the URI for the 'qualified name' which identifies the data 
        // in the repo
//        Ident graphID = defaultDemoRepoClient.makeIdentForQName(graphQN);
        
        // Collect the objects from the repo, building them from RDF raw data
        return defaultDemoRepoClient.assembleRootsFromNamedModel(graphQN);
    }
    
    /**
     * Filters each of the matching specs out of the raw list, returning a typed
     * spec object.
     * 
     * @param <T> The type of the spec, such as ConnectionSpec
     * @param clazz The class of the spec, such as ConnectionSpec.class
     * @param rawSpecs The list of raw specs to be filtered
     * @return The List of filtered & typed specs of the kind requested
     */
    private static <T> List<T> filterSpecs(
            Class<T> clazz, Set<Object> rawSpecs) {
        List<T> specs = new ArrayList();
        for (Object root : rawSpecs) {
            // Ignore anything that is not of type T
            if (root == null || 
                    !clazz.isAssignableFrom(root.getClass())) {
                continue;
            }
            specs.add((T) root);
        }
        return specs;
    }
    
    /**
     * Register the spec with the JFlux system.
     * 
     * @param <T> The type of the spec, such as ConnectionSpec
     * @param context The bundle context to register the spec with
     * @param spec The spec to be registered
     * @param specClass The class of the spec, such as ConnectionSpec.class
     * @param GroupKey
     * @param GroupQN
     * @return The managedService for the resulting registration.
     */
    private static <T extends KnownComponentImpl> ManagedService<T> registerSpec(
            BundleContext context,
            Class<T> specClass,
            T spec,
            String GroupKey,
            String GroupQN) {
        
        // Build up the properties for the service
        Properties props = new Properties();
        props.put(GroupKey, GroupQN);
        
        ServiceLifecycleProvider<T> lifecycle =
                new SimpleLifecycle<T>(spec, specClass);
        
        ManagedService<T> ms = new OSGiComponent<T>(context, lifecycle, props);
        ms.start();
        return ms;
    }
}
