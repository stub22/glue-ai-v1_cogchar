/*
 *  Copyright 2012 by The Cogchar Project (www.cogchar.org).
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

package org.cogchar.impl.thing.fancy

import org.appdapter.core.name.Ident
import org.cogchar.api.thing.ThingActionSpec
import org.cogchar.impl.thing.basic.BasicTypedValueMapWithConversion
/**
 * @author Stu B. <www.texpedient.com>
 */

object FancyThingTest {
  def main(args: Array[String]): Unit = {
    // Must enable "compile" scope for Log4J dep in order to compile this code.
    org.apache.log4j.BasicConfigurator.configure();
    org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);

    println("Ooo, it's fancy in here!")
  }

  def encodeThingActionForGraph(graphID: Ident, actionSpec: ThingActionSpec): String = {
    "";
  }
  /*
	 *	static String glueUpSilly = PREFIX_CCRT + PREFIX_UA + PREFIX_DC
			+ "INSERT DATA {  GRAPH   ccrt:user_access_sheet_22 { \n"
			+ "<http://yeah/nutty> dc:title 'How to be revolting' ;    dc:creator 'Spart A. Cus'. }}\n";
	 *
	 */

}
class ConcreteTVM() extends BasicTypedValueMapWithConversion {
  /*
	override def  getAsIdent( propName : Ident) : Ident = {
		null;
	}
	override def   getAsString(propName : Ident) : String = {
		"REGULAR_STRING_FOR_" + getRaw(propName);
	}
	override def   getAsInteger(propName : Ident) : java.lang.Integer = {
		0
	}
	override def   getAsLong(propName : Ident) : java.lang.Long = {
		0;
	}
	override def   getAsFloat(propName : Ident) : java.lang.Float = {
		0.0f;[HEZ]
	}
	override def   getAsDouble(propName : Ident) :  java.lang.Double = {
		0.0d;
	}
	override def  getSparqlText(propName : Ident) : String = {
		"SPARQL_TEXT_FOR_" + getRaw(propName);
	}
	*/
}
class SparqlRenderContext() {
  def whoopee() = {

  }
}
