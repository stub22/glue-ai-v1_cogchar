/*
 *  Copyright 2013 by The Cogchar Project (www.cogchar.org).
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.cogchar.bundle.app.puma;

import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.impl.thing.basic.BasicThingActionRouter;
import static org.cogchar.name.entity.EntityRoleCN.RKRT_NS_PREFIX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class GruesomeTAProcessingFuncs {

	static Logger theLogger = LoggerFactory.getLogger(GruesomeTAProcessingFuncs.class);
	private static BasicThingActionRouter	theRouter;

	public static BasicThingActionRouter	getActionRouter() {
		if (theRouter == null) {
            String localName = "theRouter";
            Ident agentID = new FreeIdent(RKRT_NS_PREFIX + localName, localName);
            theRouter = new BasicThingActionRouter(0L, agentID);
		}
		return theRouter;
	}

	@Deprecated
	public static void registerActionConsumers() {
		PumaAppUtils.GreedyHandleSet srec = PumaAppUtils.obtainGreedyHandleSet();
		// The VWorld does its own registration in a separate ballet.
		// Here we are just handling the reg for Web + Behavior.

		BasicThingActionRouter router = getActionRouter();
		srec.pumaWebMapper.registerActionConsumers(router, srec.rc, srec.gce);
	}
	/* This is called in two separate cases:
	 *		1) In the repoUpdateCompleted() callback of a top-level application, e.g.
	 *			a) o.f.b.demo.liftoff.Activator
	 *			b) c.h.b.oglweb.R50.Activator
	 *		2) On a call to  PumaAppContext.resetMainConfigAndCheckThingActions
	 *			which is queued indirectly from   PumaContextCommandBox.processUpdateRequestAsync
	 *			which is a crude, old form of GUI wiring to be replaced.
	 *	
	 */

	@Deprecated
	public static void processPendingThingActions() {
		PumaAppUtils.GreedyHandleSet srec = PumaAppUtils.obtainGreedyHandleSet();
		BasicThingActionRouter router = getActionRouter();
		if ((srec !=null) && (router != null)) {
		// Only known call to this 1-arg method form.
		router.consumeAllActions(srec.rc);
		} else {
			theLogger.warn("Cannot process with handleSet={} and taRouter={}", srec, router);
	}
	}

	private static class RouterRunnable implements java.lang.Runnable {

		private long mySleepMsec = 300;
		private int myErrorCount = 0;
		private int myRunCount = 0;

		@Override public void run() {
			while (true) {
				try {
					Thread.sleep(mySleepMsec);
					myRunCount++;
					if ((myRunCount % 10) == 1) {
						theLogger.info("Executing process call #" + myRunCount);
					}
					processPendingThingActions();
				} catch (Throwable t) {
					myErrorCount++;
					if (myErrorCount <= 25) {
						theLogger.error("RouterRunnable caught error #" + myErrorCount + "during run#" + myRunCount + "will continue up to 25 errors", t);
					} else {
						theLogger.error("RouterRunnable caught error #" + myErrorCount + ", EXCEEDS ALLOWED COUNT, aborting run()", t);
						throw new RuntimeException("RouterRunnable exceeded allowed error count at " + myErrorCount);
					}
				}
			}
		}
	}
	private static Thread theRouterRunnableThread = null;
	/* Generally we do not need both this AND the Joseki-sparql-callback hack running.
	 * 
	 */

	public static void startRouterDedicatedThread() {
		if (theRouterRunnableThread != null) {
			throw new RuntimeException("Attempt to restart RouterRunnable Thread - not yet implemented!");
		} else {
			RouterRunnable rr = new RouterRunnable();
			Thread t = new Thread(rr);
			t.start();
			theRouterRunnableThread = t;
		}
	}
}
