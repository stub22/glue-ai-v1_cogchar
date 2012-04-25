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

package org.cogchar.blob.emit

import org.appdapter.api.registry.VerySimpleRegistry;

import org.appdapter.api.facade.FacadeRegistryFuncs;
import org.appdapter.api.facade.FacadeSpec;
import org.appdapter.api.facade.Maker;

import org.appdapter.osgi.registry.RegistryServiceFuncs;

import org.appdapter.core.log.BasicDebugger;
/**
 *	Designed to work with or without OSGi context.
 * @author Stu B. <www.texpedient.com>
 */

object RegistryClient extends BasicDebugger  {
	def getVerySimpleRegistry() : VerySimpleRegistry = {
		RegistryServiceFuncs.getTheWellKnownRegistry(this.getClass());
	}
	protected def getRequiredOverRegistry(functionCtx : String) : VerySimpleRegistry = { 
		val vsr = getVerySimpleRegistry();
		if (vsr == null) {
			val msg = "getRequiredOverRegistry(" + functionCtx + ") : Somehow got a null OverRegistry";
			logError(msg);
			throw new Exception(msg);
		}
		return vsr;
	}
	def findOrMakeUniqueNamedObject[OT] (objClaz : Class[OT], objName : String, maker : Maker[OT]) : OT = {
		val vsr = getRequiredOverRegistry("findOrMakeUniqueNamedObject");
		FacadeRegistryFuncs.findOrMakeUniqueNamedObject(vsr, objClaz, objName, maker);

	}	
	def findOrMakeUniqueNamedObjectWithDefCons[OT](objClaz : Class[OT] , objName : String) : OT = {
		val vsr = getRequiredOverRegistry("findOrMakeUniqueNamedObjectWithDefCons");
		FacadeRegistryFuncs.findOrMakeUniqueNamedObjectWithDefCons(vsr, objClaz, objName);
	}
	def findOptionalUniqueNamedObject[OT](objClaz : Class[OT] , objName : String) : Option[OT] = {
		val vsr = getRequiredOverRegistry("findOptionalUniqueNamedObject");
		Option(vsr.findOptionalUniqueNamedObject(objClaz, objName));
	}
	
	def  findOrMakeInternalFacade[IFT, IFK](fs: FacadeSpec[IFT, IFK], optOverrideName : String ) : IFT = {	
		val facadeClaz : Class[IFT]  = fs.getFacadeClass();
		val actualName = FacadeRegistryFuncs.chooseBestLocalFacadeName(fs, optOverrideName);
		RegistryClient.findOrMakeUniqueNamedObjectWithDefCons(facadeClaz, actualName);
	}
	/**
	 * Paired with findExternalFacade, used for objects supplied from outside, mainly from JME3:
	 * 
	 * <ol><li>AssetManager</li>
	 * <li>root node</li>
	 * <li>flat GUI node</li>
	 * </ol>
	*/
	def registerExternalFacade[EFT, EFK](fs : FacadeSpec[EFT, EFK], facade : EFT, optOverrideName : String) {
		val vsr = getRequiredOverRegistry("registerExternalFacade");
		val actualName = FacadeRegistryFuncs.chooseBestLocalFacadeName(fs, optOverrideName);
		vsr.registerNamedObject(facade, actualName);
	}

	/**
	 * Paired with registerExternalFacade, used for external objects, like JME3 assetManager, rootNode, guiNode.
	 * 
	 * @param <EFT>
	 * @param objClaz
	 * @param objName
	 * @return 
	 */	
	def findExternalFacade[EFT, EFK](fs: FacadeSpec[EFT, EFK], optOverrideName : String) : FacadeHandle[EFT] = {
		val facadeClaz : Class[EFT]  = fs.getFacadeClass();
		val actualName = FacadeRegistryFuncs.chooseBestLocalFacadeName(fs, optOverrideName);
		val optObj = findOptionalUniqueNamedObject(facadeClaz, actualName);
		new FacadeHandle(optObj);
	}	
}

class FacadeHandle[FT](val opt : Option[FT]) {
	def isReady() : Boolean = opt.isDefined;
	def getOrElse(f : FT) : FT = opt.getOrElse(f);
	def getOrNull() : Any = opt.orNull(null)
}
