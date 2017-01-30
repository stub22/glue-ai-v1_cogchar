package org.cogchar.bundle.temp.wire;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;

import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.ItemFuncs;
import org.appdapter.core.name.Ident;
import org.jflux.api.service.binding.ServiceBinding.BindingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * The builder that is called to make a Spec object from the raw RDF data
 * representing a ServiceBinding.
 *
 * @author Jason R. Eads <jeads362@gmail.com>
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */

public class ServiceBindingSpecBuilder
		extends CachingComponentAssembler<ServiceBindingSpec> {
	// Defines the relationship "#Property Name" key (aka the Predicate),
	// that is followed from an individual to collect the data

	public ServiceBindingSpecBuilder(Resource builderConfRes) {
		super(builderConfRes);
	}

	private final static String theServiceJavaFQCN =
			"http://www.jflux.org/service#serviceJavaFQCN";
	private final static String theHasProperty =
			"http://www.cogchar.org/schema/scene#hasProperty";
	private final static String theBinding =
			"http://www.cogchar.org/schema/scene#Binding";
	private final static String theDependencyURI =
			"http://www.cogchar.org/schema/scene#dependencyURI";

	private final static String BINDING_STRATEGY_INVALID_WARN =
			"Invalid binding strategy for serviceBinding";
	private static final Logger theLogger = LoggerFactory.getLogger(ServiceBindingSpecBuilder.class);

	@Override
	protected Class<ServiceBindingSpec> decideComponentClass(
			Ident ident, Item item) {
		return ServiceBindingSpec.class;
	}

	@Override
	protected void initExtendedFieldsAndLinks(
			ServiceBindingSpec mkc, Item item, Assembler asmblr, Mode mode) {
		ItemAssemblyReader reader = getReader();

		// read in the data fields and store them in the Spec
		mkc.setClassName(reader.readConfigValString(
				item.getIdent(), theServiceJavaFQCN, item, ""));

		// read in the binding strategy
		Ident bindingIdent = ItemFuncs.getNeighborIdent(item, theBinding);
		Item bindingStrategy =
				item.getSingleLinkedItem(
						bindingIdent, Item.LinkDirection.FORWARD);
		if (bindingStrategy.getIdent().getLocalName().equals("lazy")) {
			mkc.setBindingStrategy(BindingStrategy.LAZY);
		} else if (bindingStrategy.getIdent().getLocalName().equals("eager")) {
			mkc.setBindingStrategy(BindingStrategy.EAGER);
		} else {
			getLogger().warn(BINDING_STRATEGY_INVALID_WARN);
			mkc.setBindingStrategy(BindingStrategy.LAZY);
		}

		// read in and build the linked properties, and storing each in the Spec
		List linkedProperties =
				reader.findOrMakeLinkedObjects(
						item, theHasProperty, asmblr, mode, null);
		List linkedDependencies =
				reader.findOrMakeLinkedObjects(
						item, theDependencyURI, asmblr, mode, null);

		for (Object lp : linkedProperties) {
			if (!(lp instanceof PropertySpec)) {
				theLogger.warn("Unexpected object found at {} = {}",
						theHasProperty, lp.toString());
				continue;
			}

			PropertySpec propertySpec = (PropertySpec) lp;
			mkc.addProperty(propertySpec.getName(), propertySpec.getValue());
		}

		for (Object ld : linkedDependencies) {
			if (!(ld instanceof ServiceDependencySpec)) {
				theLogger.warn("Unexpected object found at {} = {}",
						theDependencyURI, ld.toString());
				continue;
			}

			ServiceDependencySpec dep = (ServiceDependencySpec) ld;
			mkc.setServiceDependency(dep);

			break;
		}
	}
}