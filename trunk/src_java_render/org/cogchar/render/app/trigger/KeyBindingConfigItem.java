/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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
package org.cogchar.render.app.trigger;

import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionHelper;

import org.cogchar.blob.emit.QueryTester;

import org.cogchar.blob.emit.KeystrokeConfigEmitter;

/**
 * A class to hold the individual jMonkey key bindings. For more info see KeyBindingConfig.java
 *
 * @author Ryan Biggs <rbiggs@skyriversoftware.com>
 */
public class KeyBindingConfigItem {

	public Ident	myTypeIdent;
	public String	myBindingLocalName;
	public String	myBoundEventName;
	public String	myBoundKeyName;

	public KeyBindingConfigItem(RepoClient qi, Solution solution, KeystrokeConfigEmitter kce) {
		SolutionHelper sh = new SolutionHelper();
		myTypeIdent = sh.getIdentFromSolution(solution, kce.TYPE_VAR_NAME());
		myBindingLocalName = sh.getIdentFromSolution(solution, kce.BINDING_IDENT_VAR_NAME()).getLocalName();
		myBoundEventName = sh.getStringFromSolution(solution, kce.ACTION_VAR_NAME());
		myBoundKeyName = sh.getStringFromSolution(solution, kce.KEY_VAR_NAME());
	}
}
