/*
 * Hypothesis.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.cogchar.freckbase

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


case class Hypothesis (var myFriendID : Option[Long]) {
}

object Hypotheses extends RecordTable[Tuples.Hypo, Hypothesis]("Hypothesis") {
	val c_friendID	=		colOptLong("friend_id");

	val reqCols  = stampStar
	override val * = coreStar ~ c_friendID

	def bindPersistentHypo(ft : Tuples.Hypo) : PTypes.Hypo = {
		val f = new Hypothesis(ft._4) with Persistent;
		f.readProduct(ft);
		f;
	}
	override def bindTuple(tup : Tuples.Hypo) : PTypes.Hypo = bindPersistentHypo(tup);
	def insert() (implicit isp: Session)  : Long = {
		val rowcount = reqCols.insert(-1L, -1L);
		println("Inserted hypo count: " + rowcount);
		QueryUtils.lastInsertedID();
	}
	def updateFields(hypo : PTypes.Hypo, friendID : Long) (implicit isp: Session) {
		val hypoID : Long = hypo.myObjectIdent.get;
		QueryUtils.updateValue(tableName, c_friendID.name, hypoID, friendID);
		hypo.myFriendID = Some(friendID);
	}
	def test(friendID : Option[Long])(implicit isp: Session) : Long = {
		val hypoID = Hypotheses.insert();
		println("Inserted Hypo with ID: " + hypoID);
		val rh : PTypes.Hypo = Hypotheses.readOneOrThrow(hypoID);
		println("Reconstituted Hypo: " + rh);
		if (friendID.isDefined) {
			Hypotheses.updateFields(rh, friendID.get);
			println("Updated Hypo: " + rh);
			val urh : PTypes.Hypo = Hypotheses.readOneOrThrow(hypoID);
			println("Reconstituted updated hypo: " + urh);
		}
		hypoID;
	}
}
