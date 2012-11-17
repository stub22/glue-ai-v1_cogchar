package org.cogchar.outer.client;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetFactory;

import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;
import org.appdapter.bind.rdf.jena.model.JenaModelUtils;
/**
 * Hello world!
 */

public class TestOuterClientSOH {

	public static String	repoServerRootURL = "http://localhost:8080/cgc_repo/";
	public static String	sparqlServiceURL = "http://dbpedia.org/sparql";
	public static String	repoSvcURL = "http://localhost:8080/friendu_joseki/books";
	
	
	public static void main(String[] args) {
		runQuery (dbpQ, sparqlServiceURL);
		runQuery (repoQ, repoSvcURL);
	}
	public static void runQuery (String queryText, String svcUrl) {

		System.out.println("QueryText=\n" + queryText);
		// now creating query object
		Query parsedQuery = QueryFactory.create(queryText);
		String queryBaseURI = null;
		Syntax queryFileSyntax = Syntax.syntaxSPARQL;
		// Query parsedQuery = QueryFactory.read(queryFileURL, null, Syntax.syntaxSPARQL); // , queryBaseURI, queryFileSyntax);
		// initializing queryExecution factory with remote service.
		// **this actually was the main problem I couldn't figure out.**
		QueryExecution qExc = QueryExecutionFactory.sparqlService(svcUrl, parsedQuery);
		ResultSet resSet = qExc.execSelect();
		ResultSetRewindable rewindableResSet = ResultSetFactory.makeRewindable(resSet);
		String resultXML = ResultSetFormatter.asXMLString(rewindableResSet);		
		System.out.println("ResultSet as XML: \n" + resultXML);
	}
static String dbpQ =
"PREFIX dbo: <http://dbpedia.org/ontology/>\n" +
"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
"PREFIX : <http://dbpedia.org/resource/>\n" +
"SELECT ?name ?birth ?death ?person WHERE {\n" +
		"?person dbo:birthPlace :Berlin .   ?person dbo:birthDate ?birth .   ?person foaf:name ?name .\n" +
		"?person dbo:deathDate ?death .   FILTER (?birth < '1900-01-01'^^xsd:date) }";


static String repoQ = 
	"PREFIX books:   <http://example.org/book/>\n" +
	"PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n" +
	"SELECT ?book ?title  WHERE   { ?book dc:title ?title }";
}
