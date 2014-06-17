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
import org.cogchar.api.monitor.AppDebugMonitor;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.WantsThingAction;
import org.cogchar.name.entity.EntityRoleCN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 *
 * Will dissolve and be mostly replaced by MarkingAgent pattern, but this still may serve as hub of a crude notification
 * propagator until better options are in place.
 */
public class BasicThingActionRouter extends BasicThingActionConsumer {

	private Logger theLogger = LoggerFactory.getLogger(BasicThingActionRouter.class);
	private Map<Ident, List<WantsThingAction>> myConsumersBySrcGraphID = new HashMap<Ident, List<WantsThingAction>>();
	private Ident myViewingAgentID;
	private Long myCutoffTime;
	private AppDebugMonitor myAppMonitor;

//    public BasicThingActionRouter(){}
	public BasicThingActionRouter(Long cutoffTime, Ident viewingAgentID) {
		myCutoffTime = cutoffTime;
		myViewingAgentID = viewingAgentID;
	}

	public void setAppMonitor(AppDebugMonitor appMonitor) {
		myAppMonitor = appMonitor;
	}

	@Override public ConsumpStatus consumeAction(ThingActionSpec actionSpec, Ident srcGraphID) {
		if (myAppMonitor != null) {
			myAppMonitor.notifyThingActionTransit(AppDebugMonitor.TransitKind.TASTED_BY_ROUTER, actionSpec);
		}
		List<WantsThingAction> consumerList = findConsumersForSourceGraph(srcGraphID);
		getLogger().info("Consuming {} actions from ThingAction-graph: {}", consumerList.size(), srcGraphID);
		ConsumpStatus highestSoFar = ConsumpStatus.IGNORED;
		for (WantsThingAction consumer : consumerList) {
			ConsumpStatus stat = null;
			try {
				stat = consumer.consumeAction(actionSpec, srcGraphID);
			} // TODO: more specific exception(s) ...?
			catch (Exception e) {
				theLogger.warn("TA Consumer:{} ...caught except while handling ThingAction: {}", consumer.toString(), actionSpec.getActionSpecID());
				theLogger.warn("The exception trace is: ",  e);
				theLogger.info("Full dump of spec: {}", actionSpec);
				continue;
			}
			if (stat == null) {
				theLogger.warn("TA Consumer:{} ...null status from handling ThingAction:{}", consumer.toString(), actionSpec.getActionSpecID());			
				theLogger.info("Full dump of spec: {}", actionSpec);
				continue;
			}
			switch (stat) {
				case CONSUMED:
					if (highestSoFar != ConsumpStatus.CONSUMED) {
						highestSoFar = ConsumpStatus.CONSUMED;
					}
				case USED:
					if (highestSoFar == ConsumpStatus.IGNORED) {
						highestSoFar = ConsumpStatus.USED;
					}
					break;
				case QUEUED:
					if (highestSoFar != ConsumpStatus.USED) {
						highestSoFar = ConsumpStatus.QUEUED;
					}
					break;
				case IGNORED:
					break;
			}
		}
		return highestSoFar;
	}

	protected List<WantsThingAction> findConsumersForSourceGraph(Ident srcGraphID) {
		List<WantsThingAction> consumerList = myConsumersBySrcGraphID.get(srcGraphID);
		if (consumerList == null) {
			consumerList = new ArrayList<WantsThingAction>();
			myConsumersBySrcGraphID.put(srcGraphID, consumerList);
		}
		return consumerList;
	}

	public void appendConsumer(Ident srcGraphID, WantsThingAction consumer) {
		List<WantsThingAction> consumerList = findConsumersForSourceGraph(srcGraphID);
		consumerList.add(consumer);
	}

	/**
	 *
	 * @param rc
	 * @deprecated
	 *
	 *
	 *
	 *
	 * Called directly from
	 *
	 * Still called from Puma.GruesomeTAProcessingFuncs.processPendingThingActions(), which is called by top level app
	 * logic after any "important" repo update.
	 *
	 * * See also the inherited *2-args* form of this method in BasicThingActionConsumer.
	 *
	 */
	@Deprecated public void consumeAllActions(RepoClient rc) {
		for (Ident srcGraphID : myConsumersBySrcGraphID.keySet()) {
			getLogger().info("Consuming actions from ThingAction-graph: {}", srcGraphID);
//            consumeAllActions(rc, srcGraphID);
			// This results in calls to consumeAction(actionSpec, srcGraphID);
			viewAndMarkAllActions(rc, srcGraphID, myCutoffTime, myViewingAgentID);
		}
	}
}
