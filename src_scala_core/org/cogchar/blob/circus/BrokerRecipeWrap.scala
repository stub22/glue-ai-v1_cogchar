/*
 *  Copyright 2015 by The Cogchar Project (www.cogchar.org).
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
/**
 * @author Stu B. <www.appstract.com>
 */

package org.cogchar.blob.circus

import org.appdapter.fancy.log.VarargsLogging

import org.cogchar.api.owrap
import owrap.crcp.{GhostRecipe, GhRRefer, BrokerRecipe, BRFeature }
import owrap.mdir.{GraphPointer => MdirGraphPointer, GraphHost => MdirGraphHost, GH4SFolder}

import org.ontoware.rdf2go
import rdf2go.model.{Model => R2GoModel}
import rdf2go.model.node.{URI => R2GoURI}

import org.ontoware.rdfreactor
import rdfreactor.runtime.ReactorRuntimeEntity
import rdfreactor.schema.rdfs.{Class => RDFR_Class}


	// To get at the goodies we need (crcp:)Cogchar-Circus-Recipe ontology instance
	// val generalContentBR : BrokerRecipe = new BrokerRecipe(cbrokerModel, contentBrokerURI, false)

class BrokerRecipeWrap(val myRecipe : BrokerRecipe) {
	lazy val myRecipeURI : R2GoURI = myRecipe.asURI
	lazy val myRecipeModel : R2GoModel = myRecipe.getModel

	// Here we follow the ccrp:hasInGhostRecipe property, without knowing the specific sub-types of the values found.
	protected def findInputGHostRecipes : Traversable[GhostRecipe] = myRecipe.getAllInGhostRecipe_as.asArray
	
	// Once we start constructing arrays of wrappers like this, we are kinda leaving behind the updatability of the
	// recipe underneath us.   That is both useful and limiting, so should be done mindfully.
	protected def makePlainWrapsForInputGHostRecipes: Traversable[GHostRecipeWrap] = findInputGHostRecipes.map(new GHostRecipeWrap(_))
	
	def findInputDirectGHost4Folders : Traversable[GH4SFolder] = {
		
		// val cbrw = new ContentBrokerRecipeWrap(miloContentBR)
		
		// This is a "make" step, so we wouldn't want to do it too frequently.
		// Each time we do it, we are getting a snapshot view of to what folderRecipes our brokerRecipe is
		// currently pointing.
	
		val inputGRWs : Traversable[GHostRecipeWrap] = makePlainWrapsForInputGHostRecipes
		
		val refGRWs : Traversable[GHostRecipeWrap] = inputGRWs.filter(_.isDirectGHostReference)
		
		val gHosts : Traversable[owrap.mdir.GraphHost] = refGRWs.flatMap(_.getResultGHost)
	
		// Here we are simply filtering the gHosts by the ones with asserted rdf:type GH4SFolder.
		// This result could thus be affected by whether or not inference is active on the underlying model.
		val gHostFolders : Traversable[GH4SFolder] = gHosts.flatMap(RRUtil.maybePromote(_, GH4SFolder.RDFS_CLASS, classOf[GH4SFolder]))
		
		gHostFolders
	}
	
}

class FeatureBrokerRecipeWrap(featureBrokerRecipe: BRFeature) extends BrokerRecipeWrap(featureBrokerRecipe) {

	
		// The GhostRecipes could in theory be for any combination of Ghost3 and Ghost4 results.
		// They can simply contain literal mdir:Ghost3 and mdir:Ghost4 records, or specify an input pipline to find same.
		// But we start off with the simpler cases where the mdir:GHost records are supplied directly, using :hasGraphHost
		// which probably needs to have owl:maxCardinality = 1.
		// 
		// Not needed now, but to consider later:
		// Should we also look for input *Graph*Recipes (in contrast to *GHost* recipes) in the ContentBroker-Recipe?
		// 
		// Currently (March 2015):
		// We expect these to be mostly Ghost4 recipes for quadstores, in the form of GHost4SerialFolders, which
		// we then resolve against our available HostEntry local folder-stores (e.g. Disk, Bundle, Resource, ...).
}

object BrokerRecipeUtil {
	

	def toFeatureBrokerRecipe(recipeInst : ReactorRuntimeEntity) : BRFeature = {
		// Normally this is used when recipeInst was read using some application ontology that imports crcp:.
		// We do *not* type-check the input against asserted rdf:type, nor do we assert the type!
		// We could instead use RRUtil.promote, which uses reflection to discover the same constructor.
		val recipeURI : R2GoURI = recipeInst.asURI
		val recipeModel : R2GoModel = recipeInst.getModel
		new BRFeature(recipeModel, recipeURI, false)
	}
}