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

import com.thoughtworks.xstream.converters.Converter;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import com.thoughtworks.xstream.io.copy.HierarchicalStreamCopier;

import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppReader;

import com.thoughtworks.xstream.io.xml.Dom4JReader;
import com.thoughtworks.xstream.io.xml.Dom4JWriter;

import com.thoughtworks.xstream.io.xml.CogcharXStreamHelper;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.DocumentHelper;

import java.io.StringReader;
import java.io.StringWriter;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Stu B. <www.texpedient.com>
 */
public class Step {
	public static final String ST_SAPI5_LITERAL = "SAPI5_LITERAL";
	private		String		myText;
	// bare name because we want xstream to bind attribute
	private		String		type;
	
	public Step() { }	
	
	public void setText(String t) {
		myText = t;
        if(!myText.startsWith("<sapi>")){
            myText = "<sapi>" + myText;
        }
        if(!myText.endsWith("</sapi>")){
            myText = myText + "</sapi>";
        }
        myText = myText.replaceAll("<sapi>\\s*<sapi>", "<sapi>");
        myText = myText.replaceAll("</sapi>\\s*</sapi>", "</sapi>");
	}
	public String getText() {
		return myText;
	}
	public void setType(String t) {
		type = t;
	}
	public String getType() {
		return type;
	}
	@Override
	public String toString() {
		return   "Step{type=" + type + 
						"\n--------------------------------------------\n" 
		+ getText() + 	"\n--------------------------------------------\n}";
	}
    public String toXML(){
        myText = myText.trim();
        if(myText.isEmpty() || myText.matches("\\s*<sapi>\\s*</sapi>\\s*")){
            return "";
        }
        if(!myText.startsWith("<sapi>")){
            myText = "<sapi>" + myText;
        }
        if(!myText.endsWith("</sapi>")){
            myText = myText + "</sapi>";
        }
        myText = myText.replaceAll("<sapi>\\s*<sapi>", "<sapi>");
        myText = myText.replaceAll("</sapi>\\s*</sapi>", "</sapi>");
        return "<Step type=\"" + type + "\">" + myText + "</Step>";
    }
	public static class XStreamConverter implements Converter  {
		// Adapter to preserve arbitray XML content (e.g. SAPI)
		// in our Step contents.  Note that using this converter
		// means there MUST be a well formed xml-fragment 
		// (with a root element) contained in the Step.
		
		// See:
		// http://thread.gmane.org/gmane.comp.java.xstream.user/3911
		
		public boolean canConvert(Class clazz) {
			return clazz.equals(Step.class);
		}
		public void marshal(Object value, HierarchicalStreamWriter writer,
					MarshallingContext context) {
			// Enclosing "<Step></Step>" tags are already done for us.
			Step step = (Step) value;
			writer.addAttribute("type", step.getType());
			// We want to preserve XML content in the step.text, without escaping.
			// So we need to parse it into a hierarchy and include that hierarchy
			// in the output stream.
			
			String			stepTextXML = step.getText();
			System.out.println("Attempting to preserve stepTextXML: {" + stepTextXML + "}");
			HierarchicalStreamWriter underlyingWriter = writer.underlyingWriter();
			if (underlyingWriter instanceof Dom4JWriter) {
				Dom4JWriter		d4jWriter = (Dom4JWriter) underlyingWriter;
				// Hacked helper object lets us access a protectedMethod.
				Element 	stepElement = (Element) 
						CogcharXStreamHelper.fetchCurrentWriterObject(d4jWriter);
			/*	// Let the hacking begin!!! We create a child of the step in order to
				// get at the step, then delete the child.  A dirty trick!
				Element		stepDummyChild =  (Element) d4jWriter.createNode("DUMMY");
				Element 	stepElement = stepDummyChild.getParent();
				stepElement.remove(stepDummyChild);
			*/	
				String		wrappedText = "<root>" + stepTextXML + "</root>";
				Document 	parsedDocument = null;
				try {
					parsedDocument = DocumentHelper.parseText(wrappedText);
				} catch (DocumentException de) {
					de.printStackTrace();
					return;
				}
		        Element		parsedElement = parsedDocument.getRootElement();
		        List  	nodes = parsedElement.content();
		        ArrayList<Node>	nodesCopy = new ArrayList<Node>(nodes);
		        for (Node n : nodesCopy) {
		        	n.detach();
		        	System.out.println("Copying node: " + n);
		        	stepElement.add(n);
		        }
			} else {
				// General version, does not assume Dom4J is in use.
				// This partly works, but drops portions of mixed content, because
				// the XStream 1.3 hierarchy model is not fully general.

				StringReader	stringReader = new StringReader(stepTextXML);
				XppReader xppReader = new XppReader(stringReader);
				
				HierarchicalStreamCopier hierarchyCopier = 
						new HierarchicalStreamCopier();
				hierarchyCopier.copy(xppReader, writer);
			}
		}
		public Object unmarshal(HierarchicalStreamReader reader,
					UnmarshallingContext context) {
			Step step = new Step();
			String typeValue = reader.getAttribute("type");
			step.setType(typeValue);

			HierarchicalStreamReader underlyingReader = reader.underlyingReader();
			if (underlyingReader instanceof Dom4JReader) {
				// This should handle mixed content, assuming there is an underlying
				// dom4j reader impl.
				// It works even if there is no parent tag inside the step, e.g.
				// <Step>we love dom4j<tag/>peace baby</Step> is fine.
				Dom4JReader d4jReader = (Dom4JReader) underlyingReader;
				Element underlyingStepElement = (Element) d4jReader.getCurrent();
				String underlyingXML = underlyingStepElement.asXML();
				// This XML is correct, except that it contains the enclosing <Step> </Step>
				// tagset.  
				int	openGT = underlyingXML.indexOf('>');
				int closeLT = underlyingXML.lastIndexOf('<');
				int contentStartIndex = openGT + 1;
				int contentEndIndex = closeLT;
				String extractedContent;
				if (contentStartIndex < contentEndIndex) {
					extractedContent = underlyingXML.substring(contentStartIndex, contentEndIndex);
				} else {
					// Step tag must be empty - <Step/> (not even <Step> </Step>).
					extractedContent = null;
				}
				// System.out.println("Extracted step underlying XML content: " + extractedContent);
				step.setText(extractedContent);
			} else {	
				// General version, does not assume Dom4J.
				// This works for some XML, but does not handle mixed content.
				reader.moveDown();
				StringWriter stringWriter = new StringWriter();
				PrettyPrintWriter	ppw = new PrettyPrintWriter(stringWriter, "\t");
				// CompactWriter compactWriter = new CompactWriter(stringWriter);
				HierarchicalStreamCopier hierarchyCopier = 
						new HierarchicalStreamCopier();
				hierarchyCopier.copy(reader, ppw);
				String stepText = stringWriter.toString();
				step.setText(stepText);
				reader.moveUp();
			}
			return step;
		}
	}
    public Step copy(){
        Step s = new Step();
        s.type = type;
        s.setText(new String(myText));
        return s;
    }
}
