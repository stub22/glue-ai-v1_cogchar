package org.cogchar.lifter.boot

import net.liftweb.http.{Bootable, Html5Properties, LiftRules, Req, LiftSession}
import net.liftweb.http.provider.{HTTPRequest}
import net.liftweb.sitemap.{Menu, SiteMap, Loc}
import net.liftweb.actor.ActorLogger

import org.cogchar.impl.web.config.LiftAmbassador
import org.cogchar.lifter.model.main.{PageCommander};
import org.cogchar.lifter.app.{SpeechRestListener};

import org.jflux.impl.services.rk.lifecycle.{ManagedService, ServiceLifecycleProvider};
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.cogchar.api.thing.WantsThingAction
//import org.cogchar.bundle.app.puma.PumaAppUtils

import java.util.Properties;

import org.cogchar.impl.web.util.HasLogger
/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 * 
 * We OSGi-Export this class, and it is OSGi-imported by  ext.bundle.lift.
 * That allows some wiring to work, but we may need to go to a Fragment-bundle 
 * binding to get everything aligned properly.
 * 
 * We can change the name of the bound init class in web.xml, using init-param for the filter.
 * 
 */
class Booter extends Bootable with HasLogger {
  
  override def boot {
	 
	// This may happen before Log4J is initialized...because it is invoked whenever PAX-web decides to process
	// our web.xml.  See bottom of this file for a snapshot of how the stack looks.
	
	println("##################### Booter.boot is running println")
	myLogger.error("%%%%%%%%%%%%%%%%%%%%%%% Booter.boot - wobble knobble gobble goo")
	/*****  This code prints a stacktrace to stdout, can be used to verify where we are being invoked from.
	   See bottom of this source file for pasted sample output.
	 
	try {
		throw new Exception("Dummy");
	} catch {
		case e : Exception =>  {	
			e.printStackTrace()
		}
	}
	***********/
	// where to search for subpackages including:  snippet, 
	LiftRules.addToPackages("org.cogchar.lifter")

	// SiteMap; probably not really necessary at this point
	val pushyMenu = Menu("pushy") / "index" // This very different format from Lift in Action p.45
	val cogcharMenu = Menu(Loc("cogchar", ("cogchar" :: Nil) -> true, "Cogchar Interaction Internals")) // This is for the /cogchar directory and directories inside - format from Exploring Lift I think
	val sitemap = List(pushyMenu, cogcharMenu) // Just what we need for Pushy right now
	//LiftRules.setSiteMap(SiteMap(sitemap:_*)) // This is only commented out temporarily until Ticket 23 work is fully complete

	println("##################### Booter.boot    2222222222222222")
		
		
	LiftRules.early.append(makeUtf8)
	val myLiftAmbassador = PageCommander.getLiftAmbassador
	// Establish connection from LiftAmbassador into PageCommander
	myLiftAmbassador.setLiftMessenger(PageCommander.getMessenger)

	println("##################### Booter.boot    333333333333333333")
		
	// Is config already ready? If so, we missed it. Let's update now.
	if (myLiftAmbassador.checkConfigReady) {
	  PageCommander.initFromCogcharRDF(PageCommander.getInitialConfigId, myLiftAmbassador.getInitialConfig)
	}
	
	println("##################### Booter.boot    444444444444444444")
		
	// Add the listener for JSON speech to the dispatch table
	LiftRules.statelessDispatchTable.append(SpeechRestListener)
	
	// Have lift automatically discard session state when it is shut down
	// (Session initialization is done via "BrowserReadyIndicator" snippet instead of LiftSession.onSetupSession
	// so that Lifter knows the browser has read the default template (or another one already loaded in browser at
	// Lifter startup) and is ready to receive a page redirect to the desired template)
	LiftSession.onShutdownSession ::= ((ls:LiftSession) => PageCommander.getSessionOrg.removeSession(ls.uniqueId))
	
	println("##################### Booter.boot   9999999999999999 ")
    
	// Kind of a WAG as to how to use this, just trying it out. Actually seems 
    // to perhaps be making Comet behave better, but too early to say (and why 
    // would we expect it to?).
//    LiftRules.cometLogger = ActorLogger 
    
  }

  /**
   * Force the request to be UTF-8
   */
  private def makeUtf8(req: HTTPRequest) {
	req.setCharacterEncoding("UTF-8")
  }
}


/*  Snapshot of dummy stacktrace 2014-04-28, using Lift 2.5.1, 
 *  showing where we are in the callstack when a *default* boot method is invoked.
 *  If this class is hooked in via init-param, the stack will be a bit different,
 *  until the line :
 *			net.liftweb.http.LiftFilter.bootLift(LiftServlet.scala:928)
 *  
     [java] java.lang.Exception: Dummy
     [java] 	at bootstrap.liftweb.Boot.boot(Boot.scala:50)
     [java] 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     [java] 	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
     [java] 	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
     [java] 	at java.lang.reflect.Method.invoke(Method.java:597)
     [java] 	at net.liftweb.util.ClassHelpers$$anonfun$createInvoker$1.apply(ClassHelpers.scala:364)
     [java] 	at net.liftweb.util.ClassHelpers$$anonfun$createInvoker$1.apply(ClassHelpers.scala:362)
     [java] 	at net.liftweb.http.DefaultBootstrap$$anonfun$boot$1.apply(LiftRules.scala:2006)
     [java] 	at net.liftweb.http.DefaultBootstrap$$anonfun$boot$1.apply(LiftRules.scala:2006)
     [java] 	at net.liftweb.common.Full.map(Box.scala:553)
     [java] 	at net.liftweb.http.DefaultBootstrap$.boot(LiftRules.scala:2006)
     [java] 	at net.liftweb.http.provider.HTTPProvider$class.bootLift(HTTPProvider.scala:88)
     [java] 	at net.liftweb.http.LiftFilter.bootLift(LiftServlet.scala:928)
     [java] 	at net.liftweb.http.provider.servlet.ServletFilterProvider$class.init(ServletFilterProvider.scala:40)
     [java] 	at net.liftweb.http.LiftFilter.init(LiftServlet.scala:928)
     [java] 	at org.eclipse.jetty.servlet.FilterHolder.doStart(FilterHolder.java:102)
     [java] 	at org.eclipse.jetty.util.component.AbstractLifeCycle.start(AbstractLifeCycle.java:59)
     [java] 	at org.eclipse.jetty.servlet.ServletHandler.initialize(ServletHandler.java:748)
     [java] 	at org.eclipse.jetty.servlet.ServletHandler.updateMappings(ServletHandler.java:1221)
     [java] 	at org.eclipse.jetty.servlet.ServletHandler.setFilterMappings(ServletHandler.java:1257)
     [java] 	at org.eclipse.jetty.servlet.ServletHandler.addFilter(ServletHandler.java:1066)
     [java] 	at org.ops4j.pax.web.service.jetty.internal.JettyServerImpl$3.call(JettyServerImpl.java:313)
     [java] 	at org.ops4j.pax.web.service.jetty.internal.JettyServerImpl$3.call(JettyServerImpl.java:310)
     [java] 	at org.ops4j.pax.swissbox.core.ContextClassLoaderUtils.doWithClassLoader(ContextClassLoaderUtils.java:60)
     [java] 	at org.ops4j.pax.web.service.jetty.internal.JettyServerImpl.addFilter(JettyServerImpl.java:309)
     [java] 	at org.ops4j.pax.web.service.jetty.internal.ServerControllerImpl$Started.addFilter(ServerControllerImpl.java:291)
     [java] 	at org.ops4j.pax.web.service.jetty.internal.ServerControllerImpl.addFilter(ServerControllerImpl.java:142)
     [java] 	at org.ops4j.pax.web.service.internal.HttpServiceStarted.registerFilter(HttpServiceStarted.java:422)
     [java] 	at org.ops4j.pax.web.service.internal.HttpServiceProxy.registerFilter(HttpServiceProxy.java:155)
     [java] 	at org.ops4j.pax.web.extender.war.internal.RegisterWebAppVisitorWC.visit(RegisterWebAppVisitorWC.java:244)
     [java] 	at org.ops4j.pax.web.extender.war.internal.model.WebApp.accept(WebApp.java:602)
     [java] 	at org.ops4j.pax.web.extender.war.internal.WebAppPublisher$HttpServiceListener.register(WebAppPublisher.java:170)
     [java] 	at org.ops4j.pax.web.extender.war.internal.WebAppPublisher$HttpServiceListener.serviceChanged(WebAppPublisher.java:155)
     [java] 	at org.ops4j.pax.web.extender.war.internal.WebAppPublisher$HttpServiceListener.serviceChanged(WebAppPublisher.java:119)
     [java] 	at org.ops4j.pax.swissbox.tracker.ReplaceableService.setService(ReplaceableService.java:114)
     [java] 	at org.ops4j.pax.swissbox.tracker.ReplaceableService.access$100(ReplaceableService.java:28)
     [java] 	at org.ops4j.pax.swissbox.tracker.ReplaceableService$CollectionListener.serviceAdded(ReplaceableService.java:183)
     [java] 	at org.ops4j.pax.swissbox.tracker.ServiceCollection$Tracker.addingService(ServiceCollection.java:181)
     [java] 	at org.osgi.util.tracker.ServiceTracker$Tracked.customizerAdding(ServiceTracker.java:932)
     [java] 	at org.osgi.util.tracker.ServiceTracker$Tracked.customizerAdding(ServiceTracker.java:864)
     [java] 	at org.osgi.util.tracker.AbstractTracked.trackAdding(AbstractTracked.java:256)
     [java] 	at org.osgi.util.tracker.AbstractTracked.trackInitial(AbstractTracked.java:183)
     [java] 	at org.osgi.util.tracker.ServiceTracker.open(ServiceTracker.java:317)
     [java] 	at org.osgi.util.tracker.ServiceTracker.open(ServiceTracker.java:261)
     [java] 	at org.ops4j.pax.swissbox.tracker.ServiceCollection.onStart(ServiceCollection.java:139)
     [java] 	at org.ops4j.pax.swissbox.lifecycle.AbstractLifecycle$Stopped.start(AbstractLifecycle.java:121)
     [java] 	at org.ops4j.pax.swissbox.lifecycle.AbstractLifecycle.start(AbstractLifecycle.java:49)
     [java] 	at org.ops4j.pax.swissbox.tracker.ReplaceableService.onStart(ReplaceableService.java:146)
     [java] 	at org.ops4j.pax.swissbox.lifecycle.AbstractLifecycle$Stopped.start(AbstractLifecycle.java:121)
     [java] 	at org.ops4j.pax.swissbox.lifecycle.AbstractLifecycle.start(AbstractLifecycle.java:49)
     [java] 	at org.ops4j.pax.web.extender.war.internal.WebAppPublisher.publish(WebAppPublisher.java:81)
     [java] 	at org.ops4j.pax.web.extender.war.internal.WebXmlObserver.deploy(WebXmlObserver.java:200)
     [java] 	at org.ops4j.pax.web.extender.war.internal.WebXmlObserver.addingEntries(WebXmlObserver.java:159)
     [java] 	at org.ops4j.pax.swissbox.extender.BundleWatcher$3.run(BundleWatcher.java:224)
     [java] 	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:441)
     [java] 	at java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:303)
     [java] 	at java.util.concurrent.FutureTask.run(FutureTask.java:138)
     [java] 	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$301(ScheduledThreadPoolExecutor.java:98)
     [java] 	at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:206)
     [java] 	at java.util.concurrent.ThreadPoolExecutor$Worker.runTask(ThreadPoolExecutor.java:886)
     [java] 	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:908)
     [java] 	at java.lang.Thread.run(Thread.java:662)
 */