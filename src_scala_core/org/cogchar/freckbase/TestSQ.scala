/*
 * TestSQ.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.cogchar.freckbase

import org.scalaquery.session._
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._

// "Basic" cannot use AutoInc, so we use H2.
//import org.scalaquery.ql.basic.{BasicTable => Table}
//
import org.scalaquery.ql.extended.{ExtendedProfile, H2Driver}
import org.scalaquery.ql.extended.{ExtendedTable => ExTable}
import org.scalaquery.ql.extended.H2Driver.Implicit._

object TestSQ {

	def main(args: Array[String]): Unit = {
		// Bring the implicit session into scope
		import org.scalaquery.session.Database.threadLocalSession
		// import org.scalaquery.session.SessionFactory._
		println("TestSQ starting");
		val fbs = FreckbaseSession.serverSession();
		val mgr = new Manager(fbs);
		mgr.initTables();
		// withSession sets up the implicit session in Session factory
		mgr.sqSessionFactory.withSession {
			val photoID = Photos.test();
			val hypoID = Hypotheses.test(None);
			val obsID = Observations.test(mgr, photoID, hypoID);
			val profileID = Profiles.test();

			val entryID = Entries.test(profileID, obsID);
			val friendID = Friends.test(obsID, profileID);
			val attemptID = Attempts.test(obsID, profileID);


			Friends.printAll();
			Observations.printAll();
			Entries.printAll();
			Attempts.printAll();
			Hypotheses.printAll();
			Photos.printAll();
			Profiles.printAll();

			if (true) {
				println("Starting TCP Server");
				fbs.startTCPServer(FreckbaseSession.theTcpPort);
				println("Napping for 20 minutes");
				Thread.sleep(20 * 60 * 1000);
			}
			println("Naptime is over, cleaning up before exit.");
			fbs.cleanup();
		}
		
		println("TestSQ ending");
	}

	def testExplicitSession(mgr : Manager) {
		val dummyImage = new Array[Byte](186000);
		val explicitSession = org.scalaquery.session.Database.threadLocalSession; // SessionFactory.getThreadSession;
		val fullObsID = mgr.recordObs(explicitSession, 777, 8484, "SPIFFY",	1024, 768, dummyImage);
		println("Recorded full obs with ID: " + fullObsID);
		val rfo : PTypes.Obs = Observations.readOneOrThrow(fullObsID)(explicitSession);
		println("Reconstituted full obs: " + rfo);
		QueryUtils.updateValue(Observations.tableName, Observations.c_recogStatus.name, fullObsID, "FIXED")(explicitSession);
		fullObsID;
	}
}
