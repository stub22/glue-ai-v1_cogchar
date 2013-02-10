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

package org.cogchar.render.model.goodies;

import org.appdapter.core.component.ComponentAssemblyNames;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;

/**
 * @author Stu B. <www.texpedient.com>
 */

public class GoodyNames  {
	
	public  static String	GOODY_NS = "urn:ftd:cogchar.org:2012:goody#";
	
	public static Ident makeID (String nameTail) {
		return new FreeIdent(GOODY_NS + nameTail);
	}
	
	// Not sure if this is the best approach long term: a special Goody URI which corresponds to all goodies
	// Intended for server-side goody deletion; not yet implemented
	// Currently goodies use the ccrt prefix. If that changes, this should change too: 
	public	static Ident	ALL_GOODY_URI = new FreeIdent("urn:ftd:cogchar.org:2012:runtime#ALL");
	
	public	static Ident 	LOCATION_X = makeID("locX");
	public	static Ident	LOCATION_Y = makeID("locY");
	public	static Ident	LOCATION_Z = makeID("locZ");
	
	public	static Ident	ROTATION_AXIS_X = makeID("rotAxisX");
	public	static Ident	ROTATION_AXIS_Y = makeID("rotAxisY");
	public	static Ident	ROTATION_AXIS_Z = makeID("rotAxisZ");
	public	static Ident	ROTATION_MAG_DEG = makeID("rotMagDeg");
	
	public	static Ident	SIZE_X = makeID("sizeX");
	public	static Ident	SIZE_Y = makeID("sizeY");
	public	static Ident	SIZE_Z = makeID("sizeZ");
	
	public	static Ident	COORDINATE_X = makeID("coordinateX");
	public	static Ident	COORDINATE_Y = makeID("coordinateY");
	
	public	static Ident	TRAVEL_TIME = makeID("travelTime");
	//public	static Ident	TEXT_SIZE = makeID("textScale");
	public	static Ident	SCALE = makeID("scale");
	
	public	static Ident	SCALE_X = makeID("scaleX");
	public	static Ident	SCALE_Y = makeID("scaleY");
	public	static Ident	SCALE_Z = makeID("scaleZ");
	
	public	static Ident	COLOR_RED = makeID("colorR");
	public	static Ident	COLOR_GREEN = makeID("colorG");
	public	static Ident	COLOR_BLUE = makeID("colorB");
	public	static Ident	COLOR_ALPHA = makeID("colorAlpha");
			
	public static	Ident	TYPE_BIT_BOX = makeID("BitBox");
	public static	Ident	TYPE_BIT_CUBE = makeID("BitCube");
	public static	Ident	TYPE_FLOOR = makeID("Floor");
	public static	Ident	TYPE_TICTAC_MARK = makeID("TicTacMark");
	public static	Ident	TYPE_TICTAC_GRID = makeID("TicTacGrid");
	
	public static	Ident	TYPE_CROSSHAIR = makeID("CrossHair");
	public static	Ident	TYPE_SCOREBOARD = makeID("ScoreBoard");
	public static	Ident	TYPE_TEXT = makeID("Text2D");
	
	public  static Ident	RDF_TYPE = new FreeIdent(ComponentAssemblyNames.NS_rdf + "type");
	
	public static	Ident	ACTION_CREATE = makeID("ActionCreate");
	public static	Ident	ACTION_DELETE = makeID("ActionDelete");
	public static	Ident	ACTION_MOVE = makeID("ActionMove");
	public static	Ident	ACTION_SET = makeID("ActionSet");
	
	// This stuff gets pretty particular to the individual Goody types. Perhaps shouldn't live here but somehow in
	// BasicGoodyImpl subclasses?
	public static	Ident	BOOLEAN_STATE = makeID("booleanState");
	public static	Ident	USE_O = makeID("isPlayerO");
	public static	Ident	ROWS = makeID("rows");
	public static	Ident	TEXT = makeID("text");
	public static	Ident	SUBCOMPONENT = makeID("subComponent");

}
