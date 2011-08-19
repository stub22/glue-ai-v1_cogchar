/*
 * QueryUtils.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.cogchar.freckbase

import java.util.logging.Logger;

import java.sql.{Connection, DriverManager, Statement, PreparedStatement, ResultSet, Blob};

import org.scalaquery.session._
import org.scalaquery.simple._
import org.scalaquery.simple.StaticQuery._
// import org.scalaquery.simple.Implicit._

object QueryUtils {
	def getLastInsertID(conn : Connection) : Long = {
		/* H2 can prepareCall and execute, but cannot (yet) register output types
		 * or fetch output values

		 val cst = conn.prepareCall("CALL IDENTITY()");
		 cst.registerOutParameter(1, java.sql.Types.BIGINT);
		 val exr : Boolean = cst.execute();
		 val res : Long = cst.getLong(1);
		 cst.close();
		 */
		var lastInsertID : Long = -1;
		val st = conn.createStatement();
		val sxr = st.execute("SELECT IDENTITY()");
		val rs = st.getResultSet();
		if (rs.next()) {
			lastInsertID = rs.getLong(1);
		}
		rs.close();
		st.close();
		lastInsertID;
	}
	def lastInsertedID()(implicit isp: Session) : Long = {
		val q = queryNA[Long]("SELECT IDENTITY()");
		val resultList : List[Long] = q.list;
		println("resultList: " + resultList);
		resultList.head;
	}
	/*	Of course single field updates are not performance-optimal, but they make
		life a bit easier for now.
	*/
	def updateValue[VT](table : String, column : String, oid : Long, v : VT)(implicit isp: Session) = {
		val upText = "UPDATE " + table + " SET " + column + "=? WHERE object_id=?";
		val upper = update[(VT,Long)](upText);
		val rowcount = upper((v,oid));
		println("Update rowcount was " + rowcount + " for: " + upText + " with oid=" + oid + " and val=" + v);
	}
	def deleteRow(table : String, oid : Long)(implicit isp: Session) = {
		val delText = "DELETE FROM " + table + " WHERE object_id=?";
		val deleter = update[(Long)](delText);
		val rowcount = deleter((oid));
		println("Deleted rowcount was " + rowcount + " for: " + delText + " with oid=" + oid );
	}
}
