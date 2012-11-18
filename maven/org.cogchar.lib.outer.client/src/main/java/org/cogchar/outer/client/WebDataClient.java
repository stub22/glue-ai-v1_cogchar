/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.outer.client;

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
import org.slf4j.Logger;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class WebDataClient extends BasicDebugger {

	protected HttpClient myHttpCli = new DefaultHttpClient();

	public String execGet(String url, boolean debugFlag, String rqSummary) {
		String resultText = null;
		try {
			resultText = execGetRequest(myHttpCli, url, getLogger(), debugFlag, rqSummary);
		} catch (Throwable t) {
			getLogger().error("Problem during execGet", t);
		}
		return resultText;
	}

	protected static String execGetRequest(HttpClient httpCli, String url, Logger log, boolean debugFlag, String rqSummary)
			throws Throwable {
		String resultText = null;
		HttpGet getReq = new HttpGet(url);
		HttpResponse response = httpCli.execute(getReq);
		resultText = extractResponseEntityText(response, log, debugFlag, rqSummary);
		return resultText;
	}
	public String execPost (String url, List<NameValuePair> nvps, boolean debugFlag) {
		String resultText = null;
		try {
			resultText = execPostRequest(myHttpCli, url, nvps, getLogger(), debugFlag);
		} catch (Throwable t) {
			getLogger().error("Problem during execGet", t);
		}
		return resultText;
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
	protected static String execPostRequest(HttpClient httpCli, String postURL, List<NameValuePair> nvps,
			Logger log, boolean debugFlag) throws Throwable {

		log.info("Building post request for URL: " + postURL);
		HttpPost postReq = new HttpPost(postURL);
		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
		postReq.setEntity(formEntity);
		if (debugFlag) {
			WebDataDumper.dumpRequestInfo(postReq, log);
		}
		String rqSummary = "Posting [URL=" + postURL + ", pairs=[" + nvps + "]]";
		HttpResponse response = httpCli.execute(postReq);
		String resultText = extractResponseEntityText(response, log, debugFlag, rqSummary);
		if (debugFlag) {
			WebDataDumper.dumpResponseInfo(response, rqSummary, resultText, log);
		}
		return resultText;
	}
/**
 * Extract the meaty text part of an HTTP response, optionally printing debug along the way.
 * @param response
 * @param log
 * @param debugFlag
 * @param rqSummary
 * @return
 * @throws Throwable 
 */
	protected static String extractResponseEntityText(HttpResponse response, Logger log, boolean debugFlag, String rqSummary)
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
}
