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
	HttpClient myHttpCli = new DefaultHttpClient();

	public static void main(String[] args) {
		TestOuterClientSOH test = new TestOuterClientSOH();

		// Fun, but gives long result list
		// test.runQuery (dbpQ, dbpediaServiceURL);

		test.runQuery(bookQ, bookSvcURL);
		test.runQuery(glueQ, glueQryURL);
		test.runUpdate(glueUpSilly, glueUpdURL);
	}

	public TestOuterClientSOH() {
		forceLog4jConfig();
	}

	public void runQuery(String queryText, String svcUrl) {
		Logger log = getLogger();
		log.info("QueryUrl=[{}]  QueryText={}", svcUrl, queryText);
		// Create an ARQ parsed query object
		Query parsedQuery = QueryFactory.create(queryText);
		String queryBaseURI = null;
		Syntax queryFileSyntax = Syntax.syntaxSPARQL;
		// Query parsedQuery = QueryFactory.read(queryFileURL, null, Syntax.syntaxSPARQL); // , queryBaseURI, queryFileSyntax);

		QueryExecution qExc = QueryExecutionFactory.sparqlService(svcUrl, parsedQuery);
		ResultSet resSet = qExc.execSelect();
		ResultSetRewindable rewindableResSet = ResultSetFactory.makeRewindable(resSet);
		String resultXML = ResultSetFormatter.asXMLString(rewindableResSet);
		log.info("ResultSet as XML: \n" + resultXML);
	}

	public void runUpdate(String updateText, String svcUrl) {
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("request", updateText));
		try {
			postWebRequestNow(myHttpCli, glueUpdURL, nvps, getLogger(), true);
		} catch (Throwable t) {
			getLogger().error("Caught Exception: ", t);
		}
	}

	/**
	 * General HTTP posting method, which we make static to emphasize that there's no hidden state in it.
	 *
	 * @param httpCli
	 * @param postURL
	 * @param nvps
	 * @param log
	 * @param debugFlag
	 * @throws Throwable
	 */
	public static void postWebRequestNow(HttpClient httpCli, String postURL, List<NameValuePair> nvps,
				Logger log, boolean debugFlag) throws Throwable {
		
		log.info("Building post request for URL: " + postURL);
		HttpPost postReq = new HttpPost(postURL);
		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
		postReq.setEntity(formEntity);
		if (debugFlag) {
			dumpRequestInfo(postReq, log);
		}
		String rqSummary = "Posting [URL=" + postURL + ", pairs=[" + nvps + "]]";
		HttpResponse response = httpCli.execute(postReq);
		String resultText = responseEntityText(response, debugFlag, rqSummary, log);
		if (debugFlag) {
			dumpResponseInfo(response, rqSummary, resultText, log);
		}
	}

	public static String responseEntityText(HttpResponse response, boolean debugFlag, String rqSummary, Logger log) throws Throwable {
		String entityText = null;

		HttpEntity resEntity = response.getEntity();
		if (resEntity != null) {
			if (debugFlag) {
				log.debug("Got response entity: " + resEntity);
				log.debug("Response content length: " + resEntity.getContentLength());
				log.debug("Chunked?: " + resEntity.isChunked());
			}
			entityText = EntityUtils.toString(resEntity);
			resEntity.consumeContent();
		} else {
			log.warn("No entity attached to response to request: " + rqSummary);
		}
		return entityText;
	}

	public static void dumpRequestInfo(HttpPost postReq, Logger log) {
		log.info("Request method: " + postReq.getMethod());
		log.info("Request line: " + postReq.getRequestLine());
		Header[] allHeaders = postReq.getAllHeaders();
		log.info("POST header count: " + allHeaders.length);
		for (Header h : allHeaders) {
			log.info("Header: " + h);
		}
	}

	public static void dumpResponseInfo(HttpResponse response, String rqSummary, String entityText, Logger log)
			throws Throwable {
		log.info("Request Summary: " + rqSummary);

		if (response != null) {
			log.info("Response status line: " + response.getStatusLine());
		} else {
			log.warn("Got null response to request: " + rqSummary);
		}
		if (entityText != null) {
			log.info("Entity Text: " + entityText);
		}
	}
	static String PREFIX_FOAF = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n",
			PREFIX_XSD = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n",
			PREFIX_DC = "PREFIX dc:      <http://purl.org/dc/elements/1.1/>\n",
			PREFIX_DBO = "PREFIX dbo: <http://dbpedia.org/ontology/>\n",
			PREFIX_BOOKS = "PREFIX books:   <http://example.org/book/>\n",
			PREFIX_CCRT = "PREFIX ccrt:  <urn:ftd:cogchar.org:2012:runtime#>\n",
			PREFIX_UA = "PREFIX ua:    <http://www.cogchar.org/lift/user/config#>\n";
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
	static String glueUpSilly = PREFIX_CCRT + PREFIX_UA + PREFIX_DC
			+ "INSERT DATA {  GRAPH   ccrt:user_access_sheet_22 { \n"
			+ "<http://yeah/whatever> dc:title 'a SCARY new book' ;    dc:creator 'Wily Wanker'. }}\n";
}
