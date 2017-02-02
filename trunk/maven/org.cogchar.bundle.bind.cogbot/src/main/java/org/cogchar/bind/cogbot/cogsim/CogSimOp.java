/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.cogbot.cogsim;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stu B.
 */
public class CogSimOp {

	private static final Logger theLogger = LoggerFactory.getLogger(CogSimOp.class);
	HttpClient myClient;
	CogSimConf myConf;
	CogbotAvatar avatar;

	public CogSimOp(CogbotAvatar av, CogSimConf conf, HttpClient client) {
		myClient = (client != null) ? client : new DefaultHttpClient();
		avatar = av;
		myConf = conf;
	}

	public void postActionReqToCogbot(final String cmd, final String args, final boolean dump) {
		avatar.InvokeSerialAction(new Runnable() {
			public void run() {
				try {
					postActionReqToCogbotNow(cmd, args, dump);
				} catch (Throwable ex) {
					org.slf4j.LoggerFactory.getLogger(CogSimOp.class).error(ex.getMessage(), ex);
				}
			}
		});
	}

	public void postActionReqToCogbotNow(String cmd, String args, boolean dump) throws Throwable {
		String doURL = myConf.findOpURL(CogSimConf.Op.DO_ACTION);
		theLogger.info("Posting to URL: " + doURL);
		HttpPost postReq = new HttpPost(doURL);

		List<NameValuePair> nvps = new ArrayList<>();
		nvps.add(new BasicNameValuePair("cmd", cmd));
		nvps.add(new BasicNameValuePair("args", args));
		UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nvps, HTTP.UTF_8);
		postReq.setEntity(formEntity);
		Header[] allHeaders = postReq.getAllHeaders();
		theLogger.info("POST header count: " + allHeaders.length);
		for (Header h : allHeaders) {
			theLogger.info("Header: " + h);
		}
		theLogger.info("Request method: " + postReq.getMethod());
		theLogger.info("Request line: " + postReq.getRequestLine());
		HttpResponse response = myClient.execute(postReq);
		String rqSummary = "Posted [cmd=" + cmd + ", args=" + args + "]";
		String resultText = responseEntityText(response, dump, rqSummary);
	}

	public void dumpResponseInfo(HttpResponse response, String rqSummary, String entityText)
			throws Throwable {
		theLogger.info("RequestSummary: " + rqSummary);
		if (response != null) {
			theLogger.info("Response status line: " + response.getStatusLine());
		} else {
			theLogger.warn("Got null response to request: " + rqSummary);
		}
	}

	public String responseEntityText(HttpResponse response, boolean debug, String rqSummary) throws Throwable {
		String entityText = null;

		HttpEntity resEntity = response.getEntity();
		if (resEntity != null) {
			if (debug) {
				theLogger.debug("Got response entity: " + resEntity);
				theLogger.debug("Response content length: " + resEntity.getContentLength());
				theLogger.debug("Chunked?: " + resEntity.isChunked());
			}
			entityText = EntityUtils.toString(resEntity);
			resEntity.consumeContent();
		} else {
			theLogger.warn("No entity attached to response to request: " + rqSummary);
		}
		if (debug) {
			dumpResponseInfo(response, rqSummary, entityText);
		}
		return entityText;
	}

	protected String execGetLastThingReq(String url, String rqSummary, boolean debug) throws Throwable {
		String resultText = null;
		try {
			HttpGet getReq = new HttpGet(url);
			HttpResponse response = myClient.execute(getReq);
			resultText = responseEntityText(response, debug, rqSummary);
		} catch (Exception e) {
		}
		return resultText;
	}

	public String fetchLastThingWeSaid(boolean debug) throws Throwable {
		String thingWeSaidDocText = execGetLastThingReq(myConf.findOpURL(CogSimConf.Op.GET_SAID), "get last thing SAID", debug);
		String thingWeSaid = "extracted from docText: " + thingWeSaidDocText;
		return thingWeSaid;
	}

	public String fetchLastThingWeHeard(boolean debug) throws Throwable {
		String thingWeHeardDocText = execGetLastThingReq(myConf.findOpURL(CogSimConf.Op.GET_HEARD), "get last thing HEARD", debug);
		// String thingWeHeard = "extracted from docText: " + thingWeHeardDocText;
		String thingWeHeard = null;
		if (thingWeHeardDocText != null) {
			thingWeHeard = extractValueText(thingWeHeardDocText);
		}
		return thingWeHeard;
	}

	public String extractValueText(String xml) throws Throwable {
		Document doc = DocumentHelper.parseText(xml);
		theLogger.debug("Parsed Response Doc: " + doc.asXML());
		Node valueNode = doc.selectSingleNode("/xml/slot/value");
		String valueText = null;
		if (valueNode != null) {
			valueText = valueNode.getText();
		}
		return valueText;
	}

	public void unusedSetPostSessionParam(HttpPost postReq, String paramVal) {
		HttpParams params = new BasicHttpParams();
		params.setParameter("p1", paramVal);
		params.setParameter("thingy", "Do it CogBot!");
		theLogger.info("Post Admin Params: " + params);
		postReq.setParams(params);

	}
}
