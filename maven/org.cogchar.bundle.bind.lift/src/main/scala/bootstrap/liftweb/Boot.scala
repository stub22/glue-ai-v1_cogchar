package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.common._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
//import _root_.net.liftweb.mapper._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import _root_.net.liftweb.actor.ActorLogger
import Helpers._
//import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, DefaultConnectionIdentifier, StandardDBVendor}
import _root_.java.sql.{Connection, DriverManager}
import _root_.org.cogchar.lifter.lib._
import _root_.org.cogchar.lifter.model._
import _root_.org.cogchar.bind.lift.LiftAmbassador

import org.jflux.impl.services.rk.lifecycle.ManagedService;
import org.jflux.impl.services.rk.lifecycle.ServiceLifecycleProvider;
import org.jflux.impl.services.rk.lifecycle.utils.SimpleLifecycle;
import org.jflux.impl.services.rk.osgi.OSGiUtils;
import org.jflux.impl.services.rk.osgi.lifecycle.OSGiComponent;
import org.cogchar.api.thing.WantsThingAction
//import org.cogchar.bundle.app.puma.PumaAppUtils

import java.util.Properties;

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  
  def boot {
	
	// where to search snippet
	LiftRules.addToPackages("org.cogchar.lifter")

	// SiteMap; probably not really necessary at this point
	val pushyMenu = Menu("pushy") / "index" // This very different format from Lift in Action p.45
	val cogcharMenu = Menu(Loc("cogchar", ("cogchar" :: Nil) -> true, "Cogchar Interaction Internals")) // This is for the /cogchar directory and directories inside - format from Exploring Lift I think
	val sitemap = List(pushyMenu, cogcharMenu) // Just what we need for Pushy right now
	//LiftRules.setSiteMap(SiteMap(sitemap:_*)) // This is only commented out temporarily until Ticket 23 work is fully complete

	LiftRules.early.append(makeUtf8)
	val myLiftAmbassador = PageCommander.getLiftAmbassador
	// Establish connection from LiftAmbassador into PageCommander
	myLiftAmbassador.setLiftMessenger(PageCommander.getMessenger)
	
	// Is config already ready? If so, we missed it. Let's update now.
	if (myLiftAmbassador.checkConfigReady) {
	  PageCommander.initFromCogcharRDF(PageCommander.getInitialConfigId, myLiftAmbassador.getInitialConfig)
	}
	
	// Add the listener for JSON speech to the dispatch table
	LiftRules.statelessDispatchTable.append(SpeechRestListener)
	
	// Have lift automatically discard session state when it is shut down
	// (Session initialization is done via "BrowserReadyIndicator" snippet instead of LiftSession.onSetupSession
	// so that Lifter knows the browser has read the default template (or another one already loaded in browser at
	// Lifter startup) and is ready to receive a page redirect to the desired template)
	LiftSession.onShutdownSession ::= ((ls:LiftSession) => PageCommander.removeSession(ls.uniqueId))
    
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
