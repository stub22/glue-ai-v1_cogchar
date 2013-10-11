package org.cogchar.ext.bundle.fuseki;

import java.net.Socket;

import net.liftweb.actor.PingerException;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.fuseki.Fuseki;
import org.apache.jena.fuseki.server.FusekiConfig;
import org.apache.jena.fuseki.server.SPARQLServer;
import org.apache.jena.fuseki.server.ServerConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.modify.request.Target;
import com.hp.hpl.jena.sparql.modify.request.UpdateDrop;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.update.Update;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.sun.org.apache.bcel.internal.generic.NEW;

public class Activator implements BundleActivator, FrameworkListener {

	public void start(BundleContext context) throws Exception {
		context.addFrameworkListener(this);
	}

	public void stop(BundleContext context) throws Exception {
		// TODO add deactivation code here
		serverStop();
	}

	@Override public void frameworkEvent(FrameworkEvent event) {
		// TODO Auto-generated method stub
		if (event.getType() == FrameworkEvent.STARTED) {
			System.out.println("STARTED Event=" + event + " for bundle " + event.getBundle());
			SPARQLServer.überServlet = true;
			allocServer();
		}
	}

	// Abstraction that runs a SPARQL server for tests.

	public static final int port = 3030;
	public static final String urlRoot = "http://localhost:" + port + "/";
	public static final String datasetPath = "/dataset";
	public static final String serviceUpdate = "http://localhost:" + port + datasetPath + "/update";
	public static final String serviceQuery = "http://localhost:" + port + datasetPath + "/query";
	public static final String serviceREST = "http://localhost:" + port + datasetPath + "/data"; // ??????

	public static final String gn1 = "http://graph/1";
	public static final String gn2 = "http://graph/2";
	public static final String gn99 = "http://graph/99";

	public static final Node n1 = NodeFactory.createURI("http://graph/1");
	public static final Node n2 = NodeFactory.createURI("http://graph/2");
	public static final Node n99 = NodeFactory.createURI("http://graph/99");

	public static final Graph graph1 = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 1)))");
	public static final Graph graph2 = SSE.parseGraph("(base <http://example/> (graph (<x> <p> 2)))");

	public static final Model model1 = ModelFactory.createModelForGraph(graph1);
	public static final Model model2 = ModelFactory.createModelForGraph(graph2);

	private static SPARQLServer server = null;
	private static Object serverUpDownLockObject = new Object();
	// reference count of start/stop server
	private static int referenceCount = 0;

	// This will cause there to be one server over all tests.
	// Must be after initialization of counters
	//static { allocServer() ; }

	@BeforeClass static public void allocServer() {
		synchronized (serverUpDownLockObject) {
			if (referenceCount == 0)
				serverStart();
			referenceCount++;
		}
	}

	@AfterClass static public void freeServer() {
		synchronized (serverUpDownLockObject) {
			referenceCount--;
			if (referenceCount == 0)
				serverStop();
		}
	}

	protected static void serverStart() {
		synchronized (serverUpDownLockObject) {

			Log.logLevel(Fuseki.serverLog.getName(), org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING);
			Log.logLevel(Fuseki.requestLog.getName(), org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING);
			Log.logLevel("org.eclipse.jetty", org.apache.log4j.Level.WARN, java.util.logging.Level.WARNING);

			DatasetGraph dsg = DatasetGraphFactory.createMem();
			// This must agree with BaseServerTest
			ServerConfig conf = FusekiConfig.defaultConfiguration(datasetPath, dsg, true);
			conf.port = port;
			conf.pagesPort = port;
			//        public static final String serviceUpdate = "http://localhost:"+ServerTest.port+datasetPath+"/update" ;
			//        public static final String serviceQuery  = "http://localhost:"+ServerTest.port+datasetPath+"/query" ;
			//        public static final String serviceREST   = "http://localhost:"+ServerTest.port+datasetPath+"/data" ; // ??????

			server = new SPARQLServer(conf);
			try {
				server.start();
			} catch (Throwable t) {
				t.printStackTrace();
				server = null;
				referenceCount--;
			}
		}
	}

	protected static void serverStop() {
		synchronized (serverUpDownLockObject) {

			if (server != null) {
				server.stop();
				server = null;
			}
		}
		Log.logLevel(Fuseki.serverLog.getName(), org.apache.log4j.Level.INFO, java.util.logging.Level.INFO);
		Log.logLevel(Fuseki.requestLog.getName(), org.apache.log4j.Level.INFO, java.util.logging.Level.INFO);
		Log.logLevel("org.eclipse.jetty", org.apache.log4j.Level.INFO, java.util.logging.Level.INFO);
	}

	public static void resetServer() {
		synchronized (serverUpDownLockObject) {
			Update clearRequest = new UpdateDrop(Target.ALL);
			UpdateProcessor proc = UpdateExecutionFactory.createRemote(clearRequest, Activator.serviceUpdate);
			proc.execute();
		}
	}
}
