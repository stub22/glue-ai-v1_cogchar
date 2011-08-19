/*
 * Record.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.cogchar.freckbase

import java.util.logging.Logger;

trait Record {
	lazy val myLogger : Logger = Logger.getLogger(getClass.getName);
	
	var myObjectIdent : Option[Long] = None;
	var myCreateStamp : Option[Long] = None;
	var myUpdateStamp : Option[Long] = None;
}
