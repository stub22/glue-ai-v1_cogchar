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

package org.cogchar.api.channel;

import org.cogchar.api.channel.Channel;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * A GraphChannel is used to read, write, and listen for updates on a particular graph in a repo.
 * Because it is a Channel, it has its own URI, and it is managable via a JFlux lifecycle.
 * Behavior features should work through GraphChannels rather than directly through repos.
 * 
Graph" channel is part of channel-core, not performance.
Graph channels (like all channels) can interact with JFlux lifecycles.
* Channels are the only fully-Lifecycled objects in the public API of Cogchar-core
(besides RepoSpecLifecycle, Theater, and Scene lifecycle, which are only 
semi-public).  Note that Channels are an injection API rather than a service 
API.  Outside projects will not call methods on channels directly, but they 
can implement new channels to be called by Cogchar, and wire them up by URI.
 */

public interface GraphChannel extends Channel {

}
