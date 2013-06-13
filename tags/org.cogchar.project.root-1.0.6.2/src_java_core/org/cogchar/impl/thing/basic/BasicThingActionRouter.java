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

package org.cogchar.impl.thing.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.appdapter.core.name.Ident;
import org.appdapter.help.repo.RepoClient;
import org.cogchar.api.thing.ThingActionSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 * 
 * Will dissolve and be mostly replaced by MarkingAgent pattern, but this still may serve as hub of
 * a crude notification propagator until better options are in place.
 */

public class BasicThingActionRouter extends BasicThingActionConsumer {

	private	Map<Ident, List<BasicThingActionConsumer>>	myConsumersBySrcGraphID = new HashMap<Ident, List<BasicThingActionConsumer>>();

	@Override public ConsumpStatus consumeAction(ThingActionSpec actionSpec, Ident srcGraphID) {
		List<BasicThingActionConsumer> consumerList = findConsumersForSourceGraph(srcGraphID);
		ConsumpStatus highestSoFar = ConsumpStatus.IGNORED;
		for (BasicThingActionConsumer consumer : consumerList) {
			ConsumpStatus stat = consumer.consumeAction(actionSpec, srcGraphID);
			switch (stat) {
				case	CONSUMED:	
					return ConsumpStatus.CONSUMED;
				case	USED:		
					highestSoFar = ConsumpStatus.USED;
					break;
				case	QUEUED:
					if (highestSoFar != ConsumpStatus.USED) {
						highestSoFar = ConsumpStatus.QUEUED;
					}
					break;
				case	IGNORED:
					break;
			}
		}
		return highestSoFar;
	}
	protected	List<BasicThingActionConsumer>	findConsumersForSourceGraph(Ident srcGraphID) {
		List<BasicThingActionConsumer> consumerList = myConsumersBySrcGraphID.get(srcGraphID);
		if (consumerList == null) {
			consumerList = new ArrayList<BasicThingActionConsumer>();
			myConsumersBySrcGraphID.put(srcGraphID, consumerList);
		}
		return consumerList;
	}
	public void appendConsumer(Ident srcGraphID, BasicThingActionConsumer consumer) {
		List<BasicThingActionConsumer> consumerList = findConsumersForSourceGraph(srcGraphID);
		consumerList.add(consumer);
	}
	@Deprecated public void consumeAllActions(RepoClient rc) {
		for (Ident srcGraphID : myConsumersBySrcGraphID.keySet()) {
			getLogger().info("Consuming actions from ThingAction-graph: {}", srcGraphID);
			consumeAllActions(rc, srcGraphID);
		}
	}
	
}
