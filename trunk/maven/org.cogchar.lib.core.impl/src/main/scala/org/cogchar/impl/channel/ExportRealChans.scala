/*
 *  Copyright 2014 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.impl.channel 
/**
 * @author Stu B. <www.texpedient.com>
 * These classes are bridges to the names in related package o.c.impl.chan.fancy.
 * The names shown here are the ones currently used in our config data.
 */

import org.cogchar.impl.chan.fancy.{RealThingActionChanSpec, RealThingActionChanSpecBuilder}
import org.cogchar.impl.chan.fancy.{RealFancyChannelSpec, RealFancyChannelSpecBuilder}
import com.hp.hpl.jena.rdf.model.Resource;
	
class ThingActionChanSpec extends RealThingActionChanSpec 
class ThingActionChanSpecBuilder(builderConfRes : Resource) extends RealThingActionChanSpecBuilder(builderConfRes ) 
	
class FancyChannelSpec extends RealFancyChannelSpec 
class FancyChannelSpecBuilder(builderConfRes : Resource) extends RealFancyChannelSpecBuilder(builderConfRes ) 

/**
 * These get loaded by Assembler in context like:
 /*     [java] 98060  ERROR [Service Manager Thread - 16] (AssemblerUtils.java:93) buildAllRootsInModel - Cannot assemble item http://www.cogchar.org/schema/scene/instance#Flow-TA-Test-TAChan-C1
 [java] com.hp.hpl.jena.assembler.exceptions.AssemblerException: caught: null
 [java]   doing:
 [java]     root: http://www.cogchar.org/schema/scene/instance#Flow-TA-Test-TAChan-C1 with type: urn:ftd:cogchar.org:2012:runtime#BuildableTAChanSpec assembler class: class org.cogchar.impl.channel.ThingActionChanSpecBuilder
 [java] 
 [java] 	at com.hp.hpl.jena.assembler.assemblers.AssemblerGroup$PlainAssemblerGroup.openBySpecificType(AssemblerGroup.java:138)
 [java] 	at com.hp.hpl.jena.assembler.assemblers.AssemblerGroup$PlainAssemblerGroup.open(AssemblerGroup.java:117)
 [java] 	at com.hp.hpl.jena.assembler.assemblers.AssemblerGroup$ExpandingAssemblerGroup.open(AssemblerGroup.java:81)
 [java] 	at org.appdapter.bind.rdf.jena.assembly.AssemblerUtils.buildAllRootsInModel(AssemblerUtils.java:90)
 [java] 	at org.appdapter.bind.rdf.jena.assembly.AssemblerUtils.buildAllRootsInModel(AssemblerUtils.java:100)
 [java] 	at org.appdapter.core.store.BasicRepoImpl.assembleRootsFromNamedModel(BasicRepoImpl.java:318)
 [java] 	at org.appdapter.help.repo.RepoClientImpl.assembleRootsFromNamedModel(RepoClientImpl.scala:111)
 [java] 	at org.cogchar.outer.behav.demo.TAGraphChanWiringDemo.loadTAChanSpecs(TAGraphChanWiringDemo.java:61)
 [java] 	at org.cogchar.outer.behav.demo.TAGraphChanWiringDemo.loadAndRegisterSpecs(TAGraphChanWiringDemo.java:45)
 [java] 	at com.rkbots.demo.behavior.master.BehaviorMasterLifecycle.create(BehaviorMasterLifecycle.java:47)
 [java] 	at com.rkbots.demo.behavior.master.BehaviorMasterLifecycle.create(BehaviorMasterLifecycle.java:22)
 */
 */