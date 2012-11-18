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
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.appdapter.bind.rdf.jena.model.JenaModelUtils;
import org.appdapter.core.log.BasicDebugger;
import org.slf4j.Logger;

/**
 * Some of this code was copied-and-modified from our old web-client for Cogbot.
 */
public class TestOuterClientSOH extends BasicDebugger {
	// Fun data queries to DBPedia.
	public static String dbpediaServiceURL = "http://dbpedia.org/sparql";
	
	// Our test repository URLs.
	public static String repoBaseURL = "http://localhost:8080/friendu_joseki/";
	public static String repoBaseQryURL = repoBaseURL + "sparql/";
	public static String repoBaseUpdURL = repoBaseURL + "sparql-update/";
	public static String bookSvcURL = repoBaseQryURL + "books";
	public static String glueQryURL = repoBaseQryURL + "glue-ai";
	public static String glueUpdURL = repoBaseUpdURL + "glue-ai";
	
	AgentRepoClient	myAgentRepoClient = new AgentRepoClient();

	public static void main(String[] args) {
		TestOuterClientSOH test = new TestOuterClientSOH();
		test.runTests();
	}

	public TestOuterClientSOH() {
		forceLog4jConfig();
	}
	
	public void runTests() { 
		// Fun, but gives long result list
		// runRemoteSparqlSelectAndPrintResults (dbpediaServiceURL, dbpQ);
		runRemoteSparqlSelectAndPrintResults(bookSvcURL, bookQ);
		runRemoteSparqlSelectAndPrintResults(glueQryURL, glueQ );
		
		// Here we write to a remote SPARQL repo, adding triples to it.  
		// This writing can effectively "command" the system embedding that repo to take action.
		myAgentRepoClient.execRemoteSparqlUpdate(glueUpdURL, glueUpSilly);
		// However, to see the effect of the action, we must monitor the state of the remote repo.
		// By convention, our writes are restricted to contain only INSERT (not DELETE), except
		// under special admin circumstances, under which we have less assurance of server 
		// smoothness, and timeliness of updates.		
	}
	public void runRemoteSparqlSelectAndPrintResults(String svcUrl, String queryTxt) {
		Logger log = getLogger();
		ResultSet resSet = myAgentRepoClient.execRemoteSparqlSelect(svcUrl, queryTxt);
		ResultSetRewindable rewindableResSet = ResultSetFactory.makeRewindable(resSet);
		String resultXML = ResultSetFormatter.asXMLString(rewindableResSet);
		log.info("ResultSet as XML: \n" + resultXML);
	}


	static String PREFIX_FOAF = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n",
			PREFIX_XSD = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n",
			PREFIX_DC = "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n",
			PREFIX_DBO = "PREFIX dbo: <http://dbpedia.org/ontology/>\n",
			PREFIX_BOOKS = "PREFIX books:   <http://example.org/book/>\n",
			PREFIX_CCRT = "PREFIX ccrt:  <urn:ftd:cogchar.org:2012:runtime#>\n",
			PREFIX_UA = "PREFIX ua:    <http://www.cogchar.org/lift/user/config#>\n";
	
	// Regular SPARQL queries, sent through Jena/ARQ client infrastructure
	
	static String dbpQ =
			PREFIX_DBO + PREFIX_FOAF + PREFIX_XSD
			+ "PREFIX : <http://dbpedia.org/resource/>\n"
			+ "SELECT ?name ?birth ?death ?person WHERE {\n"
			+ "?person dbo:birthPlace :Berlin .   ?person dbo:birthDate ?birth .   ?person foaf:name ?name .\n"
			+ "?person dbo:deathDate ?death .   FILTER (?birth < '1900-01-01'^^xsd:date) }";
	static String bookQ =
			PREFIX_BOOKS + PREFIX_DC + "SELECT ?book ?title  WHERE   { ?book dc:title ?title }";
	static String glueQ = PREFIX_CCRT + PREFIX_UA
			+ "SELECT ?user ?password ?salt ?startPage WHERE { GRAPH ?qGraph { \n"
			+ "?user a ccrt:userConfig; ua:password ?password; ua:salt ?salt; ua:homePage ?startPage.}}";
	
	// Below is SPARQL-Update text, sent through HTTP post
	
	static String glueUpSilly = PREFIX_CCRT + PREFIX_UA + PREFIX_DC
			+ "INSERT DATA {  GRAPH   ccrt:user_access_sheet_22 { \n"
			+ "<http://yeah/nutty> dc:title 'How to be revolting' ;    dc:creator 'Spart A. Cus'. }}\n";
}
