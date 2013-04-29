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

package org.cogchar.api.thing;

import java.util.Iterator;
import org.appdapter.core.name.Ident;

/**  Subtypes are coded in Scala to easily handle conversions to the user's desired type,
 * which can go "towards RDF" (Jena "Resource", "RDFLiteral"...) or "towards primitive Java"
 * ("String", "Float", ...)
 * 
 * @author Stu B. <www.texpedient.com>
 */

public interface TypedValueMap {
	
	public Ident getAsIdent(Ident propName);
	public String getAsString(Ident propName);
	public Integer getAsInteger(Ident propName);
	public Long getAsLong(Ident propName);
	public Float getAsFloat(Ident propName);
	public Double getAsDouble(Ident propName);
	
	public static class TV_Exception extends RuntimeException {
		
	}
		public int getSize();
	public Iterator<Ident>	iterateKeys();
	
	public Object getRaw(Ident propName);
	public String getSparqlText(Ident propName);

}
