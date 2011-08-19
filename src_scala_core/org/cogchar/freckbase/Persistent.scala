/*
 * Persistent.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.cogchar.freckbase

import java.sql.{Connection, DriverManager, Statement, PreparedStatement, ResultSet, Blob};


import org.scalaquery.session._
import org.scalaquery.ql._
import org.scalaquery.ql.TypeMapper._

// "Basic" cannot use AutoInc, so we use H2.
//import org.scalaquery.ql.basic.{BasicTable => Table}
//
import org.scalaquery.ql.extended.{ExtendedProfile, H2Driver}
import org.scalaquery.ql.extended.{ExtendedTable => ExTable}
import org.scalaquery.ql.extended.H2Driver.Implicit._




import java.util.logging.Logger;


trait Persistent extends Record {
/*
	def tableName : String;
	def identColName : String;
	def createStampColName : String = "create_stamp";
	def updateStampColName : String = "update_stamp";
*/
	

	def readTuple(data : Tuple3[Long, Long, Long]) {
		myObjectIdent = Some(data._1);
		myCreateStamp = Some(data._2);
		myUpdateStamp = Some(data._3);
	}
	def readProduct(data : Product) {
		require (data.productArity >= 3);

		val t : Tuple3[Long, Long, Long] = data match {
			case (x1 : Long, x2 : Long, x3: Long) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _, _, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _, _, _, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _, _, _, _, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _, _, _, _, _, _) => (x1, x2, x3)
			case (x1 : Long, x2 : Long, x3: Long, _, _, _, _, _, _, _, _) => (x1, x2, x3)
			case _ => throw new RuntimeException("Bad persistent data tuple: " + data);
		}
//			: (Long, Long, Long) = ((Long) data.productElement(0), (Long) data.productElement(1), (Long) data.productElement(2));
//		}
		readTuple(t);
	}

	override def toString() : String = {
		"Persistent[objID=" + myObjectIdent + "[" + super.toString() + "]]";
	}

}

