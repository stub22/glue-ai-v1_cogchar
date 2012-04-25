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
import org.appdapter.api.facade.SubsystemRegistryFuncs;
import org.appdapter.api.facade.FacadeRegistry;
import org.appdapter.api.facade.FacadeSpec;
import org.appdapter.api.facade.Maker;

import org.appdapter.osgi.registry.RegistryServiceFuncs;

import org.appdapter.core.log.BasicDebugger;
/**
 *	Designed to work with or without OSGi context.
 * @author Stu B. <www.texpedient.com>
 */

object RegistryClient extends BasicDebugger  {
	
	val	SUBSYS_REG_RENDER		= "SYSREG_RENDER";
	val	SUBSYS_REG_PUMA			= "SYSREG_PUMA";
	val	SUBSYS_REG_BEHAVIOR		= "SYSREG_BEHAVIOR";
	
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
	def    findOrMakeSubsystemRegistry[SubRegType <: VerySimpleRegistry](
				subRegClaz :  Class[SubRegType], subRegName : String) : SubRegType = {	
		val vsr = getRequiredOverRegistry("findOrMakeSubsystemRegistry");
		SubsystemRegistryFuncs.findOrMakeSubsystemRegistry(vsr, subRegClaz, subRegName);
	}
	def findOrMakeSubsystemFacadeRegistry(subsysRegName : String) : FacadeRegistry = {
		val vsr = getRequiredOverRegistry("findOrMakeSubsystemFacadeRegistry");
		SubsystemRegistryFuncs.findOrMakeSubsystemFacadeRegistry(vsr, subsysRegName);
	}
	
	def findOrMakeUniqueNamedObject[OT] (sysRegName : String, objClaz : Class[OT], objName : String, maker : Maker[OT]) : OT = {
		val ssfr = findOrMakeSubsystemFacadeRegistry(sysRegName);
		ssfr.findOrMakeUniqueNamedObject(objClaz, objName, maker);
	}	
	def findOrMakeUniqueNamedObjectWithDefCons[OT](sysRegName : String, objClaz : Class[OT] , objName : String) : OT = {
		val ssfr = findOrMakeSubsystemFacadeRegistry(sysRegName);
		ssfr.findOrMakeUniqueNamedObjectWithDefCons(objClaz, objName);
	}
	def findOptionalUniqueNamedObject[OT](sysRegName : String, objClaz : Class[OT] , objName : String) : Option[OT] = {
		val ssfr = findOrMakeSubsystemFacadeRegistry(sysRegName);
		Option(ssfr.findOptionalUniqueNamedObject(objClaz, objName));
	}
	
	def  findOrMakeInternalFacade[IFT, IFK](sysRegName : String, fs: FacadeSpec[IFT, IFK], optOverrideName : String ) : IFT = {
		val ssfr = findOrMakeSubsystemFacadeRegistry(sysRegName);
		ssfr.findOrMakeInternalFacade(fs, optOverrideName);
	}

	def registerExternalFacade[EFT, EFK](sysRegName : String, fs : FacadeSpec[EFT, EFK], facade : EFT, optOverrideName : String) {
		val ssfr = findOrMakeSubsystemFacadeRegistry(sysRegName);
		ssfr.registerExternalFacade(fs, facade, optOverrideName);
	}

	def findExternalFacade[EFT, EFK](sysRegName : String, fs: FacadeSpec[EFT, EFK], optOverrideName : String) : FacadeHandle[EFT] = {
		val ssfr = findOrMakeSubsystemFacadeRegistry(sysRegName);
		val facadeOrNull = ssfr.findExternalFacade(fs, optOverrideName);
		new FacadeHandle(Option(facadeOrNull));
	}	
}

class FacadeHandle[FT](val opt : Option[FT]) {
	def isReady() : Boolean = opt.isDefined;
	def getOrElse(f : FT) : FT = opt.getOrElse(f);
	def getOrNull() : Any = opt.orNull(null)
}
