package org.cogchar.bundle.app.vworld.startup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.appdapter.core.log.BasicDebugger;

import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.cogchar.app.puma.config.PumaContextMediator;
import org.cogchar.bundle.app.vworld.central.VWorldMapperLifecycle;

import org.jflux.api.registry.basic.BasicDescriptor;
import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceLifecycle;
import org.jflux.api.service.binding.ServiceBinding;

import org.jflux.impl.registry.OSGiRegistry;
import org.jflux.api.service.ServiceManager;

public class Activator extends BasicDebugger implements BundleActivator {

    public void start(BundleContext context) throws Exception {

       // startVWorldLifecycle(context);
    }

    public void stop(BundleContext context) throws Exception {
        // TODO add deactivation code here
    }

//    private void startVWorldLifecycle(BundleContext context) {
//        Map<String, ServiceBinding> bindings = new HashMap<String, ServiceBinding>();
//        VWorldMapperLifecycle vMapLifecycle = new VWorldMapperLifecycle();
//
//        getBindings(bindings, vMapLifecycle);
//
//        OSGiRegistry registry = new OSGiRegistry(context);
//
//        ServiceManager serviceManager =
//                new ServiceManager(
//                vMapLifecycle,
//                bindings,
//                Collections.EMPTY_MAP,
//                null);
//
//        serviceManager.start(registry);
//    }
//
//    private static Map getBindings(Map<String, ServiceBinding> bindings, ServiceLifecycle l) {
//
//        Map<String, String> clProps = new HashMap<String, String>();
//        clProps.put("classLoader", "classLoader");
//        BasicDescriptor clDescriptor =
//                new BasicDescriptor(
//                ClassLoader.class.getName(),
//                clProps);
//
//        ServiceBinding clBinding = new ServiceBinding(
//                (ServiceDependency) l.getDependencySpecs().get(0),
//                clDescriptor,
//                ServiceBinding.BindingStrategy.LAZY);
//
//
//        bindings.put("classLoader", clBinding);
//
//        Map<String, String> configProps = new HashMap<String, String>();
//        configProps.put("bodyConfigSpec", "bodyConfigSpec");
//        BasicDescriptor configDepDescriptor =
//                new BasicDescriptor(
//                ArrayList.class.getName(),
//                configProps);
//
//        ServiceBinding configBinding = new ServiceBinding(
//                (ServiceDependency) l.getDependencySpecs().get(2),
//                configDepDescriptor,
//                ServiceBinding.BindingStrategy.LAZY);
//
//
//        bindings.put("bodyConfigSpec", configBinding);
//
//        Map<String, String> mediatorProps = new HashMap<String, String>();
//        mediatorProps.put("pumaMediator", "pumaMediator");
//        BasicDescriptor mediatorDescriptor =
//                new BasicDescriptor(
//                PumaContextMediator.class.getName(),
//                mediatorProps);
//
//        ServiceBinding mediatorBinding = new ServiceBinding(
//                (ServiceDependency) l.getDependencySpecs().get(1),
//                mediatorDescriptor,
//                ServiceBinding.BindingStrategy.LAZY);
//
//
//        bindings.put("pumaMediator", mediatorBinding);
//
//
//        Map<String, String> regrProps = new HashMap<String, String>();
//        mediatorProps.put("classLoader", "classLoader");
//        BasicDescriptor regDescriptor =
//                new BasicDescriptor(
//                PumaRegistryClient.class.getName(),
//                regrProps);
//
//        ServiceBinding regBinding = new ServiceBinding(
//                (ServiceDependency) l.getDependencySpecs().get(3),
//                regDescriptor,
//                ServiceBinding.BindingStrategy.LAZY);
//
//
//        bindings.put("theRegistryClient", regBinding);
//
//        return bindings;
//    }
}
