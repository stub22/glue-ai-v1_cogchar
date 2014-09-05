/*
 *  Copyright 2011 by The Cogchar Project (www.cogchar.org).
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

package com.thoughtworks.xstream.io.xml;

import com.thoughtworks.xstream.io.xml.AbstractDocumentWriter;

/**
 * @author Stu B. <www.texpedient.com>
 * This class lives in the thoughtworks package so it can get access
 * to a protected method we need:  AbstractDocumentWriter.getCurrent().
 * 
 * This hack was last tested with XStream 1.3.1.
 */
public class CogcharXStreamHelper {
	public static Object fetchCurrentWriterObject(AbstractDocumentWriter adw) {
		return adw.getCurrent();
	}
}
