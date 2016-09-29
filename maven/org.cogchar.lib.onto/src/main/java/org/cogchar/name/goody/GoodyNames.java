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
package org.cogchar.name.goody;

import org.appdapter.core.component.ComponentAssemblyNames;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.name.dir.NamespaceDir;

/**
 * Defines all the type+property names used to describe our GoodyActions. This class is ripe for
 * mapping into a proper ontology of goodies and v-world entities.
 *
 * @author Stu B. <www.texpedient.com>
 */
public class GoodyNames {

    public static final String GOODY_NS = NamespaceDir.GOODY_NS;

    public static final Ident makeID(String nameTail) {
        return new FreeIdent(GOODY_NS + nameTail);
    }

    // Loc + Rot are for truly 3D goodies/entities only
    public static final Ident LOCATION_X = makeID("locX");
    public static final Ident LOCATION_Y = makeID("locY");
    public static final Ident LOCATION_Z = makeID("locZ");

    public static final Ident ROTATION_AXIS_X = makeID("rotAxisX");
    public static final Ident ROTATION_AXIS_Y = makeID("rotAxisY");
    public static final Ident ROTATION_AXIS_Z = makeID("rotAxisZ");
    public static final Ident ROTATION_MAG_DEG = makeID("rotMagDeg");

    // "Size" (not "Scale") is still ambiguous re 2D vs. 3D
    public static final Ident SIZE_X = makeID("sizeX");
    public static final Ident SIZE_Y = makeID("sizeY");
    public static final Ident SIZE_Z = makeID("sizeZ");

    public static final Ident SIZE_SCALAR = makeID("sizeScalar");

    // Scale is usable in both 2D + 3D contexts.
    public static final Ident SCALE_X = makeID("scaleX");
    public static final Ident SCALE_Y = makeID("scaleY");
    public static final Ident SCALE_Z = makeID("scaleZ");

    // For 2D goodies only = screen fraction position
    public static final Ident LOC_FRAC_X = makeID("locFracX");
    public static final Ident LOC_FRAC_Y = makeID("locFracY");

    // 2D application subspace used within a goody, e.g. tic-tac-toe board
    // (TODO: Verify this description)
    public static final Ident COORDINATE_X = makeID("coordinateX");
    public static final Ident COORDINATE_Y = makeID("coordinateY");

    // Used to specify duration of the
    public static final Ident TRAVEL_TIME = makeID("travelTime");

    //public	static Ident	TEXT_SIZE = makeID("textScale");
    public static final Ident SCALE_UNIFORM = makeID("scaleUni");

    public static final Ident COLOR_RED = makeID("colorR");
    public static final Ident COLOR_GREEN = makeID("colorG");
    public static final Ident COLOR_BLUE = makeID("colorB");
    public static final Ident COLOR_ALPHA = makeID("colorAlpha");

    // Here are the allowed Types/Kinds of goody (and other entities)
    // Proper 3D goody types:
    public static final Ident TYPE_BOX = makeID("GoodyBox");
    public static final Ident TYPE_BIT_BOX = makeID("BitBox");
    public static final Ident TYPE_BIT_CUBE = makeID("BitCube");
    public static final Ident TYPE_FLOOR = makeID("Floor");
    public static final Ident TYPE_TICTAC_MARK = makeID("TicTacMark");
    public static final Ident TYPE_TICTAC_GRID = makeID("TicTacGrid");

    // 2D overlay goodies with weird behaviors - this approach is problematic.
    public static final Ident TYPE_CROSSHAIR = makeID("CrossHair");
    public static final Ident TYPE_SCOREBOARD = makeID("ScoreBoard");
    public static final Ident TYPE_TEXT = makeID("Text2D");

    // Not really goodies, but we're pretending for now until handling of VirtualEntities is more generalized
    public static final Ident TYPE_AVATAR = makeID("Avatar");
    public static final Ident TYPE_CAMERA = makeID("Camera");

    // technical construct used in RDF conveyance of goody actions
    public static final Ident RDF_TYPE = new FreeIdent(ComponentAssemblyNames.NS_rdf + "type");

    // The actions it is possible to take on a goody/entity
    public static final Ident ACTION_CREATE = makeID("ActionCreate");
    public static final Ident ACTION_DELETE = makeID("ActionDelete");
    public static final Ident ACTION_MOVE = makeID("ActionMove");
    public static final Ident ACTION_SET = makeID("ActionSet");

    // Goody-guts description specifics, which apply meaningfully only to particular goody-kinds.
    public static final Ident BOOLEAN_STATE = makeID("booleanState");
    public static final Ident USE_O = makeID("isPlayerO");
    public static final Ident ROWS = makeID("rows");
    public static final Ident TEXT = makeID("text");
    public static final Ident SUBCOMPONENT = makeID("subComponent");
    public static final Ident ATTACH_TO_GOODY = makeID("attachToGoody");

    // Not sure if this is the best approach long term: a special Goody URI which corresponds to all goodies
    // Intended for server-side goody deletion; not yet implemented
    // Currently goodies use the ccrt prefix. If that changes, this should change too:
    public static final Ident ALL_GOODY_URI = new FreeIdent(NamespaceDir.RKRT_NS_PREFIX + "ALL");
}
