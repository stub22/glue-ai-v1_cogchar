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

package org.cogchar.convoid.output.config;

import java.io.OutputStream;
import java.io.Writer;
import java.io.StringWriter;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.Dom4JWriter;

/**
 * @author Stu B. <www.texpedient.com>
 *
 */
public class BehaviorDataSaver {
	public static Document writeToDom4JDoc(Object obj) {
		// dom4JDriver produces a Dom4JXmlWriter, which cannot write mixedContent.
		// dom4jDriver.setOutputFormat(dom4jOutputFormat);
		XStream xstream = new XStream();
		Document targetDoc = DocumentHelper.createDocument();
		//BehaviorDataLoader.initBehaviorXStream(xstream);
		Dom4JWriter d4jWriter = new Dom4JWriter(targetDoc);
		xstream.marshal(obj, d4jWriter);
		return targetDoc;
	}
	public static OutputFormat getPrettyDom4jOutputFormat() {
		String indent = "\t";
		OutputFormat dom4jOutputFormat = new OutputFormat(indent, true);
		return dom4jOutputFormat;
	}
	public static void writeDocument(Document doc, OutputStream os) throws Throwable  {
		XMLWriter writer = new XMLWriter(os, getPrettyDom4jOutputFormat());
		writer.write(doc);
	}
	public static void writeDocument(Document doc, Writer w) throws Throwable  {
		XMLWriter writer = new XMLWriter(w, getPrettyDom4jOutputFormat());
		writer.write(doc);
	}
	public static String writeDocumentToString(Document doc) throws Throwable  {
		StringWriter sw = new StringWriter();
		writeDocument(doc, sw);
		return sw.toString();
	}
}
