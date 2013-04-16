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
package org.cogchar.api.scene;

import java.util.Collection;
import org.cogchar.api.perform.Channel;
import org.cogchar.api.perform.Media;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * We have not yet precisely defined or committed fully to this "rootChannel" idea.
 * It is not currently used for any practical purpose.  (2013-04-14).
 * However, the concept is that "our scene is controlled from a high-order symbolic 'root' channel, 
 * and pumps data to/from specific datastream (sub-)channels."  Thus we might say that temporarily the
 * rootChannel and sub-channels are "bound".  In practice this binding happens through the evolution of
 * "Performance" objects, which form the shared state + notification pathway between subChannels and 
 * scene/rootChannel.
 */
public interface Scene<WorldTime, RootChanType> 
/*RootChanCursor, RootChanMedia extends Media<RootChanCursor>, 
			RootChanType extends Channel<RootChanCursor, RootChanMedia, WorldTime>> 
			*/ 
{
	// public <SubChanType> 	void wireSubChannels(Collection<SubChanType> chans);
	public 	void wireSubChannels(Collection<Channel> chans);
	public RootChanType getRootChannel();
}
