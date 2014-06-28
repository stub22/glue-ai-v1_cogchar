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
package org.cogchar.impl.thing.basic;

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
import org.appdapter.core.store.InitialBinding;
import org.appdapter.core.store.Repo;
import org.appdapter.help.repo.RepoClient;
import org.appdapter.help.repo.SolutionList;
import org.appdapter.impl.store.ResourceResolver;
import org.cogchar.api.thing.ThingActionSpec;
import static org.cogchar.impl.thing.basic.BasicThingActionQResAdapter.buildActionParameterValueMap;
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

	/**
	 * Unused we think. Fetches pending ThingActions from model, and [ USED TO BUT DISABLED SINCE AT LEAST 2013-Oct
	 * physically deletes them (or at least the part of them that makes them matchable) from source model.]
	 *
	 * @param rc
	 * @param srcGraphID
	 * @return
	 */
	/*
	 @Deprecated protected List<ThingActionSpec> viewActions(RepoClient rc, Ident srcGraphID) {
	 SolutionList actionsSolList = rc.queryIndirectForAllSolutions(ThingCN.ACTION_QUERY_URI, srcGraphID);
	 BasicThingActionQResAdapter taqra = new BasicThingActionQResAdapter();
	 List<ThingActionSpec> actionSpecList = taqra.reapActionSpecList(actionsSolList, rc, srcGraphID, SOURCE_AGENT_ID);
	 // DISABLED:    Delete the actions from graph, so they are not returned on next call to this method.
	 for (ThingActionSpec tas : actionSpecList) {
	 // This actual deletion has been commented out since at least Revision 1244 (October 2013)
	 //deleteThingAction(rc, srcGraphID, tas);
	 }
	 int listSize = actionSpecList.size();
	 if (listSize != 0) {
	 theLogger.info("Returning ThingAction list of length {} from graph {}", listSize, srcGraphID);
	 } else {
	 theLogger.trace("Returning empty ThingAction list from graph {}", srcGraphID);
	 }
	 return actionSpecList;
	 }
	 */
	
	static int dubiousSleepMsec = 100;
	// Used only from PUMA to drain thingActions during init/reset, right?
	@Deprecated protected List<ThingActionSpec> takeThingActions_Safe(RepoClient rc, Ident srcGraphID) {
		try {
			return takeThingActions_Unsafe(rc, srcGraphID);
		} catch (Exception e) {
			theLogger.error(" takeThingActions " + e, e);
			try {
				Thread.sleep(dubiousSleepMsec);
			} catch (InterruptedException ee) {
				theLogger.error("Thread.sleep(" + dubiousSleepMsec + ")" + ee, ee);
			}
			return new java.util.ArrayList<ThingActionSpec>();
		}
	}

	private List<ThingActionSpec> takeThingActions_Unsafe(RepoClient rc, Ident srcGraphID) {
		// Will need to become transactional if used concurrently, like viewActions below.
		SolutionList actionsSolList = rc.queryIndirectForAllSolutions(ThingCN.ACTION_QUERY_URI, srcGraphID);
		BasicThingActionQResAdapter taqra = new BasicThingActionQResAdapter();
		List<ThingActionSpec> actionSpecList = taqra.reapActionSpecList(actionsSolList, rc, srcGraphID, SOURCE_AGENT_ID);
		// Delete the actions from graph, so they are not returned on next call to this method.
		for (ThingActionSpec tas : actionSpecList) {
			deleteThingAction(rc, srcGraphID, tas);
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

	public List<ThingActionSpec> viewActionsAndMark_TX(RepoClient rc, Ident srcGraphID, Long cutoffTStamp, Ident viewingAgentID) {
		List<ThingActionSpec> actionSpecList = null;
		com.hp.hpl.jena.query.Dataset  txDataset = null; // if this becomes non-null, then we are taking responsibility for transaction boundary.
		Repo.WithDirectory assumedRepo = rc.getRepo();
		if (assumedRepo != null) {
			// This is probably not, semantically, the "best" dataset for thingAction transfer, but that's another topic.
			com.hp.hpl.jena.query.Dataset dset = assumedRepo.getMainQueryDataset();
			Boolean supportsTrans = dset.supportsTransactions();
			Boolean alreadyInTrans = null;
			if (supportsTrans) {
				alreadyInTrans = dset.isInTransaction();
			}	
			if (supportsTrans  && (!alreadyInTrans)) {			
				// We need to start a write transaction, and then remember to commit or abort it.
				txDataset = dset;
				theLogger.info("Bracketing for TRANSACTIONAL write on dataset {}", dset);
				txDataset.begin(com.hp.hpl.jena.query.ReadWrite.WRITE);			
			} else {
				theLogger.info("Performing unbracketed read+write on dataset {}, supportsTrans={}, alreadyInTrans={}", dset, supportsTrans, alreadyInTrans);
			}
		} else {
			theLogger.warn("Could not find assumedRepo - remote repoClient?");
		}
		try {
			actionSpecList = viewActionsAndMark_Raw(rc, srcGraphID, cutoffTStamp, viewingAgentID);
			if (txDataset != null) {
				txDataset.commit();
			}
		} catch (Throwable t) {
			theLogger.error("Error during read/write operation on thingAction graph, will rollback.", t);
			if (txDataset != null) {
				txDataset.abort();
			}
		} finally { 
			if (txDataset != null) {
				txDataset.end();  // "end()" is not strictly necessary, according to Jena mailing lists
			}
		}
		if (actionSpecList == null) {
			actionSpecList = new java.util.ArrayList<ThingActionSpec>();
		}
		return actionSpecList;		
	}
	private List<ThingActionSpec> viewActionsAndMark_Raw(RepoClient rc, Ident srcGraphID, Long cutoffTStamp, Ident viewingAgentID) {	
		InitialBinding queryIB = rc.makeInitialBinding();
		Literal cutoffTimeLit = rc.makeTypedLiteral(cutoffTStamp.toString(), XSDDatatype.XSDlong);
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
	private void deleteThingAction(RepoClient rc, Ident graphID, ThingActionSpec tas) {
		Ident actionID = tas.getActionSpecID();
		Resource actionRes = rc.makeResourceForIdent(actionID);
		Repo.WithDirectory repo = rc.getRepo();
		Model gm = repo.getNamedModel(graphID);
		theLogger.info("Prior to removal from {}, graph size is {}", graphID, gm.size());
		gm.removeAll(actionRes, null, null);
		theLogger.info("After remova from {}, graph size is {}", graphID, gm.size());
	}

	// This should be done inside a write-Xaction, with boundaries that encompass the prior related reads.
	private void markThingActionSeen(RepoClient rc, Ident graphToMark, ThingActionSpec tas, Ident seeingAgentID) {
		Ident actionID = tas.getActionSpecID();
		Resource actionRes = rc.makeResourceForIdent(actionID);
		Resource agentRes = rc.makeResourceForIdent(seeingAgentID);
		Repo.WithDirectory repo = rc.getRepo();
		Model gm = repo.getNamedModel(graphToMark);
		scala.Option<String> optStr = scala.Option.apply(null);
		ResourceResolver rr = new ResourceResolver(gm, optStr);
		Property viewedByProp = rr.findOrMakeProperty(gm, ThingCN.P_viewedBy);
		Statement viewedByStmt = gm.createStatement(actionRes, viewedByProp, agentRes);
		gm.add(viewedByStmt);
	}
}
