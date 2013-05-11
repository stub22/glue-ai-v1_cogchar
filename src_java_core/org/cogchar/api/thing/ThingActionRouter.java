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

package org.cogchar.api.thing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class ThingActionRouter extends ThingActionConsumer {

	
	private	Map<Ident, List<ThingActionConsumer>>	myConsumersBySrcGraphID = new HashMap<Ident, List<ThingActionConsumer>>();

	@Override public Status consumeAction(ThingActionSpec actionSpec, Ident srcGraphID) {
		List<ThingActionConsumer> consumerList = findConsumersForSourceGraph(srcGraphID);
		Status highestSoFar = Status.IGNORED;
		for (ThingActionConsumer consumer : consumerList) {
			Status stat = consumer.consumeAction(actionSpec, srcGraphID);
			switch (stat) {
				case	CONSUMED:	
					return Status.CONSUMED;
				case	USED:		
					highestSoFar = Status.USED;
					break;
				case	QUEUED:
					if (highestSoFar != Status.USED) {
						highestSoFar = Status.QUEUED;
					}
					break;
				case	IGNORED:
					break;
			}
		}
		return highestSoFar;
	}
	protected	List<ThingActionConsumer>	findConsumersForSourceGraph(Ident srcGraphID) {
		List<ThingActionConsumer> consumerList = myConsumersBySrcGraphID.get(srcGraphID);
		if (consumerList == null) {
			consumerList = new ArrayList<ThingActionConsumer>();
			myConsumersBySrcGraphID.put(srcGraphID, consumerList);
		}
		return consumerList;
	}
	public void appendConsumer(Ident srcGraphID, ThingActionConsumer consumer) {
		List<ThingActionConsumer> consumerList = findConsumersForSourceGraph(srcGraphID);
		consumerList.add(consumer);
	}
	public void consumeAllActions(RepoClient rc) {
		for (Ident srcGraphID : myConsumersBySrcGraphID.keySet()) {
			getLogger().info("Consuming actions from ThingAction-graph: {}", srcGraphID);
			consumeAllActions(rc, srcGraphID);
		}
	}
	
}
