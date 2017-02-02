/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cogchar.lifter.generation;

import com.hp.hpl.jena.rdf.model.Model;

import org.appdapter.fancy.rclient.RepoClient;
import org.cogchar.api.thing.WantsThingAction;
import org.jflux.api.service.ServiceDependency;
import org.jflux.api.service.ServiceLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This collects the needed information for the Generator, the location to push
 * the lifter page data and the location to push the TA to trigger its display.
 *
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */
public class LifterAnswerPageGeneratorLifecycle implements
		ServiceLifecycle<WantsThingAction> {

	private static final Logger theLogger = LoggerFactory.getLogger(LifterAnswerPageGeneratorLifecycle.class);

	/**
	 * This provides the classnames for use in JFlux.
	 */
	private final static String[] theClassNameArray = new String[]{
			WantsThingAction.class.getName(),
			LifterAnswerPageGenerator.class.getName()};

	public final static String theLifterAnswerPageGeneratorSpec =
			"lifterAnswerPageGeneratorSpec";

	public static final String theQueryInterface = "queryInterface";

	/**
	 * This provides the dependencies which JFlux will provide.
	 *
	 * The spec provides the URIs for the Models that are required.
	 *
	 * The RepoClient provides a means of retrieving the Models.
	 */
	private final static ServiceDependency[] theDependencyArray = {
			new ServiceDependency(
					theLifterAnswerPageGeneratorSpec,
					LifterAnswerPageGeneratorSpec.class.getName(),
					ServiceDependency.Cardinality.MANDATORY_UNARY,
					ServiceDependency.UpdateStrategy.STATIC,
					Collections.EMPTY_MAP),
			new ServiceDependency(
					theQueryInterface,
					RepoClient.class.getName(),
					ServiceDependency.Cardinality.MANDATORY_UNARY,
					ServiceDependency.UpdateStrategy.STATIC,
					Collections.EMPTY_MAP)
	};

	//Lifecycle Constructor - required for automated assembly
	public LifterAnswerPageGeneratorLifecycle() {
	}

	@Override
	public List<ServiceDependency> getDependencySpecs() {
		return Arrays.asList(theDependencyArray);
	}

	@Override
	public WantsThingAction createService(Map<String, Object> dependencyMap) {

		// Collect the LifterAnswerPageGenerator specification
		LifterAnswerPageGeneratorSpec mySpec =
				(LifterAnswerPageGeneratorSpec) dependencyMap.get(
						theLifterAnswerPageGeneratorSpec);

		// Collect the Query-Repo interface
		RepoClient repoClient = (RepoClient) dependencyMap.get(
				theQueryInterface);

		// Collect the lifter page recieving Model
		Model lifterModelReadonly = repoClient.getNamedModelReadonly(mySpec.getLifterModelURI());  // FIXME


		// Collect the TA recieving Model
		Model thingActionModelReadonly = repoClient.getNamedModelReadonly(mySpec.getThingActionModelURI());  // FIXME

		// Instance the pageGenerator
		LifterAnswerPageGenerator pageGen = new LifterAnswerPageGenerator(
				lifterModelReadonly,
				thingActionModelReadonly);

		//TODO: confirm... is this enough?
		// does it need nothing more to get into the TA router?

		return pageGen;
	}

	@Override
	public WantsThingAction handleDependencyChange(
			WantsThingAction service,
			String changeType,
			String dependencyName,
			Object dependency,
			Map<String, Object> availableDependencies) {


		// if spec lost, dispose?

		// if spec changed, dispose and create?

		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void disposeService(WantsThingAction t, Map<String, Object> map) {

		//TODO: confirm: remove from router somehow?


		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String[] getServiceClassNames() {
		return theClassNameArray;
	}


}
