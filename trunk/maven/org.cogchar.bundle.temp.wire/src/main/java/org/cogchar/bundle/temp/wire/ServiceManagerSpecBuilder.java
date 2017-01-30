package org.cogchar.bundle.temp.wire;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;

import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.core.item.Item;
import org.appdapter.core.name.Ident;
import org.jflux.api.registry.Descriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jason G. Pallack <jgpallack@gmail.com>
 * @author Jason R. Eads <jeads362@gmail.com>
 */

public class ServiceManagerSpecBuilder
		extends CachingComponentAssembler<ServiceManagerSpec> {
	private final static String theLifecycleType =
			"http://www.cogchar.org/schema/scene#lifecycleType";
	private final static String theServiceBinding =
			"http://www.cogchar.org/schema/scene#serviceBinding";
	private final static String theRegistrationStrategy =
			"http://www.cogchar.org/schema/scene#registrationStrategy";
	private static final Logger theLogger = LoggerFactory.getLogger(ServiceManagerSpecBuilder.class);


	public ServiceManagerSpecBuilder(Resource builderConfRes) {
		super(builderConfRes);
	}

	@Override
	protected Class<ServiceManagerSpec> decideComponentClass(
			Ident ident, Item item) {
		return ServiceManagerSpec.class;
	}

	@Override
	protected void initExtendedFieldsAndLinks(
			ServiceManagerSpec mkc, Item item, Assembler asmblr, Mode mode) {
		ItemAssemblyReader reader = getReader();
		ServiceLifecycleSpec lifecycleSpec = null;

		List linkedLifecycles =
				reader.findOrMakeLinkedObjects(
						item, theLifecycleType, asmblr, mode, null);
		List linkedBindings =
				reader.findOrMakeLinkedObjects(
						item, theServiceBinding, asmblr, mode, null);
		List linkedStrategies =
				reader.findOrMakeLinkedObjects(
						item, theRegistrationStrategy, asmblr, mode, null);

		for (Object lc : linkedLifecycles) {
			if (!(lc instanceof ServiceLifecycleSpec)) {
				theLogger.warn("Unexpected object found at {} = {}",
						theLifecycleType, lc.toString());
				continue;
			}

			lifecycleSpec = (ServiceLifecycleSpec) lc;
			String lifecycleClass = lifecycleSpec.getLifecycleClassName();
			mkc.setLifecycleClassName(lifecycleClass);
			break;
		}

		for (Object sb : linkedBindings) {
			if (!(sb instanceof ServiceBindingSpec)) {
				theLogger.warn("Unexpected object found at {} = {}",
						theServiceBinding, sb.toString());
				continue;
			}

			ServiceBindingSpec bindingSpec = (ServiceBindingSpec) sb;

			Descriptor desc = bindingSpec.getDescriptor();
			mkc.addServiceBinding(desc.getClassName(), bindingSpec);
		}

		for (Object rs : linkedStrategies) {
			if (!(rs instanceof DefaultRegistrationStrategySpec)) {
				theLogger.warn("Unexpected object found at {} = {}",
						theRegistrationStrategy, rs.toString());
				continue;
			}

			DefaultRegistrationStrategySpec stratSpec =
					(DefaultRegistrationStrategySpec) rs;
			mkc.setServiceRegistration(stratSpec);
			break;
		}
	}
}
