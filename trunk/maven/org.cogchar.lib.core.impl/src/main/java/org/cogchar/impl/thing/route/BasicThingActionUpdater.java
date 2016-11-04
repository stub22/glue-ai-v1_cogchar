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
package org.cogchar.impl.thing.route;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Literal;
import org.cogchar.name.thing.ThingCN;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import java.util.List;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.core.query.InitialBinding;
import org.appdapter.fancy.gportal.DelegatingPortal;
import org.appdapter.fancy.gportal.GraphAbsorber;
import org.appdapter.fancy.gportal.GraphSupplier;
import org.appdapter.fancy.gportal.GraphQuerier;
import org.appdapter.fancy.gportal.GraphUpdater;
import org.appdapter.fancy.rclient.RepoClient;
import org.appdapter.fancy.rclient.RepoClientFuncs_TxAware;
import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware.Oper;
import org.appdapter.fancy.query.SolutionList;
import org.appdapter.fancy.model.ResourceResolver;
import org.cogchar.api.thing.ThingActionSpec;
// import static org.cogchar.impl.thing.basic.BasicThingActionQResAdapter.buildActionParameterValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Ryan Biggs <rbiggs@hansonrobokind.com>
 */
public class BasicThingActionUpdater {

	// This is just a temporary definition of the sourceAgentID until it becomes clear where this best comes from
	public static final Ident SOURCE_AGENT_ID = new FreeIdent(ThingCN.TA_NS + "RepoThingAction");
	private static Logger theLogger = LoggerFactory.getLogger(BasicThingActionUpdater.class);

	// static int dubiousSleepMsec = 100;
	// Used only from PUMA to drain thingActions during init/reset, right?
	@Deprecated protected List<ThingActionSpec> takeThingActions_TX(final RepoClient rc, final Ident srcGraphID) {
		List<ThingActionSpec> takenActions = null;
		takenActions = RepoClientFuncs_TxAware.execWriteTransCompatible(rc, null, new Oper<List<ThingActionSpec>>() {
			@Override public List<ThingActionSpec> perform() {
				return takeThingActions_Raw(rc, srcGraphID);
			}
			
		});
		if (takenActions == null) {
			takenActions = new java.util.ArrayList<ThingActionSpec>();
		}
		return takenActions;		
	}

	@Deprecated private List<ThingActionSpec> takeThingActions_Raw(RepoClient rc, Ident srcGraphID) {
		// Will need to become transactional if used concurrently, like viewActions below.
		SolutionList actionsSolList = rc.queryIndirectForAllSolutions(ThingCN.ACTION_QUERY_URI, srcGraphID);
		BasicThingActionQResAdapter taqra = new BasicThingActionQResAdapter();
		List<ThingActionSpec> actionSpecList = taqra.reapActionSpecList(actionsSolList, rc, srcGraphID, SOURCE_AGENT_ID);
		// Delete the actions from graph, so they are not returned on next call to this method.
		for (ThingActionSpec tas : actionSpecList) {
			old_deleteThingAction(rc, srcGraphID, tas);
		}
		theLogger.info("Returning ThingAction list of length {} from graph {}", actionSpecList.size(), srcGraphID);
		return actionSpecList;
		
	}

	/**
	 * Finds actions not yet seen by a particular reading agent, pulls their data, and marks them seen for that agent.
	 * Implementation involves a classic transactional "Read-then-Write" scenario.
	 * If we are already in an open transaction, this operation will preserve it.
	 * Otherwise, if transactions are supported, this op will bracket all the graph access in a single write-trans,
	 * to ensure consistency of results.
	 * 
	 * Because this is the grittiest part of the ThingAction pipeline, we expand at length to close the world.
	 * Yes, it's a naughty topic, but so far this read-then-write operation is called from two places in Cogchar:
	 *		
	 *		BasicThingActionConsumer.viewAndMarkAllActions - called in orderly manner by any agent with write-access 
	 *		to a repo.
	 * 
	 *		ThingActionGraphChan.seeThingActions - called from a channel user agent such as BThtr, which interact with 
	 *		remainder of system using repo contents of which ThingActions are a minimal examplar.  That method contains 
	 *		another huge comment for your pleasure.
	 * 
	 * Double-naughty:  The other defined sources of repo updates (besides the marking-view), with which we may
	 * have write contention, are:
	 *		1) "config loads" - infrequent and in theory we have full control, not a big concern.
	 *		2) SPARQL-Update requests from external clients, via HTTP, AMQP, other.
	 * 
	 * @param rc
	 * @param srcGraphID
	 * @param seeingAgentID
	 * @return
	 */

	public List<ThingActionSpec> viewActionsAndMark_TX(final RepoClient rc, final Ident srcGraphID, final Long cutoffTStamp, final Ident viewingAgentID) {
		List<ThingActionSpec> actionSpecList = null;

		actionSpecList = RepoClientFuncs_TxAware.execWriteTransCompatible(rc, null, new Oper<List<ThingActionSpec>>() {
			@Override public List<ThingActionSpec> perform() {
				return viewActionsAndMark_Raw(rc, srcGraphID, cutoffTStamp, viewingAgentID);
			}
			
		});		
		if (actionSpecList == null) {
			actionSpecList = new java.util.ArrayList<ThingActionSpec>();
		}
		return actionSpecList;		
	}
	private List<ThingActionSpec> viewActionsAndMark_Raw(RepoClient rc, Ident srcGraphID, Long cutoffTStamp, Ident viewingAgentID) {	
		InitialBinding queryIB = rc.makeInitialBinding();
		Literal cutoffTimeLit = rc.getDefaultRdfNodeTranslator().makeTypedLiteral(cutoffTStamp.toString(), XSDDatatype.XSDlong);
		queryIB.bindNode(ThingCN.V_cutoffTStampMsec, cutoffTimeLit);
		queryIB.bindIdent(ThingCN.V_viewingAgentID, viewingAgentID);
		// TODO:  Get the queryVarName exposed by RepoClient, currently it is private.
		// Also:  Consider keeping marker statements in a viewer-specific model, queried in compound with the source 
		// data model (so source data model is not marked by viewers, and viewers marks remain private and resettable)

		String queryGraphVarName = "qGraph"; // RepoSpecDefaultNames.DFLT_TGT_GRAPH_SPARQL_VAR;
		queryIB.bindIdent(queryGraphVarName, srcGraphID);
		SolutionList actionsSolList = rc.queryIndirectForAllSolutions(ThingCN.UNSEEN_ACTION_QUERY_URI, queryIB);
		BasicThingActionQResAdapter taqra = new BasicThingActionQResAdapter();
		// reap invokes buildActionParameterValueMap which invokes rc.queryIndirectForAllSolutions
		List<ThingActionSpec> actionSpecList = taqra.reapActionSpecList(actionsSolList, rc, srcGraphID, SOURCE_AGENT_ID);
		// Delete the actions from graph, so they are not returned on next call to this method.
		for (ThingActionSpec tas : actionSpecList) {
			// TODO:  Make a graphPortal
			markThingActionSeen(rc, srcGraphID, tas, viewingAgentID);
		}
		int listSize = actionSpecList.size();
		if (listSize != 0) {
			theLogger.info("Returning ThingAction list of length {} from graph {}", listSize, srcGraphID);
		} else {
			theLogger.trace("Returning empty ThingAction list from graph {}", srcGraphID);
		}

		return actionSpecList;
	}

	/**
	 * Q: Under what conditions are we allowed to do this directly through Dataset.getNamedModel() actions? A: Not sure
	 * - the clean-est way is to generate SPARQL-UPDATE and apply. If we are allowed to modify the model directly using
	 * Jena API, then it will be sufficient (for immediate practical purposes) to delete all triples with actionIdent as
	 * SUBJECT.
	 *
	 * @param tas
	 */
	@Deprecated private void old_deleteThingAction(RepoClient rc, Ident graphID, ThingActionSpec tas) {
		Ident actionID = tas.getActionSpecID();
		Resource actionRes = rc.getDefaultRdfNodeTranslator().makeResourceForIdent(actionID);
		//Repo.WithDirectory repo = rc.getRepo();
		theLogger.error("FIXME:  About to fetch a readonly model and then try to write to it, which will have no effect - FIXME!!!");
		Model gm = rc.getNamedModelReadonly(graphID); //  repo.getNamedModel(graphID);    FIXME
		theLogger.info("Prior to removal from {}, graph size is {}", graphID, gm.size());
		gm.removeAll(actionRes, null, null);
		theLogger.info("After remova from {}, graph size is {}", graphID, gm.size());
	}

	// This should be done inside a write-Xaction, with boundaries that encompass the prior related reads.
	// Since it does not start an explicit xact, it expects to get an automatic xact, if one is needed.
	private void markThingActionSeen(RepoClient rc, Ident graphToMark, ThingActionSpec tas, Ident seeingAgentID) {
		Ident actionID = tas.getActionSpecID();
		Resource actionRes = rc.getDefaultRdfNodeTranslator().makeResourceForIdent(actionID);
		Resource agentRes = rc.getDefaultRdfNodeTranslator().makeResourceForIdent(seeingAgentID);

		Model gm = com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel();
		scala.Option<String> none = scala.Option.apply(null);  // yields None
		ResourceResolver rr = new ResourceResolver(gm, none);
		Property viewedByProp = rr.findOrMakeProperty(gm, ThingCN.P_viewedBy);
		Statement viewedByStmt = gm.createStatement(actionRes, viewedByProp, agentRes);
		
		gm.add(viewedByStmt);
		theLogger.info("Made new-style statement for thing-seen-mark : {}, submitting through RC-portal-wrapper on the assumption that we are already in a Write TX!", viewedByStmt);
		rc.postNamedModel(graphToMark, gm);
	}

}
