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

package org.cogchar.blob.ghost

import org.appdapter.core.name.{ Ident, FreeIdent }
import org.appdapter.fancy.log.VarargsLogging
import com.hp.hpl.jena
import org.ontoware.rdf2go

import jena.query.{Query, QuerySolution, QuerySolutionMap, Dataset, ParameterizedSparqlString}
import jena.shared.PrefixMapping;

import com.hp.hpl.jena.query.{Query, QueryExecution, QueryExecutionFactory, Dataset, ResultSet, QuerySolution}
import com.hp.hpl.jena.rdf.model.{ResourceFactory, Literal}
import com.hp.hpl.jena.vocabulary.{XSD, RDF, RDFS, OWL, OWL2, DC}

import org.appdapter.bind.rdf.jena.query.JenaArqQueryFuncs_TxAware.Oper
import org.appdapter.bind.rdf.jena.query.{JenaArqResultSetProcessor, JenaArqQueryFuncs}

import org.appdapter.fancy.gportal.{ GraphPortal, DelegatingPortal, GraphSupplier, GraphQuerier, GraphAbsorber }
import org.appdapter.core.store.RepoOper

import org.appdapter.fancy.model.ResourceResolver

trait QueryUtil {
	
	protected def getPrefixResolverModel : jena.rdf.model.Model
	protected def getResolver : ResourceResolver
	
	def bindID (qsMap : QuerySolutionMap, varName : String, id : Ident, resolverJM : jena.rdf.model.Model) : Unit = {
		val jenaRes :  jena.rdf.model.Resource = resolverJM.createResource(id.getAbsUriString) // see FIXME above
		qsMap.add(varName, jenaRes)
	}
	def bindID (qsMap : QuerySolutionMap, varName : String, id : Ident) : Unit = {
		bindID(qsMap, varName, id, getPrefixResolverModel)
	}
	def bindLitPlain(qsMap : QuerySolutionMap, varName : String, plainLitTxt : String) : Unit = {
		val		plainLit : Literal = ResourceFactory.createPlainLiteral(plainLitTxt)
		qsMap.add(varName, plainLit)
	}
	def bindLitTyped(qsMap : QuerySolutionMap, varName : String, litObj : java.lang.Object) : Unit = {
		val		lit : Literal = ResourceFactory.createTypedLiteral(litObj)
		qsMap.add(varName, lit)
	}	
	
	// def makeParameterizedQuery
	def execSelectQuery(graphQuerier : GraphQuerier, parsedQuery : Query) : List[QuerySolution] = {
		// QueryExec can only be used once.
		val queryExec : QueryExecution = graphQuerier.makeQueryExec(parsedQuery)
		val qsols : List[QuerySolution] = graphQuerier.gulpingSelect_ReadTransCompatible(queryExec)
		qsols
	}
	def makeParamSparqlWithDefaultPrefixes(sparqlTxt : String) : ParameterizedSparqlString = {
		new ParameterizedSparqlString(sparqlTxt, getPrefixResolverModel)
	}
	def identToJenaRes(id: Ident, resModel : jena.rdf.model.Model) : jena.rdf.model.Resource =  {
		// TODO: Check if the id is already a JenaResourceItem!
		getResolver.findOrMakeResource(resModel, id.getAbsUriString)
	}
	def identToJenaRes(id: Ident) : jena.rdf.model.Resource =  {
		identToJenaRes(id, getPrefixResolverModel)
	}
	def qNameOrUriToJenaRes(qNameOrURI : String) : jena.rdf.model.Resource  = {
		getResolver.findOrMakeResource(getPrefixResolverModel, qNameOrURI) // see FIXME above
	}
}
/*
 * http://permalink.gmane.org/gmane.comp.apache.jena.user/2842
 * 
 * http://answers.semanticweb.com/questions/23154/is-there-a-difference-between-using-sparql-values-keyword-inside-a-graph-pattern-and-outside
 * 
 * http://stackoverflow.com/questions/17766541/saving-and-reusing-the-result-of-a-sparql-query
 * 
 * 
 *         System.out.println( "\n== useValuesFromResultSet ==" );
        final ResultSet namedResults = QueryExecutionFactory.create( findNamed, model ).execSelect();
        final QueryExecution qe = QueryExecutionFactory.create( findPersonAddress, model );
        System.out.println( "=== Query Before Adding VALUES ===\n" + qe.getQuery() );
        // Create a list of the variables from the result set
        List<Var> variables = new ArrayList<>();
        for ( final String varName : namedResults.getResultVars() ) {
            variables.add( Var.alloc( varName ));
        }
        // Create a list of the bindings from the result set.
        List<Binding> values = new ArrayList<>();
        while ( namedResults.hasNext() ) {
            values.add( namedResults.nextBinding() );
        }
        // add a values block to the query
        qe.getQuery().setValuesDataBlock(variables, values);
        System.out.println( "\n=== Query After Adding VALUES ===\n" + qe.getQuery() );
        ResultSetFormatter.out( qe.execSelect() );
 */