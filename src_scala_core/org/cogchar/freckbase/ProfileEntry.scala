/*
 * ProfileEntry.scala
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

case class ProfileEntry(val myProfileID : Long, val myObsID : Long) extends Record {
	def getObs()(implicit isp: Session) : PTypes.Obs = {
		Observations.readOneOrThrow(myObsID);
	}
}

object Entries extends RecordTable[Tuples.Entry, ProfileEntry]("Profile_Entry") {

	val c_profileID =	colReqLong("profile_id");
	val c_obsID =		colReqLong("obs_id");
	val reqCols = stampStar ~ c_profileID ~ c_obsID
	override val * = coreStar ~ c_profileID ~ c_obsID

	def bindPersistentEntry(tup : Tuples.Entry) : PTypes.Entry = {
		val pe = new ProfileEntry(tup._4, tup._5) with Persistent;
		pe.readProduct(tup);
		pe;
	}
	override def bindTuple(tup : Tuples.Entry) :  PTypes.Entry = bindPersistentEntry(tup);

	def insert(profileID : Long, obsID : Long)(implicit isp: Session) : Long = {
		val rowcount = reqCols.insert(-1L, -1L, profileID, obsID);
		println("Inserted entry count: " + rowcount);
		QueryUtils.lastInsertedID();
	}
	def listForProfile(profileID : Long)(implicit isp: Session) : List[PTypes.Entry] = {
		var entryList : List[PTypes.Entry] = Nil;
		val q = for(r <- this where {_.c_profileID is profileID}) yield r.*
		println("q: " + q.selectStatement)
		for(tup <- q) {
			val entry :  PTypes.Entry = bindTuple(tup);
			entryList = entry :: entryList;
		}
		entryList;
	}
	def test(profileID : Long, obsID : Long)(implicit isp: Session) : Long = {
		val eid = Entries.insert(profileID, obsID);
		println("Inserted entry with ID: " + eid);
		val entry : PTypes.Entry = Entries.readOneOrThrow(eid);
		println("Reconstituted entry: " + entry);
		eid;
	}
}