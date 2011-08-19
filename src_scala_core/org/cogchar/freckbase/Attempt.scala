/*
 * Attempt.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.cogchar.freckbase

import java.sql.{Connection, DriverManager, Statement, PreparedStatement, ResultSet, Blob};
import org.cogchar.freckler.protocol.{FreckleQuery, FreckleResult, FreckleSampleQuality};
import org.cogchar.vision.PortableImage;

import java.util.logging.Logger;

import org.scalaquery.session._
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._

// "Basic" cannot use AutoInc, so we use H2.
//import org.scalaquery.ql.basic.{BasicTable => Table}
//
import org.scalaquery.ql.extended.{ExtendedProfile, H2Driver}
import org.scalaquery.ql.extended.{ExtendedTable => ExTable}
import org.scalaquery.ql.extended.H2Driver.Implicit._

// TODO : Maybe add some status info about the results of this attempt.
// E.G - Was profile expanded?  If not, why not?
case class Attempt (val myObsID :	Long,
				val myProfileID : Long,
				val myScore : Double) extends Record {

}

object Attempts extends RecordTable[Tuples.Attempt, Attempt]("Attempt") { 
	val c_obsID		=		colReqLong("obs_id");
	val c_profileID	=		colReqLong("profile_id");
	val c_score		=		colReqDouble("score");

	val reqCols  = stampStar ~ c_obsID ~ c_profileID ~ c_score
	override val * = coreStar ~ c_obsID ~ c_profileID ~ c_score

	def bindPersistentAttempt(ft : Tuples.Attempt) : PTypes.Attempt = {
		val f = new Attempt(ft._4, ft._5, ft._6) with Persistent;
		f.readProduct(ft);
		f;
	}
	override def bindTuple(tup : Tuples.Attempt) : PTypes.Attempt = bindPersistentAttempt(tup);
	
	def insert( obsID : Long, profileID : Long, score : Double)(implicit isp: Session)  : Long = {
		val rowcount = reqCols.insert(-1L, -1L, obsID, profileID, score);
		println("Inserted attempt count: " + rowcount);
		QueryUtils.lastInsertedID();
	}
	def test(obsID : Long, profileID : Long)(implicit isp: Session) : Long = {
		val attemptID = Attempts.insert(obsID, profileID, -77.777);
		println("Inserted Attempt with ID: " + attemptID);
		val ra : PTypes.Attempt = Attempts.readOneOrThrow(attemptID);
		println("Reconstituted Attempt: " + ra);
		attemptID;
	}
}
