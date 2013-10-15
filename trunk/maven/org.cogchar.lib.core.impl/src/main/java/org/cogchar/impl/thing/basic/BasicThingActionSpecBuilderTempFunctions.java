
package org.cogchar.impl.thing.basic;


import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.appdapter.core.log.BasicDebugger;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;


import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.appdapter.core.log.BasicDebugger;
import org.slf4j.Logger;

import com.hp.hpl.jena.query.*;
import java.util.ArrayList;

/**
 * THIS WILL BE REPLACED
 *
 * @author Jason Randolph Eads <jeads362@gmail.com>
 */


public class BasicThingActionSpecBuilderTempFunctions extends BasicDebugger {
    
    // ENTER HERE, listed in order of initial usage
    //From AgentRepoClient
	public void execRemoteSparqlUpdate(String DO_NOT_USE_svcUrl, String updateText, boolean debugFlag) {

        String svcUrl = glueUpdURL;
        
		getLogger().debug("Sending update message:\n{}", updateText);
        
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("request", updateText));
		try {
			execPost(svcUrl, nvps, debugFlag);
		} catch (Throwable t) {
			getLogger().error("Caught Exception: ", t);
		}
	}
    
    //FROM AgentAmbassador
    private static final String repoBaseURL = "http://localhost:8080/cchr_josk/";
	private static final String repoBaseUpdURL = repoBaseURL + "sparql-update/";
	private static final String glueUpdURL = repoBaseUpdURL + "glue-ai";
    
    //From WebDataClient
    private String execPost (String url, List<NameValuePair> nvps, boolean debugFlag) {
		String resultText = null;
        HttpClient httpCli = new DefaultHttpClient();
		try {
			resultText = execPostRequest(httpCli, url, nvps, getLogger(), debugFlag);
		} catch (Throwable t) {
			getLogger().error("Problem during execGet", t);
		}
		return resultText;
	}
    
    //From WebDataClient
    private static String execPostRequest(HttpClient httpCli, String postURL, List<NameValuePair> nvps,
			Logger log, boolean debugFlag) throws Throwable {

		log.info("Building post request for URL: " + postURL);
		HttpPost postReq = new HttpPost(postURL);
		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
		postReq.setEntity(formEntity);
		String rqSummary = "Posting [URL=" + postURL + "]";
		if (debugFlag) {
			rqSummary = rqSummary + ", pairs=[" + nvps + "]";
            dumpRequestInfo(postReq, log);
		}
		
		HttpResponse response = httpCli.execute(postReq);
		log.debug("HttpClient returned a response, now extracting");
		String resultText = extractResponseEntityText(response, log, debugFlag, rqSummary);
		if (debugFlag) {
            dumpResponseInfo(response, rqSummary, resultText, log);
		}
		return resultText;
	}
    
    
    // FROM WebDataDumper
	private static void dumpRequestInfo(HttpPost postReq, Logger log) {
		log.info("Request method: " + postReq.getMethod());
		log.info("Request line: " + postReq.getRequestLine());
		Header[] allHeaders = postReq.getAllHeaders();
		log.info("POST header count: " + allHeaders.length);
		for (Header h : allHeaders) {
			log.info("Header: " + h);
		}
	}
    
    //From WebDataClient
    private static String extractResponseEntityText(HttpResponse response, Logger log, boolean debugFlag, String rqSummary)
			throws Throwable {
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

    // FROM WebDataDumper
	private static void dumpResponseInfo(HttpResponse response, String rqSummary, String entityText, Logger log)
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
}
