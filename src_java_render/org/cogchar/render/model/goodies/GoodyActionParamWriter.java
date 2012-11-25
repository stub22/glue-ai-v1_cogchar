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

import java.util.Iterator;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.cogchar.api.thing.BasicThingActionSpec;
import org.cogchar.api.thing.BasicTypedValueMap;
import org.cogchar.api.thing.ThingActionSpec;
import org.cogchar.api.thing.TypedValueMap;

/**
 * Typically used from a remote client to capture values for encoding in SPARQL-Update.
 *
 * @author Stu B. <www.texpedient.com>
 */
public class GoodyActionParamWriter {

	private BasicTypedValueMap myBTVMap;

	public GoodyActionParamWriter(BasicTypedValueMap btvMap) {
		myBTVMap = btvMap;
	}

	public void putLocation(float locX, float locY, float locZ) {
		myBTVMap.putValueAtName(GoodyNames.LOCATION_X, locX);
		myBTVMap.putValueAtName(GoodyNames.LOCATION_Y, locY);
		myBTVMap.putValueAtName(GoodyNames.LOCATION_Z, locZ);
	}

	public void putRotation(float rotAxisX, float rotAxisY, float rotAxisZ, float magDeg) {
		myBTVMap.putValueAtName(GoodyNames.ROTATION_AXIS_X, rotAxisX);
		myBTVMap.putValueAtName(GoodyNames.ROTATION_AXIS_Y, rotAxisY);
		myBTVMap.putValueAtName(GoodyNames.ROTATION_AXIS_Z, rotAxisZ);
		myBTVMap.putValueAtName(GoodyNames.ROTATION_MAG_DEG, magDeg);
	}

	public void putType(Ident someTypeID) {
		myBTVMap.putNameAtName(GoodyNames.RDF_TYPE, someTypeID);
	}

	/**
	 * Assume GRAPH is already taken care of by context.
	 *
	 * @param parentResToken - already rendered as either a URI or a variable-ref.
	 * @return
	 */
	public static String produceInsertDataTriples(TypedValueMap	paramTVM, String parentResToken) {
		StringBuffer resBuf = new StringBuffer();
		int paramCount = paramTVM.getSize();
		if (paramCount > 0) {
			resBuf.append("INSERT {\n");
			resBuf.append(parentResToken);
			Iterator<Ident> nameIter = paramTVM.iterateKeys();
			String preSep = "  ";
			while (nameIter.hasNext()) {
				resBuf.append(preSep);
				Ident paramID = nameIter.next();
				// TODO - shorten using prefix
				resBuf.append("<" + paramID.getAbsUriString() + ">  ");
				String sparqlText = paramTVM.getSparqlText(paramID);
				resBuf.append(sparqlText);
				preSep = ";  \n";
			}
			resBuf.append(". \n }\n");
		}
		return resBuf.toString();
	}
}
