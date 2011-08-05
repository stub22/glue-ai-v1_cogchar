/*
 * Parameter.java
 * 
 * Created on Dec 7, 2007, 11:49:10 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.ancient.utility;

/**
 *
 * @author josh
 */
public class Parameter {
    public Parameter(String name, String value) {
        m_name = name;
        m_value = value;
    }
    
    public Parameter(String name) {
        m_name = name;
        m_children = new Parameters();
    }

    public boolean hasChildren() { return m_children != null; }
    public Parameters getChildren() { return m_children; }

    public  String getName() { return m_name; }
    public  String getValue() { return m_value; }

    public void setName(String newName) { m_name = newName; }
    public void setValue(String newVal) { m_value = newVal; }
    
    public void createChildren() { if ( m_children == null ) { m_children = new Parameters(); } }

    private String m_name;
    private String m_value;
    private Parameters m_children;
}
