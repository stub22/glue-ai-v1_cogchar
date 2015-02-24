package org.cogchar.bundle.app.vworld.central;

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

        VWorldMapperLifecycle.getBindings(bindings, vMapLifecycle);

        OSGiRegistry registry = new OSGiRegistry(context);

        ServiceManager serviceManager =
                new ServiceManager(
                vMapLifecycle,
                bindings,
                Collections.EMPTY_MAP,
                null);
        serviceManager.start(registry);
    }

}
