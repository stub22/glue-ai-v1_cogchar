/*
 * RecordTypes.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.cogchar.freckbase

import java.sql.{Blob};

object Tuples {
	type Entry		= (Long, Long, Long, Long, Long);
	type Photo		= (Long, Long, Long, Int, Int, Blob);
	type Friend		= (Long, Long, Long, Long, Long, Option[String]);
	type Obs		= (Long, Long, Long, Long, Long, Long, String, Option[String], Option[Long]);
	type Profile	= (Long, Long, Long, String);
	type Attempt	= (Long, Long, Long, Long, Long, Double);
	type Hypo		= (Long, Long, Long, Option[Long]);
}
object PTypes {
	type Entry			= org.cogchar.freckbase.ProfileEntry with Persistent;
	type Photo			= org.cogchar.freckbase.Photo with Persistent;
	type Friend			= org.cogchar.freckbase.Friend with Persistent;
	type Obs			= org.cogchar.freckbase.Observation with Persistent;
	type Profile		= org.cogchar.freckbase.Profile with Persistent;
	type Attempt		= org.cogchar.freckbase.Attempt with Persistent;
	type Hypo			= org.cogchar.freckbase.Hypothesis with Persistent;
}
