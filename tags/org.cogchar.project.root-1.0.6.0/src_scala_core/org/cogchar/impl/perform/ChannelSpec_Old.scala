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
 * We are temporarily supporting these old names, since there is extant data using them.
 */

class ChannelSpec extends  FancyChannelSpec  {

}

class ChannelSpecBuilder(builderConfRes : Resource) extends FancyChannelSpecBuilder(builderConfRes) {

}