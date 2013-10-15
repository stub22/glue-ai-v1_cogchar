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

package org.cogchar.impl.perform

import org.cogchar.impl.channel.{FancyChannelSpec, FancyChannelSpecBuilder}
import com.hp.hpl.jena.rdf.model.Resource;
/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Currently we are in the middle of a move to deprecate the name 
 * o.c.impl.perform.ChannelSpec*, in favor of 
 * o.c.impl.channel.FancyChanSpec*
 * 
 * 
 * However, we have a lot of data that points to:
 * o.c.impl.perform.ChannelSpecBuilder
 * 
 * Specifically, these sheets:
 * GluePuma_HRKR50_TestFull.Chan
 * GluePuma_BehavMasterDemo.ChanBinding
 * - also some .TTL files, yes?
 We have now removed all dependencies on THIS class:  class ChannelSpec extends  FancyChannelSpec  {}

However, keeping this type extension below allows that data to still load successfully.
*/

class ChannelSpecBuilder(builderConfRes : Resource) extends FancyChannelSpecBuilder(builderConfRes) {

}