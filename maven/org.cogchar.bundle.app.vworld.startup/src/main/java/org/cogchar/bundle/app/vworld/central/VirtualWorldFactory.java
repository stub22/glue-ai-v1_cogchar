package org.cogchar.bundle.app.vworld.central;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import java.util.Properties;
import org.cogchar.app.puma.config.PumaContextMediator;
import org.cogchar.app.puma.registry.PumaRegistryClient;
import org.jflux.api.registry.basic.BasicDescriptor;
import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceLifecycle;
import org.jflux.api.service.ServiceManager;
import org.jflux.api.service.binding.ServiceBinding;
import org.jflux.impl.registry.OSGiRegistry;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.cogchar.app.puma.boot.PumaAppContext;
import org.cogchar.app.puma.event.CommandEvent;

/**
 *
 * @author Major Jacquote II <mjacquote@gmail.com>
 * @author
 */
public class VirtualWorldFactory {

    public static BundleContext getBundleContext() {
        Bundle b = FrameworkUtil.getBundle(VWorldRegistry.class);
        if (b == null) {
            return null;
        }
        return b.getBundleContext();
    }

    public static VWorldRegistry getOSGiVWorldMapper(BundleContext context, Properties props) {
        String filterStr = OSGiUtils.createServiceFilter(props);
        ServiceReference ref;
        try {
            ServiceReference[] refs = context.getServiceReferences(VWorldRegistry.class.getName(), filterStr);
            if (refs == null || refs.length == 0) {
                return null;
            }
            ref = refs[0];
        } catch (InvalidSyntaxException ex) {
            return null;
        }
        if (ref == null) {
            return null;
        }
        VWorldRegistry vwr = OSGiUtils.getService(VWorldRegistry.class, context, ref);
        return vwr;
    }

    public static void startVWorldLifecycle(BundleContext context) {
        Map<String, ServiceBinding> bindings = new HashMap<String, ServiceBinding>();
        VWorldMapperLifecycle vMapLifecycle = new VWorldMapperLifecycle();

        getBindings(bindings, vMapLifecycle);

        OSGiRegistry registry = new OSGiRegistry(context);

        ServiceManager serviceManager =
                new ServiceManager(
                vMapLifecycle,
                bindings,
                Collections.EMPTY_MAP,
                null);
        serviceManager.start(registry);
    }

    private static Map getBindings(Map<String, ServiceBinding> bindings, ServiceLifecycle l) {

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
//        System.out.println("clBinding info: "+clBinding.toString());
//
//        bindings.put("classLoader", clBinding);

        Map<String, String> configProps = new HashMap<String, String>();
        configProps.put("bodyConfigSpec", "bodyConfigSpec");
        BasicDescriptor configDepDescriptor =
                new BasicDescriptor(
                ArrayList.class.getName(),
                configProps);

        ServiceBinding configBinding = new ServiceBinding(
                (ServiceDependency) l.getDependencySpecs().get(1),
                configDepDescriptor,
                ServiceBinding.BindingStrategy.LAZY);


        bindings.put("bodyConfigSpec", configBinding);

        Map<String, String> mediatorProps = new HashMap<String, String>();
        mediatorProps.put("pumaMediator", "pumaMediator");
        BasicDescriptor mediatorDescriptor =
                new BasicDescriptor(
                PumaContextMediator.class.getName(),
                mediatorProps);

        ServiceBinding mediatorBinding = new ServiceBinding(
                (ServiceDependency) l.getDependencySpecs().get(0),
                mediatorDescriptor,
                ServiceBinding.BindingStrategy.LAZY);


        bindings.put("pumaMediator", mediatorBinding);


        Map<String, String> regrProps = new HashMap<String, String>();
        regrProps.put("theRegistryClient", "theRegistryClient");
        BasicDescriptor regDescriptor =
                new BasicDescriptor(
                PumaRegistryClient.class.getName(),
                regrProps);

        ServiceBinding regBinding = new ServiceBinding(
                (ServiceDependency) l.getDependencySpecs().get(2),
                regDescriptor,
                ServiceBinding.BindingStrategy.LAZY);


        bindings.put("theRegistryClient", regBinding);


        Map<String, String> appContextProps = new HashMap<String, String>();
        regrProps.put("appContext", PumaAppContext.class.getName());
        BasicDescriptor appDescriptor =
                new BasicDescriptor(
                PumaAppContext.class.getName(),
                appContextProps);

        ServiceBinding appContextBinding = new ServiceBinding(
                (ServiceDependency) l.getDependencySpecs().get(2),
                appDescriptor,
                ServiceBinding.BindingStrategy.LAZY);


        bindings.put("appContextBinding", appContextBinding);
        
        Map<String, String> commandEventProps = new HashMap<String, String>();
        regrProps.put("commandEvent", CommandEvent.class.getName());
        BasicDescriptor commandEventDescriptor =
                new BasicDescriptor(
                CommandEvent.class.getName(),
                commandEventProps);

        ServiceBinding commandEventBinding = new ServiceBinding(
                (ServiceDependency) l.getDependencySpecs().get(2),
                commandEventDescriptor,
                ServiceBinding.BindingStrategy.LAZY);


        bindings.put("appContextBinding", commandEventBinding);

        return bindings;
    }
}
