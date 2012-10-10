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

import org.appdapter.subreg.SubsystemHandle;
import org.appdapter.subreg.BasicSubsystemHandle;



/**
 *	Designed to work with or without OSGi context, however we must be careful not to mix the two (unknowingly).
 *	The credClaz constructor argument is used to try to find a bundleContext.
 * @author Stu B. <www.texpedient.com>
 */


object SubsystemHandleFinder {
	def	SUBSYS_REG_RENDER		= "SYSREG_RENDER";
	def	SUBSYS_REG_PUMA			= "SYSREG_PUMA";
	def	SUBSYS_REG_BEHAVIOR		= "SYSREG_BEHAVIOR";
	def	SUBSYS_REG_REPOSITORY	= "SYSREG_REPOSITORY";
	def	SUBSYS_REG_CCRK_BIND	= "SYSREG_CCRK_BIND";

	// exampleFS is an examplar of one of the FacadeSpecs we might use to look up a facade in the requested subsystem.
	// optCredClaz is used as the credential class, but if it is null, AND the specified facade is "external",
	// then the FacadeSpecs target class type.   Otherwise, the class of this object (SubsystemHandleFinder) will
	// be used.
	def getSubsystemHandle(subsysName : String, exampleFS : FacadeSpec[_, _], optCredClaz : Class[_] ) : SubsystemHandle = {
		// Use the internal facade class as the default credential, to usually make Netigso happily return a bundleContext.
		val credClaz : Class[_] =  exampleFS.determineCredentialClass(optCredClaz, getClass())
		return new BasicSubsystemHandle(subsysName, credClaz);
	}
	def getRenderSubsysHandle(exampleFS : FacadeSpec[_, _], optCredClaz : Class[_]) : SubsystemHandle = {
		getSubsystemHandle(SUBSYS_REG_RENDER, exampleFS, optCredClaz)
	}
		
}


