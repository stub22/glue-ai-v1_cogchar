/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cogchar.bind.cogbot.cogsim;

import java.util.logging.Logger;
import org.cogchar.bind.cogbot.main.CogbotService;
import org.cogchar.bind.cogbot.main.CogbotAvatar;

/**
 * @author Stu B.
 */
public class TestCogSim {

	private static Logger theLogger = Logger.getLogger(TestCogSim.class.getName());

	public static void main(String args[]) {
		try {
			String message = "TestCogSim sez the time is: " + System.currentTimeMillis();
			CogbotAvatar cso = CogbotService.getDefaultAvatar();
			theLogger.info("Saying: [" + message + "]");
			cso.postActionReqToCogbot("say", message, true);
			String lastSaid = cso.fetchLastThingWeSaid(true);
			theLogger.info("Last thing we said: " + lastSaid);
			String lastHeard = cso.fetchLastThingWeHeard(true);
			theLogger.info("Last thing we heard: " + lastHeard);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/*
	HttpHost target = new HttpHost("www.apache.org", 80, "http");

	// general setup
	SchemeRegistry supportedSchemes = new SchemeRegistry();

	// Register the "http" protocol scheme, it is required
	// by the default operator to look up socket factories.
	supportedSchemes.register(new Scheme("http",
	PlainSocketFactory.getSocketFactory(), 80));

	// prepare parameters
	HttpParams params = new BasicHttpParams();
	HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	HttpProtocolParams.setContentCharset(params, "UTF-8");
	HttpProtocolParams.setUseExpectContinue(params, true);

	ClientConnectionManager connMgr = new ThreadSafeClientConnManager(params,
	supportedSchemes);
	DefaultHttpClient httpclient = new DefaultHttpClient(connMgr, params);

	HttpGet req = new HttpGet("/");

	System.out.println("executing request to " + target);

	HttpResponse rsp = httpclient.execute(target, req);
	HttpEntity entity = rsp.getEntity();

	System.out.println("----------------------------------------");
	System.out.println(rsp.getStatusLine());
	Header[] headers = rsp.getAllHeaders();
	for (int i = 0; i < headers.length; i++) {
	System.out.println(headers[i]);
	}
	System.out.println("----------------------------------------");

	if (entity != null) {
	System.out.println(EntityUtils.toString(entity));
	}

	// When HttpClient instance is no longer needed,
	// shut down the connection manager to ensure
	// immediate deallocation of all system resources
	httpclient.getConnectionManager().shutdown();
	 */
}
