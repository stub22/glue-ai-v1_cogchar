/*
 * ParametersBuilder.java
 * 
 * Created on Dec 7, 2007, 2:28:00 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.ancient.utility;

import java.io.File;
import java.util.ArrayList;
// import javax.xml.parsers.FactoryConfigurationError;
// import javax.xml.parsers.ParserConfigurationException;
// import javax.xml.parsers.SAXParser;
// import javax.xml.parsers.SAXParserFactory;
// import org.xml.sax.Attributes;
// import org.xml.sax.SAXException;

/**
 *
 * @author josh
 */
public class ParametersBuilder {
    
    
    static public Parameters parseXMLParameters(File paramFile) {
		/*  javax.parsers above is causinig OSGi troubles.
        SAXParser parser = null;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
        } catch (FactoryConfigurationError e) {
            System.out.println("Factory error: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            System.out.println("Parser error: " + e.getMessage());
        } catch (SAXException e) {
            System.out.println("SAX error: " + e.getMessage());
        }
        pParser handler = new pParser();
        try {
            parser.parse(paramFile, handler);
        } catch (SAXException ex) {
            System.out.println("Parse error: " + ex.getMessage());
        } catch (Exception ex) {
            System.err.println("SaxMenuFactory.loadMenus(): " + ex.getClass().getName() + ex.getMessage());
            ex.printStackTrace();
        }
        return handler.getParams();
		*/
		return null;
		 
    }
		
		 
}

class pParser extends org.xml.sax.helpers.DefaultHandler {
    public pParser() {
        m_stack = new ArrayList();
    }

    public Parameters getParams() { return m_params; }

    public void startDocument() {
    }

    public void endElement(String namespaceURI, String localName, String qName) {
        if ( ! m_stack.isEmpty() ) {
            m_stack.remove(m_stack.size()-1);
        }
    }
/*
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        // handling parameters 
        if ( ! m_stack.isEmpty() ) {
            if ( atts.getIndex("value") != -1 ) {
                getTop().setParam(qName, atts.getValue("value"));
            } else {
                getTop().setParam(qName);
            }
            m_stack.add(getTop().getParam(qName));
        }

        if ( localName.equals("hriParameters") || qName.equals("hriParameters") ) {
            m_params = new Parameters();
            m_stack.add(m_params);
            return;
        }
    }
*/
    private Parameters getTop() {
        Object o = m_stack.get(m_stack.size()-1);
        Parameters ret = null;
        try {
            ret = (Parameters)o;
        } catch (ClassCastException e) {
            Parameter p = (Parameter)o;
            ret = p.getChildren();
            if ( ret == null ) {
                p.createChildren();
                ret = p.getChildren();
            }
        }

        return ret;
    }

    private String m_lastName;
    private ArrayList m_stack;
    private Parameters m_params;
}
