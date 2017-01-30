package org.cogchar.bundle.temp.wire;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Resource;

import org.appdapter.bind.rdf.jena.assembly.CachingComponentAssembler;
import org.appdapter.bind.rdf.jena.assembly.ItemAssemblyReader;
import org.appdapter.core.item.Item;
import org.appdapter.core.name.Ident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Jason G. Pallack <jgpallack@gmail.com>
 * @author Jason Randolph Eads <eadsjr@hansonrobokind.com>
 */

public class ServiceLifecycleSpecBuilder
		extends CachingComponentAssembler<ServiceLifecycleSpec> {
	private final static String theLifecycleJavaFQCN =
			"http://www.appdapter.org/schema/box#lifecycleJavaFQCN";
	private final static String theHasDependency =
			"http://www.jflux.org/service/dependency#hasDependency";
	private static final Logger theLogger = LoggerFactory.getLogger(ServiceLifecycleSpecBuilder.class);

	public ServiceLifecycleSpecBuilder(Resource builderConfRes) {
		super(builderConfRes);
	}

	@Override
	protected Class<ServiceLifecycleSpec> decideComponentClass(
			Ident ident, Item item) {
		return ServiceLifecycleSpec.class;
	}

	@Override
	protected void initExtendedFieldsAndLinks(
			ServiceLifecycleSpec mkc, Item item, Assembler asmblr, Mode mode) {
		ItemAssemblyReader reader = getReader();
		String className = reader.readConfigValString(
				item.getIdent(), theLifecycleJavaFQCN, item, "");
		mkc.setLifecycleClassName(className);

		List linkedDependencies = reader.findOrMakeLinkedObjects(
				item, theHasDependency, asmblr, mode, null);

		for (Object o : linkedDependencies) {
			if (o instanceof ServiceDependencySpec) {
				ServiceDependencySpec depSpec = (ServiceDependencySpec) o;
				mkc.addDependency(depSpec);
			} else {
				theLogger.warn("Unexpected object found at {} = {}",
						theHasDependency, o);
			}
		}

//        if(update.equals("static")) {
//            mkc.setUpdateStrategy(UpdateStrategy.STATIC);
//        } else if (update.equals("dynamic")) {
//            mkc.setUpdateStrategy(UpdateStrategy.DYNAMIC);
//        } else {
//            theLogger.error("Unexpected update strategy: {}", update);
//            mkc.setUpdateStrategy(null);
//        }
//        
//        if(count.equals("single") && required.equals("required")) {
//            mkc.setCardinality(Cardinality.MANDATORY_UNARY);
//        } else if(count.equals("multiple") && required.equals("required")) {
//            mkc.setCardinality(Cardinality.MANDATORY_MULTIPLE);
//        } else if(count.equals("single") && required.equals("optional")) {
//            mkc.setCardinality(Cardinality.OPTIONAL_UNARY);
//        } else if(count.equals("multiple") && required.equals("optional")) {
//            mkc.setCardinality(Cardinality.OPTIONAL_MULTIPLE);
//        } else {
//            theLogger.error("Unexpected cardinality: {} {}",
//                    count, required);
//            mkc.setCardinality(null);
//        }
	}

}
