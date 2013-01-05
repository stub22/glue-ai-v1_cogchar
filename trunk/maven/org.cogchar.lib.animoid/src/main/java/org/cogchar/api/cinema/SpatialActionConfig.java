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
package org.cogchar.api.cinema;

import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.Solution;
import org.appdapter.help.repo.SolutionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */


public abstract class SpatialActionConfig {
	
	protected Logger myLogger = LoggerFactory.getLogger(this.getClass());
	
	public Ident myUri;
	public Ident attachedItem;
	public AttachedItemType attachedItemType = AttachedItemType.NULLTYPE;
	
	public enum AttachedItemType {
		NULLTYPE, CAMERA, GOODY
	}
	
	protected void pullAttachedItemAndType(SolutionHelper sh, Solution solution) {
		attachedItem = sh.pullIdent(solution, CinemaCN.ATTACHED_ITEM_VAR_NAME);
		String typeString = sh.pullIdent(solution, CinemaCN.ATTACHED_ITEM_TYPE_VAR_NAME).getLocalName().toUpperCase();
		for (AttachedItemType testType : AttachedItemType.values()) {
			if (testType.toString().equals(typeString)) {
				attachedItemType = testType;
			}
		}
	}
	
}
