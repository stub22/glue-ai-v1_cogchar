/*
 * Parameters.java
 * 
 * Created on Dec 7, 2007, 11:48:59 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cogchar.zzz.ancient.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author josh
 */
public class Parameters {
    public Parameters()
    {
        m_params = new HashMap();
    }

    public boolean hasParam(String name) {
        return m_params.containsKey(name);
    }

    public Parameter getParam(String name) {
        return (Parameter)m_params.get(name);
    }
	
	public String getChildValue(String name) {
		String result = null;
		Parameter p = getParam(name);
		if (p != null) {
			result = p.getValue();
		}
		return result;
	}
	public String getDescendantValue(String names[]) {
		String result = null;
		if (names.length==1) {
			return getChildValue(names[0]);
		} else {
			Parameter d = getParam(names[0]);
			if (d != null) {
				Parameters dd = d.getChildren();
				if (dd != null) {
					String subNames[] = new String[names.length -1];
					for (int i=1; i < names.length; i++) {
						subNames[i-1] = names[i];
					}
					result = dd.getDescendantValue(subNames);
				}
			}
		}
		return result;
	}

    public void setParam(String name, String value) {
        m_params.put(name, new Parameter(name, value));
    }

    public void setParam(String name) {
        m_params.put(name, new Parameter(name));
    }

    public Iterator getIterator() {
        return m_params.values().iterator();
    }

    public String toString() {
        return toString(new ArrayList());
    }
    
    private String toString(ArrayList prefix) {
        String answer = new String();
        String preString = genPrefix(prefix);
        
        Iterator pIter = getIterator();
        while ( pIter.hasNext() ) {
            Parameter p = (Parameter)pIter.next();
            if ( p.hasChildren() ) {
                prefix.add(p.getName());
                answer += p.getChildren().toString(prefix);
                prefix.remove(prefix.size()-1);
                if ( p.getValue() != null ) {
                    if ( ! preString.isEmpty() ) {
                        answer += preString + '.';
                    }
                    answer += p.getName() + ':' + p.getValue() + '\n';
                }
            } else {
                if ( ! preString.isEmpty() ) {
                    answer += preString + '.';
                }
                answer += p.getName();
                if ( p.getValue() != null ) {
                     answer += ':' + p.getValue() + '\n';
                }
            }
        }
        
        return answer;
    }
    
    private String genPrefix(ArrayList prefix) {
        String answer = new String();
        
        Iterator sIter = prefix.iterator();
        while ( sIter.hasNext() ) {
            String t = (String)sIter.next();
            if ( ! answer.isEmpty() ) {
                answer += '.';
            }
            answer += t;
        }
        
        return answer;
    }
    
    private HashMap m_params;
}
