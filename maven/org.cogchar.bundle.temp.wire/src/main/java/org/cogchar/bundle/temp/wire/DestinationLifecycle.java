package org.cogchar.bundle.temp.wire;

import org.apache.qpid.client.AMQAnyDestination;
import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceLifecycle;
import org.jflux.impl.messaging.rk.utils.ConnectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.jms.Destination;

/**
 * @author Jason G. Pallack <jgpallack@gmail.com>
 */

public class DestinationLifecycle implements ServiceLifecycle<Destination> {
	private static final Logger theLogger = LoggerFactory.getLogger(DestinationLifecycle.class);
	private final static String theDestinationSpec = "destinationSpec";

	private final static ServiceDependency[] theDependencyArray = {
			new ServiceDependency(
					theDestinationSpec, DestinationSpec.class.getName(),
					ServiceDependency.Cardinality.MANDATORY_UNARY,
					ServiceDependency.UpdateStrategy.STATIC, Collections.EMPTY_MAP)
	};

	private final static String[] theClassNameArray = {
			Destination.class.getName()
	};

	public DestinationLifecycle() {
	}

	@Override
	public List<ServiceDependency> getDependencySpecs() {
		return Arrays.asList(theDependencyArray);
	}

	@Override
	public Destination createService(Map<String, Object> dependencyMap) {
		DestinationSpec spec =
				(DestinationSpec) dependencyMap.get(theDestinationSpec);
		String destName = spec.getName();
		int destType = spec.getType();

		String destRepr = buildNameString(destName, destType);

		try {
			return new AMQAnyDestination(destRepr);
		} catch (URISyntaxException ex) {
			theLogger.warn("Error starting Destination.", ex);
			return null;
		}
	}

	@Override
	public Destination handleDependencyChange(
			Destination service, String changeType, String dependencyName,
			Object dependency, Map<String, Object> availableDependencies) {
		return null;
	}

	@Override
	public void disposeService(
			Destination service, Map<String, Object> availableDependencies) {
	}

	@Override
	public String[] getServiceClassNames() {
		return theClassNameArray;
	}

	private static String buildNameString(String destName, int type) {
		String fullName = destName;

		if (type == ConnectionUtils.QUEUE) {
			fullName += "; {create: always, node: {type: queue}}";
		} else if (type == ConnectionUtils.TOPIC) {
			fullName += "; {create: always, node: {type: topic}}";
		}

		return fullName;
	}
}
